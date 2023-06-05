package com.example.friendverse.Fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.friendverse.MainActivity;
import com.example.friendverse.Model.Notification;
import com.example.friendverse.Model.User;
import com.example.friendverse.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ReportUserFragment extends Fragment {
    ImageView back;
    EditText field_report;
    Button report_btn;
    String profileid, postid, username;
    FirebaseUser firebaseUser;
    public ReportUserFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_report_user, container, false);
        profileid = getArguments().getString("profileid");
        postid = getArguments().getString("postid");

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        username = "" ;
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot item : snapshot.getChildren()) {
                    if (item.getKey().equals(firebaseUser.getUid())) {
                        User user = item.getValue(User.class);
                        username = user.getUsername();
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        back = view.findViewById(R.id.close);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getParentFragmentManager();
                fragmentManager.popBackStack();
            }
        });

        report_btn = view.findViewById(R.id.report_btn);
        field_report = view.findViewById(R.id.field_report);
        report_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (profileid != null) {
                    if (field_report.getText().toString() == null || field_report.getText().toString().equals("")) {
                        Toast.makeText(view.getContext(), "Please field to report", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        String id = FirebaseDatabase.getInstance().getReference().child("Report").child("User").push().getKey();
                        FirebaseDatabase.getInstance().getReference().child("Report").child("User").child(id).child("id").setValue(id);
                        FirebaseDatabase.getInstance().getReference().child("Report").child("User").child(id).child("username").setValue(username);
                        FirebaseDatabase.getInstance().getReference().child("Report").child("User").child(id).child("reported").setValue(profileid);
                        FirebaseDatabase.getInstance().getReference().child("Report").child("User").child(id).child("report").setValue(field_report.getText().toString());
                        FirebaseDatabase.getInstance().getReference().child("Report").child("User").child(id).child("reporter").setValue(firebaseUser.getUid());
                        Toast.makeText(view.getContext(), "Reported", Toast.LENGTH_SHORT).show();
                        field_report.setText("");
                    }
                }
                else {
                    if (field_report.getText().toString() == null || field_report.getText().toString().equals("")) {
                        Toast.makeText(view.getContext(), "Please field to report", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        String id = FirebaseDatabase.getInstance().getReference().child("Report").child("Post").push().getKey();
                        FirebaseDatabase.getInstance().getReference().child("Report").child("Post").child(id).child("id").setValue(id);
                        FirebaseDatabase.getInstance().getReference().child("Report").child("Post").child(id).child("username").setValue(username);
                        FirebaseDatabase.getInstance().getReference().child("Report").child("Post").child(id).child("reported").setValue(postid);
                        FirebaseDatabase.getInstance().getReference().child("Report").child("Post").child(id).child("report").setValue(field_report.getText().toString());
                        FirebaseDatabase.getInstance().getReference().child("Report").child("Post").child(id).child("reporter").setValue(firebaseUser.getUid());
                        Toast.makeText(view.getContext(), "Reported", Toast.LENGTH_SHORT).show();
                        field_report.setText("");
                    }
                }
            }
        });

        return view;
    }
}