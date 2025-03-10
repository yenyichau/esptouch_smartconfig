package com.jpoh.esptouch_smartconfig;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.Map;

import io.flutter.plugin.common.EventChannel;

public class FlutterEventChannelHandler implements EventChannel.StreamHandler {
    private static final String TAG = "EsptouchPlugin";
    private static final String CHANNEL_NAME = "esptouch_smartconfig/result";

    private final Context context;
    private final EventChannel eventChannel;
    private MainThreadEventSink eventSink;
    private EsptouchAsyncTask esptouchAsyncTask;

    public FlutterEventChannelHandler(@NonNull Context context, @NonNull EventChannel eventChannel) {
        this.context = context;
        this.eventChannel = eventChannel;
        eventChannel.setStreamHandler(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onListen(Object arguments, EventChannel.EventSink eventSink) {
        Log.d(TAG, "Event Listener triggered");

        if (!(arguments instanceof Map)) {
            Log.e(TAG, "Invalid arguments received in onListen");
            return;
        }

        Map<String, Object> map = (Map<String, Object>) arguments;

        String ssid = safeGetString(map, "ssid");
        String bssid = safeGetString(map, "bssid");
        String password = safeGetString(map, "password");
        String deviceCount = safeGetString(map, "deviceCount");
        String broadcast = safeGetString(map, "isBroad");

        if (ssid == null || bssid == null || password == null) {
            Log.e(TAG, "Missing required parameters for ESPTouch configuration");
            return;
        }

        Log.d(TAG, String.format("Received configuration: SSID: %s, BSSID: %s, Password: %s, DeviceCount: %s, Broadcast: %s",
                ssid, bssid, password, deviceCount, broadcast));

        this.eventSink = new MainThreadEventSink(eventSink);
        esptouchAsyncTask = new EsptouchAsyncTask(context, this.eventSink);
        esptouchAsyncTask.execute(ssid, bssid, password, deviceCount, broadcast);
    }

    @Override
    public void onCancel(Object arguments) {
        Log.d(TAG, "Cancelling ESPTouch task");

        if (esptouchAsyncTask != null) {
            esptouchAsyncTask.cancelEsptouch();
            esptouchAsyncTask = null;
        }
    }

    /**
     * Safely retrieves a String value from a Map, returning null if not present.
     */
    private String safeGetString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value instanceof String ? (String) value : null;
    }
}
