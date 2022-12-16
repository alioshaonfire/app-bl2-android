package com.buscalibre.app2.network;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;

public interface ApiResponseListener<T> extends Listener<T>, ErrorListener {
}
