package com.buscalibre.app2.events;

import com.buscalibre.app2.models.EbookList;

public class RefreshAudiobookDownloadEvent {

    private final EbookList ebookList;


    public RefreshAudiobookDownloadEvent(EbookList ebookList) {
        this.ebookList = ebookList;
    }

    public EbookList getEbookList() {
        return ebookList;
    }
}
