package com.laioffer.eventreporter;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.LOCATION_SERVICE;

public class LocationTracker implements LocationListener {
    private static final String TAG = "LocationTracker";

    private final Activity mContext;
    private static final int PERMISSIONS_REQUEST_LOCATION = 90;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60;

    private boolean mIsGPSEnabled;
    private boolean mIsNetworkEnabled;

    private Location location;
    private double latitude;
    private double longitude;
    private LocationManager locationManager;

    public LocationTracker(Activity context) {
        mContext = context;
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    /**
    *  This function returns the user's current location either from GPS or from network.
    *  GPS will be picked up with higher priority.
    *  @return current location, on emulator, default is 1600 Amphitheatre Way, Mountain View.
    */
    public Location getLocation() {
        try {
            locationManager = (LocationManager) mContext
                    .getSystemService(LOCATION_SERVICE);
            // Get GPS status
            mIsGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);
            // Get network status
            mIsNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!mIsGPSEnabled && !mIsNetworkEnabled) {
                return null;
            }

            // First, get location from network provider
            checkLocationPermission();

            Log.d(TAG, "mIsNetworkEnabled: " + mIsNetworkEnabled);
            Log.d(TAG, "mIsGPSEnabled: " + mIsGPSEnabled);
            if (mIsNetworkEnabled) {
                locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES,
                        this
                );
                if (locationManager != null) {
                    location = locationManager
                            .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    Log.d(TAG, "location after network: " + location);
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                    }
                }
            }

            // If GPS enabled, get lat/lon using GPS services
            if (mIsGPSEnabled) {
                if (location == null) {
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            this
                    );
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        Log.d(TAG, "location after gps: " + location);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

    public double getLatitude() {
        if (location != null) {
            latitude = location.getLatitude();
        }
        return latitude;
    }

    public double getLongitude() {
        if (location != null) {
            longitude = location.getLongitude();
        }
        return longitude;
    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // We need to request permission
            ActivityCompat.requestPermissions(
                    mContext,
                    new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_LOCATION
            );
        }
        return true;
    }

    public static JSONObject getLocationInfo(double lat, double lon) {
        HttpGet httpGet = new HttpGet(
                "http://maps.googleapis.com/maps/api/geocode/json?latlng="
                        + lat + "," + lon + "&sensor=true"
        );
        HttpClient client = new DefaultHttpClient();
        HttpResponse response;
        StringBuilder stringBuilder = new StringBuilder();

        try {
            response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            InputStream stream = entity.getContent();
            int b;
            while ((b = stream.read()) != -1) {
                stringBuilder.append((char) b);
            }
        } catch (ClientProtocolException e) {

        } catch (Exception e) {

        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject = new JSONObject(stringBuilder.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    /* After passing lat and lon, we sent request to Google Location backend requesting address,
     * which returns as JSON format. We need to parse the returning JSON format and transform to
     * human readable format
     * @param lat latitude
     * @param lon longitude
     * @return Array of addresses, street, city, state
     * */
    public List<String> getCurrentLocationViaJSON(double lat, double lon) {
//        Log.d(TAG, "lat: " + lat + "lon: " + lon);
        List<String> addresses = new ArrayList<>();
        JSONObject jsonObject = getLocationInfo(lat, lon);
        try {
            String status = jsonObject.getString("status").toString();
            if (status.equalsIgnoreCase("OK")) {
                JSONArray results = jsonObject.getJSONArray("results");

                int i = 0;
                do {
                    JSONObject r = results.getJSONObject(i);
                    if (!r.getString("formatted_address").equals("")) {
                        String[] formatted_addresses = r.getString("formatted_address")
                                .split(",");
                        addresses.add(formatted_addresses[0]);
                        addresses.add(formatted_addresses[1]);
                        addresses.add(formatted_addresses[2]);
                        addresses.add(formatted_addresses[3]);
                    }
                    i++;
                } while (i < results.length());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return addresses;
    }
}
