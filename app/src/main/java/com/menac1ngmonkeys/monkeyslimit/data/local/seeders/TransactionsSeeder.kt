package com.menac1ngmonkeys.monkeyslimit.data.local.seeders

import com.menac1ngmonkeys.monkeyslimit.data.local.dao.TransactionsDao
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.TransactionType
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Transactions
import java.util.Calendar
import java.util.Date

object TransactionsSeeder {
    suspend fun seed(
        transactionsDao: TransactionsDao,
        budgetsByName: Map<String, Int>,
        categoriesByName: Map<String, Int>,
        currentUserId: String = ""
    ) {
        if (transactionsDao.count() > 0) return

        // Helper to safely get budget IDs
        // If the budget name doesn't exist, it returns null (which is valid for Incomes or non-budgeted expenses)
        fun getBudget(name: String): Int? = budgetsByName[name]

        // Helper to generate specific dates (Month is 1-12)
        fun getDate(year: Int, month: Int, day: Int): Date {
            val cal = Calendar.getInstance()
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month - 1) // Calendar months are 0-indexed
            val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            cal.set(Calendar.DAY_OF_MONTH, day.coerceIn(1, maxDay))

            // Set random time to avoid ordering issues
            cal.set(Calendar.HOUR_OF_DAY, 12)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            return cal.time
        }

        val allTransactions = mutableListOf<Transactions>()

        // ==========================================
        // DECEMBER 2025 (Holiday Season)
        // ==========================================
        val decIncomes = listOf(
            Transactions(0, getDate(2025, 12, 1), 25_000_000.0, "Gaji Desember", null, null, categoriesByName.getValue("Salary"), TransactionType.INCOME, userId = currentUserId),
            Transactions(0, getDate(2025, 12, 20), 10_000_000.0, "Bonus Akhir Tahun", null, null, categoriesByName.getValue("Bonus"), TransactionType.INCOME, userId = currentUserId), // Big bonus
            Transactions(0, getDate(2025, 12, 15), 2_000_000.0, "Dividen Saham Q4", null, null, categoriesByName.getValue("Investment"), TransactionType.INCOME, userId = currentUserId)
        )

        val decExpenses = listOf(
            // Needs
            Transactions(0, getDate(2025, 12, 2), 3_000_000.0, "Groceries Bulanan", null, getBudget("Needs (Food and Drinks)"), categoriesByName.getValue("Food and Beverages"), TransactionType.EXPENSE, userId = currentUserId),
            Transactions(0, getDate(2025, 12, 5), 500_000.0, "Makan Luar Keluarga", null, getBudget("Needs (Food and Drinks)"), categoriesByName.getValue("Food and Beverages"), TransactionType.EXPENSE, userId = currentUserId),
            // Wants (High due to holidays)
            Transactions(0, getDate(2025, 12, 10), 2_500_000.0, "Kado Natal & Tahun Baru", null, getBudget("Wants (Lifestyle)"), categoriesByName.getValue("Shopping"), TransactionType.EXPENSE, userId = currentUserId),
            Transactions(0, getDate(2025, 12, 24), 1_500_000.0, "Staycation Hotel", null, getBudget("Wants (Lifestyle)"), categoriesByName.getValue("Entertainment"), TransactionType.EXPENSE, userId = currentUserId),
            Transactions(0, getDate(2025, 12, 31), 800_000.0, "Pesta BBQ Tahun Baru", null, getBudget("Wants (Lifestyle)"), categoriesByName.getValue("Food and Beverages"), TransactionType.EXPENSE, userId = currentUserId),
            // Bills
            Transactions(0, getDate(2025, 12, 1), 1_500_000.0, "Listrik & Air", null, getBudget("Bills and Housing"), categoriesByName.getValue("Bills"), TransactionType.EXPENSE, userId = currentUserId),
            Transactions(0, getDate(2025, 12, 28), 5_000_000.0, "Investasi Tambahan", null, getBudget("Investment and Donation"), categoriesByName.getValue("Other"), TransactionType.EXPENSE, userId = currentUserId)
        )
        allTransactions.addAll(decIncomes + decExpenses)


        // ==========================================
        // JANUARY 2026 (New Year)
        // ==========================================
        val janIncomes = listOf(
            Transactions(0, getDate(2026, 1, 1), 25_000_000.0, "Gaji Januari", null, null, categoriesByName.getValue("Salary"), TransactionType.INCOME, userId = currentUserId),
            Transactions(0, getDate(2026, 1, 15), 3_000_000.0, "Proyek Freelance Awal Tahun", null, null, categoriesByName.getValue("Bonus"), TransactionType.INCOME, userId = currentUserId),
            Transactions(0, getDate(2026, 1, 10), 500_000.0, "Hadiah Imlek", null, null, categoriesByName.getValue("Bonus"), TransactionType.INCOME, userId = currentUserId)
        )

        val janExpenses = listOf(
            // Needs
            Transactions(0, getDate(2026, 1, 3), 2_800_000.0, "Belanja Bulanan", null, getBudget("Needs (Food and Drinks)"), categoriesByName.getValue("Food and Beverages"), TransactionType.EXPENSE, userId = currentUserId),
            Transactions(0, getDate(2026, 1, 12), 150_000.0, "Kopi Kantor (Paket)", null, getBudget("Needs (Food and Drinks)"), categoriesByName.getValue("Food and Beverages"), TransactionType.EXPENSE, userId = currentUserId),
            // Education (New Year Resolutions)
            Transactions(0, getDate(2026, 1, 5), 1_200_000.0, "Gym Membership Tahunan", null, getBudget("Education and Health"), categoriesByName.getValue("Health"), TransactionType.EXPENSE, userId = currentUserId),
            Transactions(0, getDate(2026, 1, 7), 500_000.0, "Buku Self Improvement", null, getBudget("Education and Health"), categoriesByName.getValue("Education"), TransactionType.EXPENSE, userId = currentUserId),
            // Transport
            Transactions(0, getDate(2026, 1, 2), 400_000.0, "Isi E-Toll", null, getBudget("Transportation"), categoriesByName.getValue("Transport"), TransactionType.EXPENSE, userId = currentUserId),
            Transactions(0, getDate(2026, 1, 20), 250_000.0, "Service Motor Berkala", null, getBudget("Transportation"), categoriesByName.getValue("Transport"), TransactionType.EXPENSE, userId = currentUserId),
            // Bills
            Transactions(0, getDate(2026, 1, 1), 1_300_000.0, "Listrik & Internet", null, getBudget("Bills and Housing"), categoriesByName.getValue("Bills"), TransactionType.EXPENSE, userId = currentUserId)
        )
        allTransactions.addAll(janIncomes + janExpenses)


        // ==========================================
        // FEBRUARY 2026 (Current Month)
        // ==========================================
        val febIncomes = listOf(
            Transactions(0, getDate(2026, 2, 1), 25_000_000.0, "Gaji Februari", null, null, categoriesByName.getValue("Salary"), TransactionType.INCOME, userId = currentUserId),
            Transactions(0, getDate(2026, 2, 5), 1_500_000.0, "Dividen Saham", null, null, categoriesByName.getValue("Investment"), TransactionType.INCOME, userId = currentUserId),
            Transactions(0, getDate(2026, 2, 8), 3_500_000.0, "Hasil Sewa Ruko", null, null, categoriesByName.getValue("Investment"), TransactionType.INCOME, userId = currentUserId),
            Transactions(0, getDate(2026, 2, 10), 750_000.0, "Jual Barang Bekas", null, null, categoriesByName.getValue("Bonus"), TransactionType.INCOME, userId = currentUserId)
        )

        val febExpenses = listOf(
            // Needs
            Transactions(0, getDate(2026, 2, 2), 350_000.0, "Lunch with team", null, getBudget("Needs (Food and Drinks)"), categoriesByName.getValue("Food and Beverages"), TransactionType.EXPENSE, userId = currentUserId),
            Transactions(0, getDate(2026, 2, 5), 2_500_000.0, "Belanja mingguan (Groceries)", null, getBudget("Needs (Food and Drinks)"), categoriesByName.getValue("Food and Beverages"), TransactionType.EXPENSE, userId = currentUserId),
            Transactions(0, getDate(2026, 2, 6), 120_000.0, "Sarapan roti & kopi", null, getBudget("Needs (Food and Drinks)"), categoriesByName.getValue("Food and Beverages"), TransactionType.EXPENSE, userId = currentUserId),

            // Wants (Valentine prep?)
            Transactions(0, getDate(2026, 2, 4), 890_000.0, "Kaos & celana Uniqlo", null, getBudget("Wants (Lifestyle)"), categoriesByName.getValue("Shopping"), TransactionType.EXPENSE, userId = currentUserId),
            Transactions(0, getDate(2026, 2, 7), 600_000.0, "Tiket Konser", null, getBudget("Wants (Lifestyle)"), categoriesByName.getValue("Entertainment"), TransactionType.EXPENSE, userId = currentUserId),

            // Bills
            Transactions(0, getDate(2026, 2, 1), 1_200_000.0, "Listrik PLN Token", null, getBudget("Bills and Housing"), categoriesByName.getValue("Bills"), TransactionType.EXPENSE, userId = currentUserId),
            Transactions(0, getDate(2026, 2, 1), 450_000.0, "Internet WiFi", null, getBudget("Bills and Housing"), categoriesByName.getValue("Bills"), TransactionType.EXPENSE, userId = currentUserId),

            // Health
            Transactions(0, getDate(2026, 2, 3), 450_000.0, "Obat & Vitamin", null, getBudget("Education and Health"), categoriesByName.getValue("Health"), TransactionType.EXPENSE, userId = currentUserId),

            // Donation
            Transactions(0, getDate(2026, 2, 5), 500_000.0, "Sedekah Jumat", null, getBudget("Investment and Donation"), categoriesByName.getValue("Other"), TransactionType.EXPENSE, userId = currentUserId)
        )
        allTransactions.addAll(febIncomes + febExpenses)

        // Insert All
        transactionsDao.insertAll(allTransactions)
    }
}