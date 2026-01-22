package com.menac1ngmonkeys.monkeyslimit.ui.transaction

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatCurrency(amount: Double): String {
    return NumberFormat.getNumberInstance(Locale.Builder().setLanguage("in").setRegion("ID").build()).format(amount)
}
fun formatDateOnly(date: Date): String = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.US).format(date)
fun formatTimeOnly(date: Date): String = SimpleDateFormat("HH:mm", Locale.US).format(date)