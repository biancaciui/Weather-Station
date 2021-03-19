package com.example.weatherstation;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.List;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class WeekHistory extends AppCompatActivity {

    private DatabaseReference ref,rootNode,reference;

    private TextView day1,day1T, day1H, day1P,day2,day2T, day2H, day2P,day3,day3T, day3H, day3P,day4,day4T, day4H, day4P,day5,day5T, day5H, day5P,day6,day6T, day6H, day6P;
    private int[] minT,maxT,minH,maxH,minP,maxP;
    private String[] past6Dates;
    private ArrayList<Integer> buffer = new ArrayList<Integer>(24);; //this buffer will be used to read from the firebase database

    private TextView today;

    //for day's history
    public List<Temp_Hour> tempList = new ArrayList<>();
    private Temp_Hour_Adapter myAdapter;

    public LinearLayout includedLayout;
    public RecyclerView recyclerView;
    public LinearLayoutManager myLayoutManager;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_week_history);

        //LinearLayout linearLayout = findViewById(R.id.rlMain);
        //recyclerView = linearLayout.findViewById(R.id.recyclerView1);

//        recyclerView = findViewById(R.id.recyclerView1);
//        myAdapter = new Temp_Hour_Adapter(tempList);
//        myLayoutManager = new LinearLayoutManager(this);
//        myLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
//        recyclerView.setLayoutManager(myLayoutManager);
//        recyclerView.setItemAnimator(new DefaultItemAnimator());
//        recyclerView.setAdapter(myAdapter);
//        prepareTempDataDay();

        instantiateIntArrays();
        setLabels();
        past6Dates = extractPast6Dates();
        updateDateTextViews(past6Dates);

        getNsetTemperatures(past6Dates);
        getNsetHumidity(past6Dates);
        getNsetPressure(past6Dates);

        prepareTodayDate();

        //ConstraintLayout constraintLayout = findViewById(R.id.constraint_layout);
        //LinearLayout linearLayout = findViewById(R.id.rlMain);
        //includedLayout = findViewById(R.id.includedRecyclerLayout);

    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void prepareTodayDate(){
        today = (TextView)findViewById(R.id.todayTextView);
        LocalDateTime currentDate = LocalDateTime.now();
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("dd_MM_yyyy");
        String s = "Today: "+dayFormatter.format(currentDate);
        today.setText(s);
    }
    public void prepareTempDataDay(){
        Temp_Hour th;
        for(int i=0; i<=23; i++){
            th = new Temp_Hour(i, i);
            tempList.add(th);
        }
        myAdapter.notifyDataSetChanged();
    }
    public void updateTempTextViews(){
        for(int i=0;i<=5;i++){
            if(minT[i] == 99999 && maxT[i] == -99999){
                String buff = "Nan";
                switch (i){
                    case 0: day1T.setText(buff); break;
                    case 1: day2T.setText(buff); break;
                    case 2: day3T.setText(buff); break;
                    case 3: day4T.setText(buff); break;
                    case 4: day5T.setText(buff); break;
                    case 5: day6T.setText(buff); break;
                    default: break;
                }
            }
            else{
                if(minT[i] == 99999 && maxT[i] != -99999) minT[i] = maxT[i];
                if(maxT[i] == -99999 && minT[i] != 99999) maxT[i] = maxT[i];
                String buff = minT[i] + " - " + maxT[i];
                switch (i){
                    case 0: day1T.setText(buff); break;
                    case 1: day2T.setText(buff); break;
                    case 2: day3T.setText(buff); break;
                    case 3: day4T.setText(buff); break;
                    case 4: day5T.setText(buff); break;
                    case 5: day6T.setText(buff); break;
                    default: break;
                }
            }
        }
    }
    public void updateHumidTextViews(){
        for(int i=0;i<=5;i++){
            if(minH[i] == 99999 && maxH[i] == -99999){
                String buff = "Nan";
                switch (i){
                    case 0: day1H.setText(buff); break;
                    case 1: day2H.setText(buff); break;
                    case 2: day3H.setText(buff); break;
                    case 3: day4H.setText(buff); break;
                    case 4: day5H.setText(buff); break;
                    case 5: day6H.setText(buff); break;
                    default: break;
                }
            }
            else{
                if(minH[i] == 99999 && maxH[i] != -99999) minH[i] = maxH[i];
                if(maxH[i] == -99999 && minH[i] != 99999) maxH[i] = maxH[i];
                String buff = minH[i] + " - " + maxH[i];
                switch (i){
                    case 0: day1H.setText(buff); break;
                    case 1: day2H.setText(buff); break;
                    case 2: day3H.setText(buff); break;
                    case 3: day4H.setText(buff); break;
                    case 4: day5H.setText(buff); break;
                    case 5: day6H.setText(buff); break;
                    default: break;
                }
            }
        }
    }
    public void updatePressTextViews(){
        for(int i=0;i<=5;i++){
            if(minP[i] == 99999 && maxP[i] == -99999){
                String buff = "Nan";
                switch (i){
                    case 0: day1P.setText(buff); break;
                    case 1: day2P.setText(buff); break;
                    case 2: day3P.setText(buff); break;
                    case 3: day4P.setText(buff); break;
                    case 4: day5P.setText(buff); break;
                    case 5: day6P.setText(buff); break;
                    default: break;
                }
            }
            else{
                if(minP[i] == 99999 && maxP[i] != -99999) minP[i] = maxP[i];
                if(maxP[i] == -99999 && minP[i] != 99999) maxP[i] = maxP[i];
                String buff = minP[i] + " - " + maxP[i];
                switch (i){
                    case 0: day1P.setText(buff); break;
                    case 1: day2P.setText(buff); break;
                    case 2: day3P.setText(buff); break;
                    case 3: day4P.setText(buff); break;
                    case 4: day5P.setText(buff); break;
                    case 5: day6P.setText(buff); break;
                    default: break;
                }
            }
        }
    }
    public void initializeBuffer(){
        for(int i=0; i<=23; i++)
            buffer.add(-99999);
    }
    //for each date given as parameter, we extract and set the min and max temperatures
    public void getNsetTemperatures(String[] dates){
        rootNode = FirebaseDatabase.getInstance().getReference().child("FirebaseBranch");
        rootNode.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //extract all the Temperature data from the database
                for(int i = 0; i <= 5; i++){    //we use dates[i]
                    initializeBuffer();
                    for(int j = 0; j <= 23; j++ ) { //we use this for hours
                        //we set the path from which we extract the data from the firebase database and extract the value
                        Object x = snapshot.child("date_"+dates[i]).child("hour_"+j).child("temperature").getValue(Integer.class);
                        if (x != null) { /*System.out.println("\n\nfound a value!!! -> "+ x +"\n");*/ buffer.set(j, (int)x);}
                        //else buffer.set(j, -99999);
                    }
                    //verify that we have valid elements in the buffer
                    boolean validElems = false;
                    for (int j = 0; j <= 23; j++)
                        if (buffer.get(j) != -99999) {
                            validElems = true; break;
                        }
                    //save the min and max values
                    minT[i]=99999;
                    maxT[i]=-99999;
                    if (validElems) {
                        //calculate the min and max
                        for (int j = 0; j < 24; j++) {
                            if (minT[i] > buffer.get(j) && buffer.get(j) != -99999)
                                minT[i] = buffer.get(j);
                            if(maxT[i] < buffer.get(j) && buffer.get(j) != -99999)
                                maxT[i] = buffer.get(j);
                        }
                    }
                    //clear the buffer for the next day to come!
                    buffer.clear();
                }
                updateTempTextViews();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { Toast.makeText(WeekHistory.this, "Error fetching data", Toast.LENGTH_LONG).show();}
        });

    }
    public void getNsetHumidity(String[] dates){
        rootNode = FirebaseDatabase.getInstance().getReference().child("FirebaseBranch");
        rootNode.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //extract all the Temperature data from the database
                for(int i = 0; i <= 5; i++){    //we use dates[i]
                    initializeBuffer();
                    for(int j = 0; j <= 23; j++ ) { //we use this for hours
                        //we set the path from which we extract the data from the firebase database and extract the value
                        Object x = snapshot.child("date_"+dates[i]).child("hour_"+j).child("humidity").getValue(Integer.class);
                        if (x != null) { /*System.out.println("\n\nfound a value!!! -> "+ x +"\n");*/ buffer.set(j, (int)x);}
                        //else buffer.set(j, -99999);
                    }
                    //verify that we have valid elements in the buffer
                    boolean validElems = false;
                    for (int j = 0; j <= 23; j++)
                        if (buffer.get(j) != -99999) {
                            validElems = true; break;
                        }
                    //save the min and max values
                    minH[i]=99999;
                    maxH[i]=-99999;
                    if (validElems) {
                        //calculate the min and max
                        for (int j = 0; j < 24; j++) {
                            if (minH[i] > buffer.get(j) && buffer.get(j) != -99999)
                                minH[i] = buffer.get(j);
                            if(maxH[i] < buffer.get(j) && buffer.get(j) != -99999)
                                maxH[i] = buffer.get(j);
                        }
                    }
                    //clear the buffer for the next day to come!
                    buffer.clear();
                }
                updateHumidTextViews();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { Toast.makeText(WeekHistory.this, "Error fetching data", Toast.LENGTH_LONG).show();}
        });
    }
    public void getNsetPressure(String[] dates){
        rootNode = FirebaseDatabase.getInstance().getReference().child("FirebaseBranch");
        rootNode.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //extract all the Temperature data from the database
                for(int i = 0; i <= 5; i++){    //we use dates[i]
                    initializeBuffer();
                    for(int j = 0; j <= 23; j++ ) { //we use this for hours
                        //we set the path from which we extract the data from the firebase database and extract the value
                        Object x = snapshot.child("date_"+dates[i]).child("hour_"+j).child("pressure").getValue(Integer.class);
                        if (x != null) { /*System.out.println("\n\nfound a value!!! -> "+ x +"\n"); */buffer.set(j, (int)x);}
                        //else buffer.set(j, -99999);
                    }
                    //verify that we have valid elements in the buffer
                    boolean validElems = false;
                    for (int j = 0; j <= 23; j++)
                        if (buffer.get(j) != -99999) {
                            validElems = true; break;
                        }
                    //save the min and max values
                    minP[i]=99999;
                    maxP[i]=-99999;
                    if (validElems) {
                        //calculate the min and max
                        for (int j = 0; j < 24; j++) {
                            if (minP[i] > buffer.get(j) && buffer.get(j) != -99999)
                                minP[i] = buffer.get(j);
                            if(maxP[i] < buffer.get(j) && buffer.get(j) != -99999)
                                maxP[i] = buffer.get(j);
                        }
                    }
                    //clear the buffer for the next day to come!
                    buffer.clear();
                }
                updatePressTextViews();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { Toast.makeText(WeekHistory.this, "Error fetching data", Toast.LENGTH_LONG).show();}
        });

    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public String[] extractPast6Dates(){
        LocalDateTime[] past6Days = new LocalDateTime[6];
        String[] past6Dates = new String[6];
        LocalDateTime currentDate = LocalDateTime.now();
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("dd_MM_yyyy");
        for(int i = 1; i <= 6; i++)
            past6Days[i-1] = currentDate.minusDays(i);
        for(int i = 0; i <= 5; i++)
            past6Dates[i] = dayFormatter.format(past6Days[i]);

        return past6Dates;
    }
    public void setLabels(){
        day1 = (TextView)findViewById(R.id.day1TextView);
        day2 = (TextView)findViewById(R.id.day2TextView);
        day3 = (TextView)findViewById(R.id.day3TextView);
        day4 = (TextView)findViewById(R.id.day4TextView);
        day5 = (TextView)findViewById(R.id.day5TextView);
        day6 = (TextView)findViewById(R.id.day6TextView);
        day1T = (TextView)findViewById(R.id.day1TempTextView);
        day1H = (TextView)findViewById(R.id.day1HumidTextView);
        day1P = (TextView)findViewById(R.id.day1PressTextView);
        day2T = (TextView)findViewById(R.id.day2TempTextView);
        day2H = (TextView)findViewById(R.id.day2HumidTextView);
        day2P = (TextView)findViewById(R.id.day2PressTextView);
        day3T = (TextView)findViewById(R.id.day3TempTextView);
        day3H = (TextView)findViewById(R.id.day3HumidTextView);
        day3P = (TextView)findViewById(R.id.day3PressTextView);
        day4T = (TextView)findViewById(R.id.day4TempTextView);
        day4H = (TextView)findViewById(R.id.day4HumidTextView);
        day4P = (TextView)findViewById(R.id.day4PressTextView);
        day5T = (TextView)findViewById(R.id.day5TempTextView);
        day5H = (TextView)findViewById(R.id.day5HumidTextView);
        day5P = (TextView)findViewById(R.id.day5PressTextView);
        day6T = (TextView)findViewById(R.id.day6TempTextView);
        day6H = (TextView)findViewById(R.id.day6HumidTextView);
        day6P = (TextView)findViewById(R.id.day6PressTextView);
    }
    public void instantiateIntArrays(){
        minT = new int[6];
        maxT = new int[6];
        minH = new int[6];
        maxH = new int[6];
        minP = new int[6];
        maxP = new int[6];
    }
    public void updateDateTextViews(String[] dates){
        day1.setText(past6Dates[0]);
        day2.setText(past6Dates[1]);
        day3.setText(past6Dates[2]);
        day4.setText(past6Dates[3]);
        day5.setText(past6Dates[4]);
        day6.setText(past6Dates[5]);
    }
}