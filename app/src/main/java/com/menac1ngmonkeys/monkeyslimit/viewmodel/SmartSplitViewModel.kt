package com.menac1ngmonkeys.monkeyslimit.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class SmartSplitUiState(
    val isValidating: Boolean = false,
    val validationError: String? = null
)

class SmartSplitViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SmartSplitUiState())
    val uiState = _uiState.asStateFlow()

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    fun dismissError() {
        _uiState.update { it.copy(validationError = null) }
    }

    fun validateReceipt(context: Context, uri: Uri, onValid: () -> Unit) {
        _uiState.update { it.copy(isValidating = true, validationError = null) }

        viewModelScope.launch {
            try {
                val inputImage = InputImage.fromFilePath(context, uri)
                // Use await() to run synchronously in coroutine
                val visionText = recognizer.process(inputImage).await()
                val fullText = visionText.text

                Log.d("SmartSplit", "Validation Text: $fullText")

                if (isInvalidContent(fullText)) {
                    _uiState.update {
                        it.copy(isValidating = false, validationError = "This looks like a code snippet or screen. Please scan a valid receipt.")
                    }
                } else if (isValidDocument(fullText)) {
                    _uiState.update { it.copy(isValidating = false) }
                    onValid() // Trigger navigation
                } else {
                    _uiState.update {
                        it.copy(isValidating = false, validationError = "Could not verify this is a bill. Please ensure the text is clear and contains prices.")
                    }
                }
            } catch (e: Exception) {
                Log.e("SmartSplit", "Validation Error", e)
                _uiState.update {
                    it.copy(isValidating = false, validationError = "Failed to read image. Please try again.")
                }
            }
        }
    }

    // --- VALIDATION LOGIC (Reused) ---

    private fun isInvalidContent(text: String): Boolean {
        val lowerText = text.lowercase()
        val codeSymbols = listOf("fun ", "val ", "var ", "import ", "package ", "class ", "return", "//", "/*")
        val isCode = codeSymbols.count { lowerText.contains(it) } >= 2
        return isCode
    }

    private fun isValidDocument(text: String): Boolean {
        val lowerText = text.lowercase()
        var score = 0

        // 1. Negative Filters
        val blockList = listOf("add a new budget", "ai recommendation", "analytics", "split bill", "budgeted", "spent", "kb/s", "4g", "wifi")
        if (blockList.any { lowerText.contains(it) }) return false

        // 2. Strong Indicators (+2)
        val strongKeywords = listOf("berhasil", "success", "paid", "lunas", "transaction id", "struk", "receipt", "invoice", "payment to", "merchant", "total", "pbi")
        strongKeywords.forEach { if (lowerText.contains(it)) score += 2 }

        // 3. Weak Indicators (+1)
        val weakKeywords = listOf("subtotal", "sub-total", "sub total", "jumlah", "amount", "bayar", "cash", "tunai", "tax", "pajak", "fee", "biaya", "change", "kembali")
        weakKeywords.forEach { if (lowerText.contains(it)) score += 1 }

        // 4. Currency (+2)
        val currencyRegex = Regex("(rp|idr)\\s*[\\d\\.,]+", RegexOption.IGNORE_CASE)
        if (currencyRegex.containsMatchIn(text)) score += 2

        // 5. Date (+1)
        val dateRegex = Regex("\\d{1,4}[/-]\\d{1,2}[/-]\\d{2,4}|\\d{1,2}\\s+[a-z]{3,}", RegexOption.IGNORE_CASE)
        if (dateRegex.containsMatchIn(text)) score += 1

        Log.d("SmartSplit", "Validation Score: $score")
        return score >= 4
    }
}