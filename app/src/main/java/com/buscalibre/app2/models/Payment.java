
package com.buscalibre.app2.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;

public class Payment extends RealmObject {

    @SerializedName("productType")
    @Expose
    private Integer productType;
    @SerializedName("productKey")
    @Expose
    private String productKey;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("purchaseValue")
    @Expose
    private Integer purchaseValue;
    @SerializedName("quantity")
    @Expose
    private Integer quantity;
    @SerializedName("totalAmount")
    @Expose
    private Integer totalAmount;

    public Integer getProductType() {
        return productType;
    }

    public void setProductType(Integer productType) {
        this.productType = productType;
    }

    public String getProductKey() {
        return productKey;
    }

    public void setProductKey(String productKey) {
        this.productKey = productKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPurchaseValue() {
        return purchaseValue;
    }

    public void setPurchaseValue(Integer purchaseValue) {
        this.purchaseValue = purchaseValue;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Integer totalAmount) {
        this.totalAmount = totalAmount;
    }

}
