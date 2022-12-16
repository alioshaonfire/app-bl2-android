package com.buscalibre.app2.events;

public class RefreshProductsEvent {

    private final Boolean isRefresh;

    public RefreshProductsEvent(Boolean isRefresh) {
        this.isRefresh = isRefresh;
    }


}
