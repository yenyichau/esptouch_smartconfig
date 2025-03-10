package com.jpoh.esptouch_smartconfig;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.espressif.iot.esptouch.EsptouchTask;
import com.espressif.iot.esptouch.IEsptouchListener;
import com.espressif.iot.esptouch.IEsptouchResult;
import com.espressif.iot.esptouch.IEsptouchTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EsptouchAsyncTask extends AsyncTask<String, IEsptouchResult, List<IEsptouchResult>> {
    private static final String TAG = "EspTouchAsyncTask";
    private final Object mLock = new Object();
    private final Context context;
    private IEsptouchTask mEsptouchTask;
    private final MainThreadEventSink eventSink;

    public EsptouchAsyncTask(Context context, MainThreadEventSink eventSink) {
        this.context = context;
        this.eventSink = eventSink;
    }

    public void cancelEsptouch() {
        cancel(true);
        if (mEsptouchTask != null) {
            mEsptouchTask.interrupt();
        }
    }

    @Override
    protected List<IEsptouchResult> doInBackground(String... params) {
        if (params.length < 5) {
            Log.e(TAG, "Invalid parameters passed to EsptouchAsyncTask");
            return null;
        }

        synchronized (mLock) {
            String apSsid = params[0];
            String apBssid = params[1];
            String apPassword = params[2];
            String deviceCountData = params[3];
            String broadcastData = params[4];

            Log.d(TAG, String.format("Received configuration: SSID: %s, BSSID: %s, Password: %s, Device Count: %s, Broadcast: %s",
                    apSsid, apBssid, apPassword, deviceCountData, broadcastData));

            boolean broadcast = "YES".equalsIgnoreCase(broadcastData);
            int taskResultCount = deviceCountData.isEmpty() ? -1 : Integer.parseInt(deviceCountData);

            mEsptouchTask = new EsptouchTask(apSsid, apBssid, apPassword, context);
            mEsptouchTask.setPackageBroadcast(broadcast);

            mEsptouchTask.setEsptouchListener(result -> publishProgress(result));

            return mEsptouchTask.executeForResults(taskResultCount);
        }
    }

    @Override
    protected void onProgressUpdate(IEsptouchResult... values) {
        if (values.length == 0 || values[0] == null) return;

        IEsptouchResult result = values[0];
        Log.d(TAG, "Progress Update: " + result);

        Map<String, String> sink = new HashMap<>();
        sink.put("bssid", result.getBssid());
        sink.put("ip", result.getInetAddress().getHostAddress());

        eventSink.success(sink);
    }

    @Override
    protected void onPostExecute(List<IEsptouchResult> result) {
        if (result == null || result.isEmpty()) {
            Log.e(TAG, "No results received from ESPTouch task.");
            eventSink.endOfStream();
            return;
        }

        IEsptouchResult firstResult = result.get(result.size() - 1);

        if (firstResult.isCancelled()) {
            Log.d(TAG, "ESPTouch task was cancelled.");
            eventSink.endOfStream();
            return;
        }

        if (!firstResult.isSuc()) {
            Log.d(TAG, "ESPTouch task failed.");
            eventSink.endOfStream();
            return;
        }

        Log.d(TAG, "ESPTouch task successful: " + firstResult);

        Map<String, String> sink = new HashMap<>();
        sink.put("bssid", firstResult.getBssid());
        sink.put("ip", firstResult.getInetAddress().getHostAddress());

        eventSink.success(sink);
        eventSink.endOfStream();
    }
}
