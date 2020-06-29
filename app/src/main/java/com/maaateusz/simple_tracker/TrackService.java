package com.maaateusz.simple_tracker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.List;
import java.util.Locale;

public class TrackService extends Service {

    private String TAG = TrackService.class.getSimpleName();
    private LocationManager locationManager;
    private LocationTracker locationTracker;
    NotificationManagerCompat notificationManager;
    private Handler handler = new Handler();
    private static  final int ONGOING_NOTIFICATION_ID = 2137;
    private String channelId;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    public TrackService(){

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");

        //locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //initializeLocationManager();
        //locationTracker.isRouteStart(true);

        MainActivity.isRouteStart = true;

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        String wayLatitude = ""+ location.getLatitude();
                        Log.d(TAG, " "+ wayLatitude);
                    }
                }
            }
        };
        setNotification();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                mFusedLocationClient.requestLocationUpdates(locationRequest, mLocationCallback, Looper.myLooper());
            }
        }

        handler.postDelayed(runnable, 1000);
        //return super.onStartCommand(intent, flags, startId);
        //return START_NOT_STICKY;
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
        stopForeground(true);
        Log.d(TAG, "onDestroy");

        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }

        //notificationManager.cancelAll();
//        if(locationManager != null){
//            locationManager.removeUpdates(locationTracker);
//        }
    }

    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            //List<Location> locationList = locationResult.getLocations();
            Log.d(TAG, " "+ locationResult.getLastLocation().getLatitude() +" "+ locationResult.getLastLocation().getLongitude());

//            if (locationList.size() > 0) {
//                Location location = locationList.get(locationList.size() - 1);
//            }
        }
    };

    final Runnable runnable = new Runnable() {
        @SuppressLint("MissingPermission")
        @Override
        public void run() {
            // TODO


            //Log.d(TAG, " "+ mFusedLocationClient.getLastLocation().getResult().getLatitude());
            //Location l = locationTracker.getLocation();
            //Log.d(TAG, " "+ l.getLatitude());
            //Log.d(TAG, " "+ "RUNNABLE: ");
            //handler.postDelayed(this, 1000);
        }
    };


    @SuppressLint("MissingPermission")
    public void initializeLocationManager(){
        locationTracker = new LocationTracker(getApplicationContext());
        //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationTracker);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationTracker);
    }

    public void setNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        channelId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? createNotificationChannel(notificationManager) : "";
        //Notification notification = new Notification.Builder(this, channelId) //CHANNEL_DEFAULT_IMPORTANCE // @RequiresApi(api = Build.VERSION_CODES.O)
        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setContentTitle("Content Title")
                .setContentText("Content Text")
                .setContentIntent(pendingIntent)
                .setTicker("Ticker")
                .setOnlyAlertOnce(true)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE) //shows basic information, such as the notification's icon and the content title, but hides the notification's full content.
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setSound(null)
                .build();

        startForeground(ONGOING_NOTIFICATION_ID, notification); //ONGOING_NOTIFICATION_ID
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String createNotificationChannel(NotificationManager notificationManager){
        String channelId = "my_service_channel_id";
        String channelName = "My Foreground Service";
        NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
        channel.setImportance(NotificationManager.IMPORTANCE_NONE);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        notificationManager.createNotificationChannel(channel);
        return channelId;
    }

}
