
package com.buscalibre.app2.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;

public class ProductStatus extends RealmObject {

    @SerializedName("recibidos")
    @Expose
    private Recibidos recibidos;
    @SerializedName("por_retirar")
    @Expose
    private PorRetirar porRetirar;
    @SerializedName("anulados")
    @Expose
    private Anulados anulados;
    @SerializedName("pendientes")
    @Expose
    private Pendientes pendientes;

    public Recibidos getRecibidos() {
        return recibidos;
    }

    public void setRecibidos(Recibidos recibidos) {
        this.recibidos = recibidos;
    }

    public PorRetirar getPorRetirar() {
        return porRetirar;
    }

    public void setPorRetirar(PorRetirar porRetirar) {
        this.porRetirar = porRetirar;
    }

    public Anulados getAnulados() {
        return anulados;
    }

    public void setAnulados(Anulados anulados) {
        this.anulados = anulados;
    }

    public Pendientes getPendientes() {
        return pendientes;
    }

    public void setPendientes(Pendientes pendientes) {
        this.pendientes = pendientes;
    }

}
