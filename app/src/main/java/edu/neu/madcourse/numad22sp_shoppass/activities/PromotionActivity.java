package edu.neu.madcourse.numad22sp_shoppass.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import edu.neu.madcourse.numad22sp_shoppass.R;
import edu.neu.madcourse.numad22sp_shoppass.activities.recyclerview.FavProductsAdapter;
import edu.neu.madcourse.numad22sp_shoppass.activities.recyclerview.MyClickListener;
import edu.neu.madcourse.numad22sp_shoppass.activities.recyclerview.SellerHomeProductsAdapter;
import edu.neu.madcourse.numad22sp_shoppass.components.Product;
import edu.neu.madcourse.numad22sp_shoppass.components.Promotion;
import edu.neu.madcourse.numad22sp_shoppass.database.RealtimeDatabase;
import edu.neu.madcourse.numad22sp_shoppass.utils.BitmapConverter;

public class PromotionActivity extends AppCompatActivity {
    private static String TAG = "PromotionActivity";
    private TextView promotionStore, promotionTitle, promotionDescription;
    private RecyclerView rv_products;
    private List<Product> allProdutsStoreList = new ArrayList<>();
    private List<Product> allPromotionProductsList = new ArrayList<>();
    private List<Product> allProductsNoPromotionList = new ArrayList<>();
    private List<String> likedProductList = new ArrayList<>();
    private SellerHomeProductsAdapter sellerAdapter;
    private FavProductsAdapter buyerAdapter;

    private RealtimeDatabase myDb;
    private String storename;
    private String username;
    private String promotion_id;
    private boolean isBuyer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promotion);
        storename = getIntent().getStringExtra("storename");
        username = getIntent().getStringExtra("username");
        promotion_id = getIntent().getStringExtra("promotion_id");
        isBuyer = getIntent().getBooleanExtra("isBuyer", false);
        Log.d(TAG, "onCreate: promotion_id " + promotion_id);
        Log.d(TAG, "onCreate: isBuyer " + isBuyer);
        Log.d(TAG, "onCreate: storename " + storename);
        promotionStore = findViewById(R.id.tv_promotionStore);
        promotionStore.setText(storename);
        promotionTitle = findViewById(R.id.tv_promotionTitle);
        promotionDescription = findViewById(R.id.tv_promotionDesc);
        myDb = new RealtimeDatabase();
        createRvProducts();
    }

    public void getProductsPromotionList() {
        allPromotionProductsList.removeAll(allPromotionProductsList);
        if (!isBuyer) {
            allPromotionProductsList.add(new Product());
        }
        myDb.getPromotionFromId(promotion_id, new RealtimeDatabase.GetPromotionFromIdCallback() {
            @Override
            public void callback(Promotion promotion) {
                promotionStore.setText(promotion.store);
                promotionTitle.setText(promotion.title);
                promotionDescription.setText(promotion.description);
                List<String> productIds = new ArrayList<>();
                if (promotion.eligibleProducts.size() != 0) {
                    productIds.addAll(promotion.eligibleProducts.values());
                }
                Log.d(TAG, "callback: productIds " + productIds.size());
                for (String productId : productIds) {
                    Log.d(TAG, "callback: productid " + productId);
                    myDb.getProductFromId(productId, new RealtimeDatabase.GetProductFromIdAndStoreCallback() {
                        @Override
                        public void callback(Product product) {
                            getProductImg(product);
                            if (isBuyer) {
                                Log.d(TAG, "callback: isBuyer: " + product.id);
                                if (likedProductList.contains(product.id)) {
                                    product.isLiked = true;
                                }
                                allPromotionProductsList.add(product);
                                buyerAdapter.notifyDataSetChanged();
                            } else {
                                allPromotionProductsList.add(product);
                                sellerAdapter.notifyDataSetChanged();
                            }
                            Log.d(TAG, "callback: allPromotionProductsList size " + allPromotionProductsList.size());
                        }
                    });
                }
            }
        });
    }

    public void getProductImg(Product product) {
        List<String> uriLinks = new ArrayList<>();
        if (product.image_urls != null && product.image_urls.size() > 0) {
            uriLinks.addAll(product.image_urls.values());
        }
        for (String uriLink : uriLinks) {
            myDb.downloadImage(Uri.parse(uriLink), new RealtimeDatabase.DownloadImageCallback() {
                @Override
                public void callback(byte[] image_bytes) {
                    product.bitmapList.add(BitmapConverter.getImgBitmap(image_bytes));
                    if (isBuyer) {
                        buyerAdapter.notifyDataSetChanged();
                    } else {
                        sellerAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
    }

    public void getLikedProductList() {
        likedProductList.removeAll(likedProductList);
        myDb.getFavProducts(username, new RealtimeDatabase.GetFavProductsCallback() {
            @Override
            public void callback(ArrayList<String> product_ids) {
                likedProductList.addAll(product_ids);
            }
        });
    }

    public void createRvProducts() {
        rv_products = findViewById(R.id.rv_products_promotion);
        if (isBuyer) {
            buyerAdapter = new FavProductsAdapter(allPromotionProductsList);
        } else {
            sellerAdapter = new SellerHomeProductsAdapter(allPromotionProductsList);
        }
        MyClickListener clickListener = new MyClickListener() {
            @Override
            public void clickListener(int position) {
                if (!isBuyer && position == 0) {
                    openAddProductInPromotion();
                    Log.d(TAG, "clickListener: first item of seller");
                } else {
                    Intent intent = new Intent(PromotionActivity.this, ProductActivity.class);
                    intent.putExtra("isBuyer", isBuyer);
                    intent.putExtra("storename", storename);
                    intent.putExtra("username", username);
                    intent.putExtra("product_id", allPromotionProductsList.get(position).id);
                    startActivity(intent);
                }
            }
        };
        MyClickListener removeClickListener = new MyClickListener() {
            @Override
            public void clickListener(int position) {
                Product removeProduct = allPromotionProductsList.get(position);
                String product_id = removeProduct.id;
                String productName = removeProduct.name;
                AlertDialog.Builder builder = new AlertDialog.Builder(PromotionActivity.this)
                        .setTitle("Remove " + productName + " ?")
                        .setMessage("You will remove this item from the promotion")
                        .setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                myDb.removeProductFromPromotion(promotion_id, product_id);
                                allPromotionProductsList.remove(removeProduct);
                                sellerAdapter.notifyDataSetChanged();
                                dialogInterface.dismiss();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        };
        if (isBuyer) {
            buyerAdapter.setClickListeners(null, clickListener);
            rv_products.setAdapter(buyerAdapter);
        } else {
            sellerAdapter.setClickListeners(clickListener, removeClickListener);
            rv_products.setAdapter(sellerAdapter);
        }
        rv_products.setLayoutManager(new GridLayoutManager(this, 2, LinearLayoutManager.VERTICAL, false));
    }

    public void openAddProductInPromotion() {
        Intent intent = new Intent(PromotionActivity.this, AddProductInPromotionActivity.class);
        intent.putExtra("storename", storename);
        intent.putExtra("promotion_id", promotion_id);
        intent.putExtra("promotion_title", promotionTitle.getText().toString());
        intent.putExtra("promotion_desc", promotionDescription.getText().toString());
        intent.putExtra("list", (Serializable) allProductsNoPromotionList);
        Log.d(TAG, "openAddProductInPromotion: list size " + allPromotionProductsList.size());
        Log.d(TAG, "openAddProductInPromotion: storename " + storename);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isBuyer) {
            getLikedProductList();
        }
        getProductsPromotionList();
    }

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
                    intent = new Intent(PromotionActivity.this, BuyerHomeActivity.class);
                    intent.putExtra("username", username);
                } else {
                    intent = new Intent(PromotionActivity.this, SellerHomeActivity.class);
                    intent.putExtra("storename", storename);
                }
                startActivity(intent);
                return true;

            case R.id.log_out:
                if (isBuyer) {
                    BuyerHomeActivity.removeUsernameFromLocalCache();
                    intent = new Intent(PromotionActivity.this, ChoiceActivity.class);

                } else {
                    SellerHomeActivity.removeUsernameFromLocalCache();
                    intent = new Intent(PromotionActivity.this, ChoiceActivity.class);
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected((item));
        }

    }
}