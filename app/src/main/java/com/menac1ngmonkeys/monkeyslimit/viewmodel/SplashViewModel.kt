package com.menac1ngmonkeys.monkeyslimit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.menac1ngmonkeys.monkeyslimit.ui.state.SplashUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SplashViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // Simulate initialization (e.g., checking local DB or auth)
            // 1500ms is a good duration for branding visibility
//            delay(1500)
            _uiState.value = SplashUiState(isLoading = false)
        }
    }
}