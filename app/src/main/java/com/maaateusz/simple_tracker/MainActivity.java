package com.maaateusz.simple_tracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private TextView locationTextView;
    private TextView locationTextView2;
    private TextView locationTextView3;
    private TextView locationTextView4;
    private Button getLocationBtn;
    private Button getLocationBtn2;
    public BroadcastReceiver broadcastReceiver;
    private Location location;
    private String TAG = MainActivity.class.getSimpleName();
    private Handler customHandler = new Handler();
    private long startTime = 0l, timeInMilis = 0l, timeInMilisBuff = 0l, milliseconds = 0l, getTimeMilis = 0l;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationTextView = (TextView) findViewById(R.id.locationTextView);
        locationTextView2 = (TextView) findViewById(R.id.locationTextView2);
        locationTextView3 = (TextView) findViewById(R.id.locationTextView3);
        locationTextView4 = (TextView) findViewById(R.id.locationTextView4);
        getLocationBtn = (Button) findViewById(R.id.getLocationBtn);
        getLocationBtn2 = (Button) findViewById(R.id.getLocationBtn2);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        buttonsListeners();
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: "+ getRoadStatus());

        if(broadcastReceiver == null){
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    location = (Location) intent.getExtras().get("LOCATION");
                    updateUI(intent.getStringExtra("OTHER"));
                    Log.d(TAG, ""+ location.getLatitude() +" "+ location.getLongitude());
                }
            };
        }
        registerReceiver(broadcastReceiver, new IntentFilter("location_update"));

        if(getRoadStatus()){
            getLocationBtn2.setText("End Route");
            startTime();
            SharedPreferences sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
            startTime = sharedPreferences.getLong("milliseconds", 0);
            Log.d(TAG, "getMilis: "+ startTime);

        } else {
            getLocationBtn2.setText("Start New Route");
        }

        locationTextView.setText("Last Known Location: <Latitude | Longitude>\nDegrees: < | >\nSeconds: < | >\nMinutes: < | >\nRaw: < | >");
        locationTextView2.setText("Actual Location:\n< | >");
        locationTextView3.setText("Distance: \nSpeed: \nAvg. Speed: \nMoved: ");
        locationTextView4.setText(" 0: 0: 0:000");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
            if(broadcastReceiver != null) {
                unregisterReceiver(broadcastReceiver);
            }
        customHandler.removeCallbacks(updateTimerThread);

        SharedPreferences sharedPreferences =  this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("milliseconds", startTime);
        editor.commit();
        Log.d(TAG, "setMilis: "+ startTime);
    }

    private void buttonsListeners() {
        getLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLastKnownLocation();
            }
        });

        getLocationBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Status: "+ getRoadStatus());
                if(!getRoadStatus()){
                    getLocationBtn2.setText("End Route");
                    Log.d(TAG, "Start Service");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        MainActivity.this.startForegroundService(new Intent(MainActivity.this,  TrackService.class));
                    } else {
                        MainActivity.this.startService(new Intent(MainActivity.this,  TrackService.class));
                    }
                    startTime();
                    setRoadStatus(true);
                } else {
                    getLocationBtn2.setText("Start New Route");
                    MainActivity.this.stopService(new Intent(MainActivity.this,  TrackService.class));
                    Log.d(TAG, "Stop Service");
                    stopTime();
                    setRoadStatus(false);
                }
            }
        });

    }

    public void updateUI(String string){
        locationTextView2.setText("Actual Location: \n<" + location.getLatitude() + " | " + location.getLongitude() +">");
        locationTextView3.setText(string);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show();
                    finish();
                    System.exit(0);
                }
                return;
            }
        }
    }

    public void setLastKnownLocation(){
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

    public boolean getRoadStatus(){
        SharedPreferences sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("isRoadOn", false);
    }

    public void setRoadStatus(boolean status){
        SharedPreferences sharedPreferences =  this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isRoadOn", status);
        editor.commit();
    }

    Runnable updateTimerThread = new Runnable() {
        @Override
        public void run() {
            timeInMilis = SystemClock.uptimeMillis() - startTime;
            milliseconds = timeInMilisBuff + timeInMilis;
            int secs = (int) (milliseconds / 1000);
            int mins = secs / 60;
            int hours = (int) (mins /60);
            secs %= 60;
            mins %= 60;
            int milis = (int) (milliseconds % 1000);
            locationTextView4.setText(String.format("" + String.format("%2d", hours) + ":" + String.format("%2d", mins) + ":" + String.format("%2d", secs) + ":" + String.format("%3d", milis)));
            customHandler.postDelayed(this, 100);
        }
    };

    public void startTime(){
            startTime = 0l;
            timeInMilis = 0l;
            timeInMilisBuff = 0l;
            milliseconds = 0l;
            getTimeMilis = 0l;
            locationTextView4.setText(" 0: 0: 0:000");
            startTime = SystemClock.uptimeMillis();
            customHandler.postDelayed(updateTimerThread, 500);
        }

    public void stopTime(){
            timeInMilisBuff += timeInMilis;
            customHandler.removeCallbacks(updateTimerThread);
        }

}