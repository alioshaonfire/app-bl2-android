package com.buscalibre.app2.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.buscalibre.app2.R;
import com.buscalibre.app2.activities.PaymentDetailActivity;
import com.buscalibre.app2.constants.ServerConstants;
import com.buscalibre.app2.models.PaymentList_;
import com.buscalibre.app2.models.ProductList;
import com.buscalibre.app2.util.DateUtil;

import java.text.NumberFormat;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;

public class PaymentsHistoryAdapter extends RealmRecyclerViewAdapter<PaymentList_, PaymentsHistoryAdapter.ProductShowcaseViewHolder> {

    private RealmResults<PaymentList_> paymentListRealmResults;
    private Context context;
    private Realm realm = Realm.getDefaultInstance();


    public PaymentsHistoryAdapter(@Nullable RealmResults<PaymentList_> paymentListRealmResults, Context context, boolean autoUpdate, boolean updateOnModification) {
        super(paymentListRealmResults, autoUpdate, updateOnModification);
        this.paymentListRealmResults = paymentListRealmResults;
        this.context = context;
    }

    @NonNull
    @Override
    public ProductShowcaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_showcase_product, parent,false);
        ProductShowcaseViewHolder holder = new ProductShowcaseViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ProductShowcaseViewHolder holder, final int position) {

        PaymentList_ paymentList_ = paymentListRealmResults.get(position);
        if (paymentList_ != null){
            holder.llSellValue.setVisibility(View.INVISIBLE);
            holder.tvProductShowcaseCondition.setVisibility(View.VISIBLE);
            holder.tvProductShowcaseCondition.setText(DateUtil.convertDate(paymentList_.getPaymentDate().replace(".000Z", ""),"MMMM, yyyy").toUpperCase());
            holder.tvProductShowcaseName.setText(R.string.text116);
            holder.tvProductShowcaseName.setTypeface(Typeface.DEFAULT);
            holder.tvProductShowcaseName.setTypeface(Typeface.DEFAULT);

            holder.tvProductShowcaseCondition.setTypeface(Typeface.DEFAULT_BOLD);
            holder.tvProductShowcaseCondition.setTextColor(context.getResources().getColor(R.color.md_black_1000));

            String total = NumberFormat.getCurrencyInstance(Locale.getDefault()).format(paymentList_.getPaymentTotal());
            holder.tvProductShowcaseTotal.setText(total);
            holder.tvSellDate.setVisibility(View.VISIBLE);
            holder.tvSellDate.setText(paymentList_.getPaymentStatusText());
            holder.tvSellDate.setBackgroundColor(paymentList_.getPaymentStatus() != ServerConstants.SELLER_PAYMENT_STATUS_PENDING ? context.getResources().getColor(R.color.to_pay):context.getResources().getColor(R.color.payed));
            holder.tvSellDate.setTypeface(Typeface.DEFAULT_BOLD);
            holder.tvPaymentID.setVisibility(View.VISIBLE);
            holder.tvPaymentID.setText("#" + paymentList_.getPaymentId());
        }
    }

    @Override
    public int getItemCount() {
        return paymentListRealmResults.size();
    }

    class ProductShowcaseViewHolder extends RecyclerView.ViewHolder {

        TextView tvProductShowcaseCondition;
        TextView tvProductShowcaseName;
        TextView tvProductShowcasePrice;
        TextView tvProductShowcaseTotal;
        LinearLayout llSellValue;
        TextView tvSellDate, tvPaymentID;



        ProductShowcaseViewHolder(View itemView) {
            super(itemView);
            tvPaymentID = itemView.findViewById(R.id.tvPaymentID);
            tvSellDate = itemView.findViewById(R.id.tvSellDate);
            llSellValue = itemView.findViewById(R.id.llSellValue);
            tvProductShowcaseTotal = itemView.findViewById(R.id.tvProductShowcaseTotal);
            tvProductShowcasePrice = itemView.findViewById(R.id.tvProductShowcasePrice);
            tvProductShowcaseCondition = itemView.findViewById(R.id.tvProductShowcaseCondition);
            tvProductShowcaseName = itemView.findViewById(R.id.tvProductShowcaseName);

            itemView.setOnClickListener(v -> {
                PaymentList_ paymentList_ = paymentListRealmResults.get(getAdapterPosition());
                if (paymentList_ != null){
                    Intent intent = new Intent(context, PaymentDetailActivity.class);
                    intent.putExtra("paymentDate", paymentList_.getPaymentDate());
                    intent.putExtra("paymentId", paymentList_.getPaymentId());

                    context.startActivity(intent);
                }
            });
        }
    }
}
