package edu.neu.madcourse.numad22sp_shoppass.database;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import edu.neu.madcourse.numad22sp_shoppass.components.Customer;
import edu.neu.madcourse.numad22sp_shoppass.components.Product;
import edu.neu.madcourse.numad22sp_shoppass.components.Promotion;
import edu.neu.madcourse.numad22sp_shoppass.components.Store;
import edu.neu.madcourse.numad22sp_shoppass.utils.LocatorUtil;

public class RealtimeDatabase {
    private final FirebaseDatabase db;
    private final DatabaseReference dbRef;
    private final FirebaseStorage storage;
    private final StorageReference storageRef;

    private static String TAG = "RealtimeDatabase";

    public RealtimeDatabase() {
        this.db = FirebaseDatabase.getInstance();
        this.storage = FirebaseStorage.getInstance();
        this.dbRef = db.getReference();
        this.storageRef = storage.getReference();
    }

    /**
     * Registers a new customer in the database, keyed by their authenticated UID.
     * Rejects registration if the UID already exists.
     */
    public void registerCustomer(String uid, Customer customer, AuthResultCallback callback) {
        dbRef.child("customers").child(uid).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                    callback.onFailure("Database error.");
                } else {
                    if (task.getResult().getValue() != null) {
                        // Customer already exists for this UID — this is fine for login,
                        // the record already belongs to this authenticated user.
                        callback.onSuccess();
                    } else {
                        // New customer registration
                        dbRef.child("customers").child(uid).setValue(customer)
                                .addOnSuccessListener(aVoid -> callback.onSuccess())
                                .addOnFailureListener(e -> callback.onFailure("Failed to register customer."));
                    }
                }
            }
        });
    }

    /**
     * Registers a new store in the database. Rejects registration if the store name
     * is already taken by a different owner (ownerUid mismatch).
     */
    public void registerStore(String uid, Store store, AuthResultCallback callback) {
        dbRef.child("stores").child(store.name).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                    callback.onFailure("Database error.");
                } else {
                    if (task.getResult().getValue() != null) {
                        // Store already exists — check owner
                        HashMap<String, Object> existingStore = (HashMap<String, Object>) task.getResult().getValue();
                        String existingOwnerUid = (String) existingStore.get("ownerUid");
                        if (existingOwnerUid != null && existingOwnerUid.equals(uid)) {
                            // Same owner, allow login
                            callback.onSuccess();
                        } else {
                            // Different owner or no ownerUid — reject
                            callback.onFailure("Store name is already taken.");
                        }
                    } else {
                        // New store — register with ownerUid
                        store.ownerUid = uid;
                        dbRef.child("stores").child(store.name).setValue(store)
                                .addOnSuccessListener(aVoid -> callback.onSuccess())
                                .addOnFailureListener(e -> callback.onFailure("Failed to register store."));
                    }
                }
            }
        });
    }

    /**
     * Verifies that the authenticated user (uid) owns the store with the given name.
     */
    public void verifyStoreOwner(String uid, String storeName, AuthResultCallback callback) {
        dbRef.child("stores").child(storeName).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                    callback.onFailure("Database error.");
                } else {
                    if (task.getResult().getValue() == null) {
                        callback.onFailure("Store does not exist.");
                    } else {
                        HashMap<String, Object> existingStore = (HashMap<String, Object>) task.getResult().getValue();
                        String existingOwnerUid = (String) existingStore.get("ownerUid");
                        if (existingOwnerUid != null && existingOwnerUid.equals(uid)) {
                            callback.onSuccess();
                        } else {
                            callback.onFailure("You do not own this store.");
                        }
                    }
                }
            }
        });
    }

    public void addProduct(Product product, String store_name) {
        Map<String, Object> map = new HashMap<>();
        map.put(product.id, product);
        //according the its function, changed .child("product"). to .child("products").
        dbRef.child("products").child(store_name).updateChildren(map);
        dbRef.child("product_store").child(product.id).setValue(store_name);
    }

    public void addPromotion(Promotion promotion) {
        Map<String, Object> promotion_map = new HashMap<>();
        promotion_map.put(promotion.id, promotion);
        dbRef.child("promotions").updateChildren(promotion_map);

        Map<String, Object> promotion_store_map = new HashMap<>();
        promotion_store_map.put(promotion.id, promotion.id);
        dbRef.child("promotion_store").child(promotion.store).updateChildren(promotion_store_map);
    }

    public void addProductToPromotion(String promotion_id, String product_id) {
        ;
        dbRef.child("promotions").child(promotion_id).child("eligibleProducts").child(product_id).setValue(product_id);
    }

    public void removeProductFromPromotion(String promotion_id, String product_id) {
        ;
        dbRef.child("promotions").child(promotion_id).child("eligibleProducts").child(product_id).removeValue();
    }

    public void addFavProduct(String customer_name, String product_id) {
        Map<String, Object> map = new HashMap<>();
        map.put(product_id, product_id);
        dbRef.child("fav_products").child(customer_name).updateChildren(map);
    }

    public void addFavStore(String customer_name, String store_name) {
        Map<String, Object> map = new HashMap<>();
        map.put(store_name, store_name);
        dbRef.child("fav_stores").child(customer_name).updateChildren(map);
    }

    public void removeFavProduct(String customer_name, String product_id) {
        dbRef.child("fav_products").child(customer_name).child(product_id).removeValue();
    }

    public void removeFavStore(String customer_name, String store_name) {
        dbRef.child("fav_stores").child(customer_name).child(store_name).removeValue();
    }

    public void getFavStores(String customer_name, GetFavStoresCallback i) {
        dbRef.child("fav_stores").child(customer_name).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                    i.callback(null);
                } else {
                    HashMap<String, Object> store_map = (HashMap<String, Object>) task.getResult().getValue();
                    ArrayList<String> store_names = new ArrayList<>();
                    if (store_map != null) {
                        store_map.forEach((k, v) -> {
                            store_names.add(k);
                        });
                    }
                    i.callback(store_names);
                }
            }
        });
    }

    public void getFavProducts(String user_id, GetFavProductsCallback i) {
        dbRef.child("fav_products").child(user_id).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                    i.callback(null);
                } else {
                    HashMap<String, Object> product_map = (HashMap<String, Object>) task.getResult().getValue();
                    ArrayList<String> product_ids = new ArrayList<>();
                    if (product_map != null) {
                        product_map.forEach((k, v) -> {
                            product_ids.add(k);
                        });
                    }
                    i.callback(product_ids);
                }
            }
        });
    }

    public void getAllStores(GetAllStoresCallback i) {
        dbRef.child("stores").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                    i.callback(null);
                } else {
                    HashMap<String, Object> store_map = (HashMap<String, Object>) task.getResult().getValue();
                    ArrayList<String> store_names = new ArrayList<>();
                    store_map.forEach((k, v) -> {
                        store_names.add(k);
                    });
                    i.callback(store_names);
                }
            }
        });
    }

    //Add getAllPromotions method.
    public void getAllPromotions(GetAllPromotionsCallback i) {
        dbRef.child("promotions").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                    i.callback(null);
                } else {
                    HashMap<String, Object> store_map = (HashMap<String, Object>) task.getResult().getValue();
                    ArrayList<String> promotion_ids = new ArrayList<>();
                    if (store_map != null) {
                        store_map.forEach((k, v) -> {
                            promotion_ids.add(k);
                        });
                    }
                    i.callback(promotion_ids);
                }
            }
        });
    }

    public void getAllNearByStore(HashMap<String, Double> currentLocation, GetAllNearByStores i) {
        dbRef.child("stores").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                    i.callback(null);
                } else {
                    HashMap<String, HashMap<String, Object>> store_map = (HashMap<String, HashMap<String, Object>>) task.getResult().getValue();
                    ArrayList<String> store_names = new ArrayList<>();
                    store_map.forEach((k, v) -> {
                        double currentLatitude = currentLocation.size() > 0 ? currentLocation.get("latitude") : 0;
                        double currentLongitude = currentLocation.size() > 0 ? currentLocation.get("longitude") : 0;
                        double storeLatitude = Double.parseDouble(String.valueOf(v.get("latitude")));
                        double storeLongitude = Double.parseDouble(String.valueOf(v.get("longitude")));
                        Log.e(TAG, "storeLatitude: " + storeLatitude);
                        Log.e(TAG, "storeLongitude: " + storeLongitude);
                        if (LocatorUtil.isNearBy(currentLatitude, currentLongitude, storeLatitude, storeLongitude)) {
                            store_names.add(k);
                        }
                    });
                    i.callback(store_names);
                }
            }
        });
    }

    public void getStorePromotions(String store_id, GetStorePromotionsCallback i) {
        dbRef.child("promotion_store").child(store_id).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                    i.callback(null);
                } else {
                    HashMap<String, Object> promotion_map = (HashMap<String, Object>) task.getResult().getValue();
                    ArrayList<String> promotion_ids = new ArrayList<>();
                    if (promotion_map != null) {
                        promotion_map.forEach((k, v) -> {
                            promotion_ids.add(k);
                        });
                    }
                    i.callback(promotion_ids);
                }
            }
        });
    }

    public void getStoreProducts(String store_name, GetStoreProductsCallback i) {
        dbRef.child("products").child(store_name).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                    i.callback(null);
                } else {
                    ArrayList<Product> products = new ArrayList<>();
                    HashMap<String, Object> all_products_map = (HashMap<String, Object>) task.getResult().getValue();
                    if (all_products_map != null) {
                        all_products_map.forEach((k, v) -> {
                            HashMap<String, Object> product_map = (HashMap<String, Object>) v;
                            String name = (String) product_map.get("name");
                            String id = (String) product_map.get("id");
                            //solved the java.lang.Long cannot be cast to java.lang.Float issue
                            Float price = ((Long) product_map.get("price")).floatValue();
                            String description = (String) product_map.get("description");
                            HashMap<String, String> image_urls = (HashMap<String, String>) product_map.get("image_urls");
                            Product product = new Product(name, id, price, description, image_urls);
                            products.add(product);
                        });
                    }
                    i.callback(products);
                }
            }
        });
    }

    public void getPromotionFromId(String id, GetPromotionFromIdCallback i) {
        dbRef.child("promotions").child(id).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e(TAG, "Error getting data", task.getException());
                    i.callback(null);
                } else {
                    Log.d(TAG, "onComplete:  promotion_id: " + id);
                    HashMap<String, Object> promotion_map = (HashMap<String, Object>) task.getResult().getValue();
                    Log.d(TAG, "onComplete: task: " + promotion_map.toString());
                    String store = (String) promotion_map.get("store");
                    String id = (String) promotion_map.get("id");
                    String title = (String) promotion_map.get("title");
                    String description = (String) promotion_map.get("description");
                    Promotion promotion = new Promotion(store, id, title, description);
                    HashMap<String, String> eligible_map = (HashMap<String, String>) promotion_map.get("eligibleProducts");
                    Log.d(TAG, "onComplete: eligible_map " + (eligible_map == null));
                    if (eligible_map != null) {
                        Log.d(TAG, "onComplete: eligible_map " + eligible_map.size());
                        eligible_map.forEach((k, v) -> {
                            promotion.addEligibleProduct(v);
                        });
                    }
                    i.callback(promotion);
                }
            }
        });
    }

    public void getStoreFromName(String name, GetStoreFromNameCallback i) {
        dbRef.child("stores").child(name).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                    i.callback(null);
                } else {
                    HashMap<String, Object> store_map = (HashMap<String, Object>) task.getResult().getValue();
                    String address = (String) store_map.get("address");
                    Store store = null;
                    store = new Store(name, address);
                    i.callback(store);
                }
            }
        });
    }

    public void getProductFromId(String product_id, GetProductFromIdAndStoreCallback i) {
        dbRef.child("product_store").child(product_id).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                    i.callback(null);
                } else {
                  String store_name = (String) task.getResult().getValue();
                    if (store_name != null) {
                        Log.d(TAG, "onComplete: task.getResult().getValue() " + task.getResult().getValue());
                        Log.d(TAG, "onComplete: store_name " + store_name);
                        Log.d(TAG, "onComplete: task " + task);
                        Log.d(TAG, "onComplete: product_id " + product_id);
                        dbRef.child("products").child(store_name).child(product_id).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                if (!task.isSuccessful()) {
                                    Log.e("firebase", "Error getting data", task.getException());
                                    i.callback(null);
                                } else {
                                    HashMap<String, Object> product_map = (HashMap<String, Object>) task.getResult().getValue();
                                    //solved the java.lang.Long cannot be cast to java.lang.Float issue
                                    Float price = ((Long) product_map.get("price")).floatValue();
                                    HashMap<String, String> image_urls = (HashMap<String, String>) product_map.get("image_urls");
                                    String description = (String) product_map.get("description");
                                    String name = (String) product_map.get("name");
                                    Product product = new Product(name, product_id, price, description, image_urls);
                                    i.callback(product);
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    public void uploadImage(Uri file, String username, UploadImageCallback i) {
        if (username == null) {
            username = "unknown_user";
        }

        StorageReference ref = storageRef.child("images/" + username + "/" + file.getLastPathSegment());
        UploadTask uploadTask = ref.putFile(file);

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                // Continue with the task to get the download URL
                return ref.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    i.callback(downloadUri);
                } else {
                    // Handle failures
                    // ...
                }
            }
        });
    }

    public void downloadImage(Uri uri, DownloadImageCallback i) {
        StorageReference ref = storage.getReferenceFromUrl(uri.toString());

        System.out.println(uri.toString());
        final long TEN_MEGABYTE = 10 * 1024 * 1024;
        ref.getBytes(TEN_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                System.out.println(bytes);
                i.callback(bytes);
            }
        });
    }

    public void addImageLinkToProduct(String store_name, String product_id, String image_url) {
        String key = image_url.substring(image_url.lastIndexOf("/") + 1);
        dbRef.child("products").child(store_name).child(product_id).child("image_urls").child(key).setValue(image_url);
    }

    public interface AuthResultCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    public interface GetFavStoresCallback {
        void callback(ArrayList<String> store_names);
    }

    public interface GetAllStoresCallback {
        void callback(ArrayList<String> store_names);
    }

    public interface GetAllPromotionsCallback {
        void callback(ArrayList<String> promotion_ids);
    }

    public interface GetFavProductsCallback {
        void callback(ArrayList<String> product_ids);
    }

    public interface GetStorePromotionsCallback {
        void callback(ArrayList<String> promotion_ids);
    }

    public interface GetStoreProductsCallback {
        void callback(ArrayList<Product> products);
    }

    public interface GetPromotionFromIdCallback {
        void callback(Promotion promotion);
    }

    public interface GetStoreFromNameCallback {
        void callback(Store Store);
    }

    public interface GetProductFromIdAndStoreCallback {
        void callback(Product Product);
    }

    public interface UploadImageCallback {
        void callback(Uri downloadUri);
    }

    public interface DownloadImageCallback {
        void callback(byte[] image_bytes);
    }

    public interface GetAllProductsOfOnePromotionCallBack {
        void callback(ArrayList<Product> productList);
    }

    public interface GetAllProductsOfOneStorePromotion {
        void callback(ArrayList<Product> productList);
    }

    public interface GetAllNearByStores {
        void callback(ArrayList<String> store_names);
    }
}
