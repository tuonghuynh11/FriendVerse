package com.example.friendverse.Login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.friendverse.R;

public class StartActivity extends AppCompatActivity {
    private ImageView logoImg;
    private Button bttlogin;
    private Button bttsignup;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        logoImg = findViewById(R.id.logoImage);
        logoImg.setImageResource(R.drawable.app_icon_one);

        bttlogin = findViewById(R.id.loginbttid);
        bttsignup = findViewById(R.id.signupbttid);

        bttlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
        });
        bttsignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivity(intent);
            }
        });
    }
}