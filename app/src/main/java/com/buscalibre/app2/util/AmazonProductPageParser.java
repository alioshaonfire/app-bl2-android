package com.buscalibre.app2.util;

import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.buscalibre.app2.network.ApiManager;
import com.buscalibre.app2.network.ApiResponseListener;

import org.json.JSONObject;

public class AmazonProductPageParser implements ProductPageParser {

    private static final String QUERY_GET_CANONICAL_URL = "document.querySelector(\"link[rel='canonical']\").href";
    private static final String QUERY_GET_ASIN = "document.querySelector(\"#a\").value";

    private static final String STORE_NAME = "amazon";
    private static final String STORE_DOMAIN = "amazon.com";


    @Override
    public void checkIfProductPage(final WebView webView, final OnCheckResultListener resultListener) {
        webView.post(new Runnable() {
            @Override
            public void run() {
                // Check if the webView has a handler, i.e., it is attached to an activity
                if (webView.getHandler() != null) {
                    // An amazon web page represents a product if its canonical url contains "/dp"
                    webView.evaluateJavascript(QUERY_GET_CANONICAL_URL, new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String value) {
                            resultListener.onResult(value != null && value.contains("/dp"));
                        }
                    });
                }
            }
        });
    }

    @Override
    public void checkIfAllDifferentiatorsSelected(final WebView webView, final OnCheckResultListener resultListener) {
        webView.post(new Runnable() {
            @Override
            public void run() {
                // Check if the webView has a handler, i.e., it is attached to an activity
                if (webView.getHandler() != null) {
                    // An amazon product web page has all of its differentiators selected when it is possible
                    // to obtain the product's ASIN code.
                    webView.evaluateJavascript(QUERY_GET_ASIN, new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String value) {
                            // Note that the string received by the callback is:
                            // - The string 'null' for JS `null`, or
                            // - Java `null` for JS `undefined`, or
                            // - The actual value surrounded by double quotes.
                            //    e.g.: JS empty string is the String '""' (two double quotes)
                            // Reference: https://stackoverflow.com/a/20377857/370798
                            resultListener.onResult(value != null && !value.equals("null") && value.length() > 2);
                        }
                    });
                }
            }
        });
    }

    @Override
    public void requestProductHash(final WebView webView, final OnRequestStringResultListener resultListener) {
        webView.post(new Runnable() {
            @Override
            public void run() {
                // Check if the webView has a handler, i.e., it is attached to an activity
                if (webView.getHandler() != null) {
                    // In amazon, the ASIN is a unique product identifier, considering all of its differentiators
                    webView.evaluateJavascript(QUERY_GET_ASIN, new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String value) {
                            // Note that the string received by the callback is:
                            // - The string 'null' for JS `null`, or
                            // - Java `null` for JS `undefined`, or
                            // - The actual value surrounded by double quotes.
                            //    e.g.: JS empty string is the String '""' (two double quotes)
                            // Reference: https://stackoverflow.com/a/20377857/370798
                            if (value == null || value.equals("null") || value.length() < 3) {
                                resultListener.onResult("");
                            } else {
                                // Return the product hash removing surrounding quotes
                                resultListener.onResult(value.substring(1, value.length() - 1));
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public JsonObjectRequest getQuoteRequest(String productHash, ApiResponseListener<JSONObject> responseListener) {
        Log.d("productHash", productHash);
        Log.d("getPriceRequestUrl", ApiManager.getPriceRequestUrl(productHash, STORE_NAME));
        return new JsonObjectRequest(
                Request.Method.GET,
                ApiManager.getPriceRequestUrl(productHash, STORE_NAME),
                "",
                responseListener,
                responseListener);
    }


    @Override
    public JsonObjectRequest getCartRequest(String productHash, String condition, String shippingMethod, ApiResponseListener<JSONObject> responseListener) {
        return new JsonObjectRequest(
                Request.Method.GET,
                ApiManager.getCartRequestUrl(productHash, STORE_NAME, null, shippingMethod, condition),
                "",
                responseListener,
                responseListener);
    }

    @Override
    public String getStoreDomain(){
        return STORE_DOMAIN;
    }
}
