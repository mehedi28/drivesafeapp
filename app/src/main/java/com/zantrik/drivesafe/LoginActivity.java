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
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;

    private String mCountryCode;
    private EditText mPhoneNumber;

    private Button mGenerateBtn;
    private ProgressBar mLoginProgress;

    private TextView mLoginFeedbackText;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

        mCountryCode = "88";
        mPhoneNumber = findViewById(R.id.phone_number_text);
        mGenerateBtn = findViewById(R.id.generate_otp);
        mLoginProgress = findViewById(R.id.login_progress_bar);
        mLoginFeedbackText = findViewById(R.id.login_form_feedback);

        mLoginProgress.setVisibility(View.INVISIBLE);
        mLoginFeedbackText.setVisibility(View.INVISIBLE);



        mGenerateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String country_code = mCountryCode;
                String phone_number = mPhoneNumber.getText().toString();

                String complete_phone_number = "+" + country_code + phone_number;

                mLoginFeedbackText.setVisibility(View.INVISIBLE);

                if (country_code.isEmpty() || phone_number.isEmpty()) {
                    mLoginFeedbackText.setText("Please enter your phone number.");
                    mLoginFeedbackText.setVisibility(View.VISIBLE);
                } else {

                    Singleton.instance().storeValueString("phone", complete_phone_number);
                    mLoginProgress.setVisibility(View.VISIBLE);
                    mGenerateBtn.setEnabled(false);

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            complete_phone_number,
                            60,
                            TimeUnit.SECONDS,
                            LoginActivity.this,
                            mCallbacks
                    );

                }

            }
        });


        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                mLoginFeedbackText.setText("Verification Failed, please try again.");
                mLoginFeedbackText.setVisibility(View.VISIBLE);
                mLoginProgress.setVisibility(View.INVISIBLE);
                mGenerateBtn.setEnabled(true);
            }

            @Override
            public void onCodeSent(final String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                mLoginProgress.setVisibility(View.INVISIBLE);
                mGenerateBtn.setEnabled(true);
                Intent otpIntent = new Intent(LoginActivity.this, OtpActivity.class);
                otpIntent.putExtra("AuthCredentials", s);
                startActivity(otpIntent);
            }
        };
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

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
                            mUser.getIdToken(true)
                                    .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                                        public void onComplete(@NonNull Task<GetTokenResult> task) {
                                            if (task.isSuccessful()) {
                                                //String idToken = task.getResult().getToken();
                                                //
                                                //Log.d("newToken", idToken);
                                                createPost();
                                                // ...
                                                //sendUserToHome();
                                            } else {
                                                mLoginFeedbackText.setVisibility(View.VISIBLE);
                                                mLoginFeedbackText.setText("There was an error to sign in");
                                                // Handle error -> task.getException();
                                            }
                                        }
                                    });

                            //sendUserToHome();
                            // ...
                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                mLoginFeedbackText.setVisibility(View.VISIBLE);
                                mLoginFeedbackText.setText("There was an error verifying OTP");
                            }
                        }

                    }
                });
    }

    private void sendUserToHome() {
        mLoginProgress.setVisibility(View.INVISIBLE);
        mGenerateBtn.setEnabled(true);

        Intent homeIntent = new Intent(LoginActivity.this, MainActivity.class);
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
