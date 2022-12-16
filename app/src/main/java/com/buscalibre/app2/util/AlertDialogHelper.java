package com.buscalibre.app2.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;

import com.buscalibre.app2.R;


public class AlertDialogHelper {

    Context context;
    AlertDialog alertDialog=null;
    AlertDialogListener callBack;
    Activity current_activity;

    public AlertDialogHelper(Context context) {
        this.context = context;
        this.current_activity = (Activity) context;
        callBack = (AlertDialogListener) context;

    }

    public void dismiss(){
        alertDialog.dismiss();
    }

    /**
     * Displays the AlertDialog with 3 Action buttons
     *
     * you can set cancelable property
     *
     * @param title
     * @param message
     * @param positive
     * @param negative
     * @param neutral
     * @param from
     * @param isCancelable
     */
    public void showAlertDialog(String title,String message,String positive,String negative,String neutral,final int from,boolean isCancelable) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.AlertDialogCustom));

        if(!TextUtils.isEmpty(title))
            alertDialogBuilder.setTitle(title);
        if(!TextUtils.isEmpty(message))
            alertDialogBuilder.setMessage(message);

        if(!TextUtils.isEmpty(positive)) {
            alertDialogBuilder.setPositiveButton(positive,
                    (arg0, arg1) -> {

                        callBack.onPositiveClick(from);
                        alertDialog.dismiss();
                    });
        }
        if(!TextUtils.isEmpty(neutral)) {
            alertDialogBuilder.setNeutralButton(neutral,
                    (arg0, arg1) -> {
                        callBack.onNeutralClick(from);
                        alertDialog.dismiss();
                    });
        }
        if(!TextUtils.isEmpty(negative))
        {
            alertDialogBuilder.setNegativeButton(negative,
                    (arg0, arg1) -> {
                        callBack.onNegativeClick(from);
                        alertDialog.dismiss();
                    });
        }

        alertDialogBuilder.setCancelable(isCancelable);


        alertDialog = alertDialogBuilder.create();
        alertDialog.show();

        if(TextUtils.isEmpty(negative))
            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);
    }


    public interface AlertDialogListener {
        void onPositiveClick(int from);
        void onNegativeClick(int from);
        void onNeutralClick(int from);
    }

}


