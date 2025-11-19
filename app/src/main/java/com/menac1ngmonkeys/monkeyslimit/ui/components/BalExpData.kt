package com.menac1ngmonkeys.monkeyslimit.ui.components

import java.math.BigDecimal
import com.menac1ngmonkeys.monkeyslimit.R

sealed class BalExpData (
    val title: String,
    val amount: Double,
    val iconId: Int,
) {
    class Balance(amount: Double) : BalExpData(
        title = "Total Balance",
        amount = amount,
        iconId = R.drawable.income
    )

    class Expense(amount: Double) : BalExpData(
        title = "Expense This Month",
        amount = amount,
        iconId = R.drawable.expense
    )
}