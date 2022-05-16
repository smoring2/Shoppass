package edu.neu.madcourse.numad22sp_shoppass.components;

import java.util.ArrayList;
import java.util.HashMap;

public class Promotion {
    public String store;
    public String id;
    public String title;
    public String description;
    public HashMap<String, String> eligibleProducts;

    public Promotion(String store, String id, String title, String description) {
        this.store = store;
        this.id = id;
        this.title = title;
        this.description = description;
        this.eligibleProducts = new HashMap<String, String>();
    }

    public void addEligibleProduct(String product_id) {
        this.eligibleProducts.put(product_id, product_id);
    }
}
