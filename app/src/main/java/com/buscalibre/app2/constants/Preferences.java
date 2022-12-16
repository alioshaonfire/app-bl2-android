package com.buscalibre.app2.constants;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferences {

    private final static String NAME = "BLAPP_PREFS";
    private Preferences() {
    }
    public static String getFCMToken(final Context context) {
        final SharedPreferences settings = context.getSharedPreferences(NAME, 0);
        return settings.getString("fcm_token", "");
    }

    public static void setFCMToken(Context context, String token) {
        final SharedPreferences settings = context.getSharedPreferences(NAME, 0);
        final SharedPreferences.Editor edit = settings.edit();
        edit.putString("fcm_token", token);
        edit.apply();
    }

    public static boolean getIsTutorialHide(final Context context) {
        final SharedPreferences settings = context.getSharedPreferences(NAME, 0);
        return settings.getBoolean("is_tutorial_hide", false);
    }

    public static void setIsTutorialHide(Context context, Boolean status) {
        final SharedPreferences settings = context.getSharedPreferences(NAME, 0);
        final SharedPreferences.Editor edit = settings.edit();
        edit.putBoolean("is_tutorial_hide", status);
        edit.apply();
    }
}
