package com.example.friendverse;

import static com.example.friendverse.R.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.fragment.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.friendverse.Fragment.HomeFragment;
import com.example.friendverse.Fragment.NotifyFragment;
import com.example.friendverse.Fragment.ProfileFragment;
import com.example.friendverse.Fragment.SearchFragment;
import com.example.friendverse.Fragment.WatchFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    Fragment selectedFragment = null;

    // User
    FirebaseAuth auth;
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(layout.activity_main);
        Objects.requireNonNull(getSupportActionBar()).hide();

        // auth
        auth = FirebaseAuth.getInstance();
        String email = "nguyenthaicong265@gmail.com";
        String pass = "thaicong";
        //
        auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(MainActivity.this, task -> {
            if (task.isSuccessful()) {
                Toast.makeText(MainActivity.this, "Successful", Toast.LENGTH_SHORT).show();
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("User").child(auth.getCurrentUser().getUid());
            }
            else {
                Toast.makeText(MainActivity.this, "Failed", Toast.LENGTH_SHORT).show();
            }
        });
        //

        bottomNavigationView = findViewById(id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(navigationItemSelectedListener);

//        Bundle intent = getIntent().getExtras();
//        if (intent != null){
//            String publisher = intent.getString("publisherid");
//            SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
//            editor.putString("profileid", publisher);
//            editor.apply();
//
//            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
//                    new ProfileFragment()).commit();
//        } else {
//            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
//                    new HomeFragment()).commit();
//        }
    }

    private BottomNavigationView.OnItemSelectedListener navigationItemSelectedListener = item -> {
        switch (item.getItemId()) {
            case id.nav_home:
                selectedFragment = new HomeFragment();
                break;
            case id.nav_search:
                selectedFragment = new SearchFragment();
                break;
            case id.nav_watch:
                selectedFragment = new WatchFragment();
                break;
            case id.nav_notify:
                selectedFragment = new NotifyFragment();
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
}
