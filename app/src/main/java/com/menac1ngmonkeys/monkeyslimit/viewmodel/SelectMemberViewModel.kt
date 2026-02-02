package com.menac1ngmonkeys.monkeyslimit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Members
import com.menac1ngmonkeys.monkeyslimit.data.repository.MembersRepository
import com.menac1ngmonkeys.monkeyslimit.ui.state.SelectMemberUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SelectMemberViewModel(
    private val membersRepository: MembersRepository
) : ViewModel() {

    // Dummy "You" for display. ID -1 ensures it doesn't conflict with real DB IDs.
    private val youMember = Members(id = -1, smartSplitId = null, name = "You", contact = null, note = null)

    // 1. Load Global Contacts + Prepend "You"
    private val dbMembersFlow = membersRepository.getAllGlobalContacts()
        .map { list ->
            val sortedList = list.sortedBy { it.name }
            listOf(youMember) + sortedList
        }

    private val _searchQuery = MutableStateFlow("")
    private val _selectedMembers = MutableStateFlow<List<Members>>(emptyList())
    // We still track excluded names to filter the list
    private val _excludedNames = MutableStateFlow<List<String>>(emptyList())

    val uiState: StateFlow<SelectMemberUiState> = combine(
        dbMembersFlow,
        _searchQuery,
        _selectedMembers,
        _excludedNames
    ) { dbMembers, query, selected, excludedNames ->

        // Filter out members already in the bill (by name)
        val availableMembers = dbMembers.filter { it.name !in excludedNames }

        val filtered = if (query.isBlank()) {
            availableMembers
        } else {
            availableMembers.filter { it.name.contains(query, ignoreCase = true) }
        }

        SelectMemberUiState(
            searchQuery = query,
            filteredMembers = filtered,
            selectedMembers = selected,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SelectMemberUiState(isLoading = true)
    )

    fun setExcludedMembers(names: List<String>) {
        _excludedNames.update { names }
    }

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.update { newQuery }
    }

    fun toggleSelection(member: Members) {
        _selectedMembers.update { currentList ->
            // Use ID for real members, Name for "You"
            val exists = currentList.any { it.id == member.id && it.name == member.name }
            if (exists) {
                currentList.filterNot { it.id == member.id && it.name == member.name }
            } else {
                currentList + member
            }
        }
    }

    // Save New Global Contact (SmartSplitId = null)
    fun addNewMemberToDb(name: String, phone: String, note: String) {
        if (name.isNotBlank()) {
            viewModelScope.launch {
                val newMember = Members(
                    id = 0,
                    smartSplitId = null,
                    name = name,
                    contact = phone.ifBlank { null },
                    note = note.ifBlank { null }
                )
                membersRepository.insert(newMember)
                onSearchQueryChange("")
            }
        }
    }

    // Update Existing Global Contact
    fun updateMemberInDb(originalMember: Members, name: String, phone: String, note: String) {
        if (originalMember.id == -1) return // Don't edit "You"
        viewModelScope.launch {
            val updated = originalMember.copy(
                name = name,
                contact = phone.ifBlank { null },
                note = note.ifBlank { null }
            )
            membersRepository.update(updated)
        }
    }
}