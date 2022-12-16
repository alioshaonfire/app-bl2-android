package com.buscalibre.app2.activities

import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.buscalibre.app2.R
import com.buscalibre.app2.adapters.MenuOptionAdapter
import com.buscalibre.app2.calls.POSTFirebaseID
import com.buscalibre.app2.constants.ServerConstants
import com.buscalibre.app2.models.*
import com.buscalibre.app2.network.NetworkToken
import com.buscalibre.app2.network.NetworkUtil
import com.buscalibre.app2.network.RestClient
import com.buscalibre.app2.network.RetrofitClientInstance
import com.buscalibre.app2.util.AlertDialogHelper
import com.buscalibre.app2.util.CheckStatus
import com.buscalibre.app2.util.ConfigUtil
import io.realm.RealmList
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.activity_main_menu.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainMenuActivity : BaseActivity(), AlertDialogHelper.AlertDialogListener {

    private var countryID:String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)
        initViews()
        getInboxMessages(false)
        POSTFirebaseID.refreshTokenFirebase(applicationContext)
        CheckStatus.userLogin(realm, this@MainMenuActivity)
        verifyConnectState()
    }

    private fun verifyConnectState(){

        if(NetworkUtil.checkEnabledInternet(this)){
            if (userLogin!!.isOfflineMode){
                realm.executeTransaction {
                    userLogin!!.isOfflineMode = false
                }
                Toast.makeText(this,getString(R.string.text75), Toast.LENGTH_LONG).show()
                initOnlineMode()
                //alertDialogHelper.showAlertDialog(getString(R.string.app_name),getString(R.string.text75),getString(R.string.accept_dialog),"","",2, false)
            }else{
               initOnlineMode()
            }
        }else{
            if (userLogin!!.isOfflineMode){
                initOfflineMode()
            }else{
                val option = realm.where(Option::class.java).equalTo("type", ServerConstants.MAIN_MENU_TYPE_EBOOK_READER).findFirst()
                if (option == null){
                    Toast.makeText(applicationContext, getString(R.string.connect_error), Toast.LENGTH_LONG).show()
                    if (srlMainMenu.isRefreshing) {
                        srlMainMenu.isRefreshing = false
                    }
                    return
                }
                val localEbookResult = realm.where(LocalEbook::class.java).findAll()
                if (localEbookResult.size == 0){
                    Toast.makeText(applicationContext, getString(R.string.connect_error), Toast.LENGTH_LONG).show()
                    if (srlMainMenu.isRefreshing) {
                        srlMainMenu.isRefreshing = false
                    }
                    return
                }
                alertDialogHelper.showAlertDialog(getString(R.string.app_name),getString(R.string.text73),getString(R.string.accept_dialog),getString(R.string.cancel_dialog),"",1, false)
            }
        }
    }

    private fun initOnlineMode(){
        if (country?.id != null){
            getMenuOptionsFromServer(country?.id.toString())
        }else{
            NetworkToken.refresh(applicationContext)
        }
        getSystemConfig()
    }

    private fun initViews(){
        tvToolbarTitle.text = getString(R.string.text5)
        alertDialogHelper = AlertDialogHelper(this)
        srlMainMenu.setOnRefreshListener {
            verifyConnectState()
        }
    }

    private fun initOfflineMode(){
        realm.executeTransaction {
            userLogin!!.isOfflineMode = true
        }
        val option = realm.where(Option::class.java).equalTo("type", ServerConstants.MAIN_MENU_TYPE_EBOOK_READER).findFirst()
        val optionSingleList = RealmList<Option>()
        optionSingleList.add(option)
        showMenuOptions(optionSingleList)
        if (srlMainMenu.isRefreshing) {
            srlMainMenu.isRefreshing = false
        }
    }

    private fun getMenuOptionsFromServer(countryID:String){
        if (NetworkUtil.checkEnabledInternet(applicationContext)) {
            showProgress()
            val restClient = RetrofitClientInstance.getRetrofitInstance().create(RestClient::class.java)
            val call: Call<ServerMenuOptions> = restClient.getServerMenuOptions(userLogin?.token, ConfigUtil.getLocaleISO639(), countryID)
            call.enqueue(object : Callback<ServerMenuOptions?> {
                override fun onResponse(call: Call<ServerMenuOptions?>, response: Response<ServerMenuOptions?>) {
                    val serverMenuOptions: ServerMenuOptions? = response.body()
                    if (serverMenuOptions != null) {
                        if(serverMenuOptions.blstatus == ServerConstants.AUTH_TOKEN_EXPIRED || serverMenuOptions.blstatus == ServerConstants.AUTH_TOKEN_ERROR){
                            NetworkToken.refresh(applicationContext)
                            return
                        }
                        if (serverMenuOptions.blstatus == ServerConstants.NO_ERROR) {
                            if (serverMenuOptions.options != null){
                                ServerMenuOptions.deleteAll(realm)
                                realm.executeTransaction {
                                    realm.copyToRealm(serverMenuOptions)
                                }
                                showMenuOptions(serverMenuOptions.options)
                            }else{
                                alertDialogHelper.showAlertDialog(getString(R.string.app_name),getString(R.string.text6),getString(R.string.accept_dialog),"","",-1, false)
                            }
                        }else{
                            Toast.makeText(applicationContext, serverMenuOptions.blmessage, Toast.LENGTH_LONG).show()
                        }
                    }else{
                        Toast.makeText(applicationContext, getString(R.string.server_error), Toast.LENGTH_LONG).show()
                    }
                    hideProgress()
                    if (srlMainMenu.isRefreshing) {
                        srlMainMenu.isRefreshing = false
                    }
                }

                override fun onFailure(call: Call<ServerMenuOptions?>, t: Throwable) {
                    if (srlMainMenu.isRefreshing) {
                        srlMainMenu.isRefreshing = false
                    }
                    hideProgress()
                    Toast.makeText(applicationContext, getString(R.string.server_error), Toast.LENGTH_LONG).show()
                }
            })

        }else{
            if (srlMainMenu.isRefreshing) {
                srlMainMenu.isRefreshing = false
            }
            Toast.makeText(applicationContext, getString(R.string.connect_error), Toast.LENGTH_LONG).show()

        }
    }



    private fun getInboxMessages(isRefresh:Boolean){
        val messageNotRead = realm.where(MessageList::class.java).equalTo("read", false).findAll()
        //initDrawer(baseToolbar, messageNotRead?.size ?: 0, isRefresh)
    }

    private fun getSystemConfig(){

        if (NetworkUtil.checkEnabledInternet(applicationContext)) {

            val restClient = RetrofitClientInstance.getRetrofitInstance().create(RestClient::class.java)
            val call: Call<SystemConfig> = restClient.getSystemConfig(userLogin?.token, ConfigUtil.getLocaleISO639())
            call.enqueue(object : Callback<SystemConfig?> {
                override fun onResponse(call: Call<SystemConfig?>, response: Response<SystemConfig?>) {
                    val systemConfig: SystemConfig? = response.body()
                    if (systemConfig != null) {
                        if(systemConfig.blstatus == ServerConstants.AUTH_TOKEN_EXPIRED || systemConfig.blstatus == ServerConstants.AUTH_TOKEN_ERROR){
                            NetworkToken.refresh(applicationContext)
                            return
                        }
                        if (systemConfig.blstatus == ServerConstants.NO_ERROR) {
                            SystemConfig.deleteAll(realm)
                            realm.executeTransaction {
                                realm.copyToRealm(systemConfig)
                            }
                        }else{
                            Toast.makeText(applicationContext, systemConfig.blmessage, Toast.LENGTH_LONG).show()
                        }
                    }else{
                        Toast.makeText(applicationContext, getString(R.string.server_error), Toast.LENGTH_LONG).show()
                        //alertDialogHelper.showAlertDialog(getString(R.string.app_name),getString(R.string.server_error),getString(R.string.accept_dialog),"","",-1, false)
                    }
                }

                override fun onFailure(call: Call<SystemConfig?>, t: Throwable) {
                    Toast.makeText(applicationContext, getString(R.string.server_error), Toast.LENGTH_LONG).show()
                    //alertDialogHelper.showAlertDialog(getString(R.string.app_name),getString(R.string.server_error),getString(R.string.accept_dialog),"","",-1, false)
                }
            })

        }
    }

    override fun onResume() {
        super.onResume()
        alertDialogHelper = AlertDialogHelper(this)
        showCart()
        getInboxMessages(true)
    }

    override fun onPositiveClick(from: Int) {
        when(from){
            1 -> {
                if (srlMainMenu.isRefreshing) {
                    srlMainMenu.isRefreshing = false
                }
                initOfflineMode()
            }
            2 -> {
                if (srlMainMenu.isRefreshing) {
                    srlMainMenu.isRefreshing = false
                }
                initOnlineMode()
            }
        }
    }

    override fun onNegativeClick(from: Int) {

        when(from){
            1 -> {
                if (srlMainMenu.isRefreshing) {
                    srlMainMenu.isRefreshing = false
                }
                Toast.makeText(applicationContext, getString(R.string.connect_error), Toast.LENGTH_LONG).show()
            }
        }

    }

    override fun onNeutralClick(from: Int) {
    }

    private fun showMenuOptions(optionList: RealmList<Option>) {

        val recyclerViewLayoutManager: RecyclerView.LayoutManager = GridLayoutManager(this@MainMenuActivity, 2)
        rvMainMenu.layoutManager = recyclerViewLayoutManager
        rvMainMenu.adapter = MenuOptionAdapter(optionList, this@MainMenuActivity)
    }
}