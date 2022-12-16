
package com.buscalibre.app2.models;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;

public class PaymentHistory extends RealmObject {

    @SerializedName("blstatus")
    @Expose
    private Integer blstatus;
    @SerializedName("blmessage")
    @Expose
    private String blmessage;
    @SerializedName("paymentList")
    @Expose
    private RealmList<PaymentList_> paymentList = null;
    @SerializedName("pendingRows")
    @Expose
    private Boolean pendingRows;


    public Boolean getPendingRows() {
        return pendingRows;
    }

    public void setPendingRows(Boolean pendingRows) {
        this.pendingRows = pendingRows;
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

    public RealmList<PaymentList_> getPaymentList() {
        return paymentList;
    }

    public void setPaymentList(RealmList<PaymentList_> paymentList) {
        this.paymentList = paymentList;
    }

    public static void deleteAll(Realm realm){
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.where(PaymentList_.class).findAll().deleteAllFromRealm();
                realm.where(PaymentHistory.class).findAll().deleteAllFromRealm();
            }
        });
    }

}
