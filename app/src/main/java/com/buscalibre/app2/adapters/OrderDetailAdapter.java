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
import com.buscalibre.app2.models.Product_;

import java.text.NumberFormat;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;

public class OrderDetailAdapter extends RealmRecyclerViewAdapter<Product_, OrderDetailAdapter.ProductViewHolder>{

    private RealmResults<Product_> product_realmResults;
    private Context context;
    private Realm realm = Realm.getDefaultInstance();


    public OrderDetailAdapter(@Nullable RealmResults<Product_> product_realmResults, Context context, boolean autoUpdate, boolean updateOnModification) {
        super(product_realmResults, autoUpdate, updateOnModification);
        this.product_realmResults = product_realmResults;
        this.context = context;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_product_detail, parent,false);
        ProductViewHolder holder = new ProductViewHolder(view);
        //ButterKnife.bind(this, view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, final int position) {

        Product_ product_ = product_realmResults.get(position);
        if (product_ != null){
            holder.tvProductNameDetail.setText(product_.getName());
            holder.tvProdPrice.setText(NumberFormat.getCurrencyInstance(Locale.getDefault()).format(product_.getPurchaseValue()));
            holder.tvProdStatus.setText(product_.getStatusText());
        }
    }



    @Override
    public int getItemCount() {
        return product_realmResults.size();
    }


    class ProductViewHolder extends RecyclerView.ViewHolder {

        TextView tvProductNameDetail,tvProdPrice, tvProdStatus;

        ProductViewHolder(View itemView) {
            super(itemView);

            tvProductNameDetail = itemView.findViewById(R.id.tvProductNameDetail);
            tvProdPrice = itemView.findViewById(R.id.tvProdPrice);
            tvProdStatus = itemView.findViewById(R.id.tvProdStatus);
        }
    }
}
