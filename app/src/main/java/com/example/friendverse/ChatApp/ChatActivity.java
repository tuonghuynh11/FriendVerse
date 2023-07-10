package com.example.friendverse.ChatApp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.friendverse.Adapter.AddUserToGroupAdapter;
import com.example.friendverse.Adapter.RecentConversationAdapter;
import com.example.friendverse.Adapter.UserChatStatusAdapter;
import com.example.friendverse.Login.LoginActivity;
import com.example.friendverse.MainActivity;
import com.example.friendverse.Model.ChatMessage;
import com.example.friendverse.Model.Post;
import com.example.friendverse.Model.User;
import com.example.friendverse.Profile.SettingActivity;
import com.example.friendverse.R;
import com.example.friendverse.databinding.ActivityChatBinding;
import com.example.friendverse.listeners.ConversionListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
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
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

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
    public static List<User> allUsers;
    private List<User> userIsFollowing;
    private List<User> userIsFollowingDefault;
    private List<String> userIsFollowingID;
    private AddUserToGroupAdapter addUserToGroupAdapter;

    //Init voice call, video call
    private String token = "";
    public static com.stringee.StringeeClient client;
    public static Map<String, StringeeCall> callMap = new HashMap<>();
    public static Map<String, StringeeCall2> videocallMap = new HashMap<>();

    //Make conversation change to update recentConversation
    String conversationIdTest = "-NTdJJIMI2Bo6rz9IG5a";

    private User currentUser = new User();

    //Conversation Map
    private Map<String, List<String>> group = new HashMap<>();

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

        //Danh sach cac user dang follow minh
        userIsFollowing = new ArrayList<>();
        userIsFollowingID = new ArrayList<>();
        userIsFollowingDefault = new ArrayList<>();


        setListeners();
        init();
        initTokenCall();
        initUserActivity();

    }
 @Override
    protected void onResume() {
        super.onResume();
        try {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(auth.getCurrentUser().getUid());
            reference.child(User.ACTIVITYKEY).setValue(1);
        }
        catch (Exception ex){
            Log.e("TAG", "User not login");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(auth.getCurrentUser().getUid());
            reference.child(User.ACTIVITYKEY).setValue(0);
        }
        catch (Exception ex){
            Log.e("TAG", "User not login");
        }
    }
    public void initTokenCall() {
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

    public String initTokenCallId(){
        String to=genAccessToken("SK.0.pSlmNy1Ruqc7TGf3a473K8Vfcd8olWsH","R0RmNDNEZU1Fd2V0aFZ2Z2xLSjJtZkQ5cVNrTFhrVnI=",3600*60,currentUser.getId());
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(auth.getCurrentUser().getUid());
        reference.child(User.TOKENCALLKEY).setValue(to);
        return to;
    }
    public static String genAccessToken(String keySid, String keySecret, int expireInSecond, String userId) {
        try {
            Algorithm algorithmHS = Algorithm.HMAC256(keySecret);

            Map<String, Object> headerClaims = new HashMap<String, Object>();
            headerClaims.put("typ", "JWT");
            headerClaims.put("alg", "HS256");
            headerClaims.put("cty", "stringee-api;v=1");

            long exp = (long) (System.currentTimeMillis()) + expireInSecond * 1000;

            String token = JWT.create().withHeader(headerClaims)
                    .withClaim("jti", keySid + "-" + System.currentTimeMillis())
                    .withClaim("iss", keySid)
                    .withExpiresAt(new Date(exp))
                    .withClaim("userId",userId)
                    .sign(algorithmHS);

            return token;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
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
//                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(auth.getCurrentUser().getUid());
//                                reference.child(User.TOKENKEY).setValue(deviceToken);
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
        if (auth.getCurrentUser() == null)
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

    private void updateMapGroup() {
        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference().child("GroupChats");
        reference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot4) {
                group = new HashMap<>();
                for (DataSnapshot snapshot1 : snapshot4.getChildren()) {
                    group.put(snapshot1.getKey().toString(), new ArrayList<String>());
                    for (DataSnapshot snapshot2 : snapshot1.getChildren()) {
                        if (snapshot2.getKey().equals("members")) {
                            for (DataSnapshot snapshot3 : snapshot2.getChildren()) {
                                group.get(snapshot1.getKey()).add(snapshot3.getKey().toString());
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private boolean checkGroupHasUser(String userId, String groupChatId) {
        return group.get(groupChatId).contains(userId);
    }

    //CHeck cac group co user
    private void init() {
        updateMapGroup();
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
                    chatMessage.setConversionIdentify(snapshot.getKey());
                    chatMessage.setReceiverId(snapshot.child(ChatMessage.RECEIVERIDKEY).getValue().toString());
                    chatMessage.setSenderId(snapshot.child(ChatMessage.SENDERIDKEY).getValue().toString());
                    allConversation.add(chatMessage);
                    if (firebaseUser.getUid() == null)
                        return ;
                    if (!(firebaseUser.getUid().equals(chatMessage.getSenderId())) && !(firebaseUser.getUid().equals(chatMessage.getReceiverId()))) {
                        if (chatMessage.getReceiverId().contains("group")) {
                            if (checkGroupHasUser(firebaseUser.getUid(), chatMessage.getReceiverId())) {
                                chatMessage.setConversionName(snapshot.child(ChatMessage.KEY_RECEIVER_NAME).getValue().toString());
                                chatMessage.setConversionImage(snapshot.child(ChatMessage.KEY_RECEIVER_IMAGE).getValue().toString());
                                chatMessage.setConversionId(snapshot.child(ChatMessage.RECEIVERIDKEY).getValue().toString());
                                chatMessage.setMessage(snapshot.child(ChatMessage.KEY_LAST_MESSAGE).getValue().toString());
                                chatMessage.setDateObject(snapshot.child(ChatMessage.DATETIMEKEY).getValue(Date.class));
                                recent.add(chatMessage);
                            }
                            ;
                        }
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


        reference = FirebaseDatabase.getInstance().getReference("Follow").child(firebaseUser.getUid()).child("followers");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userIsFollowingID.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    userIsFollowingID.add(snapshot.getKey());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        reference= FirebaseDatabase.getInstance().getReference().child(User.USERKEY).child(firebaseUser.getUid());
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user= snapshot.getValue(User.class);
                currentUser=user;
                //User
                activityChatBinding.userNameTextView.setText(currentUser.getFullname());
                Picasso.get().load(currentUser.getImageurl()).placeholder(R.drawable.default_avatar).into(activityChatBinding.imageProfile);
                //User
                ////Check if have tokenCall or not and assign token

                if (user.getTokenCall()==null){
                    currentUser.setTokenCall(initTokenCallId());
                }
                if (token==null||token == "") {
                    token = currentUser.getTokenCall();
                    initStringeeConnection();
                }

                ///

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    //    @Override
    private void setListeners() {
        activityChatBinding.fabNewChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), UserChatActivity.class));
            }
        });
        activityChatBinding.imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i= new Intent(getApplicationContext(), MainActivity.class);
                finishAffinity();
                startActivity(i);
            }
        });
        activityChatBinding.groupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userIsFollowingDefault.clear();

                reference = FirebaseDatabase.getInstance().getReference("Follow").child(firebaseUser.getUid()).child("followers");
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        userIsFollowingID.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            userIsFollowingID.add(snapshot.getKey());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                userIsFollowing.clear();
                for (String id : userIsFollowingID) {
                    for (User user : allUsers) {
                        if (user.getId().equals(id)) {
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
                        ChatActivity.this
                );
                View bottomSheetView = LayoutInflater.from(getApplicationContext()).inflate
                        (
                                R.layout.layout_add_member_group,
                                null
                        );

                addUserToGroupAdapter = new AddUserToGroupAdapter(userIsFollowing, userIsFollowingDefault);

                RecyclerView users = bottomSheetView.findViewById(R.id.userRecyclerView);
                users.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                users.setAdapter(addUserToGroupAdapter);


                EditText groupName = bottomSheetView.findViewById(R.id.groupName);
                bottomSheetView.findViewById(R.id.create).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (groupName.getText().toString().trim().isEmpty()) {
                            Toast.makeText(ChatActivity.this, "Group Name is Empty", Toast.LENGTH_SHORT).show();
                        } else if (numberOfMember() <= 1) {
                            Toast.makeText(ChatActivity.this, "Number of members must greater than 2", Toast.LENGTH_SHORT).show();
                        } else {
                            //Chat screen for group

                            List<User> members = new ArrayList<>();
                            for (User user : userIsFollowingDefault) {
                                if (user.isSelected()) {
                                    members.add(user);
                                }
                            }
                            reference = FirebaseDatabase.getInstance().getReference("GroupChats");
                            String key = reference.push().getKey() + "group";
                            HashMap<String, Object> message = new HashMap<>();
                            message.put("admin", firebaseUser.getUid());
                            message.put("groupName", groupName.getText().toString());
                            message.put("image", "https://www.shareicon.net/data/256x256/2016/06/30/788858_group_512x512.png");
                            message.put("members", "");
                            reference.child(key).setValue(message);


                            for (User user : members) {
                                reference = FirebaseDatabase.getInstance().getReference("GroupChats").child(key).child("members");
                                reference.child(user.getId()).setValue(user);
                            }
                            reference.child(currentUser.getId()).setValue(currentUser);
                            Intent intent = new Intent(getApplicationContext(), GroupChatScreenActivity.class);
                            User user = new User();
                            user.setId(key);
                            user.setUsername(groupName.getText().toString());
                            user.setImageurl("https://www.shareicon.net/data/256x256/2016/06/30/788858_group_512x512.png");


                            DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Chats");
                            //add first message
                            String key1 = reference.push().getKey() + "group";
                            HashMap<String, Object> message1 = new HashMap<>();
                            message1.put("id", key);
                            message1.put("senderId", firebaseUser.getUid());
                            message1.put("receiverId", user.getId());
                            message1.put("message", "Hello EveryBody");
                            message1.put("dateObject", new Date());
                            message1.put("messageType", "text");
                            reference1.child(key1).setValue(message1);

                            //add First conversation
                            HashMap<String, Object> conversion = new HashMap<>();
                            conversion.put(ChatMessage.SENDERIDKEY, firebaseUser.getUid());
                            conversion.put(ChatMessage.KEY_SENDER_NAME, currentUser.getFullname());
                            conversion.put(ChatMessage.KEY_SENDER_IMAGE, currentUser.getImageurl());
                            conversion.put(ChatMessage.RECEIVERIDKEY, user.getId());
                            conversion.put(ChatMessage.KEY_RECEIVER_NAME, user.getUsername());
                            conversion.put(ChatMessage.KEY_RECEIVER_IMAGE, user.getImageurl());
                            conversion.put(ChatMessage.KEY_LAST_MESSAGE, "Hello Everybody");
                            conversion.put(ChatMessage.DATETIMEKEY, new Date());
                            reference1 = FirebaseDatabase.getInstance().getReference(ChatMessage.KEY_COLLECTION_CONVERSATION);
                            String conversationId = reference1.push().getKey() + "group";
                            reference1.child(conversationId).setValue(conversion);


                            //bio is number of members
                            user.setBio("" + members.size());
                            intent.putExtra(User.USERKEY, user);
                            startActivity(intent);


                            bottomSheetDialog.cancel();

                        }
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
                Log.e("Exception", e.getMessage());
            }
        });

    }

    private void login() {
        //       register("a","Nguyen Van A","a@gmail.com","123456789");
//
//        String email = "a@gmail.com";
//        String password = "123456789";
//
        String email = "c@gmail.com";
        String password = "123456789";
//        String email = "nguyenthaicong265@gmail.com";
//        String password = "thaicong";
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
                            currentUser = user;
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
        Intent intent;
        if (user.getId().contains("group")) {
            intent = new Intent(getApplicationContext(), GroupChatScreenActivity.class);
        } else {
            intent = new Intent(getApplicationContext(), ChatScreenActivity.class);

        }
        intent.putExtra(User.USERKEY, user);
        startActivity(intent);
    }

    @Override
    public void onConversionClicked(Post post) {

    }


}
