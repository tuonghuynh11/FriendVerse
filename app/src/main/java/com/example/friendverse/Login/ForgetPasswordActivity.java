package com.example.friendverse.Login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.friendverse.Adapter.ViewPagerAdapterForget;
import com.example.friendverse.DialogLoadingBar.LoadingDialog;
import com.example.friendverse.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;

public class ForgetPasswordActivity extends AppCompatActivity {
    //private TabLayout tabLayout;
    private ViewPager2 viewPager2;
    private ViewPagerAdapterForget adapter;
    private LoadingDialog loadingDialog = new LoadingDialog(this);
    private Button bttEmail;
    private EditText etEmail;
    private TextView tvLogin;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        bttEmail = findViewById(R.id.buttonNextEmailForget2);
        etEmail = findViewById(R.id.emailForgetEditText2);
        tvLogin = findViewById(R.id.tvLoginForget);
        mAuth = FirebaseAuth.getInstance();

        bttEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadingDialog.showDialog();
                String email = etEmail.getText().toString();
                if(TextUtils.isEmpty(email)){
                    etEmail.setError("Email can't be empty");
                    etEmail.requestFocus();
                    loadingDialog.hideDialog();
                }
                else{
                    mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                loadingDialog.hideDialog();
                                Toast.makeText(ForgetPasswordActivity.this, "Reset password mail sent!", Toast.LENGTH_SHORT).show();

                            }
                            else{
                                loadingDialog.hideDialog();
                                Toast.makeText(ForgetPasswordActivity.this, "Error sending email!", Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
                }
            }
        });

        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //tabLayout = findViewById(R.id.tablayoutForget);
        //viewPager2 = findViewById(R.id.viewPager2Forget);

//        tabLayout.addTab(tabLayout.newTab().setText("Phone number"));
//        tabLayout.addTab(tabLayout.newTab().setText("Email"));

//        FragmentManager fragmentManager = getSupportFragmentManager();
//        adapter = new ViewPagerAdapterForget(fragmentManager, getLifecycle());
//        viewPager2.setAdapter(adapter);

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

//        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
//            @Override
//            public void onPageSelected(int position) {
//                //tabLayout.selectTab(tabLayout.getTabAt(position));
//            }
//        });
    }
}