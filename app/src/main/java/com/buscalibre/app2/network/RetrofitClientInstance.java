package com.buscalibre.app2.network;




import android.util.Log;

import com.buscalibre.app2.BuildConfig;
import com.buscalibre.app2.models.StoreConfig;

import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClientInstance {

    private static Retrofit retrofit;
    private static final String BASE_URL = BuildConfig.SERVER_URL;



    public static Retrofit getRetrofitInstance() {

        StoreConfig storeConfig = Realm.getDefaultInstance().where(StoreConfig.class).findFirst();
        int timeOut = 10;
        Log.e("timeoutDefault", String.valueOf(timeOut));

        if (storeConfig != null){
            timeOut = storeConfig.getTimeout();
            Log.e("timeoutConfig", String.valueOf(timeOut));
        }
        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(timeOut, TimeUnit.SECONDS)
                .writeTimeout(timeOut, TimeUnit.SECONDS)
                .readTimeout(timeOut, TimeUnit.SECONDS)
                .build();
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
