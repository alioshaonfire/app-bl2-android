
package com.buscalibre.app2.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Used {

    @SerializedName("avion")
    @Expose
    private Avion avion;

    @SerializedName("barco")
    @Expose
    private Barco barco;

    public Barco getBarco() {
        return barco;
    }

    public void setBarco(Barco barco) {
        this.barco = barco;
    }

    public Avion getAvion() {
        return avion;
    }

    public void setAvion(Avion avion) {
        this.avion = avion;
    }

}
