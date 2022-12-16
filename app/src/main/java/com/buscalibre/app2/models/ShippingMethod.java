package com.buscalibre.app2.models;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class ShippingMethod {

    // Keys for (de)serialization
    public static final String PRICE = "precio";
    public static final String AVAILABLE = "disponible";
    public static final String ARRIVAL_DATE = "fecha_recepcion";
    public static final String MIN_ARRIVAL_TIME = "tiempo_en_destino_minimo";
    public static final String MAX_ARRIVAL_TIME = "tiempo_en_destino_maximo";

    // Private fields
    private String mPrice;
    private boolean mAvailable;
    private String mArrivalDate;
    private int mMinArrivalTime;
    private int mMaxArrivalTime;

    /**
     * Empty constructor, needed for deserialization.
     */
    public ShippingMethod() {
    }

    public static ShippingMethod getShippingMethodFromJson(JSONObject productShippingJson) throws JSONException {
        ShippingMethod shippingMethod = new ShippingMethod();
        Log.e("price",productShippingJson.getString(PRICE));
        Log.e("AVAILABLE",productShippingJson.getString(AVAILABLE));
        Log.e("ARRIVAL_DATE",productShippingJson.getString(ARRIVAL_DATE));
        Log.e("MIN_ARRIVAL_TIME",productShippingJson.getString(MIN_ARRIVAL_TIME));
        Log.e("MAX_ARRIVAL_TIME",productShippingJson.getString(MAX_ARRIVAL_TIME));

        shippingMethod.setPrice(productShippingJson.getString(PRICE));
        shippingMethod.setAvailable(productShippingJson.getBoolean(AVAILABLE));
        shippingMethod.setArrivalDate(productShippingJson.getString(ARRIVAL_DATE));
        shippingMethod.setMinArrivalTime(productShippingJson.getInt(MIN_ARRIVAL_TIME));
        shippingMethod.setMaxArrivalTime(productShippingJson.getInt(MAX_ARRIVAL_TIME));

        return shippingMethod;
    }

    public String getPrice() {
        return mPrice;
    }

    public void setPrice(String price) {
        mPrice = price;
    }

    public boolean isAvailable() {
        return mAvailable;
    }

    public void setAvailable(boolean available) {
        mAvailable = available;
    }

    public String getArrivalDate() {
        return mArrivalDate;
    }

    public void setArrivalDate(String arrivalDate) {
        mArrivalDate = arrivalDate;
    }

    public int getMinArrivalTime() {
        return mMinArrivalTime;
    }

    public void setMinArrivalTime(int minArrivalTime) {
        mMinArrivalTime = minArrivalTime;
    }

    public int getMaxArrivalTime() {
        return mMaxArrivalTime;
    }

    public void setMaxArrivalTime(int maxArrivalTime) {
        mMaxArrivalTime = maxArrivalTime;
    }
}
