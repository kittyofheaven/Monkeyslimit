package com.menac1ngmonkeys.monkeyslimit.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.Transactions
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transactions: Transactions): Long

    @Query("SELECT * FROM transactions")
    fun getAllTransactions(): Flow<List<Transactions>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    fun getTransactionById(id: Int): Flow<Transactions?>

    @Update
    suspend fun update(transactions: Transactions)

    @Delete
    suspend fun delete(transactions: Transactions)
}
