package com.screen_time_tracker;

import android.content.Context;
import android.util.Log;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class SyncWorker extends Worker {

    private static final String TAG = "SyncWorker";

    public SyncWorker(Context context, WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @Override
    public Result doWork() {
        Log.d(TAG, "Started daily sync");
        Context applicationContext = getApplicationContext();
        SharedMethods.syncData(applicationContext);
        Log.d(TAG, "Completed daily sync");
        return Result.success();
    }
}