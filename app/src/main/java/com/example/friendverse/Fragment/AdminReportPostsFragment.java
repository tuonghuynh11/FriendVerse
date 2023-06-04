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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.friendverse.Adapter.UserReportAllPostAdapter;
import com.example.friendverse.Adapter.UserReportPostAdapter;
import com.example.friendverse.Model.Post;
import com.example.friendverse.Model.User;
import com.example.friendverse.R;
import com.example.friendverse.listeners.ConversionListener;
import com.facebook.all.All;
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

public class AdminReportPostsFragment extends Fragment implements ConversionListener {
    ImageView back;
    Button alluser_btn, report_btn;
    RecyclerView recyclerView_allpost, recyclerView_report;
    boolean check = false;
    FirebaseUser firebaseUser;
    UserReportPostAdapter userReportAdapter_report;
    UserReportAllPostAdapter userReportAdapter_allpost;
    private List<Post> allPosts;
    private List<Post> postReports;
    private BottomSheetDialog bottomSheetDialog;
    Context context;
    private SocialAutoCompleteTextView searchText;
    List<String> idList_ban;
    private View view;
    int state;
    public AdminReportPostsFragment() {
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

        recyclerView_allpost = view.findViewById(R.id.recycler_alluser);
        recyclerView_allpost.setHasFixedSize(true);
        recyclerView_allpost.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView_report = view.findViewById(R.id.recycler_report);
        recyclerView_report.setHasFixedSize(true);
        recyclerView_report.setLayoutManager(new LinearLayoutManager(getContext()));

        allPosts = new ArrayList<Post>();
        userReportAdapter_allpost = new UserReportAllPostAdapter(getContext(), allPosts);
        recyclerView_allpost.setAdapter(userReportAdapter_allpost);

        postReports = new ArrayList<Post>();
        userReportAdapter_report = new UserReportPostAdapter(getContext(), postReports, this);
        recyclerView_report.setAdapter(userReportAdapter_report);

        searchText = view.findViewById(R.id.search_bar);

        idList_ban = new ArrayList<>();
        AllPost();
        ShowBan();
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                searchPost(s.toString().toLowerCase(), state);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchPost(s.toString(), state);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        userReportAdapter_allpost.setOnItemClickListener(new UserReportAllPostAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Post post) {
                Bundle bundle = new Bundle();
                bundle.putString("postid", post.getPostid());
                DetailReportUserFragment detailReportUserFragment = new DetailReportUserFragment();
                detailReportUserFragment.setArguments(bundle);
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, detailReportUserFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        userReportAdapter_report.setOnItemClickListener(new UserReportPostAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(Post post) {
                Bundle bundle = new Bundle();
                bundle.putString("postid", post.getPostid());
                DetailReportUserFragment detailReportUserFragment = new DetailReportUserFragment();
                detailReportUserFragment.setArguments(bundle);
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, detailReportUserFragment)
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
                alluser_btn.setBackground(getResources().getDrawable(R.drawable.button_background_blue));
                alluser_btn.setTextColor(getResources().getColor(R.color.white));
                report_btn.setBackground(getResources().getDrawable(R.drawable.button_background));
                report_btn.setTextColor(getResources().getColor(R.color.black));
                recyclerView_allpost.setVisibility(View.VISIBLE);
                recyclerView_report.setVisibility(View.GONE);
                userReportAdapter_allpost.notifyDataSetChanged();
                state = 0;
                searchPost("", 0);
            }
        });
        report_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                report_btn.setBackground(getResources().getDrawable(R.drawable.button_background_blue));
                report_btn.setTextColor(getResources().getColor(R.color.white));
                alluser_btn.setBackground(getResources().getDrawable(R.drawable.button_background));
                alluser_btn.setTextColor(getResources().getColor(R.color.black));
                recyclerView_allpost.setVisibility(View.GONE);
                recyclerView_report.setVisibility(View.VISIBLE);
                state = 1;
                searchPost("", 1);
            }
        });
        return view;
    }

    private void searchPost(String s, int state) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (state == 0) {
            recyclerView_allpost.setVisibility(View.VISIBLE);
            recyclerView_report.setVisibility(View.GONE);

            Query query = FirebaseDatabase.getInstance().getReference().child("Posts")
                    .orderByChild("postid")
                    .startAt(s)
                    .endAt(s + "\uf8ff");
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    allPosts.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Post post = snapshot.getValue(Post.class);
                        allPosts.add(post);
                    }
                    userReportAdapter_allpost.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        else {
            recyclerView_allpost.setVisibility(View.GONE);
            recyclerView_report.setVisibility(View.VISIBLE);


            Query query = FirebaseDatabase.getInstance().getReference().child("Posts")
                    .orderByChild("postid")
                    .startAt(s)
                    .endAt(s + "\uf8ff");
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    getBan();
                    postReports.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Post post = snapshot.getValue(Post.class);
                        if (idList_ban.contains(post.getPostid())) {
                            postReports.add(post);
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

    private void AllPost() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (TextUtils.isEmpty(searchText.getText().toString())) {
                    allPosts.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Post post = snapshot.getValue(Post.class);
                        allPosts.add(post);
                    }
                }
                userReportAdapter_allpost.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void getBan() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Ban").child("Posts");
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
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (TextUtils.isEmpty(searchText.getText().toString())) {
                    postReports.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Post post = snapshot.getValue(Post.class);
                        if (idList_ban.contains(post.getPostid())) {
                            postReports.add(post);
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

    }

    @Override
    public void onConversionClicked(Post post) {
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
                FirebaseDatabase.getInstance().getReference().child("Ban").child("Posts").child(post.getPostid()).removeValue();
                postReports.remove(post);
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
        searchPost("", 0);
    }
}