package com.buscalibre.app2.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.binpar.bibooks.sdk.SDKBibooks;
import com.buscalibre.app2.R;
import com.buscalibre.app2.constants.AppConstants;
import com.buscalibre.app2.events.RefreshEbookDownloadEvent;
import com.buscalibre.app2.models.EbookList;
import com.buscalibre.app2.models.LocalEbook;
import com.buscalibre.app2.models.UserLogin;
import com.buscalibre.app2.network.NetworkToken;
import com.buscalibre.app2.util.CheckStatus;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;

public class EbookListAdapter extends RealmRecyclerViewAdapter<EbookList, EbookListAdapter.ProductShowcaseViewHolder> {

    private RealmResults<EbookList> ebookListRealmResults;
    private Context context;
    private Realm realm = Realm.getDefaultInstance();


    public EbookListAdapter(@Nullable RealmResults<EbookList> ebookListRealmResults, Context context, boolean autoUpdate, boolean updateOnModification) {
        super(ebookListRealmResults, autoUpdate, updateOnModification);
        this.ebookListRealmResults = ebookListRealmResults;
        this.context = context;
    }

    @NonNull
    @Override
    public ProductShowcaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_ebooklist, parent,false);
        ProductShowcaseViewHolder holder = new ProductShowcaseViewHolder(view);
        //ButterKnife.bind(this, view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ProductShowcaseViewHolder holder, final int position) {

        EbookList ebookList = ebookListRealmResults.get(position);
        if (ebookList != null){
            holder.tvEbookName.setText(ebookList.getName());
            holder.tvEbookAuthor.setText(ebookList.getAuthor());
            holder.tvEbookIsbn.setText(ebookList.getIsbn().toString());
            Picasso.get()
                    .load(ebookList.getImageURL())
                    .fit()
                    .centerCrop()
                    .placeholder(R.drawable.preload_menu)
                    .into(holder.ivEbookImage);
            holder.ivPlayEbook.bringToFront();
            LocalEbook localEbook = realm.where(LocalEbook.class).equalTo("id", ebookList.getId()).findFirst();
            if (localEbook != null){
                holder.ivPlayEbook.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_ebook_delete));
            }else {
                holder.ivPlayEbook.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_download));
            }
            if (ebookList.isDownloading()){
                holder.pbDownload.setVisibility(View.VISIBLE);
            }else {
                holder.pbDownload.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return ebookListRealmResults.size();
    }

    class ProductShowcaseViewHolder extends RecyclerView.ViewHolder {

        TextView tvEbookAuthor;
        TextView tvEbookName;
        TextView tvEbookIsbn;
        TextView tvProductShowcaseTotal;
        ImageView ivEbookImage;
        ImageView ivPlayEbook;
        ProgressBar pbDownload;


        ProductShowcaseViewHolder(View itemView) {
            super(itemView);
            tvProductShowcaseTotal = itemView.findViewById(R.id.tvProductShowcaseTotal);
            tvEbookIsbn = itemView.findViewById(R.id.tvAudiobookIsbn);
            tvEbookAuthor = itemView.findViewById(R.id.tvAudiobookAuthor);
            tvEbookName = itemView.findViewById(R.id.tvAudiobookName);
            ivEbookImage = itemView.findViewById(R.id.ivAudiobookImage);
            ivPlayEbook = itemView.findViewById(R.id.ivPlayEbook);
            pbDownload = itemView.findViewById(R.id.pbDownload);

            itemView.setOnClickListener(v -> {
                EbookList ebookList = ebookListRealmResults.get(getAdapterPosition());
                if (ebookList.isDownloading()){
                    return;
                }
                UserLogin userLogin = realm.where(UserLogin.class).findFirst();
                assert userLogin != null;
                if (userLogin.getEbookToken() == null){
                    NetworkToken.refresh(context);
                    return;
                }
                Log.e("idBOOK", ebookListRealmResults.get(getAdapterPosition()).getId());
                Log.e("userEbookToken", userLogin.getEbookToken());
                SDKBibooks.startViewer(context, ebookListRealmResults.get(getAdapterPosition()).getIsbn(), userLogin.getEbookToken(), AppConstants.IS_PREVIEW, "es", 0f);
            });
            ivPlayEbook.setOnClickListener(v -> {

                EbookList ebookList = ebookListRealmResults.get(getAdapterPosition());
                if (ebookList.isDownloading()){
                    return;
                }
                LocalEbook localEbook = realm.where(LocalEbook.class).equalTo("id", ebookList.getId()).findFirst();

                if (localEbook != null){
                    UserLogin userLogin = realm.where(UserLogin.class).findFirst();
                    if (userLogin != null){
                        SDKBibooks.initDownloadManager(context,userLogin.getEbookToken(),ebookList.getIsbn()).deleteDownload();
                        realm.executeTransaction(realm -> {
                            ebookList.setDownloadCompleted(false);
                            ebookList.setDownloading(false);
                            localEbook.deleteFromRealm();
                        });
                    }else {
                        CheckStatus.userLogin(realm, context);
                    }

                }else {

                    EventBus.getDefault().post(new RefreshEbookDownloadEvent(ebookListRealmResults.get(getAdapterPosition())));
                    Log.e("idBOOK", ebookListRealmResults.get(getAdapterPosition()).getId());
                }
            });
        }
    }
}
