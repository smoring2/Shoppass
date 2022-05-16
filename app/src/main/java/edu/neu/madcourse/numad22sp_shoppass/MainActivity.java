package edu.neu.madcourse.numad22sp_shoppass;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import edu.neu.madcourse.numad22sp_shoppass.components.Customer;
import edu.neu.madcourse.numad22sp_shoppass.components.Product;
import edu.neu.madcourse.numad22sp_shoppass.components.Promotion;
import edu.neu.madcourse.numad22sp_shoppass.components.Store;
import edu.neu.madcourse.numad22sp_shoppass.database.RealtimeDatabase;

public class MainActivity extends AppCompatActivity {
    private RealtimeDatabase myDatabase;
    private boolean requestingStorage;
    public static ActivityResultLauncher<Intent> photoSelectLauncher;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myDatabase = new RealtimeDatabase();
        username = null;

        photoSelectLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();

                            ClipData clipData = data.getClipData();
                            if (clipData != null) {
                                for (int i = 0; i < clipData.getItemCount(); i++) {
                                    Uri imageUri = clipData.getItemAt(i).getUri();
                                    myDatabase.uploadImage(imageUri, username, path -> {
                                        System.out.println("Uploaded image to " + path);
                                    });
                                }
                            }
                        }
                    }
                }
        );

        try {
            databaseTestFunction();
        } catch (InterruptedException | JSONException e) {
            e.printStackTrace();
        }
    }

    private void databaseTestFunction() throws InterruptedException, JSONException {
        String demo_image_link = "/Download/download.png";
        username = "testImageFolder";

        clearDatabase();
        requestingStorage = false;

        Customer customer = new Customer("customer1", "123456");
        Store store = new Store("store1", "Central Park, NYC");
        myDatabase.changeUser(true, customer, null);
        myDatabase.changeUser(false, null, store);

        HashMap<String, String> image_urls = new HashMap<String, String>();
        image_urls.put("image_url1", "image_url1");
        image_urls.put("image_url2", "image_url2");
        Product product = new Product("basket", String.valueOf(UUID.randomUUID()), 10.00f, "This is a basket", image_urls);
        myDatabase.addProduct(product, store.name);

        Promotion promotion = new Promotion("store1", String.valueOf(UUID.randomUUID()), "Spring Sale", "This is a spring sale");
        promotion.addEligibleProduct(product.id);
        myDatabase.addPromotion(promotion);

        myDatabase.addFavProduct(customer.username, product.id);
        myDatabase.addFavStore(customer.username, store.name);

        String path = Environment.getExternalStorageDirectory().getPath();
        if (!requestingStorage) {
            ActivityResultLauncher<String[]> storagePermissionRequest =
                    registerForActivityResult(new ActivityResultContracts
                                    .RequestMultiplePermissions(), result -> {
                                Boolean storageReadGranted = result.getOrDefault(
                                        Manifest.permission.READ_EXTERNAL_STORAGE, false);
                                if (storageReadGranted != null && storageReadGranted) {
                                    requestingStorage = true;
                                } else {
                                    Toast.makeText(this, "Storage permission not granted", Toast.LENGTH_LONG).show();
                                    finish();
                                }
                            }
                    );
            storagePermissionRequest.launch(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE
            });
        }

        // Start an activity that lets the user select a photo to upload
        promptImageSelect();
    }

    // Prompt the user to select multiple images from local gallery and upload to firebase storage in his own directory
    // images/<username>/filename
    public void promptImageSelect() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        photoSelectLauncher.launch(intent);
    }

    private void clearDatabase() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.removeValue();
    }
}