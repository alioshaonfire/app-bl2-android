package com.buscalibre.app2.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;

public class UserRegister extends RealmObject {

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
    @SerializedName("ebookToken")
    @Expose
    private String ebookToken;

    public String getEbookToken() {
        return ebookToken;
    }

    public void setEbookToken(String ebookToken) {
        this.ebookToken = ebookToken;
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
}
