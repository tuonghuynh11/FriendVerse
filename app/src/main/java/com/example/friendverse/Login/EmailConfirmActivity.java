package com.example.friendverse.Login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.friendverse.DialogLoadingBar.LoadingDialog;
import com.example.friendverse.MailService.GMailSender;
import com.example.friendverse.R;

public class EmailConfirmActivity extends AppCompatActivity {
    private Button buttonNext;
    private TextView tvLogin;
    private TextView tvRequest;
    private EditText etCode;
    private LoadingDialog loadingDialog = new LoadingDialog(this);
    String email;
    int otp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_confirm);

        Intent intent1 = getIntent();
        String email = intent1.getExtras().getString("Email");
        otp = intent1.getExtras().getInt("OTPCode");

        buttonNext = findViewById(R.id.buttonNextEmailOTP);
        tvLogin = findViewById(R.id.tvLoginEmail);
        tvRequest = findViewById(R.id.textViewRequestEmail);
        etCode = findViewById(R.id.etverifyEmail);
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadingDialog.showDialog();

                if(etCode.getText().toString().equals(Integer.toString(otp))){
                    Intent intent = new Intent(getApplicationContext(), SignupInfoActivity.class);
                    intent.putExtra("Email1", email);
                    startActivity(intent);
                    loadingDialog.hideDialog();
                    finishAffinity();
                }
                else{
                    Toast.makeText(EmailConfirmActivity.this, "OTP is not correct", Toast.LENGTH_SHORT).show();
                    loadingDialog.hideDialog();
                }
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
                String emailSender = "friendverse123@gmail.com";
                String passEmail = "jqnzillisugvmied";
                String emailReceive = email;

                otp = (int)Math.floor(Math.random() * (1000000 - 100000 + 1) + 100000);
//                sendEmailOTP(emailReceive, emailSender, passEmail, rand_num);

                try{
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

                    StrictMode.setThreadPolicy(policy);

                    //Chinh sua lai de gui mail ve edittext duoc nhap
                    GMailSender sender = new GMailSender(emailSender, passEmail);
                    sender.sendMail("OTP to verify email", "This is OTP code for verification your email: " + Integer.toString(otp), emailSender, emailReceive);
                    Toast.makeText(EmailConfirmActivity.this,"OTP sent!",Toast.LENGTH_SHORT).show();
                }
                catch (Exception e){
                    Log.e("SendMail", e.getMessage(), e);
                }
                loadingDialog.hideDialog();
            }
        });
    }
}