package com.pos.sale.utils

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Extension functions to keep formatting logic reusable and out of business classes.
 */

/** Formats a Double amount as currency (e.g., "$12.50") */
fun Double.toCurrencyString(): String {
    return NumberFormat.getCurrencyInstance(Locale.US).format(this)
}

/** Formats a timestamp (millis) into a human-readable date/time string */
fun Long.toFormattedDateTime(): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return dateFormat.format(Date(this))
}

/** Masks a card number to show only the last four digits (e.g., "**** 1234") */
fun String.maskCardNumber(): String {
    return if (length >= 4) "**** ${takeLast(4)}" else this
}

/** Formats a Double amount as LKR currency (e.g., "LKR 12.50") */
fun Double.toLkrString(): String {
    return "LKR %.2f".format(this)
}
