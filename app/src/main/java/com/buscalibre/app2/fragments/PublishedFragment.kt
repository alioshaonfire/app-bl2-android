package com.buscalibre.app2.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.buscalibre.app2.R
import com.buscalibre.app2.adapters.ShowcaseProductAdapter
import com.buscalibre.app2.adapters.ToPostProductAdapter
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
import kotlinx.android.synthetic.main.fragment_publised.*
import kotlinx.android.synthetic.main.fragment_publised.tvNoElements
import kotlinx.android.synthetic.main.fragment_topost.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PublishedFragment : Fragment() {

    private lateinit var realm: Realm
    private lateinit var userLogin: UserLogin
    private lateinit var country:Country
    private var isCallingServer = false
    private var isRefresh = true
    private var alertDialogHelper: AlertDialogHelper? = null
    private var NUMBER_PAGE = 1
    private var FIRST_PAGE = 1
    private var PAGE_LENGHT = 10
    private var hasMoreProducts = true
    private  var toPostProductAdapter: ToPostProductAdapter? = null
    lateinit var spotsDialog: AlertDialog



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       realm = Realm.getDefaultInstance()
        spotsDialog = SpotsDialog.Builder()
            .setContext(activity)
            .setTheme(R.style.CustomProgress)
            .setCancelable(false)
            .build()
        alertDialogHelper = AlertDialogHelper(activity)
        country = realm.where(Country::class.java).equalTo("isSelected", true).findFirst()!!
        userLogin = realm.where(UserLogin::class.java).findFirst()!!
        initData()
        getSellerOrders(FIRST_PAGE, PAGE_LENGHT.toString())

    }

    private fun initData(){
        val config = realm.where(SystemConfig::class.java).findFirst()
        if (config != null){
            if (config.showcasePageLen != null){
                PAGE_LENGHT = config.showcasePageLen
            }
        }
    }

    private fun getSellerOrders(numberPage: Int, lenghtPage: String){

        if (NetworkUtil.checkEnabledInternet(activity)) {
            isCallingServer = true
            spotsDialog.show()

            val restClient = RetrofitClientInstance.getRetrofitInstance().create(RestClient::class.java)
            val json = JsonObject()
            json.addProperty("orderStatus", ServerConstants.SELLER_PRODUCT_STATUS_PUBLISHED)
            val seller = realm.where(Seller::class.java).findFirst()
            if (seller != null){
                json.addProperty("sellerId", seller.sellerId)
            }else{
                json.addProperty("sellerId", "")
            }
            Log.e("jsonShowcaseToPost", json.toString())
            val call: Call<SellerOrder> = restClient.sellerOrders(userLogin.token, ConfigUtil.getLocaleISO639(),
                country.id,numberPage.toString(), lenghtPage, json)
            call.enqueue(object : Callback<SellerOrder?> {
                override fun onResponse(call: Call<SellerOrder?>, response: Response<SellerOrder?>) {
                    val sellerOrder: SellerOrder? = response.body()
                    if (sellerOrder != null) {
                        if (sellerOrder.blstatus == ServerConstants.AUTH_TOKEN_EXPIRED || sellerOrder.blstatus == ServerConstants.AUTH_TOKEN_ERROR) {
                            NetworkToken.refresh(activity)
                            return
                        }
                        if (sellerOrder.blstatus == ServerConstants.NO_ERROR) {
                            if (numberPage == 1){
                                if(sellerOrder.orderList != null && sellerOrder.orderList.size > 0){
                                    SellerOrder.deleteAll(realm, ServerConstants.SELLER_PRODUCT_STATUS_PUBLISHED)
                                    for (order:Order in sellerOrder.orderList){
                                        order.orderStatus = ServerConstants.SELLER_PRODUCT_STATUS_PUBLISHED
                                    }
                                    realm.executeTransaction {
                                        realm.copyToRealm(sellerOrder)
                                    }
                                    hasMoreProducts = true
                                    initBookList()
                                }else{
                                    hasMoreProducts = false
                                    tvNoElements.visibility = View.VISIBLE
                                }
                            } else if(numberPage >= 1){
                                //SellerOrder.deleteAll(realm, ServerConstants.SELLER_PRODUCT_STATUS_PUBLISHED)
                                for (order:Order in sellerOrder.orderList){
                                    order.orderStatus = ServerConstants.SELLER_PRODUCT_STATUS_PUBLISHED
                                }
                                realm.executeTransaction {
                                    realm.copyToRealm(sellerOrder)
                                }
                                if (sellerOrder.orderList == null || sellerOrder.orderList.size == 0){
                                    hasMoreProducts = false
                                }
                                NUMBER_PAGE = numberPage
                                toPostProductAdapter?.notifyDataSetChanged()
                                if (sellerOrder.orderList == null) {
                                    isRefresh = false
                                    //Toast.makeText(activity, R.string.text30, Toast.LENGTH_SHORT).show()
                                }
                            }

                        } else {
                            alertDialogHelper?.showAlertDialog(getString(R.string.app_name), sellerOrder.blmessage, getString(R.string.accept_dialog), "", "", 1, false)
                        }
                    } else {
                        alertDialogHelper?.showAlertDialog(getString(R.string.app_name), getString(R.string.server_error), getString(R.string.accept_dialog), "", "", 1, false)
                    }
                    isCallingServer = false
                    if (srlProductShowcaseList.isRefreshing) {
                        srlProductShowcaseList.isRefreshing = false
                    }
                    spotsDialog.dismiss()
                }

                override fun onFailure(call: Call<SellerOrder?>, t: Throwable) {
                    isCallingServer = false
                    if (srlProductShowcaseList.isRefreshing) {
                        srlProductShowcaseList.isRefreshing = false
                    }
                    alertDialogHelper?.showAlertDialog(
                        getString(R.string.app_name), getString(R.string.server_error), getString(
                            R.string.accept_dialog
                        ), "", "", 1, false
                    )
                    spotsDialog.dismiss()
                }
            })

        }else{
            if (srlProductShowcaseList.isRefreshing) {
                srlProductShowcaseList.isRefreshing = false
            }
            alertDialogHelper?.showAlertDialog(
                getString(R.string.app_name), getString(R.string.connect_error), getString(
                    R.string.accept_dialog
                ), "", "", -1, false
            )
        }
    }

    private fun initBookList(){
        val orders = realm.where(Order::class.java)
            .equalTo("orderStatus", ServerConstants.SELLER_PRODUCT_STATUS_PUBLISHED)
            .findAll()
        if(orders == null || orders.size == 0){
            tvNoElements.visibility = View.VISIBLE
            return
        }
        val layoutManager = LinearLayoutManager(activity)
        rvPublishedBooks.layoutManager = layoutManager
        rvPublishedBooks.setHasFixedSize(true)
        toPostProductAdapter = ToPostProductAdapter(orders, activity, false, false)
        rvPublishedBooks.adapter = toPostProductAdapter

        rvPublishedBooks.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                if (!srlProductShowcaseList.isRefreshing && !isCallingServer && hasMoreProducts) {
                    if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0 && isRefresh) {
                        getSellerOrders(NUMBER_PAGE + 1, PAGE_LENGHT.toString())
                    }/*else{
                        Toast.makeText(activity, getString(R.string.text124), Toast.LENGTH_SHORT).show()
                    }*/
                }
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        srlProductShowcaseList.setOnRefreshListener {
            getSellerOrders(1, PAGE_LENGHT.toString())
            isRefresh = true
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_publised, container, false)
    }


}