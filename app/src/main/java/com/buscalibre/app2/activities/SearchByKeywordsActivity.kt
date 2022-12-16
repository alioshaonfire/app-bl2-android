package com.buscalibre.app2.activities

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import com.buscalibre.app2.R
import com.buscalibre.app2.constants.AppConstants
import com.buscalibre.app2.util.AlertDialogHelper
import com.buscalibre.app2.util.ViewUtil
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.activity_search_by_keywords.*


private var url = ""

class SearchByKeywordsActivity : BaseActivity(), AlertDialogHelper.AlertDialogListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_by_keywords)
        initViews()
        getDataFromIntent()
    }

    private fun getDataFromIntent(){
        if (intent.extras != null){
            url = intent.getStringExtra("url")!!
        }else{
            alertDialogHelper.showAlertDialog(getString(R.string.app_name), getString(
                R.string.text21), getString(R.string.accept_dialog),"","",1, false)
        }
    }

    private fun initViews(){
        //alertDialogHelper = AlertDialogHelper(this@SearchByKeywordsActivity)
        tvToolbarTitle.text = getString(R.string.text18)
        showBackButton()
        etKeySearch.requestFocus()
        ViewUtil.openKeyboard(this@SearchByKeywordsActivity)
        etKeySearch.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent): Boolean {
                // If the event is a key-down event on the "enter" button
                if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {

                    searchOnClick(etKeySearch)
                    return true
                }
                return false
            }
        })
    }



    override fun onPositiveClick(from: Int) {
        if(from == 1){
            finish()
        }
    }

    override fun onNegativeClick(from: Int) {

    }

    override fun onNeutralClick(from: Int) {

    }

    fun searchOnClick(view: View) {
        if (etKeySearch.text.toString().trim().isNotEmpty()){
            ViewUtil.hideKeyboard(this@SearchByKeywordsActivity)
            val intent = Intent(applicationContext, BaseWebViewActivity::class.java)
            intent.putExtra("url", url)
            intent.putExtra("key", etKeySearch.text.toString().trim())
            intent.putExtra("header", userLogin?.webToken)
            intent.putExtra("replace_const", AppConstants.SEARCH_BY_KEYWORD)
            intent.putExtra("hasCart", true)
            intent.putExtra("title", getString(R.string.text49))
            startActivity(intent)
        }else{
            etKeySearch.error = getString(R.string.text22)
            alertDialogHelper.showAlertDialog(getString(R.string.app_name), getString(
                R.string.text22), getString(R.string.accept_dialog),"","",-1, false)
        }
    }
}