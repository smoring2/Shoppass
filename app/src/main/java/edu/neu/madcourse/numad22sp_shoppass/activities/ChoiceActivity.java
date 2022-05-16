package edu.neu.madcourse.numad22sp_shoppass.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.google.firebase.messaging.FirebaseMessaging;
import edu.neu.madcourse.numad22sp_shoppass.R;
import edu.neu.madcourse.numad22sp_shoppass.fcm.FCMUtil;

public class ChoiceActivity extends AppCompatActivity {
    private static final String TAG = "ChoiceActivity";
    private static String CLIENT_REGISTRATION_TOKEN;
    private static String SERVER_KEY;
    private SharedPreferences sharedPreferences;
    private String loginUser;
    private String loginPassword;
    private Boolean isBuyer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choice);

        try {
            setClientToken();
        } catch (InterruptedException e) {
            Log.e(TAG, "Something's wrong while setting client's token: " + e);
        }

        // If the user is already login, they will not have to re-login after they close the app.
        this.sharedPreferences = getApplicationContext().getSharedPreferences("Preference", MODE_PRIVATE);
        connectToFCM();
        Log.e(TAG, "login local cache: " + this.sharedPreferences.getAll().toString());

        if (this.sharedPreferences.contains("loginUser")) {
            this.loginUser = this.sharedPreferences.getString("loginUser", null);
            Toast toast = Toast.makeText(getApplicationContext(), "User already login", Toast.LENGTH_SHORT);
            toast.show();
            autoLogin();
        }
    }

    public void chooseClick(View view) {
        Intent intent = new Intent(ChoiceActivity.this, LoginActivity.class);

        switch (view.getId()) {
            case R.id.btn_buyer:
                intent.putExtra("buyer", true);
                break;
            case R.id.btn_seller:
                intent.putExtra("buyer", false);
                break;
        }
        startActivity(intent);
    }

    private void connectToFCM() {
        // Connect to FCM
        try {
            SERVER_KEY = "key=" + FCMUtil.getProperties(getApplicationContext()).getProperty("SERVER_KEY");
            SharedPreferences.Editor editor = this.sharedPreferences.edit();
            editor.putString("serverKey", SERVER_KEY);
            editor.apply();
        } catch (Exception e) {
            Log.e(TAG, "Something's wrong while getting FCM server key: " + e);
        }
    }

    /**
     * Get the device/client token for the user.
     * Note that we restricted each device should only have one user. If we want to login another user, one should go to settings and clear the app data to refresh client token.
     */
    private void setClientToken() throws InterruptedException {
        Thread thread = new Thread(() -> FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Toast.makeText(this, "Something is wrong when getting client token!", Toast.LENGTH_SHORT).show();
            } else {
                CLIENT_REGISTRATION_TOKEN = task.getResult();
                Log.e(TAG, "client registration token: " + CLIENT_REGISTRATION_TOKEN);
            }
        }));

        thread.start();
        thread.join();
    }

    private void autoLogin() {
        this.loginUser = this.sharedPreferences.getString("loginUser", null);
        this.loginPassword = this.sharedPreferences.getString("loginPassword", null);
        this.isBuyer = this.sharedPreferences.getBoolean("isBuyer", false);

        Intent intent;
        if (this.isBuyer) {
            intent = new Intent(ChoiceActivity.this, BuyerHomeActivity.class);
            intent.putExtra("username", this.loginUser);
        } else {
            intent = new Intent(ChoiceActivity.this, SellerHomeActivity.class);
            intent.putExtra("storename", this.loginUser);
            intent.putExtra("address", this.loginPassword);
        }
        startActivity(intent);
    }
}