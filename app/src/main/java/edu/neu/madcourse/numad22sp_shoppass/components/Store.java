package edu.neu.madcourse.numad22sp_shoppass.components;

import org.json.JSONException;
import java.util.HashMap;
import edu.neu.madcourse.numad22sp_shoppass.utils.LocatorUtil;

public class Store {
    private String TAG = "Store";
    public String name;
    public String address;
    public String ownerUid;
    public double latitude;
    public double longitude;
    public boolean isLiked;

    public Store() {
        // Default constructor required for Firebase deserialization
    }

    public Store(String name, String address) {
        this.name = name;
        this.address = address;
        this.isLiked = false;
    }

    public Store(String name, String address, String ownerUid) {
        this.name = name;
        this.address = address;
        this.ownerUid = ownerUid;
        this.isLiked = false;
    }

    public void setLocation() throws InterruptedException, JSONException {
        HashMap<String, Double> location = LocatorUtil.geocodingHttpConnection(this.address);
        this.latitude = location.get("latitude");
        this.longitude = location.get("longitude");
    }
}
