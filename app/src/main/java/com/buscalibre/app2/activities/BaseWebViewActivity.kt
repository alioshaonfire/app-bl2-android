package com.buscalibre.app2.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.*
import cl.ionix.tbk_ewallet_sdk_android.OnePay
import cl.ionix.tbk_ewallet_sdk_android.callback.OnePayCallback
import com.buscalibre.app2.R
import com.buscalibre.app2.constants.AppConstants
import com.buscalibre.app2.events.RefreshPaymentEvent
import com.buscalibre.app2.models.PaymentUrl
import com.buscalibre.app2.network.NetworkToken
import com.buscalibre.app2.util.AlertDialogHelper
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.activity_base_web_view.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.greenrobot.eventbus.EventBus
import java.net.URLEncoder
import kotlin.concurrent.thread
import android.content.Intent
import android.net.Uri


private var url = ""
private var key = ""
private var REPLACE_CONST = ""
private var title = ""
private var header = ""
private var finalURL = ""
private var hasHeader = false
private val customHeaders: MutableMap<String, String> = HashMap()
private var hasCart = false
private var isPayment = false

open class BaseWebViewActivity : BaseActivity(), AlertDialogHelper.AlertDialogListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base_web_view)
        initViews()
        getDataFromIntent()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initViews(){

        progressBar.bringToFront()
        val webSettings: WebSettings = wvBase.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.databaseEnabled = true

        wvBase.webViewClient = object : WebViewClient() {

            override fun onPageFinished(view: WebView, url: String) {
                progressBar.visibility = View.GONE
                Log.e("urlReceived", finalURL)
                if (hasHeader && !isPayment){
                    validateUserToken(url)
                }
                if (isPayment){
                    val paymentUrl = realm.where(PaymentUrl::class.java).findFirst()
                    if (url == paymentUrl?.success){
                        alertDialogHelper.showAlertDialog(
                            getString(R.string.app_name),
                            getString(R.string.text71),
                            getString(R.string.accept_dialog),
                            "",
                            "",
                            2,
                            false
                        )
                    }else if (url == paymentUrl?.error){

                        alertDialogHelper.showAlertDialog(
                            getString(R.string.app_name),
                            getString(R.string.text72),
                            getString(R.string.accept_dialog),
                            "",
                            "",
                            -1,
                            false
                        )
                    }
                }
            }

            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
                return super.shouldInterceptRequest(view, request)
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                Log.e("onReceivedError", error.toString())
                super.onReceivedError(view, request, error)
            }

            override fun onReceivedHttpError(
                view: WebView?,
                request: WebResourceRequest?,
                errorResponse: WebResourceResponse?
            ) {
                Log.e("onReceivedHttpError", errorResponse.toString())
                super.onReceivedHttpError(view, request, errorResponse)
            }

            override fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            ) {
                Log.e("onReceivedError", description.toString())
                super.onReceivedError(view, errorCode, description, failingUrl)
            }

            @Deprecated("Deprecated in Java")
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {

                if(url != null){
                    Log.e("urlShould", url)
                    /*if(url.contains("https://www.buscalibre.cl/pagar/")){
                        val i = Intent(Intent.ACTION_VIEW)
                        i.data = Uri.parse(url)
                        startActivity(i)
                        return true
                    }*/
                    if (url.contains("https://app.onepay.cl/")){

                        val occ = url.substring(url.indexOf("data?occ="), url.indexOf("&apn")).replace("data?occ=", "")
                        Log.e("occ", occ)

                        val onepay = OnePay(this@BaseWebViewActivity)

                        onepay.initPayment(occ) { p0, p1 ->

                            Log.e("errorOnepay1", p1)

                            if (p0 != null) {
                                if(p0.name == "ONE_PAY_NOT_INSTALLED"){
                                    alertDialogHelper.showAlertDialog(
                                        getString(R.string.app_name), getString(R.string.onepay_notinstalled), getString(R.string.accept_dialog), "", "", -2, false
                                    )
                                }
                                if (p0.name == "INVALID_OCC"){
                                    alertDialogHelper.showAlertDialog(
                                        getString(R.string.app_name), getString(R.string.invalid_occ), getString(R.string.accept_dialog), "", "", -2, false
                                    )
                                }
                                Log.e("p0.name", p0.name)
                                Log.e("p0String", p0.toString())
                            }
                        }
                        return true
                    }
                }

                if (hasHeader){
                    Log.e("customHeaders", customHeaders.toString())
                    view?.loadUrl(url!!, customHeaders)
                }else{
                    view?.loadUrl(url!!)
                }
                return true
            }

            override fun onPageCommitVisible(view: WebView?, url: String?) {
                updateBrowserButtonsState()
            }
        }
        alertDialogHelper = AlertDialogHelper(this@BaseWebViewActivity)
        showCloseButton()
        showWebTools()
        hideCart()
    }

    private fun postDataOnWeb(url: String){
        thread {

        }
    }

    open fun showWebTools(){
        mBrowseBackButton.visibility = View.VISIBLE
        mBrowseForwardButton.visibility = View.VISIBLE

        mBrowseBackButton.setOnClickListener {
            wvBase.goBack()
        }
        mBrowseForwardButton.setOnClickListener {
            wvBase.goForward()

        }
    }

    /**
     * Enable or disable de back and forward buttons depending on the availability of pages in
     * the respective stacks
     */
    private fun updateBrowserButtonsState() {
        if (wvBase != null) {
            if (wvBase.canGoBack()) {
                mBrowseBackButton.isClickable = true
                mBrowseBackButton.setImageResource(R.drawable.ic_arrow_back_white)
            } else {
                mBrowseBackButton.isClickable = false
                mBrowseBackButton.setImageResource(R.drawable.ic_arrow_back_disabled)
            }
            if (wvBase.canGoForward()) {
                mBrowseForwardButton.isClickable = true
                mBrowseForwardButton.setImageResource(R.drawable.ic_arrow_forward)
            } else {
                mBrowseForwardButton.isClickable = false
                mBrowseForwardButton.setImageResource(R.drawable.ic_arrow_forward_disabled)
            }
        }
    }

    private fun validateUserToken(url: String) {

        val httpClient = OkHttpClient()

        thread {

            try {
                val requestBuilder = Request.Builder()
                requestBuilder.addHeader(AppConstants.USER_TOKEN_APP, header)
                val request = requestBuilder.url(url).build()
                val response = httpClient.newCall(request).execute()
                val userTokenStatus = response.header("user-token-status").toString()
                Log.e("userWebTokenStatus", userTokenStatus)

                if (userTokenStatus.isNotEmpty() && userTokenStatus != "null" && userTokenStatus != AppConstants.VALID_WEB_TOKEN){
                    runOnUiThread {
                        NetworkToken.refresh(applicationContext)
                    }
                }
            } catch (e: Exception) {
                Log.e("exceptionHandleReq", e.toString())
            }
        }
    }

    private fun getDataFromIntent(){
        if (intent.extras != null){
            try{
                //receive data from previous view
                url = if(intent.getStringExtra("url") != null) intent.getStringExtra("url")!! else ""
                key = if(intent.getStringExtra("key") != null) intent.getStringExtra("key")!! else ""
                title = if(intent.getStringExtra("title") != null) intent.getStringExtra("title") else ""
                header = if(intent.getStringExtra("header") != null) intent.getStringExtra("header")!! else ""
                REPLACE_CONST = if(intent.getStringExtra("replace_const") != null) intent.getStringExtra("replace_const")!! else ""
                hasCart = intent.getBooleanExtra("hasCart", false)
                isPayment = intent.getBooleanExtra("isPayment", false)

                //Check key
                finalURL = if(key != ""){
                    url.replace(REPLACE_CONST, URLEncoder.encode(key))
                }else{
                    url
                }

                //Set toolbar title
                if (title != ""){
                    tvToolbarTitle.text = title
                }

                //Show cart
                if (hasCart){
                    showCart()
                }

                //Check the header and load the page
                if (header != ""){
                    customHeaders[AppConstants.USER_TOKEN_APP] = header
                    customHeaders[AppConstants.USER_PLATFORM_APP] = AppConstants.ANDROID_PLATFORM_SERVERID
                    hasHeader = true


                    wvBase.loadUrl(finalURL, customHeaders)

                    Log.e("header", header)
                    Log.e("headerList", customHeaders.toString())

                }else{
                    wvBase.loadUrl(finalURL)

                }
                Log.e("finalURL", finalURL)
            }catch (e: Exception){
                Log.e("errorDataIntent", e.toString())
            }

        }else{
            alertDialogHelper.showAlertDialog(
                getString(R.string.app_name), getString(
                    R.string.text21
                ), getString(R.string.accept_dialog), "", "", 1, false
            )
        }
    }


    override fun onResume() {
        super.onResume()
        if (hasCart){
            showCart()
        }
    }



    override fun onPositiveClick(from: Int) {
        if(from == -1){
            finish()
        }else if (from == 2){
            finish()
            EventBus.getDefault().post(RefreshPaymentEvent(true))
        }
    }

    override fun onNegativeClick(from: Int) {

    }

    override fun onNeutralClick(from: Int) {

    }
}

