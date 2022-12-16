package com.buscalibre.app2.events;

public class SelectHomeMenuEvent {

    private final Boolean isReadSelected;

    public SelectHomeMenuEvent(Boolean isReadSelected) {
        this.isReadSelected = isReadSelected;
    }

    public Boolean getReadSelected() {
        return isReadSelected;
    }
}
