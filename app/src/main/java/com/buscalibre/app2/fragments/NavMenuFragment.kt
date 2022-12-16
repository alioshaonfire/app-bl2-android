package com.buscalibre.app2.fragments

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.buscalibre.app2.BuildConfig
import com.buscalibre.app2.R
import com.buscalibre.app2.adapters.MenuListAdapter
import com.buscalibre.app2.constants.AppConstants
import com.buscalibre.app2.models.Country
import com.buscalibre.app2.models.MenuList
import com.buscalibre.app2.models.UserLogin
import com.buscalibre.app2.util.AlertDialogHelper
import io.realm.Realm
import io.realm.RealmResults


class NavMenuFragment : Fragment(), AlertDialogHelper.AlertDialogListener {

    private val realm: Realm = Realm.getDefaultInstance()
    lateinit var userLogin:UserLogin
    lateinit var alertDialogHelper:AlertDialogHelper
    lateinit var country:Country
    private val PAYMENT_METHODS = 0
    private val INBOX = 1
    private val SELL_BOOKS = 2
    private val CHANGE_COUNTRY = 3
    private val HELP_SUPPORT = 4
    private val LOGOUT = 5
    private lateinit var rvMenuList:RecyclerView
    private lateinit var tvAppVersion:TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val mainView = inflater.inflate(R.layout.fragment_nav_menu, container, false)
        userLogin = realm.where(UserLogin::class.java).findFirst()!!
        alertDialogHelper = AlertDialogHelper(activity)
        country = realm.where(Country::class.java).equalTo("isSelected", true).findFirst()!!
        rvMenuList = mainView.findViewById(R.id.rvMenuList)
        tvAppVersion = mainView.findViewById(R.id.tvAppVersion)
        initMenuList()
        return mainView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    private fun setVersion(){
        val version: String
        var preVersion = ""
        try {
            if (BuildConfig.DEBUG) {
                preVersion = AppConstants.DEV_PREFIX
            }
            val pInfo = activity?.packageManager?.getPackageInfo(activity?.packageName!!, 0)
            version = "v" + pInfo?.versionName + preVersion
            tvAppVersion.text = version
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("packageError", e.toString())
        }
    }

    private fun initMenuOnView(menuListResults:RealmResults<MenuList>){
        val layoutManager = LinearLayoutManager(activity)
        rvMenuList.layoutManager = layoutManager
        rvMenuList.setHasFixedSize(true)
        rvMenuList.adapter = MenuListAdapter(menuListResults, activity, false, false)
        setVersion()
    }

    private fun initMenuList(){

        val menuListResults = realm.where(MenuList::class.java).findAll()
        if (menuListResults.size == 0){
            realm.executeTransaction {
                val paymentMethods = realm.createObject(MenuList::class.java)
                paymentMethods.id = PAYMENT_METHODS
                paymentMethods.name = getString(R.string.text63)
                realm.copyToRealm(paymentMethods)

                val inbox = realm.createObject(MenuList::class.java)
                inbox.id = INBOX
                inbox.name = getString(R.string.text56)
                realm.copyToRealm(inbox)

                val bookSeller = realm.createObject(MenuList::class.java)
                bookSeller.id = SELL_BOOKS
                bookSeller.name = getString(R.string.text75)
                realm.copyToRealm(bookSeller)

                val changeCountry = realm.createObject(MenuList::class.java)
                changeCountry.id = CHANGE_COUNTRY
                changeCountry.name = getString(R.string.text48)
                realm.copyToRealm(changeCountry)

                val helpSupport = realm.createObject(MenuList::class.java)
                helpSupport.id = HELP_SUPPORT
                helpSupport.name = getString(R.string.text45)
                realm.copyToRealm(helpSupport)

                val logout = realm.createObject(MenuList::class.java)
                logout.id = LOGOUT
                logout.name = getString(R.string.text4)
                realm.copyToRealm(logout)
            }
            val menuListCreated = realm.where(MenuList::class.java).findAll()
            initMenuOnView(menuListCreated)
        }else{
            initMenuOnView(menuListResults)
        }
    }



    override fun onPositiveClick(from: Int) {

    }

    override fun onNegativeClick(from: Int) {

    }

    override fun onNeutralClick(from: Int) {

    }

}