package com.buscalibre.app2.events;

import com.buscalibre.app2.models.MessageList;

public class SelectStoreEvent {

    private final Boolean isReadSelected;

    public SelectStoreEvent(Boolean isReadSelected) {
        this.isReadSelected = isReadSelected;
    }

    public Boolean getReadSelected() {
        return isReadSelected;
    }
}
