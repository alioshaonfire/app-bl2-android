
package com.buscalibre.app2.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.Realm;
import io.realm.RealmObject;

public class SellerConditions extends RealmObject {

    @SerializedName("blstatus")
    @Expose
    private Integer blstatus;
    @SerializedName("blmessage")
    @Expose
    private String blmessage;
    @SerializedName("conditions")
    @Expose
    private Conditions conditions;

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

    public Conditions getConditions() {
        return conditions;
    }

    public void setConditions(Conditions conditions) {
        this.conditions = conditions;
    }

    public static void deleteAll(Realm realm){
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.where(Conditions.class).findAll().deleteAllFromRealm();
                realm.where(Book.class).findAll().deleteAllFromRealm();
                realm.where(SellerConditions.class).findAll().deleteAllFromRealm();
            }
        });
    }
}
