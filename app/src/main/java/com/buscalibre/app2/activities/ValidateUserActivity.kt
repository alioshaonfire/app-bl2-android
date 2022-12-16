package com.buscalibre.app2.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.buscalibre.app2.R
import com.buscalibre.app2.constants.AppConstants
import com.buscalibre.app2.constants.Preferences
import com.buscalibre.app2.constants.ServerConstants
import com.buscalibre.app2.models.*
import com.buscalibre.app2.network.NetworkToken
import com.buscalibre.app2.network.NetworkUtil
import com.buscalibre.app2.network.RestClient
import com.buscalibre.app2.network.RetrofitClientInstance
import com.buscalibre.app2.util.AlertDialogHelper
import com.buscalibre.app2.util.ConfigUtil
import com.buscalibre.app2.util.SecurityUtils
import com.buscalibre.app2.util.ViewUtil
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.activity_validate_user.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private var email = ""

class ValidateUserActivity : BaseActivity(), AlertDialogHelper.AlertDialogListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_validate_user)
        getDataFromIntent()
        initViews()
    }

    private fun getDataFromIntent(){
        if (intent.extras != null){
            email = intent.getStringExtra("email")!!
            tvAnswerMail.text = email
        }
    }

    private fun initViews(){
        hideToolbar()
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        //alertDialogHelper = AlertDialogHelper(this@ValidateUserActivity)
        btConfirmPassword.setOnClickListener {
            if (validateFields()){
                initSession(email, etPassword.text.toString())
            }
        }
        etPassword.requestFocus()
        ViewUtil.openKeyboard(this@ValidateUserActivity)
        tvForgotPass.setOnClickListener {
            forgotPass(email)
        }
    }

    private fun forgotPass(email:String){
        if (NetworkUtil.checkEnabledInternet(applicationContext)) {
            showProgress()
            val json = JsonObject()
            json.addProperty("email", email.trim())
            Log.e("forgotPassJson", json.toString())
            val restClient = RetrofitClientInstance.getRetrofitInstance().create(RestClient::class.java)
            val call: Call<ForgotPass> = restClient.forgotPass(json)
            call.enqueue(object : Callback<ForgotPass?> {
                override fun onResponse(call: Call<ForgotPass?>, response: Response<ForgotPass?>) {
                    val forgotPass: ForgotPass? = response.body()
                    if (forgotPass != null) {
                        if (forgotPass.blstatus == ServerConstants.NO_ERROR) {
                            alertDialogHelper.showAlertDialog(getString(R.string.app_name),getString(R.string.text23) + " " + email + " " + getString(R.string.text24),getString(R.string.accept_dialog),"","",-1, false)
                        }else{
                            alertDialogHelper.showAlertDialog(getString(R.string.app_name),forgotPass.blmessage,getString(R.string.accept_dialog),"","",-1, false)
                        }
                    }else{
                        alertDialogHelper.showAlertDialog(getString(R.string.app_name),getString(R.string.server_error),getString(R.string.accept_dialog),"","",-1, false)
                    }
                    hideProgress()
                }

                override fun onFailure(call: Call<ForgotPass?>, t: Throwable) {
                    hideProgress()
                    alertDialogHelper.showAlertDialog(getString(R.string.app_name),getString(R.string.server_error),getString(R.string.accept_dialog),"","",-1, false)
                }
            })

        }else{
            alertDialogHelper.showAlertDialog(getString(R.string.app_name),getString(R.string.connect_error),getString(R.string.accept_dialog),"","",-1, false)

        }
    }

    private fun initSession(email:String, password:String){

        if (NetworkUtil.checkEnabledInternet(applicationContext)) {
            showProgress()
            val json = JsonObject()
            json.addProperty("email", email.trim())
            json.addProperty("password", password.trim())
            json.addProperty("firebasetoken", Preferences.getFCMToken(applicationContext))
            json.addProperty("platform", AppConstants.ANDROID_PLATFORM_SERVERID.toInt())
            json.addProperty("key", SecurityUtils.md5(AppConstants.MD5_PRE_POST_TEXT + password + AppConstants.MD5_PRE_POST_TEXT))
            Log.e("checkEmailJson", json.toString())
            val restClient = RetrofitClientInstance.getRetrofitInstance().create(RestClient::class.java)
            val call: Call<UserLogin> = restClient.initSession(json)
            call.enqueue(object : Callback<UserLogin?> {
                override fun onResponse(call: Call<UserLogin?>, response: Response<UserLogin?>) {
                    val login: UserLogin? = response.body()
                    if (login != null) {
                        if (login.blstatus == ServerConstants.NO_ERROR) {
                            UserLogin.deleteAll(realm)
                            realm.executeTransaction {
                                login.email = email
                                realm.copyToRealm(login)
                            }
                            ViewUtil.hideKeyboard(this@ValidateUserActivity)
                            Log.e("token", login.token)
                            val userCountry = realm.where(UserCountry::class.java).equalTo("userEmail", email).findFirst()
                            if (userCountry!= null){

                                val countries = realm.where(Country::class.java).findAll()
                                for (country:Country in countries){
                                    if (country.id == userCountry.countryID){
                                        realm.executeTransaction {
                                            country.isSelected = true
                                        }
                                    }else{
                                        realm.executeTransaction {
                                            country.isSelected = false
                                        }
                                    }
                                }
                                getUserCart(userCountry.countryID)
                                val intent = Intent(applicationContext, HomeActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(intent)
                                finish()
                            }else{
                                val intent = Intent(applicationContext, SelectCountryActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(intent)
                                finish()
                            }



                        }else{
                            Toast.makeText(applicationContext, login.blmessage, Toast.LENGTH_LONG).show()
                        }
                    }else{
                        alertDialogHelper.showAlertDialog(getString(R.string.app_name),getString(R.string.server_error),getString(R.string.accept_dialog),"","",-1, false)
                    }
                    hideProgress()
                }

                override fun onFailure(call: Call<UserLogin?>, t: Throwable) {
                    hideProgress()
                    alertDialogHelper.showAlertDialog(getString(R.string.app_name),getString(R.string.server_error),getString(R.string.accept_dialog),"","",-1, false)
                }
            })

        }else{
            alertDialogHelper.showAlertDialog(getString(R.string.app_name),getString(R.string.connect_error),getString(R.string.accept_dialog),"","",-1, false)

        }
    }


    private fun validateFields():Boolean{
        var isValid = true
        if (etPassword.text.toString().isEmpty()) {
            etPassword.error = getString(R.string.text13)
            isValid = false
            alertDialogHelper.showAlertDialog(getString(R.string.app_name),getString(R.string.text13),getString(R.string.accept_dialog),"","",-1, false)
        }
        return isValid
    }

    override fun onPositiveClick(from: Int) {
    }

    override fun onNegativeClick(from: Int) {
    }

    override fun onNeutralClick(from: Int) {
    }
}