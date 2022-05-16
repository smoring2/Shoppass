package edu.neu.madcourse.numad22sp_shoppass.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import edu.neu.madcourse.numad22sp_shoppass.R;
import edu.neu.madcourse.numad22sp_shoppass.activities.recyclerview.BuyerHomePromotionsAdapter;
import edu.neu.madcourse.numad22sp_shoppass.activities.recyclerview.MyClickListener;
import edu.neu.madcourse.numad22sp_shoppass.activities.recyclerview.SellerHomeProductsAdapter;
import edu.neu.madcourse.numad22sp_shoppass.activities.recyclerview.SellerHomePromotionsAdapter;
import edu.neu.madcourse.numad22sp_shoppass.components.Product;
import edu.neu.madcourse.numad22sp_shoppass.components.Promotion;
import edu.neu.madcourse.numad22sp_shoppass.database.RealtimeDatabase;
import edu.neu.madcourse.numad22sp_shoppass.utils.BitmapConverter;

public class SellerHomeActivity extends AppCompatActivity {
    private static String TAG = "SellerHomeActivity";
    private RecyclerView rv_promotions, rv_allProducts;
    private List<Promotion> promotionList = new ArrayList<>();
    private List<Product> productList = new ArrayList<>();
    private SellerHomePromotionsAdapter promotionsAdapter;
    private SellerHomeProductsAdapter productsAdapter;
    private RealtimeDatabase myDb;
    private String storename, storeAdd;
    private TextView tv_name, tv_add;
    private static SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_home);
        tv_name = findViewById(R.id.tv_sellerStoreName);
        tv_add = findViewById(R.id.tv_sellerStoreAdd);
        storename = getIntent().getStringExtra("storename");
        Log.d(TAG, "onCreate: storename " + storename);
        storeAdd = getIntent().getStringExtra("address");
        tv_name.setText(storename);
        tv_add.setText(storeAdd);
        myDb = new RealtimeDatabase();
        createRvPromotions();
        createRvAllProducts();
        this.sharedPreferences = getApplicationContext().getSharedPreferences("Preference", MODE_PRIVATE);
    }

    public void sellerHomeClick(View view) {
        Intent intent = null;
        switch (view.getId()) {
            case R.id.ib_addNewPromotion:
                intent = new Intent(SellerHomeActivity.this, SellerAddNewPromotionActivity.class);
                intent.putExtra("storename", storename);
                break;
        }
        startActivity(intent);
    }

    public void createRvPromotions() {
        rv_promotions = findViewById(R.id.rv_storePromotions);
        MyClickListener promotionClick = new MyClickListener() {
            @Override
            public void clickListener(int position) {
                Intent intent = new Intent(SellerHomeActivity.this, PromotionActivity.class);
                intent.putExtra("promotion_id", promotionList.get(position).id);
                intent.putExtra("storename", storename);
                intent.putExtra("isBuyer", false);
                startActivity(intent);
            }
        };
        MyClickListener promotionLongClick = new MyClickListener() {
            @Override
            public void clickListener(int position) {
                if (position != 0) {
                    // TODO: do we delete or edit the promotion?
                    // ans: not required
                }
            }
        };
        promotionsAdapter = new SellerHomePromotionsAdapter(promotionList);
        promotionsAdapter.setClickListener(promotionClick, promotionLongClick);
        rv_promotions.setAdapter(promotionsAdapter);
    }

    public void createRvAllProducts() {
        rv_allProducts = findViewById(R.id.rv_storeProducts);
        MyClickListener productClick = new MyClickListener() {
            @Override
            public void clickListener(int position) {
                if (position == 0) {
                    Intent intent = new Intent(SellerHomeActivity.this, SellerAddNewProductActivity.class);
                    Log.d(TAG, "clickListener: storename " + storename);
                    intent.putExtra("storename", storename);
                    intent.putExtra("isBuyer", false);
                    startActivity(intent);
                    return;
                }
                Intent intent = new Intent(SellerHomeActivity.this, ProductActivity.class);
                intent.putExtra("product_id", productList.get(position).id);
                intent.putExtra("storename", storename);
                Log.d(TAG, "clickListener: storename " + storename);
                intent.putExtra("storename", storename);
                intent.putExtra("isBuyer", false);
                startActivity(intent);
            }
        };
        MyClickListener productLongClick = new MyClickListener() {
            @Override
            public void clickListener(int position) {
                if (position != 0) {
                    // TODO: do we delete or edit the product?
                    // ans: not required
                }
            }
        };
        productsAdapter = new SellerHomeProductsAdapter(productList);
        productsAdapter.setClickListeners(productClick, productLongClick);
        rv_allProducts.setAdapter(productsAdapter);
        rv_allProducts.setLayoutManager(new GridLayoutManager(this, 2, LinearLayoutManager.VERTICAL, false));
    }

    public void getStoreAllProducts() {
        productList.removeAll(productList);
        productList.add(new Product());
        myDb.getStoreProducts(storename, new RealtimeDatabase.GetStoreProductsCallback() {
            @Override
            public void callback(ArrayList<Product> products) {
                if (products != null) {
                    productList.addAll(products);
                    getProductsImg(products);
                    productsAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    public void getProductsImg(ArrayList<Product> products) {
        if (products == null) {
            return;
        }
        for (Product p : products) {
            if (p.image_urls == null || p.image_urls.size() == 0) {
                continue;
            }
            List<String> uriLinks = new ArrayList<>(p.image_urls.values());
            for (String uriLink : uriLinks) {
                myDb.downloadImage(Uri.parse(uriLink), new RealtimeDatabase.DownloadImageCallback() {
                    @Override
                    public void callback(byte[] image_bytes) {
                        if (image_bytes != null) {
                            p.bitmapList.add(BitmapConverter.getImgBitmap(image_bytes));
                            productsAdapter.notifyDataSetChanged();
                        }
                    }
                });
            }
        }
    }

    public void getStoreAllPromotions() {
        promotionList.removeAll(promotionList);
        myDb.getStorePromotions(storename, new RealtimeDatabase.GetStorePromotionsCallback() {
            @Override
            public void callback(ArrayList<String> promotion_ids) {
                if (promotion_ids != null) {
                    for (String promotion_id : promotion_ids) {
                        myDb.getPromotionFromId(promotion_id, new RealtimeDatabase.GetPromotionFromIdCallback() {
                            @Override
                            public void callback(Promotion promotion) {
                                if (promotion == null) {
                                    return;
                                }
                                promotionList.add(promotion);
                                promotionsAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                }
            }
        });
    }

    public static void removeUsernameFromLocalCache() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getStoreAllProducts();
        getStoreAllPromotions();
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
//                intent = new Intent(SellerHomeActivity.this, SellerHomeActivity.class);
//                startActivity(intent);
                return true;

            case R.id.log_out:
                removeUsernameFromLocalCache();
                intent = new Intent(SellerHomeActivity.this, ChoiceActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected((item));
        }

    }
}


