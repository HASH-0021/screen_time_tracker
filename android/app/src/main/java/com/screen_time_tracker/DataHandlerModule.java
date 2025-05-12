package com.screen_time_tracker;

import android.content.Context;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;
import androidx.documentfile.provider.DocumentFile;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableArray;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.lang.NumberFormatException;
import java.lang.UnsupportedOperationException;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Iterator;

public class DataHandlerModule extends ReactContextBaseJavaModule {

    private static final String TAG = "DataHandlerModule";
    private final ReactApplicationContext reactContext;
    private final Context applicationContext;
    private static final String APP_PRIVATE_FILE = "data.json";

    DataHandlerModule(ReactApplicationContext context) {
        super(context);
        reactContext = context;
        applicationContext = context.getApplicationContext();
    }

    @Override
    public String getName() {
        return "DataHandlerModule";
    }

    private JSONObject generatePrivateFileData() throws JSONException {
        JSONObject data = new JSONObject();
        JSONObject settings = new JSONObject();
        JSONObject apps = new JSONObject();
        JSONObject screenTimeData = new JSONObject();
        settings.put("isDailyTotalGoalEnabled", false);
        settings.put("dailyTotalGoalGoodUsage", 4*60*60*1000);
        settings.put("dailyTotalGoalBadUsage", 6*60*60*1000);
        settings.put("isDailyAppGoalEnabled", false);
        settings.put("dailyAppGoalGoodUsage", 30*60*1000);
        settings.put("dailyAppGoalBadUsage", 90*60*1000);
        settings.put("theme", "automatic");
        data.put("settings", settings);
        PackageManager packageManager = applicationContext.getPackageManager();
        Drawable drawable = packageManager.getDefaultActivityIcon();   
        Bitmap bitmap = SharedMethods.drawableToBitmap(drawable);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String iconBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT);
        apps.put("default", iconBase64);
        data.put("apps", apps);
        Calendar calendar = Calendar.getInstance();
        Long currentTimeMillis = calendar.getTimeInMillis();
        screenTimeData.put("lastSyncTime", currentTimeMillis);
        data.put("screenTimeData",screenTimeData);
        return data;
    }

    @ReactMethod
    public void setTheme(String theme, Promise promise) {
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
            settings.put("theme", theme);
            String fileContents = jsonData.toString(2);
            FileWriter writer = new FileWriter(file);
            writer.write(fileContents);
            writer.close();
            settings = jsonData.getJSONObject("settings");
            WritableMap settingsMap = recursivelyConvertToWritableMap(settings);
            promise.resolve(settingsMap);
        } catch (JSONException e) {
            Log.e(TAG, "Error handling JSON for private file", e);
            promise.reject("JSON_ERROR", "Error handling JSON for private file: " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "Error reading/writing private file", e);
            promise.reject("IO_ERROR", "Error reading/writing private file: " + e.getMessage());
        }
    }

    @ReactMethod
    public void setDailyTotalGoal(int goodUsage, int badUsage, Promise promise) {
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
            settings.put("isDailyTotalGoalEnabled", true);
            settings.put("dailyTotalGoalGoodUsage", goodUsage);
            settings.put("dailyTotalGoalBadUsage", badUsage);
            String fileContents = jsonData.toString(2);
            FileWriter writer = new FileWriter(file);
            writer.write(fileContents);
            writer.close();
            settings = jsonData.getJSONObject("settings");
            WritableMap settingsMap = recursivelyConvertToWritableMap(settings);
            promise.resolve(settingsMap);
        } catch (JSONException e) {
            Log.e(TAG, "Error handling JSON for private file", e);
            promise.reject("JSON_ERROR", "Error handling JSON for private file: " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "Error reading/writing private file", e);
            promise.reject("IO_ERROR", "Error reading/writing private file: " + e.getMessage());
        }
    }

    @ReactMethod
    public void removeDailyTotalGoal(Promise promise) {
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
            settings.put("isDailyTotalGoalEnabled", false);            
            String fileContents = jsonData.toString(2);
            FileWriter writer = new FileWriter(file);
            writer.write(fileContents);
            writer.close();
            settings = jsonData.getJSONObject("settings");
            WritableMap settingsMap = recursivelyConvertToWritableMap(settings);
            promise.resolve(settingsMap);
        } catch (JSONException e) {
            Log.e(TAG, "Error handling JSON for private file", e);
            promise.reject("JSON_ERROR", "Error handling JSON for private file: " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "Error reading/writing private file", e);
            promise.reject("IO_ERROR", "Error reading/writing private file: " + e.getMessage());
        }
    }

    @ReactMethod
    public void setDailyAppGoal(int goodUsage, int badUsage, Promise promise) {
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
            settings.put("isDailyAppGoalEnabled", true);
            settings.put("dailyAppGoalGoodUsage", goodUsage);
            settings.put("dailyAppGoalBadUsage", badUsage);
            String fileContents = jsonData.toString(2);
            FileWriter writer = new FileWriter(file);
            writer.write(fileContents);
            writer.close();
            settings = jsonData.getJSONObject("settings");
            WritableMap settingsMap = recursivelyConvertToWritableMap(settings);
            promise.resolve(settingsMap);
        } catch (JSONException e) {
            Log.e(TAG, "Error handling JSON for private file", e);
            promise.reject("JSON_ERROR", "Error handling JSON for private file: " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "Error reading/writing private file", e);
            promise.reject("IO_ERROR", "Error reading/writing private file: " + e.getMessage());
        }
    }

    @ReactMethod
    public void removeDailyAppGoal(Promise promise) {
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
            settings.put("isDailyAppGoalEnabled", false);            
            String fileContents = jsonData.toString(2);
            FileWriter writer = new FileWriter(file);
            writer.write(fileContents);
            writer.close();
            settings = jsonData.getJSONObject("settings");
            WritableMap settingsMap = recursivelyConvertToWritableMap(settings);
            promise.resolve(settingsMap);
        } catch (JSONException e) {
            Log.e(TAG, "Error handling JSON for private file", e);
            promise.reject("JSON_ERROR", "Error handling JSON for private file: " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "Error reading/writing private file", e);
            promise.reject("IO_ERROR", "Error reading/writing private file: " + e.getMessage());
        }
    }

    @ReactMethod
    public void getSettingsData(Promise promise) {
        File file = new File(applicationContext.getFilesDir(), APP_PRIVATE_FILE);
        try {
            JSONObject jsonData;
            WritableMap settingsMap;

            if (file.exists()) {
                // Read existing data
                FileInputStream fis = new FileInputStream(file);
                int size = (int) file.length();
                byte[] buffer = new byte[size];
                fis.read(buffer);
                fis.close();
                String existingContent = new String(buffer, StandardCharsets.UTF_8);
                jsonData = new JSONObject(existingContent);
                JSONObject settings = jsonData.getJSONObject("settings");
                settingsMap = recursivelyConvertToWritableMap(settings);                

            } else {
                // Create the file with initial data
                jsonData = generatePrivateFileData();
                String fileContents = jsonData.toString(2);
                FileWriter writer = new FileWriter(file);
                writer.write(fileContents);
                writer.close();
                JSONObject settings = jsonData.getJSONObject("settings");
                settingsMap = recursivelyConvertToWritableMap(settings);
            }
            promise.resolve(settingsMap);

        } catch (JSONException e) {
            Log.e(TAG, "Error handling JSON for private file", e);
            promise.reject("JSON_ERROR", "Error handling JSON for private file: " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "Error reading/writing private file", e);
            promise.reject("IO_ERROR", "Error reading/writing private file: " + e.getMessage());
        }
    }

    @ReactMethod
    public void getScreenTimeData(Promise promise) {
        File file = new File(applicationContext.getFilesDir(), APP_PRIVATE_FILE);
        try {
            JSONObject jsonData;
            WritableMap output = Arguments.createMap();
            // Read existing data
            FileInputStream fis = new FileInputStream(file);
            int size = (int) file.length();
            byte[] buffer = new byte[size];
            fis.read(buffer);
            fis.close();
            String existingContent = new String(buffer, StandardCharsets.UTF_8);
            jsonData = new JSONObject(existingContent);
            JSONObject screenTimeData = jsonData.getJSONObject("screenTimeData");
            WritableMap screenTimeDataMap = recursivelyConvertToWritableMap(screenTimeData);
            JSONObject apps = jsonData.getJSONObject("apps");
            WritableMap appsMap = recursivelyConvertToWritableMap(apps);
            output.putMap("data", screenTimeDataMap);
            output.putMap("app", appsMap);
            promise.resolve(output);
        } catch (JSONException e) {
            Log.e(TAG, "Error handling JSON for private file", e);
            promise.reject("JSON_ERROR", "Error handling JSON for private file: " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "Error reading/writing private file", e);
            promise.reject("IO_ERROR", "Error reading/writing private file: " + e.getMessage());
        }
    }

    private WritableMap recursivelyConvertToWritableMap(JSONObject jsonObject) throws JSONException {
        WritableMap map = Arguments.createMap();
        Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            Object value = jsonObject.get(key);
            if (value instanceof JSONObject) {
                map.putMap(key, recursivelyConvertToWritableMap((JSONObject) value));
            } else if (value instanceof JSONArray) {
                map.putArray(key, recursivelyConvertToWritableArray((JSONArray) value));
            } else if (value instanceof String) {
                map.putString(key, (String) value);
            } else if (value instanceof Integer) {
                map.putInt(key, (Integer) value);
            } else if (value instanceof Boolean) {
                map.putBoolean(key, (Boolean) value);
            } else if (value instanceof Double) {
                map.putDouble(key, (Double) value);
            } else if (value instanceof Long) {
                map.putDouble(key, (Long) value);
            } else if (value == JSONObject.NULL) {
                map.putNull(key);
            }
        }
        return map;
    }

    private WritableArray recursivelyConvertToWritableArray(JSONArray jsonArray) throws JSONException {
        WritableArray array = Arguments.createArray();
        for (int i = 0; i < jsonArray.length(); i++) {
            Object value = jsonArray.get(i);
            if (value instanceof JSONObject) {
                array.pushMap(recursivelyConvertToWritableMap((JSONObject) value));
            } else if (value instanceof JSONArray) {
                array.pushArray(recursivelyConvertToWritableArray((JSONArray) value));
            } else if (value instanceof String) {
                array.pushString((String) value);
            } else if (value instanceof Integer) {
                array.pushInt((Integer) value);
            } else if (value instanceof Boolean) {
                array.pushBoolean((Boolean) value);
            } else if (value instanceof Double) {
                array.pushDouble((Double) value);
            } else if (value instanceof Long) {
                array.pushDouble((Long) value);
            } else if (value == JSONObject.NULL) {
                array.pushNull();
            }
        }
        return array;
    }

    @ReactMethod
    public void exportToPublicFile(String publicFolderUriString, Promise promise) throws JSONException {
        String publicFileName = "screen_time_tracker_data";
        WritableMap result = Arguments.createMap();
        JSONObject resultJson = SharedMethods.exportFile(publicFolderUriString, "screen_time_tracker_data", applicationContext);
        if ((Boolean) resultJson.get("success")) {
            result.putBoolean("success", true);
            result.putString("filePath", (String) resultJson.get("filePath"));
            promise.resolve(result);
        } else {
            promise.reject((String) resultJson.get("errorCode"), (String) resultJson.get("errorMessage"));
        }

    }

    @ReactMethod
    public void importFromPublicFile(String publicFileUriString, Promise promise) {
        File privateFile = new File(applicationContext.getFilesDir(), APP_PRIVATE_FILE);
        Uri publicFileUri = Uri.parse(publicFileUriString);
        ContentResolver resolver = applicationContext.getContentResolver();

        try (ParcelFileDescriptor pfdRead = resolver.openFileDescriptor(publicFileUri, "r");
             FileInputStream fisPublic = new FileInputStream(pfdRead.getFileDescriptor());
             FileOutputStream fosPrivate = new FileOutputStream(privateFile)) {

            // Read content from public file
            int size = (int) pfdRead.getStatSize(); // Get approximate size
            byte[] buffer = new byte[size > 0 ? size : 8192];
            int bytesRead;
            StringBuilder publicFileContent = new StringBuilder();
            while ((bytesRead = fisPublic.read(buffer)) != -1) {
                publicFileContent.append(new String(buffer, 0, bytesRead, StandardCharsets.UTF_8));
            }

            // Parse public file content as JSONObject
            JSONObject publicJsonObject = new JSONObject(publicFileContent.toString());
            if (!validateFileJson(publicJsonObject)) {
                promise.reject("CORRUPT_FILE", "The imported file is corrupted.");
            }

            // Write the JSONObject to the private file (overwriting)
            String privateFileContent = publicJsonObject.toString(2); // Use indentation for readability
            FileWriter writer = new FileWriter(privateFile);
            writer.write(privateFileContent);
            writer.close();

            WritableMap result = Arguments.createMap();
            result.putBoolean("success", true);
            result.putString("filePath", privateFile.getAbsolutePath());
            promise.resolve(result);

        } catch (IOException e) {
            Log.e(TAG, "IO Error copying public to private file", e);
            promise.reject("IO_ERROR", "Error copying public to private file: " + e.getMessage());
        } catch (JSONException e) {
            Log.e(TAG, "JSON Error parsing public file content", e);
            promise.reject("JSON_ERROR", "Error parsing public file content: " + e.getMessage());
        }
    }

    // Validation methods

    private Boolean validateFileJson(JSONObject fileJson) {
        try {
            Iterator<String> keys = fileJson.keys();
            Integer count = 0;
            while (keys.hasNext()) {
                String key = keys.next();
                Object value = fileJson.get(key);
                if (key.equals("settings")) {
                    if (!(value instanceof JSONObject) || !validateSettingsJson((JSONObject) value)) {
                        Log.e(TAG, key + "issue");
                        return false;
                    }
                    count++;
                } else if (key.equals("apps")) {
                    if (!(value instanceof JSONObject) || !validateAppsJson((JSONObject) value)) {
                        Log.e(TAG, key + "issue");
                        return false;
                    }
                    count++;
                } else if (key.equals("screenTimeData")) {
                    if (!(value instanceof JSONObject) || !validateScreenTimeDataJson((JSONObject) value)) {
                        Log.e(TAG, key + "issue");
                        return false;
                    }
                    count++;
                } else {
                    Log.e(TAG, "File json extra key. Key : "+key);
                    return false;
                }
            }
            if (count < 3) {
                Log.e(TAG, "File json lesser keys");
                return false;
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSON Error parsing public file content", e);
            return false;
        }
        return true;
    }

    private Boolean validateSettingsJson(JSONObject settingsJson) {
        try {
            Iterator<String> keys = settingsJson.keys();
            Integer count = 0;
            while (keys.hasNext()) {
                String key = keys.next();
                Object value = settingsJson.get(key);
                if (key.equals("isDailyTotalGoalEnabled")) {
                    if (!(value instanceof Boolean)) {
                        Log.e(TAG, key + "issue");
                        return false;
                    }
                    count++;
                } else if (key.equals("dailyTotalGoalGoodUsage")) {
                    if (!(value instanceof Integer) || (Integer) value < 0 || (Integer) value >= 24*60*60*1000) {
                        Log.e(TAG, key + "issue");
                        return false;
                    }
                    count++;
                } else if (key.equals("dailyTotalGoalBadUsage")) {
                    if (!(value instanceof Integer)  || (Integer) value < 0 || (Integer) value >= 24*60*60*1000) {
                        Log.e(TAG, key + "issue");
                        return false;
                    }
                    count++;
                } else if (key.equals("isDailyAppGoalEnabled")) {
                    if (!(value instanceof Boolean)) {
                        Log.e(TAG, key + "issue");
                        return false;
                    }
                    count++;
                } else if (key.equals("dailyAppGoalGoodUsage")) {
                    if (!(value instanceof Integer) || (Integer) value < 0 || (Integer) value >= 24*60*60*1000) {
                        Log.e(TAG, key + "issue");
                        return false;
                    }
                    count++;
                } else if (key.equals("dailyAppGoalBadUsage")) {
                    if (!(value instanceof Integer) || (Integer) value < 0 || (Integer) value >= 24*60*60*1000) {
                        Log.e(TAG, key + "issue");
                        return false;
                    }
                    count++;
                } else if (key.equals("theme")) {
                    if (!(value instanceof String)) {
                        Log.e(TAG, key + "issue");
                        return false;
                    }
                    count++;
                } else {
                    return false;
                }            
            }
            if (count < 7) {
                Log.e(TAG, "Settings json lesser keys");
                return false;
            }
            if ((Integer) settingsJson.get("dailyTotalGoalGoodUsage") >= (Integer) settingsJson.get("dailyTotalGoalBadUsage")) {
                Log.e(TAG, "dtg issue");
                return false;
            }
            if ((Integer) settingsJson.get("dailyAppGoalGoodUsage") >= (Integer) settingsJson.get("dailyAppGoalBadUsage")) {
                Log.e(TAG, "dag issue");
                return false;
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSON Error parsing public file content", e);
            return false;
        }
        return true;
    }

    private Boolean validateAppsJson(JSONObject appsJson) {
        try {
            Iterator<String> keys = appsJson.keys();
            Integer count = 0;
            while (keys.hasNext()) {
                String key = keys.next();
                Object value = appsJson.get(key);
                if (key.equals("default")) {
                    if (!(value instanceof String)) {
                        return false;
                    }
                    count++;
                } else if (!(value instanceof JSONObject) || !validateAppJson((JSONObject) value)) {
                    Log.e(TAG, key + "issue");
                    return false;
                }
            }
            if (count != 1) {
                Log.e(TAG, "Apps json lesser keys");
                return false;
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSON Error parsing public file content", e);
            return false;
        }
        return true;
    }

    private Boolean validateAppJson(JSONObject appJson) {
        try {
            Iterator<String> keys = appJson.keys();
            Integer count = 0;
            while (keys.hasNext()) {
                String key = keys.next();
                if (key.equals("appName")) {
                    if (!(appJson.get(key) instanceof String)) {
                        Log.e(TAG, key + "issue");
                        return false;
                    }
                    count++;
                } else if (key.equals("appIconBase64")) {
                    if (!(appJson.get(key) instanceof String)) {
                        Log.e(TAG, key + "issue");
                        return false;
                    }
                    count++;
                } else {
                    return false;
                }
            }
            if (count < 2) {
                Log.e(TAG, "App json lesser keys");
                return false;
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSON Error parsing public file content", e);
            return false;
        }
        return true;
    }

    private Boolean validateScreenTimeDataJson(JSONObject screenTimeDataJson) {
        try {
            Iterator<String> keys = screenTimeDataJson.keys();
            Integer count = 0;
            while (keys.hasNext()) {
                String key = keys.next();
                Object value = screenTimeDataJson.get(key);
                if (key.equals("lastSyncTime")) {
                    if (!(value instanceof Long) || (Long) value < 0) {
                        Log.e(TAG, key + "issue");
                        return false;
                    }
                    count++;
                } else if (2000 > Integer.parseInt(key) || !(value instanceof JSONObject) || !validateMonthsJson((JSONObject) value)) {
                    Log.e(TAG, key + "issue");
                    return false;
                }
            }
            if (count != 1) {
                Log.e(TAG, "Screen time data json lesser keys");
                return false;
            }
        } catch (NumberFormatException e) {
            return false;
        } catch (JSONException e) {
            Log.e(TAG, "JSON Error parsing public file content", e);
            return false;
        }
        return true;
    }

    private Boolean validateMonthsJson(JSONObject monthsJson) {
        try {
            Iterator<String> keys = monthsJson.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                Object value = monthsJson.get(key);
                if (Integer.parseInt(key) < 0 || Integer.parseInt(key) > 12 || !(value instanceof JSONObject) || !validateDaysJson((JSONObject) value)) {
                    Log.e(TAG, key + "issue");
                    return false;
                }
            }
        } catch (NumberFormatException e) {
            return false;
        } catch (JSONException e) {
            Log.e(TAG, "JSON Error parsing public file content", e);
            return false;
        }
        return true;
    }

    private Boolean validateDaysJson(JSONObject daysJson) {
        try {
            Iterator<String> keys = daysJson.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                Object value = daysJson.get(key);
                if (Integer.parseInt(key) < 0 || Integer.parseInt(key) > 31 || !(value instanceof JSONObject) || !validateDailyScreenTimeJson((JSONObject) value)) {
                    Log.e(TAG, key + "issue");
                    return false;
                }
            }
        } catch (NumberFormatException e) {
            return false;
        } catch (JSONException e) {
            Log.e(TAG, "JSON Error parsing public file content", e);
            return false;
        }
        return true;
    }

    private Boolean validateDailyScreenTimeJson(JSONObject dailyScreenTimeJson) {
        try {
            Iterator<String> keys = dailyScreenTimeJson.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                Object value = dailyScreenTimeJson.get(key);
                if (!(value instanceof Integer) || (Integer) value < 0) {
                    Log.e(TAG, key + "issue");
                    return false;
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSON Error parsing public file content", e);
            return false;
        }
        return true;
    }
}