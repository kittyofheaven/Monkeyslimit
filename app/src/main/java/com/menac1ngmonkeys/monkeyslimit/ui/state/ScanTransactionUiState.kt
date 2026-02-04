package com.menac1ngmonkeys.monkeyslimit.ui.state

data class ScanTransactionUiState(
    val isFlashOn: Boolean = false,
    val isProcessing: Boolean = false,
    val error: String? = null,
    val receiptText: String? = null // To store raw OCR text if needed later
)