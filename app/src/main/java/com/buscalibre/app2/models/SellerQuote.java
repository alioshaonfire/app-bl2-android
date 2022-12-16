
package com.buscalibre.app2.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.Realm;
import io.realm.RealmObject;

public class SellerQuote extends RealmObject {

    @SerializedName("blstatus")
    @Expose
    private Integer blstatus;
    @SerializedName("blmessage")
    @Expose
    private String blmessage;
    @SerializedName("product")
    @Expose
    private Product product;

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

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public static void deleteAll(Realm realm){
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.where(Product.class).findAll().deleteAllFromRealm();
                realm.where(SellerQuote.class).findAll().deleteAllFromRealm();

            }
        });
    }
}
