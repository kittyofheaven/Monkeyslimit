package com.menac1ngmonkeys.monkeyslimit.data.repository

import com.menac1ngmonkeys.monkeyslimit.data.local.dao.CategoriesDao
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Categories
import kotlinx.coroutines.flow.Flow

class CategoriesRepository(private val categoriesDao: CategoriesDao) {

    fun getAllCategories(): Flow<List<Categories>> = categoriesDao.getAllCategories()

    fun getCategoryById(id: Int): Flow<Categories> = categoriesDao.getCategoryById(id)

    suspend fun insert(categories: Categories): Long {
        return categoriesDao.insert(categories)
    }

    suspend fun update(categories: Categories) {
        categoriesDao.update(categories)
    }

    suspend fun delete(categories: Categories) {
        categoriesDao.delete(categories)
    }
}
