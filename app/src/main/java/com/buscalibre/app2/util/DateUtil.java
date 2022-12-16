package com.buscalibre.app2.util;

import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtil {

    public static String convertDate(String inputDate, String outputFormat) {

        DateFormat theDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        theDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = null;

        try {
            date = theDateFormat.parse(inputDate);
        } catch (Exception ignored) {
            Log.e("parseDateError", ignored.toString());
        }

        theDateFormat = new SimpleDateFormat(outputFormat);
        theDateFormat.setTimeZone(TimeZone.getDefault());

        return theDateFormat.format(date);
    }

    public static String convertDeviceDate(String inputDate, String outputFormat) {

        DateFormat theDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        theDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = null;

        try {
            date = theDateFormat.parse(inputDate);
        } catch (ParseException ignored) {
        } catch(Exception exception) {
        }

        theDateFormat = new SimpleDateFormat(outputFormat);
        theDateFormat.setTimeZone(TimeZone.getDefault());

        return theDateFormat.format(date);
    }

    public static String convertDateChart(String inputDate) {

        DateFormat theDateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        theDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = null;

        try {
            date = theDateFormat.parse(inputDate);
        } catch (ParseException ignored) {
        } catch(Exception ignored) {
        }

        theDateFormat = new SimpleDateFormat("HH:mm");
        theDateFormat.setTimeZone(TimeZone.getDefault());

        return theDateFormat.format(date);
    }
}
