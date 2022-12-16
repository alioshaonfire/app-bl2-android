package com.buscalibre.app2.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.DisplayMetrics;
import android.view.ContextThemeWrapper;

import com.buscalibre.app2.R;

/**
 * Exposes utility methods for displaying or helping in the display of elements
 * on screen
 */
public class GraphicUtils {

    /**
     * Converts dps to actual pxs of the current display
     *
     * @param context {@link Context} to use for obtaining the display metrics
     * @param dp      Length to convert, in dps
     * @return Converted length, in pxs
     */
    public static int dpToPx(Context context, int dp) {
        // References:
        // https://stackoverflow.com/a/8490361
        // https://developer.android.com/guide/practices/screens_support.html#dips-pels
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return (int) ((dp * displayMetrics.density) + 0.5f);
    }

    public static void showDialog(Context context, String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.AlertDialogCustom));
        builder.setTitle(context.getResources().getString(R.string.app_name))
                .setMessage(message)
                .setPositiveButton(context.getString(R.string.accept_dialog), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }
}
