package com.example.friendverse.Login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.friendverse.DialogLoadingBar.LoadingDialog;
import com.example.friendverse.R;

public class SignupFinishActivity extends AppCompatActivity {
    private TextView tvChangeUsername;
    private Button exploreBtt;
    private LoadingDialog loadingDialog = new LoadingDialog(this);
    String uid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_finish);
        Intent intent1 = getIntent();
        uid = intent1.getExtras().getString("UID");

        tvChangeUsername = findViewById(R.id.textViewChangeUsername);
        exploreBtt = findViewById(R.id.buttonExplore);
        tvChangeUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadingDialog.showDialog();
                Intent intent = new Intent(getApplicationContext(), ChangeUsernameActivity.class);
                intent.putExtra("UID1", uid);
                startActivity(intent);
                loadingDialog.hideDialog();
            }
        });
        exploreBtt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadingDialog.showDialog();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                loadingDialog.hideDialog();
                finishAffinity();
            }
        });
    }
}