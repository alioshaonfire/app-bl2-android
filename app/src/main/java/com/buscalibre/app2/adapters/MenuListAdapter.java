    package com.buscalibre.app2.adapters;

    import android.app.Activity;
    import android.content.Context;
    import android.content.Intent;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.ImageView;
    import android.widget.TextView;
    import android.widget.Toast;

    import androidx.annotation.NonNull;
    import androidx.annotation.Nullable;
    import androidx.recyclerview.widget.RecyclerView;

    import com.buscalibre.app2.R;
    import com.buscalibre.app2.activities.BaseWebViewActivity;
    import com.buscalibre.app2.activities.InboxActivity;
    import com.buscalibre.app2.activities.MenuSellerActivity;
    import com.buscalibre.app2.activities.PaymentMethodsActivity;
    import com.buscalibre.app2.activities.SelectCountryActivity;
    import com.buscalibre.app2.events.BaseWebViewEvent;
    import com.buscalibre.app2.events.SelectPaymentMethodEvent;
    import com.buscalibre.app2.models.Country;
    import com.buscalibre.app2.models.MenuList;
    import com.buscalibre.app2.models.UserLogin;
    import com.buscalibre.app2.network.NetworkToken;

    import org.greenrobot.eventbus.EventBus;

    import io.realm.Realm;
    import io.realm.RealmRecyclerViewAdapter;
    import io.realm.RealmResults;

    public class MenuListAdapter extends RealmRecyclerViewAdapter<MenuList, MenuListAdapter.MenuListViewHolder> {

        private RealmResults<MenuList> menuListRealmResults;
        private Context context;
        private Realm realm = Realm.getDefaultInstance();
        private final int PAYMENT_METHODS = 0;
        private final int INBOX = 1;
        private final int SELL_BOOKS = 2;
        private final int CHANGE_COUNTRY = 3;
        private final int HELP_SUPPORT = 4;
        private final int LOGOUT = 5;




        public MenuListAdapter(@Nullable RealmResults<MenuList> menuListRealmResults, Context context, boolean autoUpdate, boolean updateOnModification) {
            super(menuListRealmResults, autoUpdate, updateOnModification);
            this.menuListRealmResults = menuListRealmResults;
            this.context = context;
        }

        @NonNull
        @Override
        public MenuListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_menu_list, parent,false);
            MenuListViewHolder holder = new MenuListViewHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull MenuListViewHolder holder, final int position) {

            MenuList menuList = menuListRealmResults.get(position);
            if (menuList != null){
                int id = menuList.getID();
                holder.tvMenuListName.setText(menuList.getName());

                if (PAYMENT_METHODS == id){
                    holder.ivMenuImage.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_payment_methods));
                }
                if (INBOX == id){
                    holder.ivMenuImage.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_inbox_messages));

                }
                if (SELL_BOOKS == id){
                    holder.ivMenuImage.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_sell_books));
                }
                if (CHANGE_COUNTRY == id){
                    holder.ivMenuImage.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_flag));
                }
                if (HELP_SUPPORT == id){
                    holder.ivMenuImage.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_help));
                }
                if (LOGOUT == id){
                    holder.ivMenuImage.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_logout));
                }
            }

        }

        @Override
        public int getItemCount() {
            return menuListRealmResults.size();
        }

        class MenuListViewHolder extends RecyclerView.ViewHolder {


            TextView tvMenuListName;
            ImageView ivMenuImage;



            MenuListViewHolder(View itemView) {
                super(itemView);
                tvMenuListName = itemView.findViewById(R.id.tvMenuListName);
                ivMenuImage = itemView.findViewById(R.id.ivMenuImage);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        UserLogin userLogin = realm.where(UserLogin.class).findFirst();

                        Country country = realm.where(Country.class).equalTo("isSelected", true).findFirst();
                       MenuList menuList = menuListRealmResults.get(getAdapterPosition());
                       if (menuList != null){
                           int ID = menuList.getID();
                           if (ID == HELP_SUPPORT){
                               if(country.getUrl().getHelp() != null){
                                   /*Intent intent = new Intent(context, BaseWebViewActivity.class);
                                   intent.putExtra("url", country.getUrl().getHelp());
                                   intent.putExtra("header", userLogin.getWebToken());
                                   intent.putExtra("title", context.getString(R.string.text45));
                                   intent.putExtra("hasCart", true);
                                   ((Activity)context).startActivity(intent);*/
                                   EventBus.getDefault().post(new BaseWebViewEvent(country.getUrl().getHelp(), userLogin.getWebToken(), true, context.getString(R.string.text45), null, null));

                               }else{
                                   Toast.makeText(context, context.getString(R.string.text46), Toast.LENGTH_LONG).show();
                               }
                           }else if (ID == LOGOUT){
                               NetworkToken.refresh(context);
                           }else if (ID == CHANGE_COUNTRY){
                               Intent intent = new Intent(context, SelectCountryActivity.class);
                               ((Activity)context).startActivity(intent);
                           }else if (ID == INBOX){
                               Intent intent = new Intent(context, InboxActivity.class);
                               ((Activity)context).startActivity(intent);
                           }else if (ID == PAYMENT_METHODS){
                               EventBus.getDefault().post(new SelectPaymentMethodEvent(true));
                               /*Intent intent = new Intent(context, PaymentMethodsActivity.class);
                               ((Activity)context).startActivity(intent);*/
                           }
                           else if (ID == SELL_BOOKS){
                               Intent intent = new Intent(context, MenuSellerActivity.class);
                               ((Activity)context).startActivity(intent);
                           }
                       }
                    }
                });
            }
        }
    }
