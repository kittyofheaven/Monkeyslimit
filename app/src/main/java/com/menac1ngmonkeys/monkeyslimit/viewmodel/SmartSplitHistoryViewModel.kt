package com.menac1ngmonkeys.monkeyslimit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.menac1ngmonkeys.monkeyslimit.data.repository.SmartSplitsRepository
import com.menac1ngmonkeys.monkeyslimit.ui.state.SmartSplitHistoryUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class SmartSplitHistoryViewModel(
    private val smartSplitsRepository: SmartSplitsRepository
) : ViewModel() {

    // Convert Flow<List<SmartSplits>> -> StateFlow<SmartSplitHistoryUiState>
    val uiState: StateFlow<SmartSplitHistoryUiState> = smartSplitsRepository.getAllSmartSplits()
        .map { list ->
            // Sort by newest first
            val sortedList = list.sortedByDescending { it.createDate }
            SmartSplitHistoryUiState(splits = sortedList, isLoading = false)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SmartSplitHistoryUiState(isLoading = true)
        )
}