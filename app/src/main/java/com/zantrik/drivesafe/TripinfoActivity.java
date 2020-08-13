package com.zantrik.drivesafe;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class TripinfoActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private TextView textViewResult;
    TextView feedbackText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tripinfo);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

        feedbackText = findViewById(R.id.trip_feedback);
        textViewResult = findViewById(R.id.triplist_result);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        if(mCurrentUser == null){
//            String idToken = mCurrentUser.getProviderId();
//            createPost(idToken);
            sendUserToLogin();
        }
        else{
            getTripinfo();
        }
    }

    private void getTripinfo(){
        textViewResult.setText("");
        feedbackText.setVisibility(View.VISIBLE);
        feedbackText.setText("");
        String Phone = (Singleton.instance().fetchValueString("phone"));
        int userid = Integer.parseInt(Singleton.instance().fetchValueString("userid"));
        Api.getClient().getTripinfo(Phone, userid).enqueue(new Callback<GettripinfoReq>() {
            @SuppressLint("LongLogTag")
            @Override
            public void onResponse(Call<GettripinfoReq> call, Response<GettripinfoReq> response) {
                if(!response.isSuccessful()){
                    feedbackText.setText("code:" + String.valueOf(response.code()));
                    return;
                }
                if(response.body().getData()!= null) {
                    GetprofileRes getprofileRes = response.body().getData();
                    if(getprofileRes.Success){
                        TripinfoList[] tripinfoLists = getprofileRes.tripinfo;
                        //feedbackText.setText("Length:" + tripinfoLists.length);
                        if(tripinfoLists.length==0){
                            textViewResult.setText("No Trip Found");
                        }
                        else{
                            Collections.reverse(Arrays.asList(tripinfoLists));
                            int count=1;
                            for (TripinfoList trip : tripinfoLists) {
                                String content = "";
                                content += "Date: " + trip.tripdatetime + "\n";
                                content += "Trip Length: " + trip.triplength + "hour" + "\n";
                                content += "Score: " + trip.tripscore + "\n\n";
                                textViewResult.append(content);
                                if(count==3){
                                    return;
                                }
                                count++;
                            }
                        }
                    }
                    else{
                        feedbackText.setText("no user found");
                    }
                }
                else {
                    feedbackText.setText("no user found");
                }
            }

            @Override
            public void onFailure(Call<GettripinfoReq> call, Throwable t) {
                feedbackText.setText("error" + t.getMessage());
            }
        });
    }

    public void CANCELDATA(View view) {
        sendUserToHome();
    }

    private void sendUserToHome() {
        Intent homeIntent = new Intent(TripinfoActivity.this, MainActivity.class);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(homeIntent);
        finish();
    }

    private void sendUserToLogin() {
        Intent loginIntent = new Intent(TripinfoActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    public void SAVETRIP(View view) {

        Random r = new Random();
        double randomValue1 = 3 + (10 - 3) * r.nextDouble();
        double randomValue2 = 0 + (10 - 0) * r.nextDouble();

        textViewResult.setText("");
        feedbackText.setVisibility(View.VISIBLE);
        feedbackText.setText("");
        String Phone = (Singleton.instance().fetchValueString("phone"));
        int userid = Integer.parseInt(Singleton.instance().fetchValueString("userid"));
        int numDrowsiness= 9;
        int numPhone=5;
        double tripscore=randomValue1;
        double triplength=randomValue2;

        Api.getClient().updateTripinfo(Phone, userid, numDrowsiness, numPhone, tripscore, triplength).enqueue(new Callback<UpdatetripinfoReq>() {
            @SuppressLint("LongLogTag")
            @Override
            public void onResponse(Call<UpdatetripinfoReq> call, Response<UpdatetripinfoReq> response) {
                if(!response.isSuccessful()){
                    feedbackText.setText("code:" + String.valueOf(response.code()));
                    return;
                }
                if(response.body().getData()!= null) {
                    UpdateprofileRes updateprofileRes = response.body().getData();
                    if(updateprofileRes.Success){
                        feedbackText.setText("Added a new Trip");
                        getTripinfo();
                    }
                    else{
                        feedbackText.setText("no user found");
                    }
                }
                else {
                    feedbackText.setText("no user found");
                }
            }

            @Override
            public void onFailure(Call<UpdatetripinfoReq> call, Throwable t) {
                feedbackText.setText("error" + t.getMessage());
            }
        });
    }


}