package com.screen_time_tracker;

import androidx.annotation.NonNull;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainPackage implements ReactPackage {

    @NonNull
    @Override
    public List<ViewManager> createViewManagers(@NonNull ReactApplicationContext reactContext) {
        return Collections.emptyList();
    }
    
    @NonNull
    @Override
    public List<NativeModule> createNativeModules(@NonNull ReactApplicationContext reactContext) {
        List<NativeModule> modules = new ArrayList<>();
        modules.add(new ForegroundUsageModule(reactContext));
        modules.add(new NotificationModule(reactContext));
        modules.add(new SynchronizationModule(reactContext));
        modules.add(new DataHandlerModule(reactContext));
        modules.add(new SyncScheduleModule(reactContext));
        return modules;
    }

}