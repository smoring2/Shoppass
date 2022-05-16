package edu.neu.madcourse.numad22sp_shoppass.activities;

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

    private SharedPreferences sharedPreferences;
    private String loginUser;
    private String loginPassword;

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
        if (isBuyer) {
            et_username.setHint(R.string.login_buyer_name_hint);
            et_password.setHint(R.string.login_buyer_password_hint);
        } else {
            et_username.setHint(R.string.login_seller_name_hint);
            et_password.setHint(R.string.login_seller_password_hint);
        }

        this.sharedPreferences = getApplicationContext().getSharedPreferences("Preference", MODE_PRIVATE);
        //test();
    }

    public void loginClick(View view) throws InterruptedException, JSONException {
        String username = et_username.getText().toString();
        String password = et_password.getText().toString();
        this.loginUser = username;
        this.loginPassword = password;
        Customer customer = new Customer(username, password);
        Store store = new Store(username, password);
        myDatabase.changeUser(isBuyer, customer, store);
        storename = username;
        switch (view.getId()) {
            case R.id.btn_login:
            case R.id.btn_registry:
                if (username.trim().length() == 0 || password.trim().length() == 0) {
                    Toast.makeText(this, "Please enter your account!", Toast.LENGTH_LONG).show();
                    return;
                }
                myDatabase.changeUser(isBuyer, customer, store);
                Intent intent;
                if (isBuyer) {
                    intent = new Intent(LoginActivity.this, BuyerHomeActivity.class);
                    intent.putExtra("username", username);
                } else {
                    store.setLocation();
                    intent = new Intent(LoginActivity.this, SellerHomeActivity.class);
                    intent.putExtra("storename", username);
                    intent.putExtra("address", password);
                }
                saveUsernameToLocalCache();
                startActivity(intent);
                break;
        }
    }

    private void saveUsernameToLocalCache() {
        SharedPreferences.Editor editor = this.sharedPreferences.edit();
        editor.putString("loginUser", this.loginUser);
        editor.putString("loginPassword", this.loginPassword);
        editor.putBoolean("isBuyer", this.isBuyer);
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