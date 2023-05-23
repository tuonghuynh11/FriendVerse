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
import android.widget.TextView;
import android.widget.Toast;

import com.example.friendverse.DialogLoadingBar.LoadingDialog;
import com.example.friendverse.Fragment.PhoneFragment;
import com.example.friendverse.MainActivity;
import com.example.friendverse.Model.User;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class PhoneConfirmActivity extends AppCompatActivity {

    private static final String TAG = PhoneConfirmActivity.class.getName();
    private Button buttonNext;
    private TextView tvLogin;
    private TextView tvRequest;
    private String verificationID;
    private String phoneNum;
    private EditText etOTP;
    private LoadingDialog loadingDialog = new LoadingDialog(this);
    private PhoneAuthProvider.ForceResendingToken mForceResending;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_confirm);

        buttonNext = findViewById(R.id.buttonNextPhoneOTP);
        tvLogin = findViewById(R.id.tvLoginPhone);
        tvRequest = findViewById(R.id.textViewRequestPhone);
        etOTP = findViewById(R.id.etverifyPhone);

        mAuth = FirebaseAuth.getInstance();

        Intent intent1 = getIntent();
        verificationID = intent1.getExtras().getString("verification_ID");
        phoneNum = intent1.getExtras().getString("phone_number");

        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadingDialog.showDialog();
//                Intent intent = new Intent(getApplicationContext(), SignupInfoActivity.class);
//                startActivity(intent);
//                if(TextUtils.isEmpty(etOTP.getText().toString())){
//                    Toast.makeText(PhoneConfirmActivity.this, "Wrong OTP", Toast.LENGTH_SHORT).show();
//                }
//                else{
//                    verifycode(etOTP.getText().toString());
//                }
                if(TextUtils.isEmpty(etOTP.getText().toString())){
                    Toast.makeText(PhoneConfirmActivity.this, "Wrong OTP", Toast.LENGTH_SHORT).show();
                    loadingDialog.hideDialog();
                }
                else
                    confirmOTP(etOTP.getText().toString());
            }
        });
        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadingDialog.showDialog();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                loadingDialog.hideDialog();
            }
        });
        tvRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadingDialog.showDialog();
                sendOTPAgain();
            }
        });
    }


    private FirebaseAuth mAuth;
    private DatabaseReference reference;

    public void confirmOTP(String strOTP){
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationID, strOTP);
        signInWithPhoneAuthCredential(credential);

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

                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            String userid = firebaseUser.getUid();

                            reference = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
                            reference.child(User.ACTIVITYKEY).setValue(1);

                            reference.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    User user = dataSnapshot.getValue(User.class);
                                    LoginActivity.getCurrentUser = user;


                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    reference.child(User.ACTIVITYKEY).setValue(0);

                                }
                            });

                            reference = FirebaseDatabase.getInstance().getReference().child("Users");

                            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.hasChild(userid)){

                                    }
                                    else{
                                        reference = FirebaseDatabase.getInstance().getReference().child("Users").child(userid);


                                        HashMap<String, Object> hashMap = new HashMap<>();
                                        hashMap.put("id", userid);
                                        hashMap.put("username", "");
                                        hashMap.put("fullname", firebaseUser.getDisplayName());
                                        hashMap.put("bio", "");
                                        hashMap.put("imageurl", "default");
                                        hashMap.put("email", "");
                                        hashMap.put("website", "");
                                        hashMap.put("phonenumber", "0" + phoneNum);
                                        hashMap.put(User.ACTIVITYKEY, "1");


                                        reference.setValue(hashMap);

                                        reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                            }
                                        });

                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });





                            reference = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());

                            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.hasChild("phonenumber")){

                                    }
                                    else{
                                        reference = FirebaseDatabase.getInstance().getReference().child("Users").child(userid);


                                        HashMap<String, Object> hashMap = new HashMap<>();
                                        hashMap.put("id", userid);
                                        hashMap.put("username", "");
                                        hashMap.put("fullname", firebaseUser.getDisplayName());
                                        hashMap.put("bio", "");
                                        hashMap.put("imageurl", "default");
                                        hashMap.put("email", "");
                                        hashMap.put("website", "");
                                        hashMap.put("phonenumber", "0" + phoneNum);
                                        hashMap.put(User.ACTIVITYKEY, "1");


                                        reference.setValue(hashMap);

                                        reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                            }
                                        });

                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                            // Update UI
                            goToMainActivity(user.getPhoneNumber());
                            finishAffinity();
                            loadingDialog.hideDialog();
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                Toast.makeText(PhoneConfirmActivity.this, verificationID, Toast.LENGTH_SHORT).show();
                                Toast.makeText(PhoneConfirmActivity.this, "The verification code entered was invalid", Toast.LENGTH_SHORT).show();
                                loadingDialog.hideDialog();
                            }
                        }
                    }
                });
    }
    public void goToMainActivity(String phoneNum){
        Intent intent = new Intent(PhoneConfirmActivity.this, MainActivity.class);
        intent.putExtra("phone_number", phoneNum);
        startActivity(intent);
    }

    public void sendOTPAgain(){
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber("+84" + phoneNum)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // (optional) Activity for callback binding
                        // If no activity is passed, reCAPTCHA verification can not be used.
                        .setForceResendingToken(mForceResending)
                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                                signInWithPhoneAuthCredential(phoneAuthCredential);
                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {
                                Toast.makeText(PhoneConfirmActivity.this, "Verification failed", Toast.LENGTH_SHORT).show();
                                loadingDialog.hideDialog();
                            }

                            @Override
                            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                super.onCodeSent(s, forceResendingToken);
                                verificationID = s;
                                mForceResending = forceResendingToken;
                                loadingDialog.hideDialog();
                            }
                        })          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

//    public void sendVerifyOTP(String phoneNum){
//
//        PhoneAuthOptions options =
//                PhoneAuthOptions.newBuilder(mAuth)
//                        .setPhoneNumber("+84" + phoneNum)       // Phone number to verify
//                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
//                        .setActivity(this)                 // (optional) Activity for callback binding
//                        // If no activity is passed, reCAPTCHA verification can not be used.
//                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
//                        .build();
//        PhoneAuthProvider.verifyPhoneNumber(options);
//    }
//
//    private PhoneAuthProvider.OnVerificationStateChangedCallbacks
//            mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
//
//        @Override
//        public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
//
//            final String code = credential.getSmsCode();
//            if(code != null){
//                verifycode(code);
//            }
//
//        }
//
//        @Override
//        public void onVerificationFailed(@NonNull FirebaseException e) {
//
//
//            Toast.makeText(PhoneConfirmActivity.this, "Verification failed!", Toast.LENGTH_SHORT).show();
//        }
//
//        @Override
//        public void onCodeSent(@NonNull String verificationId,
//                               @NonNull PhoneAuthProvider.ForceResendingToken token) {
//            super.onCodeSent(verificationId, token);
//            verificationID = verificationId;
//            Toast.makeText(PhoneConfirmActivity.this, "Code sent!", Toast.LENGTH_SHORT).show();
//            //progressBar.setVisibility(View.INVISIBLE);
//        }
//    };
//    public void verifycode(String Code){
//        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationID, Code);
//        signinByCredentials(credential);
//    }
//
//    private void signinByCredentials(PhoneAuthCredential credential) {
//        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
//        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//            @Override
//            public void onComplete(@NonNull Task<AuthResult> task) {
//                if(task.isSuccessful()){
//                    Toast.makeText(PhoneConfirmActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
//                    startActivity(new Intent(PhoneConfirmActivity.this, MainActivity.class));
//                }
//            }
//        });
//    }
}