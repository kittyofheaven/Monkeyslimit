package com.menac1ngmonkeys.monkeyslimit.data.local.seeders

import com.menac1ngmonkeys.monkeyslimit.R
import com.menac1ngmonkeys.monkeyslimit.data.local.dao.CategoriesDao
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Categories
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.TransactionType

object CategoriesSeeder {
    suspend fun seed(categoriesDao: CategoriesDao) {
        // Define all your categories in one list
        val allCategories = listOf(
            // --- EXPENSE ---
            Categories(0, "Food and Beverages", R.drawable.food, "Makanan, minuman, dan dine-in/takeaway.", TransactionType.EXPENSE),
            Categories(0, "Transport", R.drawable.directions_car_48px, "Transportasi harian: bensin, parkir, tol, ojek/ride-hailing, tiket.", TransactionType.EXPENSE),
            Categories(0, "Shopping", R.drawable.shopping_bag_48px, "Belanja kebutuhan, fashion, elektronik, dan perlengkapan rumah.", TransactionType.EXPENSE),
            Categories(0, "Bills", R.drawable.receipt_long_48px, "Tagihan bulanan: listrik, air, internet, pulsa, langganan.", TransactionType.EXPENSE),
            Categories(0, "Entertainment", R.drawable.playing_cards_48px, "Hiburan dan rekreasi: bioskop, game, streaming, hobi.", TransactionType.EXPENSE),
            Categories(0, "Health", R.drawable.heart_check_48px, "Kesehatan: obat, dokter, asuransi, kebugaran.", TransactionType.EXPENSE),
            Categories(0, "Education", R.drawable.cognition_2_48px, "Pendidikan: kursus, buku, alat tulis, biaya sekolah.", TransactionType.EXPENSE),
            Categories(0, "Donation", R.drawable.salary, "Donasi: donasi perusahaan atau organisasi", TransactionType.EXPENSE),
            Categories(0, "Other", R.drawable.paid_48px, "Lain-lain di luar kategori utama.", TransactionType.EXPENSE),

            // --- INCOME ---
            Categories(0, "Salary", R.drawable.paid_48px, "Gaji bulanan atau pendapatan utama.", TransactionType.INCOME),
            Categories(0, "Allowance", R.drawable.paid_48px, "Uang saku atau pemberian.", TransactionType.INCOME),
            Categories(0, "Bonus", R.drawable.paid_48px, "Bonus kerja, THR, atau insentif.", TransactionType.INCOME),
            Categories(0, "Investment", R.drawable.paid_48px, "Dividen, keuntungan saham, atau reksa dana.", TransactionType.INCOME),
            Categories(0, "Petty Cash", R.drawable.paid_48px, "Uang temuan atau pendapatan kecil lainnya.", TransactionType.INCOME)
        )

        // Thanks to OnConflictStrategy.IGNORE + Unique Index, duplicates are safely skipped.
        categoriesDao.insertAll(allCategories)
    }
}