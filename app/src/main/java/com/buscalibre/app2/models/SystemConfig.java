
package com.buscalibre.app2.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.Realm;
import io.realm.RealmObject;

public class SystemConfig extends RealmObject {

    @SerializedName("blstatus")
    @Expose
    private Integer blstatus;
    @SerializedName("blmessage")
    @Expose
    private String blmessage;
    @SerializedName("version")
    @Expose
    private Integer version;
    @SerializedName("timeout")
    @Expose
    private Integer timeout;
    @SerializedName("store")
    @Expose
    private  StoreConfig store;
    @SerializedName("messagePageLen")
    @Expose
    private Integer messagePageLen;

    @SerializedName("ebooksPageLen")
    @Expose
    private Integer ebooksPageLen;

    @SerializedName("showcasePageLen")
    @Expose
    private Integer showcasePageLen;

    @SerializedName("seller")
    @Expose
    private Seller_ seller_;

    public Seller_ getSeller_() {
        return seller_;
    }

    public void setSeller_(Seller_ seller_) {
        this.seller_ = seller_;
    }

    public Integer getShowcasePageLen() {
        return showcasePageLen;
    }

    public void setShowcasePageLen(Integer showcasePageLen) {
        this.showcasePageLen = showcasePageLen;
    }

    public Integer getEbooksPageLen() {
        return ebooksPageLen;
    }

    public void setEbooksPageLen(Integer ebooksPageLen) {
        this.ebooksPageLen = ebooksPageLen;
    }

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

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public StoreConfig getStore() {
        return store;
    }

    public void setStore(StoreConfig store) {
        this.store = store;
    }

    public Integer getMessagePageLen() {
        return messagePageLen;
    }

    public void setMessagePageLen(Integer messagePageLen) {
        this.messagePageLen = messagePageLen;
    }

    public static void deleteAll(Realm realm){
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.where(StoreConfig.class).findAll().deleteAllFromRealm();
                realm.where(SystemConfig.class).findAll().deleteAllFromRealm();
            }
        });
    }

}
