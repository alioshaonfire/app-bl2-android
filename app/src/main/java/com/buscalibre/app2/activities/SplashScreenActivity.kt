package com.buscalibre.app2.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.buscalibre.app2.R
import com.buscalibre.app2.constants.ServerConstants
import com.buscalibre.app2.events.CartProductsEvent
import com.buscalibre.app2.models.Country
import com.buscalibre.app2.models.UserCart
import com.buscalibre.app2.models.UserLogin
import com.buscalibre.app2.network.NetworkToken
import com.buscalibre.app2.network.NetworkUtil
import com.buscalibre.app2.network.RestClient
import com.buscalibre.app2.network.RetrofitClientInstance
import com.buscalibre.app2.util.ConfigUtil
import io.realm.Realm
import org.greenrobot.eventbus.EventBus
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SplashScreenActivity : AppCompatActivity() {

    private val realm:Realm = Realm.getDefaultInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        setContentView(R.layout.activity_splash_screen)
        getUserCart()
        initSplash()

    }


    private fun initSplash(){
        val myThread: Thread = object : Thread() {
            override fun run() {
                try {
                    sleep(800)
                    val userLogin = Realm.getDefaultInstance().where(UserLogin::class.java).findFirst()
                    if (userLogin == null){
                        startActivity(Intent(applicationContext, WelcomeUserActivity::class.java))
                    }else{
                        val country = Realm.getDefaultInstance().where(Country::class.java)
                            .equalTo("isSelected", true)
                            .findFirst()

                        if(country == null){
                            startActivity(Intent(applicationContext, SelectCountryActivity::class.java))
                        }else{
                            startActivity(Intent(applicationContext, HomeActivity::class.java))
                        }
                    }
                    finish()
                } catch (e: InterruptedException) {
                    Log.e("errorSplash", e.toString())
                }
            }
        }
        myThread.start()
    }

    private fun getUserCart(){
        val country = realm.where(Country::class.java).equalTo("isSelected", true).findFirst()
        val userLogin = realm.where(UserLogin::class.java).findFirst()
        if (NetworkUtil.checkEnabledInternet(applicationContext) && userLogin != null && country != null) {
            val restClient = RetrofitClientInstance.getRetrofitInstance().create(RestClient::class.java)
            val call: Call<UserCart> = restClient.getUserCart(userLogin.token, ConfigUtil.getLocaleISO639(), country.id)
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
                                    userLogin.qtyCartProducts = userCart.products
                                }
                            }else{
                                realm.executeTransaction {
                                    userLogin.qtyCartProducts = 0
                                }
                                //Toast.makeText(applicationContext, getString(R.string.server_error), Toast.LENGTH_LONG).show()
                            }
                        }else{
                            realm.executeTransaction {
                                userLogin.qtyCartProducts = 0
                            }
                            //Toast.makeText(applicationContext, userCart.blmessage, Toast.LENGTH_LONG).show()
                        }
                    }else{
                        realm.executeTransaction {
                            userLogin.qtyCartProducts = 0
                        }
                        //Toast.makeText(applicationContext, getString(R.string.server_error), Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<UserCart?>, t: Throwable) {
                    realm.executeTransaction {
                        userLogin.qtyCartProducts = 0
                    }
                }
            })

        }
    }


}
