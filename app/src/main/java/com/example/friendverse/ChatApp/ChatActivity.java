package com.example.friendverse.ChatApp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.friendverse.Adapter.RecentConversationAdapter;
import com.example.friendverse.Adapter.UserChatStatusAdapter;
import com.example.friendverse.Model.ChatMessage;
import com.example.friendverse.Model.User;
import com.example.friendverse.R;
import com.example.friendverse.databinding.ActivityChatBinding;
import com.example.friendverse.listeners.ConversionListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;
import com.stringee.StringeeClient;
import com.stringee.call.StringeeCall;
import com.stringee.call.StringeeCall2;
import com.stringee.exception.StringeeError;
import com.stringee.listener.StatusListener;
import com.stringee.listener.StringeeConnectionListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ChatActivity extends AppCompatActivity implements ConversionListener {
    private ActivityChatBinding activityChatBinding;
    private FirebaseAuth auth;
    private DatabaseReference reference;
    private List<ChatMessage> conversation;
    private List<ChatMessage> allConversation;
    private RecentConversationAdapter recentConversationAdapter;
    private FirebaseUser firebaseUser;

    private UserChatStatusAdapter userChatStatusAdapter;
    private List<User> activityUsers;
    private List<User> allUsers;

    //Init voice call, video call
    private String token = "";
    public static com.stringee.StringeeClient client;
    public static Map<String, StringeeCall> callMap = new HashMap<>();
    public static Map<String, StringeeCall2> videocallMap = new HashMap<>();

    //Make conversation change to update recentConversation
    String conversationIdTest = "-NTdJJIMI2Bo6rz9IG5a";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_chat);
        activityChatBinding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(activityChatBinding.getRoot());
        auth = FirebaseAuth.getInstance();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        allUsers = new ArrayList<>();
        activityUsers = new ArrayList<>();
        conversation = new ArrayList<>();
        setListeners();
        init();
        login();
     //   initTokenCall();
        initUserActivity();

    }

    public void initTokenCall(){
        runOnUiThread(() -> {
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                @Override
                public void onComplete(@NonNull Task<String> task) {
                    if (task.isSuccessful()) {
                        String deviceToken = task.getResult();

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(auth.getCurrentUser().getUid());
                        reference.child(User.TOKENKEY).setValue(deviceToken);
                    }
                }
            });
        });

    }
    public void initStringeeConnection() {
        client = new StringeeClient(this);
        client.setConnectionListener(new StringeeConnectionListener() {
            @Override
            public void onConnectionConnected(StringeeClient stringeeClient, boolean b) {
                runOnUiThread(() -> {
                    FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                        @Override
                        public void onComplete(@NonNull Task<String> task) {
                            if (task.isSuccessful()) {
                                String deviceToken = task.getResult();
                                client.registerPushToken(deviceToken, new StatusListener() {
                                    @Override
                                    public void onSuccess() {
                                        android.util.Log.d("SampleCall", "register push success");
                                    }
                                });
                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(auth.getCurrentUser().getUid());
                                reference.child(User.TOKENKEY).setValue(deviceToken);
                            }
                        }
                    });
                });

            }

            @Override
            public void onConnectionDisconnected(StringeeClient stringeeClient, boolean b) {
                runOnUiThread(() -> {
                    Toast.makeText(ChatActivity.this, "The connection is disconnected", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onIncomingCall(StringeeCall stringeeCall) {
                runOnUiThread(() -> {
                    callMap.put(stringeeCall.getCallId(), stringeeCall);
                    Intent intent = new Intent(ChatActivity.this, CallActivity.class);
                    intent.putExtra("callId", stringeeCall.getCallId());
                    intent.putExtra("isInComingCall", true);
                    startActivity(intent);
                });
            }

            @Override
            public void onIncomingCall2(StringeeCall2 stringeeCall2) {
                runOnUiThread(() -> {
                    videocallMap.put(stringeeCall2.getCallId(), stringeeCall2);
                    Intent intent = new Intent(ChatActivity.this, videoCallActivity.class);
                    intent.putExtra("callId", stringeeCall2.getCallId());
                    intent.putExtra("isInComingCall", true);
                    startActivity(intent);
                });
            }

            @Override
            public void onConnectionError(StringeeClient stringeeClient, StringeeError stringeeError) {
                runOnUiThread(() -> {
                    Toast.makeText(ChatActivity.this, stringeeError.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onRequestNewToken(StringeeClient stringeeClient) {

            }

            @Override
            public void onCustomMessage(String s, JSONObject jsonObject) {

            }

            @Override
            public void onTopicMessage(String s, JSONObject jsonObject) {

            }
        });
        client.connect(token);
    }

    private void initUserActivity() {


        reference = FirebaseDatabase.getInstance().getReference().child(User.USERKEY);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (auth.getCurrentUser() == null)
                    return;
                activityUsers.clear();
                allUsers.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);

                    if (!(user.getId().equals(firebaseUser.getUid())) && user.getActivity() == 1) {
                        if (allConversation != null && isFollowed(user))
                            activityUsers.add(user);
                    }

                    allUsers.add(user);
                }

                if (activityUsers.size() != 0) {
                    userChatStatusAdapter.notifyDataSetChanged();
                    activityChatBinding.userChatStatusRecyclerView.setVisibility(View.VISIBLE);
                } else {
                    // userChatStatusAdapter.notifyDataSetChanged();
                    activityChatBinding.userChatStatusRecyclerView.setVisibility(View.GONE);
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        if (auth.getCurrentUser()==null)
            return;
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(auth.getCurrentUser().getUid());
        reference.child(User.ACTIVITYKEY).setValue(0);
        reference.child(User.ACTIVITYKEY).setValue(1);
    }

    private boolean isFollowed(User user) {
        for (ChatMessage chatMessage : allConversation) {
            if (chatMessage.getSenderId().equals(user.getId()) || chatMessage.getReceiverId().equals(user.getId())) {
                return true;
            }
        }
        return false;
    }

    private void init() {
        conversation = new ArrayList<>();
        allConversation = new ArrayList<>();
        Set<ChatMessage> recent = new HashSet<>();
        recentConversationAdapter = new RecentConversationAdapter(conversation, this, allUsers);
        activityChatBinding.conversationRecyclerView.setAdapter(recentConversationAdapter);

        activityUsers = new ArrayList<>();
        userChatStatusAdapter = new UserChatStatusAdapter(activityUsers, this);
        activityChatBinding.userChatStatusRecyclerView.setAdapter(userChatStatusAdapter);

        reference = FirebaseDatabase.getInstance().getReference().child(ChatMessage.KEY_COLLECTION_CONVERSATION);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Set<ChatMessage> recent = new HashSet<>();
                conversation.clear();
                if (auth.getCurrentUser() == null)
                    return;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.setReceiverId(snapshot.child(ChatMessage.RECEIVERIDKEY).getValue().toString());
                    chatMessage.setSenderId(snapshot.child(ChatMessage.SENDERIDKEY).getValue().toString());
                    allConversation.add(chatMessage);
                    if (firebaseUser.getUid()==null)
                        return;
                    if (!(firebaseUser.getUid().equals(chatMessage.getSenderId())) && !(firebaseUser.getUid().equals(chatMessage.getReceiverId()))) {
                        continue;
                    }
                    if (firebaseUser.getUid().equals(chatMessage.getSenderId())) {
                        chatMessage.setConversionName(snapshot.child(ChatMessage.KEY_RECEIVER_NAME).getValue().toString());
                        chatMessage.setConversionImage(snapshot.child(ChatMessage.KEY_RECEIVER_IMAGE).getValue().toString());
                        chatMessage.setConversionId(snapshot.child(ChatMessage.RECEIVERIDKEY).getValue().toString());


                    } else {

                        chatMessage.setConversionName(snapshot.child(ChatMessage.KEY_SENDER_NAME).getValue().toString());
                        chatMessage.setConversionImage(snapshot.child(ChatMessage.KEY_SENDER_IMAGE).getValue().toString());
                        chatMessage.setConversionId(snapshot.child(ChatMessage.SENDERIDKEY).getValue().toString());
                    }
                    chatMessage.setMessage(snapshot.child(ChatMessage.KEY_LAST_MESSAGE).getValue().toString());
                    chatMessage.setDateObject(snapshot.child(ChatMessage.DATETIMEKEY).getValue(Date.class));

                    recent.add(chatMessage);

                }
                conversation.addAll(recent);
                Collections.sort(conversation, (obj1, obj2) -> obj1.getDateObject().compareTo(obj2.getDateObject()));
                recentConversationAdapter.notifyDataSetChanged();
                activityChatBinding.conversationRecyclerView.smoothScrollToPosition(0);
                activityChatBinding.conversationRecyclerView.setVisibility(View.VISIBLE);
                activityChatBinding.progressBar.setVisibility(View.GONE);
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        reference = FirebaseDatabase.getInstance().getReference().child(ChatMessage.KEY_COLLECTION_CONVERSATION).child(conversationIdTest);
        reference.child(ChatMessage.KEY_LAST_MESSAGE).setValue("a");

    }

    //Lỗi thoát app nhưng không set lại activity, lỗi recent chat
    @Override
    protected void onDestroy() {
        super.onDestroy();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(auth.getCurrentUser().getUid());
        reference.child(User.ACTIVITYKEY).setValue(0);
    }


    //Mốt thêm hai cái function này vào màn main/////////////////////////


    //    @Override
    protected void onResume() {
        super.onResume();
        if (auth.getCurrentUser()==null)
            return;
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(auth.getCurrentUser().getUid());
        reference.child(User.ACTIVITYKEY).setValue(1);
    }

    private void setListeners() {
        activityChatBinding.fabNewChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), UserChatActivity.class));
            }
        });
    }

    private void register(final String username, final String fullname, String email, String password) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(ChatActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser firebaseUser = auth.getCurrentUser();
                    String userid = firebaseUser.getUid();

                    reference = FirebaseDatabase.getInstance().getReference().child(User.USERKEY).child(userid);

                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("id", userid);
                    hashMap.put("username", username.toLowerCase());
                    hashMap.put("fullname", fullname);
                    hashMap.put("bio", "");
                    hashMap.put("imageurl", "default");

                    reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                        }
                    });

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("Exception",e.getMessage());
            }
        });

    }

    private void login() {
        //       register("a","Nguyen Van A","a@gmail.com","123456789");

        String email = "a@gmail.com";
        String password = "123456789";

//        String email = "c@gmail.com";
//        String password = "123456789";
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(ChatActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(auth.getCurrentUser().getUid());
                    reference.child(User.ACTIVITYKEY).setValue(1);

                    reference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                            Intent intent = new Intent(ChatActivity.this , ChatActivity.class);
//                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                            startActivity(intent);
//                            finish();
                            User user = dataSnapshot.getValue(User.class);
                            //Mốt đem lên hàm oncreate
                            activityChatBinding.userNameTextView.setText(user.getFullname());
                            if (token == "") {
                                token = user.getTokenCall();
                                initStringeeConnection();
                            }

                            Picasso.get().load(user.getImageurl()).placeholder(R.drawable.default_avatar).into(activityChatBinding.imageProfile);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            reference.child(User.ACTIVITYKEY).setValue(0);

                        }
                    });
                } else {
                    Toast.makeText(ChatActivity.this, "Authentication Failed!", Toast.LENGTH_SHORT).show();
                }
            }
        });


//        AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
//        builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
//                .setNegativeButton("No", dialogClickListener).create().show();
        //      register("b","Nguyen Van B","b@gmail.com","123456789");
//        register("c","Nguyen Van C","c@gmail.com","123456789");
    }

    @Override
    public void onConversionClicked(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatScreenActivity.class);
        intent.putExtra(User.USERKEY, user);
        startActivity(intent);
    }
}