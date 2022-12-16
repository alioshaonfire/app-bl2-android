
package com.buscalibre.app2.models;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;

public class SellerShowcase extends RealmObject {

    @SerializedName("blstatus")
    @Expose
    private Integer blstatus;
    @SerializedName("blmessage")
    @Expose
    private String blmessage;
    @SerializedName("productList")
    @Expose
    private RealmList<ProductList> productList = null;
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
                realm.where(SellerShowcase.class).findAll().deleteAllFromRealm();
            }
        });
    }
}
