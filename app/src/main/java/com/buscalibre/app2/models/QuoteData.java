
package com.buscalibre.app2.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class QuoteData {

    @SerializedName("new")
    @Expose
    private New _new;
    @SerializedName("used")
    @Expose
    private Used used;
    @SerializedName("new prime")
    @Expose
    private NewPrime newPrime;

    public New get_new() {
        return _new;
    }

    public void set_new(New _new) {
        this._new = _new;
    }

    public Used getUsed() {
        return used;
    }

    public void setUsed(Used used) {
        this.used = used;
    }

    public New getNew() {
        return _new;
    }

    public void setNew(New _new) {
        this._new = _new;
    }

    public NewPrime getNewPrime() {
        return newPrime;
    }

    public void setNewPrime(NewPrime newPrime) {
        this.newPrime = newPrime;
    }

}
