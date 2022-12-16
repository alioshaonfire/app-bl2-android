package com.buscalibre.app2.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.Realm;
import io.realm.RealmObject;

public class SellerInfo extends RealmObject {

    @SerializedName("blstatus")
    @Expose
    private Integer blstatus;
    @SerializedName("blmessage")
    @Expose
    private String blmessage;
    @SerializedName("seller")
    @Expose
    private Seller seller;

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

    public Seller getSeller() {
        return seller;
    }

    public void setSeller(Seller seller) {
        this.seller = seller;
    }

    public static void deleteAll(Realm realm){

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.where(Seller.class).findAll().deleteAllFromRealm();
                realm.where(SellerInfo.class).findAll().deleteAllFromRealm();
            }
        });
    }
}
