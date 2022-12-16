package com.buscalibre.app2.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.bumptech.glide.Glide
import com.buscalibre.app2.R
import com.buscalibre.app2.constants.ServerConstants
import com.buscalibre.app2.models.SellerQuote
import com.buscalibre.app2.network.NetworkToken
import com.buscalibre.app2.network.NetworkUtil
import com.buscalibre.app2.network.RestClient
import com.buscalibre.app2.network.RetrofitClientInstance
import com.buscalibre.app2.util.AlertDialogHelper
import com.buscalibre.app2.util.ConfigUtil
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.activity_sell_book_quote_anim.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

private var isbn = ""

class SellBookQuoteAnimActivity : BaseActivity(), AlertDialogHelper.AlertDialogListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sell_book_quote_anim)
        getDataFromIntent()
        initViews()
    }
    private fun initViews(){
        //alertDialogHelper = AlertDialogHelper(this@SellBookQuoteAnimActivity)
        hideToolbar()
        Glide.with(this)
            .load(R.raw.wait)
            .into(ivGifAnim)
    }

    private fun getDataFromIntent(){
        if (intent.extras != null){
            isbn = intent.getStringExtra("isbn")!!
            //getSellerQuote(isbn)
            val timerObj = Timer()
            val timerTaskObj: TimerTask = object : TimerTask() {
                override fun run() {
                    timerObj.cancel()
                    val intent = Intent(applicationContext, BookDetailActivity::class.java)
                    intent.putExtra("isbn", isbn)
                    startActivity(intent)
                    finish()
                }
            }
            timerObj.schedule(timerTaskObj, 2000, 2000)
            Log.e("isbn", isbn)
        }else{
            /*alertDialogHelper.showAlertDialog(getString(R.string.app_name), getString(
                R.string.text21), getString(R.string.accept_dialog),"","",1, false)*/
        }
    }

    private fun getSellerQuote(isbn: String){
        if (NetworkUtil.checkEnabledInternet(applicationContext)) {
            val json = JsonObject()
            json.addProperty("productType", 1)
            json.addProperty("productKey", isbn)

            Log.e("jsonMessageRead", json.toString())
            val restClient = RetrofitClientInstance.getRetrofitInstance().create(RestClient::class.java)
            val call: Call<SellerQuote> = restClient.sellerQuote(
                userLogin?.token,
                ConfigUtil.getLocaleISO639(),
                country?.id,
                json
            )
            call.enqueue(object : Callback<SellerQuote?> {
                override fun onResponse(
                    call: Call<SellerQuote?>,
                    response: Response<SellerQuote?>
                ) {
                    val sellerQuote: SellerQuote? = response.body()
                    if (sellerQuote != null) {
                        if (sellerQuote.blstatus == ServerConstants.AUTH_TOKEN_EXPIRED || sellerQuote.blstatus == ServerConstants.AUTH_TOKEN_ERROR) {
                            NetworkToken.refresh(applicationContext)
                            return
                        }
                        if (sellerQuote.blstatus == ServerConstants.NO_ERROR) {
                            if(sellerQuote.product != null){
                                realm.executeTransaction {
                                    realm.copyToRealm(sellerQuote.product)
                                }
                            }
                            val timerObj = Timer()
                            val timerTaskObj: TimerTask = object : TimerTask() {
                                override fun run() {
                                    timerObj.cancel()
                                    finish()
                                }
                            }
                            timerObj.schedule(timerTaskObj, 2000, 2000)

                        } else {
                            alertDialogHelper.showAlertDialog(
                                getString(R.string.app_name), sellerQuote.blmessage, getString(
                                    R.string.accept_dialog
                                ), "", "", 1, false
                            )

                        }
                    } else {
                        alertDialogHelper.showAlertDialog(
                            getString(R.string.app_name),
                            getString(R.string.server_error),
                            getString(
                                R.string.accept_dialog
                            ),
                            "",
                            "",
                            1,
                            false
                        )
                    }
                }

                override fun onFailure(call: Call<SellerQuote?>, t: Throwable) {
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