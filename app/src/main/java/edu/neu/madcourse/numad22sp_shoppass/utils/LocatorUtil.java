package edu.neu.madcourse.numad22sp_shoppass.utils;

import android.location.Location;
import android.util.JsonReader;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import edu.neu.madcourse.numad22sp_shoppass.BuildConfig;

public class LocatorUtil {
    private static String TAG = "LocatorUtil";

    private static final String GEOCODING_RESOURCE = "https://maps.googleapis.com/maps/api/geocode/json?address=";
    private static final String API_KEY = BuildConfig.GEOCODING_API_KEY;

    public static Boolean isNearBy(double startLatitude, double startLongitude, double endLatitude, double endLongitude) {
        Location startPoint=new Location("start");
        startPoint.setLatitude(startLatitude);
        startPoint.setLongitude(startLongitude);

        Location endPoint = new Location("end");
        endPoint.setLatitude(endLatitude);
        endPoint.setLongitude(endLongitude);

        double distance = startPoint.distanceTo(endPoint);
        Log.e(TAG, "Calculated distance: " + distance);
        return distance <= 5000;
    }

    public static HashMap<String, Double> geocodingHttpConnection(String addressName) throws InterruptedException, JSONException {
        HashMap<String, Double> locationResponse = new HashMap<>();
        final JSONObject[] response = new JSONObject[1];
        Thread thread = new Thread(() -> {
            try {
                String addressURI = parseAddressQuery(addressName);
                Log.e(TAG, addressURI);
                URL url = new URL(addressURI);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                // Read FCM response.
                InputStream inputStream = conn.getInputStream();
                response[0] = convertStreamToJSON(inputStream);
            } catch (IOException | JSONException e) {
                response[0] = null;
            }
        });

        thread.start();
        thread.join();

        if (response[0].getString("status").equals("OK")) {
            JSONObject result = (JSONObject) response[0].getJSONArray("results").get(0);
            JSONObject location = result.getJSONObject("geometry").getJSONObject("location");
            locationResponse.put("longitude", Double.valueOf(location.getString("lng")));
            locationResponse.put("latitude", Double.valueOf(location.getString("lat")));
        } else {
            locationResponse.put("longitude", (double) 0);
            locationResponse.put("latitude", (double) 0);
        }

        return locationResponse;
    }

    private static String parseAddressQuery(String address) {
        StringBuffer query;
        String[] split = address.split(" ");

        query = new StringBuffer();
        query.append(GEOCODING_RESOURCE);

        if (split.length == 0) {
            return "";
        }

        for (int i = 0; i < split.length; i++) {
            query.append(split[i]);
            if (i < (split.length - 1)) {
                query.append("+");
            }
        }
        query.append("&key=" + API_KEY);

        return query.toString();
    }

    public static JSONObject convertStreamToJSON(InputStream inputStream) throws IOException, JSONException {
        BufferedReader bR = new BufferedReader(  new InputStreamReader(inputStream));
        String line = "";

        StringBuilder responseStrBuilder = new StringBuilder();
        while((line =  bR.readLine()) != null){
            responseStrBuilder.append(line);
        }
        inputStream.close();

        return new JSONObject(responseStrBuilder.toString());
    }
}
