
package com.buscalibre.app2.models;

import com.buscalibre.app2.constants.ServerConstants;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;

public class PaymentList_ extends RealmObject {

    @SerializedName("paymentId")
    @Expose
    private String paymentId;
    @SerializedName("paymentDate")
    @Expose
    private String paymentDate;
    @SerializedName("paymentTotal")
    @Expose
    private Float paymentTotal;
    @SerializedName("paymentStatus")
    @Expose
    private Integer paymentStatus = ServerConstants.SELLER_PAYMENT_STATUS_PENDING;
    @SerializedName("paymentStatusText")
    @Expose
    private String paymentStatusText;
    @SerializedName("from")
    @Expose
    private String from;
    @SerializedName("to")
    @Expose
    private String to;
    @SerializedName("quantity")
    @Expose
    private int quantity;

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(String paymentDate) {
        this.paymentDate = paymentDate;
    }

    public Float getPaymentTotal() {
        return paymentTotal;
    }

    public void setPaymentTotal(Float paymentTotal) {
        this.paymentTotal = paymentTotal;
    }

    public Integer getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(Integer paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getPaymentStatusText() {
        return paymentStatusText;
    }

    public void setPaymentStatusText(String paymentStatusText) {
        this.paymentStatusText = paymentStatusText;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
