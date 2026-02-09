package com.menac1ngmonkeys.monkeyslimit.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonSyntaxException
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Members
import com.menac1ngmonkeys.monkeyslimit.data.remote.ApiConfig
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
import java.io.FileOutputStream

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
                // --- STEP 1: PREPARE FILE FOR API ---
                val contentResolver = context.contentResolver
                val type = contentResolver.getType(imageUri) ?: "image/jpeg"
                val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(type) ?: "jpg"
                val mediaType = type.toMediaTypeOrNull()

                val file = uriToFile(context, imageUri, ".$extension")
                val requestFile = file.asRequestBody(mediaType)
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

                // --- STEP 2: CALL BACKEND API ---
                val response = ApiConfig.getApiService().predictReceipt(body)

                if (response.isSuccessful && response.body()?.ok == true) {
                    val data = response.body()?.data
                    Log.d("API_TEST", "Predict Success! Data: $data")

                    val newItems = data?.menu?.mapIndexed { index, item ->
                        val unitPrice = if (item.qty > 0) item.price_int / item.qty else item.price_int
                        SmartSplitItemUi(
                            id = index + 1,
                            name = item.nm,
                            price = unitPrice,
                            quantity = item.qty,
                            assignedMemberIds = emptyList()
                        )
                    } ?: emptyList()

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
                    Log.e("API_TEST", "Predict Failed: ${response.errorBody()?.string()}")
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to analyze receipt") }
                }
            } catch (e: Exception) {
                Log.e("API_TEST", "Predict Error", e)
                val errorMsg = if (e is JsonSyntaxException || e.message?.contains("BEGIN_OBJECT") == true) {
                    "Not a valid bill"
                } else {
                    "Error connecting to server: ${e.localizedMessage}"
                }
                _uiState.update { it.copy(isLoading = false, errorMessage = errorMsg) }
            }
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

        // 1. NEGATIVE FILTERS
        val blockList = listOf(
            "add a new budget", "ai recommendation", "analytics", "split bill",
            "budgeted", "left", "spent", "sort", "kb/s", "4g", "wifi"
        )
        if (blockList.any { lowerText.contains(it) }) return false

        // 2. STRONG INDICATORS
        val strongKeywords = listOf(
            "berhasil", "success", "successful", "selesai", "paid", "lunas",
            "id transaksi", "transaction id", "no. ref", "reference", "struk", "receipt",
            "invoice", "order id", "kode bayar", "payment to", "merchant"
        )
        strongKeywords.forEach { if (lowerText.contains(it)) score += 2 }

        // 3. WEAK INDICATORS
        val weakKeywords = listOf(
            "total", "subtotal", "jumlah", "amount", "bayar", "cash", "tunai",
            "kembali", "change", "tax", "pajak", "admin", "fee", "biaya"
        )
        weakKeywords.forEach { if (lowerText.contains(it)) score += 1 }

        // 4. CURRENCY FORMAT
        val currencyRegex = Regex("(rp|idr)\\s*[\\d\\.,]+", RegexOption.IGNORE_CASE)
        if (currencyRegex.containsMatchIn(text)) score += 2

        // 5. DATE FORMAT
        val dateRegex = Regex("\\d{1,4}[/-]\\d{1,2}[/-]\\d{2,4}|\\d{1,2}\\s+[a-z]{3,}", RegexOption.IGNORE_CASE)
        if (dateRegex.containsMatchIn(text)) score += 1

        Log.d("SmartSplit", "Document Validation Score: $score")
        return score >= 4
    }

    // --- HELPERS ---

    private fun uriToFile(context: Context, uri: Uri, extension: String): File {
        val inputStream = context.contentResolver.openInputStream(uri)
        val tempFile = File.createTempFile("upload", extension, context.cacheDir)
        val outputStream = FileOutputStream(tempFile)
        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        return tempFile
    }

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