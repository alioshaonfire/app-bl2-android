package com.buscalibre.app2.calls;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.buscalibre.app2.constants.Preferences;
import com.buscalibre.app2.models.TokenUpdated;
import com.buscalibre.app2.models.UserLogin;
import com.buscalibre.app2.network.RestClient;
import com.buscalibre.app2.network.RetrofitClientInstance;
import com.buscalibre.app2.util.ConfigUtil;
import com.google.gson.JsonObject;

import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class POSTFirebaseID {

    public static void refreshTokenFirebase(final Context context){
        UserLogin userLogin = Realm.getDefaultInstance().where(UserLogin.class).findFirst();
        if (userLogin != null){
            JsonObject json = new JsonObject();
            json.addProperty("firebasetoken", Preferences.getFCMToken(context));
            Log.e("updateFirebaseToken", Preferences.getFCMToken(context));
            RestClient restClient = RetrofitClientInstance.getRetrofitInstance().create(RestClient.class);
            Call<TokenUpdated> call = restClient.renewFirebaseToken(userLogin.getToken(), ConfigUtil.getLocaleISO639(), json);
            call.enqueue( new Callback<TokenUpdated>() {
                @Override
                public void onResponse(@NonNull Call<TokenUpdated> call, @NonNull Response<TokenUpdated> response) {
                    TokenUpdated tokenUpdated = response.body();
                    if (tokenUpdated != null){

                        Log.d("FCMtokenRenewed", response.message());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<TokenUpdated> call, @NonNull Throwable t) {
                    Log.d("FCMtokenRenewed", t.getMessage());
                    //CrashConfig.init(context,t);
                }
            });
        }
    }
}
