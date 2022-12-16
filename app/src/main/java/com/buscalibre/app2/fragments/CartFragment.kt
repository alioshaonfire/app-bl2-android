package com.buscalibre.app2.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.fragment.app.Fragment
import com.buscalibre.app2.R

import com.buscalibre.app2.constants.AppConstants
import com.buscalibre.app2.models.Country
import com.buscalibre.app2.models.UserLogin
import io.realm.Realm
import kotlinx.android.synthetic.main.fragment_my_account.*




class CartFragment : Fragment() {

    private val realm:Realm = Realm.getDefaultInstance()
    private val customHeaders: MutableMap<String, String> = HashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_my_account, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val country = realm.where(Country::class.java).findFirst()
        val userLogin = realm.where(UserLogin::class.java).findFirst()
        val url = country?.url?.cart
        customHeaders[AppConstants.USER_TOKEN_APP] = userLogin!!.webToken
        customHeaders[AppConstants.USER_PLATFORM_APP] = AppConstants.ANDROID_PLATFORM_SERVERID
        val webSettings: WebSettings = wvMyAccount.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.databaseEnabled = true
        wvMyAccount.loadUrl(url!!, customHeaders)
        wvMyAccount.webViewClient = object : WebViewClient() {

            override fun onPageFinished(view: WebView, url: String) {
                if(pbWebView != null){
                    pbWebView.visibility = View.GONE
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
                if(pbWebView != null){
                    pbWebView.visibility = View.GONE
                }
                Log.e("onReceivedError", error.toString())
                super.onReceivedError(view, request, error)
            }

            override fun onReceivedHttpError(
                view: WebView?,
                request: WebResourceRequest?,
                errorResponse: WebResourceResponse?
            ) {
                if(pbWebView != null){
                    pbWebView.visibility = View.GONE
                }
                Log.e("onReceivedHttpError", errorResponse.toString())
                super.onReceivedHttpError(view, request, errorResponse)
            }

            override fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            ) {
                if(pbWebView != null){
                    pbWebView.visibility = View.GONE
                }
                Log.e("onReceivedError", description.toString())
                super.onReceivedError(view, errorCode, description, failingUrl)
            }

            @Deprecated("Deprecated in Java")
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {

                if(url != null){
                    Log.e("urlShould", url)
                    view?.loadUrl(url, customHeaders)
                }
                Log.e("customHeaders", userLogin.webToken)
                return true
            }

            override fun onPageCommitVisible(view: WebView?, url: String?) {

            }
        }
    }
}