package com.example.friendverse.Login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.friendverse.DialogLoadingBar.LoadingDialog;
import com.example.friendverse.Model.User;
import com.example.friendverse.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Objects;

public class SignupInfoActivity extends AppCompatActivity {
    private Button buttonContinue;
    private EditText etName;
    private EditText etPassword;
    private EditText etRePassword;
    private LoadingDialog loadingDialog = new LoadingDialog(this);
    private int flag = 0;
    String email;
    String uid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_info);

        Intent intent1 = getIntent();
        email = intent1.getExtras().getString("Email1");

        buttonContinue = findViewById(R.id.buttonContinue);
        etName = findViewById(R.id.editTextName);
        etPassword = findViewById(R.id.editTextPassword);
        etRePassword = findViewById(R.id.editTextPasswordAgain);
        mAuth = FirebaseAuth.getInstance();

        buttonContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadingDialog.showDialog();
                if(TextUtils.isEmpty(etName.getText().toString())){
                    etName.setError("Name can't be empty");
                    etName.requestFocus();
                    loadingDialog.hideDialog();
                }
                else if(TextUtils.isEmpty(etPassword.getText().toString())){
                    etPassword.setError("Password can't be empty");
                    etPassword.requestFocus();
                    loadingDialog.hideDialog();
                }
                else if(TextUtils.isEmpty(etRePassword.getText().toString())){
                    etRePassword.setError("Re-Password can't be empty");
                    etRePassword.requestFocus();
                    loadingDialog.hideDialog();
                }
                else if(etPassword.getText().toString().length() < 8){
                    Toast.makeText(SignupInfoActivity.this, "Password must be at least 8 character!", Toast.LENGTH_SHORT).show();
                    loadingDialog.hideDialog();
                }
                else if(!etRePassword.getText().toString().equals(etPassword.getText().toString())){

                    Toast.makeText(SignupInfoActivity.this, "Password and Re-Password must be the same!", Toast.LENGTH_SHORT).show();
                    loadingDialog.hideDialog();
                }
                else{
                    try{
                        registerWithEmailAndPassword("", etName.getText().toString(), email, etPassword.getText().toString());



                    }
                    catch (Exception e){}

                }


            }
        });
    }


    private FirebaseAuth mAuth;
    private DatabaseReference reference;

    private void registerWithEmailAndPassword(final String username, final String fullname, String email, String password) {
//        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//            @Override
//            public void onComplete(@NonNull Task<AuthResult> task) {
//                if (task.isSuccessful()) {
//
//                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
//                    String userid = firebaseUser.getUid();
//                    uid = userid;
//                    reference = FirebaseDatabase.getInstance().getReference().child("Users").child(userid);
//
//
//                    HashMap<String, Object> hashMap = new HashMap<>();
//                    hashMap.put("id", userid);
//                    hashMap.put("username", username.toLowerCase());
//                    hashMap.put("fullname", fullname);
//                    hashMap.put("bio", "");
//                    hashMap.put("imageurl", "default");
//
//                    reference.setValue(hashMap);
//
//                    reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
//                        @Override
//                        public void onComplete(@NonNull Task<Void> task) {
//
//                        }
//                    });
//
//                }
//            }
//        });
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                    String userid = firebaseUser.getUid();
                    uid = userid;
                    reference = FirebaseDatabase.getInstance().getReference().child("Users").child(userid);


                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("id", userid);
                    hashMap.put("username", username.toLowerCase());
                    hashMap.put("fullname", fullname);
                    hashMap.put("bio", "");
                    hashMap.put("imageurl", "default");
                    hashMap.put("email", email);
                    hashMap.put("website", "");
                    hashMap.put("phonenumber", "");

                    reference.setValue(hashMap);

                    reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                        }
                    });

                    initTokenCall();

                    Toast.makeText(SignupInfoActivity.this, "success", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), SignupFinishActivity.class);
                    intent.putExtra("UID", uid);
                    loadingDialog.hideDialog();
                    startActivity(intent);
                    finishAffinity();
                }
                else{
                    try
                    {
                        throw Objects.requireNonNull(task.getException());
                    }
                    // if user enters wrong email.
                    catch (FirebaseAuthWeakPasswordException weakPassword)
                    {
                        Toast.makeText(SignupInfoActivity.this, "Weak password", Toast.LENGTH_SHORT).show();
                        flag = 1;
                        loadingDialog.hideDialog();
                    }
                    // if user enters wrong password.
                    catch (FirebaseAuthInvalidCredentialsException malformedEmail)
                    {
                        Toast.makeText(SignupInfoActivity.this, "Malformed email", Toast.LENGTH_SHORT).show();
                        flag = 1;
                        loadingDialog.hideDialog();
                    }
                    catch (FirebaseAuthUserCollisionException existEmail)
                    {
                        Toast.makeText(SignupInfoActivity.this, "Exist email", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SignupInfoActivity.this, SignupActivity.class));
                        flag = 1;
                        loadingDialog.hideDialog();
                    }
                    catch (Exception e)
                    {
                        Toast.makeText(SignupInfoActivity.this, "Error: "+ e.getMessage(), Toast.LENGTH_SHORT).show();
                        flag = 1;
                        loadingDialog.hideDialog();
                    }
                }
            }
        });


    }
    public void initTokenCall() {
        runOnUiThread(() -> {
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                @Override
                public void onComplete(@NonNull Task<String> task) {
                    if (task.isSuccessful()) {
                        String deviceToken = task.getResult();

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
                        reference.child(User.TOKENKEY).setValue(deviceToken);
                    }
                }
            });
        });

    }
}