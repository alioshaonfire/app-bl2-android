package com.buscalibre.app2.activities

import android.content.Intent
import android.os.Bundle
import com.buscalibre.app2.R
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.activity_control_panel.*

class ControlPanelActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_control_panel)
        initViews()
    }

    private fun initViews(){
        showBackButton()
        showToolbar()
        tvToolbarTitle.text = getString(R.string.panel_de_control)

        btShowcase.setOnClickListener {
            val intent = Intent(applicationContext, ShowcaseActivity::class.java)
            startActivity(intent)
        }
        btPayments.setOnClickListener {
            val intent = Intent(applicationContext, PaymentsActivity::class.java)
            startActivity(intent)
        }
        btMyBooks.setOnClickListener {
            val intent = Intent(applicationContext, MyBooksSaleActivity::class.java)
            startActivity(intent)
        }
    }
}