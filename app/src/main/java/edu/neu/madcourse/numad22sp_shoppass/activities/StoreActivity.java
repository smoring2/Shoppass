package edu.neu.madcourse.numad22sp_shoppass.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
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

import edu.neu.madcourse.numad22sp_shoppass.R;
import edu.neu.madcourse.numad22sp_shoppass.activities.recyclerview.BuyerHomePromotionsAdapter;
import edu.neu.madcourse.numad22sp_shoppass.activities.recyclerview.FavProductsAdapter;
import edu.neu.madcourse.numad22sp_shoppass.activities.recyclerview.MyClickListener;
import edu.neu.madcourse.numad22sp_shoppass.components.Product;
import edu.neu.madcourse.numad22sp_shoppass.components.Promotion;
import edu.neu.madcourse.numad22sp_shoppass.database.RealtimeDatabase;
import edu.neu.madcourse.numad22sp_shoppass.utils.BitmapConverter;
import edu.neu.madcourse.numad22sp_shoppass.fcm.FCMUtil;

public class StoreActivity extends AppCompatActivity {
    private static String TAG = "StoreActivity";
    private RecyclerView rv_promotions, rv_products;
    private BuyerHomePromotionsAdapter promotionsAdapter;
    private FavProductsAdapter productsAdapter;

    private List<Promotion> promotionList = new ArrayList<>();
    private List<Product> productList = new ArrayList<>();
    private List<String> likeProductIdsList = new ArrayList<>();
    private List<String> likeStoreNamesList = new ArrayList<>();

    private RealtimeDatabase myDb;
    private String storename, storeAdd;
    private String username;
    private boolean storeIsLiked;

    private ImageButton ib_likeStore;
    private TextView tv_storeName, tv_StoreAddInstoreActivityStorename;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);
        username = getIntent().getStringExtra("username");
        Log.d(TAG, "onCreate: username " + username);
        storename = getIntent().getStringExtra("storename");
        storeAdd = getIntent().getStringExtra("address");
        Log.d(TAG, "onCreate: storename " + storename);
        ib_likeStore = findViewById(R.id.ib_likeStore);
        tv_storeName = findViewById(R.id.tv_storeName);
        tv_StoreAddInstoreActivityStorename =findViewById(R.id.tv_StoreAddInstoreActivityStorename);
        tv_storeName.setText(storename);
        tv_StoreAddInstoreActivityStorename.setText(storeAdd);
        myDb = new RealtimeDatabase();
        getLikeStores();
        getLikeProducts();
        getStoreAllPromotions();
        getStoreAllProducts();
        createProductRv();
        createPromotionRv();
    }

    public void getLikeStores() {
        likeStoreNamesList.removeAll(likeStoreNamesList);
        myDb.getFavStores(username, new RealtimeDatabase.GetFavStoresCallback() {
            @Override
            public void callback(ArrayList<String> store_names) {
                likeStoreNamesList.addAll(store_names);
                if (likeStoreNamesList.contains(storename)) {
                    ib_likeStore.setImageResource(R.drawable.icons_likeheart_30);
                    storeIsLiked = true;
                }
            }
        });
    }

    public void getLikeProducts() {
        likeProductIdsList.removeAll(likeProductIdsList);
        myDb.getFavProducts(username, new RealtimeDatabase.GetFavProductsCallback() {
            @Override
            public void callback(ArrayList<String> product_ids) {
                likeProductIdsList.addAll(product_ids);
            }
        });
    }

    public void getStoreAllPromotions() {
        promotionList.removeAll(productList);
        myDb.getStorePromotions(storename, new RealtimeDatabase.GetStorePromotionsCallback() {
            @Override
            public void callback(ArrayList<String> promotion_ids) {
                for (String promotion_id : promotion_ids) {
                    myDb.getPromotionFromId(promotion_id, new RealtimeDatabase.GetPromotionFromIdCallback() {
                        @Override
                        public void callback(Promotion promotion) {
                            promotionList.add(promotion);
                            promotionsAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        });
    }

    public void getStoreAllProducts() {
        productList.removeAll(productList);
        myDb.getStoreProducts(storename, new RealtimeDatabase.GetStoreProductsCallback() {
            @Override
            public void callback(ArrayList<Product> products) {
                Log.d(TAG, "callback: products size " + products.size());
                productList.addAll(products);
                getProductsImg(products);
                for (Product product : products) {
                    if (likeProductIdsList.contains(product.id)) {
                        product.isLiked = true;
                    } else {
                        product.isLiked = false;
                    }
                    productsAdapter.notifyDataSetChanged();
                    Log.d(TAG, "callback: productList size " + productList.size());
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

    public void storeActivityClick(View view) {
        switch (view.getId()) {
            case R.id.ib_backarrow:
                onBackPressed();
                break;
            case R.id.ib_likeStore:
                likeOrUnlikeStore();
                break;
        }
    }

    public void likeOrUnlikeStore() {
        if (!storeIsLiked) {
            ib_likeStore.setImageResource(R.drawable.icons_likeheart_30);
            storeIsLiked = true;
            myDb.addFavStore(username, storename);
            FCMUtil.subscribeToStore(getApplicationContext(), storename.replace(" ", ""));
        } else {
            ib_likeStore.setImageResource(R.drawable.icons_unlikeheart_30);
            storeIsLiked = false;
            myDb.removeFavStore(username, storename);
            FCMUtil.unSubscribeToStore(getApplicationContext(), storename.replace(" ", ""));
        }
    }

    public void likeOrUnlikeProduct(Product product, String product_id, boolean isLiked) {
        if (isLiked) {
            myDb.removeFavProduct(username, product_id);
        } else {
            myDb.addFavProduct(username, product_id);
        }
        product.isLiked = !product.isLiked;
        productsAdapter.notifyDataSetChanged();
    }

    public void createProductRv() {
        MyClickListener prodClickListener = new MyClickListener() {
            @Override
            public void clickListener(int position) {
                Intent intent = new Intent(StoreActivity.this, ProductActivity.class);
                intent.putExtra("product_id", productList.get(position).id);
                intent.putExtra("username", username);
                intent.putExtra("storename", storename);
                startActivity(intent);
            }
        };
        MyClickListener likeClickListener = new MyClickListener() {
            @Override
            public void clickListener(int position) {
                Product product = productList.get(position);
                String product_id = product.id;
                boolean isLiked = product.isLiked;
                likeOrUnlikeProduct(product, product_id, isLiked);
            }
        };
        rv_products = findViewById(R.id.rv_storeProducts);
        productsAdapter = new FavProductsAdapter(productList);
        productsAdapter.setClickListeners(likeClickListener, prodClickListener);
        rv_products.setAdapter(productsAdapter);
        rv_products.setLayoutManager(new GridLayoutManager(this, 2, LinearLayoutManager.VERTICAL, false));
    }

    public void createPromotionRv() {
        rv_promotions = findViewById(R.id.rv_storePromotions);
        promotionsAdapter = new BuyerHomePromotionsAdapter(promotionList);
        MyClickListener clickListener = new MyClickListener() {
            @Override
            public void clickListener(int position) {
                Intent intent = new Intent(StoreActivity.this, PromotionActivity.class);
                intent.putExtra("promotion_id", promotionList.get(position).id);
                intent.putExtra("username", username);
                intent.putExtra("storename", storename);
                intent.putExtra("isBuyer", true);
                Log.d(TAG, "clickListener: promotion_id " + promotionList.get(position).id);
                Log.d(TAG, "clickListener: usernmame " + username);
                Log.d(TAG, "clickListener: storename " + storename);
                startActivity(intent);
            }
        };
        promotionsAdapter.setClickListener(clickListener);
        rv_promotions.setAdapter(promotionsAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.buyer_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //menu items
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent intent = null;
        switch (item.getItemId()) {
            case R.id.return_to_home:
                intent = new Intent(StoreActivity.this, BuyerHomeActivity.class);
                intent.putExtra("username", username);
                startActivity(intent);
                return true;

            case R.id.log_out:
                SellerHomeActivity.removeUsernameFromLocalCache();
                intent = new Intent(StoreActivity.this, ChoiceActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected((item));
        }

    }

}