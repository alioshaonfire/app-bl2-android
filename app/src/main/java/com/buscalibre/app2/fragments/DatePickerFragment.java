package com.buscalibre.app2.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.DatePicker;

import androidx.fragment.app.DialogFragment;

import com.buscalibre.app2.models.Seller_;

import java.util.Calendar;

import io.realm.Realm;

public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    private DatePickerDialog.OnDateSetListener listener;

    public static DatePickerFragment newInstance(DatePickerDialog.OnDateSetListener listener) {
        DatePickerFragment fragment = new DatePickerFragment();
        fragment.setListener(listener);
        return fragment;
    }

    public void setListener(DatePickerDialog.OnDateSetListener listener) {
        this.listener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePicker = new DatePickerDialog(getActivity(), listener, year, month, day);
        Seller_ seller_ = Realm.getDefaultInstance().where(Seller_.class).findFirst();
        Calendar min_date_c;
        Calendar max_date_c;
        if (seller_ != null){
            min_date_c = Calendar.getInstance();
            min_date_c.set(Calendar.DAY_OF_MONTH, day + seller_.getMinDays());
            datePicker.getDatePicker().setMinDate(min_date_c.getTimeInMillis());
            max_date_c = Calendar.getInstance();
            max_date_c.set(Calendar.DAY_OF_MONTH, day + seller_.getMaxDays());
            datePicker.getDatePicker().setMaxDate(max_date_c.getTimeInMillis());
        }else {
            min_date_c = Calendar.getInstance();
            datePicker.getDatePicker().setMinDate(min_date_c.getTimeInMillis());
            max_date_c = Calendar.getInstance();
            max_date_c.set(Calendar.YEAR, year + 2);
            datePicker.getDatePicker().setMaxDate(max_date_c.getTimeInMillis());
        }


        //Disable all SUNDAYS and SATURDAYS between Min and Max Dates
        for (Calendar loopdate = min_date_c; min_date_c.before(max_date_c); min_date_c.add(Calendar.DATE, 1), loopdate = min_date_c) {
            int dayOfWeek = loopdate.get(Calendar.DAY_OF_WEEK);
            if (dayOfWeek == Calendar.SUNDAY || dayOfWeek == Calendar.SATURDAY) {
                Calendar[] disabledDays =  new Calendar[1];
                disabledDays[0] = loopdate;
                //datePicker.getDatePicker().setDisabledDays(disabledDays);

            }
        }

        datePicker.getDatePicker().setMinDate(System.currentTimeMillis() + 86400000);

        // Create a new instance of DatePickerDialog and return it
        return datePicker;
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        // Do something with the date chosen by the user
    }

}
