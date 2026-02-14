package com.menac1ngmonkeys.monkeyslimit.viewmodel

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Budgets
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Categories
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.TransactionType
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Transactions
import com.menac1ngmonkeys.monkeyslimit.data.remote.ApiConfig
import com.menac1ngmonkeys.monkeyslimit.data.remote.response.ClassifyRequest
import com.menac1ngmonkeys.monkeyslimit.data.repository.BudgetsRepository
import com.menac1ngmonkeys.monkeyslimit.data.repository.CategoriesRepository
import com.menac1ngmonkeys.monkeyslimit.data.repository.TransactionsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.net.URLDecoder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern
import kotlin.math.max

data class ReviewItemUi(
    val id: Int,
    val name: String,
    val categoryId: Int,
    val budgetId: Int,
    val quantity: Int,
    val pricePerUnit: Double
)

data class LocalParsedItem(
    val name: String,
    val qty: Int,
    val price: Double
)

data class ReviewTransactionUiState(
    val budgets: List<Budgets> = emptyList(),
    val categories: List<Categories> = emptyList(),
    val imageUri: String? = null,
    val detectedDate: Date? = null,
    val detectedItems: List<ReviewItemUi> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false
)

class ReviewTransactionViewModel(
    private val transactionsRepository: TransactionsRepository,
    private val budgetsRepository: BudgetsRepository,
    private val categoriesRepository: CategoriesRepository
) : ViewModel() {

    private val _imageUri = MutableStateFlow<String?>(null)
    private val _isSaving = MutableStateFlow(false)
    private val _detectedDate = MutableStateFlow<Date?>(null)
    private val _detectedItems = MutableStateFlow<List<ReviewItemUi>>(emptyList())
    private val _isAnalyzing = MutableStateFlow(false)

    val uiState: StateFlow<ReviewTransactionUiState> = combine(
        budgetsRepository.getAllBudgets(),
        categoriesRepository.getAllCategories(),
        _imageUri,
        _isSaving,
        _detectedDate,
        _detectedItems,
        _isAnalyzing
    ) { args ->
        @Suppress("UNCHECKED_CAST") val budgets = args[0] as List<Budgets>
        @Suppress("UNCHECKED_CAST") val categories = args[1] as List<Categories>
        val imageUri = args[2] as String?
        val isSaving = args[3] as Boolean
        val date = args[4] as Date?
        @Suppress("UNCHECKED_CAST") val items = args[5] as List<ReviewItemUi>
        val isAnalyzing = args[6] as Boolean

        ReviewTransactionUiState(
            budgets = budgets,
            categories = categories,
            imageUri = imageUri,
            isLoading = isAnalyzing,
            isSaving = isSaving,
            detectedDate = date,
            detectedItems = items
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ReviewTransactionUiState()
    )

    fun setImageUri(uri: String?) {
        _imageUri.update { uri }
    }

    fun parseReceiptData(context: Context, text: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val startTime = System.currentTimeMillis()
            _isAnalyzing.update { true }
            Log.d("ReviewViewModel", "=== Starting Smart OCR Analysis ===")
            Log.d("ReviewViewModel", "OCR Text: ${text.take(50)}...")

            val decodedText = try { URLDecoder.decode(text, "UTF-8") } catch (e: Exception) { text.replace("+", " ") }

            // 1. EXTRACT DATE & TIME (Keep existing reliable logic)
            Log.d("ReviewViewModel", "Searching for Date/Time...")
            val dateStart = System.currentTimeMillis()
            extractDateAndTime(decodedText)
            Log.d("ReviewViewModel", "Date Detection took: ${System.currentTimeMillis() - dateStart}ms")
            Log.d("ReviewViewModel", "Date Detection Finished: ${_detectedDate.value}")

            // 2. DOCUMENT CLASSIFICATION (Transfer vs Store Receipt)
            val isTransfer = isDigitalTransfer(decodedText)

            if (isTransfer) {
                Log.d("ReviewViewModel", "Document Type: DIGITAL TRANSFER / E-WALLET")
                val transferStart = System.currentTimeMillis()
                processDigitalTransfer(decodedText)
                Log.d("ReviewViewModel", "Transfer Processing took: ${System.currentTimeMillis() - transferStart}ms")
            } else {
                Log.d("ReviewViewModel", "Document Type: STORE RECEIPT")
                val donutStart = System.currentTimeMillis()
                val success = processStoreReceiptWithDonut(context, decodedText)
                Log.d("ReviewViewModel", "Donut/Fallback took: ${System.currentTimeMillis() - donutStart}ms")

                // Fallback to max amount if Donut model fails
                if (!success) {
                    processFallbackAmount(decodedText)
                }
            }

            Log.d("ReviewViewModel", "ANALYSIS COMPLETE. Items detected: ${_detectedItems.value.size}")

            val totalTime = System.currentTimeMillis() - startTime
            Log.d("ReviewViewModel", "TOTAL ANALYSIS TIME: ${totalTime}ms (${totalTime / 1000}s)")
            _isAnalyzing.update { false }
        }
    }

    // ==========================================================================================
    // PARSING LOGIC: DIGITAL TRANSFERS
    // ==========================================================================================

    private fun isDigitalTransfer(text: String): Boolean {
        val lowerText = text.lowercase()

        // 1. STRONG INDICATORS (Exact Phrases)
        // If any of these exact phrases exist, it's 100% a digital transfer/e-wallet receipt.
        val strongTransferKeywords = listOf(
            "transfer successful", "pembayaran berhasil", "transaction successful",
            "rincian transaksi", "detail transaksi", "rincian pembayaran",
            "metode pembayaran", "sumber dana", "beneficiary name", "bayar ke",
            "bukti transaksi yang sah", "source of fund", "reference no", "no. referensi",
            "id transaksi"
        )

        if (strongTransferKeywords.any { lowerText.contains(it) }) {
            return true
        }

        // 2. WEAK INDICATORS (Point-based fallback)
        val weakTransferKeywords = listOf(
            "transfer", "berhasil", "sukses", "success", "pembayaran",
            "top up", "top-up", "ovo", "gopay", "shopeepay", "dana", "linkaja",
            "bca", "mandiri", "jago", "blu", "qris", "saldo", "order id", "merchant"
        )

        val receiptKeywords = listOf(
            "jl.", "jalan", "kasir", "cashier", "meja", "table", "dine in", "take away",
            "tax", "pajak", "pb1", "service charge", "kembali", "tunai", "cash", "resto", "cafe",
            "struk", "receipt"
        )

        var tScore = 0
        var rScore = 0
        weakTransferKeywords.forEach { if (lowerText.contains(it)) tScore++ }
        receiptKeywords.forEach { if (lowerText.contains(it)) rScore++ }

        return tScore >= 3 && tScore > rScore
    }

    private suspend fun processDigitalTransfer(text: String) {
        val lines = text.split('\n').map { it.trim() }.filter { it.isNotEmpty() }
        var maxAmount = 0.0
        var merchantName = ""

        // A. Extract Max Amount (Looks for Rp, IDR, or standard numbers)
        val currencyPattern = Pattern.compile("(?:Rp|IDR)\\s*[-]?\\s*([\\d., ]+)", Pattern.CASE_INSENSITIVE)
        val matcher = currencyPattern.matcher(text)
        while (matcher.find()) {
            val cleanNumber = matcher.group(1)?.replace(" ", "")?.trim() ?: continue
            if (cleanNumber.startsWith("08") && cleanNumber.length >= 10 && !cleanNumber.contains(",")) continue
            val parsedAmount = parseSmartNumber(cleanNumber)
            if (parsedAmount > maxAmount) maxAmount = parsedAmount
        }

        // B. Extract Merchant/Recipient Name using specific markers
        val markerKeywords = listOf(
            "bayar ke", "beneficiary name", "penyedia jasa", "merchant",
            "nama penerima", "dikirim ke", "payment to", "transfer to"
        )

        for (i in lines.indices) {
            val line = lines[i]
            val lowerLine = line.lowercase()

            val marker = markerKeywords.find { lowerLine.contains(it) }
            if (marker != null) {
                // Extracts text right after the marker (e.g. "Bayar Ke: by.U")
                val afterMarker = lowerLine.substringAfter(marker).replace(":", "").trim()
                if (afterMarker.length > 2) {
                    merchantName = line.substring(line.lowercase().indexOf(marker) + marker.length).replace(":", "").trim()
                } else if (i + 1 < lines.size) {
                    // Or if it overflowed to the next line (e.g. BCA Beneficiary Name)
                    merchantName = lines[i + 1]
                }
                break
            }
        }

        // C. Fallback: Find the first clean prominent string at the top of the receipt
        // Useful for OVO, Jago, Tokopedia, blu where the name is at the top without a marker
        if (merchantName.isEmpty()) {
            val ignoreWords = listOf(
                "total", "berhasil", "sukses", "success", "rp", "idr", "struk",
                "transaksi", "tanggal", "waktu", "date", "time", "status", "metode",
                "biaya", "fee", "admin", "pembayaran", "rincian", "detail", "saldo",
                "ovo", "gopay", "shopeepay", "dana", "bca", "mandiri", "jago", "transfer",
                "transaction", "qris", "blu", "nominal", "referensi", "reference"
            )
            for (line in lines) {
                val lower = line.lowercase()
                // Skip lines with long numbers (like accounts/dates) or status lines
                if (lower.matches(Regex(".*\\d{2,}.*")) || ignoreWords.any { lower == it || lower.startsWith(it) }) {
                    continue
                }
                if (line.length > 3) {
                    merchantName = line
                    break
                }
            }
        }

        if (merchantName.isEmpty()) merchantName = "Digital Transfer"

        // Title Case formatting (e.g. "THERESA ADELIA" -> "Theresa Adelia")
        merchantName = merchantName.split(" ").joinToString(" ") {
            it.lowercase().replaceFirstChar { char -> char.uppercase() }
        }

        // D. Classify the Merchant Name
        var categoryId = 0
        try {
            val req = ClassifyRequest(text = merchantName)
            val res = withTimeout(5000) {
                ApiConfig.getApiService().classifyText(req)
            }
            if (res.isSuccessful && res.body()?.ok == true) {
                val catName = res.body()?.data?.prediction ?: ""
                val allCategories = categoriesRepository.getAllCategories().first()
                categoryId = allCategories.firstOrNull { it.name.equals(catName, ignoreCase = true) }?.id ?: 0
            }
        } catch (e: Exception) {
            Log.w("ReviewViewModel", "Classification skipped or timed out. Using default category.")
            categoryId = 1 // Default to 'Uncategorized' or 'Needs'
        }

        Log.d("ReviewViewModel", "Transfer Result: Name='$merchantName', Amount=$maxAmount, CatID=$categoryId")
        val newItem = ReviewItemUi(1, merchantName, categoryId, 0, 1, maxAmount)
        _detectedItems.update { listOf(newItem) }
    }

    // ==========================================================================================
    // PARSING LOGIC: STORE RECEIPTS (DONUT API)
    // ==========================================================================================

    private suspend fun processStoreReceiptWithDonut(context: Context, fallbackText: String): Boolean {
        val uriStr = _imageUri.value ?: return false

        return try {
            val uri = Uri.parse(uriStr)
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
            val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "jpg"
            val mediaType = mimeType.toMediaTypeOrNull()

            val file = getFileFromUri(context, uri, ".$extension") ?: return false
            val requestFile = file.asRequestBody(mediaType)
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

            Log.d("ReviewViewModel", "Calling /predict for Store Receipt...")
            val response = ApiConfig.getApiService().predictReceipt(body)

            if (response.isSuccessful && response.body()?.ok == true) {
                val menu = response.body()?.data?.menu ?: emptyList()
                Log.d("ReviewViewModel", "API RESPONSE: Received ${menu.size} items from Donut.")

                if (menu.isNotEmpty()) {
                    val allCategories = categoriesRepository.getAllCategories().first()

                    val newItems = coroutineScope {
                        menu.mapIndexed { index, ocrItem ->
                            async {
                                Log.d("ReviewViewModel", "Classifying Item [${index+1}]: ${ocrItem.nm}")
                                var matchedCategoryId = 0
                                try {
                                    val req = ClassifyRequest(text = ocrItem.nm)
                                    val res = ApiConfig.getApiService().classifyText(req)
                                    if (res.isSuccessful && res.body()?.ok == true) {
                                        val catName = res.body()?.data?.prediction ?: ""
                                        matchedCategoryId = allCategories.firstOrNull { it.name.equals(catName, ignoreCase = true) }?.id ?: 0
                                        Log.d("ReviewViewModel", "   ↳ Match: $catName (ID: $matchedCategoryId)")
                                    }
                                } catch (e: Exception) {
                                    Log.e("ReviewViewModel", "   ❌ Classification error for ${ocrItem.nm}")
                                }

                                ReviewItemUi(index + 1, ocrItem.nm, matchedCategoryId, 0, ocrItem.qty, ocrItem.price_int / ocrItem.qty)
                            }
                        }.awaitAll()
                    }

                    _detectedItems.update { newItems }
                    Log.d("ReviewViewModel", "Donut Processing Finished Successfully.")
                    true
                } else {
                    Log.w("ReviewViewModel", "API RESPONSE: OK but 'menu' was empty.")
                    false
                }
            } else {
                Log.e("ReviewViewModel", "API ERROR: ${response.code()} - ${response.message()}")
                false
            }
        } catch (e: Exception) {
            Log.e("ReviewViewModel", "Donut OCR Failed", e)
            false
        }
    }

    // ==========================================================================================
    // FALLBACK LOGIC
    // ==========================================================================================

    private fun processFallbackAmount(text: String) {
        Log.d("ReviewViewModel", "Fallback triggered: Getting overall Max Amount.")
        val currencyPattern = Pattern.compile("(?:Rp|IDR)\\s*[-]?\\s*([\\d., ]+)", Pattern.CASE_INSENSITIVE)
        val matcher = currencyPattern.matcher(text)
        var maxAmount = 0.0

        while (matcher.find()) {
            val cleanNumber = matcher.group(1)?.replace(" ", "")?.trim() ?: continue
            if (cleanNumber.startsWith("08") && cleanNumber.length >= 10 && !cleanNumber.contains(",")) continue

            val parsedAmount = parseSmartNumber(cleanNumber)
            if (parsedAmount > maxAmount) maxAmount = parsedAmount
        }

        if (maxAmount > 0) {
            Log.d("ReviewViewModel", "Fallback Found Amount: $maxAmount")
            val newItem = ReviewItemUi(1, "Scanned Receipt", 0, 0, 1, maxAmount)
            _detectedItems.update { listOf(newItem) }
        } else {
            Log.w("ReviewViewModel", "Fallback found nothing.")
        }
    }

    // ==========================================================================================
    // HELPER FUNCTIONS
    // ==========================================================================================

    private fun extractDateAndTime(decodedText: String) {
        var detectedDateObj: Date? = null
        val datePattern = Pattern.compile("(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})|(\\d{4}[/-]\\d{1,2}[/-]\\d{1,2})|(\\d{1,2}[\\s\\.\\-\\/]+[a-zA-Z]{3,}[\\s\\.\\-\\/]+\\d{2,4})", Pattern.CASE_INSENSITIVE)
        val dateMatcher = datePattern.matcher(decodedText)

        if (dateMatcher.find()) {
            val cleanDate = dateMatcher.group().replace(Regex("[\\.\\-\\/,]"), " ").replace(Regex("\\s+"), " ").trim()
            val formats = listOf("d MMM yyyy", "dd MMM yyyy", "d MMMM yyyy", "dd MMMM yyyy", "d MMM yy", "dd MMM yy", "d M yyyy", "dd MM yyyy", "yyyy MM dd")
            val locales = listOf(Locale.US, Locale("id", "ID"))
            outerLoop@ for (locale in locales) {
                for (fmt in formats) {
                    try {
                        val parser = SimpleDateFormat(fmt, locale).apply { isLenient = false }
                        detectedDateObj = parser.parse(cleanDate)
                        if (detectedDateObj != null) break@outerLoop
                    } catch (e: Exception) { }
                }
            }
        }

        val timePattern = Pattern.compile("\\b([01]?\\d|2[0-3])[:.]([0-5]\\d)(?:[:.]([0-5]\\d))?\\b")
        val timeMatcher = timePattern.matcher(decodedText)
        var timeFound = false
        var detectedHour = 0
        var detectedMinute = 0

        if (timeMatcher.find()) {
            try {
                detectedHour = timeMatcher.group(1)?.toInt() ?: 0
                detectedMinute = timeMatcher.group(2)?.toInt() ?: 0
                timeFound = true
            } catch (e: Exception) { }
        }

        if (detectedDateObj != null) {
            if (timeFound) {
                val cal = Calendar.getInstance().apply { time = detectedDateObj!! }
                cal.set(Calendar.HOUR_OF_DAY, detectedHour)
                cal.set(Calendar.MINUTE, detectedMinute)
                detectedDateObj = cal.time
            }
            _detectedDate.update { detectedDateObj }
        }
    }

    private fun getFileFromUri(context: Context, uri: Uri, extension: String): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val tempFile = File.createTempFile("upload_", extension, context.cacheDir)
            tempFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            tempFile
        } catch (e: Exception) { null }
    }

    private fun parseSmartNumber(numberStr: String): Double {
        try {
            val lastComma = numberStr.lastIndexOf(',')
            val lastDot = numberStr.lastIndexOf('.')

            if (lastComma != -1 && lastDot != -1) {
                if (lastDot > lastComma) return numberStr.replace(",", "").toDouble()
                else return numberStr.replace(".", "").replace(",", ".").toDouble()
            }
            if (lastDot != -1) {
                return if (numberStr.length - lastDot - 1 == 3) numberStr.replace(".", "").toDouble() else numberStr.toDouble()
            }
            if (lastComma != -1) {
                return if (numberStr.length - lastComma - 1 == 3) numberStr.replace(",", "").toDouble() else numberStr.replace(",", ".").toDouble()
            }
            return numberStr.toDouble()
        } catch (e: Exception) { return 0.0 }
    }

    fun saveTransaction(
        context: Context, date: Date, reviewItems: List<ReviewItemUi>, type: TransactionType, onSuccess: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isSaving.update { true }
                val permanentImagePath = _imageUri.value?.let { saveImageToGallery(context, it) }

                val calendar = Calendar.getInstance().apply { time = date }
                val timeOfDay = when (calendar.get(Calendar.HOUR_OF_DAY)) {
                    in 5..11 -> "Morning"
                    in 12..16 -> "Afternoon"
                    in 17..20 -> "Evening"
                    else -> "Night"
                }

                reviewItems.forEach { item ->
                    val action = if (type == TransactionType.EXPENSE) "Paid for" else "Received from"
                    val qtyString = if (item.quantity > 1) "${item.quantity}x " else ""
                    val personalizedNote = "$qtyString${item.name}"

                    val validCategoryId = if (item.categoryId > 0) item.categoryId else 1
                    val validBudgetId = if (type == TransactionType.EXPENSE && item.budgetId > 0) item.budgetId else null

                    val newTransaction = Transactions(
                        id = 0,
                        date = date,
                        totalAmount = item.pricePerUnit * item.quantity,
                        note = personalizedNote,
                        imagePath = permanentImagePath,
                        budgetId = validBudgetId,
                        categoryId = validCategoryId,
                        type = type
                    )

                    transactionsRepository.insert(newTransaction)
                }

                launch(Dispatchers.Main) {
                    _isSaving.update { false }
                    onSuccess()
                }

            } catch (e: Exception) {
                Log.e("ReviewViewModel", "CRITICAL ERROR saving transaction", e)
                launch(Dispatchers.Main) { _isSaving.update { false } }
            }
        }
    }

    private fun saveImageToGallery(context: Context, cacheUriString: String): String? {
        try {
            val cacheUri = cacheUriString.toUri()
            val inputStream = context.contentResolver.openInputStream(cacheUri) ?: return null
            val name = "trans_${System.currentTimeMillis()}.jpg"
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/MonkeysLimit")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues) ?: return null
            resolver.openOutputStream(uri)?.use { outputStream -> inputStream.copyTo(outputStream) }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }
            return uri.toString()
        } catch (e: Exception) { return null }
    }
}