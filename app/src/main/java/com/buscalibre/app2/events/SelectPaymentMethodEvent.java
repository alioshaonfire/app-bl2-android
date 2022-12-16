package com.buscalibre.app2.events;

public class SelectPaymentMethodEvent {

    private final Boolean isReadSelected;

    public SelectPaymentMethodEvent(Boolean isReadSelected) {
        this.isReadSelected = isReadSelected;
    }

    public Boolean getReadSelected() {
        return isReadSelected;
    }
}
