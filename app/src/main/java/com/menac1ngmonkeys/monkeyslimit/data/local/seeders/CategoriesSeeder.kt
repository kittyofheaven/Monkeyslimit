package com.menac1ngmonkeys.monkeyslimit.data.local.seeders

import com.menac1ngmonkeys.monkeyslimit.data.local.dao.CategoriesDao
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Categories
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.TransactionType

object CategoriesSeeder {
    suspend fun seed(categoriesDao: CategoriesDao) {
        // Define all your categories in one list
        val allCategories = listOf(
            // --- EXPENSE ---
            Categories(0, "Food and Beverages", null, "Makanan, minuman, dan dine-in/takeaway.", TransactionType.EXPENSE),
            Categories(0, "Transport", null, "Transportasi harian: bensin, parkir, tol, ojek/ride-hailing, tiket.", TransactionType.EXPENSE),
            Categories(0, "Shopping", null, "Belanja kebutuhan, fashion, elektronik, dan perlengkapan rumah.", TransactionType.EXPENSE),
            Categories(0, "Bills", null, "Tagihan bulanan: listrik, air, internet, pulsa, langganan.", TransactionType.EXPENSE),
            Categories(0, "Entertainment", null, "Hiburan dan rekreasi: bioskop, game, streaming, hobi.", TransactionType.EXPENSE),
            Categories(0, "Health", null, "Kesehatan: obat, dokter, asuransi, kebugaran.", TransactionType.EXPENSE),
            Categories(0, "Education", null, "Pendidikan: kursus, buku, alat tulis, biaya sekolah.", TransactionType.EXPENSE),
            Categories(0, "Donation", null, "Donasi: donasi perusahaan atau organisasi", TransactionType.EXPENSE),
            Categories(0, "Other", null, "Lain-lain di luar kategori utama.", TransactionType.EXPENSE),

            // --- INCOME ---
            Categories(0, "Salary", null, "Gaji bulanan atau pendapatan utama.", TransactionType.INCOME),
            Categories(0, "Allowance", null, "Uang saku atau pemberian.", TransactionType.INCOME),
            Categories(0, "Bonus", null, "Bonus kerja, THR, atau insentif.", TransactionType.INCOME),
            Categories(0, "Investment", null, "Dividen, keuntungan saham, atau reksa dana.", TransactionType.INCOME),
            Categories(0, "Petty Cash", null, "Uang temuan atau pendapatan kecil lainnya.", TransactionType.INCOME)
        )

        // Thanks to OnConflictStrategy.IGNORE + Unique Index, duplicates are safely skipped.
        categoriesDao.insertAll(allCategories)
    }
}