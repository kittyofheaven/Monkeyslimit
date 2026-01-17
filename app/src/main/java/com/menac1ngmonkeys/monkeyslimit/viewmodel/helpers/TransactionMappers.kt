package com.menac1ngmonkeys.monkeyslimit.viewmodel.helpers

import com.menac1ngmonkeys.monkeyslimit.R
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Categories
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Transactions
import com.menac1ngmonkeys.monkeyslimit.ui.dashboard.TransactionItemData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Maps a database transaction to a UI model with formatted fields and icon.
 *
 * @param category category matched by transaction.categoryId (nullable).
 * @return UI-ready transaction item for display.
 */
fun Transactions.toTransactionItemData(category: Categories?): TransactionItemData {
    val realCategory = category ?: Categories(id = 0, name = "Other", icon = null, description = null)
    val isExpense = (realCategory.name != "Salary")

    val icon = when (realCategory.name) {
        "Food and Beverages" -> R.drawable.food
        "Transport" -> R.drawable.directions_car_48px
        "Shopping" -> R.drawable.shopping_bag_48px
        "Bills" -> R.drawable.receipt_long_48px
        "Entertainment" -> R.drawable.playing_cards_48px
        "Health" -> R.drawable.heart_check_48px
        "Education" -> R.drawable.cognition_2_48px
        "Salary" -> R.drawable.salary
        else -> R.drawable.paid_48px
    }

    return TransactionItemData(
        iconResId = icon,
        title = this.note ?: "Transaction",
        subtitle = this.date.toFormattedString(),
        category = realCategory.name,
        amount = this.totalAmount,
        isExpense = isExpense
    )
}

fun Date.toFormattedString(): String {
    val formatter = SimpleDateFormat("HH:mm - MMM dd", Locale.getDefault())
    return formatter.format(this)
}
