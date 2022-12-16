
package com.buscalibre.app2.models;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;

public class CurrentPayment extends RealmObject {

    @SerializedName("blstatus")
    @Expose
    private Integer blstatus;
    @SerializedName("blmessage")
    @Expose
    private String blmessage;
    @SerializedName("productList")
    @Expose
    private RealmList<ProductList_> productListS = null;
    @SerializedName("pendingRows")
    @Expose
    private Boolean pendingRows;
    @SerializedName("totalAmount")
    @Expose
    private Float totalAmount;

    public Boolean getPendingRows() {
        return pendingRows;
    }

    public void setPendingRows(Boolean pendingRows) {
        this.pendingRows = pendingRows;
    }

    public Float getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Float totalAmount) {
        this.totalAmount = totalAmount;
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

    public RealmList<ProductList_> getProductListS() {
        return productListS;
    }

    public void setProductListS(RealmList<ProductList_> productListS) {
        this.productListS = productListS;
    }

    public static void deleteAll(Realm realm){
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.where(ProductList_.class).findAll().deleteAllFromRealm();
                realm.where(CurrentPayment.class).findAll().deleteAllFromRealm();
            }
        });
    }
}
