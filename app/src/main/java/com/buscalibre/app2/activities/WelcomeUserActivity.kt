package com.buscalibre.app2.activities

import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import com.buscalibre.app2.R
import com.buscalibre.app2.constants.AppConstants
import com.buscalibre.app2.constants.Preferences
import com.buscalibre.app2.constants.ServerConstants
import com.buscalibre.app2.models.*
import com.buscalibre.app2.network.NetworkUtil
import com.buscalibre.app2.network.RestClient
import com.buscalibre.app2.network.RetrofitClientInstance
import com.buscalibre.app2.util.AlertDialogHelper
import com.buscalibre.app2.util.SecurityUtils
import com.buscalibre.app2.util.ValidateUtil
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.activity_welcome_user.*
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


class WelcomeUserActivity : BaseActivity(), AlertDialogHelper.AlertDialogListener {

    private var callbackManager: CallbackManager? = null
    private var email = ""
    private var fbToken = ""
    private var firstName = ""
    private var lastName = ""
    private var googleID = ""
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 100


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome_user)
        //keyHash()
        initViews()
    }


    private fun validateFields(): Boolean {
        var isValid = true
        if (etEmail.text.toString().isEmpty()) {
            etEmail.error = getString(R.string.text13)
            isValid = false
            alertDialogHelper.showAlertDialog(getString(R.string.app_name),getString(R.string.text13),getString(R.string.accept_dialog),"","",-1, false)

        } else {
            val emailValidate = etEmail.text.toString().replace(" ".toRegex(), "")
            etEmail.setText(emailValidate)
            if (!ValidateUtil.isValidEmail(etEmail.text.toString())) {
                etEmail.error = getString(R.string.text15)
                isValid = false
                alertDialogHelper.showAlertDialog(getString(R.string.app_name),getString(R.string.text15),getString(R.string.accept_dialog),"","",-1, false)

            }
        }
        return isValid
    }

    private fun setTypefaces(){

    }

    private fun keyHash(){
        val info: PackageInfo
        try {
            info = packageManager.getPackageInfo("com.buscalibre.app2", PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md: MessageDigest = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val something = String(Base64.encode(md.digest(), 0))
                //String something = new String(Base64.encodeBytes(md.digest()));
                Log.e("hash key", something)
            }
        } catch (e1: PackageManager.NameNotFoundException) {
            Log.e("name not found", e1.toString())
        } catch (e: NoSuchAlgorithmException) {
            Log.e("no such an algorithm", e.toString())
        } catch (e: Exception) {
            Log.e("exception", e.toString())
        }
    }



    private fun initViews(){
        hideToolbar()
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        //alertDialogHelper = AlertDialogHelper(this@WelcomeUserActivity)
        btLogin.setOnClickListener {
            if (validateFields()){
                checkEmail(etEmail.text.toString())
            }
        }
        callbackManager = CallbackManager.Factory.create()
        btLoginFacebook.setReadPermissions("email")

        btLoginFacebook.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                val request = GraphRequest.newMeRequest(
                    loginResult.accessToken) { _, response ->
                    try {
                        //val token = AccessToken.getCurrentAccessToken()

                        fbToken = loginResult.accessToken.token.toString()

                        firstName = if (response?.jsonObject?.getString("first_name") != null) {
                            response.jsonObject!!.getString("first_name")
                        } else {
                            ""
                        }
                        lastName = if (response?.jsonObject?.getString("last_name") != null) {
                            response.jsonObject!!.getString("last_name")
                        } else {
                            ""
                        }

                        email = if (response?.jsonObject?.getString("email") != null) {
                            response.jsonObject!!.getString("email")
                        } else {
                            ""
                        }
                        loginWithRRSS(true)
                        //loginWithFBAccount()
                    } catch (e: JSONException) {
                        Log.e("errorFb", e.toString())
                    }
                }
                val parameters = Bundle()
                parameters.putString("fields", "id,name,birthday,gender,email,first_name,last_name,picture.width(150).height(150)")
                request.parameters = parameters
                request.executeAsync()
            }

            override fun onCancel() {
                Toast.makeText(this@WelcomeUserActivity, "Login cancel", Toast.LENGTH_SHORT).show()
            }

            override fun onError(exception: FacebookException) {
                Log.e("errorLoginFB", exception.toString())
                Toast.makeText(this@WelcomeUserActivity, "Error login", Toast.LENGTH_SHORT).show()
            }
        })
        btFb.setOnClickListener {
            btLoginFacebook.performClick()
        }
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        btGoogleAuth.setOnClickListener {
            signIn()
        }
    }

    private fun signIn() {
        val signInIntent = mGoogleSignInClient.signInIntent

        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun loginWithRRSS(isFacebook:Boolean){
        if (NetworkUtil.checkEnabledInternet(applicationContext)) {
            showProgress()
            val json = JsonObject()
            json.addProperty("email", email)
            json.addProperty("password", "")
            json.addProperty("key", SecurityUtils.md5(AppConstants.MD5_PRE_POST_TEXT + "" + AppConstants.MD5_PRE_POST_TEXT))
            if(isFacebook){
                json.addProperty("fbtoken", fbToken)
                json.addProperty("fblogin", AppConstants.FB_LOGIN)
            }else{
                json.addProperty("fbtoken", googleID)
                json.addProperty("fblogin", AppConstants.GOOGLE_LOGIN)
            }

            json.addProperty("firebasetoken", Preferences.getFCMToken(applicationContext))
            json.addProperty("platform", AppConstants.ANDROID_PLATFORM_SERVERID)
            json.addProperty("firstname", firstName)
            json.addProperty("lastname", lastName)
            Log.e("registerRRSSJson", json.toString())
            val restClient: RestClient = RetrofitClientInstance.getRetrofitInstance().create(RestClient::class.java)
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
                            }
                            val userCountry = realm.where(UserCountry::class.java).equalTo("userEmail", email).findFirst()
                            if (userCountry!= null){

                                val countries = realm.where(Country::class.java).findAll()
                                for (country: Country in countries){
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
        } else {
            alertDialogHelper.showAlertDialog(getString(R.string.app_name),getString(R.string.connect_error),getString(R.string.accept_dialog),"","",-1, false)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager!!.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task: Task<GoogleSignInAccount> =
                GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)

            Log.e("idToken", account.idToken.toString())
            Log.e("googleEmail", account.email.toString())
            Log.e("googleFamilyName", account.familyName.toString())
            Log.e("googleGivenName", account.givenName.toString())

            email = if (account.email != null) account.email.toString() else ""
            googleID = if (account.idToken != null) account.idToken.toString() else ""
            firstName = if (account.givenName != null) account.givenName.toString() else ""
            lastName = if (account.familyName != null) account.familyName.toString() else ""
            loginWithRRSS(false)
            // Signed in successfully, show authenticated UI.
            Log.w("TAG", "Success")
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("TAG", "signInResult:failed code=$e")
        }
    }

    private fun checkEmail(email:String){
        if (NetworkUtil.checkEnabledInternet(applicationContext)) {
            showProgress()
            val json = JsonObject()
            json.addProperty("email", email)

            Log.e("checkEmailJson", json.toString())
            val restClient = RetrofitClientInstance.getRetrofitInstance().create(RestClient::class.java)
            val call: Call<ChechEmail> = restClient.checkEmail(json)
            call.enqueue(object : Callback<ChechEmail?> {
                override fun onResponse(call: Call<ChechEmail?>, response: Response<ChechEmail?>) {
                    val chechEmail: ChechEmail? = response.body()
                    if (chechEmail != null) {

                        if (chechEmail.blstatus == ServerConstants.NO_ERROR) {
                            val intent = Intent(this@WelcomeUserActivity, ValidateUserActivity::class.java)
                            intent.putExtra("email",email)
                            startActivity(intent)

                        }else if(chechEmail.blstatus == ServerConstants.USER_NOT_FOUND){
                            val intent = Intent(this@WelcomeUserActivity, RegisterUserActivity::class.java)
                            intent.putExtra("email",email)
                            startActivity(intent)
                        }else{
                            Toast.makeText(applicationContext, chechEmail.blmessage, Toast.LENGTH_LONG).show()
                        }
                    }else{
                        alertDialogHelper.showAlertDialog(getString(R.string.app_name),getString(R.string.server_error),getString(R.string.accept_dialog),"","",-1, false)

                    }
                    hideProgress()
                }

                override fun onFailure(call: Call<ChechEmail?>, t: Throwable) {
                    hideProgress()
                    alertDialogHelper.showAlertDialog(getString(R.string.app_name),getString(R.string.server_error),getString(R.string.accept_dialog),"","",-1, false)
                }
            })

        }else{
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
