package com.menac1ngmonkeys.monkeyslimit.data.local.seeders

import com.menac1ngmonkeys.monkeyslimit.data.local.dao.TransactionsDao
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Transactions

object TransactionsSeeder {
    suspend fun seed(
        transactionsDao: TransactionsDao,
        budgetsByName: Map<String, Int>,
        categoriesByName: Map<String, Int>
    ) {
        if (transactionsDao.count() > 0) return

        val txList = listOf(
            // --- Food and Beverages ---
            Transactions(0, SeedUtils.daysAgo(0), 3_910_000.0, "Lunch with president", null, budgetsByName.getValue("Groceries"), categoriesByName.getValue("Food and Beverages")),
            Transactions(0, SeedUtils.daysAgo(1),   910_000.0, "Lunch with team", null, budgetsByName.getValue("Groceries"), categoriesByName.getValue("Food and Beverages")),
            Transactions(0, SeedUtils.daysAgo(4),   120_000.0, "Sarapan roti & kopi", null, budgetsByName.getValue("Groceries"), categoriesByName.getValue("Food and Beverages")),
            Transactions(0, SeedUtils.daysAgo(6),   350_000.0, "Makan malam restoran Jepang", null, budgetsByName.getValue("Groceries"), categoriesByName.getValue("Food and Beverages")),
            Transactions(0, SeedUtils.daysAgo(10),  185_000.0, "Es kopi + snack", null, budgetsByName.getValue("Groceries"), categoriesByName.getValue("Food and Beverages")),

            // --- Transport ---
            Transactions(0, SeedUtils.daysAgo(2),   300_000.0, "Bensin motor", null, budgetsByName.getValue("Transport"), categoriesByName.getValue("Transport")),
            Transactions(0, SeedUtils.daysAgo(5),    45_000.0, "Parkir mall", null, budgetsByName.getValue("Transport"), categoriesByName.getValue("Transport")),
            Transactions(0, SeedUtils.daysAgo(9),    20_000.0, "Ojek ke kantor", null, budgetsByName.getValue("Transport"), categoriesByName.getValue("Transport")),
            Transactions(0, SeedUtils.daysAgo(13),  350_000.0, "Servis motor ringan", null, budgetsByName.getValue("Transport"), categoriesByName.getValue("Transport")),
            Transactions(0, SeedUtils.daysAgo(18),  150_000.0, "Tol + bensin perjalanan", null, budgetsByName.getValue("Transport"), categoriesByName.getValue("Transport")),

            // --- Shopping ---
            Transactions(0, SeedUtils.daysAgo(3), 2_500_000.0, "Belanja mingguan", null, budgetsByName.getValue("Groceries"), categoriesByName.getValue("Food and Beverages")),
            Transactions(0, SeedUtils.daysAgo(7),   890_000.0, "Kaos & celana Uniqlo", null, budgetsByName.getValue("Shopping"), categoriesByName.getValue("Shopping")),
            Transactions(0, SeedUtils.daysAgo(11), 1_250_000.0, "Keyboard mechanical", null, budgetsByName.getValue("Shopping"), categoriesByName.getValue("Shopping")),
            Transactions(0, SeedUtils.daysAgo(17),  720_000.0, "Sepatu olahraga", null, budgetsByName.getValue("Shopping"), categoriesByName.getValue("Shopping")),
            Transactions(0, SeedUtils.daysAgo(20),  135_000.0, "Aksesoris HP", null, budgetsByName.getValue("Shopping"), categoriesByName.getValue("Shopping")),

            // --- Bills ---
            Transactions(0, SeedUtils.daysAgo(5), 1_200_000.0, "Listrik PLN", null, budgetsByName.getValue("Household Monthly"), categoriesByName.getValue("Bills")),
            Transactions(0, SeedUtils.daysAgo(12),  450_000.0, "Bayar internet wifi", null, budgetsByName.getValue("Household Monthly"), categoriesByName.getValue("Bills")),
            Transactions(0, SeedUtils.daysAgo(15),  150_000.0, "Pulsa & paket data", null, budgetsByName.getValue("Household Monthly"), categoriesByName.getValue("Bills")),
            Transactions(0, SeedUtils.daysAgo(24),  980_000.0, "Iuran air PAM", null, budgetsByName.getValue("Household Monthly"), categoriesByName.getValue("Bills")),
            Transactions(0, SeedUtils.daysAgo(28),  149_000.0, "Langganan streaming", null, budgetsByName.getValue("Household Monthly"), categoriesByName.getValue("Bills")),

            // --- Entertainment ---
            Transactions(0, SeedUtils.daysAgo(7),    600_000.0, "Tiket film", null, budgetsByName.getValue("Household Monthly"), categoriesByName.getValue("Entertainment")),
            Transactions(0, SeedUtils.daysAgo(14),   200_000.0, "Top-up game Genshin", null, budgetsByName.getValue("Entertainment"), categoriesByName.getValue("Entertainment")),
            Transactions(0, SeedUtils.daysAgo(22),   350_000.0, "Board games", null, budgetsByName.getValue("Entertainment"), categoriesByName.getValue("Entertainment")),
            Transactions(0, SeedUtils.daysAgo(26),   110_000.0, "Karaoke 1 jam", null, budgetsByName.getValue("Entertainment"), categoriesByName.getValue("Entertainment")),

            // --- Health ---
            Transactions(0, SeedUtils.daysAgo(8),   450_000.0, "Obat flu", null, budgetsByName.getValue("Household Monthly"), categoriesByName.getValue("Health")),
            Transactions(0, SeedUtils.daysAgo(16),  900_000.0, "Pemeriksaan klinik", null, budgetsByName.getValue("Health"), categoriesByName.getValue("Health")),
            Transactions(0, SeedUtils.daysAgo(23),  320_000.0, "Vitamin & suplement", null, budgetsByName.getValue("Health"), categoriesByName.getValue("Health")),
            Transactions(0, SeedUtils.daysAgo(29),  600_000.0, "Gym membership", null, budgetsByName.getValue("Health"), categoriesByName.getValue("Health")),

            // --- Education ---
            Transactions(0, SeedUtils.daysAgo(10),  200_000.0, "Buku catatan", null, budgetsByName.getValue("Education"), categoriesByName.getValue("Education")),
            Transactions(0, SeedUtils.daysAgo(19), 1_400_000.0, "Kursus online Android", null, budgetsByName.getValue("Education"), categoriesByName.getValue("Education")),
            Transactions(0, SeedUtils.daysAgo(27),  125_000.0, "Alat tulis kuliah", null, budgetsByName.getValue("Education"), categoriesByName.getValue("Education")),

            // --- Other ---
            Transactions(0, SeedUtils.daysAgo(21),  300_000.0, "Hadiah teman ulang tahun", null, budgetsByName.getValue("Other"), categoriesByName.getValue("Other")),
            Transactions(0, SeedUtils.daysAgo(25),  180_000.0, "Sedekah mingguan", null, budgetsByName.getValue("Other"), categoriesByName.getValue("Other"))
        )
        transactionsDao.insertAll(txList)
    }
}

