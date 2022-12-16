package com.buscalibre.app2.adapters

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.buscalibre.app2.R
import com.buscalibre.app2.activities.*
import com.buscalibre.app2.constants.AppConfig
import com.buscalibre.app2.constants.ServerConstants
import com.buscalibre.app2.events.BaseWebViewEvent
import com.buscalibre.app2.events.SearchByKeywordsEvent
import com.buscalibre.app2.events.SelectStoreEvent
import com.buscalibre.app2.fragments.SearchByKeywordsFragment
import com.buscalibre.app2.models.Option
import com.buscalibre.app2.models.UserLogin
import com.buscalibre.app2.util.AlertDialogHelper
import com.squareup.picasso.Picasso
import io.realm.Realm
import io.realm.RealmList
import org.greenrobot.eventbus.EventBus


class MenuOptionAdapter(private val optionList: RealmList<Option>, val context:Context) : RecyclerView.Adapter<MenuOptionAdapter.CountryAdapterViewHolder>(), AlertDialogHelper.AlertDialogListener  {

    private val realm:Realm = Realm.getDefaultInstance()
    private var alertDialogHelper: AlertDialogHelper? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryAdapterViewHolder {
        alertDialogHelper = AlertDialogHelper(context)
        return CountryAdapterViewHolder(LayoutInflater.from(context).inflate(R.layout.row_menu_option, parent, false))
    }

    override fun getItemCount(): Int {
        return optionList.size
    }

    override fun onBindViewHolder(holder: CountryAdapterViewHolder, position: Int) {
        val option = optionList[position]
        if (option != null){
            holder.tvMenuName.text = option.name
            Picasso.get()
                .load(AppConfig.URL_SERVER_PROD + option.icon)
                .fit()
                .centerCrop()
                .placeholder(R.drawable.preload_menu)
                .into(holder.ivMenuIcon)

            Log.e("urlImage", AppConfig.URL_SERVER_PROD + option.icon)
            Log.e("name", option.name)


            holder.ivMenuIcon.setOnClickListener {
                val type = option.type
                var name = ""
                if(option.name != null){
                    name = option.name
                }
                val userLogin = realm.where(UserLogin::class.java).findFirst()

                if (type == ServerConstants.MAIN_MENU_TYPE_WEBVIEW){
                    EventBus.getDefault().post(BaseWebViewEvent(option.url, userLogin?.webToken, true, name, null, null))

                }else if (type == ServerConstants.MAIN_MENU_TYPE_SEARCH_ISBN){
                    val intent = Intent(context, CameraActivity::class.java)
                    intent.putExtra("url", option.url)
                    intent.putExtra("isSellBook", false)
                    intent.putExtra("header", userLogin?.webToken)
                    intent.putExtra("hasCart", true)
                    context.startActivity(intent)
                }else if (type == ServerConstants.MAIN_MENU_TYPE_EXTERNAL_STORE){

                    EventBus.getDefault().post(SelectStoreEvent(true))

                }else if (type == ServerConstants.MAIN_MENU_TYPE_SELL_BOOKS){
                    val intent = Intent(context, MenuSellerActivity::class.java)
                    context.startActivity(intent)
                }else if (type == ServerConstants.MAIN_MENU_TYPE_SEARCH_KEY_WORDS){
                    if (option.url != null){
                        /*val intent = Intent(context, SearchByKeywordsActivity::class.java)
                        intent.putExtra("url", option.url)
                        intent.putExtra("header", userLogin?.webToken)
                        intent.putExtra("hasCart", true)
                        context.startActivity(intent)*/
                        EventBus.getDefault().post(SearchByKeywordsEvent(option.url, userLogin?.webToken, true, context.getString(R.string.text18)))

                    }else{
                        alertDialogHelper?.showAlertDialog(context.getString(R.string.app_name),context.getString(
                            R.string.text21),context.getString(R.string.accept_dialog),"","",-1, false)
                    }

                }else if (type == ServerConstants.MAIN_MENU_TYPE_EXTERNAL_STORE){

                }else if (type == ServerConstants.MAIN_MENU_TYPE_EBOOK_READER){
                    val intent = Intent(context, MyEbookListActivity::class.java)
                    context.startActivity(intent)
                }else{
                    alertDialogHelper?.showAlertDialog(context.getString(R.string.app_name),context.getString(R.string.text17),context.getString(R.string.accept_dialog),"","",-1, false)

                }
            }
            if(option.type != null){
                Log.e("type", option.type.toString())

            }
            if(option.icon != null){
                Log.e("icon", option.icon)

            }
            if(option.url != null){
                Log.e("url", option.url)

            }
            if(option.id != null){
                Log.e("id", option.id)

            }
            if(option.name != null){
                Log.e("name", option.name)

            }
        }
    }

    class CountryAdapterViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var ivMenuIcon: ImageView
        var tvMenuName: TextView


        init {
            tvMenuName = itemView.findViewById(R.id.tvMenuName)
            ivMenuIcon = itemView.findViewById(R.id.ivMenuIcon)
            itemView.setOnClickListener {

            }
        }
    }

    private fun Context.drawableWithColor(@DrawableRes drawableRes: Int, @ColorInt color: Int): Drawable? {
        val pic = ContextCompat.getDrawable(this, drawableRes)
        pic?.setColorFilter(color, PorterDuff.Mode.SRC_IN)
        return pic
    }

    override fun onPositiveClick(from: Int) {

    }

    override fun onNegativeClick(from: Int) {
    }

    override fun onNeutralClick(from: Int) {
    }
}