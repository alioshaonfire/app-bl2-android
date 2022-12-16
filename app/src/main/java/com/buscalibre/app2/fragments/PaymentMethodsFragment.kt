package com.buscalibre.app2.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.buscalibre.app2.R
import com.buscalibre.app2.activities.BaseWebViewActivity
import com.buscalibre.app2.adapters.UserPaymentListAdapter

import com.buscalibre.app2.constants.ServerConstants
import com.buscalibre.app2.events.SelectNavMenuEvent
import com.buscalibre.app2.models.Country
import com.buscalibre.app2.models.NewPaymentMethod
import com.buscalibre.app2.models.UserLogin
import com.buscalibre.app2.models.UserPayments
import com.buscalibre.app2.network.NetworkToken
import com.buscalibre.app2.network.NetworkUtil
import com.buscalibre.app2.network.RestClient
import com.buscalibre.app2.network.RetrofitClientInstance
import com.buscalibre.app2.util.AlertDialogHelper
import com.buscalibre.app2.util.ConfigUtil
import io.realm.Realm
import org.greenrobot.eventbus.EventBus
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response




class PaymentMethodsFragment : Fragment() {

    private lateinit var alertDialogHelper: AlertDialogHelper
    private lateinit var btAddNewPaymentMethod:Button
    private lateinit var rvMyPaymentList:RecyclerView
    private lateinit var tvNoPayments:TextView
    private lateinit var ivBack:ImageView
    private var userLogin:UserLogin? = null
    private var country:Country? = null
    private val realm:Realm = Realm.getDefaultInstance()
    private val customHeaders: MutableMap<String, String> = HashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val mainView = inflater.inflate(R.layout.fragment_payment_methods, container, false)
        btAddNewPaymentMethod = mainView.findViewById(  R.id.btAddNewPaymentMethod)
        rvMyPaymentList = mainView.findViewById(  R.id.rvMyPaymentList)
        tvNoPayments = mainView.findViewById(R.id.tvNoPayments)
        ivBack = mainView.findViewById(R.id.ivBack)
        btAddNewPaymentMethod.setOnClickListener {
            addNewPaymentMethod()
        }
        ivBack.setOnClickListener {
            EventBus.getDefault().post(SelectNavMenuEvent(true))
        }
        alertDialogHelper = AlertDialogHelper(activity)
        initData()
        return mainView
    }


    private fun initData(){
        country = realm.where(Country::class.java).equalTo("isSelected", true).findFirst()
        userLogin = realm.where(UserLogin::class.java).findFirst()
        if (userLogin == null){
            NetworkToken.refresh(activity)
        }
    }

    private fun getUserPayments(isRefresh: Boolean){

        if (NetworkUtil.checkEnabledInternet(activity)) {

            val restClient = RetrofitClientInstance.getRetrofitInstance().create(RestClient::class.java)
            val call: Call<UserPayments> = restClient.getUserPaymentList(userLogin?.token, ConfigUtil.getLocaleISO639(), userLogin?.countryID)

            call.enqueue(object : Callback<UserPayments?> {
                override fun onResponse(call: Call<UserPayments?>, response: Response<UserPayments?>) {
                    val userPayments: UserPayments? = response.body()
                    if (userPayments != null) {
                        if (userPayments.blstatus == ServerConstants.AUTH_TOKEN_EXPIRED || userPayments.blstatus == ServerConstants.AUTH_TOKEN_ERROR) {
                            NetworkToken.refresh(activity)
                            return
                        }
                        if (userPayments.blstatus == ServerConstants.NO_ERROR) {
                            if (userPayments.paymentList != null){

                                if(userPayments.canAdd){
                                    btAddNewPaymentMethod.visibility = View.VISIBLE
                                }
                                if (userPayments.paymentList.size > 0){
                                    val lm: RecyclerView.LayoutManager = LinearLayoutManager(activity)
                                    rvMyPaymentList.layoutManager = lm
                                    rvMyPaymentList.adapter = UserPaymentListAdapter(userPayments.paymentList, activity!!)
                                }else{
                                    val lm: RecyclerView.LayoutManager = LinearLayoutManager(activity)
                                    rvMyPaymentList.layoutManager = lm
                                    rvMyPaymentList.adapter = UserPaymentListAdapter(userPayments.paymentList,
                                        activity!!
                                    )
                                    if (!isRefresh){
                                        tvNoPayments.visibility = View.VISIBLE
                                        alertDialogHelper.showAlertDialog(getString(R.string.app_name), getString(R.string.text67), getString(R.string.accept_dialog), "", "", -1, false)
                                    }
                                }
                            }else{
                                alertDialogHelper.showAlertDialog(getString(R.string.app_name), getString(R.string.text64), getString(R.string.accept_dialog), "", "", -1, false)

                            }

                        } else {
                            Toast.makeText(
                                activity,
                                userPayments.blmessage,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        alertDialogHelper.showAlertDialog(getString(R.string.app_name), getString(R.string.server_error), getString(R.string.accept_dialog), "", "", -1, false)
                    }
                    /*if (srlPaymentList.isRefreshing) {
                        srlPaymentList.isRefreshing = false
                    }*/
                }

                override fun onFailure(call: Call<UserPayments?>, t: Throwable) {
                    /*if (srlPaymentList.isRefreshing) {
                        srlPaymentList.isRefreshing = false
                    }*/
                    alertDialogHelper.showAlertDialog(
                        getString(R.string.app_name), getString(R.string.server_error), getString(
                            R.string.accept_dialog
                        ), "", "", -1, false
                    )
                }
            })

        }else{
            /*if (srlPaymentList.isRefreshing) {
                srlPaymentList.isRefreshing = false
            }*/
            alertDialogHelper.showAlertDialog(
                getString(R.string.app_name), getString(R.string.connect_error), getString(
                    R.string.accept_dialog
                ), "", "", -1, false
            )
        }
    }

    private fun addNewPaymentMethod(){

        if (NetworkUtil.checkEnabledInternet(activity)) {

            val restClient = RetrofitClientInstance.getRetrofitInstance().create(RestClient::class.java)
            val call: Call<NewPaymentMethod> = restClient.addNewPaymentMethod(userLogin?.token, ConfigUtil.getLocaleISO639(), country?.id)

            call.enqueue(object : Callback<NewPaymentMethod?> {
                override fun onResponse(call: Call<NewPaymentMethod?>, response: Response<NewPaymentMethod?>) {
                    val newPaymentMethod: NewPaymentMethod? = response.body()
                    if (newPaymentMethod != null) {
                        if (newPaymentMethod.blstatus == ServerConstants.AUTH_TOKEN_EXPIRED || newPaymentMethod.blstatus == ServerConstants.AUTH_TOKEN_ERROR) {
                            NetworkToken.refresh(activity)
                            return
                        }
                        if (newPaymentMethod.blstatus == ServerConstants.NO_ERROR) {
                            NewPaymentMethod.deleteAll(realm)
                            realm.executeTransaction {
                                realm.copyToRealm(newPaymentMethod)
                            }
                            val intent = Intent(activity, BaseWebViewActivity::class.java)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getUserPayments(false)
    }
}