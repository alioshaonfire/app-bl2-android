package com.buscalibre.app2.events;

public class SelectNavMenuEvent {

    private final Boolean isReadSelected;

    public SelectNavMenuEvent(Boolean isReadSelected) {
        this.isReadSelected = isReadSelected;
    }

    public Boolean getReadSelected() {
        return isReadSelected;
    }
}
