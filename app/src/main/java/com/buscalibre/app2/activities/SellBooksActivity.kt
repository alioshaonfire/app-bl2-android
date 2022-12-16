package com.buscalibre.app2.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.buscalibre.app2.R
import com.buscalibre.app2.adapters.SellProductAdapter
import com.buscalibre.app2.constants.ServerConstants
import com.buscalibre.app2.events.CountrySelectedEvent
import com.buscalibre.app2.events.RefreshProductsEvent
import com.buscalibre.app2.models.Conditions
import com.buscalibre.app2.models.Product
import com.buscalibre.app2.models.SellerConditions
import com.buscalibre.app2.models.SellerInfo
import com.buscalibre.app2.network.NetworkToken
import com.buscalibre.app2.network.NetworkUtil
import com.buscalibre.app2.network.RestClient
import com.buscalibre.app2.network.RetrofitClientInstance
import com.buscalibre.app2.util.AlertDialogHelper
import com.buscalibre.app2.util.ConfigUtil
import com.buscalibre.app2.util.FontUtil
import io.realm.RealmResults
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.activity_sell_books.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.*

private var canSellBooks = false

class SellBooksActivity : BaseActivity(), AlertDialogHelper.AlertDialogListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sell_books)
        initViews()
        getSellerConditions()
    }

    private fun getSellerConditions(){
        val sellerConditions = realm.where(Conditions::class.java).findFirst()
        val alertMessage = sellerConditions?.alertMessage
        if (alertMessage != null && alertMessage.isNotEmpty()){
            tvAlertMessage.visibility = View.VISIBLE
            tvAlertMessage.text = alertMessage
        }
    }



    private fun getSellerInfo(){

        if (NetworkUtil.checkEnabledInternet(applicationContext)) {
            val restClient = RetrofitClientInstance.getRetrofitInstance().create(RestClient::class.java)
            val call: Call<SellerInfo> = restClient.getSellerInfo(userLogin?.token, ConfigUtil.getLocaleISO639(), country?.id)
            call.enqueue(object : Callback<SellerInfo?> {
                override fun onResponse(call: Call<SellerInfo?>, response: Response<SellerInfo?>) {
                    val sellerInfo: SellerInfo? = response.body()
                    if (sellerInfo != null) {
                        if (sellerInfo.blstatus == ServerConstants.AUTH_TOKEN_EXPIRED || sellerInfo.blstatus == ServerConstants.AUTH_TOKEN_ERROR) {
                            NetworkToken.refresh(applicationContext)
                            return
                        }
                        if (sellerInfo.blstatus == ServerConstants.NO_ERROR) {

                            SellerInfo.deleteAll(realm)
                            realm.executeTransaction {
                                realm.copyToRealm(sellerInfo)
                            }


                        } else {
                            //alertDialogHelper.showAlertDialog(getString(R.string.app_name), sellerInfo.blmessage, getString(R.string.accept_dialog), "", "", 1, false)

                        }
                    } else {
                        //alertDialogHelper.showAlertDialog(getString(R.string.app_name), getString(R.string.server_error), getString(R.string.accept_dialog), "", "", 1, false)
                    }
                }

                override fun onFailure(call: Call<SellerInfo?>, t: Throwable) {
                    /*alertDialogHelper.showAlertDialog(
                        getString(R.string.app_name), getString(R.string.server_error), getString(
                            R.string.accept_dialog
                        ), "", "", 1, false
                    )*/
                }
            })

        }
    }

    private fun initViews(){
        showBackButton()
        showToolbar()
        tvToolbarTitle.text = getString(R.string.text75)
        //alertDialogHelper = AlertDialogHelper(this@SellBooksActivity)
        //alertDialogHelper.showAlertDialog(getString(R.string.app_name),getString(R.string.text77),getString(R.string.accept_dialog),"","",-1, false)
        btAddBook.setOnClickListener {
            val intent = Intent(applicationContext, CameraActivity::class.java)
            intent.putExtra("isSellBook", true)
            intent.putExtra("url", "")
            startActivity(intent)
        }
        btConfirmSale.setOnClickListener {
            if (canSellBooks){
                val intent = Intent(applicationContext, ConfirmSellerAddressActivity::class.java)
                startActivity(intent)
            }else{
                alertDialogHelper.showAlertDialog(getString(R.string.app_name),getString(R.string.text91),getString(R.string.accept_dialog),"","",-1, false)
            }
        }
    }

    private fun refreshTotalAmount(productList:RealmResults<Product>){
        var totalAmount = 0f
        for (product:Product in productList){
            val bookAmount = product.quantity * product.price
            totalAmount += bookAmount
        }
        val conditions = realm.where(Conditions::class.java).findFirst()
        if (conditions != null){
            tvMinAmountSell.text = getString(R.string.text122) + " " + NumberFormat.getCurrencyInstance(Locale.getDefault()).format(totalAmount)

            val rest = conditions.minAmount - totalAmount
            if (rest > 0){
                tvBookMinAmount.visibility = View.VISIBLE

                tvBookMinAmount.text = getString(R.string.text382) + " " + NumberFormat.getCurrencyInstance(Locale.getDefault()).format(rest)+ " " + getString(R.string.text384)
                canSellBooks = false
            }else{
                tvBookMinAmount.visibility = View.GONE
                canSellBooks = true

            }
        }
    }

    private fun initProductList(){
        val products = realm.where(Product::class.java).findAll()
        if (products.size == 0){
            tvNoBooks.visibility = View.VISIBLE
        }else{
            tvNoBooks.visibility = View.GONE
        }
        refreshTotalAmount(products)
        val layoutManager = LinearLayoutManager(this)
        rvSellProductList.layoutManager = layoutManager
        rvSellProductList.setHasFixedSize(true)
        val sellProductAdapter = SellProductAdapter(products, this, true, true)
        rvSellProductList.adapter = sellProductAdapter
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun refreshProducts(refreshProductsEvent: RefreshProductsEvent) {
        initProductList()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }

    override fun onPause() {
        super.onPause()
        EventBus.getDefault().unregister(this)
    }

    override fun onResume() {
        super.onResume()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        initProductList()
        getSellerInfo()
    }

    override fun onPositiveClick(from: Int) {
        if (from == 1){
        }
    }

    override fun onNegativeClick(from: Int) {
    }

    override fun onNeutralClick(from: Int) {
    }

}