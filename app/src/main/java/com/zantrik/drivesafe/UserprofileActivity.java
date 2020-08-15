package com.zantrik.drivesafe;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserprofileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;

    EditText NameText;
    EditText NidNumText;
    String name, nidNum;
    TextView feedbackText;

    private Button savebtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

        setContentView(R.layout.activity_userprofile);
        NameText = findViewById(R.id.NameText);
        NidNumText = findViewById(R.id.NidText);
        feedbackText = findViewById(R.id.profile_form_feedback);
        savebtn = findViewById(R.id.save_btn);

        feedbackText.setVisibility(View.INVISIBLE);

        getSupportActionBar().setTitle("User Profile");
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
            getUserProfile();
        }
    }

    private void sendUserToLogin() {
        Intent loginIntent = new Intent(UserprofileActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    private void getUserProfile(){
        feedbackText.setVisibility(View.VISIBLE);
        String Phone = (Singleton.instance().fetchValueString("phone"));
        int userid = Integer.parseInt(Singleton.instance().fetchValueString("userid"));

        Api.getClient().getProfile(Phone, userid).enqueue(new Callback<GetprofileReq>() {
            @SuppressLint("LongLogTag")
            @Override
            public void onResponse(Call<GetprofileReq> call, Response<GetprofileReq> response) {
                if(!response.isSuccessful()){
                    feedbackText.setText("code:" + String.valueOf(response.code()));
                    return;
                }
                if(response.body().getData()!= null) {
                    GetprofileRes getprofileRes = response.body().getData();
                    if(getprofileRes.Success){
                        Getprofileinfo getprofileinfo = getprofileRes.userprofile;
                        feedbackText.setText("");
                        NameText.setText(getprofileinfo.Name);
                        NidNumText.setText(getprofileinfo.NidNum);
                    }
                }
                else {
                    feedbackText.setText("no user found");
                }
            }

            @Override
            public void onFailure(Call<GetprofileReq> call, Throwable t) {
                feedbackText.setText("error" + t.getMessage());
            }
        });
    }

    public void POSTDATA(View view) {
        updateUserProfile();
    }

    private void updateUserProfile(){
        feedbackText.setVisibility(View.VISIBLE);
        String Phone = (Singleton.instance().fetchValueString("phone"));
        int userid = Integer.parseInt(Singleton.instance().fetchValueString("userid"));
        String Name = NameText.getText().toString();
        String NidNum = NidNumText.getText().toString();

        Api.getClient().updateProfile(Phone, userid, Name, NidNum).enqueue(new Callback<UpdateprofileReq>() {
            @Override
            public void onResponse(Call<UpdateprofileReq> call, Response<UpdateprofileReq> response) {
                if(!response.isSuccessful()){
                    feedbackText.setText("code:" + String.valueOf(response.code()));
                    return;
                }
                if(response.body().getData()!= null) {
                    UpdateprofileRes updateprofileRes = response.body().getData();
                    if(updateprofileRes.Success){
                        feedbackText.setText("profile updated");
                    }
                    else{
                        feedbackText.setText("profile updated failed");
                    }
                }
                else {
                    feedbackText.setText("no user found");
                }
            }

            @Override
            public void onFailure(Call<UpdateprofileReq> call, Throwable t) {
                feedbackText.setText("error" + t.getMessage());
            }
        });
    }
}