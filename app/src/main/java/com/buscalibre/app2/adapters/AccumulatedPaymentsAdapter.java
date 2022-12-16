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
import com.buscalibre.app2.models.Payment;
import com.buscalibre.app2.models.ProductList_;

import java.text.NumberFormat;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;

public class AccumulatedPaymentsAdapter extends RealmRecyclerViewAdapter<ProductList_, AccumulatedPaymentsAdapter.ProductShowcaseViewHolder> {

    private RealmResults<ProductList_> productList_realmResults;
    private Context context;
    private Realm realm = Realm.getDefaultInstance();


    public AccumulatedPaymentsAdapter(@Nullable RealmResults<ProductList_> productList_realmResults, Context context, boolean autoUpdate, boolean updateOnModification) {
        super(productList_realmResults, autoUpdate, updateOnModification);
        this.productList_realmResults = productList_realmResults;
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

        ProductList_ productList_ = productList_realmResults.get(position);
        if (productList_ != null){
            holder.tvTitleLeftBottom.setText(context.getResources().getString(R.string.quantity));
            //holder.tvProductShowcaseCondition.setText(productList_.getCondition() == 1 ? context.getResources().getString(R.string.newmay) : context.getResources().getString(R.string.usedmay));
            holder.tvProductShowcaseName.setText(productList_.getName());
            String price = NumberFormat.getCurrencyInstance(Locale.getDefault()).format(productList_.getPurchaseValue()) + " " + "(x" + productList_.getQuantity() + ")";
            String quantity = productList_.getQuantity() + "";
            holder.tvProductShowcasePrice.setText(quantity);
            String total = NumberFormat.getCurrencyInstance(Locale.getDefault()).format(productList_.getPurchaseValue() * productList_.getQuantity());
            holder.tvProductShowcaseTotal.setText(total);
            holder.tvTotalTitle.setText(R.string.total_date);
            //holder.tvSellDate.setVisibility(View.VISIBLE);
            //holder.tvSellDate.setText(DateUtil.convertDate(productList_.getSaleDate().replace(".000Z", ""),"dd-MM-yyyy"));

        }
    }

    @Override
    public int getItemCount() {
        return productList_realmResults.size();
    }

    class ProductShowcaseViewHolder extends RecyclerView.ViewHolder {

        TextView tvProductShowcaseCondition;
        TextView tvProductShowcaseName;
        TextView tvProductShowcasePrice;
        TextView tvProductShowcaseTotal;
        TextView tvTitleLeftBottom, tvTotalTitle;
        //TextView tvSellDate;



        ProductShowcaseViewHolder(View itemView) {
            super(itemView);
            tvTotalTitle = itemView.findViewById(R.id.tvTotalTitle);
            tvProductShowcaseTotal = itemView.findViewById(R.id.tvProductShowcaseTotal);
            tvProductShowcasePrice = itemView.findViewById(R.id.tvProductShowcasePrice);
            tvProductShowcaseCondition = itemView.findViewById(R.id.tvProductShowcaseCondition);
            tvProductShowcaseName = itemView.findViewById(R.id.tvProductShowcaseName);
            tvTitleLeftBottom = itemView.findViewById(R.id.tvTitleLeftBottom);
            //tvSellDate = itemView.findViewById(R.id.tvSellDate);
        }
    }
}
