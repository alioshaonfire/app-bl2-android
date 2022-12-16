package com.buscalibre.app2.activities

import android.os.Bundle
import com.buscalibre.app2.R
import com.buscalibre.app2.fragments.AccumulatedPaymentsFragment
import com.buscalibre.app2.fragments.PaymentHistoryFragment
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.activity_payments.*

class PaymentsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payments)
        initViews()
    }

    private fun initViews(){
        showBackButton()
        showToolbar()
        tvToolbarTitle.text = getString(R.string.text106)
        val adapter = FragmentPagerItemAdapter(
            supportFragmentManager, FragmentPagerItems.with(this)
                .add(getString(R.string.current), AccumulatedPaymentsFragment::class.java)
                .add(getString(R.string.history), PaymentHistoryFragment::class.java)
                .create()
        )
        viewpagerPayments.adapter = adapter
        tabPayments.setViewPager(viewpagerPayments)
    }
}