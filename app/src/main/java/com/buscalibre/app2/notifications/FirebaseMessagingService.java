package com.buscalibre.app2.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.buscalibre.app2.R;
import com.buscalibre.app2.activities.BaseWebViewActivity;
import com.buscalibre.app2.calls.POSTFirebaseID;
import com.buscalibre.app2.constants.AppConstants;
import com.buscalibre.app2.constants.Preferences;
import com.buscalibre.app2.models.InboxMessages;
import com.buscalibre.app2.models.UserLogin;
import com.google.firebase.messaging.RemoteMessage;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import io.realm.Realm;


public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    private String TAG = FirebaseMessagingService.class.getSimpleName();
    private String title;
    private String key;
    private String body;
    private String code;
    private String data;



    @Override
    public void onMessageReceived(@NotNull RemoteMessage remoteMessage) {

        try {
            code = remoteMessage.getData().get("code");
            if (code != null){
                Log.e("code", code);
                if (code.equals("3")){
                    //TODO: refresh cart
                }
            }
            body = Objects.requireNonNull(remoteMessage.getNotification()).getBody();
            if (body != null){
                Log.e("body", body);
            }
            data = remoteMessage.getData().get("data");
            if (data != null){
                Log.e("data", data);
            }
            title = remoteMessage.getNotification().getTitle();
            if (title != null){
                Log.e("title", title);
            }
            if (title != null){
                if (code.equals(AppConstants.NOTIFICATION_WITHOUTDATA)){
                    createNotification(title, body, "");

                }else if (code.equals(AppConstants.NOTIFICATION_HASDATA) && data != null){
                    createNotification(title, body, data);
                }
            }else {
                Log.e("silentNotification", remoteMessage.getNotification().getBody());
            }

        }catch (Exception e){
            Log.e("errorNotification", e.toString());
        }
    }


    public void createNotification(String title, String message, String url) {
        UserLogin userLogin = Realm.getDefaultInstance().where(UserLogin.class).findFirst();

        if (!url.isEmpty()){
            Intent resultIntent = new Intent(this , BaseWebViewActivity.class);
            resultIntent.putExtra("url", url);
            resultIntent.putExtra("title", title);
            //resultIntent.putExtra("header", userLogin.getWebToken());
            resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent resultPendingIntent = PendingIntent.getActivity(this,
                    0, resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
            mBuilder.setSmallIcon(R.mipmap.ic_launcher);
            mBuilder.setContentTitle(title)
                    .setContentText(message)
                    .setAutoCancel(false)
                    .setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
            mBuilder.setContentIntent(resultPendingIntent);
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel notificationChannel = new NotificationChannel("1", "NOTIFICATION_CHANNEL_NAME", importance);
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(Color.RED);
                notificationChannel.enableVibration(true);
                notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                assert mNotificationManager != null;
                mBuilder.setChannelId("1");
                mNotificationManager.createNotificationChannel(notificationChannel);
            }
            assert mNotificationManager != null;
            mNotificationManager.notify(0 /* Request Code */, mBuilder.build());
        }else {
            Intent resultIntent = new Intent(this , InboxMessages.class);
            resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent resultPendingIntent = PendingIntent.getActivity(this,
                    0, resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
            mBuilder.setSmallIcon(R.mipmap.ic_launcher);
            mBuilder.setContentTitle(title)
                    .setContentText(message)
                    .setAutoCancel(false)
                    .setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
            mBuilder.setContentIntent(resultPendingIntent);
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel notificationChannel = new NotificationChannel("1", "NOTIFICATION_CHANNEL_NAME", importance);
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(Color.RED);
                notificationChannel.enableVibration(true);
                notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                assert mNotificationManager != null;
                mBuilder.setChannelId("1");
                mNotificationManager.createNotificationChannel(notificationChannel);
            }
            assert mNotificationManager != null;
            mNotificationManager.notify(0 /* Request Code */, mBuilder.build());
        }

    }


    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.e("TokenFirebase", s);
        Preferences.setFCMToken(getApplicationContext(),s);
        POSTFirebaseID.refreshTokenFirebase(getApplicationContext());
    }
}
