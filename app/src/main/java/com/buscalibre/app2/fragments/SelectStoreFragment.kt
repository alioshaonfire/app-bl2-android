package com.buscalibre.app2.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.buscalibre.app2.R
import com.buscalibre.app2.adapters.StoreListAdapter

import com.buscalibre.app2.constants.AppConstants
import com.buscalibre.app2.constants.ServerConstants
import com.buscalibre.app2.events.SelectHomeMenuEvent
import com.buscalibre.app2.events.SelectNavMenuEvent
import com.buscalibre.app2.models.Country
import com.buscalibre.app2.models.ServerStores
import com.buscalibre.app2.models.Store
import com.buscalibre.app2.models.UserLogin
import com.buscalibre.app2.network.NetworkToken
import com.buscalibre.app2.network.NetworkUtil
import com.buscalibre.app2.network.RestClient
import com.buscalibre.app2.network.RetrofitClientInstance
import com.buscalibre.app2.util.ConfigUtil
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.activity_select_store.*
import kotlinx.android.synthetic.main.fragment_my_account.*
import org.greenrobot.eventbus.EventBus
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class SelectStoreFragment() : Fragment() {

    private val realm:Realm = Realm.getDefaultInstance()
    private lateinit var ivBack: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_select_store, container, false)
        val country = realm.where(Country::class.java).equalTo("isSelected", true).findFirst()
        val userLogin = realm.where(UserLogin::class.java).findFirst()
        //initViews()
        ivBack = view.findViewById(R.id.ivBack)
        ivBack.setOnClickListener {
            EventBus.getDefault().post(SelectHomeMenuEvent(true))
        }
        getStoreFromServer(country?.id.toString(), userLogin!!)
        return view
    }

    private fun initViews(){
        tvToolbarTitle.text = getString(R.string.text33)
        //alertDialogHelper = AlertDialogHelper(this@SelectStoreActivity)
        //showBackButton()
        //showCart()
    }

    private fun getStoreFromServer(countryID:String, userLogin:UserLogin){

        if (NetworkUtil.checkEnabledInternet(activity)) {
            //showProgress()
            val restClient = RetrofitClientInstance.getRetrofitInstance().create(RestClient::class.java)
            val call: Call<ServerStores> = restClient.getServerStores(userLogin.token, ConfigUtil.getLocaleISO639(), countryID)
            call.enqueue(object : Callback<ServerStores?> {
                override fun onResponse(call: Call<ServerStores?>, response: Response<ServerStores?>) {
                    val serverStores: ServerStores? = response.body()
                    if (serverStores != null) {
                        if(serverStores.blstatus == ServerConstants.AUTH_TOKEN_EXPIRED || serverStores.blstatus == ServerConstants.AUTH_TOKEN_ERROR){
                            NetworkToken.refresh(activity)
                            return
                        }
                        if (serverStores.blstatus == ServerConstants.NO_ERROR) {
                            if (serverStores.stores != null){
                                if (serverStores.stores.size <= 0){
                                    //alertDialogHelper.showAlertDialog(getString(R.string.app_name),getString(R.string.text34),getString(R.string.accept_dialog),"","",-1, false)

                                }else{
                                    showStoreList(serverStores.stores)
                                }
                            }
                        }else{
                            Toast.makeText(activity, serverStores.blmessage, Toast.LENGTH_LONG).show()
                        }
                    }else{
                        //alertDialogHelper.showAlertDialog(getString(R.string.app_name),getString(R.string.server_error),getString(R.string.accept_dialog),"","",-1, false)
                    }
                    //hideProgress()
                }

                override fun onFailure(call: Call<ServerStores?>, t: Throwable) {
                    //hideProgress()
                   // alertDialogHelper.showAlertDialog(getString(R.string.app_name),getString(R.string.server_error),getString(R.string.accept_dialog),"","",-1, false)
                }
            })

        }else{
            //alertDialogHelper.showAlertDialog(getString(R.string.app_name),getString(R.string.connect_error),getString(R.string.accept_dialog),"","",-1, false)

        }
    }

    private fun showStoreList(storeList: List<Store>) {
        val recyclerViewLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(context!!, LinearLayoutManager.VERTICAL, false)
        rvStoreList.layoutManager = recyclerViewLayoutManager
        rvStoreList.adapter = StoreListAdapter(storeList, context!!)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
}