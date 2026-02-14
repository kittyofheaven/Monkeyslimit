package com.menac1ngmonkeys.monkeyslimit.ui.components

import com.menac1ngmonkeys.monkeyslimit.R

sealed class BalExpData (
    val title: String,
    val amount: Double,
    val iconId: Int,
) {
    class Income(amount: Double) : BalExpData(
        title = "Income This Month",
        amount = amount,
        iconId = R.drawable.income
    )

    class Expense(amount: Double) : BalExpData(
        title = "Expense This Month",
        amount = amount,
        iconId = R.drawable.expense
    )
}