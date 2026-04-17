package com.pos.sale.utils

object Constants {

    object TransactionType {
        const val SALE = "SALE"
        const val REFUND = "REFUND"
    }

    object TransactionStatus {
        const val APPROVED = "APPROVED"
        const val DECLINED = "DECLINED"
        const val CANCELLED = "CANCELLED"
    }

    object CardType {
        const val VISA = "VISA"
        const val MASTERCARD = "MASTERCARD"
        const val AMEX = "AMEX"
    }

    object Database {
        const val NAME = "pos_sale_database"
        const val VERSION = 1
    }

    object Simulation {
        const val PROCESSING_DELAY_MS = 3000L
        const val SUCCESS_RATE_PERCENT = 70
    }

    object Merchant {
        const val NAME = "NOVUS POS MERCHANT"
        const val TERMINAL_ID = "TID-001"
    }

    object Validation {
        const val MIN_AMOUNT = 0.01
        const val MAX_AMOUNT = 999999.99
    }

    object DefaultCard {
        const val TYPE = "N/A"
        const val LAST_FOUR = "0000"
    }
}
