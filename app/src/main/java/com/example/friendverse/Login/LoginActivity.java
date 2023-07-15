package com.example.friendverse.Login;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.friendverse.DialogLoadingBar.LoadingDialog;
import com.example.friendverse.MainActivity;
import com.example.friendverse.Model.User;
import com.example.friendverse.R;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class LoginActivity extends AppCompatActivity {
    private TextView tvforgetPass;
    private Button loginButton;
    private ImageButton loginGoogleButton;
    //private ImageButton loginFacebookButton;
    private ImageButton phoneButton;
    private EditText usernameText;
    private EditText passwordText;
    private LoadingDialog loadingDialog = new LoadingDialog(this);
    private GoogleSignInClient googleSignInClient;
    private int flag = 0;

    private List<String> banUsersId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        tvforgetPass = findViewById(R.id.forgetPasstv);
        loginButton = findViewById(R.id.button);
        usernameText =findViewById(R.id.editText);
        passwordText = findViewById(R.id.editText2);
        phoneButton = findViewById(R.id.phoneLoginButton);
        loginGoogleButton = findViewById(R.id.googleButton);
        //loginFacebookButton = findViewById(R.id.facebookButton);

        mAuth = FirebaseAuth.getInstance();

        initList();

        tvforgetPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadingDialog.showDialog();
                openForgetPassword();

            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadingDialog.showDialog();
                loginUserWithEmail();

            }
        });

        phoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadingDialog.showDialog();
                startActivity(new Intent(LoginActivity.this, SignupPhoneActivity.class));
                loadingDialog.hideDialog();
            }
        });


        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("636293367764-fra1gmq1uu62kh0pdt8phqqvv2srcotm.apps.googleusercontent.com")
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, signInOptions);

        loginGoogleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadingDialog.showDialog();
                Intent intent = googleSignInClient.getSignInIntent();

                startActivityForResult(intent, 100);
            }
        });
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        // Check condition
        if (firebaseUser != null) {
            // When user already sign in redirect to profile activity
            startActivity(new Intent(LoginActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }


//        loginFacebookButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//            }
//        });
    }

    private void initList() {
        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference().child("Ban").child("Users");
        reference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                banUsersId = new ArrayList<>();
                for (DataSnapshot snapshot1: snapshot.getChildren()
                     ) {
                    banUsersId.add(snapshot1.getKey());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    public void openForgetPassword(){
        Intent intent = new Intent(getApplicationContext(), ForgetPasswordActivity.class);
        loadingDialog.hideDialog();
        startActivity(intent);
    }



    //------------------------Firebase-------------------------------------------------------------
    private FirebaseAuth mAuth;
    private DatabaseReference reference;

//    private void loginUserWithEmail(){
//        String email = usernameText.getText().toString();
//        String password = passwordText.getText().toString();
//        if(TextUtils.isEmpty(email)){
//            usernameText.setError("Username can't be empty");
//            usernameText.requestFocus();
//        }
//        else if(TextUtils.isEmpty(password)){
//            passwordText.setError("Password can't be empty");
//            passwordText.requestFocus();
//
//        }
//        else{
//            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//                @Override
//                public void onComplete(@NonNull Task<AuthResult> task) {
//                    if(task.isSuccessful()){
//                        Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
//                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
//                    }
//                    else{
//                        Toast.makeText(LoginActivity.this, "Login error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                }
//            });
//        }
//    }
    private void loginUserWithEmail() {
        //       register("a","Nguyen Van A","a@gmail.com","123456789");

        String email = usernameText.getText().toString();
        String password = passwordText.getText().toString();

    //        String email = "c@gmail.com";
    //        String password = "123456789";
        if(TextUtils.isEmpty(email)){
            usernameText.setError("Username can't be empty");
            usernameText.requestFocus();
            loadingDialog.hideDialog();
        }
        else if(TextUtils.isEmpty(password)){
            passwordText.setError("Password can't be empty");
            passwordText.requestFocus();
            loadingDialog.hideDialog();
        }
        else{
//            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//                @Override
//                public void onComplete(@NonNull Task<AuthResult> task) {
//                    if (task.isSuccessful()) {
//                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
//                        reference.child(User.ACTIVITYKEY).setValue(1);
//
//                        reference.addValueEventListener(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//
//                                User user = dataSnapshot.getValue(User.class);
//                                getCurrentUser = user;
//
//
//                            }
//
//                            @Override
//                            public void onCancelled(@NonNull DatabaseError databaseError) {
//                                reference.child(User.ACTIVITYKEY).setValue(0);
//
//                            }
//                        });
//                        Toast.makeText(LoginActivity.this, "", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            });

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){

                        flag = 0;

                        for (String s: banUsersId
                             ) {
                            if(s.equals(mAuth.getCurrentUser().getUid())){
                                Toast.makeText(LoginActivity.this, "This account is banned", Toast.LENGTH_SHORT).show();
                                loadingDialog.hideDialog();
                                FirebaseAuth.getInstance().signOut();
                                return;
                            }
                        }

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
                        reference.child(User.ACTIVITYKEY).setValue(1);

                        reference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

//                                User user = dataSnapshot.getValue(User.class);
//                                getCurrentUser = user;
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                reference.child(User.ACTIVITYKEY).setValue(0);

                            }
                        });
                        loadingDialog.hideDialog();

                        Toast.makeText(LoginActivity.this, "Login success", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finishAffinity();

                    }
                    else{
                        try
                        {
                            throw Objects.requireNonNull(task.getException());
                        }
                        // if user enters wrong email.
                        catch (FirebaseAuthInvalidUserException invalidEmail)
                        {

                            Toast.makeText(LoginActivity.this, "Invalid email", Toast.LENGTH_SHORT).show();
                            loadingDialog.hideDialog();
                            flag = 1;
                        }
                        // if user enters wrong password.
                        catch (FirebaseAuthInvalidCredentialsException wrongPassword)
                        {

                            Toast.makeText(LoginActivity.this, "Wrong password", Toast.LENGTH_SHORT).show();
                            loadingDialog.hideDialog();
                            flag = 1;
                        }
                        catch (Exception e)
                        {
                            Toast.makeText(LoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            loadingDialog.hideDialog();
                        }
                    }
                }
            });
        }


    }






    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {

            Task<GoogleSignInAccount> signInAccountTask = GoogleSignIn.getSignedInAccountFromIntent(data);

            if (signInAccountTask.isSuccessful()) {



                try {

                    GoogleSignInAccount googleSignInAccount = signInAccountTask.getResult(ApiException.class);

                    if (googleSignInAccount != null) {

                        AuthCredential authCredential = GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(), null);

                        mAuth.signInWithCredential(authCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if (task.isSuccessful()) {
                                    // When task is successful redirect to profile activity display Toast

                                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                    String userid = firebaseUser.getUid();

                                    for (String s: banUsersId
                                    ) {
                                        if(s.equals(userid)){
                                            googleSignInClient = GoogleSignIn.getClient(LoginActivity.this, GoogleSignInOptions.DEFAULT_SIGN_IN);
                                            Toast.makeText(LoginActivity.this, "This account is banned", Toast.LENGTH_SHORT).show();
                                            loadingDialog.hideDialog();
                                            FirebaseAuth.getInstance().signOut();
                                            googleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){

                                                    }
                                                }
                                            });
                                            return;
                                        }
                                    }

                                    reference = FirebaseDatabase.getInstance().getReference().child("Users").child(userid);
                                    reference.child(User.ACTIVITYKEY).setValue(1);
                                    String s = "Google sign in successful";

                                    displayToast(s);
                                    reference.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

//                                            User user = dataSnapshot.getValue(User.class);
//                                            if(user != null){
//                                                getCurrentUser = user;
//                                            }



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
                                                loadingDialog.hideDialog();
                                                startActivity(new Intent(LoginActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                                                finishAffinity();
                                            }
                                            else{
                                                reference = FirebaseDatabase.getInstance().getReference().child("Users").child(userid);


                                                HashMap<String, Object> hashMap = new HashMap<>();
                                                hashMap.put("id", userid);
                                                hashMap.put("username", userid);
                                                hashMap.put("fullname", firebaseUser.getDisplayName());
                                                hashMap.put("bio", "");
                                                hashMap.put("imageurl", "default");
                                                hashMap.put("email", firebaseUser.getEmail());
                                                hashMap.put("website", "");
                                                hashMap.put("phonenumber", "");
                                                hashMap.put(User.ACTIVITYKEY, "1");


                                                reference.setValue(hashMap);

                                                reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                    }
                                                });

                                                initTokenCall();
                                                Toast.makeText(LoginActivity.this, "This is your first time signin. Please restart app!", Toast.LENGTH_SHORT).show();

                                                Timer timer = new Timer();
                                                timer.schedule(new TimerTask() {
                                                    @Override
                                                    public void run() {

//                                            loadingDialog.hideDialog();
//                                            startActivity(new Intent(LoginActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                                                        finishAffinity();
                                                        System.exit(0);
                                                    }
                                                }, 4*1000);
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
                                            if(snapshot.hasChild("fullname")){
                                                loadingDialog.hideDialog();
                                                startActivity(new Intent(LoginActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                                                finishAffinity();
                                            }
                                            else{
                                                //reference = FirebaseDatabase.getInstance().getReference().child("Users").child(userid);


                                                HashMap<String, Object> hashMap = new HashMap<>();
                                                hashMap.put("id", userid);
                                                hashMap.put("username", userid);
                                                hashMap.put("fullname", firebaseUser.getDisplayName());
                                                hashMap.put("bio", "");
                                                hashMap.put("imageurl", "default");
                                                hashMap.put("email", firebaseUser.getEmail());
                                                hashMap.put("website", "");
                                                hashMap.put("phonenumber", "");
                                                hashMap.put(User.ACTIVITYKEY, "1");


                                                reference.setValue(hashMap);

                                                reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                    }
                                                });
                                                initTokenCall();
                                                Toast.makeText(LoginActivity.this, "This is your first time signin. Please restart app!", Toast.LENGTH_SHORT).show();

                                                Timer timer = new Timer();
                                                timer.schedule(new TimerTask() {
                                                    @Override
                                                    public void run() {

//                                            loadingDialog.hideDialog();
//                                            startActivity(new Intent(LoginActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                                                        finishAffinity();
                                                        System.exit(0);
                                                    }
                                                }, 4*1000);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });



                                    //displayToast("Firebase authentication successful");
                                } else {
                                    // When task is unsuccessful display Toast
                                    displayToast("Authentication Failed :" + task.getException().getMessage());
                                    loadingDialog.hideDialog();
                                }
                            }
                        });
                    }
                } catch (ApiException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void displayToast(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    public static User getCurrentUser;

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