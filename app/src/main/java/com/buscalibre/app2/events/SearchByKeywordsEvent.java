package com.buscalibre.app2.events;

public class SearchByKeywordsEvent {

    private final String url;
    private final String header;
    private final Boolean hasCart;
    private final String title;

    public SearchByKeywordsEvent(String url, String header, Boolean hasCart, String title) {
        this.url = url;
        this.header = header;
        this.hasCart = hasCart;
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public String getHeader() {
        return header;
    }

    public Boolean getHasCart() {
        return hasCart;
    }

    public String getTitle() {
        return title;
    }
}
