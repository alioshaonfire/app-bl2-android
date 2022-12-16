
package com.buscalibre.app2.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Avion {

    @SerializedName("precio")
    @Expose
    private String precio;
    @SerializedName("disponible")
    @Expose
    private Boolean disponible;
    @SerializedName("fecha_recepcion")
    @Expose
    private String fechaRecepcion;
    @SerializedName("tiempo_en_destino_minimo")
    @Expose
    private Integer tiempoEnDestinoMinimo;
    @SerializedName("tiempo_en_destino_maximo")
    @Expose
    private Integer tiempoEnDestinoMaximo;

    public String getPrecio() {
        return precio;
    }

    public void setPrecio(String precio) {
        this.precio = precio;
    }

    public Boolean getDisponible() {
        return disponible;
    }

    public void setDisponible(Boolean disponible) {
        this.disponible = disponible;
    }

    public String getFechaRecepcion() {
        return fechaRecepcion;
    }

    public void setFechaRecepcion(String fechaRecepcion) {
        this.fechaRecepcion = fechaRecepcion;
    }

    public Integer getTiempoEnDestinoMinimo() {
        return tiempoEnDestinoMinimo;
    }

    public void setTiempoEnDestinoMinimo(Integer tiempoEnDestinoMinimo) {
        this.tiempoEnDestinoMinimo = tiempoEnDestinoMinimo;
    }

    public Integer getTiempoEnDestinoMaximo() {
        return tiempoEnDestinoMaximo;
    }

    public void setTiempoEnDestinoMaximo(Integer tiempoEnDestinoMaximo) {
        this.tiempoEnDestinoMaximo = tiempoEnDestinoMaximo;
    }

}
