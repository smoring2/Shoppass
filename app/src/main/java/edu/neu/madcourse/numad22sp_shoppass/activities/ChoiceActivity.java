package edu.neu.madcourse.numad22sp_shoppass.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;

import edu.neu.madcourse.numad22sp_shoppass.R;
import edu.neu.madcourse.numad22sp_shoppass.database.RealtimeDatabase;
import edu.neu.madcourse.numad22sp_shoppass.fcm.FCMUtil;

public class ChoiceActivity extends AppCompatActivity {
    private static final String TAG = "ChoiceActivity";
    private static String CLIENT_REGISTRATION_TOKEN;
    private static String SERVER_KEY;
    private SharedPreferences sharedPreferences;
    private String loginUser;
    private Boolean isBuyer;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choice);

        mAuth = FirebaseAuth.getInstance();

        try {
            setClientToken();
        } catch (InterruptedException e) {
            Log.e(TAG, "Something's wrong while setting client's token: " + e);
        }

        // If the user is already login, they will not have to re-login after they close the app.
        this.sharedPreferences = getApplicationContext().getSharedPreferences("Preference", MODE_PRIVATE);
        connectToFCM();
        Log.e(TAG, "login local cache: " + this.sharedPreferences.getAll().toString());

        // Only auto-login if there is a valid Firebase Auth session
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && this.sharedPreferences.contains("loginUser")) {
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

    /**
     * Auto-login only proceeds when FirebaseAuth confirms the user is still authenticated.
     * The cached prefs (loginUser, isBuyer) are used only for UI routing — the actual
     * identity comes from FirebaseAuth.
     */
    private void autoLogin() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // No valid Firebase Auth session — clear stale prefs and require re-login
            SharedPreferences.Editor editor = this.sharedPreferences.edit();
            editor.clear();
            editor.apply();
            Toast.makeText(this, "Session expired. Please sign in again.", Toast.LENGTH_LONG).show();
            return;
        }

        this.loginUser = this.sharedPreferences.getString("loginUser", null);
        this.isBuyer = this.sharedPreferences.getBoolean("isBuyer", false);

        if (this.loginUser == null) {
            return;
        }

        Intent intent;
        if (this.isBuyer) {
            intent = new Intent(ChoiceActivity.this, BuyerHomeActivity.class);
            intent.putExtra("username", this.loginUser);
        } else {
            intent = new Intent(ChoiceActivity.this, SellerHomeActivity.class);
            intent.putExtra("storename", this.loginUser);
            // Address will be fetched from the database by SellerHomeActivity if needed
            intent.putExtra("address", "");
        }
        startActivity(intent);
    }
}
