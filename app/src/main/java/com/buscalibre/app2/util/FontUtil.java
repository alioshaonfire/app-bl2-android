package com.buscalibre.app2.util;

import android.content.Context;
import android.graphics.Typeface;

public class FontUtil {

    public static Typeface getMonserratBoldTypeface(Context context) {
        return Typeface.createFromAsset(context.getAssets(), "fonts/monserrat_bold.otf");
    }

    public static Typeface getArialRegularTypeface(Context context) {
        return Typeface.createFromAsset(context.getAssets(), "fonts/arial_regular.ttf");
    }

    public static Typeface getArialBoldTypeface(Context context) {
        return Typeface.createFromAsset(context.getAssets(), "fonts/arial_bold.ttf");
    }
    public static Typeface getMonserratLightTypeface(Context context) {
        return Typeface.createFromAsset(context.getAssets(), "fonts/montserrat_light.otf");
    }
    public static Typeface getMonserratRegularTypeface(Context context) {
        return Typeface.createFromAsset(context.getAssets(), "fonts/montserrat_regular.otf");
    }

    public static Typeface getSfprodisplayRegularTypeface(Context context) {
        return Typeface.createFromAsset(context.getAssets(), "fonts/SFProDisplay-Regular.ttf");
    }

    public static Typeface getLobsterRegularTypeface(Context context) {
        return Typeface.createFromAsset(context.getAssets(), "fonts/Lobster-Regular.ttf");
    }
}
