
package edu.neu.madcourse.numad22sp_shoppass.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import edu.neu.madcourse.numad22sp_shoppass.Examples;
import edu.neu.madcourse.numad22sp_shoppass.R;
import edu.neu.madcourse.numad22sp_shoppass.activities.recyclerview.ProductImagesAdapter;
import edu.neu.madcourse.numad22sp_shoppass.components.Product;
import edu.neu.madcourse.numad22sp_shoppass.database.RealtimeDatabase;
import edu.neu.madcourse.numad22sp_shoppass.utils.BitmapConverter;

public class ProductActivity extends AppCompatActivity {

    private RecyclerView rv_prodImgs;
    private ProductImagesAdapter productImagesAdapter;
    private List<Bitmap> imgBitmapList = new ArrayList<>();

    private ImageButton ib_descArrow, ib_likeProd;
    private TextView tv_allDesc;
    private TextView tv_prodName, tv_prod_productName, tv_prod_productPrice;
    private boolean allDescShown;

    private Product currentProduct;

    private String username, product_id, storename;
    private String productTitle, productDesc, productPrice;
    private RealtimeDatabase myDb;
    private boolean isBuyer;

    private static String TAG = "ProductActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);
        myDb = new RealtimeDatabase();
        username = getIntent().getStringExtra("username");
        product_id = getIntent().getStringExtra("product_id");
        storename = getIntent().getStringExtra("storename");
        isBuyer = getIntent().getBooleanExtra("isBuyer", true);
        Log.d(TAG, "onCreate: username " + username);
        Log.d(TAG, "onCreate: product_id " + product_id);
        Log.d(TAG, "onCreate: storename " + storename);
        Log.d(TAG, "onCreate: isBuyer " + isBuyer);

        getCurrentProduct();
        createRvProductImgs();

        ib_descArrow = findViewById(R.id.ib_triright);
        ib_likeProd = findViewById(R.id.ib_prod_likeProduct);
        tv_allDesc = findViewById(R.id.tv_prod_alldesc);
        tv_prod_productName = findViewById(R.id.tv_prod_productName);
        tv_prodName = findViewById(R.id.tv_prodName);
        tv_prodName.setText(storename);
        tv_prod_productPrice = findViewById(R.id.tv_prod_productPrice);

        allDescShown = false;
        //if (!isBuyer) {
        ib_likeProd.setVisibility(View.GONE);
        ib_likeProd.setEnabled(false);
    }


    public void getCurrentProduct() {
        myDb.getProductFromId(product_id, new RealtimeDatabase.GetProductFromIdAndStoreCallback() {
            @Override
            public void callback(Product product) {
                currentProduct = product;
                tv_prod_productName.setText(product.name);
                tv_prod_productPrice.setText("$ " + currentProduct.price);
                Log.d(TAG, "callback: currentProduct " + currentProduct.toString());
                if (isBuyer) {
                    getIfLikedProduct();
                }
                getBitMapList();
            }
        });
    }

    public void getIfLikedProduct() {
        myDb.getFavProducts(username, new RealtimeDatabase.GetFavProductsCallback() {
            @Override
            public void callback(ArrayList<String> product_ids) {
                if (product_ids.contains(currentProduct.id)) {
                    ib_likeProd.setImageResource(R.drawable.icons_likeheart_30);
                    currentProduct.isLiked = true;
                }
            }
        });

    }

    public void getBitMapList() {
        imgBitmapList.removeAll(imgBitmapList);
        List<String> imgs = new ArrayList<>();
        if (currentProduct.image_urls != null && currentProduct.image_urls.size() > 0) {
            imgs.addAll(new ArrayList<>(currentProduct.image_urls.values()));
        }
        downloadImgs(imgs);
    }

    public void downloadImgs(List<String> imgUriList) {
        if (imgUriList.size() == 0) {
            Uri uri = BitmapConverter.drawableToUri(BitmapConverter.NO_PIC_SHOW, getResources());
            downloadAnImg(uri);
            return;
        }
        for (String uriStr : imgUriList) {
            Uri uri = Uri.parse(uriStr);
            downloadAnImg(uri);
        }
    }

    public void downloadAnImg(Uri uri) {
        myDb.downloadImage(uri, new RealtimeDatabase.DownloadImageCallback() {
            @Override
            public void callback(byte[] image_bytes) {
                if (image_bytes != null) {
                    imgBitmapList.add(BitmapConverter.getImgBitmap(image_bytes));
                    productImagesAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    public void createRvProductImgs() {
        rv_prodImgs = findViewById(R.id.rv_prod_images);
        productImagesAdapter = new ProductImagesAdapter(imgBitmapList);
        rv_prodImgs.setAdapter(productImagesAdapter);
    }

    public void prodActivityClick(View view) {
        switch (view.getId()) {
            case R.id.ib_triright:
                if (allDescShown) {
                    tv_allDesc.setVisibility(View.GONE);
                    ib_descArrow.setImageResource(Examples.ARROW_RIGHT);
                } else {
                    tv_allDesc.setVisibility(View.VISIBLE);
                    if (currentProduct != null && currentProduct.description != null) {
                        tv_allDesc.setText(currentProduct.description);
                    }
                    ib_descArrow.setImageResource(Examples.ARROW_DOWN);
                }
                allDescShown = !allDescShown;
                break;
//            case R.id.ib_prod_likeProduct:
//                addLikedProduct();
//                break;
            case R.id.ib_backarrow:
                onBackPressed();
                break;
        }
    }

//    public void addLikedProduct() {
//        if (!currentProduct.isLiked) {
//            myDb.addFavProduct(username, product_id);
//            ib_likeProd.setImageResource(R.drawable.icons_likeheart_30);
//        }
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        if (isBuyer) {
            inflater.inflate(R.menu.buyer_menu, menu);
        } else {
            inflater.inflate(R.menu.seller_menu, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    //menu items
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent intent = null;
        switch (item.getItemId()) {
            case R.id.return_to_home:
                if (isBuyer) {
                    intent = new Intent(ProductActivity.this, BuyerHomeActivity.class);
                    intent.putExtra("username", username);
                } else {
                    intent = new Intent(ProductActivity.this, SellerHomeActivity.class);
                    intent.putExtra("storename", storename);
                }
                startActivity(intent);
                return true;

            case R.id.log_out:
                if (isBuyer) {
                    BuyerHomeActivity.removeUsernameFromLocalCache();
                    intent = new Intent(ProductActivity.this, ChoiceActivity.class);
                } else {
                    SellerHomeActivity.removeUsernameFromLocalCache();
                    intent = new Intent(ProductActivity.this, ChoiceActivity.class);
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected((item));
        }

    }

}