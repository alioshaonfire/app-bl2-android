package com.buscalibre.app2.adapters

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.buscalibre.app2.R
import com.buscalibre.app2.constants.AppConfig
import com.buscalibre.app2.constants.AppConstants
import com.buscalibre.app2.events.CountrySelectedEvent
import com.buscalibre.app2.models.Country
import com.squareup.picasso.Picasso
import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmRecyclerViewAdapter
import io.realm.RealmResults
import org.greenrobot.eventbus.EventBus


class CountryListAdapter(private val countryResults: RealmList<Country>, val context:Context) : RecyclerView.Adapter<CountryListAdapter.CountryAdapterViewHolder>()  {

    private val realm:Realm = Realm.getDefaultInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryAdapterViewHolder {

        return CountryAdapterViewHolder(LayoutInflater.from(context).inflate(R.layout.row_country, parent, false))
    }

    override fun getItemCount(): Int {
        return countryResults.size
    }

    override fun onBindViewHolder(holder: CountryAdapterViewHolder, position: Int) {
        val country = countryResults[position]
        if (country != null){
            holder.tvCountryName.text = country.name
            Picasso.get()
                .load(AppConfig.URL_SERVER_PROD + country.icon)
                .placeholder(R.drawable.preload_icon)
                .into(holder.ivCountryFlag)

            if (country.isSelected){
                holder.rlRowCountry.setBackgroundColor(context.resources.getColor(R.color.selected_country))
                holder.rlRowCountry.background.alpha = 30
                holder.ivCountryCheck.visibility = View.VISIBLE
                EventBus.getDefault().post(CountrySelectedEvent(country))
            }else{
                holder.ivCountryCheck.visibility = View.INVISIBLE
                holder.rlRowCountry.setBackgroundColor(context.resources.getColor(android.R.color.white))
            }
            holder.rlRowCountry.setOnClickListener {

                for (co:Country in countryResults){
                    if (co.id != country.id){
                        realm.executeTransaction { co.isSelected = false }
                    }
                }
                realm.executeTransaction { country.isSelected = true }
                EventBus.getDefault().post(CountrySelectedEvent(country))
                notifyDataSetChanged()
            }
        }
    }

    class CountryAdapterViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var ivCountryFlag: ImageView
        var tvCountryName: TextView
        var rlRowCountry: RelativeLayout
        var ivCountryCheck: ImageView


        init {
            ivCountryCheck = itemView.findViewById(R.id.ivCountryCheck)
            rlRowCountry = itemView.findViewById(R.id.rlRowCountry)
            ivCountryFlag = itemView.findViewById(R.id.ivCountryFlag)
            tvCountryName = itemView.findViewById(R.id.tvCountryName)
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