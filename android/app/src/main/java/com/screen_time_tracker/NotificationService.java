package com.screen_time_tracker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStatsManager;
import android.app.usage.UsageEvents;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.Calendar;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NotificationService extends Service {

    private static final String TAG = "NotificationService";
    private static final String NOTIFICATION_CHANNEL_ID_1 = "screen-time-notification";
    private static final int NOTIFICATION_ID_1 = 1;
    public static boolean isScreenTimeNotificationRunning = false;
    private static final String NOTIFICATION_CHANNEL_ID_2 = "goal-notification";
    private static final int NOTIFICATION_ID_2 = 2;
    public static boolean isTotalGoalNotificationRunning = false;
    private static final int NOTIFICATION_ID_3 = 3;
    public static boolean isAppGoalNotificationRunning = false;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateNotificationRunnable;
    private String currentPackageName = "";
    private Long currentScreenTime = 0L;
    private Long totalScreenTime = 0L;
    private Long totalScreenTimeGoodGoal = 4L*60*60*1000;
    private Long totalScreenTimeBadGoal = 6L*60*60*1000;
    private Long appScreenTimeGoodGoal = 30L*60*1000;
    private Long appScreenTimeBadGoal = 90L*60*1000;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            if ("START_SCREEN_TIME_NOTIFICATION".equals(intent.getAction())) {
                isScreenTimeNotificationRunning = true;
                startForegroundService();
                updateNotificationRunnable = this::sendNotification;
                handler.post(updateNotificationRunnable);
            } else if ("STOP_SCREEN_TIME_NOTIFICATION".equals(intent.getAction())) {
                isScreenTimeNotificationRunning = false;
                isTotalGoalNotificationRunning = false;
                isAppGoalNotificationRunning = false;
                stopForeground(true);
                stopSelf();
                handler.removeCallbacks(updateNotificationRunnable);
            } else if ("START_TOTAL_GOAL_NOTIFICATION".equals(intent.getAction())) {
                isTotalGoalNotificationRunning = true;
                totalScreenTimeGoodGoal = ((Integer) intent.getIntExtra("goodGoal",4*60*60*1000)).longValue();
                totalScreenTimeBadGoal = ((Integer) intent.getIntExtra("badGoal",6*60*60*1000)).longValue();
            } else if ("STOP_TOTAL_GOAL_NOTIFICATION".equals(intent.getAction())) {
                isTotalGoalNotificationRunning = false;
            } else if ("START_APP_GOAL_NOTIFICATION".equals(intent.getAction())) {
                isAppGoalNotificationRunning = true;
                appScreenTimeGoodGoal = ((Integer) intent.getIntExtra("goodGoal",30*60*1000)).longValue();
                appScreenTimeBadGoal = ((Integer) intent.getIntExtra("badGoal",90*60*1000)).longValue();
            } else if ("STOP_APP_GOAL_NOTIFICATION".equals(intent.getAction())) {
                isAppGoalNotificationRunning = false;
            } else if ("UPDATE_TOTAL_GOAL_TIME".equals(intent.getAction())) {
                totalScreenTimeGoodGoal = ((Integer) intent.getIntExtra("goodGoal",4*60*60*1000)).longValue();
                totalScreenTimeBadGoal = ((Integer) intent.getIntExtra("badGoal",6*60*60*1000)).longValue();
            } else if ("UPDATE_APP_GOAL_TIME".equals(intent.getAction())) {
                appScreenTimeGoodGoal = ((Integer) intent.getIntExtra("goodGoal",30*60*1000)).longValue();
                appScreenTimeBadGoal = ((Integer) intent.getIntExtra("badGoal",90*60*1000)).longValue();
            }
        }
        return START_STICKY; // If the service is killed, Android will try to restart it
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name1 = "Screen Time Notification Channel";
            String description1 = "Channel for persistent screen time notification";
            int importance1 = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel1 = new NotificationChannel(NOTIFICATION_CHANNEL_ID_1, name1, importance1);
            channel1.setDescription(description1);
            
            CharSequence name2 = "Goal Notification Channel";
            String description2 = "Channel for goal notification";
            int importance2 = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel2 = new NotificationChannel(NOTIFICATION_CHANNEL_ID_2, name2, importance2);
            channel2.setDescription(description2);
            
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel1);
                notificationManager.createNotificationChannel(channel2);
            }
        }
    }

    private Notification buildScreenTimeNotification(String currentAppName) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        // Create an intent for the stop action
        Intent stopIntent = new Intent(this, NotificationService.class);
        stopIntent.setAction("STOP_SCREEN_TIME_NOTIFICATION");
        PendingIntent stopPendingIntent = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID_1)
                .setSmallIcon(R.mipmap.screen_time_tracker_icon)
                .setContentTitle("Today's Screen Time")
                .setContentText(currentAppName + " - " + convertToReadableTime(currentScreenTime))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setContentIntent(pendingIntent) // Clicking the notification opens the app
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopPendingIntent); // Add the stop button

        return builder.build();
    }

    private Notification buildTotalGoalModerateUsageNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID_2)
                .setSmallIcon(R.mipmap.screen_time_tracker_icon)
                .setContentTitle("Total Screen Time Goal")
                .setContentText("Total screen time has reached moderate usage. Consider reducing the device usage.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent); // Clicking the notification opens the app                

        return builder.build();
    }

    private Notification buildTotalGoalBadUsageNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID_2)
                .setSmallIcon(R.mipmap.screen_time_tracker_icon)
                .setContentTitle("Total Screen Time Goal")
                .setContentText("Total screen time has reached bad usage. Please stop using the device unless it is necessary.")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentIntent(pendingIntent); // Clicking the notification opens the app                

        return builder.build();
    }

    private Notification buildAppGoalModerateUsageNotification(String currentAppName) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID_2)
                .setSmallIcon(R.mipmap.screen_time_tracker_icon)
                .setContentTitle("App Screen Time Goal ("+currentAppName+")")
                .setContentText("This app's screen time has reached moderate usage. Consider reducing this app's usage.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent); // Clicking the notification opens the app                

        return builder.build();
    }

    private Notification buildAppGoalBadUsageNotification(String currentAppName) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID_2)
                .setSmallIcon(R.mipmap.screen_time_tracker_icon)
                .setContentTitle("App Screen Time Goal ("+currentAppName+")")
                .setContentText("This app's screen time has reached bad usage. Please stop using this app unless it is necessary.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent); // Clicking the notification opens the app                

        return builder.build();
    }

    private void startForegroundService() {
        try{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
                if (usageStatsManager == null) {
                    Log.e(TAG, "UsageStatsManager is null.");
                    return;
                }

                calculateScreenTime(usageStatsManager);

                PackageManager packageManager = getPackageManager();

                ApplicationInfo appInfo = packageManager.getApplicationInfo(currentPackageName, 0);                    
                String currentAppName = packageManager.getApplicationLabel(appInfo).toString();
                
                Notification notification1 = buildScreenTimeNotification(currentAppName);
                startForeground(NOTIFICATION_ID_1, notification1);
            } else {
                Log.e(TAG, "Usage stats are available on and above android version lollipop.");
            }
        } catch(Exception e){
            Log.e(TAG, "Error getting foreground usage: " + e.toString());
        }
    }

    private void sendNotification() {
        try{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
                if (usageStatsManager == null) {
                    Log.e(TAG, "UsageStatsManager is null.");
                    return;
                }

                calculateScreenTime(usageStatsManager);

                PackageManager packageManager = getPackageManager();

                ApplicationInfo appInfo = packageManager.getApplicationInfo(currentPackageName, 0);                    
                String currentAppName = packageManager.getApplicationLabel(appInfo).toString();
                
                Notification notification1 = buildScreenTimeNotification(currentAppName);
                NotificationManager notificationManager1 = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManager1 != null) {
                    notificationManager1.notify(NOTIFICATION_ID_1, notification1);
                }

                if (isTotalGoalNotificationRunning) {
                    if ((totalScreenTime/1000) == (totalScreenTimeGoodGoal/1000)) {
                        Notification notification2 = buildTotalGoalModerateUsageNotification();
                        NotificationManager notificationManager2 = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        if (notificationManager2 != null) {
                            notificationManager2.notify(NOTIFICATION_ID_2, notification2);
                        }
                    } else if ((totalScreenTime/1000) == (totalScreenTimeBadGoal/1000)) {
                        Notification notification2 = buildTotalGoalBadUsageNotification();
                        NotificationManager notificationManager2 = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        if (notificationManager2 != null) {
                            notificationManager2.notify(NOTIFICATION_ID_2, notification2);
                        }
                    }
                }

                if (isAppGoalNotificationRunning) {
                    if ((currentScreenTime/1000) == (appScreenTimeGoodGoal/1000)) {
                        Notification notification3 = buildAppGoalModerateUsageNotification(currentAppName);
                        NotificationManager notificationManager3 = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        if (notificationManager3 != null) {
                            notificationManager3.notify(NOTIFICATION_ID_3, notification3);
                        }
                    } else if ((currentScreenTime/1000) == (appScreenTimeBadGoal/1000)) {
                        Notification notification3 = buildAppGoalBadUsageNotification(currentAppName);
                        NotificationManager notificationManager3 = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        if (notificationManager3 != null) {
                            notificationManager3.notify(NOTIFICATION_ID_3, notification3);
                        }
                    }
                }

                handler.postDelayed(updateNotificationRunnable, 1000); // Update every 1 second                    
            } else {
                Log.e(TAG, "Usage stats are available on and above android version lollipop.");
            }
        } catch(Exception e){
            Log.e(TAG, "Error getting foreground usage: " + e.toString());
        }        
    }

    private void calculateScreenTime(UsageStatsManager usageStatsManager) {
        Calendar calendar = Calendar.getInstance();
        Long endTime = calendar.getTimeInMillis();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Long startTime = calendar.getTimeInMillis();

        UsageEvents events = usageStatsManager.queryEvents(startTime, endTime);
        List<UsageEvents.Event> eventList = new ArrayList<>();

        while (events.hasNextEvent()) {
            UsageEvents.Event event = new UsageEvents.Event();
            events.getNextEvent(event);
            eventList.add(event);
        }

        totalScreenTime = 0L;

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
                    totalScreenTime += duration;
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
                    totalScreenTime += duration;
                }
            }
        }

        for (String packageName : foregroundEvents.keySet()) {
            Long duration = endTime - foregroundEvents.get(packageName);
            currentPackageName = packageName;
            currentScreenTime = usageStatsMap.getOrDefault(packageName, 0L) + duration;
            totalScreenTime += duration;
        }

    }

    private String convertToReadableTime(Long time) {
        Long totalSeconds = time / 1000;
        Long seconds = totalSeconds % 60;
        Long totalMinutes = totalSeconds / 60;
        Long minutes = totalMinutes % 60;
        Long hours = totalMinutes / 60;
        String readableScreenTime = "";

        if (hours < 10) {
            readableScreenTime = "0"+String.valueOf(hours);
        } else {
            readableScreenTime = String.valueOf(hours);
        }

        readableScreenTime += ":";

        if (minutes < 10) {
            readableScreenTime += "0"+String.valueOf(minutes);
        } else {
            readableScreenTime += String.valueOf(minutes);
        }

        readableScreenTime += ":";

        if (seconds < 10) {
            readableScreenTime += "0"+String.valueOf(seconds);
        } else {
            readableScreenTime += String.valueOf(seconds);
        }

        return readableScreenTime;
    }
}