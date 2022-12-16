package com.buscalibre.app2.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.View
import android.widget.Toast
import com.buscalibre.app2.R
import com.buscalibre.app2.constants.ServerConstants
import com.buscalibre.app2.models.MessageList
import com.buscalibre.app2.models.StandarResponse
import com.buscalibre.app2.models.UserCart
import com.buscalibre.app2.network.NetworkToken
import com.buscalibre.app2.network.NetworkUtil
import com.buscalibre.app2.network.RestClient
import com.buscalibre.app2.network.RetrofitClientInstance
import com.buscalibre.app2.util.AlertDialogHelper
import com.buscalibre.app2.util.ConfigUtil
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.activity_message_detail.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MessageDetailActivity : BaseActivity(), AlertDialogHelper.AlertDialogListener {

    private var messageID = ""
    private var messageList:MessageList? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_detail)
        initViews()
        initData()
    }

    private fun initData() {
        if(intent.extras != null){
            messageID = intent.extras!!.getString("id")!!
            messageList = realm.where(MessageList::class.java).equalTo("id", messageID).findFirst()
            if (messageList != null){
                if (!messageList!!.read){
                    setMessageAsRead(messageList!!.id)
                    realm.executeTransaction {
                        messageList!!.read = true
                    }
                }
                tvTitleDetail.text = messageList!!.title
                //tvMessageDetail.text = messageList!!.body
                tvMessageDetail.text = Html.fromHtml(messageList!!.body, Html.FROM_HTML_MODE_COMPACT);

                if(messageList!!.content != null && messageList!!.content.isNotEmpty()){
                    btSeeMore.visibility = View.VISIBLE
                    btSeeMore.setOnClickListener {
                        val intent = Intent(this, BaseWebViewActivity::class.java)
                        intent.putExtra("url", messageList!!.content)
                        intent.putExtra("title", messageList!!.title)
                        //intent.putExtra("header", userLogin!!.webToken)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    }
                }
            }
        }
    }

    private fun setMessageAsRead(messageID:String){

        if (NetworkUtil.checkEnabledInternet(applicationContext)) {

            val json = JsonObject()
            val messageList = JsonArray()
            messageList.add(messageID)
            json.add("messageList",messageList)
            Log.e("jsonMessageRead", json.toString())
            val restClient = RetrofitClientInstance.getRetrofitInstance().create(RestClient::class.java)
            val call: Call<StandarResponse> = restClient.setMessageAsRead(userLogin?.token, ConfigUtil.getLocaleISO639(), json)
            call.enqueue(object : Callback<StandarResponse?> {
                override fun onResponse(call: Call<StandarResponse?>, response: Response<StandarResponse?>) {
                    val standarResponse: StandarResponse? = response.body()
                    if (standarResponse != null) {
                        if(standarResponse.blstatus == ServerConstants.AUTH_TOKEN_EXPIRED || standarResponse.blstatus == ServerConstants.AUTH_TOKEN_ERROR){
                            NetworkToken.refresh(applicationContext)
                            return
                        }
                        if (standarResponse.blstatus == ServerConstants.NO_ERROR) {

                        }else{
                            Toast.makeText(applicationContext, standarResponse.blmessage, Toast.LENGTH_LONG).show()
                        }
                    }else{
                        alertDialogHelper.showAlertDialog(getString(R.string.app_name),getString(R.string.server_error),getString(R.string.accept_dialog),"","",-1, false)
                    }
                }

                override fun onFailure(call: Call<StandarResponse?>, t: Throwable) {
                    alertDialogHelper.showAlertDialog(getString(R.string.app_name),getString(R.string.server_error),getString(R.string.accept_dialog),"","",-1, false)
                }
            })

        }else{
            alertDialogHelper.showAlertDialog(getString(R.string.app_name),getString(R.string.connect_error),getString(R.string.accept_dialog),"","",-1, false)
        }
    }

    private fun initViews() {
        showBackButton()
        //showCart()
        //alertDialogHelper = AlertDialogHelper(this@MessageDetailActivity)
        tvToolbarTitle.text = getString(R.string.text57)
    }

    override fun onPositiveClick(from: Int) {

    }

    override fun onNegativeClick(from: Int) {
    }

    override fun onNeutralClick(from: Int) {
    }

}