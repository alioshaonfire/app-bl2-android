package com.buscalibre.app2.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.buscalibre.app2.R
import com.buscalibre.app2.adapters.CountryListAdapter
import com.buscalibre.app2.constants.ServerConstants
import com.buscalibre.app2.events.CountrySelectedEvent
import com.buscalibre.app2.models.*
import com.buscalibre.app2.network.NetworkToken
import com.buscalibre.app2.network.NetworkUtil
import com.buscalibre.app2.network.RestClient
import com.buscalibre.app2.network.RetrofitClientInstance
import com.buscalibre.app2.util.AlertDialogHelper
import com.buscalibre.app2.util.CheckStatus
import com.buscalibre.app2.util.ConfigUtil
import com.google.gson.JsonObject
import io.realm.RealmList
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.activity_select_country.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class SelectCountryActivity : BaseActivity(), AlertDialogHelper.AlertDialogListener {

    private var countryID:String = ""
    lateinit var countryList:RealmList<Country>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_country)
        CheckStatus.userLogin(realm, this@SelectCountryActivity)
        initViews()
        //initCountries()
        getCountryFromServer()
    }

    override fun onBackPressed() {
    }

    /*private fun initCountries(){
        val countryResults = realm.where(Country::class.java).findAll()
        if (countryResults.size > 0){
            val list:RealmList<Country> = RealmList()
            list.addAll(countryResults)
             countryList = list
            showCountryList(list)
        }else{
            getCountryFromServer()
        }
    }*/

    private fun initViews(){
        tvToolbarTitle.text = getString(R.string.text1)
        //alertDialogHelper = AlertDialogHelper(this@SelectCountryActivity)
        //showBackButton()
        srlCountryList.setOnRefreshListener {
            getCountryFromServer()
        }
    }

    private fun getCountryFromServer(){
        if (NetworkUtil.checkEnabledInternet(applicationContext)) {
            showProgress()
            val restClient = RetrofitClientInstance.getRetrofitInstance().create(RestClient::class.java)
            val call: Call<ServerCountries> = restClient.getServerCountries(userLogin?.token, ConfigUtil.getLocaleISO639())
            call.enqueue(object : Callback<ServerCountries?> {
                override fun onResponse(call: Call<ServerCountries?>, response: Response<ServerCountries?>) {
                    val serverCountries: ServerCountries? = response.body()
                    if (serverCountries != null) {
                        if(serverCountries.blstatus == ServerConstants.AUTH_TOKEN_EXPIRED || serverCountries.blstatus == ServerConstants.AUTH_TOKEN_ERROR){
                            NetworkToken.refresh(applicationContext)
                            return
                        }
                        if (serverCountries.blstatus == ServerConstants.NO_ERROR) {
                            ServerCountries.deleteAll(realm)
                            realm.executeTransaction {
                                realm.copyToRealm(serverCountries)
                            }
                            countryList = serverCountries.countries
                            showCountryList(countryList)
                        }else{
                            Toast.makeText(applicationContext, serverCountries.blmessage, Toast.LENGTH_LONG).show()
                        }
                    }else{
                        alertDialogHelper.showAlertDialog(getString(R.string.app_name),getString(R.string.server_error),getString(R.string.accept_dialog),"","",-1, false)
                    }
                    hideProgress()
                    if (srlCountryList.isRefreshing) {
                        srlCountryList.isRefreshing = false
                    }
                }

                override fun onFailure(call: Call<ServerCountries?>, t: Throwable) {
                    hideProgress()
                    if (srlCountryList.isRefreshing) {
                        srlCountryList.isRefreshing = false
                    }
                    alertDialogHelper.showAlertDialog(getString(R.string.app_name),getString(R.string.server_error),getString(R.string.accept_dialog),"","",-1, false)
                }
            })

        }else{
            alertDialogHelper.showAlertDialog(getString(R.string.app_name),getString(R.string.connect_error),getString(R.string.accept_dialog),"","",-1, false)
        }
    }

    private fun setSelectedCountry(countryID:String){

        if (NetworkUtil.checkEnabledInternet(applicationContext)) {
            val json = JsonObject()
            json.addProperty("countryId", countryID)
            val restClient = RetrofitClientInstance.getRetrofitInstance().create(RestClient::class.java)
            val call: Call<StandarResponse> = restClient.setSelectedCountry(userLogin?.token, ConfigUtil.getLocaleISO639(), json)
            call.enqueue(object : Callback<StandarResponse?> {
                override fun onResponse(call: Call<StandarResponse?>, response: Response<StandarResponse?>) {
                    val standarResponse: StandarResponse? = response.body()
                    if (standarResponse != null) {
                    }
                }

                override fun onFailure(call: Call<StandarResponse?>, t: Throwable) {

                }
            })

        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun countrySelected(countrySelectedEvent: CountrySelectedEvent) {
        countryID = countrySelectedEvent.country.id
        getUserCart(countryID)
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
    }

    override fun onPositiveClick(from: Int) {
    }

    override fun onNegativeClick(from: Int) {
    }

    override fun onNeutralClick(from: Int) {
    }

    private fun showCountryList(countryList: RealmList<Country>) {
        val recyclerViewLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this@SelectCountryActivity, LinearLayoutManager.VERTICAL, false)
        rvCountries.layoutManager = recyclerViewLayoutManager
        rvCountries.adapter = CountryListAdapter(countryList, this@SelectCountryActivity)
    }

    fun confirmCountrySelectedOnClick(view: View) {

        if (countryID != ""){
            val countriesResult = realm.where(Country::class.java).findAll()
            for (co:Country in countriesResult){

                if (co.id == countryID){
                    val userCountry = realm.where(UserCountry::class.java).equalTo("userEmail", userLogin?.email).findFirst()
                    if(countryID != userCountry?.countryID){
                        val products = realm.where(Product::class.java).findAll()
                        realm.executeTransaction {
                            products.deleteAllFromRealm()
                        }
                    }
                    realm.executeTransaction {
                        co.isSelected = true
                        userLogin?.countryID = co.id
                    }
                    if (userCountry != null){
                        realm.executeTransaction {
                            userCountry.countryID = countryID
                        }
                    }else{
                        UserCountry.create(realm,countryID, userLogin?.email)
                    }
                    setSelectedCountry(countryID)

                    val intent = Intent(applicationContext, HomeActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }else{
                    realm.executeTransaction {
                        co.isSelected = false
                    }
                }
            }

        }else{
            alertDialogHelper.showAlertDialog(getString(R.string.app_name),getString(R.string.text2),getString(R.string.accept_dialog),"","",-1, false)
        }
    }

}