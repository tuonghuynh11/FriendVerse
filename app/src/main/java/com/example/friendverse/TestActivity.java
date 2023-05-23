package com.example.friendverse;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;

import com.example.friendverse.Fragment.EditProfileFragment;

public class TestActivity extends AppCompatActivity {
    public static Context contextOfApplication;
    public static Context getContextOfApplication()
    {
        return contextOfApplication;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        contextOfApplication = getApplicationContext();
        getSupportFragmentManager().beginTransaction().add(R.id.container, new EditProfileFragment()).commit();
    }
}