package com.example.friendverse.Login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.friendverse.Login.NewPasswordActivity;
import com.example.friendverse.R;

public class ForgetPassEmailActivity extends AppCompatActivity {
    private Button buttonEmail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_pass_email);

        buttonEmail = findViewById(R.id.buttonNextEmailOTPForget);
        buttonEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), NewPasswordActivity.class);
                startActivity(intent);
            }
        });
    }
}