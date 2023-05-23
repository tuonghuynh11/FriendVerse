package com.example.friendverse;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.view.MenuItem;
import com.example.friendverse.Fragment.HomeFragment;
import com.example.friendverse.Fragment.NotificationFragment;
import com.example.friendverse.Fragment.ProfileFragment;
import com.example.friendverse.Fragment.SearchFragment;
import com.example.friendverse.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private Fragment selecterFragment ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        if (bottomNavigationView != null) {
            bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    switch (menuItem.getItemId()) {
                        case R.id.nav_home:
                            selecterFragment = new HomeFragment();
                            break;

                        case R.id.nav_search:
                            selecterFragment = new SearchFragment();
                            break;

                        case R.id.nav_add:
                            selecterFragment = null;
                            startActivity(new Intent(MainActivity.this, PostActivity.class));

                            break;

//                        case R.id.nav_heart:
//                            selecterFragment = new NotificationFragment();
//                            break;
//
//                        case R.id.nav_profile:
//
//                            selecterFragment = new ProfileFragment();
//                            break;
                    }
                    if (selecterFragment != null) {
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selecterFragment).commit();
                    }
                    return true;
                }

            });
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
    }
}
