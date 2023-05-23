package com.example.friendverse;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    Button button;
    EditText emailText;
    EditText PasswordText;
    FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.button2);
        emailText = findViewById(R.id.email);
        PasswordText = findViewById(R.id.password);
        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if(user != null){
            Intent i = new Intent(getApplicationContext(), StoryActivity.class);
            i.putExtra("userid", user.getUid());
            startActivity(i);
        }
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = String.valueOf(emailText.getText());
                String password = String.valueOf(PasswordText.getText());
                if(TextUtils.isEmpty(email)){
                    Toast.makeText(getApplicationContext(), "You haven't typed your email", Toast.LENGTH_LONG);
                    return;
                }
                if(TextUtils.isEmpty(password)){
                    Toast.makeText(getApplicationContext(), "You haven't typed your password", Toast.LENGTH_LONG);
                    return;
                }
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(getApplicationContext(), "Login successful", Toast.LENGTH_LONG);
                            Intent i = new Intent(getApplicationContext(), AddPost.class);
                            FirebaseUser user2 = auth.getCurrentUser();

                            i.putExtra("userid", user2.getUid());

                            startActivity(i);
                            finish();
                        }
                        else{
                            Toast.makeText(getApplicationContext(), "Login failed", Toast.LENGTH_LONG);

                        }

                    }
                });

            }
        });
    }
}