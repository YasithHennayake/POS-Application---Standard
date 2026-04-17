package com.pos.sale.repository

import com.pos.sale.data.dao.TransactionDao
import com.pos.sale.data.entities.Transaction
import com.pos.sale.requestmodel.SaleRequest
import com.pos.sale.responsemodel.TransactionResponse
import com.pos.sale.utils.Constants
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao
) {

    fun getAllTransactions(): Flow<List<Transaction>> = transactionDao.getAll()

    suspend fun getTransactionById(id: Long): Transaction? = transactionDao.getById(id)

    fun getTransactionsByStatus(status: String): Flow<List<Transaction>> =
        transactionDao.getByStatus(status)

    fun getTodayTransactionCount(): Flow<Int> {
        val startOfDay = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        return transactionDao.getCountSince(startOfDay)
    }

    suspend fun processTransaction(request: SaleRequest): TransactionResponse {
        delay(Constants.Simulation.PROCESSING_DELAY_MS)

        val isSuccess = Random.nextInt(100) < Constants.Simulation.SUCCESS_RATE_PERCENT
        val timestamp = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
        val randomDigits = Random.nextInt(100, 999).toString()
        val transactionId = "TXN${dateFormat.format(Date(timestamp))}$randomDigits"

        val status = if (isSuccess) Constants.TransactionStatus.APPROVED
        else Constants.TransactionStatus.DECLINED

        val message = if (isSuccess) "Transaction approved"
        else "Transaction declined by issuer"

        val transaction = Transaction(
            amount = request.amount,
            transactionType = request.transactionType,
            status = status,
            cardType = Constants.DefaultCard.TYPE,
            cardLastFour = Constants.DefaultCard.LAST_FOUR,
            transactionId = transactionId,
            timestamp = timestamp,
            message = message
        )
        transactionDao.insert(transaction)

        return TransactionResponse(
            success = isSuccess,
            transactionId = transactionId,
            message = message,
            timestamp = timestamp
        )
    }
}
