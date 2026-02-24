package com.menac1ngmonkeys.monkeyslimit.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Members
import com.menac1ngmonkeys.monkeyslimit.data.remote.ApiConfig
import com.menac1ngmonkeys.monkeyslimit.data.remote.response.OcrItem
import com.menac1ngmonkeys.monkeyslimit.ui.state.DraftItem
import com.menac1ngmonkeys.monkeyslimit.ui.state.DraftMember
import com.menac1ngmonkeys.monkeyslimit.ui.state.ReviewSmartSplitUiState
import com.menac1ngmonkeys.monkeyslimit.ui.state.SmartSplitDraft
import com.menac1ngmonkeys.monkeyslimit.ui.state.SmartSplitItemUi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class ReviewSmartSplitViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewSmartSplitUiState())
    val uiState = _uiState.asStateFlow()

    init {
        addMember("You")
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun scanReceipt(context: Context, imageUri: Uri) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // --- STEP 1: PREPARE FILE FOR API (EXIF + Compression) ---
                val file = prepareImageFile(context, imageUri)

                if (file == null) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to process image") }
                    return@launch
                }

                val mediaType = "image/jpeg".toMediaTypeOrNull()
                val requestFile = file.asRequestBody(mediaType)
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

                // --- STEP 2: CALL BACKEND API ---
                Log.d("SmartSplitViewModel", "Calling /predict for Smart Split...")
                val response = ApiConfig.getApiService().predictReceipt(body)

                if (response.isSuccessful && response.body()?.ok == true) {
                    val data = response.body()?.data

                    // --- STEP 3: AI-PROOF JSON PARSING ---
                    val menuElement = data?.menu
                    val menuList = mutableListOf<OcrItem>()

                    if (menuElement != null) {
                        try {
                            val gson = Gson()
                            if (menuElement.isJsonArray) {
                                // The AI behaved nicely and returned a List
                                val array = gson.fromJson(menuElement, Array<OcrItem>::class.java)
                                menuList.addAll(array)
                            } else if (menuElement.isJsonObject) {
                                // The AI hallucinated and returned a single Object
                                val singleItem = gson.fromJson(menuElement, OcrItem::class.java)
                                menuList.add(singleItem)
                            }
                        } catch (e: Exception) {
                            Log.e("SmartSplitViewModel", "Failed to parse AI menu JSON", e)
                        }
                    }

                    Log.d("SmartSplitViewModel", "API RESPONSE: Parsed ${menuList.size} items safely.")

                    val newItems = menuList.mapIndexed { index, item ->
                        val safeQty = if (item.qty > 0) item.qty else 1
                        val unitPrice = item.price_int / safeQty

                        SmartSplitItemUi(
                            id = index + 1,
                            name = item.nm ?: "Unknown Item",
                            price = unitPrice,
                            quantity = safeQty,
                            assignedMemberIds = emptyList()
                        )
                    }

                    val tax = parsePrice(data?.subTotal?.taxPrice)
                    val service = parsePrice(data?.subTotal?.servicePrice)
                    val discount = parsePrice(data?.subTotal?.discountPrice)

                    _uiState.update {
                        it.copy(
                            items = newItems,
                            tax = tax,
                            service = service,
                            discount = discount,
                            isLoading = false
                        )
                    }
                } else {
                    Log.e("SmartSplitViewModel", "Predict Failed: ${response.errorBody()?.string()}")
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to analyze receipt") }
                }
            } catch (e: Exception) {
                Log.e("SmartSplitViewModel", "Predict Error", e)
                val errorMsg = if (e is JsonSyntaxException || e.message?.contains("BEGIN_OBJECT") == true) {
                    "Not a valid bill"
                } else {
                    "Error connecting to server: ${e.localizedMessage}"
                }
                _uiState.update { it.copy(isLoading = false, errorMessage = errorMsg) }
            }
        }
    }

    // --- IMAGE PREPARATION LOGIC ---

    private fun prepareImageFile(context: Context, uri: Uri): File? {
        return try {
            // 1. GUARANTEE FIX: Copy the raw URI stream to a physical file FIRST.
            // This forces Android to fully write the EXIF headers to disk where we can read them.
            val tempRawFile = File.createTempFile("raw_upload_", ".jpg", context.cacheDir)
            context.contentResolver.openInputStream(uri)?.use { input ->
                tempRawFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            // 2. Read EXIF from the PHYSICAL file path (100% Reliable)
            val exif = ExifInterface(tempRawFile.absolutePath)
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            val rotation = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }

            // 3. Load the Bitmap from the physical file
            val originalBitmap = BitmapFactory.decodeFile(tempRawFile.absolutePath) ?: return null

            // 4. Physically rotate the image upright
            var finalBitmap = originalBitmap
            if (rotation != 0f) {
                val matrix = Matrix().apply { postRotate(rotation) }
                finalBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true)
            }

            // 5. Shrink the dimensions to mimic WhatsApp (Orientation Aware)
            // We use isLandscape to ensure we don't accidentally squash a wide image into a tall box
            val isLandscape = finalBitmap.width > finalBitmap.height
            val maxWidth = if (isLandscape) 1280f else 960f
            val maxHeight = if (isLandscape) 960f else 1280f

            var width = finalBitmap.width.toFloat()
            var height = finalBitmap.height.toFloat()

            if (width > maxWidth || height > maxHeight) {
                val ratio = minOf(maxWidth / width, maxHeight / height)
                width *= ratio
                height *= ratio
            }

            finalBitmap = Bitmap.createScaledBitmap(finalBitmap, width.toInt(), height.toInt(), true)

            // 6. Compress to ~100KB (90% JPEG) and overwrite the temp file
            tempRawFile.outputStream().use { out ->
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }

            val finalSizeKB = tempRawFile.length() / 1024
            Log.d("SmartSplitViewModel", "🚀 FINAL COMPRESSED UPLOAD SIZE: $finalSizeKB KB")

            tempRawFile
        } catch (e: Exception) {
            Log.e("SmartSplitViewModel", "Image Prep Error", e)
            null
        }
    }

    // --- VALIDATION LOGIC (Copied from ScanTransactionViewModel) ---

    private fun isInvalidContent(text: String): Boolean {
        val lowerText = text.lowercase()
        val codeSymbols = listOf("fun ", "val ", "var ", "import ", "package ", "class ", "return", "//", "/*")
        val isCode = codeSymbols.count { lowerText.contains(it) } >= 2
        return isCode
    }

    private fun isValidDocument(text: String): Boolean {
        val lowerText = text.lowercase()
        var score = 0

        val blockList = listOf(
            "add a new budget", "ai recommendation", "analytics", "split bill",
            "budgeted", "left", "spent", "sort", "kb/s", "4g", "wifi"
        )
        if (blockList.any { lowerText.contains(it) }) return false

        val strongKeywords = listOf(
            "berhasil", "success", "successful", "selesai", "paid", "lunas",
            "id transaksi", "transaction id", "no. ref", "reference", "struk", "receipt",
            "invoice", "order id", "kode bayar", "payment to", "merchant"
        )
        strongKeywords.forEach { if (lowerText.contains(it)) score += 2 }

        val weakKeywords = listOf(
            "total", "subtotal", "jumlah", "amount", "bayar", "cash", "tunai",
            "kembali", "change", "tax", "pajak", "admin", "fee", "biaya"
        )
        weakKeywords.forEach { if (lowerText.contains(it)) score += 1 }

        val currencyRegex = Regex("(rp|idr)\\s*[\\d\\.,]+", RegexOption.IGNORE_CASE)
        if (currencyRegex.containsMatchIn(text)) score += 2

        val dateRegex = Regex("\\d{1,4}[/-]\\d{1,2}[/-]\\d{2,4}|\\d{1,2}\\s+[a-z]{3,}", RegexOption.IGNORE_CASE)
        if (dateRegex.containsMatchIn(text)) score += 1

        Log.d("SmartSplit", "Document Validation Score: $score")
        return score >= 4
    }

    // --- HELPERS ---

    private fun parsePrice(priceStr: String?): Double {
        if (priceStr.isNullOrBlank()) return 0.0
        return try {
            val clean = priceStr.replace(",", "").replace(".", "")
            clean.toDouble()
        } catch (e: Exception) {
            0.0
        }
    }

    // ... (Existing State Management Functions) ...
    fun setSplitEvenly(enable: Boolean) {
        _uiState.update { state ->
            if (enable) {
                val allMemberIds = state.availableMembers.map { it.id }
                val updatedItems = state.items.map { item -> item.copy(assignedMemberIds = allMemberIds) }
                state.copy(isSplitEvenly = true, items = updatedItems)
            } else {
                state.copy(isSplitEvenly = false)
            }
        }
    }
    fun setSplitName(name: String) { _uiState.update { it.copy(splitName = name) } }
    fun selectMemberForAssignment(memberId: Int?) {
        _uiState.update { state -> state.copy(selectedMemberIdForAssignment = if (state.selectedMemberIdForAssignment == memberId) null else memberId) }
    }
    fun updateTax(amount: Double) { _uiState.update { it.copy(tax = amount) } }
    fun updateService(amount: Double) { _uiState.update { it.copy(service = amount) } }
    fun updateDiscount(amount: Double) { _uiState.update { it.copy(discount = amount) } }
    fun updateOthers(amount: Double) { _uiState.update { it.copy(others = amount) } }

    fun addMember(name: String) {
        if (name.isBlank()) return
        _uiState.update { state ->
            val currentMinId = state.availableMembers.minOfOrNull { it.id }?.coerceAtMost(0) ?: 0
            val newId = currentMinId - 1
            val newMember = Members(id = newId, smartSplitId = 0, name = name, contact = null, note = null)
            var updatedItems = state.items
            if (state.isSplitEvenly) {
                updatedItems = updatedItems.map { item -> item.copy(assignedMemberIds = item.assignedMemberIds + newId) }
            }
            state.copy(availableMembers = state.availableMembers + newMember, items = updatedItems)
        }
    }
    fun addMembers(draftMembers: List<DraftMember>) {
        _uiState.update { state ->
            val existingNames = state.availableMembers.map { it.name }.toSet()
            val newMembers = draftMembers.filter { it.name !in existingNames }.map { dm ->
                Members(id = dm.id, smartSplitId = 0, name = dm.name, contact = dm.contact, note = dm.note)
            }
            var updatedItems = state.items
            if (state.isSplitEvenly) {
                val newIds = newMembers.map { it.id }
                updatedItems = updatedItems.map { item -> item.copy(assignedMemberIds = item.assignedMemberIds + newIds) }
            }
            state.copy(availableMembers = state.availableMembers + newMembers, items = updatedItems)
        }
    }
    fun removeMember(memberId: Int) {
        _uiState.update { state ->
            val updatedMembers = state.availableMembers.filter { it.id != memberId }
            val updatedItems = state.items.map { item ->
                if (item.assignedMemberIds.contains(memberId)) item.copy(assignedMemberIds = item.assignedMemberIds - memberId) else item
            }
            val newSelection = if (state.selectedMemberIdForAssignment == memberId) null else state.selectedMemberIdForAssignment
            state.copy(availableMembers = updatedMembers, items = updatedItems, selectedMemberIdForAssignment = newSelection)
        }
    }
    fun toggleMemberAssignment(itemId: Int, memberId: Int) {
        _uiState.update { state ->
            val newSplitEvenly = false
            val updatedItems = state.items.map { item ->
                if (item.id == itemId) {
                    val currentAssignments = item.assignedMemberIds.toMutableList()
                    if (currentAssignments.contains(memberId)) currentAssignments.remove(memberId) else currentAssignments.add(memberId)
                    item.copy(assignedMemberIds = currentAssignments)
                } else { item }
            }
            state.copy(items = updatedItems, isSplitEvenly = newSplitEvenly)
        }
    }
    fun addItem(name: String, price: Double, quantity: Int) {
        _uiState.update { state ->
            val newId = (state.items.maxOfOrNull { it.id } ?: 0) + 1
            val initialAssignments = if (state.isSplitEvenly) state.availableMembers.map { it.id } else emptyList()
            val newItem = SmartSplitItemUi(newId, name, quantity, price, initialAssignments)
            state.copy(items = state.items + newItem)
        }
    }
    fun updateItem(id: Int, name: String, price: Double, quantity: Int) {
        _uiState.update { state ->
            val updatedList = state.items.map { item -> if (item.id == id) item.copy(name = name, price = price, quantity = quantity) else item }
            state.copy(items = updatedList)
        }
    }
    fun deleteItem(id: Int) {
        _uiState.update { state -> state.copy(items = state.items.filter { it.id != id }) }
    }
    fun setImageUri(uri: String?) { _uiState.update { it.copy(imageUri = uri) } }
    fun checkBackendHealth() { viewModelScope.launch { try { ApiConfig.getApiService().getHealth() } catch (e: Exception) { Log.e("API_TEST", "Health Check Error", e) } } }
    fun createDraft(): SmartSplitDraft {
        val state = uiState.value
        return SmartSplitDraft(
            splitName = state.splitName, total = state.total, tax = state.tax, service = state.service, discount = state.discount, others = state.others, imageUri = state.imageUri,
            members = state.availableMembers.map { DraftMember(it.id, it.name, it.contact, it.note) },
            items = state.items.map { DraftItem(it.name, it.price, it.quantity, it.assignedMemberIds) }
        )
    }
}