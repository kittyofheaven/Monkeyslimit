package com.menac1ngmonkeys.monkeyslimit.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Categories
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoriesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(categories: Categories): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<Categories>)

    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<Categories>>

    @Query("SELECT * FROM categories WHERE id = :id")
    fun getCategoryById(id: Int): Flow<Categories?>

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun count(): Int

    @Update
    suspend fun update(categories: Categories)

    @Delete
    suspend fun delete(categories: Categories)
}
