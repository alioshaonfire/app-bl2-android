package com.buscalibre.app2.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.buscalibre.app2.R
import com.buscalibre.app2.constants.Preferences
import com.buscalibre.app2.constants.ServerConstants
import com.buscalibre.app2.dialogs.TutorialDialog
import com.buscalibre.app2.models.Seller
import com.buscalibre.app2.models.SellerConditions
import com.buscalibre.app2.models.SellerInfo
import com.buscalibre.app2.network.NetworkToken
import com.buscalibre.app2.network.NetworkUtil
import com.buscalibre.app2.network.RestClient
import com.buscalibre.app2.network.RetrofitClientInstance
import com.buscalibre.app2.util.AlertDialogHelper
import com.buscalibre.app2.util.ConfigUtil
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.activity_menu_seller.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MenuSellerActivity : BaseActivity(), AlertDialogHelper.AlertDialogListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_seller)
        //alertDialogHelper = AlertDialogHelper(this@MenuSellerActivity)
        getSellerConditions()
        getSellerInfo()
        if(!Preferences.getIsTutorialHide(applicationContext)){
            initTutorial()
        }
    }

    private fun initTutorial() {
        val fm = fragmentManager
        val showTutorialDialog = TutorialDialog()
        showTutorialDialog.show(fm, "Show tutorial")
    }



    private fun initViews(){
        showBackButton()
        showToolbar()
        tvToolbarTitle.text = getString(R.string.text75)
        btSellBooks.setOnClickListener {
            val intent = Intent(applicationContext, SellBooksActivity::class.java)
            startActivity(intent)
        }
        btControlPanel.setOnClickListener {
            val seller = realm.where(Seller::class.java).findFirst()
            if (seller != null){
                val intent = Intent(applicationContext, ControlPanelActivity::class.java)
                startActivity(intent)
            }else{
                alertDialogHelper.showAlertDialog(getString(R.string.app_name), getString(R.string.first_sale), getString(R.string.accept_dialog), "", "", -1, false)
            }
        }

        llMenuButtons.visibility = View.VISIBLE
    }

    private fun getSellerInfo(){

        if (NetworkUtil.checkEnabledInternet(applicationContext)) {
            val restClient = RetrofitClientInstance.getRetrofitInstance().create(RestClient::class.java)
            val call: Call<SellerInfo> = restClient.getSellerInfo(userLogin?.token, ConfigUtil.getLocaleISO639(), country?.id)
            call.enqueue(object : Callback<SellerInfo?> {
                override fun onResponse(call: Call<SellerInfo?>, response: Response<SellerInfo?>) {
                    val sellerInfo: SellerInfo? = response.body()
                    if (sellerInfo != null) {
                        if (sellerInfo.blstatus == ServerConstants.AUTH_TOKEN_EXPIRED || sellerInfo.blstatus == ServerConstants.AUTH_TOKEN_ERROR) {
                            NetworkToken.refresh(applicationContext)
                            return
                        }
                        if (sellerInfo.blstatus == ServerConstants.NO_ERROR) {

                            SellerInfo.deleteAll(realm)
                            realm.executeTransaction {
                                realm.copyToRealm(sellerInfo)
                            }


                        } else {
                            //alertDialogHelper.showAlertDialog(getString(R.string.app_name), sellerInfo.blmessage, getString(R.string.accept_dialog), "", "", 1, false)

                        }
                    } else {
                        //alertDialogHelper.showAlertDialog(getString(R.string.app_name), getString(R.string.server_error), getString(R.string.accept_dialog), "", "", 1, false)
                    }
                }

                override fun onFailure(call: Call<SellerInfo?>, t: Throwable) {
                    /*alertDialogHelper.showAlertDialog(
                        getString(R.string.app_name), getString(R.string.server_error), getString(
                            R.string.accept_dialog
                        ), "", "", 1, false
                    )*/
                }
            })

        }
    }

    private fun getSellerConditions(){

        if (NetworkUtil.checkEnabledInternet(applicationContext)) {
            val restClient = RetrofitClientInstance.getRetrofitInstance().create(RestClient::class.java)
            val call: Call<SellerConditions> = restClient.sellerConditions(userLogin?.token, ConfigUtil.getLocaleISO639(), country?.id)
            call.enqueue(object : Callback<SellerConditions?> {
                override fun onResponse(call: Call<SellerConditions?>, response: Response<SellerConditions?>) {
                    val sellerConditions: SellerConditions? = response.body()
                    if (sellerConditions != null) {
                        if (sellerConditions.blstatus == ServerConstants.AUTH_TOKEN_EXPIRED || sellerConditions.blstatus == ServerConstants.AUTH_TOKEN_ERROR) {
                            NetworkToken.refresh(applicationContext)
                            return
                        }
                        if (sellerConditions.blstatus == ServerConstants.NO_ERROR) {
                            SellerConditions.deleteAll(realm)
                            realm.executeTransaction {
                                realm.copyToRealm(sellerConditions)
                            }
                                if (sellerConditions.conditions != null && sellerConditions.conditions.minAmount != null){
                                    initViews()
                                }else{
                                    alertDialogHelper.showAlertDialog(getString(R.string.app_name), getString(R.string.text375), getString(R.string.accept_dialog), "", "", 1, false)
                                }


                        } else {
                            alertDialogHelper.showAlertDialog(getString(R.string.app_name), sellerConditions.blmessage, getString(R.string.accept_dialog), "", "", 1, false)

                        }
                    } else {
                        alertDialogHelper.showAlertDialog(getString(R.string.app_name), getString(R.string.server_error), getString(R.string.accept_dialog), "", "", 1, false)
                    }
                }

                override fun onFailure(call: Call<SellerConditions?>, t: Throwable) {
                    alertDialogHelper.showAlertDialog(
                        getString(R.string.app_name), getString(R.string.server_error), getString(
                            R.string.accept_dialog
                        ), "", "", 1, false
                    )
                }
            })

        }else{
            alertDialogHelper.showAlertDialog(
                getString(R.string.app_name), getString(R.string.connect_error), getString(
                    R.string.accept_dialog
                ), "", "", -1, false
            )
        }
    }

    override fun onPositiveClick(from: Int) {
        if (from == 1){
            finish()
        }
    }

    override fun onNegativeClick(from: Int) {
    }

    override fun onNeutralClick(from: Int) {
    }
}