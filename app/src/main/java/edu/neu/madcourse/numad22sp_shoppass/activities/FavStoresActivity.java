package edu.neu.madcourse.numad22sp_shoppass.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.List;

import edu.neu.madcourse.numad22sp_shoppass.Examples;
import edu.neu.madcourse.numad22sp_shoppass.R;
import edu.neu.madcourse.numad22sp_shoppass.activities.recyclerview.FavStoresAdapter;
import edu.neu.madcourse.numad22sp_shoppass.activities.recyclerview.MyClickListener;
import edu.neu.madcourse.numad22sp_shoppass.components.Store;
import edu.neu.madcourse.numad22sp_shoppass.database.RealtimeDatabase;

public class FavStoresActivity extends AppCompatActivity {
    private RecyclerView rv_favStores;
    private FavStoresAdapter favStoresAdapter;
    private List<Store> storeList = new ArrayList<>();
    private ImageButton ib_back;
    private RealtimeDatabase myDb;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fav_stores);
        username = getIntent().getStringExtra("username");
        myDb = new RealtimeDatabase();
        rv_favStores = findViewById(R.id.rv_fav_stores);
        storeList = new ArrayList<>();
        MyClickListener clickListener = new MyClickListener() {
            @Override
            public void clickListener(int position) {
                Intent intent = new Intent(FavStoresActivity.this, StoreActivity.class);
                intent.putExtra("storename", storeList.get(position).name);
                intent.putExtra("username", username);
                startActivity(intent);
            }
        };
        favStoresAdapter = new FavStoresAdapter(storeList);
        favStoresAdapter.setClickListener(clickListener);
        rv_favStores.setAdapter(favStoresAdapter);
        ib_back = findViewById(R.id.ib_backarrow);
        ib_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    public void getAllFavStores() {
        storeList.removeAll(storeList);
        myDb.getFavStores(username, new RealtimeDatabase.GetFavStoresCallback() {
            @Override
            public void callback(ArrayList<String> store_names) {
                for (String storeName : store_names) {
                    myDb.getStoreFromName(storeName, new RealtimeDatabase.GetStoreFromNameCallback() {
                        @Override
                        public void callback(Store store) {
                            store.isLiked = true;
                            storeList.add(store);
                            favStoresAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getAllFavStores();
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
                intent = new Intent(FavStoresActivity.this, BuyerHomeActivity.class);
                intent.putExtra("username", username);
                startActivity(intent);
                return true;

            case R.id.log_out:
                BuyerHomeActivity.removeUsernameFromLocalCache();
                intent = new Intent(FavStoresActivity.this, ChoiceActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected((item));
        }

    }
}