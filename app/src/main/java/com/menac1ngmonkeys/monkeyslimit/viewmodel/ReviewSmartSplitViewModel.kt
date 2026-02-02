package com.menac1ngmonkeys.monkeyslimit.viewmodel

import androidx.lifecycle.ViewModel
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Members
import com.menac1ngmonkeys.monkeyslimit.ui.state.DraftItem
import com.menac1ngmonkeys.monkeyslimit.ui.state.DraftMember
import com.menac1ngmonkeys.monkeyslimit.ui.state.ReviewSmartSplitUiState
import com.menac1ngmonkeys.monkeyslimit.ui.state.SmartSplitDraft
import com.menac1ngmonkeys.monkeyslimit.ui.state.SmartSplitItemUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ReviewSmartSplitViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewSmartSplitUiState())
    val uiState = _uiState.asStateFlow()

    init {
        addMember("You")
    }

    // --- NEW: Split Evenly Logic ---

    fun setSplitEvenly(enable: Boolean) {
        _uiState.update { state ->
            if (enable) {
                // Assign ALL members to ALL items
                val allMemberIds = state.availableMembers.map { it.id }
                val updatedItems = state.items.map { item ->
                    item.copy(assignedMemberIds = allMemberIds)
                }
                state.copy(isSplitEvenly = true, items = updatedItems)
            } else {
                // Just turn off the flag, keep assignments as is
                state.copy(isSplitEvenly = false)
            }
        }
    }

    // --- State Updates ---

    fun setSplitName(name: String) {
        _uiState.update { it.copy(splitName = name) }
    }

    fun selectMemberForAssignment(memberId: Int?) {
        _uiState.update { state ->
            val newSelection = if (state.selectedMemberIdForAssignment == memberId) null else memberId
            state.copy(selectedMemberIdForAssignment = newSelection)
        }
    }

    fun updateTax(amount: Double) { _uiState.update { it.copy(tax = amount) } }
    fun updateService(amount: Double) { _uiState.update { it.copy(service = amount) } }
    fun updateDiscount(amount: Double) { _uiState.update { it.copy(discount = amount) } }
    fun updateOthers(amount: Double) { _uiState.update { it.copy(others = amount) } }

    // --- Member Management ---

    fun addMember(name: String) {
        if (name.isBlank()) return
        _uiState.update { state ->
            val currentMinId = state.availableMembers.minOfOrNull { it.id }?.coerceAtMost(0) ?: 0
            val newId = currentMinId - 1
            val newMember = Members(id = newId, smartSplitId = 0, name = name, contact = null, note = null)

            var updatedItems = state.items
            // If Split Evenly is ON, add new member to ALL items automatically
            if (state.isSplitEvenly) {
                updatedItems = updatedItems.map { item ->
                    item.copy(assignedMemberIds = item.assignedMemberIds + newId)
                }
            }

            state.copy(
                availableMembers = state.availableMembers + newMember,
                items = updatedItems
            )
        }
    }

    fun addMembers(draftMembers: List<DraftMember>) {
        _uiState.update { state ->
            val existingNames = state.availableMembers.map { it.name }.toSet()
            val newMembers = draftMembers
                .filter { it.name !in existingNames }
                .map { dm ->
                    Members(
                        id = dm.id,
                        smartSplitId = 0,
                        name = dm.name,
                        contact = dm.contact,
                        note = dm.note
                    )
                }

            var updatedItems = state.items
            // If Split Evenly is ON, add new members to ALL items automatically
            if (state.isSplitEvenly) {
                val newIds = newMembers.map { it.id }
                updatedItems = updatedItems.map { item ->
                    item.copy(assignedMemberIds = item.assignedMemberIds + newIds)
                }
            }

            state.copy(
                availableMembers = state.availableMembers + newMembers,
                items = updatedItems
            )
        }
    }

    fun removeMember(memberId: Int) {
        _uiState.update { state ->
            val updatedMembers = state.availableMembers.filter { it.id != memberId }
            val updatedItems = state.items.map { item ->
                if (item.assignedMemberIds.contains(memberId)) {
                    item.copy(assignedMemberIds = item.assignedMemberIds - memberId)
                } else {
                    item
                }
            }
            val newSelection = if (state.selectedMemberIdForAssignment == memberId) null else state.selectedMemberIdForAssignment

            state.copy(
                availableMembers = updatedMembers,
                items = updatedItems,
                selectedMemberIdForAssignment = newSelection
            )
        }
    }

    // --- Item Management ---

    fun toggleMemberAssignment(itemId: Int, memberId: Int) {
        _uiState.update { state ->
            // If user manually changes assignment, disable "Split Evenly"
            val newSplitEvenly = false

            val updatedItems = state.items.map { item ->
                if (item.id == itemId) {
                    val currentAssignments = item.assignedMemberIds.toMutableList()
                    if (currentAssignments.contains(memberId)) {
                        currentAssignments.remove(memberId)
                    } else {
                        currentAssignments.add(memberId)
                    }
                    item.copy(assignedMemberIds = currentAssignments)
                } else {
                    item
                }
            }
            state.copy(items = updatedItems, isSplitEvenly = newSplitEvenly)
        }
    }

    fun addItem(name: String, price: Double, quantity: Int) {
        _uiState.update { state ->
            val newId = (state.items.maxOfOrNull { it.id } ?: 0) + 1

            // If Split Evenly is ON, assign ALL members to this new item
            val initialAssignments = if (state.isSplitEvenly) {
                state.availableMembers.map { it.id }
            } else {
                emptyList()
            }

            val newItem = SmartSplitItemUi(newId, name, quantity, price, initialAssignments)
            state.copy(items = state.items + newItem)
        }
    }

    fun updateItem(id: Int, name: String, price: Double, quantity: Int) {
        _uiState.update { state ->
            val updatedList = state.items.map { item ->
                if (item.id == id) item.copy(name = name, price = price, quantity = quantity) else item
            }
            state.copy(items = updatedList)
        }
    }

    fun deleteItem(id: Int) {
        _uiState.update { state ->
            state.copy(items = state.items.filter { it.id != id })
        }
    }

    fun setImageUri(uri: String?) {
        _uiState.update { it.copy(imageUri = uri) }
    }

    fun createDraft(): SmartSplitDraft {
        val state = uiState.value
        return SmartSplitDraft(
            splitName = state.splitName,
            total = state.total,
            tax = state.tax,
            service = state.service,
            discount = state.discount,
            others = state.others,
            imageUri = state.imageUri,
            members = state.availableMembers.map {
                DraftMember(it.id, it.name, it.contact, it.note)
            },
            items = state.items.map {
                DraftItem(it.name, it.price, it.quantity, it.assignedMemberIds)
            }
        )
    }
}