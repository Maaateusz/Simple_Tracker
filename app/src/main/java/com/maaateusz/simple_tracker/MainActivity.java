package com.maaateusz.simple_tracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private TextView locationTextView;
    private TextView locationTextView2;
    private TextView locationTextView3;
    private Button getLocationBtn;
    private Button getLocationBtn2;
    private LocationManager locationManager;
    private LocationTracker locationTracker;
    private boolean isPermissionGranted = false;
    private boolean isRouteStart = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationTextView = (TextView) findViewById(R.id.locationTextView);
        locationTextView2 = (TextView) findViewById(R.id.locationTextView2);
        locationTextView3 = (TextView) findViewById(R.id.locationTextView3);
        getLocationBtn = (Button) findViewById(R.id.getLocationBtn);
        getLocationBtn2 = (Button) findViewById(R.id.getLocationBtn2);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        getLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                @SuppressLint("MissingPermission")
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location != null) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();

                    locationTextView.setText("Last Known Location: <Latitude | Longitude>\n" +
                            "Degrees: <" + Location.convert(latitude, Location.FORMAT_DEGREES) + " | " + Location.convert(longitude, Location.FORMAT_DEGREES) + ">\n" +
                            "Seconds: <" + Location.convert(latitude, Location.FORMAT_SECONDS) + " | " + Location.convert(longitude, Location.FORMAT_DEGREES) + ">\n" +
                            "Minutes: <" + Location.convert(latitude, Location.FORMAT_MINUTES) + " | " + Location.convert(longitude, Location.FORMAT_DEGREES) + ">\n" +
                            "Raw: <" + latitude + " | " + longitude +">");
                }
            }
        });

        getLocationBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isRouteStart){
                    locationManager.removeUpdates(locationTracker);
                    initializeLocationManager();
                    isRouteStart = true;
                    locationTracker.isRouteStart(true);
                    getLocationBtn2.setText("End Route");
                } else {
                    isRouteStart = false;
                    locationManager.removeUpdates(locationTracker);
                    initializeLocationManager();
                    getLocationBtn2.setText("Start New Route");
                }
            }
        });

    }

    @SuppressLint("MissingPermission")
    public void initializeLocationManager(){
        locationTracker = new LocationTracker(this);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationTracker);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    isPermissionGranted = true;
                    initializeLocationManager();
                    Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show();
                } else {
                    finish();
                    System.exit(0);
                    Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //locationManager.removeUpdates(locationTracker);
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onResume() {
        super.onResume();
        //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,MINIMUM_TIME_BETWEEN_UPDATES,MINIMUM_DISTANCE_CHANGE_FOR_UPDATES, myLocationListener);
        //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationTracker);
    }
}