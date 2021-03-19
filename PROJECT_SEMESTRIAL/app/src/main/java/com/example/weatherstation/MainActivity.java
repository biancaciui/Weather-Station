package com.example.weatherstation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.hardware.SensorManager.getAltitude;

import okhttp3.Call;
import okhttp3.Callback;
import  okhttp3.MediaType;
import  okhttp3.RequestBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    public TextView temperature, humidity, pressure;
    private DatabaseReference ref,rootNode,reference;
    Button button,buttonHistory;

    //TextView test;

    public TextView altitude, longitude, latitude;
    private LocationManager locationManager;
    private LocationListener locationListener; // listens for location changes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //check the connection to the Firebase database
        //Toast.makeText(this, "Firebase connected successfully", Toast.LENGTH_LONG).show();

        //assign each variable declared to an actual textView
        temperature = (TextView) findViewById(R.id.tempLabel);
        humidity = (TextView) findViewById(R.id.humidLabel);
        pressure = (TextView) findViewById(R.id.pressureLabel);
        button = (Button) findViewById(R.id.button);
        buttonHistory = (Button) findViewById(R.id.button2);
        buttonHistory.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                openWeekHistory();
            }
        });

        altitude = (TextView) findViewById(R.id.altitudeLabel);
        longitude = (TextView) findViewById(R.id.longitudeLabel);
        latitude = (TextView) findViewById(R.id.latitudeLabel);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                //get the reference of the database
                rootNode = FirebaseDatabase.getInstance().getReference().child("FirebaseBranch");

                Date date = new Date(System.currentTimeMillis());
                SimpleDateFormat dayFormatter= new SimpleDateFormat("dd_MM_yyyy");//use as dayFormatter.format(date)
                SimpleDateFormat hourFormatter = new SimpleDateFormat("HH");//use as hourFormatter.format(date)

                //aici creez path-ul unde vreau sa INSEREZ VALORI
                if (hourFormatter.format(date).startsWith("0"))
                    reference = rootNode.child("date_"+dayFormatter.format(date)).child("hour_"+hourFormatter.format(date).charAt(1)).child("latitude");
                else
                    reference = rootNode.child("date_"+dayFormatter.format(date)).child("hour_"+hourFormatter.format(date)).child("latitude");
                //called whenever the location is updated
                double lat = location.getLatitude();
                lat = Math.floor(lat * 1000000) / 1000000;//truncate to 6 digits after decimal point
                //inserez valoarea
                reference.setValue(lat);

                if (hourFormatter.format(date).startsWith("0"))
                    reference = rootNode.child("date_"+dayFormatter.format(date)).child("hour_"+hourFormatter.format(date).charAt(1)).child("longitude");
                else
                    reference = rootNode.child("date_"+dayFormatter.format(date)).child("hour_"+hourFormatter.format(date)).child("longitude");
                double lon = location.getLongitude();
                lon = Math.floor(lon * 1000000) / 1000000;
                reference.setValue(lon);

                latitude.setText(String.valueOf(lat));
                longitude.setText(String.valueOf(lon));

                if (hourFormatter.format(date).startsWith("0"))
                    reference = rootNode.child("date_"+dayFormatter.format(date)).child("hour_"+hourFormatter.format(date).charAt(1)).child("altitude");
                else
                    reference = rootNode.child("date_"+dayFormatter.format(date)).child("hour_"+hourFormatter.format(date)).child("altitude");

                if(location.hasAltitude() && location.getAltitude() != 0.0){    //only update if the value is a valid one
                    String alti = String.valueOf((int)location.getAltitude());
                    alti += " m";
                    altitude.setText(alti);
                    reference.setValue((int)location.getAltitude());
                }
                else{
                    try {
                        double alti = getAltitude(lon,lat);
                        if(alti != 0.0){ //only update if the value is a valid one, else, we keep the same altitude as before
                            String altiS = String.valueOf((int)alti) + " m";
                            altitude.setText(altiS);
                            reference.setValue((int)alti);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            @Override

            public void onProviderDisabled(String s){
                //if GPS isn't turned on, the user will be sent to settings where he can enable the GPS
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        };

        //permission check
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.INTERNET
                }, 1);//we set the request code to use it later
                return;
            }
            else{
                requestLoc();
            }


    }

    private double getAltitude(Double longitude, Double latitude) throws IOException {
        double result = 0.0;
        OkHttpClient client = new OkHttpClient();
        String url = "http://ned.usgs.gov/epqs/"
                + "pqs.php?x=" + String.valueOf(longitude)
                + "&y=" + String.valueOf(latitude)
                + "&units=Meters&output=json";

        final Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //test.setText("FAILURE!");
                    }
                });

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful()){
                    final String responseS = response.body().string();
                    //we can't access directly th   e test label, we need to create a new thread that can access it
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                           //test.setText(responseS);
                        }
                    });
                }
            }
        });
        return result;
    }

    public void openWeekHistory(){
        Intent intent = new Intent(this, WeekHistory.class);
        startActivity(intent);
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode){
            case 1:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    requestLoc();
                return;
        }
    };

    private void requestLoc() {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //create the connection to the firebase database
                ref = FirebaseDatabase.getInstance().getReference().child("FirebaseBranch");
                ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //get the date and hour
                        Date date = new Date(System.currentTimeMillis());
                        SimpleDateFormat dayFormatter= new SimpleDateFormat("dd_MM_yyyy");//use as dayFormatter.format(date)
                        SimpleDateFormat hourFormatter = new SimpleDateFormat("HH");//use as hourFormatter.format(date)

                        //get data from database
                        //reference = ref.child("date_"+dayFormatter.format(date)).child("hour_"+hourFormatter.format(date));
                        String tempS="", humidS="", pressS="", altiS="";
                        Object temp, humid, press, alti;
                        if(hourFormatter.format(date).startsWith("0")) {
                            //la fel, setez path-ul DE UNDE VREAU SA EXTRAG VALORI
                            temp = dataSnapshot.child("date_"+dayFormatter.format(date)).child("hour_"+hourFormatter.format(date).charAt(1)).child("temperature").getValue();
                            humid = dataSnapshot.child("date_"+dayFormatter.format(date)).child("hour_"+hourFormatter.format(date).charAt(1)).child("humidity").getValue();
                            press = dataSnapshot.child("date_"+dayFormatter.format(date)).child("hour_"+hourFormatter.format(date).charAt(1)).child("pressure").getValue();
                            alti = dataSnapshot.child("date_"+dayFormatter.format(date)).child("hour_"+hourFormatter.format(date).charAt(1)).child("altitude").getValue();
                        }
                        else{
                            temp = dataSnapshot.child("date_"+dayFormatter.format(date)).child("hour_"+hourFormatter.format(date)).child("temperature").getValue();
                            humid = dataSnapshot.child("date_"+dayFormatter.format(date)).child("hour_"+hourFormatter.format(date)).child("humidity").getValue();
                            press = dataSnapshot.child("date_"+dayFormatter.format(date)).child("hour_"+hourFormatter.format(date)).child("pressure").getValue();
                            alti = dataSnapshot.child("date_"+dayFormatter.format(date)).child("hour_"+hourFormatter.format(date)).child("altitude").getValue();
                        }

                        if (temp != null) tempS = temp.toString(); else tempS = "0";
                        if(humid != null) humidS = humid.toString(); else humidS = "0";
                        if(press != null) pressS = press.toString(); else pressS = "0";
                        if(alti != null) altiS = alti.toString(); else altiS = "0";

                        tempS += " °C";
                        humidS += " %";
                        pressS += " Pa";
                        altiS += " m";

                        //set text to the newly read data
                        temperature.setText(tempS);
                        humidity.setText(humidS);
                        pressure.setText(pressS);
                        altitude.setText(altiS);

                        //request the location
                        //first param is the provider -> gps
                        //second param is the refresh interval -> 5 seconds
                        //third param is the min distance that triggers the update of the location
                        //      (if it is 5 for ex, the location listener will be called after we moved >= 5 meters from the prev location)
                        //      we set it to 0 because the update will be done in 5 seconds anyway
                        try {
                            locationManager.requestLocationUpdates("gps", 5000, 0, locationListener);
                        }catch (SecurityException se){};
                    }
                    //read the gps coordinates and get the altitude

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

    }
}