package com.buscalibre.app2.activities

import android.content.Intent
import android.os.Bundle
import com.buscalibre.app2.R
import com.buscalibre.app2.util.AlertDialogHelper
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.activity_sell_success.*

class SellSuccessActivity : BaseActivity(), AlertDialogHelper.AlertDialogListener {
    private var sellerId = 0
    private var purchaseOrderId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sell_success)
        initViews()
        initData()
    }

    private fun initData() {
        if(intent.extras != null){
            sellerId = intent.extras!!.getInt("sellerId")
            purchaseOrderId = intent.extras!!.getInt("purchaseOrderId")
            tvOrderNumber.text = getString(R.string.number_sell_id) + purchaseOrderId
        }
    }

    private fun initViews(){
        //showCloseButton()
        showToolbar()
        //alertDialogHelper = AlertDialogHelper(this@SellSuccessActivity)
        tvToolbarTitle.text = getString(R.string.text78)
        btFinishSellBooks.setOnClickListener {
            finish()
        }
        tvGoToConsig.setOnClickListener {
            val intent = Intent(this, ShowcaseActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onPositiveClick(from: Int) {

    }

    override fun onNegativeClick(from: Int) {
    }

    override fun onNeutralClick(from: Int) {
    }
}