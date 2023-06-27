package com.example.friendverse.Fragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.friendverse.Model.User;
import com.example.friendverse.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class FriendMapsFragment extends Fragment {
    private static String FINE_LOCATION = android.Manifest.permission.ACCESS_FINE_LOCATION;
    private static String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    public final static int LOCATION_REQUEST_CODE = 44;
    public GoogleMap map;
    public List<LatLng> allMarkers;
    private MarkerOptions myLocation;
    private FusedLocationProviderClient clientLocation;
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    public List<User> userList = new ArrayList<>();
    List<String> userFollowersListId = new ArrayList<>();
      int isNotify=0;
    private User currentUser;
    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(GoogleMap googleMap) {
            makeRequestPermission();
            map = googleMap;
            map.getUiSettings().setZoomGesturesEnabled(true);
            map.getUiSettings().setCompassEnabled(false);
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                initUserFollowersList();
                getUsers();
                //getDeviceLocation();
            }
//            map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
//                @Override
//                public void onMapClick(@NonNull LatLng latLng) {
//                    Address infor = getInforByLatlng(latLng);
//                    map.addMarker(new MarkerOptions().position(latLng).title(infor.getAdminArea()).snippet(infor.getAddressLine(0)));
//                    allMarkers.add(latLng);
//                    Toast.makeText(getContext(), infor.getAddressLine(0), Toast.LENGTH_SHORT).show();
//
//                }
//            });
//            map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
//                @Override
//                public boolean onMarkerClick(@NonNull Marker marker) {
//                    Address infor = getInforByLatlng(marker.getPosition());
//                    Toast.makeText(getContext(), infor.getAddressLine(0), Toast.LENGTH_SHORT).show();
//                    return false;
//                }
//            });

        }
    };

    private void getLocationOfFriends() {
        for (User user : userList) {
            if (user.getLongitude()==0&&user.getLatitude()==0)
                continue;
            MarkerOptions position = new MarkerOptions().position(new LatLng(user.getLatitude(), user.getLongitude()));
            if (android.os.Build.VERSION.SDK_INT > 9) {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
            }
            URL url = null;
            try {

                url = new URL(user.getImageurl());
            } catch (MalformedURLException e) {
                Log.i("TAG", "url is NULL");
            }
            Bitmap bmp = null;
            if (url!=null){
                try {

                    bmp =Bitmap.createScaledBitmap(BitmapFactory.decodeStream(url.openConnection().getInputStream()),150,150,false);
                } catch (IOException e) {
                    Log.i("TAG", "Bit Map is NULL");
                }
            }

            if (bmp == null) {
                int height = 200;
                int width = 200;
                BitmapDrawable bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.default_user_avatar);
                Bitmap b = bitmapdraw.getBitmap();
                Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
                position.icon(BitmapDescriptorFactory.fromBitmap(getRoundedCornerBitmap(smallMarker)));
            } else {
                position.icon(BitmapDescriptorFactory.fromBitmap(getRoundedCornerBitmap(bmp)));
            }
           float distance= distanceBetweenTwoLocation(new LatLng(myLocation.getPosition().latitude,myLocation.getPosition().longitude),new LatLng(user.getLatitude(),user.getLongitude()));
            //Set info for friend location
            Address infor = getInforByLatlng(new LatLng(user.getLatitude(),user.getLongitude()));

            position.snippet((double) Math.round((distance/1000) * 10) / 10 +" km\n"+ infor.getAddressLine(0));
            map.addMarker(position.title(user.getUsername()));
            map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

                @Override
                public View getInfoWindow(Marker arg0) {
                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) {

                    LinearLayout info = new LinearLayout(getActivity());
                    info.setOrientation(LinearLayout.VERTICAL);

                    TextView  title = new TextView(getActivity());
                    title.setTextColor(Color.BLACK);
                    title.setGravity(Gravity.CENTER);
                    title.setTypeface(null, Typeface.BOLD);
                    title.setText(marker.getTitle());

                    TextView snippet = new TextView(getActivity());
                    snippet.setGravity(Gravity.CENTER);
                    snippet.setTextColor(Color.GRAY);
                    snippet.setText(marker.getSnippet());

                    info.addView(title);
                    info.addView(snippet);

                    return info;
                }
            });
        }

    }

    private float distanceBetweenTwoLocation(LatLng my, LatLng friend){
        float[] results = new float[1];
        Location.distanceBetween(my.latitude,my.longitude,friend.latitude,friend.longitude,results);
        if (results==null)
            return -1;
        return results[0];
    }
    public Address getInforByLatlng(LatLng latLng) {
        Geocoder geocoder = new Geocoder(getContext());
        try {
            // lay thong tin nao do
            ArrayList<Address> addresses = (ArrayList<Address>) geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            // lay thong tin
            Address infor = addresses.get(0);
            return infor;
        } catch (Exception e) {
            Toast.makeText(getContext(), "Can't recognize position", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return null;

        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {


        makeRequestPermission();

        return inflater.inflate(R.layout.fragment_friend_maps, container, false);
    }

    private void makeRequestPermission() {
        String[] permissions = {android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        if ((ActivityCompat.checkSelfPermission(requireActivity(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            if ((ActivityCompat.checkSelfPermission(requireActivity(), COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
                return;
            } else {
                ActivityCompat.requestPermissions(requireActivity(), permissions, LOCATION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(requireActivity(), permissions, LOCATION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getDeviceLocation();
            }
        }
    }

    public void getDeviceLocation() {
        clientLocation = LocationServices.getFusedLocationProviderClient(getActivity());
        try {
            Task location = clientLocation.getLastLocation();
            location.addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        Location currentLoc = (Location) task.getResult();
                        try {
                            myLocation = new MarkerOptions().position(new LatLng(currentLoc.getLatitude(), currentLoc.getLongitude()));

                        }catch (Exception e){
                            if (isNotify==0){
                                Toast.makeText(getActivity(), "Your Location turns off", Toast.LENGTH_SHORT).show();
                                isNotify=1;
                            }
                            else {
                                Toast.makeText(getActivity(), "You need to turns on your location", Toast.LENGTH_SHORT).show();
                            }
                            return;
                        }
                        if (android.os.Build.VERSION.SDK_INT > 9) {
                            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                            StrictMode.setThreadPolicy(policy);
                        }
                        URL url = null;
                        try {

                            url = new URL(currentUser.getImageurl());
                        } catch (MalformedURLException e) {
                            Log.i("TAG", "url is NULL");
                        }
                        Bitmap bmp = null;
                        if (url!=null){
                            try {

                                bmp =Bitmap.createScaledBitmap(BitmapFactory.decodeStream(url.openConnection().getInputStream()),150,150,false);
                            } catch (IOException e) {
                                Log.i("TAG", "Bit Map is NULL");
                            }
                        }

                        if (bmp == null) {
                            int height = 200;
                            int width = 200;
                            BitmapDrawable bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.default_user_avatar);
                            Bitmap b = bitmapdraw.getBitmap();
                            Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
                            myLocation.icon(BitmapDescriptorFactory.fromBitmap(getRoundedCornerBitmap(smallMarker)));

                        } else {
                            myLocation.icon(BitmapDescriptorFactory.fromBitmap(getRoundedCornerBitmap(bmp)));
                        }
                        Address infor = getInforByLatlng(myLocation.getPosition());
                        map.addMarker(myLocation.title("My Location").snippet(infor.getAddressLine(0)).anchor(0.5f, 1));
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLoc.getLatitude(), currentLoc.getLongitude()), 15f));
                        allMarkers.add(new LatLng(myLocation.getPosition().latitude, myLocation.getPosition().longitude));

                        //add my location to firebase
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(auth.getCurrentUser().getUid());
                        reference.child(User.LATITUDE).setValue(myLocation.getPosition().latitude);
                        reference.child(User.LONGITUDE).setValue(myLocation.getPosition().longitude);
                    }
                }
            });
        } catch (SecurityException e) {

        }
    }
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
        Bitmap result = null;
        try {
            result = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(result);

            int color = 0xff424242;
            Paint paint = new Paint();
            Rect rect = new Rect(0, 0, 200, 200);

            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(color);
            canvas.drawRoundRect(new RectF(new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight())), 100, 100, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(bitmap, rect, rect, paint);

        } catch (NullPointerException e) {
        } catch (OutOfMemoryError o) {
        }
        return result;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
        Places.initialize(getActivity(),"AIzaSyBg8RtAbXyLYcRpQOe2KPwCuNNvW-Rrq70");
        allMarkers = new ArrayList<>();
        initUserFollowersList();
        getUsers();
    }

    public void initUserFollowersList() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Follow").child(auth.getCurrentUser().getUid()).child("followers");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userFollowersListId = new ArrayList<>();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    userFollowersListId.add(snapshot1.getKey());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void getUsers() {

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = firebaseUser.getUid();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    if (!(currentUserId.equals(user.getId())) && userFollowersListId.contains(user.getId())) {
                        userList.add(user);
                    }
                    if (user.getId().equals(currentUserId)) {
                        currentUser = user;
                    }
                }
                myLocation= new MarkerOptions();
                myLocation.position(new LatLng(currentUser.getLatitude(),currentUser.getLongitude()));

                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    //makeRequestPermission();
                    return;
                }
                getDeviceLocation();
                getLocationOfFriends();
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
