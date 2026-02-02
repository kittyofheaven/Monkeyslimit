package com.menac1ngmonkeys.monkeyslimit.ui.state

import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Members

data class SelectMemberUiState(
    val searchQuery: String = "",
    val filteredMembers: List<Members> = emptyList(), // Changed String -> Members
    val selectedMembers: List<Members> = emptyList(), // Changed String -> Members
    val isLoading: Boolean = false
) {
    // Helper: Check if a member with the exact name already exists
    val isSearchQueryInList: Boolean
        get() = filteredMembers.any { it.name.equals(searchQuery, ignoreCase = true) }
}