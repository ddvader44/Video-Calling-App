package com.example.adouble;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {
    private CountryCodePicker ccp;
    private EditText phone;
    private EditText code;
    private Button continueButton;
    private String checker="" , phoneNumber="";
    private RelativeLayout relativeLayout;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private FirebaseAuth mAuth;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private ProgressDialog loadingBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        loadingBar = new ProgressDialog(this);
        phone = findViewById(R.id.phoneText);
        code = findViewById(R.id.codeText);
        continueButton = findViewById(R.id.continueNextButton);
        relativeLayout = findViewById(R.id.phoneAuth);
        ccp = (CountryCodePicker) findViewById(R.id.ccp);
        ccp.registerCarrierNumberEditText(phone);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(continueButton.getText().equals("Submit") || checker.equals("Code Sent"))
                {
                    String verificationCode = code.getText().toString();
                    if(verificationCode.equals(""))
                    {
                        Toast.makeText(LoginActivity.this, "Please write verification code", Toast.LENGTH_SHORT).show();
                    }else
                    {
                        loadingBar.setTitle("Code Verification");
                        loadingBar.setMessage("Please wait while we verify your code");
                        loadingBar.setCanceledOnTouchOutside(false);
                        loadingBar.show();
                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId,verificationCode);
                        signInWithPhoneAuthCredential(credential);
                    }
                }
                else
                {
                    phoneNumber = ccp.getFullNumberWithPlus();
                    if(!phoneNumber.equals(""))
                    {
                        loadingBar.setTitle("Phone Number Verification");
                        loadingBar.setMessage("Please wait while we verify your Phone Number");
                        loadingBar.setCanceledOnTouchOutside(false);
                        loadingBar.show();
                        PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber,60,TimeUnit.SECONDS,LoginActivity.this,callbacks);

                    }else
                    {
                        Toast.makeText(LoginActivity.this, "Please write valid Phone Number!", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });
        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Toast.makeText(LoginActivity.this, "Invalid Phone Number!", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
                relativeLayout.setVisibility(View.VISIBLE);
                continueButton.setText("Continue");
                code.setVisibility(View.GONE);
            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                mVerificationId = s;
                mResendToken = forceResendingToken;
                relativeLayout.setVisibility(View.GONE);
                checker = "Code Sent";
                continueButton.setText("Submit");
                code.setVisibility(View.VISIBLE);
                loadingBar.dismiss();
                Toast.makeText(LoginActivity.this, "Code has been sent!", Toast.LENGTH_SHORT).show();
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser != null)
        {
            Intent homeIntent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(homeIntent);
            finish();
        }
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            loadingBar.dismiss();
                            Toast.makeText(LoginActivity.this, "Congratulations! You are logged in!", Toast.LENGTH_SHORT).show();
                            nextActivity();
                        } else {
                            loadingBar.dismiss();
                            String error = task.getException().toString();
                            Toast.makeText(LoginActivity.this,"Error:- "+error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    private void nextActivity()
    {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }
}
