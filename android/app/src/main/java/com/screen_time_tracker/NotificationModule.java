package com.screen_time_tracker;

import android.content.Intent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.app.Activity;
import android.Manifest;
import android.os.Build;
import android.os.PowerManager;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Promise;


public class NotificationModule extends ReactContextBaseJavaModule {

    private static final String TAG = "NotificationModule";
    public static final int REQUEST_POST_NOTIFICATIONS = 1001;
    private Promise postNotificationsPermissionPromise;
    private static final String APP_PRIVATE_FILE = "data.json";
    private final Context applicationContext;

    NotificationModule(ReactApplicationContext context) {
        super(context);
        applicationContext = context.getApplicationContext();
    }

    @NonNull
    @Override
    public String getName() {
        return "NotificationModule";
    }

    @ReactMethod
    public void startScreenTimeNotification() {
        Intent serviceIntent = new Intent(applicationContext, NotificationService.class);
        serviceIntent.setAction("START_SCREEN_TIME_NOTIFICATION");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            applicationContext.startForegroundService(serviceIntent);
        } else {
            applicationContext.startService(serviceIntent);
        }
        Log.d(TAG, "Screen time notification service started");
    }

    @ReactMethod
    public void stopScreenTimeNotification() {
        Intent serviceIntent = new Intent(applicationContext, NotificationService.class);
        serviceIntent.setAction("STOP_SCREEN_TIME_NOTIFICATION");
        applicationContext.startService(serviceIntent); // No need for startForegroundService here
        Log.d(TAG, "Screen time notification service stopped");
    }

    @ReactMethod
    public void isScreenTimeNotificationRunning(Promise promise) {
        promise.resolve(NotificationService.isScreenTimeNotificationRunning);
    }

    @ReactMethod
    public void startTotalGoalNotification() {
        Intent serviceIntent = new Intent(applicationContext, NotificationService.class);
        serviceIntent.setAction("START_TOTAL_GOAL_NOTIFICATION");

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
            JSONObject settings = jsonData.getJSONObject("settings");
            Integer goodGoal = (Integer) settings.get("dailyTotalGoalGoodUsage");
            Integer badGoal = (Integer) settings.get("dailyTotalGoalBadUsage");
            serviceIntent.putExtra("goodGoal", goodGoal);
            serviceIntent.putExtra("badGoal", badGoal);
        } catch (JSONException e) {
            Log.e(TAG, "Error handling JSON for private file", e);
        } catch (IOException e) {
            Log.e(TAG, "Error reading/writing private file", e);
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            applicationContext.startForegroundService(serviceIntent);
        } else {
            applicationContext.startService(serviceIntent);
        }
        Log.d(TAG, "Total goal notification service started");
    }

    @ReactMethod
    public void stopTotalGoalNotification() {
        Intent serviceIntent = new Intent(applicationContext, NotificationService.class);
        serviceIntent.setAction("STOP_TOTAL_GOAL_NOTIFICATION");
        applicationContext.startService(serviceIntent); // No need for startForegroundService here
        Log.d(TAG, "Total goal notification service stopped");
    }

    @ReactMethod
    public void isTotalGoalNotificationRunning(Promise promise) {
        promise.resolve(NotificationService.isTotalGoalNotificationRunning);
    }

    @ReactMethod
    public void startAppGoalNotification() {
        Intent serviceIntent = new Intent(applicationContext, NotificationService.class);
        serviceIntent.setAction("START_APP_GOAL_NOTIFICATION");
        
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
            JSONObject settings = jsonData.getJSONObject("settings");
            Integer goodGoal = (Integer) settings.get("dailyAppGoalGoodUsage");
            Integer badGoal = (Integer) settings.get("dailyAppGoalBadUsage");
            serviceIntent.putExtra("goodGoal", goodGoal);
            serviceIntent.putExtra("badGoal", badGoal);
        } catch (JSONException e) {
            Log.e(TAG, "Error handling JSON for private file", e);
        } catch (IOException e) {
            Log.e(TAG, "Error reading/writing private file", e);
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            applicationContext.startForegroundService(serviceIntent);
        } else {
            applicationContext.startService(serviceIntent);
        }
        Log.d(TAG, "App goal notification service started");
    }

    @ReactMethod
    public void stopAppGoalNotification() {
        Intent serviceIntent = new Intent(applicationContext, NotificationService.class);
        serviceIntent.setAction("STOP_APP_GOAL_NOTIFICATION");
        applicationContext.startService(serviceIntent); // No need for startForegroundService here
        Log.d(TAG, "App goal notification service stopped");
    }

    @ReactMethod
    public void isAppGoalNotificationRunning(Promise promise) {
        promise.resolve(NotificationService.isAppGoalNotificationRunning);
    }

    @ReactMethod
    public void updateTotalGoalTime(Integer goodGoal, Integer badGoal) {
        Intent serviceIntent = new Intent(applicationContext, NotificationService.class);
        serviceIntent.setAction("UPDATE_TOTAL_GOAL_TIME");
        serviceIntent.putExtra("goodGoal", goodGoal);
        serviceIntent.putExtra("badGoal", badGoal);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            applicationContext.startForegroundService(serviceIntent);
        } else {
            applicationContext.startService(serviceIntent);
        }
        Log.d(TAG, "Total goal time updated");
    }

    @ReactMethod
    public void updateAppGoalTime(Integer goodGoal, Integer badGoal) {
        Intent serviceIntent = new Intent(applicationContext, NotificationService.class);
        serviceIntent.setAction("UPDATE_APP_GOAL_TIME");
        serviceIntent.putExtra("goodGoal", goodGoal);
        serviceIntent.putExtra("badGoal", badGoal);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            applicationContext.startForegroundService(serviceIntent);
        } else {
            applicationContext.startService(serviceIntent);
        }
        Log.d(TAG, "App goal time updated");
    }

    @ReactMethod
    public void checkPostNotificationsPermission(Promise promise) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Activity currentActivity = getCurrentActivity();
            if (currentActivity != null) {
                if (ContextCompat.checkSelfPermission(currentActivity, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                    promise.resolve(true);
                } else {
                    promise.resolve(false);
                }
            } else {
                promise.reject("NO_ACTIVITY", "No current activity found.");
            }
        } else {
            promise.resolve(true);
        }
    }

    @ReactMethod
    public void openPostNotificationsSettings(Promise promise) {
        Activity currentActivity = getCurrentActivity();
        if (currentActivity != null) {
            Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, currentActivity.getPackageName());
            try {
                currentActivity.startActivity(intent);
                promise.resolve(true); // Indicate that settings were opened
            } catch (Exception e) {
                promise.reject("SETTINGS_ERROR", "Unable to open notification settings.");
            }
        } else {
            promise.reject("NO_ACTIVITY", "No current activity found.");
        }
    }

    @ReactMethod
    public void hasBackgroundUsagePermission(Promise promise) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager pm = (PowerManager) applicationContext.getSystemService(Context.POWER_SERVICE);
            if (pm != null && pm.isIgnoringBatteryOptimizations(applicationContext.getPackageName())) {
                promise.resolve(true);
            } else {
                promise.resolve(false);
            }
        } else {
            promise.resolve(true); // On older versions, background restrictions are different
        }
    }

    @ReactMethod
    public void openBackgroundUsageSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + applicationContext.getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            applicationContext.startActivity(intent);
        } else {
            // On older versions, there isn't a direct settings screen for this
            Log.e(TAG,"Background usage settings not directly available on this Android version.");
        }
    }
}