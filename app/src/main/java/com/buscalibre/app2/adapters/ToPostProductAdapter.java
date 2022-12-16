package com.buscalibre.app2.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.buscalibre.app2.R;
import com.buscalibre.app2.activities.OrderDetailActivity;
import com.buscalibre.app2.models.Anulados;
import com.buscalibre.app2.models.Order;
import com.buscalibre.app2.models.Pendientes;
import com.buscalibre.app2.models.PorRetirar;
import com.buscalibre.app2.models.ProductList;
import com.buscalibre.app2.models.ProductStatus;
import com.buscalibre.app2.models.Recibidos;
import com.buscalibre.app2.util.DateUtil;

import java.text.NumberFormat;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;

public class ToPostProductAdapter extends RealmRecyclerViewAdapter<Order, ToPostProductAdapter.ProductShowcaseViewHolder> {

    private RealmResults<Order> orderRealmResults;
    private Context context;
    private Realm realm = Realm.getDefaultInstance();


    public ToPostProductAdapter(@Nullable RealmResults<Order> orderRealmResults, Context context, boolean autoUpdate, boolean updateOnModification) {
        super(orderRealmResults, autoUpdate, updateOnModification);
        this.orderRealmResults = orderRealmResults;
        this.context = context;
    }

    @NonNull
    @Override
    public ProductShowcaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_to_post_product, parent,false);
        ProductShowcaseViewHolder holder = new ProductShowcaseViewHolder(view);
        //ButterKnife.bind(this, view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ProductShowcaseViewHolder holder, final int position) {

        Order order = orderRealmResults.get(position);
        if (order != null){
            holder.tvOrderPostProd.setText(order.getOrderId());
            holder.tvDatePostProd.setText(DateUtil.convertDate(order.getDate().replace(".000Z", ""), "dd MMMM yyyy").toUpperCase());
            ProductStatus productStatus = order.getProductStatus();
            if (productStatus != null){
                Recibidos recibidos = productStatus.getRecibidos();
                if (recibidos != null){
                    holder.llRecibidos.setVisibility(View.VISIBLE);
                    holder.tvRecibidosOrderPostProd.setText(" " + recibidos.getCount() + "(" + NumberFormat.getCurrencyInstance(Locale.getDefault()).format(recibidos.getTotal()) + ")");
                }

                PorRetirar porRetirar = productStatus.getPorRetirar();
                if (porRetirar != null){
                    holder.llPorRetirar.setVisibility(View.VISIBLE);
                    holder.tvPorRetirarOrderPostProd.setText(" " + porRetirar.getCount() + "(" + NumberFormat.getCurrencyInstance(Locale.getDefault()).format(porRetirar.getTotal()) + ")");
                }

                Anulados anulados = productStatus.getAnulados();
                if (anulados != null){
                    holder.llAnulados.setVisibility(View.VISIBLE);
                    holder.tvAnuladosOrderPostProd.setText(" " + anulados.getCount() + "(" + NumberFormat.getCurrencyInstance(Locale.getDefault()).format(anulados.getTotal()) + ")");
                }
                Pendientes pendientes = productStatus.getPendientes();
                if (pendientes != null){
                    holder.llPendientes.setVisibility(View.VISIBLE);
                    holder.tvPendientesOrderPostProd.setText(" " + pendientes.getCount() + "(" + NumberFormat.getCurrencyInstance(Locale.getDefault()).format(pendientes.getTotal()) + ")");
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return orderRealmResults.size();
    }

    class ProductShowcaseViewHolder extends RecyclerView.ViewHolder {

        TextView tvDatePostProd, tvOrderPostProd, tvRecibidosOrderPostProd, tvPorRetirarOrderPostProd,
                tvAnuladosOrderPostProd, tvPendientesOrderPostProd;
        LinearLayout llRecibidos, llPorRetirar, llAnulados, llPendientes;




        ProductShowcaseViewHolder(View itemView) {
            super(itemView);
            tvDatePostProd = itemView.findViewById(R.id.tvDatePostProd);
            tvOrderPostProd = itemView.findViewById(R.id.tvOrderPostProd);
            tvRecibidosOrderPostProd = itemView.findViewById(R.id.tvRecibidosOrderPostProd);
            llRecibidos = itemView.findViewById(R.id.llRecibidos);
            llPorRetirar = itemView.findViewById(R.id.llPorRetirar);
            tvPorRetirarOrderPostProd = itemView.findViewById(R.id.tvPorRetirarOrderPostProd);
            tvAnuladosOrderPostProd = itemView.findViewById(R.id.tvAnuladosOrderPostProd);
            llAnulados = itemView.findViewById(R.id.llAnulados);
            llPendientes = itemView.findViewById(R.id.llPendientes);
            tvPendientesOrderPostProd = itemView.findViewById(R.id.tvPendientesOrderPostProd);

            itemView.setOnClickListener(v -> {
                Order order = orderRealmResults.get(getAdapterPosition());
                if (order != null){
                    Intent intent = new Intent(context, OrderDetailActivity.class);
                    intent.putExtra("orderId", order.getOrderId());
                    context.startActivity(intent);
                }
            });
        }
    }
}
