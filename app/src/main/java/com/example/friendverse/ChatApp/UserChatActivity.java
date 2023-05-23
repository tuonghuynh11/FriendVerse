package com.example.friendverse.ChatApp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.friendverse.Adapter.UsersChatAdapter;
import com.example.friendverse.Model.User;
import com.example.friendverse.R;
import com.example.friendverse.databinding.ActivityUserChatBinding;
import com.example.friendverse.listeners.UserListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UserChatActivity extends AppCompatActivity implements UserListener {
    private ActivityUserChatBinding binding;
    private UsersChatAdapter usersChatAdapter;
    List<User> userList = new ArrayList<>();
    FirebaseUser firebaseUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_chat);
        binding = ActivityUserChatBinding.inflate(getLayoutInflater());
        usersChatAdapter = new UsersChatAdapter(userList, this);
        binding.userRecyclerView.setHasFixedSize(true);
        binding.userRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.userRecyclerView.setAdapter(usersChatAdapter);
        setContentView(binding.getRoot());
        getUsers();
        setListeners();
    }

    private void setListeners() {
        binding.imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void getUsers() {
        //Mốt chỉnh lại chỉ hiện thị những người đang followed
        loading(true);
        firebaseUser=FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId= firebaseUser.getUid();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");
        //DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                loading(false);
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    if (!(currentUserId.equals(user.getId()))) {
                        userList.add(user);
                    }
                }
                if (!(userList.size() > 0)) {
                    showErrorMessage();
                    return;
                }
                usersChatAdapter.notifyDataSetChanged();
                binding.userRecyclerView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void showErrorMessage() {
        binding.textErrorMessage.setText(String.format("%s", "No user available"));
        binding.textErrorMessage.setVisibility(View.VISIBLE);

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
        Intent intent= new Intent(getApplicationContext(),ChatScreenActivity.class);
        intent.putExtra(User.USERKEY,user);
        startActivity(intent);
        finish();
    }
}