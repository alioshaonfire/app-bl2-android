package com.buscalibre.app2.events;

import com.buscalibre.app2.models.EbookList;

public class RefreshEbookDownloadEvent {

    private final EbookList ebookList;


    public RefreshEbookDownloadEvent(EbookList ebookList) {
        this.ebookList = ebookList;
    }

    public EbookList getEbookList() {
        return ebookList;
    }
}
