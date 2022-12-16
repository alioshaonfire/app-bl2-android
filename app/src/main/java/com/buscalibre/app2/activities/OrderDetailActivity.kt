package com.buscalibre.app2.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.buscalibre.app2.R
import com.buscalibre.app2.adapters.OrderDetailAdapter
import com.buscalibre.app2.adapters.SellProductAdapter
import com.buscalibre.app2.constants.AppConstants
import com.buscalibre.app2.constants.ServerConstants
import com.buscalibre.app2.models.*
import com.buscalibre.app2.network.NetworkToken
import com.buscalibre.app2.network.NetworkUtil
import com.buscalibre.app2.network.RestClient
import com.buscalibre.app2.network.RetrofitClientInstance
import com.buscalibre.app2.util.ConfigUtil
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.activity_base.*

import kotlinx.android.synthetic.main.activity_order_detail.*
import kotlinx.android.synthetic.main.activity_sell_books.rvSellProductList
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URLEncoder

class OrderDetailActivity : BaseActivity() {

    private var orderId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_detail)
        initViews()
        getDataFromIntent()
    }

    private fun getDataFromIntent(){
        if (intent.extras != null){
            try{
                //receive data from previous view
                orderId = if(intent.getStringExtra("orderId") != null) intent.getStringExtra("orderId")!! else ""
                tvOrderNum.text = getString(R.string.consignaci_n) + " " + orderId
                getOrderDetail(orderId)
            }catch (e: Exception){
                Log.e("errorDataIntent", e.toString())
            }

        }
    }

    private fun initViews(){
        showBackButton()
        showToolbar()
        tvToolbarTitle.text = getString(R.string.order_detail_title)
    }

    private fun getOrderDetail(orderId:String){

        if (NetworkUtil.checkEnabledInternet(applicationContext)) {
            spotsDialog.show()
            val restClient = RetrofitClientInstance.getRetrofitInstance().create(RestClient::class.java)
            val json = JsonObject()
            val seller = realm.where(Seller::class.java).findFirst()
            if (seller != null){
                json.addProperty("sellerId", seller.sellerId)
            }else{
                json.addProperty("sellerId", "")
            }
            json.addProperty("orderId", orderId)
            Log.e("jsonOrderDetail", json.toString())

            val call: Call<OrderDetail> = restClient.getOrderDetail(userLogin?.token, ConfigUtil.getLocaleISO639(),
                country?.id, json)
            call.enqueue(object : Callback<OrderDetail?> {
                override fun onResponse(call: Call<OrderDetail?>, response: Response<OrderDetail?>) {
                    val orderDetail: OrderDetail? = response.body()
                    if (orderDetail != null) {
                        if (orderDetail.blstatus == ServerConstants.AUTH_TOKEN_EXPIRED || orderDetail.blstatus == ServerConstants.AUTH_TOKEN_ERROR) {
                            NetworkToken.refresh(applicationContext)
                            return
                        }
                        if (orderDetail.blstatus == ServerConstants.NO_ERROR) {
                            OrderDetail.deleteAll(realm)
                            realm.executeTransaction {
                                realm.copyToRealm(orderDetail)
                            }
                            initProductList()


                        } else {
                            alertDialogHelper.showAlertDialog(getString(R.string.app_name), orderDetail.blmessage, getString(R.string.accept_dialog), "", "", 1, false)
                        }
                    } else {
                        alertDialogHelper?.showAlertDialog(getString(R.string.app_name), getString(R.string.server_error), getString(R.string.accept_dialog), "", "", 1, false)
                    }
                    spotsDialog.hide()

                }

                override fun onFailure(call: Call<OrderDetail?>, t: Throwable) {
                    spotsDialog.hide()

                    alertDialogHelper?.showAlertDialog(
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

    private fun initProductList(){
        val products = realm.where(Product_::class.java).findAll()

        val layoutManager = LinearLayoutManager(this)
        rvOrderDetailList.layoutManager = layoutManager
        rvOrderDetailList.setHasFixedSize(true)
        val orderDetailAdapter = OrderDetailAdapter(products, this, true, true)
        rvOrderDetailList.adapter = orderDetailAdapter
    }
}