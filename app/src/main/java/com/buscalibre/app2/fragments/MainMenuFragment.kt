package com.buscalibre.app2.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.buscalibre.app2.R
import com.buscalibre.app2.adapters.MenuOptionAdapter
import com.buscalibre.app2.constants.ServerConstants
import com.buscalibre.app2.models.*
import com.buscalibre.app2.network.NetworkToken
import com.buscalibre.app2.network.NetworkUtil
import com.buscalibre.app2.network.RestClient
import com.buscalibre.app2.network.RetrofitClientInstance
import com.buscalibre.app2.util.AlertDialogHelper
import com.buscalibre.app2.util.ConfigUtil
import io.realm.Realm
import io.realm.RealmList
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainMenuFragment : Fragment(), AlertDialogHelper.AlertDialogListener {

    private val realm: Realm = Realm.getDefaultInstance()
    private lateinit var userLogin:UserLogin
    private lateinit var alertDialogHelper:AlertDialogHelper
    private lateinit var country:Country
    private lateinit var srlMainMenu:SwipeRefreshLayout
    private lateinit var rvMainMenu:RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val mainView = inflater.inflate(R.layout.fragment_main_menu, container, false)
        srlMainMenu = mainView.findViewById(R.id.srlMainMenu)
        rvMainMenu = mainView.findViewById(R.id.rvMainMenu)
        userLogin = realm.where(UserLogin::class.java).findFirst()!!
        alertDialogHelper = AlertDialogHelper(activity)
        country = realm.where(Country::class.java).equalTo("isSelected", true).findFirst()!!
        return mainView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        verifyConnectState()
        srlMainMenu.setOnRefreshListener {
            verifyConnectState()
        }
    }

    private fun initOnlineMode(){
        getMenuOptionsFromServer(country.id.toString())
        getSystemConfig()
    }

    private fun verifyConnectState(){

        if(NetworkUtil.checkEnabledInternet(activity)){
            if (userLogin.isOfflineMode){
                realm.executeTransaction {
                    userLogin.isOfflineMode = false
                }
                Toast.makeText(activity,getString(R.string.text75), Toast.LENGTH_LONG).show()
                initOnlineMode()
                //alertDialogHelper.showAlertDialog(getString(R.string.app_name),getString(R.string.text75),getString(R.string.accept_dialog),"","",2, false)
            }else{
                initOnlineMode()
            }
        }else{
            if (userLogin.isOfflineMode){
                initOfflineMode()
            }else{
                val option = realm.where(Option::class.java).equalTo("type", ServerConstants.MAIN_MENU_TYPE_EBOOK_READER).findFirst()
                if (option == null){
                    Toast.makeText(activity, getString(R.string.connect_error), Toast.LENGTH_LONG).show()
                    if (srlMainMenu.isRefreshing) {
                        srlMainMenu.isRefreshing = false
                    }
                    return
                }
                val localEbookResult = realm.where(LocalEbook::class.java).findAll()
                if (localEbookResult.size == 0){
                    Toast.makeText(activity, getString(R.string.connect_error), Toast.LENGTH_LONG).show()
                    if (srlMainMenu.isRefreshing) {
                        srlMainMenu.isRefreshing = false
                    }
                    return
                }
                alertDialogHelper.showAlertDialog(getString(R.string.app_name),getString(R.string.text73),getString(R.string.accept_dialog),getString(R.string.cancel_dialog),"",1, false)
            }
        }
    }

    private fun initOfflineMode(){
        realm.executeTransaction {
            userLogin.isOfflineMode = true
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
        if (NetworkUtil.checkEnabledInternet(activity)) {

            val restClient = RetrofitClientInstance.getRetrofitInstance().create(RestClient::class.java)
            val call: Call<ServerMenuOptions> = restClient.getServerMenuOptions(userLogin.token, ConfigUtil.getLocaleISO639(), countryID)
            call.enqueue(object : Callback<ServerMenuOptions?> {
                override fun onResponse(call: Call<ServerMenuOptions?>, response: Response<ServerMenuOptions?>) {
                    val serverMenuOptions: ServerMenuOptions? = response.body()
                    if (serverMenuOptions != null) {
                        if(serverMenuOptions.blstatus == ServerConstants.AUTH_TOKEN_EXPIRED || serverMenuOptions.blstatus == ServerConstants.AUTH_TOKEN_ERROR){
                            NetworkToken.refresh(activity)
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
                            Toast.makeText(activity, serverMenuOptions.blmessage, Toast.LENGTH_LONG).show()
                        }
                    }else{
                        Toast.makeText(activity, getString(R.string.server_error), Toast.LENGTH_LONG).show()
                    }
                    //hideProgress()
                    if (srlMainMenu.isRefreshing) {
                        srlMainMenu.isRefreshing = false
                    }
                }

                override fun onFailure(call: Call<ServerMenuOptions?>, t: Throwable) {
                    if (srlMainMenu.isRefreshing) {
                        srlMainMenu.isRefreshing = false
                    }
                    //hideProgress()
                    Toast.makeText(activity, getString(R.string.server_error), Toast.LENGTH_LONG).show()
                }
            })

        }else{
            if (srlMainMenu.isRefreshing) {
                srlMainMenu.isRefreshing = false
            }
            Toast.makeText(activity, getString(R.string.connect_error), Toast.LENGTH_LONG).show()
        }
    }


    private fun getSystemConfig(){

        if (NetworkUtil.checkEnabledInternet(activity)) {

            val restClient = RetrofitClientInstance.getRetrofitInstance().create(RestClient::class.java)
            val call: Call<SystemConfig> = restClient.getSystemConfig(userLogin?.token, ConfigUtil.getLocaleISO639())
            call.enqueue(object : Callback<SystemConfig?> {
                override fun onResponse(call: Call<SystemConfig?>, response: Response<SystemConfig?>) {
                    val systemConfig: SystemConfig? = response.body()
                    if (systemConfig != null) {
                        if(systemConfig.blstatus == ServerConstants.AUTH_TOKEN_EXPIRED || systemConfig.blstatus == ServerConstants.AUTH_TOKEN_ERROR){
                            NetworkToken.refresh(activity)
                            return
                        }
                        if (systemConfig.blstatus == ServerConstants.NO_ERROR) {
                            SystemConfig.deleteAll(realm)
                            realm.executeTransaction {
                                realm.copyToRealm(systemConfig)
                            }
                        }else{
                            Toast.makeText(activity, systemConfig.blmessage, Toast.LENGTH_LONG).show()
                        }
                    }else{
                        Toast.makeText(activity, getString(R.string.server_error), Toast.LENGTH_LONG).show()
                        //alertDialogHelper.showAlertDialog(getString(R.string.app_name),getString(R.string.server_error),getString(R.string.accept_dialog),"","",-1, false)
                    }
                }

                override fun onFailure(call: Call<SystemConfig?>, t: Throwable) {
                    Toast.makeText(activity, getString(R.string.server_error), Toast.LENGTH_LONG).show()
                    //alertDialogHelper.showAlertDialog(getString(R.string.app_name),getString(R.string.server_error),getString(R.string.accept_dialog),"","",-1, false)
                }
            })

        }
    }

    private fun showMenuOptions(optionList: RealmList<Option>) {

        val recyclerViewLayoutManager: RecyclerView.LayoutManager = GridLayoutManager(activity, 2)
        rvMainMenu.layoutManager = recyclerViewLayoutManager
        rvMainMenu.adapter = MenuOptionAdapter(optionList, activity!!)
    }

    override fun onPositiveClick(from: Int) {
    }

    override fun onNegativeClick(from: Int) {
    }

    override fun onNeutralClick(from: Int) {
    }

}