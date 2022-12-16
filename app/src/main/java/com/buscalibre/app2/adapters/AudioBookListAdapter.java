    package com.buscalibre.app2.adapters;

    import android.content.Context;
    import android.util.Log;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.ImageView;
    import android.widget.ProgressBar;
    import android.widget.TextView;
    import android.widget.Toast;

    import androidx.annotation.NonNull;
    import androidx.annotation.Nullable;
    import androidx.recyclerview.widget.RecyclerView;

    import com.binpar.bibooks.sdk.SDKBibooks;
    import com.buscalibre.app2.R;
    import com.buscalibre.app2.constants.AppConstants;
    import com.buscalibre.app2.events.RefreshAudiobookDownloadEvent;
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

public class AudioBookListAdapter extends RealmRecyclerViewAdapter<EbookList, AudioBookListAdapter.ProductShowcaseViewHolder> {

    private RealmResults<EbookList> ebookListRealmResults;
    private Context context;
    private Realm realm = Realm.getDefaultInstance();


    public AudioBookListAdapter(@Nullable RealmResults<EbookList> ebookListRealmResults, Context context, boolean autoUpdate, boolean updateOnModification) {
        super(ebookListRealmResults, autoUpdate, updateOnModification);
        this.ebookListRealmResults = ebookListRealmResults;
        this.context = context;
    }

    @NonNull
    @Override
    public ProductShowcaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_audiobooklist, parent,false);
        ProductShowcaseViewHolder holder = new ProductShowcaseViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ProductShowcaseViewHolder holder, final int position) {

        EbookList ebookList = ebookListRealmResults.get(position);
        if (ebookList != null){
            holder.tvAudiobookName.setText(ebookList.getName());
            holder.tvAudiobookAuthor.setText(ebookList.getAuthor());
            holder.tvAudiobookIsbn.setText(ebookList.getIsbn().toString());
            Picasso.get()
                    .load(ebookList.getImageURL())
                    .fit()
                    .centerCrop()
                    .placeholder(R.drawable.preload_menu)
                    .into(holder.ivAudiobookImage);
        }
        holder.ivPlayAudioEbook.bringToFront();
        LocalEbook localEbook = realm.where(LocalEbook.class).equalTo("id", ebookList.getId()).findFirst();
        if (localEbook != null){
            holder.ivPlayAudioEbook.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_ebook_delete));
        }else {
            holder.ivPlayAudioEbook.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_download));
        }
        if (ebookList.isDownloading()){
            holder.pbDownload.setVisibility(View.VISIBLE);
        }else {
            holder.pbDownload.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return ebookListRealmResults.size();
    }

    class ProductShowcaseViewHolder extends RecyclerView.ViewHolder {

        TextView tvAudiobookAuthor;
        TextView tvAudiobookName;
        TextView tvAudiobookIsbn;
        ImageView ivAudiobookImage;
        ImageView ivPlayAudioEbook;
        ProgressBar pbDownload;


        ProductShowcaseViewHolder(View itemView) {
            super(itemView);
            tvAudiobookIsbn = itemView.findViewById(R.id.tvAudiobookIsbn);
            tvAudiobookAuthor = itemView.findViewById(R.id.tvAudiobookAuthor);
            tvAudiobookName = itemView.findViewById(R.id.tvAudiobookName);
            ivAudiobookImage = itemView.findViewById(R.id.ivAudiobookImage);
            ivPlayAudioEbook = itemView.findViewById(R.id.ivPlayAudioEbook);
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

                Log.e("idAudiobook", ebookList.getId());
                Log.e("userEbookToken", userLogin.getEbookToken());
                SDKBibooks.startViewer(context, ebookList.getIsbn(), userLogin.getEbookToken(), AppConstants.IS_PREVIEW, "es", 0f);

            });
            ivPlayAudioEbook.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

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

                        EventBus.getDefault().post(new RefreshAudiobookDownloadEvent(ebookList));
                        Log.e("idAudioBOOK", ebookList.getId());
                    }
                }
            });
        }
    }
}
