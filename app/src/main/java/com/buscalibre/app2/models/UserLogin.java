package com.buscalibre.app2.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.Realm;
import io.realm.RealmObject;


public class UserLogin extends RealmObject {

    @SerializedName("blstatus")
    @Expose
    private Integer blstatus;
    @SerializedName("blmessage")
    @Expose
    private String blmessage;
    @SerializedName("token")
    @Expose
    private String token;
    @SerializedName("webToken")
    @Expose
    private String webToken;
    @SerializedName("superID")
    @Expose
    private String superID;
    @SerializedName("ebookToken")
    @Expose
    private String ebookToken = "";
    private boolean isOfflineMode = false;
    private String email;
    private String countryID;
    private int qtyCartProducts = 0;

    public int getQtyCartProducts() {
        return qtyCartProducts;
    }

    public void setQtyCartProducts(int qtyCartProducts) {
        this.qtyCartProducts = qtyCartProducts;
    }

    public boolean isOfflineMode() {
        return isOfflineMode;
    }

    public void setOfflineMode(boolean offlineMode) {
        isOfflineMode = offlineMode;
    }

    public String getEbookToken() {
        return ebookToken;
    }

    public void setEbookToken(String ebookToken) {
        this.ebookToken = ebookToken;
    }

    public String getSuperID() {
        return superID;
    }

    public void setSuperID(String superID) {
        this.superID = superID;
    }

    public String getCountryID() {
        return countryID;
    }

    public void setCountryID(String countryID) {
        this.countryID = countryID;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getWebToken() {
        return webToken;
    }

    public void setWebToken(String webToken) {
        this.webToken = webToken;
    }

    public static void deleteAll(Realm realm){
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.where(UserLogin.class).findAll().deleteAllFromRealm();
            }
        });
    }
}
