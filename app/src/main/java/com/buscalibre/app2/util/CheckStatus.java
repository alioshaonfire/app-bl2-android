package com.buscalibre.app2.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.buscalibre.app2.activities.WelcomeUserActivity;
import com.buscalibre.app2.models.UserLogin;

import io.realm.Realm;

public class CheckStatus {

    public static void userLogin(Realm realm, Context activityContext){

        UserLogin userLogin = realm.where(UserLogin.class).findFirst();
        if (userLogin == null){
            Intent intent = new Intent(activityContext, WelcomeUserActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            activityContext.startActivity(intent);
            ((Activity)activityContext).finish();
        }
    }
}
