package com.screen_time_tracker;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.io.ByteArrayOutputStream;
import java.util.Calendar;

import android.app.usage.UsageStatsManager;
import android.app.usage.UsageEvents;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;

public class SynchronizationModule extends ReactContextBaseJavaModule {
    private static final String TAG = "SynchronizationModule";
    private static final String APP_PRIVATE_FILE = "data.json";

    private final ReactApplicationContext reactContext;
    private final Context applicationContext;

    SynchronizationModule(ReactApplicationContext context) {
        super(context);
        reactContext = context;
        applicationContext = context.getApplicationContext();
    }

    @NonNull
    @Override
    public String getName() {
        return "SynchronizationModule";
    }

    @ReactMethod
    public void getLastSyncTime(Promise promise) {
        File file = new File(applicationContext.getFilesDir(), APP_PRIVATE_FILE);
        try {
            JSONObject jsonData;
            FileInputStream fis = new FileInputStream(file);
            int size = (int) file.length();
            byte[] buffer = new byte[size];
            fis.read(buffer);
            fis.close();
            String existingContent = new String(buffer, StandardCharsets.UTF_8);
            jsonData = new JSONObject(existingContent);
            JSONObject screenTimeData = jsonData.getJSONObject("screenTimeData");
            Long lastSyncTime = screenTimeData.getLong("lastSyncTime");
            promise.resolve(String.valueOf(lastSyncTime));
        } catch (JSONException e) {
            Log.e(TAG, "Error handling JSON for private file", e);
            promise.reject("JSON_ERROR", "Error handling JSON for private file: " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "Error reading/writing private file", e);
            promise.reject("IO_ERROR", "Error reading/writing private file: " + e.getMessage());
        }
    }

    @ReactMethod
    public void synchronizeData() {
        SharedMethods.syncData(applicationContext);
    }
}

