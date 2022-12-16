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

import java.text.NumberFormat;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;

public class ShowcaseProductAdapter extends RealmRecyclerViewAdapter<ProductList, ShowcaseProductAdapter.ProductShowcaseViewHolder> {

    private RealmResults<ProductList> productListRealmResults;
    private Context context;
    private Realm realm = Realm.getDefaultInstance();


    public ShowcaseProductAdapter(@Nullable RealmResults<ProductList> productListRealmResults, Context context, boolean autoUpdate, boolean updateOnModification) {
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

        ProductList productList = productListRealmResults.get(position);
        if (productList != null){
            //holder.tvProductShowcaseCondition.setText(productList.getCondition() == 1 ? context.getResources().getString(R.string.newmay) : context.getResources().getString(R.string.usedmay));
            holder.tvProductShowcaseName.setText(productList.getName());
            String price = NumberFormat.getCurrencyInstance(Locale.getDefault()).format(productList.getPurchaseValue()) + " " + "(x" + productList.getQuantity() + ")";
            holder.tvProductShowcasePrice.setText(price);
            String total = NumberFormat.getCurrencyInstance(Locale.getDefault()).format(productList.getPurchaseValue() * productList.getQuantity());
            holder.tvProductShowcaseTotal.setText(total);
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



        ProductShowcaseViewHolder(View itemView) {
            super(itemView);
            tvProductShowcaseTotal = itemView.findViewById(R.id.tvProductShowcaseTotal);
            tvProductShowcasePrice = itemView.findViewById(R.id.tvProductShowcasePrice);
            tvProductShowcaseCondition = itemView.findViewById(R.id.tvProductShowcaseCondition);
            tvProductShowcaseName = itemView.findViewById(R.id.tvProductShowcaseName);

            itemView.setOnClickListener(v -> {

            });
        }
    }
}
