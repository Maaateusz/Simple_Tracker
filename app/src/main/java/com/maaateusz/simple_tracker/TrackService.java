package com.maaateusz.simple_tracker;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class TrackService extends Service {

    private LocationManager locationManager;
    private LocationTracker locationTracker;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Toast.makeText(this, "Create", Toast.LENGTH_SHORT).show();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //initializeLocationManager();
        //locationManager.removeUpdates(locationTracker);
        initializeLocationManager();
        locationTracker.isRouteStart(true);

        super.onCreate();
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
        //return super.onStartCommand(intent, flags, startId);
        //return START_NOT_STICKY;
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "Destroy", Toast.LENGTH_SHORT).show();
        if(locationManager != null){
            locationManager.removeUpdates(locationTracker);
        }
        super.onDestroy();
    }
}
