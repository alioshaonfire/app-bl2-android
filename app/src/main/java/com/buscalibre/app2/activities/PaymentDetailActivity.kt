package com.buscalibre.app2.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.buscalibre.app2.R
import com.buscalibre.app2.adapters.DetailPaymentsAdapter
import com.buscalibre.app2.constants.ServerConstants
import com.buscalibre.app2.models.PaymentDetail
import com.buscalibre.app2.models.ProductList
import com.buscalibre.app2.models.Seller
import com.buscalibre.app2.network.NetworkToken
import com.buscalibre.app2.network.NetworkUtil
import com.buscalibre.app2.network.RestClient
import com.buscalibre.app2.network.RetrofitClientInstance
import com.buscalibre.app2.util.AlertDialogHelper
import com.buscalibre.app2.util.ConfigUtil
import com.buscalibre.app2.util.DateUtil
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.activity_payment_detail.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.*


class PaymentDetailActivity : BaseActivity(), AlertDialogHelper.AlertDialogListener {

    private var paymentDate = ""
    private var paymentId = ""

    private  var detailPaymentsAdapter: DetailPaymentsAdapter? = null
    private var convertedPaymentDate = ""



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_detail)
        getDataFromIntent()
        initViews()
    }
    private fun getDataFromIntent(){
        if (intent.extras != null){
            paymentDate = intent.getStringExtra("paymentDate")!!
            paymentId = intent.getStringExtra("paymentId")!!
            val title = getString(R.string.payment_detail) + " #" + paymentId
            //val title = getString(R.string.payment_detail)
            tvToolbarTitle.text = title
            convertedPaymentDate = DateUtil.convertDate(paymentDate.replace(".000Z", ""), "yyyy-MM")
            Log.e("paymentDate", paymentDate)
            getPaymentsDetail()
        }else                            alertDialogHelper.showAlertDialog(getString(R.string.app_name), getString(
                    R.string.text117), getString(R.string.accept_dialog), "", "", 3, false)

    }

    private fun initViews(){
        showBackButton()
        showToolbar()
        srlSaleDetailList.setOnRefreshListener {
            getPaymentsDetail()
        }
    }

    private fun getPaymentsDetail(){

        if (NetworkUtil.checkEnabledInternet(applicationContext)) {
            spotsDialog.show()
            val restClient = RetrofitClientInstance.getRetrofitInstance().create(RestClient::class.java)
            val json = JsonObject()
            json.addProperty("productType", ServerConstants.SELLER_PRODUCT_TYPE_BOOK)
            json.addProperty("paymentId", paymentId)

            val seller = realm.where(Seller::class.java).findFirst()
            if (seller != null){
                json.addProperty("sellerId", seller.sellerId)
            }else{
                json.addProperty("sellerId", "")
            }

            Log.e("jsonPaymentDetail", json.toString())
            val call: Call<PaymentDetail> = restClient.getPaymentDetail(userLogin?.token, ConfigUtil.getLocaleISO639(),
                country?.id, json)
            call.enqueue(object : Callback<PaymentDetail?> {
                override fun onResponse(call: Call<PaymentDetail?>, response: Response<PaymentDetail?>) {
                    val paymentDetail: PaymentDetail? = response.body()
                    if (paymentDetail != null) {
                        if (paymentDetail.blstatus == ServerConstants.AUTH_TOKEN_EXPIRED || paymentDetail.blstatus == ServerConstants.AUTH_TOKEN_ERROR) {
                            NetworkToken.refresh(applicationContext)
                            return
                        }
                        if (paymentDetail.blstatus == ServerConstants.NO_ERROR) {
                            PaymentDetail.deleteAll(realm)
                            realm.executeTransaction {
                                realm.copyToRealm(paymentDetail)
                            }
                            tvPaymentStatus.text = paymentDetail.paymentStatusText
                            tvPaymentStatus.setBackgroundColor(
                                if (paymentDetail.paymentStatus != ServerConstants.SELLER_PAYMENT_STATUS_PENDING) resources
                                    .getColor(R.color.to_pay) else resources
                                    .getColor(R.color.payed)
                            )
                            tvPaymentDate.text = DateUtil.convertDate(paymentDetail.paymentDate.replace(".000Z", ""), "dd-MM-yyyy HH:mm")
                            tvPaymentBank.text = paymentDetail.destination.name + "/" + paymentDetail.destination.accountType
                            tvPaymentBankAccount.text = paymentDetail.destination.accountNumber
                            llPaymentDetailPay.visibility = View.VISIBLE
                            rlTotalView.visibility = View.VISIBLE
                            initPaymentList()

                        } else {
                            alertDialogHelper.showAlertDialog(getString(R.string.app_name), paymentDetail.blmessage, getString(R.string.accept_dialog), "", "", 1, false)
                        }
                    } else {
                        alertDialogHelper?.showAlertDialog(getString(R.string.app_name), getString(R.string.server_error), getString(R.string.accept_dialog), "", "", 1, false)
                    }
                    spotsDialog.hide()

                    if (srlSaleDetailList.isRefreshing) {
                        srlSaleDetailList.isRefreshing = false
                    }
                }

                override fun onFailure(call: Call<PaymentDetail?>, t: Throwable) {
                    spotsDialog.hide()
                    if (srlSaleDetailList.isRefreshing) {
                        srlSaleDetailList.isRefreshing = false
                    }
                    alertDialogHelper?.showAlertDialog(
                        getString(R.string.app_name), getString(R.string.server_error), getString(
                            R.string.accept_dialog
                        ), "", "", 1, false
                    )
                }
            })

        }else{
            if (srlSaleDetailList.isRefreshing) {
                srlSaleDetailList.isRefreshing = false
            }
            alertDialogHelper.showAlertDialog(
                getString(R.string.app_name), getString(R.string.connect_error), getString(
                    R.string.accept_dialog
                ), "", "", -1, false
            )
        }
    }

    private fun initPaymentList(){
        val products = realm.where(ProductList::class.java).findAll()
        val layoutManager = LinearLayoutManager(applicationContext)
        rvSaleDetailList.layoutManager = layoutManager
        rvSaleDetailList.setHasFixedSize(true)
        detailPaymentsAdapter = DetailPaymentsAdapter(products, applicationContext, true, true)
        rvSaleDetailList.adapter = detailPaymentsAdapter
        var total = 0f
        for (product: ProductList in products){
            total += (product.purchaseValue * product.quantity)
        }
        val totalAmount = NumberFormat.getCurrencyInstance(Locale.getDefault()).format(total)
        tvMonthTotal.text = totalAmount
    }

    override fun onPositiveClick(from: Int) {
        super.onPositiveClick(from)
        if (from == 3){
            finish()
        }
    }

    override fun onNegativeClick(from: Int) {
        super.onNegativeClick(from)
    }

    override fun onNeutralClick(from: Int) {
        super.onNeutralClick(from)
    }
}