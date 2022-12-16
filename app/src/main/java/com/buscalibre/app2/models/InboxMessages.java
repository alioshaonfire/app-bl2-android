
package com.buscalibre.app2.models;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;

public class InboxMessages extends RealmObject {

    @SerializedName("blstatus")
    @Expose
    private Integer blstatus;
    @SerializedName("blmessage")
    @Expose
    private String blmessage;
    @SerializedName("messageList")
    @Expose
    private RealmList<MessageList> messageList = null;
    @SerializedName("pendingRows")
    @Expose
    private Boolean pendingRows;

    public Integer getBlstatus() {
        return blstatus;
    }

    public void setBlstatus(Integer blstatus) {
        this.blstatus = blstatus;
    }

    public String getBlmessage() {
        return blmessage;
    }

    public void setBlmessage(String blmessage) {
        this.blmessage = blmessage;
    }

    public RealmList<MessageList> getMessageList() {
        return messageList;
    }

    public void setMessageList(RealmList<MessageList> messageList) {
        this.messageList = messageList;
    }

    public Boolean getPendingRows() {
        return pendingRows;
    }

    public void setPendingRows(Boolean pendingRows) {
        this.pendingRows = pendingRows;
    }

    public static void deleteAll(Realm realm){
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.where(MessageList.class).findAll().deleteAllFromRealm();
                realm.where(InboxMessages.class).findAll().deleteAllFromRealm();

            }
        });
    }
}
