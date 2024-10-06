package com.dukeKart.android.firebase;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.NotificationCompat;


import com.dukeKart.android.database.AppConstants;
import com.dukeKart.android.database.Preferences;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.dukeKart.android.R;


import org.json.JSONException;
import org.json.JSONObject;

public class MyMessagingService extends FirebaseMessagingService {

    public static final int NOTIFICATION_ID = 100;
    private static final String TAG = "MyFMService";
    Preferences preferences;
    JSONObject json;
    Thread readthread;
    SharedPreferences mSettings;
    SharedPreferences.Editor editor;
    String CHANNEL_ID = "123";

    int count = 0;
    private NotificationManager mManager;

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        preferences = new Preferences(this);
        preferences.set(AppConstants.fcmToken, s);
        preferences.commit();
        Log.e("newToken", s);


        getSharedPreferences("_", MODE_PRIVATE).edit().putString("fb", s).apply();
    }

    /*@Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.v("mnbv", String.valueOf(remoteMessage));

        final Map<String, String> body = remoteMessage.getData();

        // final String body = String.valueOf(remoteMessage.getData());
        Log.v("allData", String.valueOf(body));

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {

                Intent intent = new Intent("com.pickyourload.android");
                try {
                    json = new JSONObject(body);

                    Uri alarmSound;

                    alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                    createNotification(getString(R.string.app_name),json.getString("body"),alarmSound);

                    Log.v("jsonNotification", String.valueOf(json));

                    if (json.has("notification_type")) {
                        if (json.getString("notification_type").equals("02")) {
                            JSONObject jsonObject = json.getJSONObject("booking_details");
                            Log.v("welknf", String.valueOf(jsonObject));
                            JSONObject booking_data = jsonObject.getJSONObject("booking_data");
                            JSONObject driverdata = jsonObject.getJSONObject("driverdata");
                            intent.putExtra("notification_type", json.getString("notification_type"));
                            intent.putExtra("base_fare", booking_data.getString("deliveFare"));
                            intent.putExtra("rate_per_km", booking_data.getString("rate_per_km"));
                            intent.putExtra("km", booking_data.getString("km"));
                            intent.putExtra("tax", booking_data.getString("tax"));
                            intent.putExtra("loaderCharge", booking_data.getString("loaderCharge"));
                            intent.putExtra("name", driverdata.getString("name"));
                            intent.putExtra("driver_id", booking_data.getString("driver_id"));
                            intent.putExtra("bookingId", booking_data.getString("id"));
                            if (!driverdata.isNull("image") || !driverdata.getString("image").equals(""))
                                intent.putExtra("image", driverdata.getString("image"));
                            else
                                intent.putExtra("image", "");

                        }
                        else if (json.getString("notification_type").equals("3")) {
                            intent.putExtra("notification_type", json.getString("notification_type"));
                            JSONObject jsonObject = json.getJSONObject("booking_details");

                            intent.putExtra("pickupLat", jsonObject.getString("pickupLat"));
                            intent.putExtra("pickupLong", jsonObject.getString("pickupLong"));
                            intent.putExtra("driver_id", jsonObject.getString("driver_id"));

                        } else
                            intent.putExtra("notification_type", json.getString("notification_type"));

                    } else
                        intent.putExtra("notification_type", "100");

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                sendBroadcast(intent);
            }
        });
    }*/


    // Playing notification sound
    public void playNotificationSound() {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void createNotification(String title, String message, Intent intent) {
        playNotificationSound();
        mSettings = getSharedPreferences("MyUniquePreferenceFile", MODE_PRIVATE);

        int c = mSettings.getInt("count", 0);
        int count = 0;
        count = count + c;

        Log.e("Counting0", "" + count);
        editor = mSettings.edit();
        editor.putInt("count", count);
        editor.putString("Notify", "yes");
        Log.e("NOTIFICATION", " Notification coming");
        editor.commit();
        Log.e("Counting2", "" + count);
//        DashBoard.updateMyActivity(getApplicationContext(), message);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Uri defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel androidChannel = new NotificationChannel(CHANNEL_ID,
                    title, NotificationManager.IMPORTANCE_HIGH);
            // Sets whether notifications posted to this channel should display notification lights
            androidChannel.enableLights(true);
            // Sets whether notification posted to this channel should vibrate.
            androidChannel.enableVibration(true);
            // Sets the notification light color for notifications posted to this channel
            androidChannel.setLightColor(R.color.colorPrimary);
            //androidChannel.setSound(null, null);

            // Sets whether notifications posted to this channel appear on the lockscreen or not
            androidChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            getManager().createNotificationChannel(androidChannel);

            NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setContentTitle(title)
                    .setNumber(++count)
                    .setContentText(message)
                    .setTicker(title)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                    .setSound(defaultUri)
                    //.setSound(alarmSound, STREAM_NOTIFICATION)
                    .setAutoCancel(true)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setContentIntent(contentIntent);

            getManager().notify(NOTIFICATION_ID, notification.build());

        } else {
            try {

                @SuppressLint({"NewApi", "LocalSuppress"}) NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this).setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_foreground))
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle(title)
                        .setTicker(title)
                        .setNumber(++count)
                        .setContentText(message)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                        .setContentIntent(contentIntent)
                        .setDefaults(Notification.DEFAULT_ALL)
                        //.setSound(alarmSound, STREAM_NOTIFICATION)
                        .setLights(0xFF760193, 300, 1000)
                        .setAutoCancel(true)
                        .setVibrate(new long[]{200, 400});


                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(NOTIFICATION_ID/* ID of notification */, notificationBuilder.build());

            } catch (SecurityException se) {
                se.printStackTrace();
            }
        }
    }

    private void handleNotification(RemoteMessage.Notification notification) {
        String message = notification.getBody();


        Log.e("Counting0", "" + message);


    }


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(TAG, "FCM Message Id: " + remoteMessage.getNotification().getTag());
        Log.d(TAG, "FCM Notification Message: " + remoteMessage.getData() + "...." + remoteMessage.getFrom());

        playNotificationSound();
        preferences = new Preferences(this);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {

/*                DashBoard_Frag.imNotify.setVisibility(View.VISIBLE);
                preferences.set(AppSettings.notify, "true");
                preferences.commit();*/
            }
        });

        preferences.set("MyNotifications", "true");
        preferences.commit();
        if (remoteMessage.getNotification() != null) {
            Log.e(TAG, "Notification Body: " + remoteMessage.getNotification().getBody());
            handleNotification(remoteMessage.getNotification());

        }
        Intent resultIntent = new Intent("com.dukekart.android");
        resultIntent.putExtra("Notification", "notify");
        try {
            if (remoteMessage.getData() != null) {


//
               /* if (remoteMessage.getData().containsKey("notification_type")) {

                    if (remoteMessage.getData().get("notification_type").equals("02")) {
                        JSONObject jsonObject = new JSONObject(remoteMessage.getData().get("booking_details"));
                        Log.v("CheckNotifyVal", String.valueOf(jsonObject));

                        resultIntent.putExtra("name", jsonObject.getString("name"));
                        resultIntent.putExtra("mobile", jsonObject.getString("mobile"));
                        resultIntent.putExtra("otp", jsonObject.getString("otp"));
                        resultIntent.putExtra("notification_type", remoteMessage.getData().get("notification_type"));
                        AppSettings.putString(AppSettings.lastBookingId,jsonObject.getString("id"));


                    } else if (remoteMessage.getData().get("notification_type").equals("09")) {
                        JSONObject jsonObject = new JSONObject(remoteMessage.getData().get("booking_details"));
                        resultIntent.putExtra("notification_type", remoteMessage.getData().get("notification_type"));

                       *//* resultIntent.putExtra("pickupLat", jsonObject.getString("pickupLat"));
                        resultIntent.putExtra("pickupLong", jsonObject.getString("pickupLong"));
                        resultIntent.putExtra("driver_id", jsonObject.getString("driver_id"));*//*

                    } else
                        resultIntent.putExtra("notification_type", remoteMessage.getData().get("notification_type"));
                }*/

            } else
                resultIntent.putExtra("notification_type", "100");
        } catch (Exception e) {
            e.printStackTrace();
        }
//
        createNotification(getString(R.string.app_name), remoteMessage.getNotification().getBody(), resultIntent);
        sendBroadcast(resultIntent);
        JSONObject json = null;
        try {
            json = new JSONObject(remoteMessage.getData().toString());
            Log.v("MyPvalue", json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }


    private NotificationManager getManager() {
        if (mManager == null) {
            mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mManager;
    }


}