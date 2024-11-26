package com.school.vacationplanner.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.school.vacationplanner.R;

public class NotificationUtility {

    // constants
    private static final String TAG = "NotificationUtility";
    private static final String CHANNEL_ID = "vacation_alert_channel";
    private static final String CHANNEL_NAME = "Vacation Alerts";
    private static final String NOTIFICATION_TITLE = "Vacation Alert";


    // variables
    private static boolean notificationsEnabled = true;


    // custom methods
    public static void setNotificationsEnabled(boolean isEnabled) {
        Log.d(TAG, "setNotificationsEnabled: " + (isEnabled ? "Enabling" : "Disabling") + " notifications.");
        notificationsEnabled = isEnabled;
    }

    public static boolean areNotificationsEnabled() {
        Log.d(TAG, "areNotificationsEnabled: " + (notificationsEnabled ? "Notifications are enabled" : "Notifications are disabled"));
        return notificationsEnabled;
    }

    public static void showVacationNotification(Context context, String vacationTitle, String message) {
        Log.d(TAG, "showVacationNotification: Checking if notifications are enabled for vacation: " + vacationTitle);
        if (areNotificationsEnabled()) {
            Log.d(TAG, "showVacationNotification: Notifications are enabled. Showing notification for vacation: " + vacationTitle);
            sendNotification(context, vacationTitle, message);
        } else {
            Log.d(TAG, "showVacationNotification: Notifications are disabled. Skipping notification for vacation: " + vacationTitle);
        }
    }

    private static void sendNotification(Context context, String vacationTitle, String message) {
        Log.d(TAG, "sendNotification: Sending notification for vacation: " + vacationTitle);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Log.d(TAG, "sendNotification: NotificationManager initialized. Creating notification channel.");
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        notificationManager.createNotificationChannel(channel);

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.vacation_icon)
                .setContentTitle(NOTIFICATION_TITLE)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();
        Log.d(TAG, "sendNotification: Notification built. Sending notification.");
        notificationManager.notify(1, notification);
    }
}
