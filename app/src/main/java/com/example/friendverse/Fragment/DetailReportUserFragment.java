package com.example.friendverse.Fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.friendverse.Adapter.ReporterAdapter;
import com.example.friendverse.Adapter.UserProfileAdapter;
import com.example.friendverse.Adapter.UserReportAdapter;
import com.example.friendverse.Model.Post;
import com.example.friendverse.Model.Reporter;
import com.example.friendverse.Model.User;
import com.example.friendverse.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.List;

public class DetailReportUserFragment extends Fragment {
    private String profileid, postid;
    private BottomSheetDialog bottomSheetDialog;
    private TextView idpost, description;
    private ImageView post_image, options;
    private ImageView close;
    private LinearLayout linear_infopost;
    private RecyclerView recycler_view_report;
    ReporterAdapter userReportAdapter;
    List<User> userReportList;
    List<Reporter> reporterList;
    private FirebaseUser firebaseUser;

    public DetailReportUserFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_detail_report_user, container, false);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        profileid = getArguments().getString("profileid");
        postid = getArguments().getString("postid");


        close = view.findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getParentFragmentManager();
                fragmentManager.popBackStack();
//                Fragment fragment = new AdminReportFragment();
//                FragmentManager fragmentManager = getParentFragmentManager();
//                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//                fragmentTransaction.replace(R.id.fragment_container, fragment).addToBackStack(null);
//                fragmentTransaction.commit();
            }
        });
        linear_infopost = view.findViewById(R.id.linear_infopost);
        if (postid != null) {
            if (!linear_infopost.isShown()) {
                linear_infopost.setVisibility(View.VISIBLE);
            }
            idpost = view.findViewById(R.id.idpost);
            post_image = view.findViewById(R.id.post_image);
            description = view.findViewById(R.id.description);
            getPostInfo();
            getReporter("Post");
        }
        else {
            getReporter("User");
        }

        recycler_view_report = view.findViewById(R.id.recycler_view_report);
        recycler_view_report.setHasFixedSize(true);
        recycler_view_report.setLayoutManager(new LinearLayoutManager(this.getContext()));
        userReportList = new ArrayList<>();
        reporterList = new ArrayList<>();
        userReportAdapter = new ReporterAdapter(this.getContext(), userReportList, reporterList);
        recycler_view_report.setAdapter(userReportAdapter);

        options = view.findViewById(R.id.options);
        options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog = new BottomSheetDialog(
                        view.getContext(), R.style.BottomSheetDialogTheme
                );
                View bottomSheetView = LayoutInflater.from(view.getContext()).inflate
                        (
                                R.layout.layout_bottom_alluser_sheet,
                                (LinearLayout)view.findViewById(R.id.bottomSheetContainer)
                        );
                TextView tv_unban = bottomSheetView.findViewById(R.id.ban);
                bottomSheetView.findViewById(R.id.ban).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (profileid !=null) {
                            FirebaseDatabase.getInstance().getReference().child("Ban").child("Users").child(profileid).setValue(true);
                        }
                        else {
                            FirebaseDatabase.getInstance().getReference().child("Ban").child("Posts").child(postid).setValue(true);
                        }
                        bottomSheetDialog.cancel();
                        FragmentManager fragmentManager = getParentFragmentManager();
                        fragmentManager.popBackStack();
//                        fragmentManager.popBackStack();
//                        Fragment fragment = new AdminReportFragment();
//                        FragmentManager newfragmentManager = getParentFragmentManager();
//                        FragmentTransaction fragmentTransaction = newfragmentManager.beginTransaction();
//                        fragmentTransaction.replace(R.id.fragment_container, fragment).addToBackStack(null).commit();

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
        });
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void getPostInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts").child(postid);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Post post = snapshot.getValue(Post.class);
                idpost.setText(post.getPostid());
                description.setText(post.getDescription());
                Picasso.get().load(post.getPostimage()).placeholder(R.mipmap.ic_launcher).into(post_image);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void getReporter(String tag) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Report")
                .child(tag);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                reporterList.clear();
                for (DataSnapshot item : snapshot.getChildren()) {
                    Reporter reporter = item.getValue(Reporter.class);
                    if (tag.equals("User")) {
                        if (reporter.getReported().equals(profileid)) {
                            reporterList.add(reporter);
                        }
                    }
                    else if (tag.equals("Post")) {
                        if (reporter.getReported().equals(postid)) {
                            reporterList.add(reporter);
                        }
                    }
                }
                userReportAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}