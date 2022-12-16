package com.buscalibre.app2.activities

import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.buscalibre.app2.R
import com.buscalibre.app2.adapters.StoreListAdapter
import com.buscalibre.app2.constants.ServerConstants
import com.buscalibre.app2.fragments.BrowseFragment
import com.buscalibre.app2.models.ServerStores
import com.buscalibre.app2.models.Store
import com.buscalibre.app2.network.NetworkToken
import com.buscalibre.app2.network.NetworkUtil
import com.buscalibre.app2.network.RestClient
import com.buscalibre.app2.network.RetrofitClientInstance
import com.buscalibre.app2.util.AlertDialogHelper
import com.buscalibre.app2.util.ConfigUtil
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.activity_select_store.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

var mBrowseFragment: BrowseFragment? = null


class SelectStoreActivity : BaseActivity(), AlertDialogHelper.AlertDialogListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_store)
        mBrowseFragment = supportFragmentManager.findFragmentByTag(BrowseFragment.TAG) as BrowseFragment?
        initViews()
        getStoreFromServer(country?.id.toString())
    }

    private fun initViews(){
        tvToolbarTitle.text = getString(R.string.text33)
        //alertDialogHelper = AlertDialogHelper(this@SelectStoreActivity)
        showBackButton()
        showCart()
    }

    private fun getStoreFromServer(countryID:String){

        if (NetworkUtil.checkEnabledInternet(applicationContext)) {
            showProgress()
            val restClient = RetrofitClientInstance.getRetrofitInstance().create(RestClient::class.java)
            val call: Call<ServerStores> = restClient.getServerStores(userLogin?.token, ConfigUtil.getLocaleISO639(), countryID)
            call.enqueue(object : Callback<ServerStores?> {
                override fun onResponse(call: Call<ServerStores?>, response: Response<ServerStores?>) {
                    val serverStores: ServerStores? = response.body()
                    if (serverStores != null) {
                        if(serverStores.blstatus == ServerConstants.AUTH_TOKEN_EXPIRED || serverStores.blstatus == ServerConstants.AUTH_TOKEN_ERROR){
                            NetworkToken.refresh(applicationContext)
                            return
                        }
                        if (serverStores.blstatus == ServerConstants.NO_ERROR) {
                            if (serverStores.stores != null){
                                if (serverStores.stores.size <= 0){
                                    alertDialogHelper.showAlertDialog(getString(R.string.app_name),getString(R.string.text34),getString(R.string.accept_dialog),"","",-1, false)

                                }else{
                                    showStoreList(serverStores.stores)
                                }
                            }
                        }else{
                            Toast.makeText(applicationContext, serverStores.blmessage, Toast.LENGTH_LONG).show()
                        }
                    }else{
                        alertDialogHelper.showAlertDialog(getString(R.string.app_name),getString(R.string.server_error),getString(R.string.accept_dialog),"","",-1, false)
                    }
                    hideProgress()
                }

                override fun onFailure(call: Call<ServerStores?>, t: Throwable) {
                    hideProgress()
                    alertDialogHelper.showAlertDialog(getString(R.string.app_name),getString(R.string.server_error),getString(R.string.accept_dialog),"","",-1, false)
                }
            })

        }else{
            alertDialogHelper.showAlertDialog(getString(R.string.app_name),getString(R.string.connect_error),getString(R.string.accept_dialog),"","",-1, false)

        }
    }

    private fun showStoreList(storeList: List<Store>) {
        val recyclerViewLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this@SelectStoreActivity, LinearLayoutManager.VERTICAL, false)
        rvStoreList.layoutManager = recyclerViewLayoutManager
        rvStoreList.adapter = StoreListAdapter(storeList, this@SelectStoreActivity)
    }

    override fun onPositiveClick(from: Int) {

    }

    override fun onNegativeClick(from: Int) {

    }

    override fun onNeutralClick(from: Int) {

    }
}