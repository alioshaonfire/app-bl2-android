package com.buscalibre.app2.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;

public class Seller_ extends RealmObject {

    @SerializedName("minDays")
    @Expose
    private Integer minDays;
    @SerializedName("maxDays")
    @Expose
    private Integer maxDays;
    @SerializedName("pickupMinDays")
    @Expose
    private RealmList<Integer> pickupMinDays = null;

    public RealmList<Integer> getPickupMinDays() {
        return pickupMinDays;
    }

    public void setPickupMinDays(RealmList<Integer> pickupMinDays) {
        this.pickupMinDays = pickupMinDays;
    }

    public Integer getMinDays() {
        return minDays;
    }

    public void setMinDays(Integer minDays) {
        this.minDays = minDays;
    }

    public Integer getMaxDays() {
        return maxDays;
    }

    public void setMaxDays(Integer maxDays) {
        this.maxDays = maxDays;
    }
}
