package com.maaateusz.simple_tracker;

import android.content.Context;
import android.content.Intent;
import android.location.Location;

import java.util.ArrayList;
import java.util.List;

class LocationTracker {

    private final Context context;
    //The computed distance is stored in results[0]. If results has length 2 or greater, the initial bearing is stored in results[1]. If results has length 3 or greater, the final bearing is stored in results[2].
    //private float[] result = new float[3];
    private final float[] result = new float[1];
    private final List<Location> twoLocations;
    private float distance, speedSum, avgSpeed;
    private int counter;
    private String other;
    private Location actualLocation;

    public LocationTracker(Context context){
        twoLocations = new ArrayList<>();
        distance = 0;
        speedSum = 0;
        avgSpeed = 0;
        counter = 0;
        other = "Distance: \nSpeed: \nAvg. Speed: \nMoved: ";
        this.context = context;
    }

    public void calculatePosition(Location location) {
        if(location != null) {
            actualLocation = location;
                if (twoLocations.size() < 1) {
                    twoLocations.add(actualLocation);
                } else if (twoLocations.size() == 1) {
                    twoLocations.add(actualLocation);
                    calculateOthers();
                } else {
                    twoLocations.set(0, twoLocations.get(1));
                    twoLocations.set(1, actualLocation);
                    calculateOthers();
                }
        }
    }

    public void sendBroadcastData(){
        Intent i = new Intent("location_update");
        i.putExtra("LOCATION", actualLocation);
        i.putExtra("OTHER", other);
        context.sendBroadcast(i);
    }

    public void calculateOthers(){
        float speed = actualLocation.getSpeed();
        counter++;
        speedSum += speed;
        avgSpeed = speedSum / counter;
        float avgSpeed2 = avgSpeed * 3.6f;
        float speed2 = speed * 3.6f;
        Location.distanceBetween((twoLocations.get(0)).getLatitude(), (twoLocations.get(0)).getLongitude(), actualLocation.getLatitude(), actualLocation.getLongitude(), result);

        other = "Distance:  " + String.format("%.3f", distance) +"m  |  "+
                String.format("%.3f", (distance/1000)) +"km\nSpeed:  "+
                String.format("%.3f", speed) +"m/s  |  "+
                String.format("%.3f", speed2) +"km/h\nAvg. Speed:  "+
                String.format("%.3f", avgSpeed) +"m/s  |  "+
                String.format("%.3f", avgSpeed2) +"km/h\nMoved:  "+
                String.format("%.3f", result[0]) +"m\nAccuracy:  "+
                String.format("%.3f", actualLocation.getAccuracy()) +"m\nAltitude:  "+
                String.format("%.3f", actualLocation.getAltitude()) +"m\nBearing:  "+
                String.format("%.3f", actualLocation.getBearing()) +"°";
        distance += result[0];
    }

}
