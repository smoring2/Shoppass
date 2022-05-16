package edu.neu.madcourse.numad22sp_shoppass.components;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Product implements Serializable {
    public String name;
    public String id;
    public String description;
    public HashMap<String, String> image_urls;
    public List<Bitmap> bitmapList;
    public Float price;
    public boolean isLiked;
    public boolean isChosenForPromotion;
    public Product(){

    }

    public Product(String name, String id, Float price, String description, HashMap<String, String> image_urls) {
        this.name = name;
        this.price = price;
        this.description = description;
        this.image_urls = image_urls;
        this.bitmapList = new ArrayList<>();
        this.id = id;
        this.isLiked = false;
        this.isChosenForPromotion = false;
    }
}
