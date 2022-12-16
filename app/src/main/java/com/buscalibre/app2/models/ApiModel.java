package com.buscalibre.app2.models;

public class ApiModel {

    // Keys for (de)serialization
    public static final String RESPONSE = "response";
    public static final String SUCCESS = "success";
    public static final String ERROR = "error";
    public static final String ERROR_CODE = "error_code";

    // Private fields
    private boolean mSuccess;
    private String mError;
    private Integer mErrorCode;

    public boolean isSuccessful() {
        return mSuccess;
    }

    public void setSuccess(boolean success) {
        mSuccess = success;
    }

    public String getError() {
        return mError;
    }

    public void setError(String error) {
        mError = error;
    }

    public Integer getErrorCode() {
        return mErrorCode;
    }

    public void setErrorCode(Integer errorCode) {
        mErrorCode = errorCode;
    }
}
