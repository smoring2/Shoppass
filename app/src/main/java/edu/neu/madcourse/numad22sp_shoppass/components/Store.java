package edu.neu.madcourse.numad22sp_shoppass.components;

import org.json.JSONException;
import java.util.HashMap;
import edu.neu.madcourse.numad22sp_shoppass.utils.LocatorUtil;

public class Store {
    private String TAG = "Store";
    public String name;
    public String address;
    public double latitude;
    public double longitude;
    public boolean isLiked;

    public Store(String name, String address) {
        this.name = name;
        this.address = address;
        this.isLiked = false;
    }

    public void setLocation() throws InterruptedException, JSONException {
        HashMap<String, Double> location = LocatorUtil.geocodingHttpConnection(this.address);
        this.latitude = location.get("latitude");
        this.longitude = location.get("longitude");
    }
}
