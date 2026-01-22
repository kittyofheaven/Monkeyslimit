package com.menac1ngmonkeys.monkeyslimit.data.local.seeders

import com.menac1ngmonkeys.monkeyslimit.data.local.dao.TransactionsDao
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.TransactionType
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Transactions

object TransactionsSeeder {
    suspend fun seed(
        transactionsDao: TransactionsDao,
        budgetsByName: Map<String, Int>,
        categoriesByName: Map<String, Int>
    ) {
        if (transactionsDao.count() > 0) return

        // 1. EXPENSES
        val expenseList = listOf(
            // --- Food and Beverages ---
            Transactions(0, SeedUtils.daysAgo(0), 3_910_000.0, "Lunch with president", null, budgetsByName["Groceries"], categoriesByName.getValue("Food and Beverages"), TransactionType.EXPENSE),
            Transactions(0, SeedUtils.daysAgo(1),   910_000.0, "Lunch with team", null, budgetsByName["Groceries"], categoriesByName.getValue("Food and Beverages"), TransactionType.EXPENSE),
            Transactions(0, SeedUtils.daysAgo(4),   120_000.0, "Sarapan roti & kopi", null, budgetsByName["Groceries"], categoriesByName.getValue("Food and Beverages"), TransactionType.EXPENSE),
            Transactions(0, SeedUtils.daysAgo(6),   350_000.0, "Makan malam restoran Jepang", null, budgetsByName["Groceries"], categoriesByName.getValue("Food and Beverages"), TransactionType.EXPENSE),
            Transactions(0, SeedUtils.daysAgo(10),  185_000.0, "Es kopi + snack", null, budgetsByName["Groceries"], categoriesByName.getValue("Food and Beverages"), TransactionType.EXPENSE),

            // --- Transport ---
            Transactions(0, SeedUtils.daysAgo(2),   300_000.0, "Bensin motor", null, budgetsByName["Transport"], categoriesByName.getValue("Transport"), TransactionType.EXPENSE),
            Transactions(0, SeedUtils.daysAgo(5),    45_000.0, "Parkir mall", null, budgetsByName["Transport"], categoriesByName.getValue("Transport"), TransactionType.EXPENSE),
            Transactions(0, SeedUtils.daysAgo(9),    20_000.0, "Ojek ke kantor", null, budgetsByName["Transport"], categoriesByName.getValue("Transport"), TransactionType.EXPENSE),
            Transactions(0, SeedUtils.daysAgo(13),  350_000.0, "Servis motor ringan", null, budgetsByName["Transport"], categoriesByName.getValue("Transport"), TransactionType.EXPENSE),
            Transactions(0, SeedUtils.daysAgo(18),  150_000.0, "Tol + bensin perjalanan", null, budgetsByName["Transport"], categoriesByName.getValue("Transport"), TransactionType.EXPENSE),

            // --- Shopping ---
            Transactions(0, SeedUtils.daysAgo(3), 2_500_000.0, "Belanja mingguan", null, budgetsByName["Groceries"], categoriesByName.getValue("Food and Beverages"), TransactionType.EXPENSE), // Note: Corrected category to Food based on 'Belanja mingguan' usually being groceries, but using F&B category ID here as per original logic or change to Shopping if appropriate
            Transactions(0, SeedUtils.daysAgo(7),   890_000.0, "Kaos & celana Uniqlo", null, budgetsByName["Shopping"], categoriesByName.getValue("Shopping"), TransactionType.EXPENSE),
            Transactions(0, SeedUtils.daysAgo(11), 1_250_000.0, "Keyboard mechanical", null, budgetsByName["Shopping"], categoriesByName.getValue("Shopping"), TransactionType.EXPENSE),
            Transactions(0, SeedUtils.daysAgo(17),  720_000.0, "Sepatu olahraga", null, budgetsByName["Shopping"], categoriesByName.getValue("Shopping"), TransactionType.EXPENSE),
            Transactions(0, SeedUtils.daysAgo(20),  135_000.0, "Aksesoris HP", null, budgetsByName["Shopping"], categoriesByName.getValue("Shopping"), TransactionType.EXPENSE),

            // --- Bills ---
            Transactions(0, SeedUtils.daysAgo(5), 1_200_000.0, "Listrik PLN", null, budgetsByName["Bills"], categoriesByName.getValue("Bills"), TransactionType.EXPENSE),
            Transactions(0, SeedUtils.daysAgo(12),  450_000.0, "Bayar internet wifi", null, budgetsByName["Bills"], categoriesByName.getValue("Bills"), TransactionType.EXPENSE),
            Transactions(0, SeedUtils.daysAgo(15),  150_000.0, "Pulsa & paket data", null, budgetsByName["Bills"], categoriesByName.getValue("Bills"), TransactionType.EXPENSE),
            Transactions(0, SeedUtils.daysAgo(24),  980_000.0, "Iuran air PAM", null, budgetsByName["Bills"], categoriesByName.getValue("Bills"), TransactionType.EXPENSE),
            Transactions(0, SeedUtils.daysAgo(28),  149_000.0, "Langganan streaming", null, budgetsByName["Bills"], categoriesByName.getValue("Bills"), TransactionType.EXPENSE),

            // --- Entertainment ---
            Transactions(0, SeedUtils.daysAgo(7),    600_000.0, "Tiket film", null, budgetsByName["Entertainment"], categoriesByName.getValue("Entertainment"), TransactionType.EXPENSE),
            Transactions(0, SeedUtils.daysAgo(14),   200_000.0, "Top-up game Genshin", null, budgetsByName["Entertainment"], categoriesByName.getValue("Entertainment"), TransactionType.EXPENSE),
            Transactions(0, SeedUtils.daysAgo(22),   350_000.0, "Board games", null, budgetsByName["Entertainment"], categoriesByName.getValue("Entertainment"), TransactionType.EXPENSE),
            Transactions(0, SeedUtils.daysAgo(26),   110_000.0, "Karaoke 1 jam", null, budgetsByName["Entertainment"], categoriesByName.getValue("Entertainment"), TransactionType.EXPENSE),

            // --- Health ---
            Transactions(0, SeedUtils.daysAgo(8),   450_000.0, "Obat flu", null, budgetsByName["Health"], categoriesByName.getValue("Health"), TransactionType.EXPENSE),
            Transactions(0, SeedUtils.daysAgo(16),  900_000.0, "Pemeriksaan klinik", null, budgetsByName["Health"], categoriesByName.getValue("Health"), TransactionType.EXPENSE),
            Transactions(0, SeedUtils.daysAgo(23),  320_000.0, "Vitamin & suplement", null, budgetsByName["Health"], categoriesByName.getValue("Health"), TransactionType.EXPENSE),
            Transactions(0, SeedUtils.daysAgo(29),  600_000.0, "Gym membership", null, budgetsByName["Health"], categoriesByName.getValue("Health"), TransactionType.EXPENSE),

            // --- Education ---
            Transactions(0, SeedUtils.daysAgo(10),  200_000.0, "Buku catatan", null, budgetsByName["Education"], categoriesByName.getValue("Education"), TransactionType.EXPENSE),
            Transactions(0, SeedUtils.daysAgo(19), 1_400_000.0, "Kursus online Android", null, budgetsByName["Education"], categoriesByName.getValue("Education"), TransactionType.EXPENSE),
            Transactions(0, SeedUtils.daysAgo(27),  125_000.0, "Alat tulis kuliah", null, budgetsByName["Education"], categoriesByName.getValue("Education"), TransactionType.EXPENSE),

            // --- Other ---
            Transactions(0, SeedUtils.daysAgo(21),  300_000.0, "Hadiah teman ulang tahun", null, budgetsByName["Other"], categoriesByName.getValue("Other"), TransactionType.EXPENSE),
            Transactions(0, SeedUtils.daysAgo(25),  180_000.0, "Sedekah mingguan", null, budgetsByName["Other"], categoriesByName.getValue("Other"), TransactionType.EXPENSE)
        )

        // 2. INCOMES (New!)
        // Note: budgetId is null for incomes.
        val incomeList = listOf(
            Transactions(
                id = 0,
                date = SeedUtils.daysAgo(28),
                totalAmount = 25_000_000.0,
                note = "Gaji Bulanan",
                imagePath = null,
                budgetId = null, // Income is not part of a budget limit
                categoryId = categoriesByName.getValue("Salary"),
                type = TransactionType.INCOME
            ),
            Transactions(
                id = 0,
                date = SeedUtils.daysAgo(15),
                totalAmount = 2_500_000.0,
                note = "Proyek Freelance",
                imagePath = null,
                budgetId = null,
                categoryId = categoriesByName.getValue("Bonus"),
                type = TransactionType.INCOME
            ),
            Transactions(
                id = 0,
                date = SeedUtils.daysAgo(5),
                totalAmount = 500_000.0,
                note = "Dividen Saham",
                imagePath = null,
                budgetId = null,
                categoryId = categoriesByName.getValue("Investment"),
                type = TransactionType.INCOME
            )
        )

        transactionsDao.insertAll(expenseList + incomeList)
    }
}

