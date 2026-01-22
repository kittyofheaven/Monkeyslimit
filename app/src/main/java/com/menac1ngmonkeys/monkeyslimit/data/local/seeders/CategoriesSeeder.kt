package com.menac1ngmonkeys.monkeyslimit.data.local.seeders

import com.menac1ngmonkeys.monkeyslimit.data.local.dao.CategoriesDao
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Categories
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.TransactionType

object CategoriesSeeder {
    suspend fun seed(categoriesDao: CategoriesDao) {
        val existing = categoriesDao.count()
        if (existing > 0) return

        // 1. EXPENSE CATEGORIES
        val expenseCategories = listOf(
            Categories(
                id = 0,
                name = "Food and Beverages",
                icon = null,
                description = "Makanan, minuman, dan dine-in/takeaway.",
                type = TransactionType.EXPENSE
            ),
            Categories(
                id = 0,
                name = "Transport",
                icon = null,
                description = "Transportasi harian: bensin, parkir, tol, ojek/ride-hailing, tiket.",
                type = TransactionType.EXPENSE
            ),
            Categories(
                id = 0,
                name = "Shopping",
                icon = null,
                description = "Belanja kebutuhan, fashion, elektronik, dan perlengkapan rumah.",
                type = TransactionType.EXPENSE
            ),
            Categories(
                id = 0,
                name = "Bills",
                icon = null,
                description = "Tagihan bulanan: listrik, air, internet, pulsa, langganan.",
                type = TransactionType.EXPENSE
            ),
            Categories(
                id = 0,
                name = "Entertainment",
                icon = null,
                description = "Hiburan dan rekreasi: bioskop, game, streaming, hobi.",
                type = TransactionType.EXPENSE
            ),
            Categories(
                id = 0,
                name = "Health",
                icon = null,
                description = "Kesehatan: obat, dokter, asuransi, kebugaran.",
                type = TransactionType.EXPENSE
            ),
            Categories(
                id = 0,
                name = "Education",
                icon = null,
                description = "Pendidikan: kursus, buku, alat tulis, biaya sekolah.",
                type = TransactionType.EXPENSE
            ),
            Categories(
                id = 0,
                name = "Donation",
                icon = null,
                description = "Donasi: donasi perusahaan atau organisasi",
                type = TransactionType.EXPENSE
            ),
            Categories(
                id = 0,
                name = "Other",
                icon = null,
                description = "Lain-lain di luar kategori utama.",
                type = TransactionType.EXPENSE
            )
        )

        // 2. INCOME CATEGORIES (New!)
        val incomeCategories = listOf(
            Categories(
                id = 0,
                name = "Salary",
                icon = null, // You can add resource IDs or string names here if you have icons
                description = "Gaji bulanan atau pendapatan utama.",
                type = TransactionType.INCOME
            ),
            Categories(
                id = 0,
                name = "Allowance",
                icon = null,
                description = "Uang saku atau pemberian.",
                type = TransactionType.INCOME
            ),
            Categories(
                id = 0,
                name = "Bonus",
                icon = null,
                description = "Bonus kerja, THR, atau insentif.",
                type = TransactionType.INCOME
            ),
            Categories(
                id = 0,
                name = "Investment",
                icon = null,
                description = "Dividen, keuntungan saham, atau reksa dana.",
                type = TransactionType.INCOME
            ),
            Categories(
                id = 0,
                name = "Petty Cash",
                icon = null,
                description = "Uang temuan atau pendapatan kecil lainnya.",
                type = TransactionType.INCOME
            )
        )

        categoriesDao.insertAll(expenseCategories + incomeCategories)
    }
}
