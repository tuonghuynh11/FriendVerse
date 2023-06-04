package com.example.friendverse.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.friendverse.Adapter.UserReportAdapter;
import com.example.friendverse.Adapter.UserReportAllUserAdapter;
import com.example.friendverse.Model.Post;
import com.example.friendverse.Model.User;
import com.example.friendverse.R;
import com.example.friendverse.listeners.ConversionListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
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

public class AdminReportFragment extends Fragment implements ConversionListener {
    ImageView back;
    Button alluser_btn, report_btn;
    RecyclerView recyclerView_alluser, recyclerView_report;
    boolean check = false;
    FirebaseUser firebaseUser;
    UserReportAdapter userReportAdapter_report;
    UserReportAllUserAdapter userReportAdapter_alluser;
    private List<User> allUsers;
    private List<User> userReports;
    private BottomSheetDialog bottomSheetDialog;
    Context context;
    private SocialAutoCompleteTextView searchText;
    List<String> idList_ban;
    private View view;
    int state;
    public AdminReportFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getBan();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view =  inflater.inflate(R.layout.fragment_admin_report, container, false);

        alluser_btn = view.findViewById(R.id.alluserbtn);
        report_btn = view.findViewById(R.id.reportbtn);
        state = 0;

        recyclerView_alluser = view.findViewById(R.id.recycler_alluser);
        recyclerView_alluser.setHasFixedSize(true);
        recyclerView_alluser.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView_alluser.setVisibility(View.VISIBLE);
        recyclerView_report = view.findViewById(R.id.recycler_report);
        recyclerView_report.setHasFixedSize(true);
        recyclerView_report.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView_report.setVisibility(View.GONE);

        allUsers = new ArrayList<User>();
        userReportAdapter_alluser = new UserReportAllUserAdapter(getContext(), allUsers);
        recyclerView_alluser.setAdapter(userReportAdapter_alluser);

        userReports = new ArrayList<User>();
        userReportAdapter_report = new UserReportAdapter(getContext(), userReports, this);
        recyclerView_report.setAdapter(userReportAdapter_report);

        searchText = view.findViewById(R.id.search_bar);

        idList_ban = new ArrayList<>();
        AllUser();
        ShowBan();
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                searchUser(s.toString().toLowerCase(), state);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUser(s.toString().toLowerCase(), state);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        userReportAdapter_alluser.setOnItemClickListener(new UserReportAllUserAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(User user) {
            }
        });

        userReportAdapter_report.setOnItemClickListener(new UserReportAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(User user) {
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
                alluser_btn.setBackground(getResources().getDrawable(R.drawable.button_background_blue));
                alluser_btn.setTextColor(getResources().getColor(R.color.white));
                report_btn.setBackground(getResources().getDrawable(R.drawable.button_background));
                report_btn.setTextColor(getResources().getColor(R.color.black));
                recyclerView_alluser.setVisibility(View.VISIBLE);
                recyclerView_report.setVisibility(View.GONE);
                userReportAdapter_alluser.notifyDataSetChanged();
                state = 0;
                searchUser("", 0);
            }
        });
        report_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                report_btn.setBackground(getResources().getDrawable(R.drawable.button_background_blue));
                report_btn.setTextColor(getResources().getColor(R.color.white));
                alluser_btn.setBackground(getResources().getDrawable(R.drawable.button_background));
                alluser_btn.setTextColor(getResources().getColor(R.color.black));
                userReportAdapter_report.notifyDataSetChanged();
                recyclerView_alluser.setVisibility(View.GONE);
                recyclerView_report.setVisibility(View.VISIBLE);
                state = 1;
                searchUser("", 1);
            }
        });
        return view;
    }

    private void searchUser(String s, int state) {
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
                    getBan();
                    userReports.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User user = snapshot.getValue(User.class);
                        if (idList_ban.contains(user.getId())) {
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
    private void getBan() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Ban").child("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                idList_ban.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (snapshot.getKey() != "") {
                        idList_ban.add(snapshot.getKey());
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void ShowBan() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (TextUtils.isEmpty(searchText.getText().toString())) {
                    userReports.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User user = snapshot.getValue(User.class);
                        if (idList_ban.contains(user.getId())) {
                            userReports.add(user);
                        }
                    }
                }
                userReportAdapter_report.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onConversionClicked(User user) {
        bottomSheetDialog = new BottomSheetDialog(
                view.getContext(), R.style.BottomSheetDialogTheme
        );
        View bottomSheetView = LayoutInflater.from(view.getContext()).inflate
                (
                        R.layout.layout_bottom_alluser_sheet,
                        (LinearLayout)view.findViewById(R.id.bottomSheetContainer)
                );
        TextView tv_unban = bottomSheetView.findViewById(R.id.ban);
        tv_unban.setText("Unban");
        bottomSheetView.findViewById(R.id.ban).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseDatabase.getInstance().getReference().child("Ban").child("Users").child(user.getId()).removeValue();
                userReports.remove(user);
                userReportAdapter_report.notifyDataSetChanged();
                bottomSheetDialog.cancel();
            }
        });
        bottomSheetView.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.cancel();
            }
        });
        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

    @Override
    public void onConversionClicked(Post post) {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        getBan();
        searchUser("", 0);
    }
}