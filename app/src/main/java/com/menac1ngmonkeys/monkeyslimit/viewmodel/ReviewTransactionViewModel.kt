package com.menac1ngmonkeys.monkeyslimit.viewmodel

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Budgets
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Categories
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.TransactionType
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Transactions
import com.menac1ngmonkeys.monkeyslimit.data.repository.BudgetsRepository
import com.menac1ngmonkeys.monkeyslimit.data.repository.CategoriesRepository
import com.menac1ngmonkeys.monkeyslimit.data.repository.TransactionsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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

    val uiState: StateFlow<ReviewTransactionUiState> = combine(
        budgetsRepository.getAllBudgets(),
        categoriesRepository.getAllCategories(),
        _imageUri,
        _isSaving,
        _detectedDate,
        _detectedItems
    ) { args ->
        @Suppress("UNCHECKED_CAST")
        val budgets = args[0] as List<Budgets>
        @Suppress("UNCHECKED_CAST")
        val categories = args[1] as List<Categories>
        val imageUri = args[2] as String?
        val isSaving = args[3] as Boolean
        val date = args[4] as Date?
        @Suppress("UNCHECKED_CAST")
        val items = args[5] as List<ReviewItemUi>

        ReviewTransactionUiState(
            budgets = budgets,
            categories = categories,
            imageUri = imageUri,
            isLoading = false,
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

    /**
     * INTELLIGENT PARSING (Decoupled Date & Time)
     */
    fun parseReceiptText(text: String) {
        viewModelScope.launch {
            // 1. CLEANUP
            val decodedText = try {
                URLDecoder.decode(text, "UTF-8")
            } catch (e: Exception) {
                text.replace("+", " ")
            }

            Log.d("ReviewViewModel", "=== OCR Analysis Start ===")
            Log.d("ReviewViewModel", "Full Cleaned Text:\n$decodedText")

            // =================================================================================
            // 2a. EXTRACT DATE (Independent)
            // =================================================================================
            var detectedDateObj: Date? = null

            // Matches: 03/09/2025, 2025-09-03, 03 Sep 2025
            val datePattern = Pattern.compile(
                "(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})|" +
                        "(\\d{4}[/-]\\d{1,2}[/-]\\d{1,2})|" +
                        "(\\d{1,2}[\\s\\.\\-\\/]+[a-zA-Z]{3,}[\\s\\.\\-\\/]+\\d{2,4})",
                Pattern.CASE_INSENSITIVE
            )
            val dateMatcher = datePattern.matcher(decodedText)

            if (dateMatcher.find()) {
                val rawDate = dateMatcher.group()
                val cleanDate = rawDate.replace(Regex("[\\.\\-\\/,]"), " ").replace(Regex("\\s+"), " ").trim()

                val formats = listOf(
                    "d MMM yyyy", "dd MMM yyyy", "d MMMM yyyy", "dd MMMM yyyy",
                    "d MMM yy", "dd MMM yy",
                    "d M yyyy", "dd MM yyyy", "yyyy MM dd", "dd MM yyyy", "yyyy MM dd"
                )
                val locales = listOf(Locale.US, Locale("id", "ID"))

                outerLoop@ for (locale in locales) {
                    for (fmt in formats) {
                        try {
                            val parser = SimpleDateFormat(fmt, locale)
                            parser.isLenient = false
                            detectedDateObj = parser.parse(cleanDate)
                            if (detectedDateObj != null) {
                                Log.d("ReviewViewModel", "Date Found: $detectedDateObj")
                                break@outerLoop
                            }
                        } catch (e: Exception) { }
                    }
                }
            }

            // =================================================================================
            // 2b. EXTRACT TIME (Independent)
            // =================================================================================
            // Matches: 16:48, 09:09, 22:09:27 (Colon or Dot separator)
            val timePattern = Pattern.compile("\\b([01]?\\d|2[0-3])[:.]([0-5]\\d)(?:[:.]([0-5]\\d))?\\b")
            val timeMatcher = timePattern.matcher(decodedText)

            var detectedHour = 0
            var detectedMinute = 0
            var timeFound = false

            if (timeMatcher.find()) {
                try {
                    detectedHour = timeMatcher.group(1)?.toInt() ?: 0
                    detectedMinute = timeMatcher.group(2)?.toInt() ?: 0
                    Log.d("ReviewViewModel", "Time Found: $detectedHour:$detectedMinute")
                    timeFound = true
                } catch (e: Exception) {
                    Log.w("ReviewViewModel", "Time parse error")
                }
            }

            // =================================================================================
            // 2c. MERGE DATE & TIME
            // =================================================================================
            if (detectedDateObj != null) {
                if (timeFound) {
                    val cal = Calendar.getInstance()
                    cal.time = detectedDateObj
                    cal.set(Calendar.HOUR_OF_DAY, detectedHour)
                    cal.set(Calendar.MINUTE, detectedMinute)
                    // Keep seconds 0 if not found, or add logic to capture group(3) above if needed
                    detectedDateObj = cal.time
                }
                _detectedDate.update { detectedDateObj }
            }

            // =================================================================================
            // 3. EXTRACT AMOUNT (Existing Logic)
            // =================================================================================
            val currencyPattern = Pattern.compile(
                "(?:Rp|IDR)\\s*[-]?\\s*([\\d., ]+)",
                Pattern.CASE_INSENSITIVE
            )
            val matcher = currencyPattern.matcher(decodedText)
            var maxAmount = 0.0

            while (matcher.find()) {
                val rawNumber = matcher.group(1) ?: continue
                val cleanNumber = rawNumber.replace(" ", "").trim()

                if (cleanNumber.startsWith("08") && cleanNumber.length >= 10 && !cleanNumber.contains(",")) {
                    continue
                }

                val start = max(0, matcher.start() - 30)
                val end = matcher.start()
                val contextBefore = decodedText.substring(start, end).lowercase()

                val invalidKeywords = listOf("saldo", "balance", "sisa", "limit", "awal", "akhir")
                if (invalidKeywords.any { contextBefore.contains(it) }) {
                    continue
                }

                val parsedAmount = parseSmartNumber(cleanNumber)
                if (parsedAmount > maxAmount) {
                    maxAmount = parsedAmount
                }
            }

            if (maxAmount > 0) {
                val name = if (maxAmount > 500000) "Transfer" else "Scanned Receipt"
                val newItem = ReviewItemUi(
                    id = 1,
                    name = name,
                    categoryId = 0,
                    budgetId = 0,
                    quantity = 1,
                    pricePerUnit = maxAmount
                )
                _detectedItems.update { listOf(newItem) }
            }
        }
    }

    private fun parseSmartNumber(numberStr: String): Double {
        try {
            val lastComma = numberStr.lastIndexOf(',')
            val lastDot = numberStr.lastIndexOf('.')

            if (lastComma != -1 && lastDot != -1) {
                if (lastDot > lastComma) {
                    return numberStr.replace(",", "").toDouble() // US
                } else {
                    return numberStr.replace(".", "").replace(",", ".").toDouble() // Indo
                }
            }

            if (lastDot != -1) {
                val decimals = numberStr.length - lastDot - 1
                return if (decimals == 3) {
                    numberStr.replace(".", "").toDouble()
                } else {
                    numberStr.toDouble()
                }
            }

            if (lastComma != -1) {
                val decimals = numberStr.length - lastComma - 1
                return if (decimals == 3) {
                    numberStr.replace(",", "").toDouble()
                } else {
                    numberStr.replace(",", ".").toDouble()
                }
            }

            return numberStr.toDouble()
        } catch (e: Exception) {
            return 0.0
        }
    }

    fun saveTransaction(
        context: Context,
        date: Date,
        reviewItems: List<ReviewItemUi>,
        type: TransactionType,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _isSaving.update { true }

            var permanentImagePath: String? = null
            val currentImageUri = _imageUri.value

            if (currentImageUri != null) {
                permanentImagePath = saveImageToGallery(context, currentImageUri)
            }

            reviewItems.forEach { item ->
                val lineItemTotal = item.pricePerUnit * item.quantity
                val finalBudgetId = if (type == TransactionType.EXPENSE) item.budgetId else null

                val newTransaction = Transactions(
                    id = 0,
                    date = date,
                    totalAmount = lineItemTotal,
                    note = if (item.quantity > 1) "${item.name} (x${item.quantity})" else item.name,
                    imagePath = permanentImagePath,
                    budgetId = finalBudgetId,
                    categoryId = item.categoryId,
                    type = type
                )

                transactionsRepository.insert(newTransaction)
            }

            _isSaving.update { false }
            launch(Dispatchers.Main) { onSuccess() }
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
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                ?: return null

            resolver.openOutputStream(uri)?.use { outputStream ->
                inputStream.copyTo(outputStream)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }

            return uri.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}