package com.jpoh.esptouch_smartconfig;

import androidx.annotation.NonNull;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiInfo;
import android.location.LocationManager;
import android.os.Build;
import android.util.Log;
import androidx.core.content.ContextCompat;
import java.util.HashMap;
import java.util.Map;

public class WifiInfoData {
    private static final String TAG = "esptouch_smartconfig";
    private final WifiManager wifiManager;
    private final Context context;

    public WifiInfoData(WifiManager wifiManager, Context context) {
        this.wifiManager = wifiManager;
        this.context = context;
    }

    private boolean hasPermission(String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean checkPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return true;  // No extra permission required before Android O (8.0)
        }

        boolean hasWifiState = hasPermission(Manifest.permission.CHANGE_WIFI_STATE);
        boolean hasFineLocation = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        boolean hasCoarseLocation = hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && (!hasFineLocation || !hasWifiState)) {
            logPermissionWarning("Android Q (10.0) requires CHANGE_WIFI_STATE & ACCESS_FINE_LOCATION.");
            return false;
        }

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.P) {
            if (!hasWifiState) {
                logPermissionWarning("Android P (9.0) requires CHANGE_WIFI_STATE.");
                return false;
            }
            if (!hasFineLocation && !hasCoarseLocation) {
                logPermissionWarning("Android P (9.0) requires either ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION.");
                return false;
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (locationManager != null && !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                logPermissionWarning("Android P (9.0) and above require GPS to be enabled.");
                return false;
            }
        }

        return hasWifiState || hasFineLocation || hasCoarseLocation;
    }

    private void logPermissionWarning(String message) {
        Log.w(TAG, "Missing Permissions: " + message + "\n" +
                "Check: https://developer.android.com/guide/topics/connectivity/wifi-scan");
    }

    public Map<String, String> getWifiData() {
        if (!checkPermissions()) {
            return new HashMap<>(); // Return an empty map instead of null to avoid crashes
        }

        Map<String, String> returnData = new HashMap<>();
        WifiInfo wifiInfo = getWifiInfo();

        if (wifiInfo == null) {
            return returnData;
        }

        String ssid = wifiInfo.getSSID();
        if (ssid != null) {
            ssid = ssid.replaceAll("\"", "");
            if ("<unknown ssid>".equals(ssid)) {
                ssid = null;
            }
        }
        returnData.put("wifiName", ssid);
        Log.d(TAG, "WiFi SSID: " + ssid);

        String bssid = wifiInfo.getBSSID();
        returnData.put("bssid", bssid);
        Log.d(TAG, "BSSID: " + bssid);

        int ipInt = wifiInfo.getIpAddress();
        String ip = (ipInt != 0) ? String.format("%d.%d.%d.%d",
                (ipInt & 0xff), (ipInt >> 8 & 0xff), (ipInt >> 16 & 0xff), (ipInt >> 24 & 0xff)) : null;
        returnData.put("ip", ip);
        Log.d(TAG, "Device IP: " + ip);

        return returnData;
    }

    private WifiInfo getWifiInfo() {
        return (wifiManager != null) ? wifiManager.getConnectionInfo() : null;
    }
}
