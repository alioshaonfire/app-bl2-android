package com.buscalibre.app2.util;

import android.webkit.WebView;

import androidx.annotation.NonNull;

import com.android.volley.toolbox.JsonObjectRequest;
import com.buscalibre.app2.network.ApiResponseListener;

import org.json.JSONObject;

/**
 * Parses data from a WebView that might contain a product page
 */
public interface ProductPageParser {

    /**
     * Asynchronously determines if the current page represents a product, sending the result to a callback
     *
     * @param webView        {@link WebView} displaying the current page
     * @param resultListener Listener that will be invoked as a callback when the result is obtained
     */
    void checkIfProductPage(final WebView webView, final OnCheckResultListener resultListener);


    /**
     * Asynchronously determines if, given that the current page being shown is a product page, all
     * the product differentiators have been selected
     *
     * @param webView        {@link WebView} displaying the current page
     * @param resultListener Listener that will be invoked as a callback when the result is obtained
     */
    void checkIfAllDifferentiatorsSelected(final WebView webView, final OnCheckResultListener resultListener);


    /**
     * Asynchronously checks for a unique string representing a product with all of its differentiators
     * selected, sending the result to a callback
     *
     * @param webView        {@link WebView} displaying the current page
     * @param resultListener Listener that will be invoked as a callback when the result is obtained
     */
    void requestProductHash(final WebView webView, final OnRequestStringResultListener resultListener);

    /**
     * Returns a {@link JsonObjectRequest} for obtaining the quote of a product.
     *
     * @param productHash      Unique product hash to quote
     * @param responseListener Listener to include in the request for both success and error responses
     * @return A {@link JsonObjectRequest} for obtaining the quote of a product.
     */
    JsonObjectRequest getQuoteRequest(String productHash, ApiResponseListener<JSONObject> responseListener);

    /**
     * Returns a {@link JsonObjectRequest} for obtaining the cart URL of a quoted product.
     *
     * @param productHash      Unique product hash to quote
     * @param condition        Condition of the quoted product for the selected price
     * @param shippingMethod   Shipping method of the selected price
     * @param responseListener Listener to include in the request for both success and error responses
     * @return A {@link JsonObjectRequest} for obtaining the cart of a quoted product.
     */
    JsonObjectRequest getCartRequest(String productHash, String condition, String shippingMethod, ApiResponseListener<JSONObject> responseListener);

    /**
     * Returns the string representing the domain for a given store
     */
    String getStoreDomain();

    interface OnCheckResultListener {
        void onResult(boolean result);
    }

    interface OnRequestStringResultListener {
        void onResult(@NonNull String result);
    }
}
