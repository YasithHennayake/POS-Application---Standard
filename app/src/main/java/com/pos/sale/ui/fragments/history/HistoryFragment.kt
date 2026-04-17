package com.pos.sale.ui.fragments.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pos.sale.core.base.BaseFragment
import com.pos.sale.core.base.BaseViewModel
import com.pos.sale.data.entities.Transaction
import com.pos.sale.databinding.FragmentHistoryBinding
import com.pos.sale.databinding.ItemTransactionBinding
import com.pos.sale.utils.toCurrencyString
import com.pos.sale.utils.toFormattedDateTime
import com.pos.sale.viewmodels.SaleViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Fragment displaying the list of past transactions.
 * The adapter is an inner class — it only exists in the context of this fragment
 * and doesn't need to be shared or reused elsewhere.
 */
@AndroidEntryPoint
class HistoryFragment : BaseFragment<FragmentHistoryBinding>() {

    private val saleViewModel: SaleViewModel by activityViewModels()
    private var transactionAdapter: TransactionAdapter? = null

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentHistoryBinding = FragmentHistoryBinding.inflate(inflater, container, false)

    override fun getBaseViewModel(): BaseViewModel = saleViewModel

    override fun setupUI() {
        setupRecyclerView()
        observeTransactions()
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter(emptyList())
        binding.rvTransactions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = transactionAdapter
        }
    }

    private fun observeTransactions() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                saleViewModel.allTransactions.collectLatest { transactions ->
                    transactionAdapter?.updateList(transactions)
                }
            }
        }
    }

    override fun onDestroyView() {
        // Clear adapter reference before binding is nulled by BaseFragment
        transactionAdapter = null
        super.onDestroyView()
    }

    /**
     * Adapter scoped to HistoryFragment — handles rendering of transaction list items.
     * Kept as a private inner class because it is tightly coupled to this fragment
     * and has no reuse scenario elsewhere.
     */
    private inner class TransactionAdapter(
        private var transactions: List<Transaction>
    ) : RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

        inner class ViewHolder(val binding: ItemTransactionBinding) :
            RecyclerView.ViewHolder(binding.root)

        fun updateList(newTransactions: List<Transaction>) {
            transactions = newTransactions
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemTransactionBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val transaction = transactions[position]
            with(holder.binding) {
                tvAmount.text = transaction.amount.toCurrencyString()
                tvStatus.text = transaction.status
                tvTransactionType.text = transaction.transactionType
                tvTimestamp.text = transaction.timestamp.toFormattedDateTime()
                tvCardInfo.text = "${transaction.cardType} •••• ${transaction.cardLastFour}"
            }
        }

        override fun getItemCount(): Int = transactions.size
    }
}
