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
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import edu.neu.madcourse.numad22sp_shoppass.R;
import edu.neu.madcourse.numad22sp_shoppass.activities.recyclerview.AddProductsInPromotionAdapter;
import edu.neu.madcourse.numad22sp_shoppass.activities.recyclerview.MyClickListener;
import edu.neu.madcourse.numad22sp_shoppass.components.Product;
import edu.neu.madcourse.numad22sp_shoppass.components.Promotion;
import edu.neu.madcourse.numad22sp_shoppass.database.RealtimeDatabase;
import edu.neu.madcourse.numad22sp_shoppass.utils.BitmapConverter;

public class AddProductInPromotionActivity extends AppCompatActivity {
    private static String TAG = "AddProductInPromotionActivity";

    private List<Product> allProductsStoreList = new ArrayList<>();
    private List<Product> allPromotionProductsList = new ArrayList<>();
    private List<Product> allProductsNoPromotionList = new ArrayList<>();
    private List<String> newProductsChosenProm = new ArrayList<>();

    private RealtimeDatabase myDb;
    private String storename;
    private String promotion_id;

    private RecyclerView rv_productsNoPromotion;
    private AddProductsInPromotionAdapter adapter;

    private TextView promotionStore, promotionTitle, promotionDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_product_in_promotion);
        myDb = new RealtimeDatabase();
        storename = getIntent().getStringExtra("storename");
        promotion_id = getIntent().getStringExtra("promotion_id");
        createRvNoPromotionProducts();
        //getProductsAndImgs();

        promotionStore = findViewById(R.id.tv_promotionStore);
        promotionStore.setText(storename);
        promotionTitle = findViewById(R.id.tv_promotionTitle);
        promotionTitle.setText(getIntent().getStringExtra("promotion_title"));
        promotionDescription = findViewById(R.id.tv_promotionDesc);
        promotionDescription.setText(getIntent().getStringExtra("promotion_desc"));
        getProductsStoreList();

    }

    public void createRvNoPromotionProducts() {
        rv_productsNoPromotion = findViewById(R.id.rv_productsNoProm);
        adapter = new AddProductsInPromotionAdapter(allProductsNoPromotionList);
        MyClickListener prodClickListener = new MyClickListener() {
            @Override
            public void clickListener(int position) {
                Intent intent = new Intent(AddProductInPromotionActivity.this, ProductActivity.class);
                intent.putExtra("product_id", allProductsNoPromotionList.get(position).id);
                intent.putExtra("storename", storename);
                startActivity(intent);
            }
        };
        MyClickListener addClickListener = new MyClickListener() {
            @Override
            public void clickListener(int position) {
                Product product = allProductsNoPromotionList.get(position);
                String product_id = product.id;
                if (newProductsChosenProm.contains(product_id)) {
                    product.isChosenForPromotion = false;
                    newProductsChosenProm.remove(product_id);
                } else {
                    product.isChosenForPromotion = true;
                    newProductsChosenProm.add(product_id);
                }
                Log.d(TAG, "clickListener: newProductsChosenProm size " + newProductsChosenProm.size());
                adapter.notifyDataSetChanged();
            }

        };
        adapter.setClickListeners(addClickListener, prodClickListener);
        rv_productsNoPromotion.setAdapter(adapter);
        rv_productsNoPromotion.setLayoutManager(new GridLayoutManager(this, 2, LinearLayoutManager.VERTICAL, false));
    }

    public void newProductsPromotionClick(View view) {
        switch (view.getId()) {
            case R.id.btn_uploadANewProdsPromotion:
                addProductsInPromotion();
                break;
            case R.id.btn_cancelANewProdsPromotion:
                cleanPage();
                break;
        }
    }
    public void addProductsInPromotion() {
        for (int i = 0; i < newProductsChosenProm.size(); i++) {
            String product_id = newProductsChosenProm.get(i);
            myDb.addProductToPromotion(promotion_id, product_id);
            if (i == newProductsChosenProm.size() - 1) {
                cleanPage();
            }
        }
    }
    public void cleanPage() {
        onBackPressed();
    }

    public void getProductsNoPromotionList(List<String> productIds) {
        allProductsNoPromotionList.removeAll(allProductsNoPromotionList);
        for (Product product : allProductsStoreList) {
            if (!productIds.contains(product.id)) {
                product.isChosenForPromotion = false;
                allProductsNoPromotionList.add(product);
                getProductsImgs(product);
            }
        }
        adapter.notifyDataSetChanged();
        Log.d(TAG, "getProductsNoPromotionList: size " + allProductsNoPromotionList.size());
    }

    public void getProductsPromotionList() {
        allPromotionProductsList.removeAll(allPromotionProductsList);
        myDb.getPromotionFromId(promotion_id, new RealtimeDatabase.GetPromotionFromIdCallback() {
            @Override
            public void callback(Promotion promotion) {
                promotionTitle.setText(promotion.title);
                promotionDescription.setText(promotion.description);
                List<String> productIds = new ArrayList<>(promotion.eligibleProducts.values());
                getProductsNoPromotionList(productIds);
                Log.d(TAG, "callback: productIds " + productIds.size());
                for (String productId : productIds) {
                    Log.d(TAG, "callback: productid " + productId);
                    myDb.getProductFromId(productId, new RealtimeDatabase.GetProductFromIdAndStoreCallback() {
                        @Override
                        public void callback(Product Product) {
                            allPromotionProductsList.add(Product);
                            Log.d(TAG, "callback: allPromotionProductsList size " + allPromotionProductsList.size());
                        }
                    });
                }
            }
        });

    }

    public void getProductsImgs(Product product) {
        List<String> uriLinks = new ArrayList<>();

        if (product.image_urls != null && product.image_urls.size() > 0) {
            uriLinks = new ArrayList<>(product.image_urls.values());

        }
        for (String uriLink : uriLinks) {
            myDb.downloadImage(Uri.parse(uriLink), new RealtimeDatabase.DownloadImageCallback() {
                @Override
                public void callback(byte[] image_bytes) {
                    if (image_bytes != null) {
                        product.bitmapList.add(BitmapConverter.getImgBitmap(image_bytes));
                        adapter.notifyDataSetChanged();
                    }
                }
            });
        }
    }


    public void getProductsStoreList() {
        allProductsStoreList.removeAll(allProductsStoreList);
        myDb.getStoreProducts(storename, new RealtimeDatabase.GetStoreProductsCallback() {
            @Override
            public void callback(ArrayList<Product> products) {
                allProductsStoreList.addAll(products);
                getProductsPromotionList();
            }
        });
        Log.d(TAG, "getProductsStoreList: size " + allProductsStoreList.size());
    }

    @Override
    protected void onResume() {
        super.onResume();
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
                intent = new Intent(AddProductInPromotionActivity.this, SellerHomeActivity.class);
                intent.putExtra("storename", storename);
                startActivity(intent);
                return true;

            case R.id.log_out:
                SellerHomeActivity.removeUsernameFromLocalCache();
                intent = new Intent(AddProductInPromotionActivity.this, ChoiceActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected((item));
        }

    }
}