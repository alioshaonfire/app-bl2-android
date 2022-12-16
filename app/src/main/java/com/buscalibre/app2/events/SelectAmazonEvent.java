package com.buscalibre.app2.events;

public class SelectAmazonEvent {

    private final Boolean isReadSelected;

    public SelectAmazonEvent(Boolean isReadSelected) {
        this.isReadSelected = isReadSelected;
    }

    public Boolean getReadSelected() {
        return isReadSelected;
    }
}
