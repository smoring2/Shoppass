package edu.neu.madcourse.numad22sp_shoppass.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import edu.neu.madcourse.numad22sp_shoppass.BuildConfig;
import edu.neu.madcourse.numad22sp_shoppass.MainActivity;
import edu.neu.madcourse.numad22sp_shoppass.R;
import edu.neu.madcourse.numad22sp_shoppass.activities.recyclerview.MyClickListener;
import edu.neu.madcourse.numad22sp_shoppass.activities.recyclerview.NewProductsImgsAdapter;
import edu.neu.madcourse.numad22sp_shoppass.components.Product;
import edu.neu.madcourse.numad22sp_shoppass.database.RealtimeDatabase;
import edu.neu.madcourse.numad22sp_shoppass.utils.BitmapConverter;

public class SellerAddNewProductActivity extends AppCompatActivity {
    private EditText et_newProductName, et_newProductPrice, et_newProductDesc;
    private List<String> imgUriList = new ArrayList<>();
    private List<String> imgDbPathList = new ArrayList<>();
    private RecyclerView rv_imgs;
    private NewProductsImgsAdapter imgsAdapter;

    private RealtimeDatabase myDb;
    private ActivityResultLauncher<Intent> photoSelectLauncher = MainActivity.photoSelectLauncher;

    private static int TAKE_PHOTO_REQUEST = 1212, CUT_PICTURE = 1313;
    private String capturePath = "";

    private String[] permissions = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
    };
    private int PERMISSION_CODE = 100;

    private static String TAG = "SellerAddNewProductActivity";
    private String storename;
    private String product_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_add_new_product);
        storename = getIntent().getStringExtra("storename");
        // getAllPermissions();
        myDb = new RealtimeDatabase();
        et_newProductName = findViewById(R.id.et_newProductTitle);
        et_newProductPrice = findViewById(R.id.et_newProductId);
        et_newProductDesc = findViewById(R.id.et_newPromotionDesc);
        createRvImgs();
    }

    public void newProductClick(View view) throws IOException {
        switch (view.getId()) {
            case R.id.btn_uploadANewPromotion:
                //check if can upload the product
                String name = et_newProductName.getText().toString();
                String price = et_newProductPrice.getText().toString();
                String desc = et_newProductDesc.getText().toString();
                if (name.length() == 0 || price.length() == 0) {
                    Toast.makeText(SellerAddNewProductActivity.this, "Please input the name and price", Toast.LENGTH_LONG).show();
                } else {
                    uploadNewProduct(name, price, desc, new HashMap<>());
                }
                break;
            case R.id.btn_cancelANewPromotion:
                cleanPage();
                break;
            case R.id.btn_newProductImgCamera:
                //open camera
                takeAPhoto();
                break;
            case R.id.btn_newProductImgPhoto:
                //get img from photo
                openPhotos();
                break;
        }
    }

    public void cleanPage() {
        et_newProductName.setText("");
        et_newProductPrice.setText("");
        et_newProductDesc.setText("");
        imgUriList.removeAll(imgUriList);
        imgsAdapter.notifyDataSetChanged();
    }

    public void createRvImgs() {
        rv_imgs = findViewById(R.id.rv_newProductImgs);
        imgsAdapter = new NewProductsImgsAdapter(imgUriList);
        MyClickListener deleteClickListener = new MyClickListener() {
            @Override
            public void clickListener(int position) {
                imgUriList.remove(position);
                imgsAdapter.notifyDataSetChanged();
            }
        };
        imgsAdapter.setDeleteClickListener(deleteClickListener);
        rv_imgs.setAdapter(imgsAdapter);
        rv_imgs.setLayoutManager(new GridLayoutManager(this, 2, LinearLayoutManager.VERTICAL, false));
    }

    public void uploadNewProduct(String name, String price, String desc, HashMap<String, String> map) {
        product_id = String.valueOf(UUID.randomUUID());
        Log.d(TAG, "uploadNewProduct: product_id " + product_id);
        Float priceFloat = null;
        try {
            priceFloat = Float.parseFloat(price);
            Product product = new Product(name, product_id, priceFloat, desc, map);
            myDb.addProduct(product, storename);
            uploadImages(imgUriList);
        } catch (Exception e) {
            Toast.makeText(SellerAddNewProductActivity.this,
                    "Please input the valid price",
                    Toast.LENGTH_LONG)
                    .show();
            Log.d(TAG, "uploadNewProduct: exception " + e);
        }
        if (priceFloat != null) {

        }


    }

    public void uploadImages(List<String> imgUriList) {
        if (imgUriList.size() == 0) {
            Uri uri = BitmapConverter.drawableToUri(BitmapConverter.NO_PIC_SHOW, getResources());
            uploadAnImg(uri);
            cleanPage();
            return;
        }
        for (int i = 0; i < imgUriList.size(); i++) {
            String uriStr = imgUriList.get(i);
            Uri uri = Uri.parse(uriStr);
            uploadAnImg(uri);
            if (i == imgUriList.size() - 1) {
                cleanPage();
                Toast.makeText(SellerAddNewProductActivity.this,
                        "A new product is uploaded successfully!",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    public void uploadAnImg(Uri uri) {
        myDb.uploadImage(uri, storename, new RealtimeDatabase.UploadImageCallback() {
            @Override
            public void callback(Uri downloadUri) {
                String imgUri = downloadUri.toString();
                Log.d(TAG, "callback: storename " + storename);
                Log.d(TAG, "callback: product_id " + product_id);
                myDb.addImageLinkToProduct(storename, product_id, imgUri);
            }
        });
    }


    public void takeAPhoto() throws IOException {
        String state = Environment.getExternalStorageState();
        Log.d(TAG, "takeAPhoto: BuildConfig.APPLICATION_ID " + BuildConfig.APPLICATION_ID);
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            File filesDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            String imgName = String.valueOf(Calendar.getInstance().getTimeInMillis());
            File imgFile = File.createTempFile(
                    imgName,
                    "jpg",
                    filesDir
            );
            capturePath = imgFile.getAbsolutePath();
            Intent photoIn = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Uri fileUri;
            if (imgFile != null) {
                if (Build.VERSION.SDK_INT >= 24) {
                    fileUri = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider", imgFile);
                } else {
                    fileUri = Uri.fromFile(imgFile);
                }
                photoIn.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                startActivityForResult(photoIn, TAKE_PHOTO_REQUEST);
            }
        }
//        } else {
//            Toast.makeText(SellerAddNewProductActivity.this, "Please insert the SD storage", Toast.LENGTH_LONG).show();
//        }

    }

    public void openPhotos() {
        Intent localIntent = new Intent();
        localIntent.setType("image/*");
        localIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(localIntent, CUT_PICTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode " + requestCode);
        Log.d(TAG, "onActivityResult: resultCode " + resultCode);
        if (requestCode == TAKE_PHOTO_REQUEST && resultCode == RESULT_OK) {
            File file = new File(capturePath);
            Uri fileUri = Uri.fromFile(file);
            imgUriList.add(fileUri.toString());
            imgsAdapter.notifyDataSetChanged();
        } else if (requestCode == TAKE_PHOTO_REQUEST && resultCode != RESULT_OK) {
            Toast.makeText(
                    SellerAddNewProductActivity.this,
                    "Taking A Photo Failed",
                    Toast.LENGTH_LONG
            ).show();
        } else if (requestCode == CUT_PICTURE && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            imgUriList.add(uri.toString());
            imgsAdapter.notifyDataSetChanged();
        } else if (requestCode == CUT_PICTURE && resultCode != RESULT_OK) {
            Toast.makeText(
                    SellerAddNewProductActivity.this,
                    "Uploading A Photo Failed",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    public void getAllPermissions() {
        List<String> permissionList = new ArrayList<>();
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(permissions[i]);
            }
        }
        if (permissionList.size() > 0) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean hasperPermission = false;
        if (requestCode == PERMISSION_CODE) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == -1) {
                    hasperPermission = true;
                }
            }
            if (hasperPermission) {
                AlertDialog alertDialog = new AlertDialog.Builder(this)
                        .setTitle("Hint")
                        .setMessage("Please allow all permissions")
                        .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                                Uri packageUri = Uri.parse("package:" + getPackageName());
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageUri);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        })
                        .create();
                alertDialog.show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.seller_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //menu items
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent intent = null;
        switch (item.getItemId()) {
            case R.id.return_to_home:
                intent = new Intent(SellerAddNewProductActivity.this, SellerHomeActivity.class);
                intent.putExtra("storename", storename);
                startActivity(intent);
                return true;

            case R.id.log_out:
                SellerHomeActivity.removeUsernameFromLocalCache();
                intent = new Intent(SellerAddNewProductActivity.this, ChoiceActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected((item));
        }

    }
}