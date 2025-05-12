package com.screen_time_tracker;

import android.content.Context;
import android.util.Log;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class SyncScheduleModule extends ReactContextBaseJavaModule {

    private final Context applicationContext;
    private static final String TAG = "SyncScheduleModule";
    private static final String WORK_TAG_DAILY_SYNC = "dailySyncWork";

    SyncScheduleModule(ReactApplicationContext context) {
        super(context);
        applicationContext = context.getApplicationContext();
    }

    @Override
    public String getName() {
        return "SyncScheduleModule";
    }


    @ReactMethod
    public void scheduleDailySync() {
        Calendar now = Calendar.getInstance();
        Calendar targetTime = Calendar.getInstance();
        targetTime.set(Calendar.HOUR_OF_DAY, 0);
        targetTime.set(Calendar.MINUTE, 0);
        targetTime.set(Calendar.SECOND, 0);
        targetTime.set(Calendar.MILLISECOND, 0);

        if (targetTime.before(now)) {
            targetTime.add(Calendar.DAY_OF_YEAR, 1);
        }

        long initialDelay = targetTime.getTimeInMillis() - now.getTimeInMillis();

        PeriodicWorkRequest periodicWorkRequest = new PeriodicWorkRequest.Builder(
                SyncWorker.class,
                1, // Repeat interval
                TimeUnit.DAYS
        )
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .addTag(WORK_TAG_DAILY_SYNC)
                .build();

        WorkManager.getInstance(applicationContext)
                .enqueueUniquePeriodicWork(WORK_TAG_DAILY_SYNC, ExistingPeriodicWorkPolicy.REPLACE, periodicWorkRequest);

        Log.d(TAG, "Scheduled daily app sync.");
    }

    @ReactMethod
    public void cancelScheduledCallModuleB() {
        WorkManager.getInstance(applicationContext).cancelAllWorkByTag(WORK_TAG_DAILY_SYNC);
        Log.d(TAG, "Cancelled scheduled daily app sync.");
    }
}