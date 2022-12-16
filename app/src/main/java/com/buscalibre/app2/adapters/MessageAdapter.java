package com.buscalibre.app2.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.buscalibre.app2.R;
import com.buscalibre.app2.activities.MessageDetailActivity;
import com.buscalibre.app2.events.MessageSelectedEvent;
import com.buscalibre.app2.models.MessageList;
import com.buscalibre.app2.util.DateUtil;

import org.greenrobot.eventbus.EventBus;

import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;

public class MessageAdapter extends RealmRecyclerViewAdapter<MessageList, MessageAdapter.MessageViewHolder> {

    private RealmResults<MessageList> messageListRealmResults;
    private Context context;
    private Realm realm = Realm.getDefaultInstance();


    public MessageAdapter(@Nullable RealmResults<MessageList> messageListRealmResults, Context context, boolean autoUpdate, boolean updateOnModification) {
        super(messageListRealmResults, autoUpdate, updateOnModification);
        this.messageListRealmResults = messageListRealmResults;
        this.context = context;
    }

    @NonNull
    @Override
    public MessageAdapter.MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_message, parent,false);
        MessageAdapter.MessageViewHolder holder = new MessageAdapter.MessageViewHolder(view);
        //ButterKnife.bind(this, view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.MessageViewHolder holder, final int position) {

        MessageList messageList = messageListRealmResults.get(position);
        if (messageList != null){
            if (messageList.getRead()){
                holder.cbIsSelected.setVisibility(View.INVISIBLE);
                holder.tvDateMessage.setTypeface(null, Typeface.NORMAL);
                holder.tvTitleMessage.setTypeface(null, Typeface.NORMAL);
                holder.tvBodyMessage.setTypeface(null, Typeface.NORMAL);
                holder.ivMessageRow.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_read_message));
            }else {
                holder.cbIsSelected.setVisibility(View.VISIBLE);
                holder.tvDateMessage.setTypeface(null, Typeface.BOLD);
                holder.tvTitleMessage.setTypeface(null, Typeface.BOLD);
                holder.tvBodyMessage.setTypeface(null, Typeface.BOLD);
                holder.ivMessageRow.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_unread_message));
            }
            if (messageList.getReadSelected()){
                holder.cbIsSelected.setChecked(true);
            }else {
                holder.cbIsSelected.setChecked(false);
            }
            holder.tvDateMessage.setText(DateUtil.convertDate(messageList.getDate().replace(".000Z", ""),"dd MMMM, HH:mm") + " Hrs.");
            holder.tvTitleMessage.setText(messageList.getTitle());
            holder.tvBodyMessage.setText(Html.fromHtml(messageList.getBody(), Html.FROM_HTML_MODE_COMPACT));

        }
    }

    @Override
    public int getItemCount() {
        return messageListRealmResults.size();
    }

    class MessageViewHolder extends RecyclerView.ViewHolder {

/*        @BindView(R.id.tvPartyName)
        TextView tvPartyName;*/
        TextView tvBodyMessage;
        TextView tvTitleMessage;
        TextView tvDateMessage;
        ImageView ivMessageRow;
        CheckBox cbIsSelected;

        MessageViewHolder(View itemView) {
            super(itemView);
            ivMessageRow = itemView.findViewById(R.id.ivMessageRow);
            tvDateMessage = itemView.findViewById(R.id.tvDateMessage);
            tvTitleMessage = itemView.findViewById(R.id.tvTitleMessage);
            tvBodyMessage = itemView.findViewById(R.id.tvBodyMessage);
            cbIsSelected = itemView.findViewById(R.id.cbIsSelected);
            cbIsSelected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    MessageList messageList = messageListRealmResults.get(getAdapterPosition());
                    if (!messageList.getRead() && !messageList.getReadSelected()){
                        EventBus.getDefault().post(new MessageSelectedEvent(messageList, isChecked));
                    }else if (!messageList.getRead() && messageList.getReadSelected()){
                        EventBus.getDefault().post(new MessageSelectedEvent(messageList, isChecked));
                    }
                    cbIsSelected.setChecked(isChecked);
                }
            });
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, MessageDetailActivity.class);
                    intent.putExtra("id", messageListRealmResults.get(getAdapterPosition()).getId());
                    context.startActivity(intent);
                }
            });
        }
    }
}
