package com.buscalibre.app2.activities

import android.os.Bundle
import com.buscalibre.app2.R
import com.buscalibre.app2.fragments.PublishedFragment
import com.buscalibre.app2.fragments.ToPostFragment
import com.buscalibre.app2.util.AlertDialogHelper
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.activity_showcase.*


class ShowcaseActivity : BaseActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_showcase)
        initViews()
    }

    private fun initViews(){
        showBackButton()
        showToolbar()
        tvToolbarTitle.text = getString(R.string.text107)
        val adapter = FragmentPagerItemAdapter(
            supportFragmentManager, FragmentPagerItems.with(this)
                .add(getString(R.string.text111), ToPostFragment::class.java)
                .add(getString(R.string.text110), PublishedFragment::class.java)
                .create()
        )
        viewpagerShowcase.adapter = adapter
        tabShowcase.setViewPager(viewpagerShowcase)
    }

}