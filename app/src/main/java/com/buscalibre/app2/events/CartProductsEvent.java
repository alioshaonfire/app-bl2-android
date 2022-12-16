package com.buscalibre.app2.events;

public class CartProductsEvent {

    private final int qty;

    public CartProductsEvent(int qty) {
        this.qty = qty;
    }

    public int getQty() {
        return qty;
    }
}
