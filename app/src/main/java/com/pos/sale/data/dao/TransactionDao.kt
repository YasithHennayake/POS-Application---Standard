package com.pos.sale.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pos.sale.data.entities.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: Transaction): Long

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAll(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): Transaction?

    @Query("SELECT * FROM transactions WHERE status = :status ORDER BY timestamp DESC")
    fun getByStatus(status: String): Flow<List<Transaction>>

    @Query("SELECT COUNT(*) FROM transactions WHERE timestamp >= :startOfDay")
    fun getCountSince(startOfDay: Long): Flow<Int>
}
