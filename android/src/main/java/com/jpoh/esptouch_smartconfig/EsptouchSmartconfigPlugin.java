package com.jpoh.esptouch_smartconfig;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import androidx.annotation.NonNull;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodChannel;

public class EsptouchSmartconfigPlugin implements FlutterPlugin {
    private static final String TAG = "EsptouchSmartconfig";
    private static final String EVENT_CHANNEL_NAME = "esptouch_smartconfig/result";
    private static final String METHOD_CHANNEL_NAME = "esptouch_smartconfig";

    private EventChannel eventChannel;
    private MethodChannel methodChannel;
    private FlutterEventChannelHandler eventChannelHandler;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
        Log.d(TAG, "Plugin attached to engine");
        setupChannels(binding.getBinaryMessenger(), binding.getApplicationContext());
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        Log.d(TAG, "Plugin detached from engine");

        if (methodChannel != null) {
            methodChannel.setMethodCallHandler(null);
            methodChannel = null;
        }

        if (eventChannel != null) {
            eventChannel.setStreamHandler(null);
            eventChannel = null;
        }

        eventChannelHandler = null;
    }

    private void setupChannels(BinaryMessenger messenger, Context context) {
        methodChannel = new MethodChannel(messenger, METHOD_CHANNEL_NAME);
        eventChannel = new EventChannel(messenger, EVENT_CHANNEL_NAME);

        // Initialize event channel handler
        eventChannelHandler = new FlutterEventChannelHandler(context, eventChannel);

        // Initialize WifiManager
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfoData wifiInfoFlutter = new WifiInfoData(wifiManager, context);

        // Initialize method channel handler
        FlutterMethodChannelHandler methodChannelHandler = new FlutterMethodChannelHandler(wifiInfoFlutter);
        methodChannel.setMethodCallHandler(methodChannelHandler);
    }
}
