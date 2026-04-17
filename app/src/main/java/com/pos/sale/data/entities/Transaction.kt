package com.pos.sale.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a persisted transaction record.
 * Each completed (or failed) transaction is stored locally for history/reporting.
 */
@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val transactionType: String,
    val status: String,
    val cardType: String,
    val cardLastFour: String,
    val transactionId: String,
    val timestamp: Long,
    val message: String
)
