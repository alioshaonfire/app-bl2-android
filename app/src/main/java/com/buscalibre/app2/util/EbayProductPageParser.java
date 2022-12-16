package com.buscalibre.app2.util;

import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.buscalibre.app2.network.ApiManager;
import com.buscalibre.app2.network.ApiResponseListener;

import org.json.JSONObject;

public class EbayProductPageParser implements ProductPageParser {

    //private static final String QUERY_GET_ITEM_ID = "document.querySelector('#getData').getAttribute('data-itemid')";
    private static final String QUERY_GET_ITEM_ID = "document.querySelector('#appbnr_itm_id').getAttribute('value')";

    //private static final String QUERY_GET_SKU_ID = "document.querySelector('#skuId') == null ? null : document.querySelector('#skuId').value";
    private static final String QUERY_GET_VARIATION_ID = "$('#variationId').val()";
    private static final String QUERY_GET_JOINED_DIFFS =
            "$.map($('#msku_list select option:selected'), function(element) {return $(element).text().replace(/[ -]+/g, '-')}).join('_')";

    private static final String STORE_NAME = "ebay";
    private static final String STORE_DOMAIN = "ebay.com";

    @Override
    public void checkIfProductPage(final WebView webView, final OnCheckResultListener resultListener) {
        webView.post(new Runnable() {
            @Override
            public void run() {
                // Check if the webView has a handler, i.e., it is attached to an activity
                if (webView.getHandler() != null) {
                    // An ebay web page represents a product if has an item id
                    webView.evaluateJavascript(QUERY_GET_ITEM_ID, new ValueCallback<String>() {
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
    public void checkIfAllDifferentiatorsSelected(final WebView webView, final OnCheckResultListener resultListener) {
        webView.post(new Runnable() {
            @Override
            public void run() {
                // Check if the webView has a handler, i.e., it is attached to an activity
                if (webView.getHandler() != null) {
                    // An ebay product web page requires further selection of differentiators if it has
                    // an empty variation id. Otherwise, it has no differentiators (undefined variation id)
                    // or it does have them and they have already been selected (non-null, non-empty variation id).
                    webView.evaluateJavascript(QUERY_GET_VARIATION_ID, new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String value) {
                            // Note that the string received by the callback is:
                            // - The string 'null' for JS `null`, or
                            // - Java `null` for JS `undefined`, or
                            // - The actual value surrounded by double quotes.
                            //    e.g.: JS empty string is the String '""' (two double quotes)
                            // Reference: https://stackoverflow.com/a/20377857/370798
                            resultListener.onResult(!(value != null && value.equals("\"\"")));
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
                    // In ebay, a product is uniquely identified by both its item id and its differentiators (if any).
                    // We use the union, separated by a pipe ('|'), of the item id and the union of differentiators (which in turn
                    // are underscore ('_') separated) a unique hash for a product
                    webView.evaluateJavascript(QUERY_GET_ITEM_ID, new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(final String itemId) {
                            // Note that the strings received by the callbacks will be:
                            // - The string '""' (two double quotes) for JS empty string
                            // - The string 'null' for JS `null`
                            // - Java `null` for JS `undefined`
                            // Reference: https://stackoverflow.com/a/20377857/370798
                            if (itemId == null || itemId.equals("null") || itemId.length() < 3) {
                                resultListener.onResult("");
                            } else {
                                webView.evaluateJavascript(QUERY_GET_JOINED_DIFFS, new ValueCallback<String>() {
                                    @Override
                                    public void onReceiveValue(String diffs) {
                                        // Remove trailing quotation marks
                                        String productHash = itemId.substring(1, itemId.length() - 1);
                                        if (diffs != null && !diffs.equals("null") && diffs.length() > 2) {
                                            // Remove trailing quotation marks
                                            diffs = diffs.substring(1, diffs.length() - 1);
                                            productHash += "|" + diffs;
                                        }
                                        resultListener.onResult(productHash);
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public JsonObjectRequest getQuoteRequest(String productHash, ApiResponseListener<JSONObject> responseListener) {
        // Split the hash containing both the item id and the SKU id.
        String productCode;
        String joinedDifferentiators = null;
        if (productHash.contains("|")) {
            String[] splitHash = productHash.split("\\|");
            productCode = splitHash[0];
            joinedDifferentiators = splitHash[1];
        } else {
            productCode = productHash;
        }

        Log.d("productCode", productCode);

        return new JsonObjectRequest(
                Request.Method.GET,
                ApiManager.getPriceRequestUrl(productCode, STORE_NAME, joinedDifferentiators),
                "",
                responseListener,
                responseListener);
    }

    @Override
    public JsonObjectRequest getCartRequest(String productHash, String condition, String shippingMethod, ApiResponseListener<JSONObject> responseListener) {
        // Split the hash containing both the item id and the SKU id.
        String productCode;
        String differentiatorsCode = null;
        if (productHash.contains("|")) {
            String[] splitHash = productHash.split("\\|");
            productCode = splitHash[0];
            differentiatorsCode = splitHash[1];
        } else {
            productCode = productHash;
        }

        return new JsonObjectRequest(
                Request.Method.GET,
                ApiManager.getCartRequestUrl(productCode, STORE_NAME, differentiatorsCode, shippingMethod, null),
                "",
                responseListener,
                responseListener);
    }


    @Override
    public String getStoreDomain(){
        return STORE_DOMAIN;
    }
}
