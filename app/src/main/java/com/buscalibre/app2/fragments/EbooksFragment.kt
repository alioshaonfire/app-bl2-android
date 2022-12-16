package com.buscalibre.app2.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.binpar.bibooks.sdk.DownloadManager
import com.binpar.bibooks.sdk.SDKBibooks
import com.buscalibre.app2.R
import com.buscalibre.app2.adapters.EbookListAdapter
import com.buscalibre.app2.constants.ServerConstants
import com.buscalibre.app2.events.RefreshEbookDownloadEvent
import com.buscalibre.app2.models.*
import com.buscalibre.app2.network.NetworkToken
import com.buscalibre.app2.network.NetworkUtil
import com.buscalibre.app2.network.RestClient
import com.buscalibre.app2.network.RetrofitClientInstance
import com.buscalibre.app2.util.AlertDialogHelper
import com.buscalibre.app2.util.ConfigUtil
import com.google.gson.JsonObject
import io.realm.Realm
import kotlinx.android.synthetic.main.fragment_ebooks.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EbooksFragment : Fragment() {

    private lateinit var realm: Realm
    private lateinit var userLogin: UserLogin
    private lateinit var country:Country
    private var isCallingServer = false
    private var isRefresh = true
    private var alertDialogHelper: AlertDialogHelper? = null
    private var NUMBER_PAGE = 1
    private var FIRST_PAGE = 1
    private var PAGE_LENGHT = 10
    private var hasMoreProducts = true
    private  var EbookListAdapter:EbookListAdapter? = null
    private var ebookToken = ""
    private var isFirstTime = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }


    override fun onResume() {
        super.onResume()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun refreshEbookDownload(refreshEbookDownloadEvent: RefreshEbookDownloadEvent) {
        val ebookList = refreshEbookDownloadEvent.ebookList
        if (ebookList != null){
            val isbn = ebookList.isbn
            realm.executeTransaction {
                ebookList.isDownloading = true
            }
            try{
                SDKBibooks.initDownloadManager(context, ebookToken, isbn).start(
                    DownloadManager.DownloadFinishCallback {
                        realm.executeTransaction {
                            val existLocalEbook = realm.where(LocalEbook::class.java).equalTo("id", ebookList.id).findFirst()
                            if (existLocalEbook == null) {
                                val localEbook = realm.createObject(LocalEbook::class.java)
                                localEbook.id = ebookList.id
                                localEbook.type = ebookList.type
                                localEbook.isbn = ebookList.isbn
                                localEbook.name = ebookList.name
                                localEbook.author = ebookList.author
                                localEbook.image = ebookList.image
                                localEbook.imageURL = ebookList.imageURL
                                localEbook.userEmail = userLogin.email
                            }
                            ebookList.isDownloading = false
                            ebookList.isDownloadCompleted = true
                        }
                        Log.e("EBookDownload", "OK")
                    })
            }catch (exception: Exception){
                realm.executeTransaction {
                    ebookList.isDownloading = false
                    ebookList.isDownloadCompleted = false
                }
                Log.e("EBookDownloadError", exception.toString())
            }
        }
    }

    private fun initData(){
        val config = realm.where(SystemConfig::class.java).findFirst()
        if (config != null){
            if (config.ebooksPageLen != null){
                PAGE_LENGHT = config.ebooksPageLen
            }
        }
    }

    private fun getEbooksFromServer(numberPage: Int, lenghtPage: String){
        if (NetworkUtil.checkEnabledInternet(activity)) {
            val jsonObject = JsonObject()
            jsonObject.addProperty("ebookType", ServerConstants.EBOOK_TYPE_EBOOK)

            val restClient = RetrofitClientInstance.getRetrofitInstance().create(RestClient::class.java)

            val call: Call<Ebook> = restClient.getEbooks(
                userLogin.token, ConfigUtil.getLocaleISO639(), jsonObject,
                numberPage.toString(), lenghtPage
            )

            call.enqueue(object : Callback<Ebook?> {
                override fun onResponse(call: Call<Ebook?>, response: Response<Ebook?>) {
                    val ebook: Ebook? = response.body()
                    if (ebook != null) {
                        if (ebook.blstatus == ServerConstants.AUTH_TOKEN_EXPIRED || ebook.blstatus == ServerConstants.AUTH_TOKEN_ERROR) {
                            NetworkToken.refresh(activity)
                            return
                        }
                        if (ebook.blstatus == ServerConstants.NO_ERROR) {
                            hasMoreProducts = ebook.pendingRows
                            if (numberPage == 1) {
                                if (ebook.ebookList != null && ebook.ebookList.size > 0) {
                                    Ebook.deleteAll(realm, ServerConstants.EBOOK_TYPE_EBOOK)
                                    realm.executeTransaction {
                                        realm.copyToRealm(ebook.ebookList)
                                    }
                                    //initBookList()
                                    //EbookListAdapter?.notifyDataSetChanged()
                                    NUMBER_PAGE = numberPage
                                    hasMoreProducts = true
                                    tvNoEbooksAvailable.visibility = View.GONE
                                    initBookList()
                                } else {
                                    hasMoreProducts = false
                                    tvNoEbooksAvailable.visibility = View.VISIBLE
                                }
                            } else if (numberPage >= 1) {
                                realm.executeTransaction {
                                    realm.copyToRealm(ebook.ebookList)
                                }
                                tvNoEbooksAvailable.visibility = View.GONE
                                if (ebook.ebookList == null || ebook.ebookList.size == 0) {
                                    hasMoreProducts = false
                                }
                                NUMBER_PAGE = numberPage
                                //EbookListAdapter?.notifyDataSetChanged()
                                if (ebook.ebookList == null) {
                                    isRefresh = false
                                    Toast.makeText(activity, R.string.text30, Toast.LENGTH_SHORT)
                                        .show()
                                }
                            }


                        } else {
                            alertDialogHelper?.showAlertDialog(
                                getString(R.string.app_name), ebook.blmessage, getString(
                                    R.string.accept_dialog
                                ), "", "", 1, false
                            )
                        }
                    } else {
                        tvNoEbooksAvailable.visibility = View.VISIBLE
                        alertDialogHelper?.showAlertDialog(
                            getString(R.string.app_name),
                            getString(R.string.server_error),
                            getString(
                                R.string.accept_dialog
                            ),
                            "",
                            "",
                            1,
                            false
                        )
                    }
                    if (srlEbooksList.isRefreshing) {
                        srlEbooksList.isRefreshing = false
                    }
                    isCallingServer = false
                }

                override fun onFailure(call: Call<Ebook?>, t: Throwable) {
                    isCallingServer = false
                    if (srlEbooksList.isRefreshing) {
                        srlEbooksList.isRefreshing = false
                    }
                    tvNoEbooksAvailable.visibility = View.VISIBLE
                    alertDialogHelper?.showAlertDialog(
                        getString(R.string.app_name), getString(R.string.server_error), getString(
                            R.string.accept_dialog
                        ), "", "", 1, false
                    )
                    Log.e("errorEbookList", t.toString())
                }
            })

        }else{
            tvNoEbooksAvailable.visibility = View.VISIBLE
            if (srlEbooksList.isRefreshing) {
                srlEbooksList.isRefreshing = false
            }
            alertDialogHelper?.showAlertDialog(
                getString(R.string.app_name), getString(R.string.connect_error), getString(
                    R.string.accept_dialog
                ), "", "", -1, false
            )
            isCallingServer = false
        }
    }

    private fun initBookList(){
        val ebookListRealmResults = realm.where(EbookList::class.java)
            .equalTo("type", ServerConstants.EBOOK_TYPE_EBOOK)
            .distinct("id")
            .findAll()
        val layoutManager = LinearLayoutManager(activity)
        rvEBooks.layoutManager = layoutManager
        rvEBooks.setHasFixedSize(true)
        EbookListAdapter = EbookListAdapter(
            ebookListRealmResults,
            activity,
            true,
            true
        )
        rvEBooks.adapter = EbookListAdapter
        rvEBooks.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                if (isFirstTime) {
                    isFirstTime = false
                    return
                }
                if (!srlEbooksList.isRefreshing && !isCallingServer && hasMoreProducts) {
                    if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0 && isRefresh) {
                        isCallingServer = true
                        getEbooksFromServer(NUMBER_PAGE + 1, PAGE_LENGHT.toString())
                    } else {
                        //Toast.makeText(activity, getString(R.string.text124), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
    private fun initOfflineBookList(){
        srlEbooksList.isEnabled = false
        srlEbooksList.isRefreshing = false

        val backupEbooks = realm.where(LocalEbook::class.java).equalTo("type", ServerConstants.EBOOK_TYPE_EBOOK).findAll()
        for (localEbook:LocalEbook in backupEbooks){
            val localEbookList = realm.where(EbookList::class.java)
                .equalTo("id", localEbook.id)
                .findFirst()
            if (localEbookList != null){
                realm.executeTransaction {
                    localEbookList.isDownloadCompleted = true
                }
            }
        }

        val localEbookCompleted = realm.where(EbookList::class.java)
            .equalTo("type", ServerConstants.EBOOK_TYPE_EBOOK)
            .distinct("id")
            .equalTo("isDownloadCompleted", true)
            .findAll()

        val layoutManager = LinearLayoutManager(activity)
        rvEBooks.layoutManager = layoutManager
        rvEBooks.setHasFixedSize(true)
        rvEBooks.isNestedScrollingEnabled = false
        EbookListAdapter = EbookListAdapter(
            localEbookCompleted,
            activity,
            true,
            true
        )
        rvEBooks.adapter = EbookListAdapter
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        realm = Realm.getDefaultInstance()
        alertDialogHelper = AlertDialogHelper(activity)
        country = realm.where(Country::class.java).equalTo("isSelected", true).findFirst()!!
        userLogin = realm.where(UserLogin::class.java).findFirst()!!
        if (userLogin.ebookToken != null){
            ebookToken = userLogin.ebookToken
        }else{
            NetworkToken.refresh(activity)
        }
        initData()
        startEbook()
        srlEbooksList.setOnRefreshListener {
            startEbook()
            isRefresh = true
        }
    }

    private fun startEbook(){
        try{
            if (userLogin.isOfflineMode){
                initOfflineBookList()
            }else{
                getEbooksFromServer(FIRST_PAGE, PAGE_LENGHT.toString())
            }
        }catch (ex:Exception){

        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ebooks, container, false)
    }


}