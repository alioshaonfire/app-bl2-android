package com.buscalibre.app2.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.buscalibre.app2.R;
import com.buscalibre.app2.models.ProductList;
import com.buscalibre.app2.util.DateUtil;

import java.text.NumberFormat;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;

public class DetailPaymentsAdapter extends RealmRecyclerViewAdapter<ProductList, DetailPaymentsAdapter.ProductShowcaseViewHolder> {

    private RealmResults<ProductList> productListRealmResults;
    private Context context;
    private Realm realm = Realm.getDefaultInstance();


    public DetailPaymentsAdapter(@Nullable RealmResults<ProductList> productListRealmResults, Context context, boolean autoUpdate, boolean updateOnModification) {
        super(productListRealmResults, autoUpdate, updateOnModification);
        this.productListRealmResults = productListRealmResults;
        this.context = context;
    }

    @NonNull
    @Override
    public ProductShowcaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_showcase_product, parent,false);
        ProductShowcaseViewHolder holder = new ProductShowcaseViewHolder(view);
        //ButterKnife.bind(this, view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ProductShowcaseViewHolder holder, final int position) {

        ProductList payment = productListRealmResults.get(position);
        if (payment != null){
            holder.tvTitleLeftBottom.setText(context.getResources().getString(R.string.quantity));
            holder.tvProductShowcaseCondition.setText(payment.getCondition() == 1 ? context.getResources().getString(R.string.newmay) : context.getResources().getString(R.string.usedmay));
            holder.tvProductShowcaseName.setText(payment.getName());
            String price = NumberFormat.getCurrencyInstance(Locale.getDefault()).format(payment.getPurchaseValue()) + " " + "(x" + payment.getQuantity() + ")";
            String quantity = payment.getQuantity() + "";
            holder.tvProductShowcasePrice.setText(quantity);
            String total = NumberFormat.getCurrencyInstance(Locale.getDefault()).format(payment.getPurchaseValue() * payment.getQuantity());
            holder.tvProductShowcaseTotal.setText(total);
            holder.tvSellDate.setVisibility(View.VISIBLE);
            holder.tvSellDate.setText(DateUtil.convertDate(payment.getSaleDate().replace(".000Z", ""),"dd-MM-yyyy"));

        }
    }

    @Override
    public int getItemCount() {
        return productListRealmResults.size();
    }

    class ProductShowcaseViewHolder extends RecyclerView.ViewHolder {

        TextView tvProductShowcaseCondition;
        TextView tvProductShowcaseName;
        TextView tvProductShowcasePrice;
        TextView tvProductShowcaseTotal;
        TextView tvTitleLeftBottom;
        TextView tvSellDate;



        ProductShowcaseViewHolder(View itemView) {
            super(itemView);
            tvProductShowcaseTotal = itemView.findViewById(R.id.tvProductShowcaseTotal);
            tvProductShowcasePrice = itemView.findViewById(R.id.tvProductShowcasePrice);
            tvProductShowcaseCondition = itemView.findViewById(R.id.tvProductShowcaseCondition);
            tvProductShowcaseName = itemView.findViewById(R.id.tvProductShowcaseName);
            tvTitleLeftBottom = itemView.findViewById(R.id.tvTitleLeftBottom);
            tvSellDate = itemView.findViewById(R.id.tvSellDate);
        }
    }
}
