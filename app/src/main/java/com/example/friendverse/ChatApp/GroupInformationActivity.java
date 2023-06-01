package com.example.friendverse.ChatApp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.friendverse.Adapter.AddUserToGroupAdapter;
import com.example.friendverse.Model.ChatMessage;
import com.example.friendverse.Model.User;
import com.example.friendverse.R;
import com.example.friendverse.databinding.ActivityGroupInfomationBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;

public class GroupInformationActivity extends AppCompatActivity {

    private static final int GALLERY_REQ_CODE = 3000;
    private Uri imageUri;
    private View bottomSheetView;
    private ActivityGroupInfomationBinding activityGroupInfomationBinding;
    private String groupId;
    private String groupImage;
    private String groupName;
    private String groupAdminId;
    private String groupConversationId;
    StorageReference storageReference;
    private String myUri;

    private FirebaseUser firebaseUser;

    private List<User> userIsFollowing;
    private List<User> userIsFollowingDefault;
    private List<String> userIsFollowingID;
    private List<String> userInGroupID;
    private AddUserToGroupAdapter addUserToGroupAdapter;
    private DatabaseReference reference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityGroupInfomationBinding = ActivityGroupInfomationBinding.inflate(getLayoutInflater());
        setContentView(activityGroupInfomationBinding.getRoot());

        //Danh sach cac user dang follow minh
        userIsFollowing = new ArrayList<>();
        userIsFollowingID = new ArrayList<>();
        userIsFollowingDefault = new ArrayList<>();
        userInGroupID=new ArrayList<>();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference("chatImages");
        Intent i = getIntent();
        if (i.getExtras() != null) {
            if (i.getStringExtra("interacter") != null) {
                groupId = i.getStringExtra("interacter");
                getGroupAdmin();
                getMemberIdOfGroup();
                updateMemBerOfGroup();
                if (i.getStringExtra("interacterImage") != null) {
                    groupImage = i.getStringExtra("interacterImage");
                }
                if (i.getStringExtra("interacterFullName") != null) {
                    groupName = i.getStringExtra("interacterFullName");
                }
                if (i.getStringExtra("conversationId") != null) {
                    groupConversationId = i.getStringExtra("conversationId");
                }
            }
        }
        activityGroupInfomationBinding.userNameTextView.setText(groupName);
        Glide.with(this).load(groupImage).into(activityGroupInfomationBinding.imageProfileBtn);

        //Action
        activityGroupInfomationBinding.imageSent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i =new Intent(getApplicationContext(), SentImagesGroupActivity.class);
                i.putExtra("groupId", groupId);
                startActivity(i);
            }
        });
        activityGroupInfomationBinding.memberList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i =new Intent(getApplicationContext(), MembersGroupActivity.class);
                i.putExtra("groupId", groupId);
                i.putExtra("groupAdminId", groupAdminId);

                startActivity(i);
            }
        });

        activityGroupInfomationBinding.LeaveTheGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (firebaseUser.getUid().equals(groupAdminId)){
                    String replaceAdmin="";
                    for (String user1:userInGroupID){
                        if (!user1.equals(groupAdminId)){
                            replaceAdmin= user1;
                            break;
                        }
                    }
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("GroupChats").child(groupId);
                    reference.child("admin").setValue(replaceAdmin);
                    reference = FirebaseDatabase.getInstance().getReference().child("GroupChats").child(groupId).child("members");
                    reference.child(firebaseUser.getUid()).removeValue();
                }
                else {
                    reference = FirebaseDatabase.getInstance().getReference().child("GroupChats").child(groupId).child("members");
                    reference.child(firebaseUser.getUid()).removeValue();
                }
                Intent i= new Intent(getApplicationContext(),ChatActivity.class);
                finishAffinity();
                startActivity(i);

            }
        });
        //Action

        activityGroupInfomationBinding.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        activityGroupInfomationBinding.changeNameOrImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(
                        GroupInformationActivity.this
                );
                bottomSheetView = LayoutInflater.from(getApplicationContext()).inflate
                        (
                                R.layout.edit_group_info_layout,
                                null
                        );
                bottomSheetView.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bottomSheetDialog.cancel();
                    }
                });


                EditText groupNameChange = bottomSheetView.findViewById(R.id.groupNameChange);
                bottomSheetView.findViewById(R.id.finish).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DatabaseReference reference;

                        if (imageUri != null) {
                            runOnUiThread(() -> {
                                uploadImage();
                            });


                        }
                        if (!groupNameChange.getText().toString().trim().isEmpty()) {
                            reference = FirebaseDatabase.getInstance().getReference().child("GroupChats").child(groupId);
                            reference.child("groupName").setValue(groupNameChange.getText().toString());
                            activityGroupInfomationBinding.userNameTextView.setText(groupNameChange.getText().toString());

                            reference = FirebaseDatabase.getInstance().getReference().child(ChatMessage.KEY_COLLECTION_CONVERSATION).child(groupConversationId);
                            reference.child("receiverName").setValue(groupNameChange.getText().toString());
                        }
                        bottomSheetDialog.cancel();

                    }
                });
                bottomSheetView.findViewById(R.id.uploadImage).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent iGallery = new Intent(Intent.ACTION_PICK);
                        iGallery.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(iGallery, GALLERY_REQ_CODE);
                    }
                });

                groupNameChange.setHint(groupName);
                RoundedImageView groupImage1 = bottomSheetView.findViewById(R.id.imageProfileBtn);
                Glide.with(getApplicationContext()).load(groupImage).into(groupImage1);
                bottomSheetDialog.setContentView(bottomSheetView);
                bottomSheetDialog.show();
            }
        });


        activityGroupInfomationBinding.addMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userIsFollowingDefault.clear();

                userIsFollowing.clear();
                for (String id : userIsFollowingID) {
                    for (User user : ChatActivity.allUsers) {
                        if (user.getId().equals(id)&&!userInGroupID.contains(user.getId())) {
                            userIsFollowing.add(user);
                            userIsFollowingDefault.add(user);
                            break;
                        }
                    }
                }

                for (User user : userIsFollowingDefault) {
                    user.setSelected(false);
                }


                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(
                        GroupInformationActivity.this
                );
                View bottomSheetView = LayoutInflater.from(getApplicationContext()).inflate
                        (
                                R.layout.layout_add_member_group,
                                null
                        );


                bottomSheetView.findViewById(R.id.groupName).setVisibility(View.GONE);
                TextView title = bottomSheetView.findViewById(R.id.titleTextView);
                title.setTextColor(Color.WHITE);

                TextView addBtn = bottomSheetView.findViewById(R.id.create);
                addBtn.setText("Add");


                //Adapter - RecyclerView
                addUserToGroupAdapter = new AddUserToGroupAdapter(userIsFollowing, userIsFollowingDefault);

                RecyclerView users = bottomSheetView.findViewById(R.id.userRecyclerView);
                users.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                users.setAdapter(addUserToGroupAdapter);


                bottomSheetView.findViewById(R.id.create).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (numberOfMember() == 0) {
                            bottomSheetDialog.cancel();
                            return;
                        }
                        DatabaseReference reference;
                        List<User> members = new ArrayList<>();
                        for (User user : userIsFollowingDefault) {
                            if (user.isSelected()) {
                                members.add(user);
                            }
                        }
                        for (User user : members) {
                            reference = FirebaseDatabase.getInstance().getReference("GroupChats").child(groupId).child("members");
                            reference.child(user.getId()).setValue(user);
                            userIsFollowing.remove(user);
                            userIsFollowingDefault.remove(user);
                        }
                        addUserToGroupAdapter.notifyDataSetChanged();
                        bottomSheetDialog.cancel();


                    }
                });
                bottomSheetView.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bottomSheetDialog.cancel();
                    }
                });


                EditText search = bottomSheetView.findViewById(R.id.search);
                search.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        searchUser(s.toString().toLowerCase(), userIsFollowing);

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        searchUser(s.toString(), userIsFollowing);

                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });

                bottomSheetDialog.setContentView(bottomSheetView);
                bottomSheetDialog.show();
            }
        });
    }

    private void updateMemBerOfGroup(){
        DatabaseReference reference;
        reference = FirebaseDatabase.getInstance().getReference("Follow").child(firebaseUser.getUid()).child("followers");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userIsFollowingID.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (!userInGroupID.contains(snapshot.getKey().toString()))
                        userIsFollowingID.add(snapshot.getKey());
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void getMemberIdOfGroup() {
        DatabaseReference reference;
        reference = FirebaseDatabase.getInstance().getReference("GroupChats").child(groupId).child("members");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1:snapshot.getChildren()){
                    userInGroupID.add(snapshot1.getKey());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private int numberOfMember() {
        int count = 0;
        for (User user : userIsFollowingDefault) {
            if (user.isSelected()) {
                count++;
            }
        }
        return count;
    }

    private void searchUser(String toString, List<User> userList) {
        userList.clear();
        if (toString.equals("")) {
            userList.addAll(userIsFollowingDefault);
        } else {
            for (User user : userIsFollowingDefault) {
                if (user.getUsername().startsWith(toString)) {
                    userList.add(user);
                }
            }
        }

        addUserToGroupAdapter.notifyDataSetChanged();


    }

    private void getGroupAdmin() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("GroupChats").child(groupId);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupAdminId = snapshot.child("admin").getValue().toString();
                String id =firebaseUser.getUid();
                if (groupAdminId.trim().equals(id.trim())) {
                    activityGroupInfomationBinding.addMember.setVisibility(View.VISIBLE);
                }
                else {
                    activityGroupInfomationBinding.addMember.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == GALLERY_REQ_CODE) {
                imageUri = data.getData();
                RoundedImageView roundedImageView = bottomSheetView.findViewById(R.id.imageProfileBtn);
                roundedImageView.setImageURI(imageUri);

            }
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();

        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }


    private void uploadImage() {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sending");
        progressDialog.show();

        if (imageUri != null) {
            final StorageReference filereference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));

            filereference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    filereference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            myUri = uri.toString();
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(ChatMessage.KEY_COLLECTION_CONVERSATION);
                            reference.child(groupConversationId).child("receiverImage").setValue(myUri);
                            reference = FirebaseDatabase.getInstance().getReference().child("GroupChats").child(groupId);
                            reference.child("image").setValue(myUri);
                            Glide.with(getApplicationContext()).load(myUri).into(activityGroupInfomationBinding.imageProfileBtn);
                            progressDialog.show();
                        }
                    });
                }
            });

        } else {
            Toast.makeText(this, "No image selected!", Toast.LENGTH_SHORT).show();
        }

    }
}