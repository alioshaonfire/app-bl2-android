package com.buscalibre.app2.activities

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.buscalibre.app2.R
import com.buscalibre.app2.constants.AppConfig
import com.buscalibre.app2.constants.ServerConstants
import com.buscalibre.app2.models.Country
import com.buscalibre.app2.models.UserCart
import com.buscalibre.app2.models.UserLogin
import com.buscalibre.app2.network.NetworkToken
import com.buscalibre.app2.network.NetworkUtil
import com.buscalibre.app2.network.RestClient
import com.buscalibre.app2.network.RetrofitClientInstance
import com.buscalibre.app2.util.AlertDialogHelper
import com.buscalibre.app2.util.ConfigUtil
import com.squareup.picasso.Picasso
import dmax.dialog.SpotsDialog
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_base.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

open class BaseActivity : AppCompatActivity(), AlertDialogHelper.AlertDialogListener{

    lateinit var spotsDialog: AlertDialog
    lateinit var alertDialogHelper: AlertDialogHelper
    var realm: Realm = Realm.getDefaultInstance()
    var userLogin: UserLogin? = null
    var country:Country? = null
    val HOME = 1L
    val MY_ACCOUNT = 2L
    val PAYMENT_METHODS = 6L
    val BOOK_SELLER = 7L
    val LOGOUT = 3L
    val SELECT_COUNTRY = 4L
    val INBOX = 5L
    var qtyProducts = 0

    //lateinit var drawerbuilder:DrawerBuilder
    var hasNewMessages = false
    //lateinit var item:PrimaryDrawerItem

    private val TAG = "BaseActivity"

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    override fun setContentView(layoutResID: Int) {

        country = realm.where(Country::class.java).equalTo("isSelected", true).findFirst()
        userLogin = realm.where(UserLogin::class.java).findFirst()

        spotsDialog = SpotsDialog.Builder()
            .setContext(this@BaseActivity)
            .setTheme(R.style.CustomProgress)
            .setCancelable(false)
            .build()

        alertDialogHelper = AlertDialogHelper(this@BaseActivity)

        val coordinatorLayout: CoordinatorLayout = layoutInflater.inflate(R.layout.activity_base, null) as CoordinatorLayout
        val activityContainer: FrameLayout = coordinatorLayout.findViewById(R.id.layout_container)
        layoutInflater.inflate(layoutResID, activityContainer, true)
        super.setContentView(coordinatorLayout)
        setSupportActionBar(baseToolbar)
    }

    open fun showBackButton(){

        ivBackButton.visibility = View.VISIBLE
        ivBackButton.setOnClickListener {
            finish()
        }
    }

    open fun showCloseButton(){
        ivCloseButton.visibility = View.VISIBLE
        ivCloseButton.setOnClickListener {
            finish()

        }
    }

    open fun showFlagSelector(){
        Picasso.get()
            .load(AppConfig.URL_SERVER_PROD + country?.icon)
            .placeholder(R.drawable.preload_icon)
            .into(ivSelectFlag)

        ivSelectFlag.visibility = View.VISIBLE
        ivSelectFlag.setOnClickListener {
            val intent = Intent(applicationContext, SelectCountryActivity::class.java)
            startActivity(intent)
        }
    }

    open fun showCart(){
        getUserCart(country!!.id)
        llCartTool.visibility = View.VISIBLE
        tvCartQty.bringToFront()
        llCartTool.setOnClickListener {
            if (country?.url?.cart != null){
                val intent = Intent(applicationContext, BaseWebViewActivity::class.java)
                intent.putExtra("url", country?.url?.cart)
                intent.putExtra("header", userLogin!!.webToken)
                intent.putExtra("title", getString(R.string.text51))
                startActivity(intent)
            }else{
                Toast.makeText(applicationContext, getString(R.string.text46), Toast.LENGTH_LONG).show()
            }
        }
        baseToolbar.bringToFront()
    }

    open fun hideCart(){
        llCartTool.visibility = View.GONE

    }

    open fun showToolbar(){
        baseToolbar.visibility = View.VISIBLE
    }

    open fun hideToolbar(){
        baseToolbar.visibility = View.GONE
    }


    open fun showProgress(){
        if(spotsDialog != null){
            spotsDialog.show()
        }
    }

    open fun hideProgress(){
        if(spotsDialog != null){
            spotsDialog.hide()
        }
    }

    override fun onPositiveClick(from: Int) {
        if (from == 2){
            logOut()
        }
    }

    private fun logOut(){
        realm.executeTransaction {
            realm.where(UserLogin::class.java).findAll()?.deleteAllFromRealm()
        }
        val countries = realm.where(Country::class.java).findAll()
        for (co:Country in countries){
            realm.executeTransaction {
                co.isSelected = false
            }
        }
        val intent = Intent(applicationContext, WelcomeUserActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    open fun getUserCart(countryID:String){

        if (NetworkUtil.checkEnabledInternet(applicationContext)) {
            val restClient = RetrofitClientInstance.getRetrofitInstance().create(RestClient::class.java)
            val call: Call<UserCart> = restClient.getUserCart(userLogin?.token, ConfigUtil.getLocaleISO639(), countryID)
            call.enqueue(object : Callback<UserCart?> {
                override fun onResponse(call: Call<UserCart?>, response: Response<UserCart?>) {
                    val userCart: UserCart? = response.body()
                    if (userCart != null) {
                        if(userCart.blstatus == ServerConstants.AUTH_TOKEN_EXPIRED || userCart.blstatus == ServerConstants.AUTH_TOKEN_ERROR){
                            NetworkToken.refresh(applicationContext)
                            return
                        }
                        if (userCart.blstatus == ServerConstants.NO_ERROR) {
                            if (userCart.products != null){
                                realm.executeTransaction {
                                    userLogin?.qtyCartProducts = userCart.products
                                }
                                tvCartQty.text = userCart.products.toString()
                            }else{
                                Toast.makeText(applicationContext, getString(R.string.server_error), Toast.LENGTH_LONG).show()
                            }
                        }else{
                            Toast.makeText(applicationContext, userCart.blmessage, Toast.LENGTH_LONG).show()
                        }
                    }else{
                        Toast.makeText(applicationContext, getString(R.string.server_error), Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<UserCart?>, t: Throwable) {
                    Toast.makeText(applicationContext, getString(R.string.server_error), Toast.LENGTH_LONG).show()
                }
            })

        }
    }



    override fun onNegativeClick(from: Int) {
    }

    override fun onNeutralClick(from: Int) {
    }
}