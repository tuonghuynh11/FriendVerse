package com.example.friendverse.ChatApp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.friendverse.Adapter.UsersChatAdapter;
import com.example.friendverse.Model.ChatMessage;
import com.example.friendverse.Model.User;
import com.example.friendverse.R;
import com.example.friendverse.databinding.ActivityMembersGroup2Binding;
import com.example.friendverse.databinding.ActivityUserChatBinding;
import com.example.friendverse.listeners.UserListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;

public class MembersGroupActivity extends AppCompatActivity implements UserListener {
    private UsersChatAdapter usersChatAdapter;
    List<User> userList = new ArrayList<>();

    List<String> membersId = new ArrayList<>();
    private ActivityMembersGroup2Binding binding;
    private String groupId;
    private String groupAdminId;
    private View bottomSheetView;
    private FirebaseUser firebaseUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_members_group2);
        binding = ActivityMembersGroup2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        usersChatAdapter = new UsersChatAdapter(userList, this);
        binding.memberRecyclerView.setHasFixedSize(true);
        binding.memberRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.memberRecyclerView.setAdapter(usersChatAdapter);

        if (getIntent().getExtras() != null) {
            if (getIntent().getStringExtra("groupId") != null) {
                groupId = getIntent().getStringExtra("groupId").toString();
            }if (getIntent().getStringExtra("groupAdminId") != null) {
                groupAdminId = getIntent().getStringExtra("groupAdminId").toString();
            }
        }

        DatabaseReference reference =  FirebaseDatabase.getInstance().getReference().child("GroupChats").child(groupId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupAdminId= snapshot.child("admin").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        getUsers();
        setListeners();

    }

    private void setListeners() {
        binding.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void getUsers() {
        loading(true);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("GroupChats").child(groupId).child("members");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                loading(false);
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    userList.add(user);
                }

                usersChatAdapter.notifyDataSetChanged();
                binding.memberRecyclerView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    public void onUserClicked(User user) {


        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(
                MembersGroupActivity.this
        );
        bottomSheetView = LayoutInflater.from(getApplicationContext()).inflate
                (
                        R.layout.member_option_layout,
                        null
                );
        bottomSheetView.findViewById(R.id.viewProfile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Thêm sau
                bottomSheetDialog.cancel();
            }
        });

        if (userList.size()<=3){
            bottomSheetView.findViewById(R.id.removeUser).setVisibility(View.GONE);
        }
        else if (groupAdminId.equals(firebaseUser.getUid())){
            bottomSheetView.findViewById(R.id.removeUser).setVisibility(View.VISIBLE);
        }
        else {
            bottomSheetView.findViewById(R.id.removeUser).setVisibility(View.GONE);

        }
        bottomSheetView.findViewById(R.id.makeConversation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ChatScreenActivity.class);
                intent.putExtra(User.USERKEY, user);
                startActivity(intent);
                finish();
                bottomSheetDialog.cancel();
            }
        });
        if (user.getId().equals(groupAdminId)){
            TextView textView = bottomSheetView.findViewById(R.id.removeUser);
            textView.setText("Leave The Group");
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String replaceAdmin="";
                    for (User user1:userList){
                        if (!user1.getId().equals(groupAdminId)){
                            replaceAdmin= user1.getId();
                            break;
                        }
                    }
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("GroupChats").child(groupId);
                    reference.child("admin").setValue(replaceAdmin);
                    reference = FirebaseDatabase.getInstance().getReference().child("GroupChats").child(groupId).child("members");
                    reference.child(user.getId()).removeValue();

                    Intent i= new Intent(getApplicationContext(),ChatActivity.class);
                    finishAffinity();
                    startActivity(i);
                }
            });
            bottomSheetView.findViewById(R.id.makeConversation).setVisibility(View.GONE);
        }
        else{
            bottomSheetView.findViewById(R.id.removeUser).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("GroupChats").child(groupId).child("members");
                    reference.child(user.getId()).removeValue();
                    bottomSheetDialog.cancel();
                }
            });

        }

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }


}