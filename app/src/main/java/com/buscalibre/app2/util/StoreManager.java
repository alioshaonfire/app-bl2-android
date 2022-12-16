package com.buscalibre.app2.util;

import android.content.Context;

import androidx.annotation.StringDef;

import com.buscalibre.app2.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by samuel on 20-09-17.
 */

public class StoreManager {

    /**
     * Returns the URL of the home of a given store
     * @param context {@link Context} used for reading the string resources
     * @param store {@link Store} string
     * @return the URL of the received Store.
     */
    public static String getStoreUrl(Context context, @Store String store) {
        String url = null;
        switch (store) {
            case Store.AMAZON:
                url = context.getString(R.string.url_amazon);
                break;
            case Store.EBAY:
                url = context.getString(R.string.url_ebay);
                break;
            case Store.BHPHOTO:
                url = context.getString(R.string.url_bhphoto);
                break;
        }
        return url;
    }

    /**
     * Returns a new instance of a {@link ProductPageParser}, according to the received store
     *
     */
    public static ProductPageParser getProductPageParser(@Store String store) {
        ProductPageParser parser = null;
        switch (store) {
            case Store.AMAZON:
                parser = new AmazonProductPageParser();
                break;
            case Store.EBAY:
                parser = new EbayProductPageParser();
                break;
            case Store.BHPHOTO:
                parser = new BhPhotoProductPageParser();
                break;
        }
        return parser;
    }

    /**
     * Defines an enum annotation for the possible kinds of stores.
     * This is preferred over regular enums in android.
     * References:
     * https://developer.android.com/studio/write/annotations.html#enum-annotations
     * https://stackoverflow.com/a/45175692/370798
     */
    @Retention(RetentionPolicy.SOURCE)
    @StringDef({Store.AMAZON, Store.EBAY, Store.BHPHOTO, Store.NOVEDADES, Store.VENDIDOS, Store.CODESCAN, Store.SEARCH})
    public @interface Store {
        // Possible Stores (note that by definition these fields are public, static and final)
        String AMAZON = "amazon";
        String EBAY = "ebay";
        String BHPHOTO = "bhphoto";

        String NOVEDADES = "novedades";
        String VENDIDOS = "vendidos";
        String CODESCAN = "codescan";
        String SEARCH = "search";
    }
}
