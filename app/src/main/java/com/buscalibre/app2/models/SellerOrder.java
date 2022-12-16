
package com.buscalibre.app2.models;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmResults;

public class SellerOrder extends RealmObject {

    @SerializedName("blstatus")
    @Expose
    private Integer blstatus;
    @SerializedName("blmessage")
    @Expose
    private String blmessage;
    @SerializedName("orderList")
    @Expose
    private RealmList<Order> orderList = null;

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

    public RealmList<Order> getOrderList() {
        return orderList;
    }

    public void setOrderList(RealmList<Order> orderList) {
        this.orderList = orderList;
    }

    public static void deleteAll(Realm realm, int orderStatus){
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {

                RealmResults<Order> orderRealmResults = realm.where(Order.class)
                        .equalTo("orderStatus", orderStatus)
                        .findAll();

                for (Order order:orderRealmResults){
                    order.getProductStatus().getRecibidos().deleteFromRealm();
                    order.getProductStatus().getAnulados().deleteFromRealm();
                    order.getProductStatus().getPendientes().deleteFromRealm();
                    order.getProductStatus().getPorRetirar().deleteFromRealm();
                    order.getProductStatus().deleteFromRealm();
                    order.deleteFromRealm();
                }
                realm.where(SellerOrder.class).findAll().deleteAllFromRealm();

            }
        });
    }
}
