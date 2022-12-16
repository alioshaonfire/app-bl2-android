package com.buscalibre.app2.events;

public class RefreshPaymentEvent {

    private final boolean isrefresh;


    public RefreshPaymentEvent(boolean isrefresh) {
        this.isrefresh = isrefresh;
    }

    public boolean isIsrefresh() {
        return isrefresh;
    }
}
