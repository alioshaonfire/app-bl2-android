package com.buscalibre.app2.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import com.buscalibre.app2.R
import com.buscalibre.app2.constants.ServerConstants
import com.buscalibre.app2.models.Book
import com.buscalibre.app2.models.Product
import com.buscalibre.app2.models.SellerQuote
import com.buscalibre.app2.network.NetworkToken
import com.buscalibre.app2.network.NetworkUtil
import com.buscalibre.app2.network.RestClient
import com.buscalibre.app2.network.RetrofitClientInstance
import com.buscalibre.app2.util.AlertDialogHelper
import com.buscalibre.app2.util.ConfigUtil
import com.google.gson.JsonObject
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.activity_book_detail.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList


private var isbn = ""
private var counterQty = 1
private lateinit var product:Product
private var bookList = ArrayList<String>()
private var conditionSelected = ""


class BookDetailActivity : BaseActivity(), AlertDialogHelper.AlertDialogListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_detail)
        initViews()
        getDataFromIntent()
    }

    private fun initViews(){

        showBackButton()
        tvToolbarTitle.text = getString(R.string.text388)
        btRemoveQty.setOnClickListener {
            if (counterQty > 1){
                counterQty -= 1
                tvBooksQty.text = counterQty.toString()
            }
        }
        btAddQty.setOnClickListener {
            counterQty += 1
            tvBooksQty.text = counterQty.toString()
        }
        btSaveProduct.setOnClickListener {
            val sameSavedProduct = realm.where(Product::class.java).equalTo("isbn", isbn).findFirst()
            if (sameSavedProduct != null){

                realm.executeTransaction {
                    val bookPrice = product.price
                    if (bookPrice != sameSavedProduct.price){
                        sameSavedProduct.price = bookPrice
                    }
                    sameSavedProduct.quantity = sameSavedProduct.quantity + counterQty
                }
            }else{
                realm.executeTransaction {
                    product.quantity = counterQty
                    product.isbn = isbn
                    product.conditionBookID = "2"
                    product.conditionBookLabel = "Usado en buen estado"
                    realm.copyToRealm(product)
                }
            }
            counterQty = 1
            finish()


        }

        btErrorBack.setOnClickListener {
            finish()
        }
        val bookResults = realm.where(Book::class.java).findAll()
        bookList = ArrayList<String>()
        if (bookResults != null){
            for (book:Book in bookResults){
                bookList.add(book.label)
            }
        }else{
            finish()
        }
        spinnerCondition.item = bookList
        spinnerCondition.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
                conditionSelected = bookList.get(position)
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }
        spinnerCondition.selectedItemListColor = resources.getColor(R.color.oran_bl)
    }

    private fun getDataFromIntent(){
        if (intent.extras != null){
            isbn = intent.getStringExtra("isbn")!!
            getSellerQuote(isbn)
            Log.e("isbn", isbn)
        }
    }

    private fun getSellerQuote(isbn: String){
        if (NetworkUtil.checkEnabledInternet(applicationContext)) {
            val json = JsonObject()
            json.addProperty("productType", 1)
            json.addProperty("productKey", isbn)

            Log.e("jsonMessageRead", json.toString())
            val restClient = RetrofitClientInstance.getRetrofitInstance().create(RestClient::class.java)
            val call: Call<SellerQuote> = restClient.sellerQuote(
                userLogin?.token,
                ConfigUtil.getLocaleISO639(),
                country?.id,
                json
            )
            call.enqueue(object : Callback<SellerQuote?> {
                override fun onResponse(
                    call: Call<SellerQuote?>,
                    response: Response<SellerQuote?>
                ) {
                    val sellerQuote: SellerQuote? = response.body()
                    if (sellerQuote != null) {
                        if (sellerQuote.blstatus == ServerConstants.AUTH_TOKEN_EXPIRED || sellerQuote.blstatus == ServerConstants.AUTH_TOKEN_ERROR) {
                            NetworkToken.refresh(applicationContext)
                            return
                        }
                        if (sellerQuote.blstatus == ServerConstants.NO_ERROR) {
                            if (sellerQuote.product != null) {
                                tvIsbnDesc.text = isbn
                                tvTitleNameDesc.text = sellerQuote.product.name
                                tvBookDesc.text = sellerQuote.product.description
                                if(sellerQuote.product.imageURL.isNotEmpty()){
                                    Picasso.get()
                                        .load(sellerQuote.product.imageURL)
                                        .fit()
                                        .centerCrop()
                                        .placeholder(R.drawable.preload_menu)
                                        .into(ivBookDesc)
                                }

                                tvPriceBook.text =
                                    NumberFormat.getCurrencyInstance(Locale.getDefault()).format(
                                        sellerQuote.product.price
                                    )
                                product = sellerQuote.product
                                rlQuoteSuccess.visibility = View.VISIBLE
                                btSaveProduct.visibility = View.VISIBLE
                            }
                        }else if(sellerQuote.blstatus == ServerConstants.NO_QUOTE_ISBN){
                            rlBookHeader.visibility = View.GONE
                            llQtyCounter.visibility = View.GONE
                            spinnerCondition.visibility = View.GONE
                            llPriceBook.visibility = View.GONE
                            llPriceBookQuote.visibility = View.GONE
                            rlQuoteSuccessError.visibility = View.VISIBLE
                            tvBookStateNote.visibility = View.GONE

                        } else {
                            rlQuoteSuccessError.visibility = View.VISIBLE
                            btErrorBack.visibility = View.VISIBLE
                        }
                    } else {
                        rlQuoteSuccessError.visibility = View.VISIBLE
                        btErrorBack.visibility = View.VISIBLE


                    }
                }

                override fun onFailure(call: Call<SellerQuote?>, t: Throwable) {
                    rlQuoteSuccessError.visibility = View.VISIBLE
                    btErrorBack.visibility = View.VISIBLE

                }
            })

        }else{
            rlQuoteSuccessError.visibility = View.VISIBLE
            btErrorBack.visibility = View.VISIBLE
        }
    }

    override fun onPositiveClick(from: Int) {
        if (from == 1){
            finish()
        }
    }

    override fun onNegativeClick(from: Int) {

    }

    override fun onNeutralClick(from: Int) {
    }

}