package com.pos.sale.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.pos.sale.data.dao.TransactionDao
import com.pos.sale.data.entities.Transaction
import com.pos.sale.utils.Constants

@Database(
    entities = [Transaction::class],
    version = Constants.Database.VERSION,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
}
