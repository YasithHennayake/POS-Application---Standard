package com.pos.sale.ui.fragments.sale

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.pos.sale.R
import com.pos.sale.core.base.BaseFragment
import com.pos.sale.core.base.BaseViewModel
import com.pos.sale.databinding.FragmentSaleBinding
import com.pos.sale.utils.Constants
import com.pos.sale.utils.TransactionState
import com.pos.sale.utils.toFormattedDateTime
import com.pos.sale.utils.toLkrString
import com.pos.sale.viewmodels.SaleViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class SaleFragment : BaseFragment<FragmentSaleBinding>() {

    private val saleViewModel: SaleViewModel by activityViewModels()
    private val dateTimeHandler = Handler(Looper.getMainLooper())
    private lateinit var dateTimeRunnable: Runnable
    private var isProcessingInput = false

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSaleBinding = FragmentSaleBinding.inflate(inflater, container, false)

    override fun getBaseViewModel(): BaseViewModel = saleViewModel

    override fun setupUI() {
        setupHeader()
        setupTransactionTypeToggle()
        setupAmountInput()
        setupFocusClearing()
        setupActionButtons()
        observeAmountDisplay()
        observeValidationErrors()
        observeTodayCount()
        observeTransactionState()
    }

    private fun setupHeader() {
        binding.tvMerchantName.text = Constants.Merchant.NAME
        binding.tvTerminalId.text = getString(R.string.terminal_label) + " " + Constants.Merchant.TERMINAL_ID

        dateTimeRunnable = object : Runnable {
            override fun run() {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                binding.tvDateTime.text = dateFormat.format(Date())
                dateTimeHandler.postDelayed(this, 1000)
            }
        }
        dateTimeHandler.post(dateTimeRunnable)
    }

    private fun setupTransactionTypeToggle() {
        binding.toggleTransactionType.check(R.id.btnTypeSale)

        binding.toggleTransactionType.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val type = when (checkedId) {
                    R.id.btnTypeSale -> Constants.TransactionType.SALE
                    R.id.btnTypeRefund -> Constants.TransactionType.REFUND
                    else -> Constants.TransactionType.SALE
                }
                saleViewModel.selectTransactionType(type)
            }
        }
    }

    private fun setupAmountInput() {
        // Tap amount card → focus hidden input → show keyboard
        binding.cardAmount.setOnClickListener {
            binding.etHiddenInput.requestFocus()
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.etHiddenInput, InputMethodManager.SHOW_IMPLICIT)
        }

        // Hidden EditText captures keyboard: each digit → onDigitPressed, deletion → onBackspacePressed
        binding.etHiddenInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isProcessingInput) return
                isProcessingInput = true

                val text = s?.toString() ?: ""
                if (text.isNotEmpty()) {
                    // Process each new digit
                    for (ch in text) {
                        if (ch.isDigit()) {
                            saleViewModel.onDigitPressed(ch.digitToInt())
                        }
                    }
                    s?.clear()
                } else if (s != null && s.isEmpty()) {
                    //
                }

                isProcessingInput = false
            }
        })

        // Catch backspace/delete key since TextWatcher doesn't reliably detect deletion on empty text
        binding.etHiddenInput.setOnKeyListener { _, keyCode, event ->
            if (event.action == android.view.KeyEvent.ACTION_DOWN &&
                (keyCode == android.view.KeyEvent.KEYCODE_DEL || keyCode == android.view.KeyEvent.KEYCODE_FORWARD_DEL)
            ) {
                saleViewModel.onBackspacePressed()
                true
            } else {
                false
            }
        }
    }

    @Suppress("ClickableViewAccessibility")
    private fun setupFocusClearing() {
        binding.rootLayout.setOnTouchListener { _, _ ->
            binding.etHiddenInput.clearFocus()
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.etHiddenInput.windowToken, 0)
            false
        }
    }

    private fun setupActionButtons() {
        binding.btnClearAll.setOnClickListener {
            saleViewModel.clearAll()
            binding.toggleTransactionType.check(R.id.btnTypeSale)
        }

        binding.btnCharge.setOnClickListener {
            saleViewModel.validateAndCharge()
        }

        binding.btnCancelProcessing.setOnClickListener {
            saleViewModel.cancelTransaction()
        }

        binding.btnNewTransaction.setOnClickListener {
            saleViewModel.clearAll()
            binding.toggleTransactionType.check(R.id.btnTypeSale)
        }
    }

    private fun observeAmountDisplay() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                saleViewModel.amountDisplay.collectLatest { display ->
                    binding.tvAmountDisplay.text = display

                    // Update summary amount
                    binding.tvSummaryAmount.text = display
                    binding.tvSummaryType.text = saleViewModel.selectedTransactionType.value

                    // Update charge button
                    val cents = saleViewModel.amountCents.value
                    if (cents > 0) {
                        binding.btnCharge.text = getString(R.string.btn_charge_with_amount, display.removePrefix("LKR "))
                        binding.btnCharge.isEnabled = true
                    } else {
                        binding.btnCharge.text = getString(R.string.btn_charge)
                        binding.btnCharge.isEnabled = false
                    }
                }
            }
        }
    }

    private fun observeValidationErrors() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                saleViewModel.validationError.collectLatest { error ->
                    if (error != null) {
                        binding.tvAmountError.text = error
                        binding.tvAmountError.visibility = View.VISIBLE
                    } else {
                        binding.tvAmountError.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun observeTodayCount() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                saleViewModel.todayTransactionCount.collectLatest { count ->
                    binding.tvTodayCount.text = getString(R.string.transactions_today, count)
                }
            }
        }
    }

    private fun observeTransactionState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                saleViewModel.transactionState.collectLatest { state ->
                    when (state) {
                        is TransactionState.Idle,
                        is TransactionState.EnteringAmount -> {
                            binding.processingOverlay.visibility = View.GONE
                            binding.resultOverlay.visibility = View.GONE
                            setInputsEnabled(true)
                        }

                        is TransactionState.Processing -> {
                            binding.processingOverlay.visibility = View.VISIBLE
                            binding.resultOverlay.visibility = View.GONE
                            setInputsEnabled(false)
                        }

                        is TransactionState.Success -> {
                            binding.processingOverlay.visibility = View.GONE
                            binding.resultOverlay.visibility = View.VISIBLE
                            setInputsEnabled(false)

                            binding.tvResultIcon.text = "\u2713"
                            binding.tvResultIcon.setTextColor(
                                ContextCompat.getColor(requireContext(), R.color.success)
                            )
                            binding.tvResultStatus.text = getString(R.string.result_approved)
                            binding.tvResultStatus.setTextColor(
                                ContextCompat.getColor(requireContext(), R.color.success)
                            )
                            binding.tvResultMessage.text = state.message
                            binding.tvResultTransactionId.text = state.transactionId
                            binding.rowTransactionId.visibility = View.VISIBLE
                            binding.tvResultAmount.text = saleViewModel.getAmountAsDouble().toLkrString()
                            binding.tvResultDetails.text = saleViewModel.selectedTransactionType.value
                            binding.tvResultTimestamp.text = System.currentTimeMillis().toFormattedDateTime()
                        }

                        is TransactionState.Failure -> {
                            binding.processingOverlay.visibility = View.GONE
                            binding.resultOverlay.visibility = View.VISIBLE
                            setInputsEnabled(false)

                            binding.tvResultIcon.text = "\u2717"
                            binding.tvResultIcon.setTextColor(
                                ContextCompat.getColor(requireContext(), R.color.error)
                            )
                            binding.tvResultStatus.text = getString(R.string.result_declined)
                            binding.tvResultStatus.setTextColor(
                                ContextCompat.getColor(requireContext(), R.color.error)
                            )
                            binding.tvResultMessage.text = state.message
                            binding.rowTransactionId.visibility = View.GONE
                            binding.tvResultAmount.text = saleViewModel.getAmountAsDouble().toLkrString()
                            binding.tvResultDetails.text = saleViewModel.selectedTransactionType.value
                            binding.tvResultTimestamp.text = System.currentTimeMillis().toFormattedDateTime()
                        }

                        is TransactionState.Cancelled -> {
                            binding.processingOverlay.visibility = View.GONE
                            binding.resultOverlay.visibility = View.VISIBLE
                            setInputsEnabled(false)

                            binding.tvResultIcon.text = "!"
                            binding.tvResultIcon.setTextColor(
                                ContextCompat.getColor(requireContext(), R.color.warning)
                            )
                            binding.tvResultStatus.text = getString(R.string.transaction_cancelled_title)
                            binding.tvResultStatus.setTextColor(
                                ContextCompat.getColor(requireContext(), R.color.warning)
                            )
                            binding.tvResultMessage.text = getString(R.string.transaction_cancelled_message)
                            binding.rowTransactionId.visibility = View.GONE
                            binding.tvResultAmount.text = saleViewModel.getAmountAsDouble().toLkrString()
                            binding.tvResultDetails.text = saleViewModel.selectedTransactionType.value
                            binding.tvResultTimestamp.text = System.currentTimeMillis().toFormattedDateTime()
                        }
                    }
                }
            }
        }
    }

    private fun setInputsEnabled(enabled: Boolean) {
        binding.mainContent.alpha = if (enabled) 1.0f else 0.3f
        binding.cardAmount.isEnabled = enabled
        binding.btnCharge.isEnabled = enabled
        binding.btnClearAll.isEnabled = enabled
        binding.toggleTransactionType.isEnabled = enabled
        if (!enabled) {
            binding.etHiddenInput.clearFocus()
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.etHiddenInput.windowToken, 0)
        }
    }

    override fun onDestroyView() {
        dateTimeHandler.removeCallbacks(dateTimeRunnable)
        super.onDestroyView()
    }
}
