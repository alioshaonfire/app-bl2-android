
package com.buscalibre.app2.models;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;

public class Ebook extends RealmObject {

    @SerializedName("blstatus")
    @Expose
    private Integer blstatus;
    @SerializedName("blmessage")
    @Expose
    private String blmessage;
    @SerializedName("ebookList")
    @Expose
    private RealmList<EbookList> ebookList = null;
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

    public RealmList<EbookList> getEbookList() {
        return ebookList;
    }

    public void setEbookList(RealmList<EbookList> ebookList) {
        this.ebookList = ebookList;
    }

    public Boolean getPendingRows() {
        return pendingRows;
    }

    public void setPendingRows(Boolean pendingRows) {
        this.pendingRows = pendingRows;
    }

    public static void deleteAll(Realm realm, int type){
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                if(type != 0){
                    realm.where(EbookList.class).equalTo("type", type).findAll().deleteAllFromRealm();
                }else {
                    realm.where(EbookList.class).findAll().deleteAllFromRealm();
                }
                realm.where(Ebook.class).findAll().deleteAllFromRealm();
            }
        });
    }

}
