package com.buscalibre.app2.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import com.buscalibre.app2.R
import com.buscalibre.app2.constants.AppConstants
import com.buscalibre.app2.constants.Preferences
import com.buscalibre.app2.constants.ServerConstants
import com.buscalibre.app2.models.UserLogin
import com.buscalibre.app2.models.UserRegister
import com.buscalibre.app2.network.NetworkUtil
import com.buscalibre.app2.network.RestClient
import com.buscalibre.app2.network.RetrofitClientInstance
import com.buscalibre.app2.util.AlertDialogHelper
import com.buscalibre.app2.util.SecurityUtils
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.activity_register_user.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterUserActivity : BaseActivity(), AlertDialogHelper.AlertDialogListener {

    private var email:String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_user)
        getDataFromIntent()
        initViews()
    }

    private fun getDataFromIntent(){
        if (intent.extras != null){
            email = intent.getStringExtra("email")!!
            tvEmail.text = email
        }
    }

    private fun validateFields(): Boolean {
        var isValid = true

        if (etPassword.text.toString().isEmpty()) {
            etPassword.error = "Ingrese contraseña"
            isValid = false
        }


        if (etRepeatPassword.text.toString().isEmpty()) {
            etRepeatPassword.error = "Ingrese contraseña"
            isValid = false
        } else {
            val emailValidate: String =
                etRepeatPassword.text.toString().trim { it <= ' ' }
            etRepeatPassword.setText(emailValidate)

            if (etPassword.text.toString() != etRepeatPassword.text.toString()) {
                etRepeatPassword.error = "La contraseña no coincide."
                etPassword.error = "La contraseña no coincide."
                isValid = false
            }
        }

        if (etUserFirstName.text.toString().isEmpty()) {
            etUserFirstName.error = "Ingrese nombre"
            isValid = false
        }
        if (etUserLastName.text.toString().isEmpty()) {
            etUserLastName.bringToFront()
            etUserLastName.error = "Ingrese apellido"
            isValid = false
        }
        return isValid
    }

    private fun initViews(){
        hideToolbar()
        //alertDialogHelper = AlertDialogHelper(this@RegisterUserActivity)
        tvModify.setOnClickListener {
            finish()
        }
    }

    fun registerUserOnClick(view: View) {
        if (validateFields()){
            registerUser(email, etPassword.text.toString(), etUserFirstName.text.toString(), etUserLastName.text.toString())
        }
    }

    private fun registerUser(email:String, password:String, name:String, lastName:String){
        if (NetworkUtil.checkEnabledInternet(applicationContext)) {
            if (validateFields()) {
                showProgress()
                val restClient: RestClient = RetrofitClientInstance.getRetrofitInstance().create(RestClient::class.java)
                val json = JsonObject()
                json.addProperty("email", email.trim())
                json.addProperty("password", password.trim())
                json.addProperty("key", SecurityUtils.md5(AppConstants.MD5_PRE_POST_TEXT + password + AppConstants.MD5_PRE_POST_TEXT))
                json.addProperty("firstname", name.trim())
                json.addProperty("lastname", lastName.trim())
                json.addProperty("fblogin", AppConstants.BL_LOGIN)
                json.addProperty("platform", AppConstants.ANDROID_PLATFORM_SERVERID.toInt())
                json.addProperty("firebasetoken", Preferences.getFCMToken(applicationContext))
                Log.e("registerJson", json.toString())
                val userRegisterCall: Call<UserRegister> = restClient.registerUser(json)
                userRegisterCall.enqueue(object : Callback<UserRegister?> {
                    override fun onResponse(call: Call<UserRegister?>, response: Response<UserRegister?>) {
                        val userRegister: UserRegister? = response.body()
                        if (userRegister != null) {
                            if (userRegister.blstatus == ServerConstants.NO_ERROR) {
                                UserLogin.deleteAll(realm)
                                realm.executeTransaction {
                                    val userLogin = realm.createObject(UserLogin::class.java)
                                    userLogin.token = userRegister.token
                                    userLogin.webToken = userRegister.webToken
                                    userLogin.email = email
                                    userLogin.blmessage = userRegister.blmessage
                                    userLogin.blstatus = userRegister.blstatus
                                    userLogin.ebookToken = userRegister.ebookToken
                                }
                                val intent = Intent(applicationContext, SelectCountryActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(intent)
                                finish()
                            } else {
                                alertDialogHelper.showAlertDialog(getString(R.string.app_name),userRegister.blmessage,getString(R.string.accept_dialog),"","",-1, false)

                            }
                        } else {
                            alertDialogHelper.showAlertDialog(getString(R.string.app_name),getString(R.string.server_error),getString(R.string.accept_dialog),"","",-1, false)
                        }
                        hideProgress()
                    }

                    override fun onFailure(call: Call<UserRegister?>, t: Throwable) {
                        hideProgress()
                        alertDialogHelper.showAlertDialog(getString(R.string.app_name),getString(R.string.server_error),getString(R.string.accept_dialog),"","",-1, false)

                    }
                })
            }
        } else {
            alertDialogHelper.showAlertDialog(getString(R.string.app_name),getString(R.string.connect_error),getString(R.string.accept_dialog),"","",-1, false)
        }
    }

    override fun onPositiveClick(from: Int) {
    }

    override fun onNegativeClick(from: Int) {
    }

    override fun onNeutralClick(from: Int) {
    }
}
