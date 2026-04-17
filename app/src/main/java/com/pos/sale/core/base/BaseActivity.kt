package com.pos.sale.core.base

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Base Activity that standardises the ViewBinding setup and ViewModel error observation
 */
abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {

    private var _binding: VB? = null
    protected val binding: VB get() = _binding!!

    /** Subclass inflates its ViewBinding here */
    abstract fun inflateBinding(): VB

    /** Called after binding is ready — subclass sets up UI listeners/observers here */
    abstract fun setupUI()

    /**subclass returns its BaseViewModel to enable automatic error observation */
    open fun getBaseViewModel(): BaseViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = inflateBinding()
        setContentView(binding.root)
        observeErrors()
        setupUI()
    }

    /** Collects error state from the ViewModel in a lifecycle-aware manner */
    private fun observeErrors() {
        val viewModel = getBaseViewModel() ?: return
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.errorMessage.collectLatest { error ->
                    error?.let {
                        Toast.makeText(this@BaseActivity, it, Toast.LENGTH_SHORT).show()
                        viewModel.clearError()
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
