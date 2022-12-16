
package com.buscalibre.app2.models;

import java.util.List;

import com.buscalibre.app2.constants.ServerConstants;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;

public class PaymentDetail extends RealmObject {

    @SerializedName("blstatus")
    @Expose
    private Integer blstatus;
    @SerializedName("blmessage")
    @Expose
    private String blmessage;
    @SerializedName("totalPurchase")
    @Expose
    private Float totalPurchase;
    @SerializedName("paymentDate")
    @Expose
    private String paymentDate;
    @SerializedName("paymentStatus")
    @Expose
    private Integer paymentStatus = ServerConstants.SELLER_PAYMENT_STATUS_PENDING;
    @SerializedName("paymentStatusText")
    @Expose
    private String paymentStatusText;
    @SerializedName("paymentMethod")
    @Expose
    private String paymentMethod;
    @SerializedName("destination")
    @Expose
    private Destination destination;
    @SerializedName("productList")
    @Expose
    private RealmList<ProductList> productList = null;

    public String getPaymentStatusText() {
        return paymentStatusText;
    }

    public void setPaymentStatusText(String paymentStatusText) {
        this.paymentStatusText = paymentStatusText;
    }

    public Integer getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(Integer paymentStatus) {
        this.paymentStatus = paymentStatus;
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

    public Float getTotalPurchase() {
        return totalPurchase;
    }

    public void setTotalPurchase(Float totalPurchase) {
        this.totalPurchase = totalPurchase;
    }

    public String getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(String paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Destination getDestination() {
        return destination;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    public RealmList<ProductList> getProductList() {
        return productList;
    }

    public void setProductList(RealmList<ProductList> productList) {
        this.productList = productList;
    }

    public static void deleteAll(Realm realm){
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.where(ProductList.class).findAll().deleteAllFromRealm();
                realm.where(Destination.class).findAll().deleteAllFromRealm();
                realm.where(PaymentDetail.class).findAll().deleteAllFromRealm();
            }
        });
    }

}
