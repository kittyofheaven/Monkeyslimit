package com.menac1ngmonkeys.monkeyslimit.viewmodel

import androidx.lifecycle.ViewModel
import com.menac1ngmonkeys.monkeyslimit.ui.state.ScanTransactionUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ScanTransactionViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ScanTransactionUiState())
    val uiState = _uiState.asStateFlow()

    fun toggleFlash() {
        _uiState.update { it.copy(isFlashOn = !it.isFlashOn) }
    }
}