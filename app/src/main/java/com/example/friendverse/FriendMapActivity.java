package com.example.friendverse;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.friendverse.Fragment.FriendMapsFragment;
import com.example.friendverse.Model.User;
import com.example.friendverse.databinding.ActivityFriendMapBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.List;

public class FriendMapActivity extends AppCompatActivity {

    private ActivityFriendMapBinding activityFriendMapBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityFriendMapBinding = ActivityFriendMapBinding.inflate(getLayoutInflater());
        setContentView(activityFriendMapBinding.getRoot());
        initMap();
        setListener();

    }

    private void initMap() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.map, new FriendMapsFragment(), "GGMAP");
        fragmentTransaction.commit();
    }

    private void setListener() {

        activityFriendMapBinding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {

                String friendLocation = activityFriendMapBinding.searchView.getQuery().toString();

                if (friendLocation != null) {
                    try {
                        User friends = new User();
                        for (User friend : ((FriendMapsFragment) getSupportFragmentManager().findFragmentById(R.id.map)).userList) {
                            if (friend.getUsername().equals(friendLocation.toString().trim())) {
                                friends = friend;
                            }
                        }
                        if (friends.getId() == null)
                        {
                            Toast.makeText(FriendMapActivity.this, "You haven't followed this user or Your friend doesn't share his location" , Toast.LENGTH_SHORT).show();
                            return false;
                        }

                        LatLng end = new LatLng(friends.getLatitude(), friends.getLongitude());
                        ((FriendMapsFragment) getSupportFragmentManager().findFragmentById(R.id.map)).map.animateCamera(CameraUpdateFactory.newLatLngZoom(end, 19));
                    } catch (Exception e) {
                        Toast.makeText(FriendMapActivity.this, "Can't find your friend" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(FriendMapActivity.this, "Please enter your friend's user name ", Toast.LENGTH_SHORT).show();
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });


        activityFriendMapBinding.menuOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopup();
            }
        });
        activityFriendMapBinding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        activityFriendMapBinding.returnMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((FriendMapsFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getDeviceLocation();
            }
        });
    }

    public void showPopup() {

        PopupMenu popup = new PopupMenu(this, activityFriendMapBinding.menuOptions);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_context, popup.getMenu());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            popup.setForceShowIcon(true);
        }
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.normal:
                        ((FriendMapsFragment) getSupportFragmentManager().findFragmentByTag("GGMAP")).map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        break;
                    case R.id.satellite:
                        ((FriendMapsFragment) getSupportFragmentManager().findFragmentById(R.id.map)).map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                        break;
                    case R.id.hybrid:
                        ((FriendMapsFragment) getSupportFragmentManager().findFragmentById(R.id.map)).map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                        break;
                    case R.id.terrain:
                        ((FriendMapsFragment) getSupportFragmentManager().findFragmentById(R.id.map)).map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                        break;
                    case R.id.none:
                        ((FriendMapsFragment) getSupportFragmentManager().findFragmentById(R.id.map)).map.setMapType(GoogleMap.MAP_TYPE_NONE);
                        break;
                }
                return false;
            }
        });
        popup.show();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FriendMapsFragment.LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ((FriendMapsFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getDeviceLocation();
                ((FriendMapsFragment) getSupportFragmentManager().findFragmentById(R.id.map)).initUserFollowersList();
                ((FriendMapsFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getUsers();
            }
        }
    }
}