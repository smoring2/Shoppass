package edu.neu.madcourse.numad22sp_shoppass.activities;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.neu.madcourse.numad22sp_shoppass.R;
import edu.neu.madcourse.numad22sp_shoppass.activities.recyclerview.BuyerHomeAllShopsAdapter;
import edu.neu.madcourse.numad22sp_shoppass.activities.recyclerview.BuyerHomeFavStoresAdapter;
import edu.neu.madcourse.numad22sp_shoppass.activities.recyclerview.BuyerHomePromotionsAdapter;
import edu.neu.madcourse.numad22sp_shoppass.activities.recyclerview.MyClickListener;
import edu.neu.madcourse.numad22sp_shoppass.components.Promotion;
import edu.neu.madcourse.numad22sp_shoppass.components.Store;
import edu.neu.madcourse.numad22sp_shoppass.database.RealtimeDatabase;

public class BuyerHomeActivity extends AppCompatActivity {
    private static String TAG = "BuyerHomeActivity";

    // recyclerview
    private RecyclerView rv_nearbyStores, rv_promotions, rv_allshops;
    private List<Store> nearbyStoreList = new ArrayList<>(), allStoreList = new ArrayList<>();
    private List<Promotion> promotionList = new ArrayList<>();
    private BuyerHomePromotionsAdapter homePromotionsAdapter;
    private BuyerHomeFavStoresAdapter homeNearbyStoresAdapter;
    private BuyerHomeAllShopsAdapter homeAllShopsAdapter;

    // widgets
    private ConstraintLayout cl_list;
    private TextView tv_username;

    // dbl
    private RealtimeDatabase myDb;
    private String username;

    private static SharedPreferences sharedPreferences;

    // GPS
    private FusedLocationProviderClient fusedLocationClient;
    private HashMap<String, Double> currentLocation = new HashMap<>();
    private Double currentLongitude;
    private Double currentLatitude;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private final int locationRequestCode = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buyer_home);
        myDb = new RealtimeDatabase();
        username = getIntent().getStringExtra("username");
        rv_nearbyStores = findViewById(R.id.rv_favStores);
        rv_promotions = findViewById(R.id.rv_buyerHomePromotion);
        rv_allshops = findViewById(R.id.rv_shops);
        cl_list = findViewById(R.id.cl_list);
        cl_list.setVisibility(View.GONE);
        tv_username = findViewById(R.id.tv_listUsername);
        try {
            setCurrentLocation();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        createRvNearbyStores();
        createRvPromotions();
        createRvAllShops();

        this.sharedPreferences = getApplicationContext().getSharedPreferences("Preference", MODE_PRIVATE);

        // If the user click on the notification to open this activity, redirect to promotion page
        if (getIntent().getStringExtra("promotion_id") != null) {
            Log.e(TAG, getIntent().getStringExtra("promotion_id"));
            Log.e(TAG, getIntent().getStringExtra("storename"));
            Log.e(TAG, getIntent().getStringExtra("username"));
            Log.e(TAG, String.valueOf(getIntent().getBooleanExtra("isBuyer", true)));
            Intent intent = new Intent(this, PromotionActivity.class);
            intent.putExtra("promotion_id", getIntent().getStringExtra("promotion_id"));
            intent.putExtra("storename", getIntent().getStringExtra("storename"));
            intent.putExtra("username", getIntent().getStringExtra("username"));
            intent.putExtra("isBuyer", getIntent().getBooleanExtra("isBuyer", true));
            startActivity(intent);
        }

        Log.e(TAG, "ON CREATE");
    }

    public void getAllStores() {
        allStoreList.removeAll(allStoreList);
        myDb.getAllStores(new RealtimeDatabase.GetAllStoresCallback() {
            @Override
            public void callback(ArrayList<String> store_names) {
                for (String storeName : store_names) {
                    myDb.getStoreFromName(storeName, new RealtimeDatabase.GetStoreFromNameCallback() {
                        @Override
                        public void callback(Store Store) {
                            allStoreList.add(Store);
                            homeAllShopsAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        });
    }

    public void getAllPromotions() {
        promotionList.removeAll(promotionList);
        myDb.getAllPromotions(new RealtimeDatabase.GetAllPromotionsCallback() {
            @Override
            public void callback(ArrayList<String> promotion_ids) {
                for (String promotion_id : promotion_ids) {
                    myDb.getPromotionFromId(promotion_id, new RealtimeDatabase.GetPromotionFromIdCallback() {
                        @Override
                        public void callback(Promotion promotion) {
                            promotionList.add(promotion);
                            homePromotionsAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        });
    }

    public void getNearbyStores() {
        nearbyStoreList.removeAll(nearbyStoreList);
        myDb.getAllNearByStore(this.currentLocation, new RealtimeDatabase.GetAllNearByStores() {
            @Override
            public void callback(ArrayList<String> store_names) {
                for (String storeName : store_names) {
                    myDb.getStoreFromName(storeName, new RealtimeDatabase.GetStoreFromNameCallback() {
                        @Override
                        public void callback(Store Store) {
                            nearbyStoreList.add(Store);
                            homeNearbyStoresAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        });
    }

    public void buyerHomeTopClick(View view) {
        switch (view.getId()) {
            case R.id.ib_showUserAccount:
                if (cl_list.getVisibility() == View.VISIBLE) {
                    cl_list.setVisibility(View.GONE);
                } else {
                    cl_list.setVisibility(View.VISIBLE);
                    cl_list.bringToFront();
                    tv_username.setText(username);
                }
        }
    }

    public void listClick(View view) {
        Intent intent = null;
        switch (view.getId()) {
            case R.id.tv_listFavProducts:
                intent = new Intent(BuyerHomeActivity.this, FavProductsActivity.class);
                break;
            case R.id.tv_listFavStores:
                intent = new Intent(BuyerHomeActivity.this, FavStoresActivity.class);
                break;
        }
        intent.putExtra("username", username);
        startActivity(intent);
    }

    public void createRvNearbyStores() {
        MyClickListener clickListener = new MyClickListener() {
            @Override
            public void clickListener(int position) {
                Store clickedStore = nearbyStoreList.get(position);
                Intent intent = new Intent(BuyerHomeActivity.this, StoreActivity.class);
                intent.putExtra("username", username);
                intent.putExtra("storename", clickedStore.name);
                intent.putExtra("isBuyer", true);
                intent.putExtra("address", clickedStore.address);
                startActivity(intent);
            }
        };
        homeNearbyStoresAdapter = new BuyerHomeFavStoresAdapter(nearbyStoreList);
        homeNearbyStoresAdapter.setClickListener(clickListener);
        rv_nearbyStores.setAdapter(homeNearbyStoresAdapter);
        Log.d(TAG, "createRvFavStores: size " + nearbyStoreList.size());
    }

    public void createRvPromotions() {
        MyClickListener clickListener = new MyClickListener() {
            @Override
            public void clickListener(int position) {
                Intent intent = new Intent(BuyerHomeActivity.this, PromotionActivity.class);
                intent.putExtra("promotion_id", promotionList.get(position).id);
                intent.putExtra("username", username);
                intent.putExtra("isBuyer", true);
                Log.d(TAG, "clickListener: promotion_id " +  promotionList.get(position).id);
                startActivity(intent);
            }
        };
        homePromotionsAdapter = new BuyerHomePromotionsAdapter(promotionList);
        homePromotionsAdapter.setClickListener(clickListener);
        rv_promotions.setAdapter(homePromotionsAdapter);
        getAllPromotions();
        Log.d(TAG, "createRvPromotions: size " + promotionList.size());
    }

    public void createRvAllShops() {
        MyClickListener clickListener = new MyClickListener() {
            @Override
            public void clickListener(int position) {
                Store clickedStore = allStoreList.get(position);
                Intent intent = new Intent(BuyerHomeActivity.this, StoreActivity.class);
                intent.putExtra("username", username);
                intent.putExtra("storename", clickedStore.name);
                intent.putExtra("address", clickedStore.address);
                intent.putExtra("isBuyer", true);
                Log.d(TAG, "clickListener: allshops username " + username);
                Log.d(TAG, "clickListener: allshops storename " + clickedStore.name);
                Log.d(TAG, "clickListener: allshops address " + clickedStore.address);
                Log.d(TAG, "clickListener: allshops isBuyer " + true);

                startActivity(intent);
            }
        };
        homeAllShopsAdapter = new BuyerHomeAllShopsAdapter(allStoreList);
        homeAllShopsAdapter.setClickListener(clickListener);
        rv_allshops.setAdapter(homeAllShopsAdapter);
        Log.d(TAG, "createRvAllShops: size " + allStoreList.size());
    }

    public static void removeUsernameFromLocalCache() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    private void setCurrentLocation() throws InterruptedException {
        fusedLocationClient = (FusedLocationProviderClient) LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(20 * 1000);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NotNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        currentLongitude = location.getLongitude();
                        currentLatitude = location.getLatitude();
                        currentLocation.put("latitude", currentLatitude);
                        currentLocation.put("longitude", currentLongitude);
                        Log.e(TAG, "location call back" + currentLongitude + " / " + currentLatitude);
                    }
                }
            }
        };

        // Check permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission not granted
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, locationRequestCode);
        } else {
            // Already permission granted
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                currentLongitude = location.getLongitude();
                currentLatitude = location.getLatitude();
                currentLocation.put("latitude", currentLatitude);
                currentLocation.put("longitude", currentLongitude);
                Log.e(TAG, "Already permission granted" + currentLongitude + " / " + currentLatitude);
            });
        }
        Thread.sleep(1000);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int [] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1000) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, locationRequestCode);
                    return;
                }
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
                fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                    currentLongitude = location.getLongitude();
                    currentLatitude = location.getLatitude();
                    currentLocation.put("latitude", currentLatitude);
                    currentLocation.put("longitude", currentLongitude);
                    Log.e(TAG, currentLongitude + " / " + currentLatitude);
                });
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        cl_list.setVisibility(View.GONE);
        Log.e(TAG, "ON START");
    }

    @Override
    protected void onResume() {
        super.onResume();
        cl_list.setVisibility(View.GONE);
        try {
            setCurrentLocation();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        getAllStores();
        getNearbyStores();
        getAllPromotions();
        createRvNearbyStores();
        Log.e(TAG, "ON RESUME");
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
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
//                intent = new Intent(SellerAddNewProductActivity.this, SellerHomeActivity.class);
//                startActivity(intent);
                return true;

            case R.id.log_out:
                BuyerHomeActivity.removeUsernameFromLocalCache();
                intent = new Intent(BuyerHomeActivity.this, ChoiceActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected((item));
        }

    }
}