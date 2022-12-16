package com.buscalibre.app2.network;



import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class ApiManager {
    // API URLS
    public static final String BASE_URL = "https://www.buscalibre.cl";
    public static final String API_URL = "/v2";

    public static final String ENDPOINT_PRICE = "/app-price";
    public static final String ENDPOINT_CART = "/app-cart";

    /**
     * Returns the URL for sending a request to obtain the price(s) of a product
     */
    public static String getPriceRequestUrl(String productCode, String storeName, String differentiatorsCode) {
        String url = BASE_URL + API_URL + ENDPOINT_PRICE;

        try {
            url += "?codigo=" + URLEncoder.encode(productCode, "UTF-8");
            url += "&sitio=" + URLEncoder.encode(storeName, "UTF-8");
            if (differentiatorsCode != null) {
                url += "&diff=" + URLEncoder.encode(differentiatorsCode, "UTF-8");
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
        return url;
    }

    /**
     * Returns the URL for sending a request to obtain the price(s) of a product
     */
    public static String getPriceRequestUrl(String productCode, String storeName) {
        return getPriceRequestUrl(productCode, storeName, null);
    }

    /**
     * Returns the URL for sending a request to obtain the cart url of a quoted product
     */
    public static String getCartRequestUrl(String productCode, String storeName, String differentiatorsCode, String shippingMethod, String condition) {
        String url = BASE_URL + API_URL + ENDPOINT_CART;
        try {
            url += "?codigo=" + URLEncoder.encode(productCode, "UTF-8");
            url += "&sitio=" + URLEncoder.encode(storeName, "UTF-8");
            url += "&envio=" + URLEncoder.encode(shippingMethod, "UTF-8");

            if (differentiatorsCode != null) {
                url += "&diff=" + URLEncoder.encode(differentiatorsCode, "UTF-8");
            }
            if (condition != null) {
                url += "&condition=" + URLEncoder.encode(condition, "UTF-8");
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
        return url;

    }
}
