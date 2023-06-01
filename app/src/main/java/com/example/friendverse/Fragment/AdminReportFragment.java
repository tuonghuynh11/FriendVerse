package com.example.friendverse.Fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.example.friendverse.Adapter.UserAdapter;
import com.example.friendverse.Adapter.UserReportAdapter;
import com.example.friendverse.Adapter.UserReportAllUserAdapter;
import com.example.friendverse.Model.User;
import com.example.friendverse.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.hendraanggrian.appcompat.widget.SocialAutoCompleteTextView;

import java.util.ArrayList;
import java.util.List;

public class AdminReportFragment extends Fragment {
    ImageView back;
    Button alluser_btn, report_btn;
    RecyclerView recyclerView_alluser, recyclerView_report;
    FirebaseUser firebaseUser;
    UserReportAdapter userReportAdapter_report;
    UserReportAllUserAdapter userReportAdapter_alluser;
    private List<String> idList_ban;
    private List<User> allUsers;
    private List<User> userReports;
    Context context;
    private SocialAutoCompleteTextView searchText;
    int state = 0;
    public AdminReportFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_admin_report, container, false);

        alluser_btn = view.findViewById(R.id.alluserbtn);
        report_btn = view.findViewById(R.id.reportbtn);

        recyclerView_alluser = view.findViewById(R.id.recycler_alluser);
        recyclerView_alluser.setHasFixedSize(true);
        recyclerView_alluser.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView_report = view.findViewById(R.id.recycler_report);
        recyclerView_report.setHasFixedSize(true);
        recyclerView_report.setLayoutManager(new LinearLayoutManager(getContext()));

        allUsers = new ArrayList<User>();
        userReportAdapter_alluser = new UserReportAllUserAdapter(getContext(), allUsers);
        recyclerView_alluser.setAdapter(userReportAdapter_alluser);

        userReports = new ArrayList<User>();
        userReportAdapter_report = new UserReportAdapter(getContext(), userReports);
        recyclerView_report.setAdapter(userReportAdapter_report);

        searchText = view.findViewById(R.id.search_bar);

        idList_ban = new ArrayList<>();
        AllUser();
        AllUserReport();
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                searchUser(s.toString().toLowerCase());
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUser(s.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        userReportAdapter_alluser.setOnItemClickListener(new UserReportAllUserAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(User user) {
                Bundle bundle = new Bundle();
                bundle.putString("userId", user.getId());
                ProfileFragment profileFragment = new ProfileFragment();
                profileFragment.setArguments(bundle);
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, profileFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        userReportAdapter_report.setOnItemClickListener(new UserReportAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(User user) {
                Bundle bundle = new Bundle();
                bundle.putString("userId", user.getId());
                ProfileFragment profileFragment = new ProfileFragment();
                profileFragment.setArguments(bundle);
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, profileFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });


        back = view.findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getParentFragmentManager();
                fragmentManager.popBackStack();
            }
        });
        alluser_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (state != 0) {
                    alluser_btn.setBackground(getResources().getDrawable(R.drawable.button_background_blue));
                    alluser_btn.setTextColor(getResources().getColor(R.color.white));
                    report_btn.setBackground(getResources().getDrawable(R.drawable.button_background));
                    report_btn.setTextColor(getResources().getColor(R.color.black));
                    recyclerView_alluser.setVisibility(View.VISIBLE);
                    recyclerView_report.setVisibility(View.GONE);
                    state = 0;
                }
            }
        });
        report_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (state == 0) {
                    report_btn.setBackground(getResources().getDrawable(R.drawable.button_background_blue));
                    report_btn.setTextColor(getResources().getColor(R.color.white));
                    alluser_btn.setBackground(getResources().getDrawable(R.drawable.button_background));
                    alluser_btn.setTextColor(getResources().getColor(R.color.black));
                    recyclerView_alluser.setVisibility(View.GONE);
                    recyclerView_report.setVisibility(View.VISIBLE);
                    state = 1;
                }
            }
        });
        return view;
    }

    private void searchUser(String s) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (state == 0) {
            recyclerView_alluser.setVisibility(View.VISIBLE);
            recyclerView_report.setVisibility(View.GONE);

            Query query = FirebaseDatabase.getInstance().getReference().child("Users")
                    .orderByChild("username")
                    .startAt(s)
                    .endAt(s + "\uf8ff");
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    allUsers.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User user = snapshot.getValue(User.class);
                        if (!user.getId().equals(currentUser.getUid())) {
                            allUsers.add(user);
                        }
                    }
                    userReportAdapter_alluser.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        else {
            recyclerView_alluser.setVisibility(View.GONE);
            recyclerView_report.setVisibility(View.VISIBLE);

            Query query = FirebaseDatabase.getInstance().getReference().child("Users")
                    .orderByChild("username")
                    .startAt(s)
                    .endAt(s + "\uf8ff");
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    userReports.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User user = snapshot.getValue(User.class);
                        if (!user.getId().equals(currentUser.getUid())) {
                            userReports.add(user);
                        }
                    }
                    userReportAdapter_report.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }
    private void showUsersBan() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userReports.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    User user = snapshot.getValue(User.class);
                    for (String id : idList_ban){
                        if (user.getId().equals(id)){
                            userReports.add(user);
                        }
                    }
                }
                userReportAdapter_report.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private void getBan() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Ban");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                idList_ban.clear();
                for (DataSnapshot item : snapshot.getChildren()){
                    idList_ban.add(item.getKey().toString());
                }
                showUsersBan();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void AllUser() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (TextUtils.isEmpty(searchText.getText().toString())) {
                    allUsers.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User user = snapshot.getValue(User.class);
                        allUsers.add(user);
                    }
                }
                userReportAdapter_alluser.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void AllUserReport() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (TextUtils.isEmpty(searchText.getText().toString())) {
                    userReports.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User user = snapshot.getValue(User.class);
                        userReports.add(user);
                    }
                    userReportAdapter_report.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}