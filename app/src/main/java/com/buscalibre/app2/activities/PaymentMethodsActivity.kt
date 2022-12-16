package com.buscalibre.app2.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.buscalibre.app2.R
import com.buscalibre.app2.adapters.UserPaymentListAdapter
import com.buscalibre.app2.constants.ServerConstants
import com.buscalibre.app2.events.RefreshPaymentEvent
import com.buscalibre.app2.models.NewPaymentMethod
import com.buscalibre.app2.models.UserPayments
import com.buscalibre.app2.network.NetworkToken
import com.buscalibre.app2.network.NetworkUtil
import com.buscalibre.app2.network.RestClient
import com.buscalibre.app2.network.RetrofitClientInstance
import com.buscalibre.app2.util.AlertDialogHelper
import com.buscalibre.app2.util.ConfigUtil
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.activity_payment_methods.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response




class PaymentMethodsActivity : BaseActivity(), AlertDialogHelper.AlertDialogListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_methods)
        initViews()
        getUserPayments(false)
    }

    private fun initViews() {
        showBackButton()
        alertDialogHelper = AlertDialogHelper(this@PaymentMethodsActivity)
        tvToolbarTitle.text = getString(R.string.text63)
        srlPaymentList.setOnRefreshListener {
            getUserPayments(false)
        }
    }

    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }


    override fun onResume() {
        super.onResume()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun refreshPaymentList(refreshPaymentEvent: RefreshPaymentEvent) {
        getUserPayments(true)
    }

    private fun getUserPayments(isRefresh: Boolean){

        if (NetworkUtil.checkEnabledInternet(applicationContext)) {

            val restClient = RetrofitClientInstance.getRetrofitInstance().create(RestClient::class.java)
            val call: Call<UserPayments> = restClient.getUserPaymentList(userLogin?.token, ConfigUtil.getLocaleISO639(), userLogin?.countryID)

            call.enqueue(object : Callback<UserPayments?> {
                override fun onResponse(call: Call<UserPayments?>, response: Response<UserPayments?>) {
                    val userPayments: UserPayments? = response.body()
                    if (userPayments != null) {
                        if (userPayments.blstatus == ServerConstants.AUTH_TOKEN_EXPIRED || userPayments.blstatus == ServerConstants.AUTH_TOKEN_ERROR) {
                            NetworkToken.refresh(applicationContext)
                            return
                        }
                        if (userPayments.blstatus == ServerConstants.NO_ERROR) {
                            if (userPayments.paymentList != null){

                                if(userPayments.canAdd){
                                    btAddNewPaymentMethod.visibility = View.VISIBLE
                                }
                                if (userPayments.paymentList.size > 0){
                                    val lm: RecyclerView.LayoutManager = LinearLayoutManager(applicationContext)
                                    rvMyPaymentList.layoutManager = lm
                                    rvMyPaymentList.adapter = UserPaymentListAdapter(userPayments.paymentList, this@PaymentMethodsActivity)
                                }else{
                                    val lm: RecyclerView.LayoutManager = LinearLayoutManager(applicationContext)
                                    rvMyPaymentList.layoutManager = lm
                                    rvMyPaymentList.adapter = UserPaymentListAdapter(userPayments.paymentList, this@PaymentMethodsActivity)
                                    if (!isRefresh){
                                        alertDialogHelper.showAlertDialog(getString(R.string.app_name), getString(R.string.text67), getString(R.string.accept_dialog), "", "", -1, false)
                                    }
                                }
                            }else{
                                alertDialogHelper.showAlertDialog(getString(R.string.app_name), getString(R.string.text64), getString(R.string.accept_dialog), "", "", -1, false)

                            }

                        } else {
                            Toast.makeText(
                                applicationContext,
                                userPayments.blmessage,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        alertDialogHelper.showAlertDialog(getString(R.string.app_name), getString(R.string.server_error), getString(R.string.accept_dialog), "", "", -1, false)
                    }
                    if (srlPaymentList.isRefreshing) {
                        srlPaymentList.isRefreshing = false
                    }
                }

                override fun onFailure(call: Call<UserPayments?>, t: Throwable) {
                    if (srlPaymentList.isRefreshing) {
                        srlPaymentList.isRefreshing = false
                    }
                    alertDialogHelper.showAlertDialog(
                        getString(R.string.app_name), getString(R.string.server_error), getString(
                            R.string.accept_dialog
                        ), "", "", -1, false
                    )
                }
            })

        }else{
            if (srlPaymentList.isRefreshing) {
                srlPaymentList.isRefreshing = false
            }
            alertDialogHelper.showAlertDialog(
                getString(R.string.app_name), getString(R.string.connect_error), getString(
                    R.string.accept_dialog
                ), "", "", -1, false
            )
        }
    }

    private fun addNewPaymentMethod(){

        if (NetworkUtil.checkEnabledInternet(applicationContext)) {

            val restClient = RetrofitClientInstance.getRetrofitInstance().create(RestClient::class.java)
            val call: Call<NewPaymentMethod> = restClient.addNewPaymentMethod(userLogin?.token, ConfigUtil.getLocaleISO639(), country?.id)
            call.enqueue(object : Callback<NewPaymentMethod?> {
                override fun onResponse(call: Call<NewPaymentMethod?>, response: Response<NewPaymentMethod?>) {
                    val newPaymentMethod: NewPaymentMethod? = response.body()
                    if (newPaymentMethod != null) {
                        if (newPaymentMethod.blstatus == ServerConstants.AUTH_TOKEN_EXPIRED || newPaymentMethod.blstatus == ServerConstants.AUTH_TOKEN_ERROR) {
                            NetworkToken.refresh(applicationContext)
                            return
                        }
                        if (newPaymentMethod.blstatus == ServerConstants.NO_ERROR) {
                            NewPaymentMethod.deleteAll(realm)
                            realm.executeTransaction {
                                realm.copyToRealm(newPaymentMethod)
                            }
                            val intent = Intent(applicationContext, BaseWebViewActivity::class.java)
                            intent.putExtra("url", newPaymentMethod.paymentUrl?.add)
                            intent.putExtra("header", userLogin!!.webToken)
                            intent.putExtra("title", getString(R.string.text69))
                            intent.putExtra("hasCart", false)
                            intent.putExtra("isPayment", true)
                            startActivity(intent)
                        } else {
                            alertDialogHelper.showAlertDialog(getString(R.string.app_name), newPaymentMethod.blmessage, getString(R.string.accept_dialog), "", "", -1, false)

                        }
                    } else {
                        alertDialogHelper.showAlertDialog(getString(R.string.app_name), getString(R.string.server_error), getString(R.string.accept_dialog), "", "", -1, false)
                    }
                }

                override fun onFailure(call: Call<NewPaymentMethod?>, t: Throwable) {
                    alertDialogHelper.showAlertDialog(
                        getString(R.string.app_name), getString(R.string.server_error), getString(
                            R.string.accept_dialog
                        ), "", "", -1, false
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

    fun addNewPayment(view: View) {
        addNewPaymentMethod()
    }

    override fun onPositiveClick(from: Int) {

    }

    override fun onNegativeClick(from: Int) {
    }

    override fun onNeutralClick(from: Int) {
    }


}