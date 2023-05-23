package com.example.friendverse.Fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.friendverse.Login.PhoneConfirmActivity;
import com.example.friendverse.MainActivity;
import com.example.friendverse.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;


public class PhoneFragment extends Fragment {

    private static final String TAG = PhoneFragment.class.getName();
    private Button buttonNext;
    private EditText etPhoneNum;
    private String verificationID;
    private String codeOTP;
    ProgressBar progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_phone, container, false);
        buttonNext = v.findViewById(R.id.buttonNextPhone);
        etPhoneNum = v.findViewById(R.id.phoneEditText);
        progressBar = v.findViewById(R.id.bar);

        mAuth = FirebaseAuth.getInstance();

        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(TextUtils.isEmpty(etPhoneNum.getText().toString())){
                    Toast.makeText(getActivity(), "Invalid Phone number!", Toast.LENGTH_SHORT).show();
                }
                else{
                    String number = etPhoneNum.getText().toString();
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

        return v;
    }

    private FirebaseAuth mAuth;

    public void sendVerifyOTP(String phoneNum){

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber("+84" + phoneNum)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this.getActivity())                 // (optional) Activity for callback binding
                        // If no activity is passed, reCAPTCHA verification can not be used.
                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                                signInWithPhoneAuthCredential(phoneAuthCredential);
                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {
                                Toast.makeText(getActivity(), "Verification failed", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                super.onCodeSent(s, forceResendingToken);
                                goToConfirmActivity(phoneNum, s);
                            }
                        })          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }


    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this.getActivity(), new OnCompleteListener<AuthResult>() {
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
                                Toast.makeText(getActivity(), "The verification code entered was invalid", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    public void goToConfirmActivity(String phoneNum, String s){
        Intent intent = new Intent(getActivity(), PhoneConfirmActivity.class);
        intent.putExtra("phone_number", phoneNum);
        intent.putExtra("verification_ID", s);
        startActivity(intent);
    }
    public void goToMainActivity(String phoneNum){
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.putExtra("phone_number", phoneNum);
        startActivity(intent);
    }

//    private PhoneAuthProvider.OnVerificationStateChangedCallbacks
//    mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
//
//        @Override
//        public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
//
//            final String code = credential.getSmsCode();
//            if(code != null){
//                verifycode(code);
//                codeOTP = code;
//            }
//
//        }
//
//        @Override
//        public void onVerificationFailed(@NonNull FirebaseException e) {
//
//
//            Toast.makeText(getActivity(), "Verification failed!", Toast.LENGTH_SHORT).show();
//        }
//
//        @Override
//        public void onCodeSent(@NonNull String verificationId,
//                @NonNull PhoneAuthProvider.ForceResendingToken token) {
//            super.onCodeSent(verificationId, token);
//            verificationID = verificationId;
//            Toast.makeText(getActivity(), "Code sent!", Toast.LENGTH_SHORT).show();
//            progressBar.setVisibility(View.INVISIBLE);
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
//                    Toast.makeText(getActivity(), "Login successful", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
//    }
}