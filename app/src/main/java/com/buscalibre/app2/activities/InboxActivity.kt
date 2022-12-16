package com.buscalibre.app2.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.buscalibre.app2.R
import com.buscalibre.app2.adapters.MessageAdapter
import com.buscalibre.app2.constants.ServerConstants
import com.buscalibre.app2.events.CountrySelectedEvent
import com.buscalibre.app2.events.MessageSelectedEvent
import com.buscalibre.app2.models.InboxMessages
import com.buscalibre.app2.models.MessageList
import com.buscalibre.app2.models.StandarResponse
import com.buscalibre.app2.models.SystemConfig
import com.buscalibre.app2.network.NetworkToken
import com.buscalibre.app2.network.NetworkUtil
import com.buscalibre.app2.network.RestClient
import com.buscalibre.app2.network.RetrofitClientInstance
import com.buscalibre.app2.util.AlertDialogHelper
import com.buscalibre.app2.util.ConfigUtil
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.realm.RealmResults
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.activity_inbox.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private var isCallingServer = false
private var isRefresh = true

private var NUMBER_PAGE = 1
private var FIRST_PAGE = 1
private var messageListRealmResults: RealmResults<MessageList>? = null
private var PAGE_LENGHT = 25
private var hasMoreMessages = true
private var messageAdapter: MessageAdapter? = null


class InboxActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inbox)
        initViews()
        initData()
        getInboxMessages(1, PAGE_LENGHT.toString())
    }

    private fun initData(){
        val config = realm.where(SystemConfig::class.java).findFirst()
        if (config != null){
            if (config.messagePageLen != null){
                PAGE_LENGHT = config.messagePageLen
            }
        }
    }

    private fun initViews(){
        showBackButton()
        //showCart()
        alertDialogHelper = AlertDialogHelper(this@InboxActivity)
        tvToolbarTitle.text = getString(R.string.text56)
        srlMessageList.setOnRefreshListener {
            getInboxMessages(1, PAGE_LENGHT.toString())
            isRefresh = true
        }
        btSetReadMessages.setOnClickListener {
            val messageListResults:RealmResults<MessageList> = realm.where(MessageList::class.java).equalTo("isReadSelected", true).findAll()
            if (messageListResults.size > 0){
                setMessageAsRead(messageListResults)
            }else{
                alertDialogHelper.showAlertDialog(getString(R.string.app_name),"No hay mensajes seleccionados.",getString(R.string.accept_dialog),"","",-1, false)
            }
        }
    }

    private fun setMessageAsRead(messageListResults:RealmResults<MessageList>){

        if (NetworkUtil.checkEnabledInternet(applicationContext)) {

            val json = JsonObject()
            val messageListArray = JsonArray()
            for (messageList:MessageList in messageListResults){
                messageListArray.add(messageList.id)
            }
            json.add("messageList",messageListArray)
            Log.e("jsonMessageRead", json.toString())
            val restClient = RetrofitClientInstance.getRetrofitInstance().create(RestClient::class.java)
            val call: Call<StandarResponse> = restClient.setMessageAsRead(userLogin?.token, ConfigUtil.getLocaleISO639(), json)
            call.enqueue(object : Callback<StandarResponse?> {
                override fun onResponse(call: Call<StandarResponse?>, response: Response<StandarResponse?>) {
                    val standarResponse: StandarResponse? = response.body()
                    if (standarResponse != null) {
                        if(standarResponse.blstatus == ServerConstants.AUTH_TOKEN_EXPIRED || standarResponse.blstatus == ServerConstants.AUTH_TOKEN_ERROR){
                            NetworkToken.refresh(applicationContext)
                            return
                        }
                        if (standarResponse.blstatus == ServerConstants.NO_ERROR) {
                            for (message:MessageList in messageListResults){
                                realm.executeTransaction {
                                    message.read = true
                                }
                            }
                            messageAdapter?.notifyDataSetChanged()
                            alertDialogHelper.showAlertDialog(getString(R.string.app_name),"Sus mensajes han sido marcados como leídos.",getString(R.string.accept_dialog),"","",-1, false)

                        }else{
                            Toast.makeText(applicationContext, standarResponse.blmessage, Toast.LENGTH_LONG).show()
                        }
                    }else{
                        alertDialogHelper.showAlertDialog(getString(R.string.app_name),getString(R.string.server_error),getString(R.string.accept_dialog),"","",-1, false)
                    }
                }

                override fun onFailure(call: Call<StandarResponse?>, t: Throwable) {
                    alertDialogHelper.showAlertDialog(getString(R.string.app_name),getString(R.string.server_error),getString(R.string.accept_dialog),"","",-1, false)
                }
            })

        }else{
            alertDialogHelper.showAlertDialog(getString(R.string.app_name),getString(R.string.connect_error),getString(R.string.accept_dialog),"","",-1, false)
        }
    }

    private fun getInboxMessages(numberPage: Int, lenghtPage: String){

        if (NetworkUtil.checkEnabledInternet(applicationContext)) {
            isCallingServer = true
            showProgress()
            val restClient = RetrofitClientInstance.getRetrofitInstance().create(RestClient::class.java)
            val call: Call<InboxMessages> = restClient.getInboxMessages(userLogin?.token, ConfigUtil.getLocaleISO639(), numberPage.toString(), lenghtPage)
            Log.e("numberpage", numberPage.toString())
            Log.e("lenghtPage", lenghtPage)

            call.enqueue(object : Callback<InboxMessages?> {
                override fun onResponse(call: Call<InboxMessages?>, response: Response<InboxMessages?>) {

                    val inboxMessages: InboxMessages? = response.body()
                    if (inboxMessages != null) {
                        if (inboxMessages.blstatus == ServerConstants.AUTH_TOKEN_EXPIRED || inboxMessages.blstatus == ServerConstants.AUTH_TOKEN_ERROR) {
                            NetworkToken.refresh(applicationContext)
                            return
                        }
                        if (inboxMessages.blstatus == ServerConstants.NO_ERROR) {
                            if (numberPage == 1) {
                                if(inboxMessages.messageList != null && inboxMessages.messageList.size > 0){
                                    InboxMessages.deleteAll(realm)
                                    realm.executeTransaction { realm ->
                                        realm.copyToRealm(inboxMessages)
                                    }
                                    messageAdapter?.notifyDataSetChanged()
                                    NUMBER_PAGE = numberPage
                                    hasMoreMessages = true
                                    initMessageList()
                                }else{
                                    hasMoreMessages = false
                                    tvNoMessages.visibility = View.VISIBLE
                                }
                            } else if (numberPage >= 1) {
                                realm.executeTransaction { realm ->
                                    realm.copyToRealm(inboxMessages)
                                }
                                if (inboxMessages.messageList == null || inboxMessages.messageList.size == 0){
                                    hasMoreMessages = false
                                }
                                NUMBER_PAGE = numberPage
                                messageAdapter!!.notifyDataSetChanged()
                                if (inboxMessages.messageList == null) {
                                    isRefresh = false
                                    Toast.makeText(this@InboxActivity, R.string.text30, Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            Toast.makeText(applicationContext, inboxMessages.blmessage, Toast.LENGTH_LONG).show()
                        }
                    } else {
                        alertDialogHelper.showAlertDialog(getString(R.string.app_name), getString(R.string.server_error), getString(
                                R.string.accept_dialog
                            ), "", "", -1, false
                        )
                    }
                    if (srlMessageList.isRefreshing) {
                        srlMessageList.isRefreshing = false
                    }
                    hideProgress()
                    isCallingServer = false

                }

                override fun onFailure(call: Call<InboxMessages?>, t: Throwable) {
                    if (srlMessageList.isRefreshing) {
                        srlMessageList.isRefreshing = false
                    }
                    isCallingServer = false
                    hideProgress()
                    alertDialogHelper.showAlertDialog(getString(R.string.app_name), getString(R.string.server_error), getString(
                            R.string.accept_dialog
                        ), "", "", -1, false
                    )
                }
            })

        }else{
            if (srlMessageList.isRefreshing) {
                srlMessageList.isRefreshing = false
            }
            alertDialogHelper.showAlertDialog(getString(R.string.app_name), getString(R.string.connect_error), getString(
                    R.string.accept_dialog
                ), "", "", -1, false)
            isCallingServer = false

        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun messageSelected(messageSelectedEvent: MessageSelectedEvent) {
        val messageList = messageSelectedEvent.messageList
        if(messageList != null){
            realm.executeTransaction {
                messageList.readSelected = messageSelectedEvent.readSelected
            }
            messageAdapter?.notifyDataSetChanged()
        }
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
        messageAdapter?.notifyDataSetChanged()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    /**
     * Create a RecyclerView from party list
     */
    private fun initMessageList() {
        messageListRealmResults = realm.where(MessageList::class.java).findAll()
        val layoutManager = LinearLayoutManager(this)
        rvInboxlist.layoutManager = layoutManager
        rvInboxlist.setHasFixedSize(true)
        if(messageListRealmResults != null && messageListRealmResults?.size == 0){
            tvNoMessages.visibility = View.VISIBLE
            tvNoMessages.bringToFront()
        }else{
            tvNoMessages.visibility = View.GONE
        }
        messageAdapter = MessageAdapter(messageListRealmResults, this@InboxActivity, false, false)

        rvInboxlist.adapter = messageAdapter
        rvInboxlist.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                if (!srlMessageList.isRefreshing && !isCallingServer && hasMoreMessages) {
                    if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0 && isRefresh) {
                        getInboxMessages(NUMBER_PAGE + 1, PAGE_LENGHT.toString())
                    }
                }
            }
        })
    }
}