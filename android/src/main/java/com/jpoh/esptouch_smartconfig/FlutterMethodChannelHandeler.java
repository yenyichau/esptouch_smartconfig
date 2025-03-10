package com.jpoh.esptouch_smartconfig;

import androidx.annotation.NonNull;
import android.net.wifi.WifiInfo;
import android.util.Log;
import java.util.HashMap;
import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/**
 * Handles method calls from Flutter via MethodChannel
 */
class FlutterMethodChannelHandler implements MethodCallHandler {
    private static final String TAG = "esptouch_smartconfig";
    private final WifiInfoData wifiInfoFlutter;

    FlutterMethodChannelHandler(WifiInfoData wifiInfoFlutter) {
        this.wifiInfoFlutter = wifiInfoFlutter;
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        if ("getWifiData".equals(call.method)) {
            result.success(wifiInfoFlutter.getWifiData());
        } else {
            result.notImplemented();
        }
    }
}
