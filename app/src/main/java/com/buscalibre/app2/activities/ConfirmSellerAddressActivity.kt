package com.buscalibre.app2.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.buscalibre.app2.R
import com.buscalibre.app2.constants.ServerConstants
import com.buscalibre.app2.models.Product
import com.buscalibre.app2.models.Seller
import com.buscalibre.app2.models.SellerSuccess
import com.buscalibre.app2.models.Seller_
import com.buscalibre.app2.network.NetworkToken
import com.buscalibre.app2.network.NetworkUtil
import com.buscalibre.app2.network.RestClient
import com.buscalibre.app2.network.RetrofitClientInstance
import com.buscalibre.app2.util.AlertDialogHelper
import com.buscalibre.app2.util.ConfigUtil
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.activity_confirm_seller_address.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*


private var hasTaxId = false
private var hasValidTaxId = false
private var hasPickUpDate = false
private var dateToServer = ""
private var currentSellerConfig:Seller_? = null
private lateinit var datePickerDialog: DatePickerDialog
private const val DEFAULT_WAITING_DAYS = 4
private const val HEAD_API = "https://maps.googleapis.com/maps/api/geocode/json?address="
private const val API_KEY = "&key=AIzaSyBJ3DwEznn-mB4R_7ec5kHOvpL8TWTOoeM"



class ConfirmSellerAddressActivity : BaseActivity(), AlertDialogHelper.AlertDialogListener,
    DatePickerDialog.OnDateSetListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirm_seller_address)
        currentSellerConfig = Realm.getDefaultInstance().where(Seller_::class.java).findFirst()
        initViews()
        initData()
    }

    private fun initViews(){
        showBackButton()
        showToolbar()
        btAddressValidate.setOnClickListener {
            if(etAddress.text.toString().isEmpty()){
                notValidAddress()
                return@setOnClickListener
            }
            if(etAddress.text.toString().isNotEmpty() && NetworkUtil.checkEnabledInternet(applicationContext)){
                getFormattedAddress()
            }
        }

        btSelectDate.setOnClickListener {

            createCalendar()
        }

        tvToolbarTitle.text = getString(R.string.text92)
        btConfirmSellBooks.setOnClickListener {
            if (validateFields()){
                alertDialogHelper.showAlertDialog(
                    getString(R.string.app_name), getString(R.string.text99), getString(
                        R.string.accept_dialog
                    ), getString(R.string.cancel_dialog), "", 1, false
                )
            }
        }

        etRUT.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus && !hasTaxId){
                if(!validateRut(etRUT.text.toString().trim())){
                    hasValidTaxId = false
                    etRUT.error = getString(R.string.valid_rut)
                    alertDialogHelper.showAlertDialog(
                        getString(R.string.app_name), getString(R.string.valid_rut), getString(
                            R.string.accept_dialog
                        ), "", "", -1, false
                    )
                }else{
                    hasValidTaxId = true
                    var rut = etRUT.text.toString().trim().replace("\\D+", "").replace("-", "").replace(
                        ".",
                        ""
                    )
                    Log.e("rut", rut)
                    rut = StringBuilder(rut).insert(rut.length - 1, "-").toString()
                    etRUT.setText(rut.toUpperCase())
                }

            }
        }
    }

    private fun createCalendar(){
        val c = Calendar.getInstance()
        val year = c[Calendar.YEAR]
        val month = c[Calendar.MONTH]
        val day = c[Calendar.DAY_OF_MONTH]
        val minDate: Calendar
        val maxDate: Calendar
        val numberDayOnWeek = SimpleDateFormat("u", Locale.ENGLISH).format(System.currentTimeMillis()).toInt()
        val pickUpMinDays = if(currentSellerConfig?.pickupMinDays != null) currentSellerConfig!!.pickupMinDays[numberDayOnWeek - 1] else DEFAULT_WAITING_DAYS

        Log.e("year", year.toString())
        Log.e("month", month.toString())
        Log.e("day", day.toString())
        Log.e("numberDayOnWeek", numberDayOnWeek.toString())
        Log.e("pickUpMinDays", pickUpMinDays.toString())

        datePickerDialog = DatePickerDialog.newInstance(this@ConfirmSellerAddressActivity, year, month, day)

        if (currentSellerConfig != null) {
            minDate = Calendar.getInstance()
            minDate[Calendar.DAY_OF_MONTH] = day + pickUpMinDays!!
            datePickerDialog.minDate = minDate
            maxDate = Calendar.getInstance()
            maxDate[Calendar.DAY_OF_MONTH] = day + currentSellerConfig!!.maxDays
            datePickerDialog.maxDate = maxDate
            Log.e("minDate", (day + pickUpMinDays).toString())
            Log.e("maxDate", (day + currentSellerConfig!!.maxDays).toString())
        } else {
            minDate = Calendar.getInstance()
            minDate[Calendar.DAY_OF_MONTH] = day + DEFAULT_WAITING_DAYS
            datePickerDialog.minDate = minDate
            maxDate = Calendar.getInstance()
            maxDate[Calendar.YEAR] = year + 2
            datePickerDialog.maxDate = maxDate
        }

        datePickerDialog.isThemeDark = false
        datePickerDialog.showYearPickerFirst(false)
        datePickerDialog.setTitle(getString(R.string.pickup_date_))

        datePickerDialog.setOnDateSetListener { view, year, monthOfYear, dayOfMonth ->

            val dateFormat = SimpleDateFormat("EEEE")
            val date = Date(year, monthOfYear, dayOfMonth - 1)
            val dayOfWeek = dateFormat.format(date)
            val selectedDate: String =  getString(R.string.pickup_date) + "\n" + dayOfWeek + " " + dayOfMonth.toString() + "/" + (monthOfYear + 1) + "/" + year
            tvSelectedDate.text = selectedDate
            hasPickUpDate = true

            val dateFormatServer = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val dateServer = getDate(year,monthOfYear,dayOfMonth)
            dateToServer = dateFormatServer.format(dateServer)
            Log.e("dateToServer", dateToServer)

        }

        var loopdate = minDate
        while (minDate.before(maxDate)) {
            val dayOfWeek = loopdate[Calendar.DAY_OF_WEEK]
            if (dayOfWeek == Calendar.SUNDAY || dayOfWeek == Calendar.SATURDAY) {
                val disabledDays = arrayOfNulls<Calendar>(1)
                disabledDays[0] = loopdate
                datePickerDialog.disabledDays = disabledDays;
            }
            minDate.add(Calendar.DATE, 1)
            loopdate = minDate
        }

        datePickerDialog.show(supportFragmentManager, "DatePickerDialog")
    }

    private fun getFormattedAddress(){

        val address = etAddress.text.toString().replace(" ","%20")
        val queue = Volley.newRequestQueue(applicationContext)
        val url = HEAD_API + address + API_KEY
        val stringRequest = StringRequest(
            Request.Method.GET, url,
            {
                try {
                    val formattedAddress = it.substring(it.indexOf("formatted_address\" : \""),it.indexOf("\",\n" +
                            "         \"geometry\" :"))
                    etAddress.setText(formattedAddress.replace("formatted_address","").replace("\"","").replace(":","").trim())
                }catch (e:Exception){
                    notValidAddress()
                    Log.e("notValidAddress", e.stackTraceToString())
                }
            })
        {
            notValidAddress()
        }
        queue.add(stringRequest)
    }

    private fun notValidAddress(){
        etAddress.error = getString(R.string.adrr)
    }

    private fun getDate(year: Int, month: Int, day: Int): Date? {
        val cal = Calendar.getInstance()
        cal[Calendar.YEAR] = year
        cal[Calendar.MONTH] = month
        cal[Calendar.DAY_OF_MONTH] = day
        cal[Calendar.HOUR_OF_DAY] = 0
        cal[Calendar.MINUTE] = 0
        cal[Calendar.SECOND] = 0
        cal[Calendar.MILLISECOND] = 0
        return cal.time
    }

    private fun initData(){
        val seller = realm.where(Seller::class.java).findFirst()
        if (seller != null){
            val taxID = seller.taxId
            if(taxID != null && taxID.isNotEmpty()){
                etRUT.setText(taxID)
                etRUT.isFocusable = false
                etRUT.isClickable = true
                etRUT.isActivated = false
                etRUT.setCursorVisible(false);
                etRUT.setKeyListener(null);
                hasTaxId = true
                hasValidTaxId = true
            }
        }
    }





    private fun validateRut(rut: String): Boolean {
        var rut = rut
        var isValid = false
        try {
            rut = rut.toUpperCase()
            rut = rut.replace(".", "")
            rut = rut.replace("-", "")
            var rutAux = rut.substring(0, rut.length - 1).toInt()
            val dv = rut[rut.length - 1]
            var m = 0
            var s = 1
            while (rutAux != 0) {
                s = (s + rutAux % 10 * (9 - m++ % 6)) % 11
                rutAux /= 10
            }
            if (dv == (if (s != 0) s + 47 else 75).toChar()) {
                isValid = true
            }
        } catch (e: NumberFormatException) {
        } catch (e: Exception) {
        }
        return isValid
    }

    private fun confirmSell(){

        if (NetworkUtil.checkEnabledInternet(applicationContext)) {
            spotsDialog.show()
            val restClient = RetrofitClientInstance.getRetrofitInstance().create(RestClient::class.java)
            val json = JsonObject()
            val pickUp = JsonObject()
            val seller = JsonObject()
            val sellerDB = realm.where(Seller::class.java).findFirst()
            if (sellerDB != null){
                seller.addProperty("sellerId", sellerDB.sellerId.toString())
            }else{
                seller.addProperty("taxId", etRUT.text.toString())
            }
            json.add("seller", seller)
            if(etDepartment.text.toString().isNotEmpty()){
                pickUp.addProperty("address", etAddress.text.toString() + " " + etDepartment.text.toString())
            }else{
                pickUp.addProperty("address", etAddress.text.toString())
            }
            pickUp.addProperty("county", etCounty.text.toString())
            pickUp.addProperty("city", etCity.text.toString())
            pickUp.addProperty("phone", etPhoneNumber.text.toString())
            pickUp.addProperty("date", dateToServer)

            if (etZipCode.text.toString().isNotEmpty()){
                pickUp.addProperty("zip", etZipCode.text.toString())
            }

            json.add("pickup", pickUp)

            val productList = JsonArray()

            val products = realm.where(Product::class.java).findAll()

            for (product1:Product in products){
                val product = JsonObject()
                product.addProperty("productType", 1)
                product.addProperty("productKey", product1.isbn)
                product.addProperty("purchaseValue", product1.price)
                product.addProperty("quantity", product1.quantity)
                product.addProperty("condition", product1.conditionBookID)
                productList.add(product)
            }

            json.add("productList", productList)

            Log.e("jsonSellerSuccess", json.toString())
            val call: Call<SellerSuccess> = restClient.sellerOrder(
                userLogin?.token,
                ConfigUtil.getLocaleISO639(),
                country?.id,
                json
            )
            call.enqueue(object : Callback<SellerSuccess?> {
                override fun onResponse(
                    call: Call<SellerSuccess?>,
                    response: Response<SellerSuccess?>
                ) {
                    val sellerSuccess: SellerSuccess? = response.body()
                    if (sellerSuccess != null) {
                        if (sellerSuccess.blstatus == ServerConstants.AUTH_TOKEN_EXPIRED || sellerSuccess.blstatus == ServerConstants.AUTH_TOKEN_ERROR) {
                            NetworkToken.refresh(applicationContext)
                            return
                        }
                        if (sellerSuccess.blstatus == ServerConstants.NO_ERROR) {

                            val intent = Intent(applicationContext, SellSuccessActivity::class.java)
                            intent.putExtra("purchaseOrderId", sellerSuccess.purchaseOrderId)
                            startActivity(intent)
                            Product.deleteAll(realm)
                            finish()

                        } else {
                            alertDialogHelper.showAlertDialog(
                                getString(R.string.app_name), sellerSuccess.blmessage, getString(
                                    R.string.accept_dialog
                                ), "", "", -1, false
                            )

                        }
                    } else {
                        alertDialogHelper.showAlertDialog(
                            getString(R.string.app_name),
                            getString(R.string.server_error),
                            getString(
                                R.string.accept_dialog
                            ),
                            "",
                            "",
                            -1,
                            false
                        )
                    }
                    spotsDialog.dismiss()
                }

                override fun onFailure(call: Call<SellerSuccess?>, t: Throwable) {
                    spotsDialog.dismiss()
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

    private fun validateFields():Boolean{
        var validateComplete = true

        if (etAddress.text.isEmpty()){
            validateComplete = false
            etAddress.error = getString(R.string.valid_address)
            alertDialogHelper.showAlertDialog(
                getString(R.string.app_name), getString(R.string.valid_address), getString(
                    R.string.accept_dialog
                ), "", "", -1, false
            )
        }

        if (!hasValidTaxId){
            validateComplete = false
            alertDialogHelper.showAlertDialog(
                getString(R.string.app_name), getString(R.string.valid_rut), getString(
                    R.string.accept_dialog
                ), "", "", -1, false
            )
            etRUT.error = getString(R.string.valid_rut)
        }

        if (!hasPickUpDate){
            validateComplete = false
            alertDialogHelper.showAlertDialog(
                getString(R.string.app_name), getString(R.string.elige_un_d_a_para_el_retiro_de_tus_libros), getString(
                    R.string.accept_dialog
                ), "", "", -1, false
            )
        }

        if (etCounty.text.isEmpty()){
            validateComplete = false
            etCounty.error = getString(R.string.text97)
            alertDialogHelper.showAlertDialog(
                getString(R.string.app_name), getString(R.string.enter_county), getString(
                    R.string.accept_dialog
                ), "", "", -1, false
            )
        }

        if (etCity.text.isEmpty()){
            validateComplete = false
            etCity.error = getString(R.string.valid_city)
            alertDialogHelper.showAlertDialog(
                getString(R.string.app_name), getString(R.string.valid_city), getString(
                    R.string.accept_dialog
                ), "", "", -1, false
            )
        }

        if (etPhoneNumber.text.isEmpty()){
            validateComplete = false
            etPhoneNumber.error = getString(R.string.inputl_valid_phonenumber)
            alertDialogHelper.showAlertDialog(
                getString(R.string.app_name), getString(R.string.inputl_valid_phonenumber), getString(
                    R.string.accept_dialog
                ), "", "", -1, false
            )
        }
        return validateComplete
    }

    override fun onPositiveClick(from: Int) {
        if (from == 1){
            confirmSell()
        }
    }

    override fun onNegativeClick(from: Int) {
    }

    override fun onNeutralClick(from: Int) {
    }

    override fun onDateSet(view: DatePickerDialog?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
    }
}