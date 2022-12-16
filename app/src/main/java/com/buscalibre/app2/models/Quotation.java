package com.buscalibre.app2.models;


import android.util.Log;

import com.buscalibre.app2.ApplicationJvm;
import com.buscalibre.app2.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Quotation extends ApiModel {

    // Keys for (de)serialization
    public static final String WARNING = "warning";
    public static final String WARNING_CODE = "code";

    public static final String CONDITION_NEW = "new";
    public static final String CONDITION_USED = "used";
    public static final String CONDITION_NEW_PRIME = "new prime";
    public static final String CONDITION_REFURBISHED = "refurbished";

    public static final String SHIPPING_METHOD_AIRPLANE = "avion";
    public static final String SHIPPING_METHOD_SHIP = "barco";

    /**
     * Array used for populating {@link #mShippingMethods}
     */
    private static final String[] CONDITION_KEYS =
            new String[]{
                    CONDITION_NEW,
                    CONDITION_USED,
                    CONDITION_NEW_PRIME,
                    CONDITION_REFURBISHED
            };

    /**
     * Array used for populating {@link #mShippingMethods}
     */
    private static final String[] SHIPPING_METHOD_KEYS =
            new String[]{
                    SHIPPING_METHOD_AIRPLANE,
                    SHIPPING_METHOD_SHIP
            };

    // Private fields
    private String mWarning;
    private Integer mWarningCode;

    /**
     * Dictionary of {@link ShippingMethod}, indexed by their condition name and their shipping method
     */
    private Map<String, Map<String, ShippingMethod>> mShippingMethods = new HashMap<>();

    /**
     * Empty constructor, needed for deserialization.
     */
    public Quotation() {
    }

    public static Quotation getQuotationFromJson(JSONObject quotationJson) {
        Quotation quotation = new Quotation();
        try {
            // We can directly use the keys of the expected JSONObject. If missing, a JSONException
            // will be thrown, and a quotation with a custom error will be constructed in the catch block
            quotation.setSuccess(true);
            if (quotation.isSuccessful()) {

                // Products with no valid shipping method should not be considered
                boolean atLeastOneShippingMethod = false;

                /**
                 * The response object keys are the possible conditions for the product (new, used...).
                 * Each condition has 0 or more keys representing the available shipping methods, which
                 * in turn contain all the info needed for building a {@link ShippingMethod}
                 */
                JSONObject response = quotationJson.getJSONObject("quoteData");

                for (String conditionKey : CONDITION_KEYS) {
                    if (response.has(conditionKey)) {
                        JSONObject condition = response.getJSONObject(conditionKey);
                        for (String shippingMethodKey : SHIPPING_METHOD_KEYS) {
                            Log.e("shippingMethodKey", shippingMethodKey);
                            if (condition.has(shippingMethodKey)) {
                                JSONObject shippingMethodJson = condition.getJSONObject(shippingMethodKey);
                                ShippingMethod shippingMethod = ShippingMethod.getShippingMethodFromJson(shippingMethodJson);


                                quotation.addShippingMethod(conditionKey, shippingMethodKey, shippingMethod);
                                Log.e("getMaxArrivalTime", shippingMethod.getMaxArrivalTime() + "");
                                Log.e("getPrice", shippingMethod.getPrice() + "");
                                atLeastOneShippingMethod = true;
                            }
                        }
                    }
                }

                // Throw error for products with no shipping method
                if(!atLeastOneShippingMethod) {
                    String jsonExceptionMessage =
                            String.format(
                                    //ApplicationJvm.getContext().getString(R.string.quotation_parsing_error),
                                    quotationJson.toString()
                            );
                    throw new JSONException(jsonExceptionMessage);
                }

                // Set the warning, if any
                /*if (quotationJson.has(WARNING)) {
                    quotation.setWarning(quotationJson.getString(WARNING));
                    quotation.setWarningCode(quotationJson.getInt(WARNING_CODE));
                }*/

            } else {
                // If the request was unsuccessful, add the error and its code
                //quotation.setError(quotationJson.getString(ERROR));
                //quotation.setErrorCode(quotationJson.getInt(ERROR_CODE));
            }
        } catch (Exception e) {
            // There was an error parsing the received quotation.
            Log.e("qerror", e.toString());
            quotation = new Quotation();
            quotation.setSuccess(false);
            //quotation.setError(ApplicationJvm.getContext().getString(R.string.error_parse));
            // Since this is not an error received from the back-end, it has no code, so it will be null
        }
        return quotation;
    }

    public String getWarning() {
        return mWarning;
    }

    public void setWarning(String warning) {
        mWarning = warning;
    }

    public Integer getWarningCode() {
        return mWarningCode;
    }

    public void setWarningCode(Integer warningCode) {
        mWarningCode = warningCode;
    }

    public void addShippingMethod(String conditionKey, String shippingMethodKey, ShippingMethod method) {

        if (!mShippingMethods.containsKey(conditionKey)) {
            mShippingMethods.put(conditionKey, new HashMap<String, ShippingMethod>());
        }
        mShippingMethods.get(conditionKey).put(shippingMethodKey, method);
    }

    /**
     * Returns the available shipping methods, indexed by product condition and shipping method kind
     */
    public Map<String, Map<String, ShippingMethod>> getShippingMethods() {
        return mShippingMethods;
    }
}
