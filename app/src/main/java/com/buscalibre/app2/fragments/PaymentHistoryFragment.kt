package com.buscalibre.app2.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.buscalibre.app2.R
import com.buscalibre.app2.adapters.PaymentsHistoryAdapter
import com.buscalibre.app2.constants.ServerConstants
import com.buscalibre.app2.models.*
import com.buscalibre.app2.network.NetworkToken
import com.buscalibre.app2.network.NetworkUtil
import com.buscalibre.app2.network.RestClient
import com.buscalibre.app2.network.RetrofitClientInstance
import com.buscalibre.app2.util.AlertDialogHelper
import com.buscalibre.app2.util.ConfigUtil
import com.google.gson.JsonObject
import dmax.dialog.SpotsDialog
import io.realm.Realm
import kotlinx.android.synthetic.main.fragment_payment_history.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PaymentHistoryFragment : Fragment(){

    private lateinit var realm: Realm
    private lateinit var userLogin: UserLogin
    private lateinit var country: Country
    private var isCallingServer = false
    private var isRefresh = true
    private  var paymentsHistoryAdapter: PaymentsHistoryAdapter? = null
    private var alertDialogHelper: AlertDialogHelper? = null
    private var NUMBER_PAGE = 1
    private var FIRST_PAGE = 1
    private var PAGE_LENGHT = 10
    private var hasMorePayments = true
    lateinit var spotsDialog: AlertDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        realm = Realm.getDefaultInstance()
        //alertDialogHelper = AlertDialogHelper(activity)
        country = realm.where(Country::class.java).equalTo("isSelected", true).findFirst()!!
        userLogin = realm.where(UserLogin::class.java).findFirst()!!
        spotsDialog = SpotsDialog.Builder()
            .setContext(activity)
            .setTheme(R.style.CustomProgress)
            .setCancelable(false)
            .build()
        initData()
        getPaymentHistory(FIRST_PAGE, PAGE_LENGHT.toString())
    }

    private fun initData(){
        val config = realm.where(SystemConfig::class.java).findFirst()
        if (config != null){
            if (config.showcasePageLen != null){
                PAGE_LENGHT = config.showcasePageLen
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        srlHistoryPaymentList.setOnRefreshListener {
            getPaymentHistory(1, PAGE_LENGHT.toString())
            isRefresh = true
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_payment_history, container, false)
    }

    private fun getPaymentHistory(numberPage: Int, lenghtPage: String){

        if (NetworkUtil.checkEnabledInternet(activity)) {
            isCallingServer = true
            spotsDialog.show()
            val restClient = RetrofitClientInstance.getRetrofitInstance().create(RestClient::class.java)
            val json = JsonObject()
            json.addProperty("productType", ServerConstants.SELLER_PRODUCT_TYPE_BOOK)
            //json.addProperty("paymentStatus", "null")

            val seller = realm.where(Seller::class.java).findFirst()
            if (seller != null){
                json.addProperty("sellerId", seller.sellerId)
            }else{
                json.addProperty("sellerId", "")
            }
            Log.e("jsonPaymentHistory", json.toString())
            val call: Call<PaymentHistory> = restClient.getPaymentHistory(userLogin.token, ConfigUtil.getLocaleISO639(),
                country.id, numberPage.toString(), lenghtPage, json)
            call.enqueue(object : Callback<PaymentHistory?> {
                override fun onResponse(call: Call<PaymentHistory?>, response: Response<PaymentHistory?>) {
                    val paymentHistory: PaymentHistory? = response.body()
                    if (paymentHistory != null) {
                        if (paymentHistory.blstatus == ServerConstants.AUTH_TOKEN_EXPIRED || paymentHistory.blstatus == ServerConstants.AUTH_TOKEN_ERROR) {
                            NetworkToken.refresh(activity)
                            return
                        }
                        if (paymentHistory.blstatus == ServerConstants.NO_ERROR) {

                            if (numberPage == 1){
                                if(paymentHistory.paymentList != null && paymentHistory.paymentList.size > 0){
                                    PaymentHistory.deleteAll(realm)
                                    realm.executeTransaction {
                                        realm.copyToRealm(paymentHistory)
                                    }
                                    hasMorePayments = true
                                    initPaymentHistoryList()
                                }else{
                                    hasMorePayments = false
                                    tvNoPayments.visibility = View.VISIBLE

                                }
                            } else if(numberPage >= 1){
                                realm.executeTransaction {
                                    realm.copyToRealm(paymentHistory)
                                }

                                if (paymentHistory.paymentList == null || paymentHistory.paymentList.size == 0){
                                    hasMorePayments = false
                                }
                                NUMBER_PAGE = numberPage
                                paymentsHistoryAdapter?.notifyDataSetChanged()
                                if (paymentHistory.paymentList == null) {
                                    isRefresh = false
                                    //Toast.makeText(activity, R.string.text30, Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            alertDialogHelper?.showAlertDialog(getString(R.string.app_name), paymentHistory.blmessage, getString(R.string.accept_dialog), "", "", 1, false)
                        }
                    } else {
                        alertDialogHelper?.showAlertDialog(getString(R.string.app_name), getString(R.string.server_error), getString(R.string.accept_dialog), "", "", 1, false)
                    }
                    if (srlHistoryPaymentList.isRefreshing) {
                        srlHistoryPaymentList.isRefreshing = false
                    }
                    spotsDialog.dismiss()
                    isCallingServer = false

                }

                override fun onFailure(call: Call<PaymentHistory?>, t: Throwable) {
                    if (srlHistoryPaymentList.isRefreshing) {
                        srlHistoryPaymentList.isRefreshing = false
                    }
                    alertDialogHelper?.showAlertDialog(
                        getString(R.string.app_name), getString(R.string.server_error), getString(
                            R.string.accept_dialog
                        ), "", "", 1, false
                    )
                    spotsDialog.dismiss()
                    isCallingServer = false
                }
            })

        }else{
            if (srlHistoryPaymentList.isRefreshing) {
                srlHistoryPaymentList.isRefreshing = false
            }
            alertDialogHelper?.showAlertDialog(
                getString(R.string.app_name), getString(R.string.connect_error), getString(
                    R.string.accept_dialog
                ), "", "", -1, false
            )
        }
    }

    private fun initPaymentHistoryList(){
        val paymentListResults = realm.where(PaymentList_::class.java).findAll()
        if (paymentListResults == null || paymentListResults.size == 0){
            tvNoPayments.visibility = View.VISIBLE
            return
        }
        val layoutManager = LinearLayoutManager(activity)
        rvHistoryPaymentList.layoutManager = layoutManager
        rvHistoryPaymentList.setHasFixedSize(true)
        paymentsHistoryAdapter = PaymentsHistoryAdapter(paymentListResults, activity, false, false)
        rvHistoryPaymentList.adapter = paymentsHistoryAdapter

        rvHistoryPaymentList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                    if (!srlHistoryPaymentList.isRefreshing && !isCallingServer && hasMorePayments) {
                        if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0 && isRefresh) {
                            getPaymentHistory(NUMBER_PAGE + 1, PAGE_LENGHT.toString())
                        }/*else{
                        Toast.makeText(activity, getString(R.string.text124), Toast.LENGTH_SHORT).show()
                    }*/
                    }
                }
            })
    }


}