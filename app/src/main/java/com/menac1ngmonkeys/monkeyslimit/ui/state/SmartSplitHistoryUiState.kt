package com.menac1ngmonkeys.monkeyslimit.ui.state

import com.menac1ngmonkeys.monkeyslimit.data.local.entity.SmartSplits

data class SmartSplitHistoryUiState(
    val splits: List<SmartSplits> = emptyList(),
    val isLoading: Boolean = false
)