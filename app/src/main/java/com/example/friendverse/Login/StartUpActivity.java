package com.example.friendverse.Login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import com.example.friendverse.R;

import java.util.Timer;
import java.util.TimerTask;

public class StartUpActivity extends AppCompatActivity {
    private ImageView logoImg;
    Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_up);

        logoImg = findViewById(R.id.ImgLogo);
        logoImg.setImageResource(R.drawable.app_icon_one);

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                Intent intent = new Intent(StartUpActivity.this, StartActivity.class);
                startActivity(intent);

                finishAffinity();

            }
        }, 2000);
    }
}