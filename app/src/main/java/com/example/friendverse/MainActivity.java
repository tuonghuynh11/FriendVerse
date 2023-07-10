package com.example.friendverse;

import static com.example.friendverse.R.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.friendverse.Fragment.HomeFragment;
import com.example.friendverse.Fragment.NotificationFragment;
import com.example.friendverse.Fragment.NotifyFragment;
import com.example.friendverse.Fragment.ProfileFragment;
import com.example.friendverse.Fragment.ReelFragment;
import com.example.friendverse.Fragment.SearchFragment;
import com.example.friendverse.Fragment.WatchFragment;
import com.example.friendverse.Login.LoginActivity;
import com.example.friendverse.Login.StartActivity;
import com.example.friendverse.Login.StartUpActivity;
import com.example.friendverse.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    public Fragment selectedFragment = null;

//    // User
    FirebaseAuth auth;
//    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(MainActivity.this, StartActivity.class));
            return;
        }
        setContentView(layout.activity_main);

        bottomNavigationView = findViewById(id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(navigationItemSelectedListener);


        Bundle intent = getIntent().getExtras();
        if (intent != null){
            String publisher = intent.getString("profileid");
            SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
            editor.putString("profileid", publisher);
            editor.apply();

            Bundle bundle =new Bundle();
            bundle.putString("profileid",publisher);
            bundle.putString("isClose","1");

            Fragment fragment =new ProfileFragment();
            fragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    fragment).commit();
        }
        else {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new HomeFragment()).commit();
        }

    }

    private BottomNavigationView.OnItemSelectedListener navigationItemSelectedListener = item -> {
        switch (item.getItemId()) {
            case id.nav_home:
                selectedFragment = new HomeFragment();
                HomeFragment.position=0;
                break;
            case id.nav_search:
                selectedFragment = new SearchFragment();
                break;
            case id.nav_watch:
                selectedFragment = new ReelFragment();
                break;
            case id.nav_notify:
                selectedFragment = new NotificationFragment();
                break;
            case id.nav_profile:
                Bundle passData = new Bundle();
                passData.putString("profileid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                selectedFragment = new ProfileFragment();
                selectedFragment.setArguments(passData);
                break;
        }
        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction().replace(id.fragment_container, selectedFragment).commit();
        }
        return true;
    };

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(MainActivity.this, StartActivity.class));
            finishAffinity();
            return;
        }
    }
    protected void onResume() {
        super.onResume();
        if (auth.getCurrentUser()==null)
            return;
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(auth.getCurrentUser().getUid());
        reference.child(User.ACTIVITYKEY).setValue(0);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(auth.getCurrentUser().getUid());
            reference.child(User.ACTIVITYKEY).setValue(0);
        }
        catch (Exception ex){
            Log.e("TAG", "User not login");
        }
    }
}
