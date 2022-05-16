package edu.neu.madcourse.numad22sp_shoppass.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.List;

import edu.neu.madcourse.numad22sp_shoppass.Examples;
import edu.neu.madcourse.numad22sp_shoppass.R;
import edu.neu.madcourse.numad22sp_shoppass.activities.recyclerview.FavProductsAdapter;
import edu.neu.madcourse.numad22sp_shoppass.activities.recyclerview.MyClickListener;
import edu.neu.madcourse.numad22sp_shoppass.components.Product;
import edu.neu.madcourse.numad22sp_shoppass.database.RealtimeDatabase;
import edu.neu.madcourse.numad22sp_shoppass.utils.BitmapConverter;

public class FavProductsActivity extends AppCompatActivity {
    private RecyclerView rv_favProducts;
    private List<Product> productList = new ArrayList<>();
    private FavProductsAdapter favProductsAdapter;
    private ImageButton ib_back;

    private String username;
    private RealtimeDatabase myDb;

    private static String TAG = "FavProductsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fav_products);
        myDb = new RealtimeDatabase();
        username = getIntent().getStringExtra("username");
        Log.d(TAG, "onCreate: username " + username);
        createRvFavProducts();
        ib_back = findViewById(R.id.ib_backarrow);
        ib_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    public void getFavProductList() {
        productList.removeAll(productList);
        Log.d(TAG, "getFavProductList: ");
        Log.d(TAG, "getFavProductList: username " + username);
        myDb.getFavProducts(username, new RealtimeDatabase.GetFavProductsCallback() {
            @Override
            public void callback(ArrayList<String> product_ids) {
                for (String product_id : product_ids) {
                    Log.d(TAG, "callback: product_id " + product_id);
                    myDb.getProductFromId(product_id, new RealtimeDatabase.GetProductFromIdAndStoreCallback() {
                        @Override
                        public void callback(Product product) {
                            product.isLiked = true;
                            downloadImgs(product);
                            productList.add(product);
                            Log.d(TAG, "callback: productList size " + productList.size());
                            favProductsAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        });
    }

    public void downloadImgs(Product product) {
        List<String> imgUrlList = new ArrayList<>();
        if (product.image_urls != null && product.image_urls.size() > 0) {
            imgUrlList = new ArrayList<>(product.image_urls.values());
        }
        if (imgUrlList.size() == 0) {
            Drawable drawable = getDrawable(R.drawable.example_product_img);
            Bitmap bitmap = BitmapConverter.drawableToBitmap(drawable);
            product.bitmapList.add(bitmap);
            favProductsAdapter.notifyDataSetChanged();
            return;
        }
        for (String url : imgUrlList) {
            Uri uri = Uri.parse(url);
            myDb.downloadImage(uri, new RealtimeDatabase.DownloadImageCallback() {
                @Override
                public void callback(byte[] image_bytes) {
                    Bitmap bitmap = BitmapConverter.getImgBitmap(image_bytes);
                    product.bitmapList.add(bitmap);
                    favProductsAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    public void createRvFavProducts() {
        rv_favProducts = findViewById(R.id.rv_fav_products);
        MyClickListener openProdClickListener = new MyClickListener() {
            @Override
            public void clickListener(int position) {
                Intent intent = new Intent(FavProductsActivity.this, ProductActivity.class);
                String productId = productList.get(position).id;
                intent.putExtra("product_id", productId);
                intent.putExtra("username", username);
                startActivity(intent);
            }
        };
        MyClickListener likeClickListener = new MyClickListener() {
            @Override
            public void clickListener(int position) {
                String product_id = productList.get(position).id;
                removeProductFromLikeList(product_id);
            }
        };
        favProductsAdapter = new FavProductsAdapter(productList);
        favProductsAdapter.setClickListeners(likeClickListener, openProdClickListener);
        rv_favProducts.setAdapter(favProductsAdapter);
        rv_favProducts.setLayoutManager(new GridLayoutManager(this, 2, LinearLayoutManager.VERTICAL, false));
    }

    public void removeProductFromLikeList(String product_id) {
        myDb.removeFavProduct(username, product_id);
        getFavProductList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getFavProductList();
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
                intent = new Intent(FavProductsActivity.this, BuyerHomeActivity.class);
                intent.putExtra("username", username);
                startActivity(intent);
                return true;

            case R.id.log_out:
                BuyerHomeActivity.removeUsernameFromLocalCache();
                intent = new Intent(FavProductsActivity.this, ChoiceActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected((item));
        }

    }
}