package com.buscalibre.app2.notifications;

import android.util.Log;

import com.buscalibre.app2.calls.POSTFirebaseID;
import com.buscalibre.app2.constants.Preferences;


public class TokenFirebase extends com.google.firebase.messaging.FirebaseMessagingService {

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.e("TokenFirebase", token);
        Preferences.setFCMToken(getApplicationContext(),token);
        POSTFirebaseID.refreshTokenFirebase(getApplicationContext());
    }
}
