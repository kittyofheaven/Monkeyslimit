package com.menac1ngmonkeys.monkeyslimit.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.menac1ngmonkeys.monkeyslimit.ui.state.ScanTransactionUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ScanTransactionViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ScanTransactionUiState())
    val uiState = _uiState.asStateFlow()

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    fun toggleFlash() {
        _uiState.update { it.copy(isFlashOn = !it.isFlashOn) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun analyzeReceipt(context: Context, imageUri: Uri, onSuccess: (String) -> Unit) {
        _uiState.update { it.copy(isProcessing = true, error = null) }

        viewModelScope.launch {
            try {
                val image = InputImage.fromFilePath(context, imageUri)

                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        val fullText = visionText.text
                        Log.d("ReceiptScanner", "Raw Text detected: \n$fullText")

                        if (isInvalidContent(fullText)) {
                            _uiState.update {
                                it.copy(
                                    isProcessing = false,
                                    error = "This looks like a code snippet or screen. Please scan a receipt."
                                )
                            }
                        } else if (isValidDocument(fullText)) {
                            Log.d("ReceiptScanner", "Document Validated! Proceeding.")
                            _uiState.update { it.copy(isProcessing = false, receiptText = fullText) }
                            onSuccess(fullText)
                        } else {
                            Log.w("ReceiptScanner", "Validation Failed. Not enough keywords.")
                            _uiState.update {
                                it.copy(
                                    isProcessing = false,
                                    error = "Could not verify this document. Ensure text is clear."
                                )
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("ReceiptScanner", "Scan failed", e)
                        _uiState.update {
                            it.copy(isProcessing = false, error = "Failed to read image.")
                        }
                    }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isProcessing = false, error = "Error loading image: ${e.localizedMessage}")
                }
            }
        }
    }

    private fun isInvalidContent(text: String): Boolean {
        val lowerText = text.lowercase()
        val codeSymbols = listOf("fun ", "val ", "var ", "import ", "package ", "class ", "return", "//", "/*")
        val isCode = codeSymbols.count { lowerText.contains(it) } >= 2
        if (isCode) Log.w("ReceiptScanner", "Blocked: Detected Code Syntax")
        return isCode
    }

    /**
     * UPDATED VALIDATION:
     * Checks for Paper Receipt keywords OR Digital Transfer keywords.
     */
    private fun isValidDocument(text: String): Boolean {
        val lowerText = text.lowercase()
        var score = 0

        // 1. NEGATIVE FILTERS (Immediate Fail)
        // These words appear in App UI but rarely on a physical/digital receipt
        val blockList = listOf(
            "add a new budget", "ai recommendation", "analytics", "split bill",
            "budgeted", "left", "spent", "sort", "kb/s", "4g", "wifi"
        )
        if (blockList.any { lowerText.contains(it) }) {
            Log.w("ReceiptScanner", "Blocked: Detected App UI keywords.")
            return false
        }

        // 2. STRONG INDICATORS (Receipt/Transfer Specifics) - Weight: +2
        val strongKeywords = listOf(
            // Transaction Status
            "berhasil", "success", "successful", "selesai", "paid", "lunas",
            // Receipt Identifiers
            "id transaksi", "transaction id", "no. ref", "reference", "struk", "receipt",
            "invoice", "order id", "kode bayar", "payment to", "merchant"
        )
        strongKeywords.forEach { if (lowerText.contains(it)) score += 2 }

        // 3. WEAK INDICATORS (Common Words) - Weight: +1
        val weakKeywords = listOf(
            "total", "subtotal", "jumlah", "amount", "bayar", "cash", "tunai",
            "kembali", "change", "tax", "pajak", "admin", "fee", "biaya"
        )
        weakKeywords.forEach { if (lowerText.contains(it)) score += 1 }

        // 4. CURRENCY FORMAT - Weight: +2 (Reduced from +3)
        // Currency alone shouldn't be enough to pass (need at least 1 keyword)
        val currencyRegex = Regex("(rp|idr)\\s*[\\d\\.,]+", RegexOption.IGNORE_CASE)
        if (currencyRegex.containsMatchIn(text)) score += 2

        // 5. DATE FORMAT - Weight: +1
        val dateRegex = Regex("\\d{1,4}[/-]\\d{1,2}[/-]\\d{2,4}|\\d{1,2}\\s+[a-z]{3,}", RegexOption.IGNORE_CASE)
        if (dateRegex.containsMatchIn(text)) score += 1

        Log.d("ReceiptScanner", "Document Score: $score (Threshold: 4)")

        // INCREASED THRESHOLD:
        // Needs (Currency + 1 Strong Keyword) OR (Currency + 2 Weak Keywords)
        return score >= 4
    }
}