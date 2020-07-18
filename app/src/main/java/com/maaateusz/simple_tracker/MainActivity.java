package com.maaateusz.simple_tracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private TextView staticLocationTextView;
    private TextView dynamicLocationTextView2;
    private TextView otherLocationTextView3;
    private TextView timerTextView4;
    private Button getLastLocationBtn;
    private Button getLocationBtn2;
    public BroadcastReceiver broadcastReceiver;
    private Location location;
    //private String TAG = MainActivity.class.getSimpleName();
    private final Handler customHandler = new Handler();
    private long startTime = 0L;
    private long timeInMillis = 0L;
    private long l = 0L;
    private long milliseconds = 0L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        staticLocationTextView = (TextView) findViewById(R.id.staticLocationTextView);
        dynamicLocationTextView2 = (TextView) findViewById(R.id.dynamicLocationTextView2);
        otherLocationTextView3 = (TextView) findViewById(R.id.otherLocationTextView3);
        timerTextView4 = (TextView) findViewById(R.id.timerTextView4);
        getLastLocationBtn = (Button) findViewById(R.id.getLastLocationBtn);
        getLocationBtn2 = (Button) findViewById(R.id.getLocationBtn2);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        if(isLocationEnabled(this)) Toast.makeText(this, "Location On", Toast.LENGTH_SHORT).show();
        else Toast.makeText(this, "Location Off", Toast.LENGTH_SHORT).show();

        buttonsListeners();

        staticLocationTextView.setText("Last Known Location: <Latitude | Longitude>\nDegrees: < | >\nSeconds: < | >\nMinutes: < | >\nRaw: < | >");
        dynamicLocationTextView2.setText("Actual Location:\n< | >");
        otherLocationTextView3.setText("Distance: \nSpeed: \nAvg. Speed: \nMoved: ");
        timerTextView4.setTextColor(Color.RED);
        timerTextView4.setText(" 0: 0: 0:000");
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(broadcastReceiver == null){
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    location = (Location) Objects.requireNonNull(intent.getExtras()).get("LOCATION");
                    updateUI(intent.getStringExtra("OTHER"));
                }
            };
        }
        registerReceiver(broadcastReceiver, new IntentFilter("location_update"));

        if(getRoadStatus()){
            getLocationBtn2.setText("End Route");
            startTime();
            SharedPreferences sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
            startTime = sharedPreferences.getLong("startTime", 0);
        } else {
            getLocationBtn2.setText("Start New Route");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences sharedPreferences =  this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("startTime", startTime);
        editor.apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
            if(broadcastReceiver != null) {
                unregisterReceiver(broadcastReceiver);
            }
        customHandler.removeCallbacks(updateTimerThread);
    }

    private void buttonsListeners() {
        getLastLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLastKnownLocation();
            }
        });

        getLocationBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!getRoadStatus()){
                    getLocationBtn2.setText("End Route");
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
                    stopTime();
                    setRoadStatus(false);
                }
            }
        });
    }

    public void updateUI(String string){
        dynamicLocationTextView2.setText("Actual Location: \n<" + location.getLatitude() + " | " + location.getLongitude() +">");
//        SpannableStringBuilder spannable = new SpannableStringBuilder(string);
//        spannable.setSpan(
//                new ForegroundColorSpan(Color.RED),
//                0, // start
//                9, // end
//                Spannable.SPAN_EXCLUSIVE_INCLUSIVE
//        );
//        try {
//            spannable.setSpan(
//                    new ForegroundColorSpan(Color.RED),
//                    28, // start
//                    34, // end
//                    Spannable.SPAN_EXCLUSIVE_INCLUSIVE
//            );
//            spannable.setSpan(
//                    new ForegroundColorSpan(Color.RED),
//                    58, // start
//                    69, // end
//                    Spannable.SPAN_EXCLUSIVE_INCLUSIVE
//            );
//        } catch (Exception e){}

        //otherLocationTextView3.setText(spannable);
        otherLocationTextView3.setText(string);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show();
                finish();
                System.exit(0);
            }
        }
    }

    public void setLastKnownLocation(){
        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            staticLocationTextView.setText("Last Known Location: <Latitude | Longitude>\n" +
                    "Degrees: <" + Location.convert(latitude, Location.FORMAT_DEGREES) + " | " + Location.convert(longitude, Location.FORMAT_DEGREES) + ">\n" +
                    "Seconds: <" + Location.convert(latitude, Location.FORMAT_SECONDS) + " | " + Location.convert(longitude, Location.FORMAT_DEGREES) + ">\n" +
                    "Minutes:  <" + Location.convert(latitude, Location.FORMAT_MINUTES) + " | " + Location.convert(longitude, Location.FORMAT_DEGREES) + ">\n" +
                    "Raw:         <" + latitude + " | " + longitude +">");
        }
    }

    public static Boolean isLocationEnabled(Context context)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // This is new method provided in API 28
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            return lm.isLocationEnabled();
        } else {
            // This is Deprecated in API 28
            int mode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE,
                    Settings.Secure.LOCATION_MODE_OFF);
            return  (mode != Settings.Secure.LOCATION_MODE_OFF);
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
        editor.apply();
    }

    final Runnable updateTimerThread = new Runnable() {
        @Override
        public void run() {
            timeInMillis = SystemClock.uptimeMillis() - startTime;
            milliseconds = l + timeInMillis;
            int secs = (int) (milliseconds / 1000);
            int mins = secs / 60;
            int hours = (int) (mins /60);
            secs %= 60;
            mins %= 60;
            int millis = (int) (milliseconds % 1000);
            timerTextView4.setText("" + String.format("%2d", hours) + ":" + String.format("%2d", mins) + ":" + String.format("%2d", secs) + ":" + String.format("%3d", millis));
            customHandler.postDelayed(this, 100);
        }
    };

    public void startTime(){
            startTime = 0L;
            timeInMillis = 0L;
            l = 0L;
            milliseconds = 0L;
            timerTextView4.setText(" 0: 0: 0:000");
            if(getRoadStatus()) {
                SharedPreferences sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
                startTime = sharedPreferences.getLong("startTime", 0);
            } else {
                startTime = SystemClock.uptimeMillis();
            }
            customHandler.postDelayed(updateTimerThread, 500);
        }

    public void stopTime(){
            l += timeInMillis;
            customHandler.removeCallbacks(updateTimerThread);
        }

}