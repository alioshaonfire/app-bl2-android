package com.buscalibre.app2.util;

import android.text.TextUtils;

public class ValidateUtil {

    public static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public static boolean isEmpty(String string){
        if (string == null || string.isEmpty()){
            return true;
        }else {
            return false;
        }
    }
}
