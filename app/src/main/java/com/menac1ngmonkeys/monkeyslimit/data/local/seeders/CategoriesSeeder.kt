package com.menac1ngmonkeys.monkeyslimit.data.local.seeders

import com.menac1ngmonkeys.monkeyslimit.data.local.dao.CategoriesDao
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Categories

object CategoriesSeeder {
    suspend fun seed(categoriesDao: CategoriesDao) {
        val existing = categoriesDao.count()
        if (existing > 0) return

        val defaultCategories = listOf(
            Categories(
                id = 0,
                name = "Food and Beverages",
                icon = null,
                description = "Makanan, minuman, dan dine-in/takeaway."
            ),
            Categories(
                id = 0,
                name = "Transport",
                icon = null,
                description = "Transportasi harian: bensin, parkir, tol, ojek/ride-hailing, tiket."
            ),
            Categories(
                id = 0,
                name = "Shopping",
                icon = null,
                description = "Belanja kebutuhan, fashion, elektronik, dan perlengkapan rumah."
            ),
            Categories(
                id = 0,
                name = "Bills",
                icon = null,
                description = "Tagihan bulanan: listrik, air, internet, pulsa, langganan."
            ),
            Categories(
                id = 0,
                name = "Entertainment",
                icon = null,
                description = "Hiburan dan rekreasi: bioskop, game, streaming, hobi."
            ),
            Categories(
                id = 0,
                name = "Health",
                icon = null,
                description = "Kesehatan: obat, dokter, asuransi, kebugaran."
            ),
            Categories(
                id = 0,
                name = "Education",
                icon = null,
                description = "Pendidikan: kursus, buku, alat tulis, biaya sekolah."
            ),
            Categories(
                id = 0,
                name = "Other",
                icon = null,
                description = "Lain-lain di luar kategori utama."
            )
        )

        categoriesDao.insertAll(defaultCategories)
    }
}
