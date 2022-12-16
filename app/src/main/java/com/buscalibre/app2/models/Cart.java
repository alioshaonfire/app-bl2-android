package com.buscalibre.app2.models;
;

import com.buscalibre.app2.ApplicationJvm;
import com.buscalibre.app2.R;

import org.json.JSONException;
import org.json.JSONObject;

public class Cart extends ApiModel {

    // Key for (de)serialization
    public static final String URI = "uri";

    private String mUri;

    /**
     * Empty constructor, needed for deserialization.
     */
    public Cart() {
    }



    public static Cart getCartFromJson(JSONObject cartJson) {
        Cart cart = new Cart();
        try {
            // We can directly use the keys of the expected JSONObject. If missing, a JSONException
            // will be thrown, and a cart with a custom error will be constructed in the catch block
            cart.setSuccess(cartJson.getBoolean(SUCCESS));
            if (cart.isSuccessful()) {

                /**
                 * The response object contains the cart uri
                 */
                JSONObject response = cartJson.getJSONObject(RESPONSE);
                cart.setUri(response.getString(URI));
            } else {
                // If the request was unsuccessful, add the error and its code
                cart.setError(cartJson.getString(ERROR));
                cart.setErrorCode(cartJson.getInt(ERROR_CODE));
            }
        } catch (JSONException e) {
            // There was an error parsing the received quotation.
            cart = new Cart();
            cart.setSuccess(false);
            //cart.setError(ApplicationJvm.getContext().getString(R.string.error_parse));
            // Since this is not an error received from the back-end, it has no code, so it will be null
        }
        return cart;
    }

    public String getUri() {
        return mUri;
    }

    public void setUri(String uri) {
        mUri = uri;
    }
}
