package com.buscalibre.app2.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import cl.ionix.tbk_ewallet_sdk_android.OnePay
import com.buscalibre.app2.R

import com.buscalibre.app2.constants.AppConstants
import com.buscalibre.app2.models.Country
import com.buscalibre.app2.models.PaymentUrl
import com.buscalibre.app2.models.UserLogin
import com.buscalibre.app2.network.NetworkToken
import com.buscalibre.app2.util.AlertDialogHelper
import com.facebook.react.bridge.UiThreadUtil.runOnUiThread
import io.realm.Realm
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import kotlin.concurrent.thread



class BaseWebViewFragment : Fragment(), AlertDialogHelper.AlertDialogListener {

    private val realm:Realm = Realm.getDefaultInstance()
    private val customHeaders: MutableMap<String, String> = HashMap()
    private lateinit var progressBar:ProgressBar
    private lateinit var mBrowseBackButton:ImageButton
    private lateinit var ibCloseView: ImageView
    private lateinit var mBrowseForwardButton:ImageButton
    private lateinit var tvTitle:TextView
    private lateinit var alertDialogHelper:AlertDialogHelper
    private lateinit var wvBaseFragment:WebView
    private var url = ""
    private var key = ""
    private var REPLACE_CONST = ""
    private var title = ""
    private var header = ""
    private var finalURL = ""
    private var hasHeader = false
    private var hasCart = false
    private var isPayment = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_base_web_view, container, false)
        wvBaseFragment = view.findViewById(R.id.wvBaseFragment)
        tvTitle = view.findViewById(R.id.browse_toolbar_tv_choose_store)
        progressBar = view.findViewById(R.id.progressBar)
        mBrowseBackButton = view.findViewById(R.id.browse_toolbar_ib_back)
        mBrowseForwardButton = view.findViewById(R.id.browse_toolbar_ib_forward)
        ibCloseView = view.findViewById(R.id.ibCloseView)
        getDataFromBundle()
        initViews()
        return view

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val country = realm.where(Country::class.java).findFirst()
        val userLogin = realm.where(UserLogin::class.java).findFirst()
        //val url = country?.url?.cart
        customHeaders[AppConstants.USER_TOKEN_APP] = userLogin!!.webToken
        customHeaders[AppConstants.USER_PLATFORM_APP] = AppConstants.ANDROID_PLATFORM_SERVERID

    }

    private fun initViews(){

        progressBar.bringToFront()
        alertDialogHelper = AlertDialogHelper(activity)

        ibCloseView.setOnClickListener {
            activity?.onBackPressed()
        }
        val webSettings: WebSettings = wvBaseFragment.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.databaseEnabled = true

        wvBaseFragment.webViewClient = object : WebViewClient() {

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
                    if(url.contains("https://www.buscalibre.cl/pagar/")){
                        val i = Intent(Intent.ACTION_VIEW)
                        i.data = Uri.parse(url)
                        startActivity(i)
                        return true
                    }
                    if (url.contains("https://app.onepay.cl/")){

                        val occ = url.substring(url.indexOf("data?occ="), url.indexOf("&apn")).replace("data?occ=", "")
                        Log.e("occ", occ)

                        val onepay = OnePay(activity)

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
                    if (hasHeader){
                        Log.e("customHeaders", customHeaders.toString())
                        view?.loadUrl(url, customHeaders)
                    }else{
                        view?.loadUrl(url)
                    }
                }


                return true
            }

            override fun onPageCommitVisible(view: WebView?, url: String?) {
                updateBrowserButtonsState()
            }
        }
        alertDialogHelper = AlertDialogHelper(activity)
        //showCloseButton()
        showWebTools()
    }

    private fun showWebTools(){
        mBrowseBackButton.visibility = View.VISIBLE
        mBrowseForwardButton.visibility = View.VISIBLE

        mBrowseBackButton.setOnClickListener {
            wvBaseFragment.goBack()
        }
        mBrowseForwardButton.setOnClickListener {
            wvBaseFragment.goForward()

        }
    }


     //Enable or disable de back and forward buttons depending on the availability of pages in
    //the respective stacks

    private fun updateBrowserButtonsState() {
        if (wvBaseFragment.canGoBack()) {
            mBrowseBackButton.isClickable = true
            mBrowseBackButton.setImageResource(R.drawable.ic_arrow_back_white)
        } else {
            mBrowseBackButton.isClickable = false
            mBrowseBackButton.setImageResource(R.drawable.ic_arrow_back_disabled)
        }
        if (wvBaseFragment.canGoForward()) {
            mBrowseForwardButton.isClickable = true
            mBrowseForwardButton.setImageResource(R.drawable.ic_arrow_forward)
        } else {
            mBrowseForwardButton.isClickable = false
            mBrowseForwardButton.setImageResource(R.drawable.ic_arrow_forward_disabled)
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
                        NetworkToken.refresh(activity)
                    }
                }
            } catch (e: Exception) {
                Log.e("exceptionHandleReq", e.toString())
            }
        }
    }

    private fun getDataFromBundle(){
        if (arguments != null && !requireArguments().isEmpty){
            try{
                //receive data from previous view
                url = if(requireArguments().getString("url") != null) requireArguments().getString("url")!! else ""
                key = if(requireArguments().getString("key") != null) requireArguments().getString("key")!! else ""
                title = if(requireArguments().getString("title") != null) requireArguments().getString("title")!! else ""
                header = if(requireArguments().getString("header") != null) requireArguments().getString("header")!! else ""
                REPLACE_CONST = if(requireArguments().getString("replace_const") != null) requireArguments().getString(
                    "replace_const"
                )!! else ""
                hasCart = requireArguments().getBoolean("hasCart", false)
                isPayment = requireArguments().getBoolean("isPayment", false)

                //Check key
                finalURL = if(key != ""){
                    url.replace(REPLACE_CONST, URLEncoder.encode(key))
                }else{
                    url
                }

                //Set toolbar title
                if (title != ""){
                    tvTitle.text = title
                }

                //Show cart
                if (hasCart){
                    //showCart()
                }

                //Check the header and load the page
                if (header != ""){
                    customHeaders[AppConstants.USER_TOKEN_APP] = header
                    customHeaders[AppConstants.USER_PLATFORM_APP] = AppConstants.ANDROID_PLATFORM_SERVERID
                    hasHeader = true


                    wvBaseFragment.loadUrl(finalURL, customHeaders)

                    Log.e("header", header)
                    Log.e("headerList", customHeaders.toString())

                }else{
                    wvBaseFragment.loadUrl(finalURL)

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




    override fun onPositiveClick(from: Int) {
        if(from == -1){
        }else if (from == 2){

        }
    }

    override fun onNegativeClick(from: Int) {

    }

    override fun onNeutralClick(from: Int) {

    }
}