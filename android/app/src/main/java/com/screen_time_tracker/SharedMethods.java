package com.screen_time_tracker;

import android.app.usage.UsageStatsManager;
import android.app.usage.UsageEvents;
import android.content.Context;
import android.content.ContentResolver;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.net.Uri;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Base64;
import android.util.Log;
import androidx.documentfile.provider.DocumentFile;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.io.ByteArrayOutputStream;
import java.util.Calendar;


public class SharedMethods {
    private static final String APP_PRIVATE_FILE = "data.json";
    private static final String TAG = "SharedMethods";

    public static Bitmap drawableToBitmap(Drawable drawable) {
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

    public static void syncData(Context applicationContext) {
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
            JSONObject apps = jsonData.getJSONObject("apps");
            JSONObject screenTimeData = jsonData.getJSONObject("screenTimeData");
            Long startTime = screenTimeData.getLong("lastSyncTime");
            Calendar calendar = Calendar.getInstance();
            Long endTime = calendar.getTimeInMillis();
            Long monthInMillis = 30L*24*60*60*1000;
            if ((endTime - startTime) > monthInMillis) {
                startTime = endTime - monthInMillis; // If last sync time is more than 30 days ago, then sync only for the past 30 days.
            }
            UsageStatsManager usageStatsManager = (UsageStatsManager) applicationContext.getSystemService(Context.USAGE_STATS_SERVICE);
            while (true) {
                calendar.setTimeInMillis(startTime);

                // Creating JSON objects if not present for the dates
                String currentYear = String.valueOf(calendar.get(Calendar.YEAR));
                if (!screenTimeData.has(currentYear)) {
                    screenTimeData.put(currentYear, new JSONObject());
                }
                JSONObject yearData = (JSONObject) screenTimeData.get(currentYear);
                String currentMonth = String.valueOf(calendar.get(Calendar.MONTH)+1);
                if (!yearData.has(currentMonth)) {
                    yearData.put(currentMonth, new JSONObject());
                }
                JSONObject monthData = (JSONObject) yearData.get(currentMonth);
                String currentDay = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
                if (!monthData.has(currentDay)) {
                    monthData.put(currentDay, new JSONObject());
                }
                JSONObject dayData = (JSONObject) monthData.get(currentDay);

                calendar.add(Calendar.DAY_OF_YEAR, 1);  // Move to the next day
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                Long endOfDay = calendar.getTimeInMillis();
                if (endTime <= endOfDay) {
                    Map<String, Long> usageStatsMap = getUsageStatsMap(usageStatsManager,startTime,endTime);
                    for (String packageName : usageStatsMap.keySet()) {
                        if (!apps.has(packageName)) {
                            JSONObject appJson = getAppJson(packageName, applicationContext);
                            apps.put(packageName,appJson);
                        }
                        Long appScreenTime = 0L;
                        if (dayData.has(packageName)) {
                            appScreenTime = ((Integer) dayData.get(packageName)).longValue();
                        }
                        appScreenTime += usageStatsMap.get(packageName);
                        dayData.put(packageName, appScreenTime);
                    }
                    break;
                } else {
                    Map<String, Long> usageStatsMap = getUsageStatsMap(usageStatsManager,startTime,endOfDay);
                    for (String packageName : usageStatsMap.keySet()) {
                        if (!apps.has(packageName)) {
                            JSONObject appJson = getAppJson(packageName, applicationContext);
                            apps.put(packageName,appJson);
                        }
                        Long appScreenTime = 0L;
                        if (dayData.has(packageName)) {
                            appScreenTime = ((Integer) dayData.get(packageName)).longValue();
                        }
                        appScreenTime += usageStatsMap.get(packageName);
                        dayData.put(packageName, appScreenTime);
                    }
                    startTime = endOfDay;
                }
            }
            screenTimeData.put("lastSyncTime", endTime);
            String fileContents = jsonData.toString(2);
            FileWriter writer = new FileWriter(file);
            writer.write(fileContents);
            writer.close();
        } catch (JSONException e) {
            Log.e(TAG, "Error handling JSON for private file", e);
        } catch (IOException e) {
            Log.e(TAG, "Error reading/writing private file", e);
        }
    }
    
    public static Map<String, Long> getUsageStatsMap(UsageStatsManager usageStatsManager, Long startTime, Long endTime) {
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

    public static JSONObject getAppJson(String packageName, Context applicationContext) throws JSONException {
        JSONObject appJson = new JSONObject();
        PackageManager packageManager = applicationContext.getPackageManager();
        try {
            ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, 0);
            String appName = packageManager.getApplicationLabel(appInfo).toString();
            appJson.put("appName", appName);
            Drawable drawable = packageManager.getApplicationIcon(appInfo);
            String iconBase64 = getIconBase64(drawable);
            appJson.put("appIconBase64", iconBase64);
        } catch (PackageManager.NameNotFoundException e) {
            appJson.put("appName", packageName);
            Drawable drawable = packageManager.getDefaultActivityIcon();
            String iconBase64 = getIconBase64(drawable);
            appJson.put("appIconBase64", iconBase64);
        } catch (JSONException e) {
            Log.e(TAG, "Error handling JSON for private file", e);
        }
        return appJson;
    }

    public static String getIconBase64(Drawable drawable) {     
        Bitmap bitmap = drawableToBitmap(drawable);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String iconBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT);
        return iconBase64;
    }

    public static JSONObject exportFile(String publicFolderUriString, String publicFileName, Context applicationContext) throws JSONException {
        syncData(applicationContext);
        File privateFile = new File(applicationContext.getFilesDir(), APP_PRIVATE_FILE);
        Uri publicFolderUri = Uri.parse(publicFolderUriString);
        ContentResolver resolver = applicationContext.getContentResolver();
        DocumentFile publicDir = DocumentFile.fromTreeUri(applicationContext, publicFolderUri);
        DocumentFile publicFile = null;
        JSONObject result = new JSONObject();

        if (!privateFile.exists()) {
            result.put("success", false);
            result.put("errorCode", "FILE_NOT_FOUND");
            result.put("errorMessage", "Private file does not exist: " + privateFile.getAbsolutePath());
            return result;
        }

        if (publicDir == null || !publicDir.exists() || !publicDir.isDirectory() || !publicDir.canWrite()) {
            result.put("success", false);
            result.put("errorCode", "DIRECTORY_ERROR");
            result.put("errorMessage", "Invalid public directory Uri or not writable: " + publicFolderUriString);
            return result;
        }

        // Check if the file exists
        publicFile = publicDir.findFile(publicFileName+".json");

        // If the file doesn't exist, create it
        if (publicFile == null) {
            publicFile = publicDir.createFile("application/json", publicFileName);
            if (publicFile == null) {
                result.put("success", false);
                result.put("errorCode", "FILE_CREATION_FAILED");
                result.put("errorMessage", "Failed to create public file: " + publicFileName + " in " + publicFolderUriString);
                return result;
            }
        } else if (!publicFile.isFile() || !publicFile.canWrite()) {
            result.put("success", false);
            result.put("errorCode", "FILE_ERROR");
            result.put("errorMessage", "Existing public file is not a writable file: " + publicFileName + " in " + publicFolderUriString);            
            return result;
        }

        try (FileInputStream fis = new FileInputStream(privateFile);
             ParcelFileDescriptor pfd = resolver.openFileDescriptor(publicFile.getUri(), "w");
             FileOutputStream fos = new FileOutputStream(pfd.getFileDescriptor())) {

            byte[] buffer = new byte[1024];
            int read;
            while ((read = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, read);
            }

            result.put("success", true);
            result.put("filePath", publicFile.getUri().toString());            

        } catch (IOException e) {
            Log.e(TAG, "Error transferring data to public file", e);
            result.put("success", false);
            result.put("errorCode", "IO_ERROR");
            result.put("errorMessage", "Error transferring data to public file: " + e.getMessage());
        }
        return result;
    }
}