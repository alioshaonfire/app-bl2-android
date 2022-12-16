package com.buscalibre.app2.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.buscalibre.app2.R;
import com.buscalibre.app2.constants.AppConfig;
import com.buscalibre.app2.events.RefreshProductsEvent;
import com.buscalibre.app2.models.Product;
import com.buscalibre.app2.util.AlertDialogHelper;
import com.buscalibre.app2.util.CircleTransform;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import java.text.NumberFormat;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;

public class SellProductAdapter extends RealmRecyclerViewAdapter<Product, SellProductAdapter.ProductViewHolder> implements AlertDialogHelper.AlertDialogListener {

    private RealmResults<Product> productRealmResults;
    private Context context;
    private Realm realm = Realm.getDefaultInstance();
    private AlertDialogHelper alertDialogHelper;
    private Product currentProduct;


    public SellProductAdapter(@Nullable RealmResults<Product> productRealmResults, Context context, boolean autoUpdate, boolean updateOnModification) {
        super(productRealmResults, autoUpdate, updateOnModification);
        this.productRealmResults = productRealmResults;
        this.context = context;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_sell_product, parent,false);
        ProductViewHolder holder = new ProductViewHolder(view);
        //ButterKnife.bind(this, view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, final int position) {

        Product product = productRealmResults.get(position);
        if (product != null){
            holder.tvProductName.setText(product.getName());
            holder.tvProductDesc.setText(product.getDescription());
            //holder.tvQtyProduct.setText("x " + product.getQuantity());
            String productPrice = NumberFormat.getCurrencyInstance(Locale.getDefault()).format(product.getPrice()) + " (x" + product.getQuantity() + ")";
            holder.tvUnitProductPrice.setText(productPrice);
           /* if (product.isUsed()){
                holder.tvConditionProduct.setText(context.getResources().getString(R.string.usedmay));
            }else {
                holder.tvConditionProduct.setText(context.getResources().getString(R.string.newmay));
            }*/
            float total = product.getQuantity() * product.getPrice();
            holder.tvTotalByProduct.setText(NumberFormat.getCurrencyInstance(Locale.getDefault()).format(total));
            if (product.getImageURL() != null && !product.getImageURL().isEmpty()){
                Picasso.get()
                        .load(product.getImageURL())
                        .fit()
                        .centerCrop()
                        .placeholder(R.drawable.preload_menu)
                        .into(holder.ivBookDesc);
            }
            holder.ivDeleteBook.setOnClickListener(v -> {
                //currentProduct = product;
                //alertDialogHelper.showAlertDialog(context.getString(R.string.app_name),context.getString(R.string.text123),context.getString(R.string.accept_dialog),context.getString(R.string.cancel_dialog),"",1, false);
                showDialog(product);

            });
        }
    }

    private void showDialog(Product product) {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.AlertDialogCustom));
        builder.setTitle(context.getResources().getString(R.string.app_name))
                .setMessage(context.getString(R.string.text123))
                .setPositiveButton(context.getString(R.string.accept_dialog), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        realm.executeTransaction(realm -> product.deleteFromRealm());
                        EventBus.getDefault().post(new RefreshProductsEvent(true));
                    }
                })
                .setNegativeButton(context.getString(R.string.cancel_dialog), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    @Override
    public int getItemCount() {
        return productRealmResults.size();
    }

    @Override
    public void onPositiveClick(int from) {
        if (from == 1){
            realm.executeTransaction(realm -> currentProduct.deleteFromRealm());
            EventBus.getDefault().post(new RefreshProductsEvent(true));
        }
    }

    @Override
    public void onNegativeClick(int from) {

    }

    @Override
    public void onNeutralClick(int from) {

    }

    class ProductViewHolder extends RecyclerView.ViewHolder {

/*        @BindView(R.id.tvPartyName)
        TextView tvPartyName;*/
        TextView tvUnitProductPrice;
        TextView tvProductName;
        ImageView ivBookDesc;
        TextView tvProductDesc;
        ImageView ivDeleteBook;
        //TextView tvQtyProduct;
       // TextView tvConditionProduct;
        TextView tvTotalByProduct;


        ProductViewHolder(View itemView) {
            super(itemView);
            tvTotalByProduct = itemView.findViewById(R.id.tvTotalByProduct);
            tvUnitProductPrice = itemView.findViewById(R.id.tvUnitProductPrice);
            ivBookDesc = itemView.findViewById(R.id.ivBookDesc);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductDesc = itemView.findViewById(R.id.tvProductDesc);
            ivDeleteBook = itemView.findViewById(R.id.ivDeleteBook);
            //tvQtyProduct = itemView.findViewById(R.id.tvQtyProduct);
            //tvConditionProduct = itemView.findViewById(R.id.tvConditionProduct);
            itemView.setOnClickListener(v -> {

            });
            alertDialogHelper = new AlertDialogHelper(context);
        }
    }
}
