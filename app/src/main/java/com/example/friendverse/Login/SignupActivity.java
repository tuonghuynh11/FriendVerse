package com.example.friendverse.Login;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.friendverse.Adapter.ViewPagerAdapter;
import com.example.friendverse.DialogLoadingBar.LoadingDialog;
import com.example.friendverse.Login.LoginActivity;
import com.example.friendverse.MailService.GMailSender;
import com.example.friendverse.R;
import com.google.android.material.tabs.TabLayout;

public class SignupActivity extends AppCompatActivity {
    private ImageView imgView;
    private TabLayout tabLayout;
    private ViewPager2 viewPager2;
    private ViewPagerAdapter adapter;
    private TextView tvLogin;
    private EditText emailET;
    private Button bttNext;
    private LoadingDialog loadingDialog = new LoadingDialog(this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        imgView = findViewById(R.id.imageViewSignup);
//        tabLayout = findViewById(R.id.tablayoutSignup);
        //viewPager2 = findViewById(R.id.viewPager2Signup);
        tvLogin = findViewById(R.id.tvLoginSignup);
        emailET = findViewById(R.id.emailEditText2);
        bttNext = findViewById(R.id.buttonNextEmail2);


        imgView.setImageResource(R.drawable.user_image);

        //tabLayout.addTab(tabLayout.newTab().setText("Phone number"));
//        tabLayout.addTab(tabLayout.newTab().setText("Email"));

        //FragmentManager fragmentManager = getSupportFragmentManager();
        //adapter = new ViewPagerAdapter(fragmentManager, getLifecycle());
        //viewPager2.setAdapter(adapter);
//        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
//            @Override
//            public void onTabSelected(TabLayout.Tab tab) {
//                viewPager2.setCurrentItem(tab.getPosition());
//            }
//
//            @Override
//            public void onTabUnselected(TabLayout.Tab tab) {
//
//            }
//
//            @Override
//            public void onTabReselected(TabLayout.Tab tab) {
//
//            }
//        });
//
//        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
//            @Override
//            public void onPageSelected(int position) {
//                tabLayout.selectTab(tabLayout.getTabAt(position));
//            }
//        });

        bttNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadingDialog.showDialog();
                String emailSender = "friendverse123@gmail.com";
                String passEmail = "jqnzillisugvmied";
                String emailReceive = emailET.getText().toString();

                int rand_num = (int)Math.floor(Math.random() * (1000000 - 100000 + 1) + 100000);
//                sendEmailOTP(emailReceive, emailSender, passEmail, rand_num);
                if(TextUtils.isEmpty(emailReceive)){
                    emailET.setError("Email can't be empty");
                    emailET.requestFocus();
                    loadingDialog.hideDialog();
                }
                else{
                    try{
                        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

                        StrictMode.setThreadPolicy(policy);

                        //Chinh sua lai de gui mail ve edittext duoc nhap
                        GMailSender sender = new GMailSender(emailSender, passEmail);
                        sender.sendMail("OTP to verify email", "This is OTP code for verification your email: " + Integer.toString(rand_num), emailSender, emailReceive);
                        Toast.makeText(SignupActivity.this,"OTP sent!",Toast.LENGTH_SHORT).show();

                    }
                    catch (Exception e){
                        Log.e("SendMail: ", e.getMessage(), e);
                    }
                    //loadingDialog.hideDialog();
                    Intent intent = new Intent(SignupActivity.this, EmailConfirmActivity.class);
                    intent.putExtra("Email", emailReceive);
                    intent.putExtra("OTPCode", rand_num);
                    loadingDialog.hideDialog();
                    startActivity(intent);
                }


            }
        });

        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
        });
    }
}