
package com.buscalibre.app2.models;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UserPayments {

    @SerializedName("blstatus")
    @Expose
    private Integer blstatus;
    @SerializedName("blmessage")
    @Expose
    private String blmessage;
    @SerializedName("canAdd")
    @Expose
    private Boolean canAdd = false;
    @SerializedName("paymentList")
    @Expose
    private List<PaymentList> paymentList = null;

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

    public List<PaymentList> getPaymentList() {
        return paymentList;
    }

    public void setPaymentList(List<PaymentList> paymentList) {
        this.paymentList = paymentList;
    }

    public Boolean getCanAdd() {
        return canAdd;
    }

    public void setCanAdd(Boolean canAdd) {
        this.canAdd = canAdd;
    }

}
