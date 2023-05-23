package com.example.friendverse.Login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.friendverse.DialogLoadingBar.LoadingDialog;
import com.example.friendverse.MainActivity;
import com.example.friendverse.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class SignupPhoneActivity extends AppCompatActivity {

    private static final String TAG = SignupPhoneActivity.class.getName();
    private Button nextButton;
    private EditText etPhone;
    private LoadingDialog loadingDialog = new LoadingDialog(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_phone);

        nextButton = findViewById(R.id.buttonNextPhone2);
        etPhone = findViewById(R.id.phoneEditText2);

        mAuth = FirebaseAuth.getInstance();

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(TextUtils.isEmpty(etPhone.getText().toString())){
                    loadingDialog.showDialog();
                    etPhone.setError("Phone number can't be empty");
                    etPhone.requestFocus();
//                    Toast.makeText(SignupPhoneActivity.this, "Invalid Phone number!", Toast.LENGTH_SHORT).show();
                    loadingDialog.hideDialog();
                }
                else{
                    String number = etPhone.getText().toString();
                    loadingDialog.showDialog();
//                    progressBar.setVisibility(View.VISIBLE);
//                    sendVerifyOTP(number);
//                    Intent intent = new Intent(getActivity(), PhoneConfirmActivity.class);
//                    intent.putExtra("veriID", verificationID);
//                    intent.putExtra("code", codeOTP);
//                    startActivity(intent);
                    sendVerifyOTP(number);
                }
            }
        });
    }



    private FirebaseAuth mAuth;

    public void sendVerifyOTP(String phoneNum){

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber("+84" + phoneNum)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // (optional) Activity for callback binding
                        // If no activity is passed, reCAPTCHA verification can not be used.
                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                                signInWithPhoneAuthCredential(phoneAuthCredential);
                                loadingDialog.hideDialog();
                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {
                                Toast.makeText(SignupPhoneActivity.this, "Verification failed", Toast.LENGTH_SHORT).show();
                                loadingDialog.hideDialog();
                            }

                            @Override
                            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                super.onCodeSent(s, forceResendingToken);
                                goToConfirmActivity(phoneNum, s);
                                loadingDialog.hideDialog();
                            }
                        })          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }


    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.e(TAG, "signInWithCredential:success");

                            FirebaseUser user = task.getResult().getUser();
                            // Update UI
                            goToMainActivity(user.getPhoneNumber());
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                Toast.makeText(SignupPhoneActivity.this, "The verification code entered was invalid", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    public void goToConfirmActivity(String phoneNum, String s){
        Intent intent = new Intent(SignupPhoneActivity.this, PhoneConfirmActivity.class);
        intent.putExtra("phone_number", phoneNum);
        intent.putExtra("verification_ID", s);
        startActivity(intent);
    }
    public void goToMainActivity(String phoneNum){
        Intent intent = new Intent(SignupPhoneActivity.this, MainActivity.class);
        intent.putExtra("phone_number", phoneNum);
        startActivity(intent);
    }
}