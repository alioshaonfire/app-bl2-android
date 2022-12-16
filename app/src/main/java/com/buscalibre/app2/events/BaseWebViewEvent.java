package com.buscalibre.app2.events;

public class BaseWebViewEvent {

    private final String url;
    private final String header;
    private final Boolean hasCart;
    private final String title;
    private final String replace_const;
    private final String key;

    public BaseWebViewEvent(String url, String header, Boolean hasCart, String title, String replace_const, String key) {
        this.url = url;
        this.header = header;
        this.hasCart = hasCart;
        this.title = title;
        this.replace_const = replace_const;
        this.key = key;
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

    public String getReplace_const() {
        return replace_const;
    }

    public String getKey() {
        return key;
    }
}
