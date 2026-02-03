package com.menac1ngmonkeys.monkeyslimit.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Categories
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.TransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoriesDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(categories: Categories): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(categories: List<Categories>)

    // Filter by type (e.g., show only Income categories in dropdown)
    @Query("SELECT * FROM categories WHERE type = :type")
    fun getCategoriesByType(type: TransactionType): Flow<List<Categories>>

    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<Categories>>

    @Query("SELECT * FROM categories")
    suspend fun getAllNow(): List<Categories>

    @Query("SELECT * FROM categories WHERE id = :id")
    fun getCategoryById(id: Int): Flow<Categories?>

    // --- ADDED THIS METHOD TO FIX DUPLICATES ---
    @Query("SELECT * FROM categories WHERE name = :name AND type = :type LIMIT 1")
    suspend fun getCategoryByNameAndType(name: String, type: TransactionType): Categories?
    // -------------------------------------------

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun count(): Int

    @Update
    suspend fun update(categories: Categories)

    @Delete
    suspend fun delete(categories: Categories)
}
