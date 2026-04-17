package com.pos.sale.responsemodel

/**
 * Data class representing the result of a processed transaction.
 * Returned by the repository's simulated processing logic.
 */
data class TransactionResponse(
    val success: Boolean,
    val transactionId: String,
    val message: String,
    val timestamp: Long
)
