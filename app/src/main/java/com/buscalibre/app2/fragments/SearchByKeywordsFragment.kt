package com.buscalibre.app2.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.buscalibre.app2.R
import com.buscalibre.app2.activities.BaseWebViewActivity
import com.buscalibre.app2.constants.AppConstants
import com.buscalibre.app2.events.BaseWebViewEvent
import com.buscalibre.app2.models.Country
import com.buscalibre.app2.models.UserLogin
import com.buscalibre.app2.util.AlertDialogHelper
import com.buscalibre.app2.util.ViewUtil
import io.realm.Realm
import org.greenrobot.eventbus.EventBus


class SearchByKeywordsFragment : Fragment(), AlertDialogHelper.AlertDialogListener {

    private val realm:Realm = Realm.getDefaultInstance()
    private lateinit var etKeySearch:EditText
    private lateinit var btSearchKeyword:Button
    private var userLogin:UserLogin? = null
    private var country:Country? = null
    private var url = ""
    private lateinit var ivBack:ImageView
    private lateinit var alertDialogHelper: AlertDialogHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_search_by_keywords, container, false)
        etKeySearch = view.findViewById(R.id.etKeySearch)
        btSearchKeyword = view.findViewById(R.id.btSearchKeyword)
        ivBack = view.findViewById(R.id.ivBack)
        initViews()
        getDataFromIntent()
        return view
    }

    private fun getDataFromIntent(){
        if (arguments != null){
            url = requireArguments().getString("url")!!
        }else{
            alertDialogHelper.showAlertDialog(getString(R.string.app_name), getString(
                R.string.text21), getString(R.string.accept_dialog),"","",1, false)
        }
    }

    private fun initViews(){
        alertDialogHelper = AlertDialogHelper(activity)
        //tvToolbarTitle.text = getString(R.string.text18)
        //showBackButton()
        etKeySearch.requestFocus()
        ViewUtil.openKeyboard(activity)
        etKeySearch.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent): Boolean {
                // If the event is a key-down event on the "enter" button
                if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {

                    searchOnClick()
                    return true
                }
                return false
            }
        })
        ivBack.setOnClickListener {
            ViewUtil.hideKeyboard(activity)
            activity?.onBackPressed()
        }
        btSearchKeyword.setOnClickListener {
            if (etKeySearch.text.toString().trim().isNotEmpty()){
                ViewUtil.hideKeyboard(activity)
                /*val intent = Intent(activity, BaseWebViewActivity::class.java)
                intent.putExtra("url", url)
                intent.putExtra("key", etKeySearch.text.toString().trim())
                intent.putExtra("header", userLogin?.webToken)
                intent.putExtra("replace_const", AppConstants.SEARCH_BY_KEYWORD)
                intent.putExtra("hasCart", true)
                intent.putExtra("title", getString(R.string.text49))
                startActivity(intent)*/
                EventBus.getDefault().post(BaseWebViewEvent(url, userLogin?.webToken, true, getString(R.string.text49), AppConstants.SEARCH_BY_KEYWORD, etKeySearch.text.toString().trim()))

            }else{
                etKeySearch.error = getString(R.string.text22)
                //alertDialogHelper.showAlertDialog(getString(R.string.app_name), getString(R.string.text22), getString(R.string.accept_dialog),"","",-1, false)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        country = realm.where(Country::class.java).findFirst()
        userLogin = realm.where(UserLogin::class.java).findFirst()

    }
    private fun searchOnClick() {
        if (etKeySearch.text.toString().trim().isNotEmpty()){
            ViewUtil.hideKeyboard(activity)
            val intent = Intent(activity, BaseWebViewActivity::class.java)
            intent.putExtra("url", url)
            intent.putExtra("key", etKeySearch.text.toString().trim())
            intent.putExtra("header", userLogin?.webToken)
            intent.putExtra("replace_const", AppConstants.SEARCH_BY_KEYWORD)
            intent.putExtra("hasCart", true)
            intent.putExtra("title", getString(R.string.text49))
            startActivity(intent)
        }else{
            etKeySearch.error = getString(R.string.text22)
            //alertDialogHelper.showAlertDialog(getString(R.string.app_name), getString(R.string.text22), getString(R.string.accept_dialog),"","",-1, false)
        }
    }

    override fun onPositiveClick(from: Int) {
        if(from == 1){
            activity?.onBackPressed()
        }
    }

    override fun onNegativeClick(from: Int) {

    }

    override fun onNeutralClick(from: Int) {

    }
}