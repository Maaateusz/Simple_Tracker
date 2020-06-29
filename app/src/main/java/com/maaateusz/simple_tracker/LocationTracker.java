package com.maaateusz.simple_tracker;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

class LocationTracker implements LocationListener {
    private TextView locationTextView2;
    private TextView locationTextView3;
    private Context context;
    //The computed distance is stored in results[0]. If results has length 2 or greater, the initial bearing is stored in results[1]. If results has length 3 or greater, the final bearing is stored in results[2].
    //private float[] result = new float[3];
    private float[] result = new float[1];
    private List<Location> twoLocations;
    private float distance, speedSum, avgSpeed;
    private int counter;
    private boolean isRouteStart = false;
    private String other;
    private Location loc;

    public LocationTracker( Context context){
        twoLocations = new ArrayList<>();
        distance = 0;
        speedSum = 0;
        avgSpeed = 0;
        counter = 0;
        //locationTextView2 = ((Activity)context).findViewById(R.id.locationTextView2);
        //locationTextView3 = ((Activity)context).findViewById(R.id.locationTextView3);
        this.context = context;
    }

    public Location getLocation(){
        return loc;
//        if(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
//            Toast.makeText(context,"Permission not granted!", Toast.LENGTH_SHORT).show();
//            return null;
//        }
//        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
//        boolean isGPSenabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
//        if(isGPSenabled){
//            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 1, this);
//            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//            return location;
//        } else{
//            Toast.makeText(context,"Turn ON GPS!", Toast.LENGTH_SHORT).show();
//        }
//        return null;
    }

    // latitude | longitude
    @Override
    public void onLocationChanged(Location location) {
        loc = location;
        //locationTextView2.setText("Actual Location: \n<" + location.getLatitude() + " | " + location.getLongitude() +">");
        if(isRouteStart) {
            if (twoLocations.size() < 1) {
                twoLocations.add(location);
            } else if (twoLocations.size() == 1) {
                twoLocations.add(location);
                calculatePosition(location);
            } else {
                twoLocations.set(0, twoLocations.get(1));
                twoLocations.set(1, location);
                calculatePosition(location);
            }
        }

        //Toast.makeText(context,"Send Broadcast",Toast.LENGTH_LONG).show();
        Intent i = new Intent("location_update");
        i.putExtra("LOCATION", location);
        i.putExtra("OTHER", other);
        context.sendBroadcast(i);
        Log.d("LOCATION", ""+ location.getLatitude());
    }

    public void isRouteStart(boolean isRouteStart){
        this.isRouteStart = isRouteStart;
    }

    public void calculatePosition(Location location){
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        float speed = location.getSpeed();
        counter++;
        speedSum += speed;
        avgSpeed = speedSum / counter;
        Location.distanceBetween((twoLocations.get(0)).getLatitude(), (twoLocations.get(0)).getLongitude(), latitude, longitude, result);
        //locationTextView3.setText("Distance: " + distance +"m\nSpeed: "+ speed +"m/s\nAvg. Speed: "+ avgSpeed +"m/s\nMoved: "+ result[0] +"m");
        other = "Distance: " + distance +"m\nSpeed: "+ speed +"m/s\nAvg. Speed: "+ avgSpeed +"m/s\nMoved: "+ result[0] +"m";
        distance += result[0];
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(context,"GPS is enabled",Toast.LENGTH_LONG).show();
    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(context,"GPS is disabled",Toast.LENGTH_LONG).show();
        Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }
}
