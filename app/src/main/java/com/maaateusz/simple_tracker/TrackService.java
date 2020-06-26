package com.maaateusz.simple_tracker;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class TrackService extends Service {

    private LocationManager locationManager;
    private LocationTracker locationTracker;
    NotificationManagerCompat notificationManager;

    private Looper serviceLooper;
    private ServiceHandler serviceHandler;

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            // Normally we would do some work here, like download a file.
            // For our sample, we just sleep for 5 seconds.
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                // Restore interrupt status.
                Thread.currentThread().interrupt();
            }
            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
            stopSelf(msg.arg1);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Toast.makeText(this, "Create", Toast.LENGTH_SHORT).show();

        // Start up the thread running the service. Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block. We also make it
        // background priority so CPU-intensive work doesn't disrupt our UI.
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        serviceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper);

        /*createNotificationChannel();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "123")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Title")
                .setContentText("Content")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOnlyAlertOnce(true);

        notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(100, builder.build());
*/
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //initializeLocationManager();
        //locationManager.removeUpdates(locationTracker);
        initializeLocationManager();
        locationTracker.isRouteStart(true);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification =
                null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notification = new Notification.Builder(this, "123")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("Title")
                    .setContentText("Content")
                    .setContentIntent(pendingIntent)
                    .build();
        }

        startForeground(100, notification);
        MainActivity.isRouteStart = true;
        super.onCreate();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Name";
            String description = "Description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("123", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @SuppressLint("MissingPermission")
    public void initializeLocationManager(){
        locationTracker = new LocationTracker(getApplicationContext());
        //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationTracker);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, locationTracker);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Start", Toast.LENGTH_SHORT).show();

        Message msg = serviceHandler.obtainMessage();
        msg.arg1 = startId;
        serviceHandler.sendMessage(msg);

        //return super.onStartCommand(intent, flags, startId);
        //return START_NOT_STICKY;
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "Destroy", Toast.LENGTH_SHORT).show();
        notificationManager.cancelAll();
        if(locationManager != null){
            locationManager.removeUpdates(locationTracker);
        }
        super.onDestroy();
    }
}
