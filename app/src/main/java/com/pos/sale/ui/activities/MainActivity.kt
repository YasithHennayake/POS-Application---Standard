package com.pos.sale.ui.activities

import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import com.pos.sale.core.base.BaseActivity
import com.pos.sale.core.base.BaseViewModel
import com.pos.sale.databinding.ActivityMainBinding
import com.pos.sale.viewmodels.SaleViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>() {

    private val saleViewModel: SaleViewModel by viewModels()

    override fun inflateBinding(): ActivityMainBinding =
        ActivityMainBinding.inflate(layoutInflater)

    override fun getBaseViewModel(): BaseViewModel = saleViewModel

    override fun setupUI() {
        WindowCompat.setDecorFitsSystemWindows(window, true)
    }
}
