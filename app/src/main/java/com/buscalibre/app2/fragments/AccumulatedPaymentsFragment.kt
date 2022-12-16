package com.buscalibre.app2.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.buscalibre.app2.R
import com.buscalibre.app2.adapters.AccumulatedPaymentsAdapter
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
import kotlinx.android.synthetic.main.fragment_accumulated_payments.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.*

class AccumulatedPaymentsFragment : Fragment() {

    private lateinit var realm: Realm
    private lateinit var userLogin: UserLogin
    private lateinit var country:Country
    private  var accumulatedPaymentsAdapter: AccumulatedPaymentsAdapter? = null
    private var alertDialogHelper: AlertDialogHelper? = null
    private var NUMBER_PAGE = 1
    private var FIRST_PAGE = 1
    private var PAGE_LENGHT = 10
    private var hasMorePayments = true
    private var isCallingServer = false
    private var isRefresh = true
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
        getCurrentPayment(FIRST_PAGE, PAGE_LENGHT.toString())
    }

    override fun onResume() {
        super.onResume()

    }
    private fun initData(){
        val config = realm.where(SystemConfig::class.java).findFirst()
        if (config != null){
            if (config.showcasePageLen != null){
                PAGE_LENGHT = config.showcasePageLen
            }
        }
    }

    private fun getCurrentPayment(numberPage: Int, lenghtPage: String){

        if (NetworkUtil.checkEnabledInternet(activity)) {
            spotsDialog.show()
            isCallingServer = true
            val restClient = RetrofitClientInstance.getRetrofitInstance().create(RestClient::class.java)
            val json = JsonObject()
            json.addProperty("productType", ServerConstants.SELLER_PRODUCT_TYPE_BOOK)
            val seller = realm.where(Seller::class.java).findFirst()
            if (seller != null){
                json.addProperty("sellerId", seller.sellerId)
            }else{
                json.addProperty("sellerId", "")
            }
            Log.e("jsonCurrentPayment", json.toString())
            val call: Call<CurrentPayment> = restClient.getCurrentPayment(userLogin.token, ConfigUtil.getLocaleISO639(),
                country.id,numberPage.toString(), lenghtPage, json)
            call.enqueue(object : Callback<CurrentPayment?> {
                override fun onResponse(call: Call<CurrentPayment?>, response: Response<CurrentPayment?>) {
                    val currentPayment: CurrentPayment? = response.body()
                    if (currentPayment != null) {
                        if (currentPayment.blstatus == ServerConstants.AUTH_TOKEN_EXPIRED || currentPayment.blstatus == ServerConstants.AUTH_TOKEN_ERROR) {
                            NetworkToken.refresh(activity)
                            return
                        }
                        if (currentPayment.blstatus == ServerConstants.NO_ERROR) {

                            if (numberPage == 1){
                                if(currentPayment.productListS != null && currentPayment.productListS.size > 0){
                                    CurrentPayment.deleteAll(realm)
                                    realm.executeTransaction {
                                        realm.copyToRealm(currentPayment)
                                    }
                                    initAccumulatedPaymentList()
                                }else{
                                    hasMorePayments = false
                                    tvNoBooks.visibility = View.VISIBLE
                                }
                            } else if(numberPage >= 1){
                                realm.executeTransaction {
                                    realm.copyToRealm(currentPayment)
                                }
                                NUMBER_PAGE = numberPage
                                accumulatedPaymentsAdapter?.notifyDataSetChanged()
                                if (currentPayment.productListS == null) {
                                    isRefresh = false
                                    //Toast.makeText(activity, R.string.text30, Toast.LENGTH_SHORT).show()
                                }
                            }
                            hasMorePayments = currentPayment.pendingRows
                            if (currentPayment.productListS == null || currentPayment.productListS.size == 0){
                                hasMorePayments = false
                            }
                        }else if(currentPayment.blstatus == ServerConstants.NO_PENDINGS_PAYMENTS){
                            alertDialogHelper?.showAlertDialog(getString(R.string.app_name), getString(R.string.no_payments), getString(R.string.accept_dialog), "", "", -1, false)

                        } else {
                            alertDialogHelper?.showAlertDialog(getString(R.string.app_name), currentPayment.blmessage, getString(R.string.accept_dialog), "", "", -1, false)
                        }
                    } else {
                        alertDialogHelper?.showAlertDialog(getString(R.string.app_name), getString(R.string.server_error), getString(R.string.accept_dialog), "", "", 1, false)
                    }
                    if (srlAccumulatedPaymentList.isRefreshing) {
                        srlAccumulatedPaymentList.isRefreshing = false
                    }
                    isCallingServer = false
                    spotsDialog.dismiss()
                }

                override fun onFailure(call: Call<CurrentPayment?>, t: Throwable) {
                    if (srlAccumulatedPaymentList.isRefreshing) {
                        srlAccumulatedPaymentList.isRefreshing = false
                    }
                    alertDialogHelper?.showAlertDialog(
                        getString(R.string.app_name), getString(R.string.server_error), getString(
                            R.string.accept_dialog
                        ), "", "", 1, false
                    )
                    isCallingServer = false
                    spotsDialog.dismiss()
                }
            })

        }else{
            if (srlAccumulatedPaymentList.isRefreshing) {
                srlAccumulatedPaymentList.isRefreshing = false
            }
            alertDialogHelper?.showAlertDialog(
                getString(R.string.app_name), getString(R.string.connect_error), getString(
                    R.string.accept_dialog
                ), "", "", -1, false
            )
        }
    }

    private fun initAccumulatedPaymentList(){

        val productListResults = realm.where(ProductList_::class.java).findAll()
        if (productListResults == null || productListResults.size == 0){
            tvNoBooks.visibility = View.VISIBLE
            return
        }
        val layoutManager = LinearLayoutManager(activity)
        rvAccumulatedPaymentList.layoutManager = layoutManager
        rvAccumulatedPaymentList.setHasFixedSize(true)
        accumulatedPaymentsAdapter = AccumulatedPaymentsAdapter(productListResults, activity, true, true)
        rvAccumulatedPaymentList.adapter = accumulatedPaymentsAdapter
        var total = 0f
        for (productList_:ProductList_ in productListResults){
            total += (productList_.purchaseValue * productList_.quantity)
        }
        val totalAmount = NumberFormat.getCurrencyInstance(Locale.getDefault()).format(total)
        tvMonthTotal.text = totalAmount
        //rlTotalView.visibility = View.VISIBLE

        rvAccumulatedPaymentList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                    if (!srlAccumulatedPaymentList.isRefreshing && !isCallingServer && hasMorePayments) {
                        if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0 && isRefresh) {
                            getCurrentPayment(NUMBER_PAGE + 1, PAGE_LENGHT.toString())
                        }/*else{
                        Toast.makeText(activity, getString(R.string.text124), Toast.LENGTH_SHORT).show()
                    }*/
                    }
                }
            })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        srlAccumulatedPaymentList.setOnRefreshListener {
            getCurrentPayment(1, PAGE_LENGHT.toString())
            isRefresh = true
        }
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_accumulated_payments, container, false)
    }
}