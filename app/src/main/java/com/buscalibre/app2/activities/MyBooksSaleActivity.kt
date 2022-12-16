package com.buscalibre.app2.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.buscalibre.app2.R
import com.buscalibre.app2.adapters.ShowcaseProductAdapter
import com.buscalibre.app2.constants.ServerConstants
import com.buscalibre.app2.models.ProductList
import com.buscalibre.app2.models.Seller
import com.buscalibre.app2.models.SellerShowcase
import com.buscalibre.app2.models.SystemConfig
import com.buscalibre.app2.network.NetworkToken
import com.buscalibre.app2.network.NetworkUtil
import com.buscalibre.app2.network.RestClient
import com.buscalibre.app2.network.RetrofitClientInstance
import com.buscalibre.app2.util.ConfigUtil
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.activity_my_books_sale.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyBooksSaleActivity : BaseActivity() {

    private var NUMBER_PAGE = 1
    private var FIRST_PAGE = 1
    private var PAGE_LENGHT = 10
    private var hasMoreProducts = true
    private var isCallingServer = false
    private var isRefresh = true
    private  var showcaseProductAdapter: ShowcaseProductAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_books_sale)
        initData()
        initViews()
    }

    private fun initViews(){
        showBackButton()
        showToolbar()
        tvToolbarTitle.text = getString(R.string.mis_libros_a_la_venta)
        srlProductShowcaseList.setOnRefreshListener {
            getSellerShowCase(1, PAGE_LENGHT.toString())
            isRefresh = true
        }
    }
    private fun initData(){
        val config = realm.where(SystemConfig::class.java).findFirst()
        if (config != null){
            if (config.showcasePageLen != null){
                PAGE_LENGHT = config.showcasePageLen
            }
            getSellerShowCase(FIRST_PAGE, PAGE_LENGHT.toString())
        }
    }

    private fun getSellerShowCase(numberPage: Int, lenghtPage: String){
        if (NetworkUtil.checkEnabledInternet(applicationContext)) {
            spotsDialog.show()
            val restClient = RetrofitClientInstance.getRetrofitInstance().create(RestClient::class.java)
            val json = JsonObject()
            json.addProperty("productType", ServerConstants.SELLER_PRODUCT_TYPE_BOOK)
            val seller = realm.where(Seller::class.java).findFirst()
            if (seller != null){
                json.addProperty("sellerId", seller.sellerId)
            }else{
                json.addProperty("sellerId", "")
            }
            Log.e("jsonShowcase", json.toString())
            val call: Call<SellerShowcase> = restClient.sellerShowcase(userLogin?.token, ConfigUtil.getLocaleISO639(),
                country?.id, numberPage.toString(), lenghtPage, json)
            call.enqueue(object : Callback<SellerShowcase?> {
                override fun onResponse(call: Call<SellerShowcase?>, response: Response<SellerShowcase?>) {
                    val sellerShowcase: SellerShowcase? = response.body()
                    if (sellerShowcase != null) {
                        if (sellerShowcase.blstatus == ServerConstants.AUTH_TOKEN_EXPIRED || sellerShowcase.blstatus == ServerConstants.AUTH_TOKEN_ERROR) {
                            NetworkToken.refresh(applicationContext)
                            return
                        }
                        if (sellerShowcase.blstatus == ServerConstants.NO_ERROR) {
                            hasMoreProducts = sellerShowcase.pendingRows
                            if (numberPage == 1) {
                                if(sellerShowcase.productList != null && sellerShowcase.productList.size > 0){
                                    SellerShowcase.deleteAll(realm)
                                    realm.executeTransaction {
                                        realm.copyToRealm(sellerShowcase)
                                    }
                                    //initBookList()
                                    showcaseProductAdapter?.notifyDataSetChanged()
                                    NUMBER_PAGE = numberPage
                                    hasMoreProducts = true
                                    initBookList()
                                }else{
                                    hasMoreProducts = false
                                    tvNoElements.visibility = View.VISIBLE
                                }
                            } else if (numberPage >= 1) {
                                realm.executeTransaction {
                                    realm.copyToRealm(sellerShowcase)
                                }
                                if (sellerShowcase.productList == null || sellerShowcase.productList.size == 0){
                                    hasMoreProducts = false
                                }
                                NUMBER_PAGE = numberPage
                                showcaseProductAdapter?.notifyDataSetChanged()
                                if (sellerShowcase.productList == null) {
                                    isRefresh = false
                                    Toast.makeText(applicationContext, R.string.text30, Toast.LENGTH_SHORT).show()
                                }
                            }



                        } else {
                            alertDialogHelper?.showAlertDialog(getString(R.string.app_name), sellerShowcase.blmessage, getString(R.string.accept_dialog), "", "", 1, false)
                        }
                    } else {
                        alertDialogHelper?.showAlertDialog(getString(R.string.app_name), getString(R.string.server_error), getString(R.string.accept_dialog), "", "", 1, false)
                    }
                    if (srlProductShowcaseList.isRefreshing) {
                        srlProductShowcaseList.isRefreshing = false
                    }
                    spotsDialog.dismiss()
                }

                override fun onFailure(call: Call<SellerShowcase?>, t: Throwable) {
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
        val products = realm.where(ProductList::class.java).findAll()
        if (products == null || products.size == 0){
            tvNoElements.visibility = View.VISIBLE
            return
        }
        val layoutManager = LinearLayoutManager(this@MyBooksSaleActivity)
        rvShowcase.layoutManager = layoutManager
        rvShowcase.setHasFixedSize(true)
        showcaseProductAdapter = ShowcaseProductAdapter(products, this@MyBooksSaleActivity, false, false)
        rvShowcase.adapter = showcaseProductAdapter
        rvShowcase.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                if (!srlProductShowcaseList.isRefreshing && !isCallingServer && hasMoreProducts) {
                    if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0 && isRefresh) {
                        getSellerShowCase(NUMBER_PAGE + 1, PAGE_LENGHT.toString())
                    }/*else{
                        Toast.makeText(applicationContext, getString(R.string.text124), Toast.LENGTH_SHORT).show()
                    }*/
                }
            }
        })
    }
}