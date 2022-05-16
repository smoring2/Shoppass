package edu.neu.madcourse.numad22sp_shoppass.fcm;

import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;
import android.content.Context;
import android.content.res.AssetManager;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import edu.neu.madcourse.numad22sp_shoppass.R;
import edu.neu.madcourse.numad22sp_shoppass.components.Promotion;

public class FCMUtil {
    private static final String TAG = "FCMUtils";

    public static void subscribeToStore(Context context, String store) {
        FirebaseMessaging.getInstance().subscribeToTopic(store)
                .addOnCompleteListener(task -> {
                    String msg;
                    if (!task.isSuccessful()) {
                        msg = context.getString(R.string.msg_subscribed_fail);
                    } else {
                        msg = context.getString(R.string.msg_subscribed_success);
                    }
                    Log.e(TAG, "Subscribe to " + store);
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                });
    }

    public static void unSubscribeToStore(Context context, String store) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(store)
                .addOnCompleteListener(task -> {
                    String msg;
                    if (!task.isSuccessful()) {
                        msg = context.getString(R.string.msg_unsubscribed_fail);
                    } else {
                        msg = context.getString(R.string.msg_unsubscribed_success);
                    }
                    Log.e(TAG, "Unsubscribe to " + store);
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                });
    }

    public static void sendMessageToSubscribers(Context context, String serverToken, Promotion promotion) {
        new Thread(() -> {
            try {
                // Prepare data
                JSONObject jPayload = new JSONObject();
                JSONObject jData = new JSONObject();
                Log.e(TAG, promotion.store);
                Log.e(TAG, promotion.title);
                Log.e(TAG, promotion.description);
                Log.e(TAG, promotion.id);

                try {
                    jData.put("store", promotion.store);
                    jData.put("title", promotion.title);
                    jData.put("eligibleProducts", promotion.eligibleProducts);
                    jData.put("description", promotion.description);
                    jData.put("promotion_id", promotion.id);

                    jPayload.put("to", "/topics/" + promotion.store.replace(" ", ""));
                    jPayload.put("priority", "high");
                    jPayload.put("data", jData);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                final String resp = fcmHttpConnection(serverToken, jPayload);
                Log.e(TAG, "SEND MESSAGE TO SUBSCRIBER RESPONSE: " + resp);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static String fcmHttpConnection(String serverToken, JSONObject jsonObject) throws InterruptedException {
        final String[] response = new String[1];
        Thread thread = new Thread(() -> {
            try {
                URL url = new URL("https://fcm.googleapis.com/fcm/send");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", serverToken);
                conn.setDoOutput(true);

                // Send FCM message content.
                OutputStream outputStream = conn.getOutputStream();
                outputStream.write(jsonObject.toString().getBytes());
                outputStream.close();

                // Read FCM response.
                InputStream inputStream = conn.getInputStream();
                response[0] = convertStreamToString(inputStream);
            } catch (IOException e) {
                response[0] = null;
            }
        });

        thread.start();
        thread.join();
        return response[0];
    }

    public static String convertStreamToString(InputStream inputStream) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String len;
            while ((len = bufferedReader.readLine()) != null) {
                stringBuilder.append(len);
            }
            bufferedReader.close();
            return stringBuilder.toString().replace(",", ",\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void postToastMessage(final String message, final Context context){
        Handler handler = new Handler(Looper.getMainLooper());

        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    public static Properties getProperties(Context context) {
        Properties properties = new Properties();
        AssetManager assetManager = context.getAssets();
        InputStream inputStream;
        try {
            inputStream = assetManager.open("firebase.properties");
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return properties;
    }
}