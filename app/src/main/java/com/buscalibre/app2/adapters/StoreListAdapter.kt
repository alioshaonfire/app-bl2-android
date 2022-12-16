package com.buscalibre.app2.adapters

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.buscalibre.app2.R
import com.buscalibre.app2.activities.StoreWebviewActivity
import com.buscalibre.app2.activities.WelcomeUserActivity
import com.buscalibre.app2.constants.AppConfig
import com.buscalibre.app2.events.SelectAmazonEvent
import com.buscalibre.app2.events.SelectEbayEvent
import com.buscalibre.app2.models.Store
import com.buscalibre.app2.util.StoreManager
import com.squareup.picasso.Picasso
import io.realm.Realm
import org.greenrobot.eventbus.EventBus


class StoreListAdapter(private val countryResults: List<Store>, val context:Context) : RecyclerView.Adapter<StoreListAdapter.StoreAdapterViewHolder>()  {

    private val realm:Realm = Realm.getDefaultInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoreAdapterViewHolder {

        return StoreAdapterViewHolder(LayoutInflater.from(context).inflate(R.layout.row_store, parent, false))
    }

    override fun getItemCount(): Int {
        return countryResults.size
    }

    override fun onBindViewHolder(holder: StoreAdapterViewHolder, position: Int) {
        val store = countryResults[position]
        Picasso.get()
            .load(AppConfig.URL_SERVER_PROD + store.icon)
            .placeholder(R.drawable.preload_icon)
            .into(holder.ivStoreFlag)


        holder.rlRowStore.setOnClickListener {

            if (store.id == null){
                return@setOnClickListener
            }
            if(store.type == 1){
                EventBus.getDefault().post(SelectAmazonEvent(true))
                /*val intent = Intent(context, StoreWebviewActivity::class.java)
                intent.putExtra("storeSelected", "amazon")
                context.startActivity(intent)*/
            }else if(store.type == 2){
                EventBus.getDefault().post(SelectEbayEvent(true))

                /*val intent = Intent(context, StoreWebviewActivity::class.java)
                intent.putExtra("storeSelected", "ebay")
                context.startActivity(intent)*/

            }else if(store.type == 3){
                val intent = Intent(context, StoreWebviewActivity::class.java)
                intent.putExtra("storeSelected", "bhphoto")
                context.startActivity(intent)
            }


        }
    }

    class StoreAdapterViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var ivStoreFlag: ImageView
        var rlRowStore: RelativeLayout


        init {
            rlRowStore = itemView.findViewById(R.id.rlRowStore)
            ivStoreFlag = itemView.findViewById(R.id.ivStoreFlag)

            itemView.setOnClickListener {

            }
        }
    }

    private fun Context.drawableWithColor(@DrawableRes drawableRes: Int, @ColorInt color: Int): Drawable? {
        val pic = ContextCompat.getDrawable(this, drawableRes)
        pic?.setColorFilter(color, PorterDuff.Mode.SRC_IN)
        return pic
    }


}