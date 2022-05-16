package edu.neu.madcourse.numad22sp_shoppass.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.util.UUID;
import edu.neu.madcourse.numad22sp_shoppass.R;
import edu.neu.madcourse.numad22sp_shoppass.components.Promotion;
import edu.neu.madcourse.numad22sp_shoppass.database.RealtimeDatabase;
import edu.neu.madcourse.numad22sp_shoppass.fcm.FCMUtil;

public class SellerAddNewPromotionActivity extends AppCompatActivity {
    private static String TAG = "SellerAddNewPromotionActivity";
    private EditText et_newPromotionTitle, et_newPromotionDesc;
    private TextView tv_newPromotionId;
    private RealtimeDatabase myDb;
    private String storename;
    private String promotionId;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_add_new_promotion);
        storename = getIntent().getStringExtra("storename");
        et_newPromotionTitle = findViewById(R.id.et_newProductTitle);
        et_newPromotionDesc = findViewById(R.id.et_newPromotionDesc);
        promotionId = String.valueOf(UUID.randomUUID());
        tv_newPromotionId = findViewById(R.id.tv_newProductId);
        tv_newPromotionId.setText(promotionId);
        myDb = new RealtimeDatabase();
        this.sharedPreferences = getApplicationContext().getSharedPreferences("Preference", MODE_PRIVATE);
    }

    public void newPromotionClick(View view) {
        switch (view.getId()) {
            case R.id.btn_cancelANewPromotion:
                et_newPromotionTitle.setText("");
                et_newPromotionDesc.setText("");
                break;
            case R.id.btn_uploadANewPromotion:
                String promotionTitle = et_newPromotionTitle.getText().toString();
                String promotionDesc = et_newPromotionDesc.getText().toString();
                if (promotionTitle.length() == 0) {
                    Toast.makeText(this, "Promotion Title can not be empty", Toast.LENGTH_LONG).show();
                } else {
                    Promotion promotion = new Promotion(storename, promotionId, promotionTitle, promotionDesc);
                    myDb.addPromotion(promotion);
                    Log.d(TAG, "newPromotionClick: new Promotion added!");
                    String serverKey = this.sharedPreferences.getString("serverKey", null);
                    Log.e(TAG, "SERVER KEY IS " + serverKey);
                    FCMUtil.sendMessageToSubscribers(getApplicationContext(), serverKey, promotion);
                    Toast.makeText(SellerAddNewPromotionActivity.this,
                            "A new promotion is created successfully!",
                            Toast.LENGTH_LONG).show();
                    onBackPressed();
                }
                break;
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.seller_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent intent = null;
        switch (item.getItemId()) {
            case R.id.return_to_home:
                intent = new Intent(SellerAddNewPromotionActivity.this, SellerHomeActivity.class);
                intent.putExtra("storename", storename);
                startActivity(intent);
                return true;

            case R.id.log_out:
                SellerHomeActivity.removeUsernameFromLocalCache();
                intent = new Intent(SellerAddNewPromotionActivity.this, ChoiceActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected((item));
        }

    }
}