package com.zantrik.drivesafe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OtpActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;

    private String mAuthVerificationId;

    private EditText mOtpText;
    private Button mVerifyBtn;

    private ProgressBar mOtpProgress;

    private TextView mOtpFeedback;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

        mAuthVerificationId = getIntent().getStringExtra("AuthCredentials");

        mOtpFeedback = findViewById(R.id.otp_form_feedback);
        mOtpProgress = findViewById(R.id.opt_progress_bar);
        mOtpText = findViewById(R.id.otpText);

        mVerifyBtn = findViewById(R.id.OTPbtn);

        mVerifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String otp = mOtpText.getText().toString();

                mOtpFeedback.setVisibility(View.INVISIBLE);

                if(otp.isEmpty()){

                    mOtpFeedback.setVisibility(View.VISIBLE);
                    mOtpFeedback.setText("Please enter otp");

                } else {

                    mOtpProgress.setVisibility(View.VISIBLE);
                    mVerifyBtn.setEnabled(false);

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mAuthVerificationId, otp);
                    signInWithPhoneAuthCredential(credential);
                }

            }
        });
    }

    private void signInWithPhoneAuthCredential(final PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(OtpActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
                            mUser.getIdToken(true)
                                    .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                                        public void onComplete(@NonNull Task<GetTokenResult> task) {
                                            if (task.isSuccessful()) {
                                                String idToken = task.getResult().getToken();
                                                //Log.d("newToken", idToken);
                                                createPost();
                                                // ...
                                                //sendUserToHome();
                                            } else {
                                                // Handle error -> task.getException();

                                            }
                                        }
                                    });
                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                mOtpFeedback.setVisibility(View.VISIBLE);
                                mOtpFeedback.setText("There was an error verifying OTP");
                            }

                            mOtpProgress.setVisibility(View.INVISIBLE);
                            mVerifyBtn.setEnabled(true);
                        }
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        if(mCurrentUser != null){
//            String idToken = mCurrentUser.getProviderId();
//            createPost(idToken);
            sendUserToHome();
        }
    }

    public void sendUserToHome() {
        mOtpProgress.setVisibility(View.INVISIBLE);
        mVerifyBtn.setEnabled(true);
        Intent homeIntent = new Intent(OtpActivity.this, MainActivity.class);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(homeIntent);
        finish();
    }

    private void createPost() {
        String Phone = (Singleton.instance().fetchValueString("phone"));
        Api.getClient().createPost(Phone).enqueue(new Callback<LoginReq>() {
            @SuppressLint("LongLogTag")
            @Override
            public void onResponse(Call<LoginReq> call, Response<LoginReq> response) {
                if(!response.isSuccessful()){
                    //Log.d("********isSuccessful*************:", String.valueOf(response.code()));
                    return;
                }
                if(response.body().getData()!= null) {
                    LoginRes result = response.body().getData();
                    if(result.Success){
                        Singleton.instance().storeValueString("userid", String.valueOf(result.userid));
                        sendUserToHome();
                    }
                }
                else {
                    Log.d("********isSuccessful*************:", "No user found");
                }
            }
            @SuppressLint("LongLogTag")
            @Override
            public void onFailure(Call<LoginReq> call, Throwable t) {
                Log.d("********error*************::", t.getMessage());
            }
        });
    }
}
