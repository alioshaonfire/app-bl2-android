package com.buscalibre.app2.util;

import java.util.Locale;

public class ConfigUtil {

    public static String getLocaleISO639(){
        return Locale.getDefault().toString();
    }
}
