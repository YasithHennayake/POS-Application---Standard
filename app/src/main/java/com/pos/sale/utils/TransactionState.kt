package com.pos.sale.utils

/**
 * Sealed class representing all possible states of a transaction flow.
 * The ViewModel exposes this via StateFlow, and the UI layer reacts to each state.
 */
sealed class TransactionState {

    /** Initial state — no transaction in progress */
    data object Idle : TransactionState()

    /** User is entering the sale amount */
    data object EnteringAmount : TransactionState()

    /** Transaction is being processed (simulated delay) */
    data object Processing : TransactionState()

    /** Transaction completed successfully */
    data class Success(val transactionId: String, val message: String) : TransactionState()

    /** Transaction failed */
    data class Failure(val message: String) : TransactionState()

    /** Transaction was cancelled by the user */
    data object Cancelled : TransactionState()
}
