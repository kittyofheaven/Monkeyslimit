package com.menac1ngmonkeys.monkeyslimit.ui.state

import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Members

/**
 * Represents a single item row in the Smart Split review screen.
 */
data class SmartSplitItemUi(
    val id: Int, // Temporary ID for UI distinction
    val name: String,
    val quantity: Int,
    val price: Double,
    val assignedMemberIds: List<Int> = emptyList() // IDs of members assigned to this item
)

/**
 * UI State for the Review Smart Split Screen.
 * Holds the bill details before they are saved to the database.
 */
data class ReviewSmartSplitUiState(
    val imageUri: String? = null,
    val splitName: String = "",
    val items: List<SmartSplitItemUi> = emptyList(),

    val isSplitEvenly: Boolean = false,

    // Extra Fields
    val tax: Double = 0.0,
    val service: Double = 0.0,
    val discount: Double = 0.0, // New
    val others: Double = 0.0,   // New

    val availableMembers: List<Members> = emptyList(),
    val selectedMemberIdForAssignment: Int? = null,
    val isLoading: Boolean = false
) {
    val subtotal: Double
        get() = items.sumOf { it.price * it.quantity }

    val total: Double
        get() = (subtotal + tax + service + others - discount).coerceAtLeast(0.0)
}