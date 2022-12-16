package com.buscalibre.app2.events;

public class SelectEbayEvent {

    private final Boolean isReadSelected;

    public SelectEbayEvent(Boolean isReadSelected) {
        this.isReadSelected = isReadSelected;
    }

    public Boolean getReadSelected() {
        return isReadSelected;
    }
}
