package com.pos.sale.viewmodels

import androidx.lifecycle.viewModelScope
import com.pos.sale.core.base.BaseViewModel
import com.pos.sale.data.entities.Transaction
import com.pos.sale.repository.TransactionRepository
import com.pos.sale.requestmodel.SaleRequest
import com.pos.sale.utils.Constants
import com.pos.sale.utils.TransactionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SaleViewModel @Inject constructor(
    private val repository: TransactionRepository
) : BaseViewModel() {

    private val _transactionState = MutableStateFlow<TransactionState>(TransactionState.Idle)
    val transactionState: StateFlow<TransactionState> = _transactionState.asStateFlow()

    val allTransactions = repository.getAllTransactions()

    val todayTransactionCount = repository.getTodayTransactionCount()

    private var processingJob: Job? = null

    // --- Form State (cents-based) ---

    private val _amountCents = MutableStateFlow(0L)
    val amountCents: StateFlow<Long> = _amountCents.asStateFlow()

    val amountDisplay: StateFlow<String> = _amountCents
        .map { formatCentsToDisplay(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, formatCentsToDisplay(0L))

    private val _selectedTransactionType = MutableStateFlow(Constants.TransactionType.SALE)
    val selectedTransactionType: StateFlow<String> = _selectedTransactionType.asStateFlow()

    private val _validationError = MutableStateFlow<String?>(null)
    val validationError: StateFlow<String?> = _validationError.asStateFlow()

    // --- Phase 1 Methods ---

    fun startAmountEntry() {
        _transactionState.value = TransactionState.EnteringAmount
    }

    fun startTransaction(request: SaleRequest) {
        processingJob = viewModelScope.launch {
            _transactionState.value = TransactionState.Processing
            try {
                val response = repository.processTransaction(request)
                _transactionState.value = if (response.success) {
                    TransactionState.Success(
                        transactionId = response.transactionId,
                        message = response.message
                    )
                } else {
                    TransactionState.Failure(message = response.message)
                }
            } catch (e: Exception) {
                _transactionState.value = TransactionState.Failure(
                    message = e.message ?: "An unexpected error occurred"
                )
                setError(e.message ?: "An unexpected error occurred")
            }
        }
    }

    fun cancelTransaction() {
        processingJob?.cancel()
        processingJob = null
        _transactionState.value = TransactionState.Cancelled
    }

    fun resetState() {
        _transactionState.value = TransactionState.Idle
    }

    suspend fun getTransactionById(id: Long): Transaction? =
        repository.getTransactionById(id)

    // --- Cents-based Amount Methods ---

    fun onDigitPressed(digit: Int) {
        val newAmount = _amountCents.value * 10 + digit
        // Max 999,999.99 = 99999999 cents
        if (newAmount <= 99_999_999L) {
            _amountCents.value = newAmount
        }
        _validationError.value = null
    }

    fun onBackspacePressed() {
        _amountCents.value = _amountCents.value / 10
        _validationError.value = null
    }

    fun onClearAmount() {
        _amountCents.value = 0L
        _validationError.value = null
    }

    fun selectTransactionType(type: String) {
        _selectedTransactionType.value = type
    }

    fun getAmountAsDouble(): Double {
        return _amountCents.value / 100.0
    }

    fun validateAndCharge() {
        val amount = getAmountAsDouble()

        when {
            amount < Constants.Validation.MIN_AMOUNT -> {
                _validationError.value = "Please enter an amount"
                return
            }
            amount > Constants.Validation.MAX_AMOUNT -> {
                _validationError.value = "Amount exceeds maximum limit"
                return
            }
        }

        _validationError.value = null

        val request = SaleRequest(
            amount = amount,
            transactionType = _selectedTransactionType.value
        )
        startTransaction(request)
    }

    fun clearAll() {
        _amountCents.value = 0L
        _selectedTransactionType.value = Constants.TransactionType.SALE
        _validationError.value = null
        resetState()
    }

    companion object {
        fun formatCentsToDisplay(cents: Long): String {
            val whole = cents / 100
            val fraction = cents % 100
            val nf = NumberFormat.getIntegerInstance(Locale.US)
            return "LKR ${nf.format(whole)}.${"%02d".format(fraction)}"
        }
    }
}
