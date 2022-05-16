package edu.neu.madcourse.numad22sp_shoppass.fcm;

import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.util.Locale;

import edu.neu.madcourse.numad22sp_shoppass.R;
import edu.neu.madcourse.numad22sp_shoppass.activities.BuyerHomeActivity;
import edu.neu.madcourse.numad22sp_shoppass.activities.PromotionActivity;

/**
 * Reference: https://github.com/firebase/quickstart-android/blob/320f5fb45f155de3daf8b997c3788a4a187a024d/messaging/app/src/main/java/com/google/firebase/quickstart/fcm/java/MyFirebaseMessagingService.java
 */
public class FCMService extends FirebaseMessagingService {
    private static final String TAG = "FCMService";
    private SharedPreferences sharedPreferences;

    /**
     * Messages received. There are two types of message:
     * 1. Data messages are handled in this function whether the app is in the foreground or background.
     * 2. Notification messages are only received in this function when the app is in the foreground.
     * Not getting messages here? See why this may be: https://goo.gl/39bRNJ
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if (remoteMessage.getData().size() > 0) {
            Log.e(TAG, "Message data payload: " + remoteMessage.getData());
        }

        this.sharedPreferences = getApplicationContext().getSharedPreferences("Preference", MODE_PRIVATE);
        if (this.sharedPreferences.contains("loginUser")) {
            sendNotification(remoteMessage);
        }
    }

    /**
     * There are two scenarios when onNewToken is called:
     *   1) When a new token is generated on initial app startup
     *   2) Whenever an existing token is changed
     *
     * Under #2, there are three scenarios when the existing token is changed:
     *   A) App is restored to a new device
     *   B) User uninstalls/reinstalls the app
     *   C) User clears app data
     * @param token
     */
    @Override
    public void onNewToken(String token) {
        Log.e(TAG, "Refreshed token: " + token);
        super.onNewToken(token);
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param remoteMessage Received message
     */
    @RequiresApi(api = Build.VERSION_CODES.P)
    private void sendNotification(RemoteMessage remoteMessage) {
        Log.e(TAG, "Create new notification: " + remoteMessage.getData());

        Intent intent = redirectToPromotionPage(remoteMessage);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle(Html.fromHtml(String.format(Locale.getDefault(), "<strong>%s</strong> has a new promotion!", remoteMessage.getData().get("store"))))
                        .setContentText("Check out our latest promotion\uD83D\uDE00")
                        .setAutoCancel(false)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent)
                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                        .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0, notificationBuilder.build());
    }

    private Intent redirectToPromotionPage(RemoteMessage remoteMessage) {
        Intent intent = new Intent(this, BuyerHomeActivity.class);
        Bundle b = new Bundle();
        b.putString("promotion_id", remoteMessage.getData().get("promotion_id"));
        b.putString("storename", remoteMessage.getData().get("store"));
        b.putString("username", this.sharedPreferences.getString("loginUser", null));
        b.putBoolean("isBuyer", true);
        intent.putExtras(b);

        return intent;
    }
}