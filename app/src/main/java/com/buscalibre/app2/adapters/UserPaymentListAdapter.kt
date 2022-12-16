package com.buscalibre.app2.adapters

import android.app.AlertDialog
import android.content.Context
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.buscalibre.app2.R
import com.buscalibre.app2.constants.AppConfig
import com.buscalibre.app2.constants.ServerConstants
import com.buscalibre.app2.events.RefreshPaymentEvent
import com.buscalibre.app2.models.PaymentList
import com.buscalibre.app2.models.StandarResponse
import com.buscalibre.app2.models.UserLogin
import com.buscalibre.app2.network.NetworkToken
import com.buscalibre.app2.network.NetworkUtil
import com.buscalibre.app2.network.RestClient
import com.buscalibre.app2.network.RetrofitClientInstance
import com.buscalibre.app2.util.ConfigUtil
import com.squareup.picasso.Picasso
import dmax.dialog.SpotsDialog
import io.realm.Realm
import org.greenrobot.eventbus.EventBus
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class UserPaymentListAdapter(private val countryResults: List<PaymentList>, val context: Context) : RecyclerView.Adapter<UserPaymentListAdapter.UserPaymentAdapterViewHolder>()  {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserPaymentAdapterViewHolder {

        return UserPaymentAdapterViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.row_payment,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return countryResults.size
    }

    override fun onBindViewHolder(holder: UserPaymentAdapterViewHolder, position: Int) {
        val paymentList = countryResults[position]
        val cardName = context.getString(R.string.text68) + " " + paymentList.lastDigits
        holder.tvPaymentAddedName.text = cardName.uppercase()
        //holder.tvPaymentAddedName!!.typeface = FontUtil.getMonserratLightTypeface(context)
        Picasso.get()
            .load(AppConfig.URL_SERVER_PROD + paymentList.icon)
            .placeholder(R.drawable.ic_card_placeholder)
            .into(holder.ivCardWhite)
        if (paymentList.default) {
            holder.ivSelectedPayment.visibility = View.VISIBLE
        }
        holder.rlSelectedPayment.setOnClickListener {
            if (!paymentList.default){
                changeActivePayment(paymentList.id)
            }
        }

        holder.ivDeletePaymentMethod.setOnClickListener {

            val builder =
                AlertDialog.Builder(ContextThemeWrapper(context, R.style.AlertDialogCustom))
            builder.setTitle(context.getString(R.string.app_name))
            builder.setMessage(context.getString(R.string.text65))
            builder.setPositiveButton(
                context.getString(R.string.accept_dialog)
            ) { _, _ ->
                deletePayment(paymentList.id)

            }
            builder.setNegativeButton(
                context.getString(R.string.cancel_dialog)
            ) { dialog, which -> dialog.dismiss() }
            builder.setCancelable(false)
            builder.show()
        }

    }

    class UserPaymentAdapterViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var ivSelectedPayment: ImageView
        var tvPaymentAddedName: TextView
        var ivDeletePaymentMethod: ImageView
        var ivCardWhite: ImageView
        var rlSelectedPayment: RelativeLayout


        init {
            ivSelectedPayment = itemView.findViewById(R.id.ivSelectedPayment)
            ivDeletePaymentMethod = itemView.findViewById(R.id.ivDeletePaymentMethod)
            ivCardWhite = itemView.findViewById(R.id.ivCardWhite)
            tvPaymentAddedName = itemView.findViewById(R.id.tvPaymentAddedName)
            rlSelectedPayment = itemView.findViewById(R.id.rlSelectedPayment)

        }
    }

    private fun changeActivePayment(paymentID: String) {
        if (NetworkUtil.checkEnabledInternet(context)) {
            val spotsDialog = SpotsDialog.Builder()
                .setContext(context)
                .setTheme(R.style.CustomProgress)
                .build()
            spotsDialog.show()
            val restClient: RestClient = RetrofitClientInstance.getRetrofitInstance().create(
                RestClient::class.java
            )
            val userLogin = Realm.getDefaultInstance().where(UserLogin::class.java).findFirst()
            val call: Call<StandarResponse> = restClient.setCurrentPaymentMethod(
                userLogin?.token,
                ConfigUtil.getLocaleISO639(),
                paymentID
            )
            call.enqueue(object : Callback<StandarResponse?> {
                override fun onResponse(
                    call: Call<StandarResponse?>,
                    response: Response<StandarResponse?>
                ) {
                    val paymentDelete: StandarResponse? = response.body()
                    if (paymentDelete != null) {
                        if (paymentDelete.blstatus == ServerConstants.AUTH_TOKEN_EXPIRED) {
                            NetworkToken.refresh(context)
                            return
                        }
                        if (paymentDelete.blstatus == ServerConstants.NO_ERROR) {
                            EventBus.getDefault().post(RefreshPaymentEvent(true))
                        } else {
                            Toast.makeText(context, paymentDelete.blmessage, Toast.LENGTH_LONG)
                                .show()
                        }
                    } else {
                        Toast.makeText(
                            context,
                            context.getString(R.string.connect_error),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    spotsDialog.dismiss()
                }

                override fun onFailure(call: Call<StandarResponse?>, t: Throwable) {
                    spotsDialog.dismiss()
                }
            })
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.no_internet_error),
                Toast.LENGTH_LONG
            ).show()

        }
    }

    private fun deletePayment(paymentID: String) {
        if (NetworkUtil.checkEnabledInternet(context)) {
            val spotsDialog = SpotsDialog.Builder()
                .setContext(context)
                .setTheme(R.style.CustomProgress)
                .build()
            spotsDialog.show()
            val restClient: RestClient = RetrofitClientInstance.getRetrofitInstance().create(
                RestClient::class.java
            )
            val userLogin = Realm.getDefaultInstance().where(UserLogin::class.java).findFirst()
            val call: Call<StandarResponse> = restClient.removeSelectedPaymentID(
                userLogin?.token,
                ConfigUtil.getLocaleISO639(),
                paymentID
            )
            call.enqueue(object : Callback<StandarResponse?> {
                override fun onResponse(
                    call: Call<StandarResponse?>,
                    response: Response<StandarResponse?>
                ) {
                    val paymentDelete: StandarResponse? = response.body()
                    if (paymentDelete != null) {
                        if (paymentDelete.blstatus == ServerConstants.AUTH_TOKEN_EXPIRED) {
                            NetworkToken.refresh(context)
                            return
                        }
                        if (paymentDelete.blstatus == ServerConstants.NO_ERROR) {
                            EventBus.getDefault().post(RefreshPaymentEvent(true))
                        } else {
                            Toast.makeText(context, paymentDelete.blmessage, Toast.LENGTH_LONG)
                                .show()
                        }
                    } else {
                        Toast.makeText(
                            context,
                            context.getString(R.string.connect_error),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    spotsDialog.dismiss()
                }

                override fun onFailure(call: Call<StandarResponse?>, t: Throwable) {
                    spotsDialog.dismiss()
                }
            })
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.no_internet_error),
                Toast.LENGTH_LONG
            ).show()

        }
    }



}