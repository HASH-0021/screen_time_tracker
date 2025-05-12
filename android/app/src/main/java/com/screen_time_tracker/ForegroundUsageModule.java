package com.screen_time_tracker;

import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.app.usage.UsageEvents;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.Calendar;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.io.ByteArrayOutputStream;

public class ForegroundUsageModule extends ReactContextBaseJavaModule {

    private static final String TAG = "ForegroundUsageModule";

    private final Context applicationContext;

    ForegroundUsageModule(ReactApplicationContext context) {
        super(context);
        applicationContext = context.getApplicationContext();
    }

    @NonNull
    @Override
    public String getName() {
        return "ForegroundUsageModule";
    }

    @com.facebook.react.bridge.ReactMethod
    public void getForegroundUsage(Promise promise) {
        try{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                UsageStatsManager usageStatsManager = (UsageStatsManager) applicationContext.getSystemService(Context.USAGE_STATS_SERVICE);
                if (usageStatsManager == null) {
                    promise.reject("USAGE_STATS_SERVICE_NULL", "UsageStatsManager is null.");
                    return;
                }

                Map<String, Long> usageStatsMap = getUsageStatsMap(usageStatsManager);
                
                WritableArray output = getOutput(usageStatsMap);

                promise.resolve(output);
            } else {
                promise.reject("API_LEVEL_TOO_LOW", "Usage stats are available on and above android version lollipop.");
            }
        } catch(Exception e){
            promise.reject("GET_USAGE_ERROR", "Error getting foreground usage: " + e.toString());
        }
    }

    @com.facebook.react.bridge.ReactMethod
    public void hasUsageAccessPermission(Promise promise) {
        try{
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP){
                AppOpsManager appOps = (AppOpsManager) applicationContext.getSystemService(Context.APP_OPS_SERVICE);
                if (appOps == null) {
                    promise.reject("APP_OPS_SERVICE_NULL", "AppOpsManager is null.");
                }
                assert appOps != null;
                int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), applicationContext.getPackageName());
                promise.resolve(mode == AppOpsManager.MODE_ALLOWED);
            } else {
                promise.reject("API_LEVEL_TOO_LOW", "Usage stats are available on and above android version lollipop.");
            }
        } catch(Exception e){
            promise.reject("USAGE_ACCESS_PERMISSION_ERROR", "Error checking permission: " + e.toString());
        }
    }

    @com.facebook.react.bridge.ReactMethod
    public void openUsageAccessSettings(Promise promise) {
        try{
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP){
                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                applicationContext.startActivity(intent);
                promise.resolve(true);
            } else {
                promise.reject("API_LEVEL_TOO_LOW", "Usage stats are available on and above android version lollipop.");
            }
        } catch(Exception e){
            promise.reject("USAGE_ACCESS_SETTINGS_INTENT_FAILED", "Error opening settings: " + e.toString());
        }
    }

    public Map<String, Long> getUsageStatsMap(UsageStatsManager usageStatsManager) {
        Calendar calendar = Calendar.getInstance();
        Long endTime = calendar.getTimeInMillis();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Long startTime = calendar.getTimeInMillis();
        Log.d(TAG, startTime+" - "+endTime);
        Log.d(TAG, String.valueOf((endTime - startTime)/1000));

        UsageEvents events = usageStatsManager.queryEvents(startTime, endTime);
        List<UsageEvents.Event> eventList = new ArrayList<>();

        while (events.hasNextEvent()) {
            UsageEvents.Event event = new UsageEvents.Event();
            events.getNextEvent(event);
            eventList.add(event);
        }

        Map<String, Long> usageStatsMap = new HashMap<>();
        Map<String, Long> foregroundEvents = new HashMap<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            for (UsageEvents.Event event : eventList) {
                if (event.getEventType() == UsageEvents.Event.ACTIVITY_RESUMED) {
                    foregroundEvents.put(event.getPackageName(), event.getTimeStamp());
                }
                if (event.getEventType() == UsageEvents.Event.ACTIVITY_PAUSED) {
                    Long eventStartTime = foregroundEvents.remove(event.getPackageName());
                    if (eventStartTime == null) {
                        eventStartTime = startTime;
                    }
                    Long duration = event.getTimeStamp() - eventStartTime;
                    usageStatsMap.put(event.getPackageName(), usageStatsMap.getOrDefault(event.getPackageName(), 0L) + duration);
                }
            }
        } else {
            for (UsageEvents.Event event : eventList) {
                if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                    foregroundEvents.put(event.getPackageName(), event.getTimeStamp());
                }
                if (event.getEventType() == UsageEvents.Event.MOVE_TO_BACKGROUND) {
                    Long eventStartTime = foregroundEvents.remove(event.getPackageName());
                    if (eventStartTime == null) {
                        eventStartTime = startTime;
                    }
                    Long duration = event.getTimeStamp() - eventStartTime;
                    usageStatsMap.put(event.getPackageName(), usageStatsMap.getOrDefault(event.getPackageName(), 0L) + duration);
                }
            }
        }

        for (String packageName : foregroundEvents.keySet()) {
            Long duration = endTime - foregroundEvents.get(packageName);
            usageStatsMap.put(packageName, usageStatsMap.getOrDefault(packageName, 0L) + duration);
        }

        return usageStatsMap;
    }

    public WritableArray getOutput(Map<String, Long> usageStatsMap) {
        try {

            PackageManager packageManager = applicationContext.getPackageManager();

            WritableArray result = Arguments.createArray();
            
            for (String packageName : usageStatsMap.keySet()) {
                Long time = usageStatsMap.get(packageName);
                if (time > 0) {

                    WritableMap appUsage = Arguments.createMap();

                    appUsage.putString("packageName", packageName);

                    appUsage.putDouble("totalTimeInForeground", time);
                
                    ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, 0);
                    
                    String appName = packageManager.getApplicationLabel(appInfo).toString();
                    appUsage.putString("appName", appName);

                    Drawable drawable = packageManager.getApplicationIcon(appInfo);
                    Bitmap bitmap = SharedMethods.drawableToBitmap(drawable);
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                    byte[] byteArray = byteArrayOutputStream.toByteArray();
                    String iconBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT);
                    appUsage.putString("appIconBase64", iconBase64);
                    Log.d(TAG, appName+" : "+time);
                    
                    result.pushMap(appUsage);
                }
            }

            return result;
        } catch(PackageManager.NameNotFoundException e) {
            WritableArray result = Arguments.createArray();
            Log.e(TAG, e.toString());
            return result;
        }
    }

    public Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && drawable instanceof AdaptiveIconDrawable) {
            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } else {
            int width = drawable.getIntrinsicWidth();
            int height = drawable.getIntrinsicHeight();
            width = width > 0 ? width : 100;
            height = height > 0 ? height : 100;
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            paint.setColor(Color.LTGRAY);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), paint);
            return bitmap;
        }
    }
}