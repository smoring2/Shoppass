package edu.neu.madcourse.numad22sp_shoppass.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;

import edu.neu.madcourse.numad22sp_shoppass.R;
import edu.neu.madcourse.numad22sp_shoppass.components.Customer;
import edu.neu.madcourse.numad22sp_shoppass.components.Store;
import edu.neu.madcourse.numad22sp_shoppass.database.RealtimeDatabase;
import edu.neu.madcourse.numad22sp_shoppass.utils.BitmapConverter;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private boolean isBuyer;
    private EditText et_username, et_password;
    private Button btn_login, btn_signup;

    public String storename;
    private RealtimeDatabase myDatabase;
    private FirebaseAuth mAuth;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_in);
        isBuyer = getIntent().getBooleanExtra("buyer", true);
        et_username = findViewById(R.id.et_loginName);
        et_password = findViewById(R.id.et_loginPassWord);
        btn_login = findViewById(R.id.btn_login);
        btn_signup = findViewById(R.id.btn_registry);
        myDatabase = new RealtimeDatabase();
        mAuth = FirebaseAuth.getInstance();
        if (isBuyer) {
            et_username.setHint(R.string.login_buyer_name_hint);
            et_password.setHint(R.string.login_buyer_password_hint);
        } else {
            et_username.setHint(R.string.login_seller_name_hint);
            et_password.setHint(R.string.login_seller_password_hint);
        }

        this.sharedPreferences = getApplicationContext().getSharedPreferences("Preference", MODE_PRIVATE);
    }

    /**
     * Converts a username/store-name into a synthetic email for Firebase Auth.
     * Firebase Auth requires an email format, so we append a domain suffix.
     */
    private String toEmail(String name) {
        return name.trim().toLowerCase() + "@shoppass.local";
    }

    public void loginClick(View view) throws InterruptedException, JSONException {
        String username = et_username.getText().toString();
        String password = et_password.getText().toString();

        if (username.trim().length() == 0 || password.trim().length() == 0) {
            Toast.makeText(this, "Please enter your account!", Toast.LENGTH_LONG).show();
            return;
        }

        switch (view.getId()) {
            case R.id.btn_login:
                performSignIn(username, password);
                break;
            case R.id.btn_registry:
                performSignUp(username, password);
                break;
        }
    }

    /**
     * Sign in: authenticates existing credentials via Firebase Auth.
     * Only navigates on successful authentication.
     */
    private void performSignIn(String username, String password) {
        if (isBuyer) {
            String email = toEmail(username);
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                navigateToBuyerHome(username);
                            } else {
                                Log.w(TAG, "signInWithEmail:failure", task.getException());
                                Toast.makeText(LoginActivity.this,
                                        "Authentication failed. Check your username and password.",
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        } else {
            // Seller sign-in: authenticate via Firebase Auth, then verify store ownership
            String email = toEmail(username);
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                String uid = user.getUid();
                                myDatabase.verifyStoreOwner(uid, username, new RealtimeDatabase.AuthResultCallback() {
                                    @Override
                                    public void onSuccess() {
                                        // Fetch store address from DB for display
                                        myDatabase.getStoreFromName(username, new RealtimeDatabase.GetStoreFromNameCallback() {
                                            @Override
                                            public void callback(Store store) {
                                                String address = (store != null) ? store.address : "";
                                                navigateToSellerHome(username, address);
                                            }
                                        });
                                    }

                                    @Override
                                    public void onFailure(String errorMessage) {
                                        mAuth.signOut();
                                        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                                    }
                                });
                            } else {
                                Log.w(TAG, "signInWithEmail:failure", task.getException());
                                Toast.makeText(LoginActivity.this,
                                        "Authentication failed. Check your store name and password.",
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
    }

    /**
     * Sign up: creates a new Firebase Auth account and registers the user/store in RTDB.
     * Only navigates on successful creation and registration.
     */
    private void performSignUp(String username, String password) {
        if (password.trim().length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters.", Toast.LENGTH_LONG).show();
            return;
        }

        if (isBuyer) {
            String email = toEmail(username);
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                String uid = user.getUid();
                                Customer customer = new Customer(username, uid);
                                myDatabase.registerCustomer(uid, customer, new RealtimeDatabase.AuthResultCallback() {
                                    @Override
                                    public void onSuccess() {
                                        navigateToBuyerHome(username);
                                    }

                                    @Override
                                    public void onFailure(String errorMessage) {
                                        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                                    }
                                });
                            } else {
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                Toast.makeText(LoginActivity.this,
                                        "Registration failed. Username may already be taken.",
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        } else {
            // Seller sign-up: password field is the store address for sellers,
            // but we still need a real password for auth. Use a dedicated password
            // field. For sellers, username = store name, password = auth password.
            // The address is entered in the password field per the existing UI.
            // We need a real credential — use the address as the password for Firebase Auth.
            // NOTE: This preserves the existing UX where sellers enter store name + address.
            String address = password;
            String email = toEmail(username);
            mAuth.createUserWithEmailAndPassword(email, address)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                String uid = user.getUid();
                                Store store = new Store(username, address, uid);
                                myDatabase.registerStore(uid, store, new RealtimeDatabase.AuthResultCallback() {
                                    @Override
                                    public void onSuccess() {
                                        try {
                                            store.setLocation();
                                        } catch (InterruptedException | JSONException e) {
                                            Log.e(TAG, "Error setting store location", e);
                                        }
                                        navigateToSellerHome(username, address);
                                    }

                                    @Override
                                    public void onFailure(String errorMessage) {
                                        // Clean up the auth account if store registration fails
                                        FirebaseUser currentUser = mAuth.getCurrentUser();
                                        if (currentUser != null) {
                                            currentUser.delete();
                                        }
                                        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                                    }
                                });
                            } else {
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                Toast.makeText(LoginActivity.this,
                                        "Registration failed. Store name may already be taken.",
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
    }

    private void navigateToBuyerHome(String username) {
        saveSessionToLocalCache(username, isBuyer);
        Intent intent = new Intent(LoginActivity.this, BuyerHomeActivity.class);
        intent.putExtra("username", username);
        startActivity(intent);
    }

    private void navigateToSellerHome(String storename, String address) {
        saveSessionToLocalCache(storename, isBuyer);
        Intent intent = new Intent(LoginActivity.this, SellerHomeActivity.class);
        intent.putExtra("storename", storename);
        intent.putExtra("address", address);
        startActivity(intent);
    }

    /**
     * Saves session metadata to local cache. The actual authentication state
     * is managed by FirebaseAuth — these prefs are only used to restore the
     * UI on app restart, and autoLogin re-verifies the FirebaseAuth session.
     */
    private void saveSessionToLocalCache(String identifier, boolean isBuyer) {
        SharedPreferences.Editor editor = this.sharedPreferences.edit();
        editor.putString("loginUser", identifier);
        editor.putBoolean("isBuyer", isBuyer);
        editor.apply();

        Log.e(TAG, "login local cache: " + this.sharedPreferences.getAll().toString());
    }

    public void test() {
        RealtimeDatabase mydb = new RealtimeDatabase();
        String product_id = "100ff6d5-66a2-46a6-98c6-3aa86d066bf6";
        Uri uri = BitmapConverter.drawableToUri(BitmapConverter.NO_PIC_SHOW, getResources());
        mydb.uploadImage(uri, "xi", new RealtimeDatabase.UploadImageCallback() {
            @Override
            public void callback(Uri downloadUri) {
                System.out.println("uri str " + downloadUri.toString());
                mydb.addImageLinkToProduct("xi", product_id, downloadUri.toString());
            }
        });
        return;
    }


}
