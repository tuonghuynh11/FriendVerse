package com.example.friendverse.ChatApp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.devlomi.record_view.OnRecordListener;
import com.example.friendverse.Adapter.ChatAdapter;
import com.example.friendverse.Model.ChatMessage;
import com.example.friendverse.Model.User;
import com.example.friendverse.R;
import com.example.friendverse.Service.MyFirebaseMessagingService;
import com.example.friendverse.databinding.ActivityChatScreenBinding;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChatScreenActivity extends AppCompatActivity {
    private User receiverUser;
    private User senderUser;
    private ActivityChatScreenBinding activityChatScreenBinding;

    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private FirebaseUser firebaseUser;
    private DatabaseReference reference;
    private String conversationId = null;

    //Cấp quyền truy cập audio
    private static final int AUDIO_PERMISSION_CODE = 3;
    //Audio
    private MediaRecorder mediaRecorder;
    private String audioPath;

    //Code cấp quyền truy cập ảnh
    private final int GALLERY_REQ_CODE = 1000;

    //Cấp quyền truy cập camera

    private static final int CAMERA_PERMISSION_CODE = 1;
    private static final int CAMERA_REQUEST_CODE = 2;

    //Upload Image
    Uri imageUri;
    String myUrl;
    StorageTask uploadTask;
    StorageReference storageReference;

    //Call video, voice call

//    private String token="";
//    public static com.stringee.StringeeClient client;
//    public static Map<String, StringeeCall> callMap= new HashMap<>();
//    public static Map<String, StringeeCall2> videocallMap= new HashMap<>();


    //Notification
    private int isNotification = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_screen);
        activityChatScreenBinding = ActivityChatScreenBinding.inflate(getLayoutInflater());
        setContentView(activityChatScreenBinding.getRoot());
        storageReference = FirebaseStorage.getInstance().getReference("chatImages");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (getIntent().getExtras()!=null){
            if (getIntent().getStringExtra("notification")!=null){
                isNotification=1;
            }
        }
        loadReceiverDetails();
        setListeners();
        init();
        listenMessages();
        MyFirebaseMessagingService.cancelNotification(this);
    }

    private void init() {
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(
                receiverUser.getImageurl(),
                chatMessages,
                firebaseUser.getUid(),
                ChatScreenActivity.this
        );
        activityChatScreenBinding.chatRecyclerView.setAdapter(chatAdapter);
        reference = FirebaseDatabase.getInstance().getReference().child("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        reference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                senderUser = dataSnapshot.getValue(User.class);
//                token=senderUser.getTokenCall();
//                initStringeeConnection();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        checkConversionID();


    }

    private void listenMessages() {
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int count = chatMessages.size();
                chatMessages.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ChatMessage chatMessage = snapshot.getValue(ChatMessage.class);
                    if (chatMessage.getSenderId().equals(firebaseUser.getUid()) && chatMessage.getReceiverId().equals(receiverUser.getId())) {
                        chatMessage.setDateTime(getDateTime(chatMessage.getDateObject()));
                        chatMessages.add(chatMessage);
                    } else if (chatMessage.getReceiverId().equals(firebaseUser.getUid()) && chatMessage.getSenderId().equals(receiverUser.getId())) {
                        chatMessage.setDateTime(getDateTime(chatMessage.getDateObject()));
                        chatMessages.add(chatMessage);
                    }
                }
                Collections.sort(chatMessages, (obj1, obj2) -> obj1.getDateObject().compareTo(obj2.getDateObject()));
                if (chatMessages.size() == count) {
                    chatAdapter.notifyDataSetChanged();

                } else {
                    chatAdapter.notifyItemRangeInserted(chatMessages.size(), chatMessages.size());
                    activityChatScreenBinding.chatRecyclerView.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
                    activityChatScreenBinding.chatRecyclerView.setHasFixedSize(false);

                }
                activityChatScreenBinding.chatRecyclerView.setVisibility(View.VISIBLE);
                activityChatScreenBinding.progressBar.setVisibility(View.GONE);
//                chatAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkConversionID() {
        DatabaseReference reference2 = FirebaseDatabase.getInstance().getReference(ChatMessage.KEY_COLLECTION_CONVERSATION);

        reference2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String senderid = snapshot.child(ChatMessage.SENDERIDKEY).getValue().toString();
                    String receiverid = snapshot.child(ChatMessage.RECEIVERIDKEY).getValue().toString();
                    if ((senderUser.getId().equals(senderid) && receiverUser.getId().equals(receiverid)) || (senderUser.getId().equals(receiverid) && receiverUser.getId().equals(senderid))) {
                        conversationId = snapshot.getKey().toString();
                        if (chatAdapter.conversionID == null)
                            chatAdapter.conversionID = conversationId;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void sendMessage() {
        if (activityChatScreenBinding.inputMessage.getText().toString() == "" || activityChatScreenBinding.inputMessage.getText().toString().isEmpty()) {
            Toast.makeText(this, "Can not send an empty message", Toast.LENGTH_SHORT).show();
            return;
        }
        String key = reference.push().getKey();
        HashMap<String, Object> message = new HashMap<>();
        message.put("id", key);
        message.put("senderId", firebaseUser.getUid());
        message.put("receiverId", receiverUser.getId());
        message.put("message", activityChatScreenBinding.inputMessage.getText().toString());
        message.put("dateObject", new Date());
        message.put("messageType", "text");
        reference.child(key).setValue(message);

        getToken(activityChatScreenBinding.inputMessage.getText().toString(), senderUser.getUsername(), senderUser.getFullname(), senderUser.getId(), receiverUser.getId(), senderUser.getImageurl());
        if (conversationId != null) {
            updateConversion(activityChatScreenBinding.inputMessage.getText().toString());
        } else {
            HashMap<String, Object> conversion = new HashMap<>();
            conversion.put(ChatMessage.SENDERIDKEY, firebaseUser.getUid());
            conversion.put(ChatMessage.KEY_SENDER_NAME, senderUser.getFullname());
            conversion.put(ChatMessage.KEY_SENDER_IMAGE, senderUser.getImageurl());
            conversion.put(ChatMessage.RECEIVERIDKEY, receiverUser.getId());
            conversion.put(ChatMessage.KEY_RECEIVER_NAME, receiverUser.getFullname());
            conversion.put(ChatMessage.KEY_RECEIVER_IMAGE, receiverUser.getImageurl());
            conversion.put(ChatMessage.KEY_LAST_MESSAGE, activityChatScreenBinding.inputMessage.getText().toString());
            conversion.put(ChatMessage.DATETIMEKEY, new Date());
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference(ChatMessage.KEY_COLLECTION_CONVERSATION);
            conversationId = reference.push().getKey();
            if (chatAdapter.conversionID == null)
                chatAdapter.conversionID = conversationId;
            chatAdapter.lastConversion = activityChatScreenBinding.inputMessage.getText().toString();

            reference.child(conversationId).setValue(conversion);


        }
        activityChatScreenBinding.inputMessage.setText(null);
    }

    //Push Notification
    private void getToken(String message, String username, String fullName, String userID, String hisId, String userImage) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(User.USERKEY).child(hisId);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String token = snapshot.child("token").getValue().toString();


                JSONObject to = new JSONObject();
                JSONObject data = new JSONObject();
                try {
                    data.put("title", username);
                    data.put("fullname", fullName);
                    data.put("username", username);
                    data.put("message", message);
                    data.put("hisID", userID);
                    data.put("hisImage", userImage);
                    data.put("type", "normal");


                    to.put("to", token);
                    to.put("data", data);

                    sendNotification(to);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void sendNotification(JSONObject to) {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, "https://fcm.googleapis.com/fcm/send", to, response -> {
            Log.d("notification", "sendNotification: " + response);
        }, error -> {
            Log.d("notification", "sendNotification: " + error);
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                Map<String, String> map = new HashMap<>();
                map.put("Authorization", "key=" + "AAAAlCYKR9Q:APA91bFogM0PXenfS8CZD8x8nNdgrZ0HAWagF87GRp_GTlER7FBUg1-dYb_sDjSCX2ABwTS4jOUEnbhAnxWW9K7yUi97sopHEiyhOuhNTgZdclzH3cdEkJJQudZjtwkST8FY3MHttnZx");
                map.put("Content-Type", "application/json");
                return map;
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        request.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(request);
    }

    //Push Notification

    private void updateConversion(String message) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(ChatMessage.KEY_COLLECTION_CONVERSATION).child(conversationId);
        reference.child(ChatMessage.KEY_LAST_MESSAGE).setValue(message);
        chatAdapter.lastConversion = message;

    }

    private void loadReceiverDetails() {
        receiverUser = (User) getIntent().getSerializableExtra(User.USERKEY);
        activityChatScreenBinding.textName.setText(receiverUser.getFullname());
    }

    private void uploadImage() {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sending");
        progressDialog.show();

        if (imageUri != null) {
            final StorageReference filereference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));

            uploadTask = filereference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    return filereference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        myUrl = downloadUri.toString();

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");

                        String postid = reference.push().getKey();
                        String key = reference.push().getKey();
                        HashMap<String, Object> message = new HashMap<>();
                        message.put("id", key);
                        message.put("senderId", firebaseUser.getUid());
                        message.put("receiverId", receiverUser.getId());
                        message.put("message", myUrl);
                        message.put("dateObject", new Date());
                        message.put("messageType", "image");
                        reference.child(key).setValue(message);
                        getToken("image", senderUser.getUsername(), senderUser.getFullname(), senderUser.getId(), receiverUser.getId(), senderUser.getImageurl());

                        if (conversationId != null) {
                            updateConversion("image");
                        } else {
                            HashMap<String, Object> conversion = new HashMap<>();
                            conversion.put(ChatMessage.SENDERIDKEY, firebaseUser.getUid());
                            conversion.put(ChatMessage.KEY_SENDER_NAME, senderUser.getFullname());
                            conversion.put(ChatMessage.KEY_SENDER_IMAGE, senderUser.getImageurl());
                            conversion.put(ChatMessage.RECEIVERIDKEY, receiverUser.getId());
                            conversion.put(ChatMessage.KEY_RECEIVER_NAME, receiverUser.getFullname());
                            conversion.put(ChatMessage.KEY_RECEIVER_IMAGE, receiverUser.getImageurl());
                            conversion.put(ChatMessage.KEY_LAST_MESSAGE, "image");
                            conversion.put(ChatMessage.DATETIMEKEY, new Date());
                            DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference(ChatMessage.KEY_COLLECTION_CONVERSATION);
                            conversationId = reference1.push().getKey();
                            reference1.child(conversationId).setValue(conversion);
                            if (chatAdapter.conversionID == null)
                                chatAdapter.conversionID = conversationId;
                            chatAdapter.lastConversion = activityChatScreenBinding.inputMessage.getText().toString();


                        }
                        activityChatScreenBinding.inputMessage.setText(null);

                        progressDialog.dismiss();

                    } else {
                        Toast.makeText(ChatScreenActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ChatScreenActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "No image selected!", Toast.LENGTH_SHORT).show();
        }

    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();

        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }


    private void setListeners() {
        activityChatScreenBinding.imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNotification==1){
                    Intent i= new Intent(getApplicationContext(),ChatActivity.class);
                    finishAffinity();
                    startActivity(i);
                }
                else {
                    onBackPressed();
                }
            }
        });
        activityChatScreenBinding.layoutSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });


        activityChatScreenBinding.camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] listPermission = new String[1];
                listPermission[0] = (Manifest.permission.CAMERA);
                if (ContextCompat.checkSelfPermission(
                        ChatScreenActivity.this,
                        Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                ) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, CAMERA_REQUEST_CODE);
                } else {
                    ActivityCompat.requestPermissions(
                            ChatScreenActivity.this,
                            listPermission,
                            CAMERA_PERMISSION_CODE
                    );
                }


            }
        });
        activityChatScreenBinding.sendImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent iGallery = new Intent(Intent.ACTION_PICK);
                iGallery.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(iGallery, GALLERY_REQ_CODE);

            }
        });

        activityChatScreenBinding.cancelSendImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activityChatScreenBinding.layoutSend.setVisibility(View.VISIBLE);
                activityChatScreenBinding.layoutSendImage.setVisibility(View.GONE);
                activityChatScreenBinding.imageReviewLayout.setVisibility(View.GONE);
            }
        });
        activityChatScreenBinding.layoutSendImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
                activityChatScreenBinding.layoutSend.setVisibility(View.VISIBLE);
                activityChatScreenBinding.layoutSendImage.setVisibility(View.GONE);
                activityChatScreenBinding.imageReviewLayout.setVisibility(View.GONE);
            }
        });
        activityChatScreenBinding.recordButton.setRecordView(activityChatScreenBinding.recordView);
        activityChatScreenBinding.recordButton.setListenForRecord(false);
        activityChatScreenBinding.recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] listPermission = new String[1];
                listPermission[0] = (Manifest.permission.RECORD_AUDIO);
                if (ContextCompat.checkSelfPermission(
                        ChatScreenActivity.this,
                        Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                ) {
                    activityChatScreenBinding.recordButton.setListenForRecord(true);
                } else {
                    ActivityCompat.requestPermissions(
                            ChatScreenActivity.this,
                            listPermission,
                            AUDIO_PERMISSION_CODE
                    );
                }
            }
        });
        activityChatScreenBinding.recordView.setOnRecordListener(new OnRecordListener() {
            @Override
            public void onStart() {
                //Start Recording..
                Log.d("RecordView", "onStart");
                setUpRecording();
                try {
                    mediaRecorder.prepare();
                    mediaRecorder.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                activityChatScreenBinding.inputMessage.setVisibility(View.INVISIBLE);
                activityChatScreenBinding.sendImage.setVisibility(View.INVISIBLE);
                activityChatScreenBinding.camera.setVisibility(View.INVISIBLE);
                activityChatScreenBinding.recordView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancel() {
                //On Swipe To Cancel
                Log.d("RecordView", "onCancel");
                mediaRecorder.reset();
                mediaRecorder.release();
                File file = new File(audioPath);
                if (file.exists())
                    file.delete();

                activityChatScreenBinding.inputMessage.setVisibility(View.VISIBLE);
                activityChatScreenBinding.sendImage.setVisibility(View.VISIBLE);
                activityChatScreenBinding.camera.setVisibility(View.VISIBLE);
                activityChatScreenBinding.recordView.setVisibility(View.GONE);
            }

            @Override
            public void onFinish(long recordTime, boolean limitReached) {
                //Stop Recording..
                //limitReached to determine if the Record was finished when time limit reached.
                Log.d("RecordView", "onFinish");
                try {
                    mediaRecorder.stop();
                    mediaRecorder.release();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                activityChatScreenBinding.inputMessage.setVisibility(View.VISIBLE);
                activityChatScreenBinding.sendImage.setVisibility(View.VISIBLE);
                activityChatScreenBinding.camera.setVisibility(View.VISIBLE);
                activityChatScreenBinding.recordView.setVisibility(View.GONE);

                sendRecordingMessage(audioPath);
            }

            @Override
            public void onLessThanSecond() {
                //When the record time is less than One Second
                Log.d("RecordView", "onLessThanSecond");

                mediaRecorder.reset();
                mediaRecorder.release();

                File file = new File(audioPath);
                if (file.exists())
                    file.delete();

                activityChatScreenBinding.inputMessage.setVisibility(View.VISIBLE);
                activityChatScreenBinding.recordView.setVisibility(View.GONE);

            }

            @Override
            public void onLock() {
                //When Lock gets activated
                Log.d("RecordView", "onLock");
            }

        });
        activityChatScreenBinding.voiceCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (receiverUser==null){
//                  loadReceiverDetails();
//                }
                Intent intent = new Intent(getApplicationContext(), CallActivity.class);
                intent.putExtra("to", receiverUser.getUsername().trim());
                intent.putExtra("isInComingCall", false);
                startActivity(intent);
//               initStringeeConnection();
            }
        });
        activityChatScreenBinding.videoCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), videoCallActivity.class);
                intent.putExtra("to", receiverUser.getUsername().trim());
                intent.putExtra("isInComingCall", false);
                startActivity(intent);
                // initStringeeConnection();

            }
        });
        activityChatScreenBinding.moreInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), ImageSentGalleryActivity.class);
                i.putExtra("interacter", receiverUser.getId());
                i.putExtra("interacterImage", receiverUser.getImageurl());
                i.putExtra("interacterFullName", receiverUser.getFullname());
                startActivity(i);
            }
        });

    }

//    public void initStringeeConnection() {
//        client=new StringeeClient(this);
//        client.setConnectionListener(new StringeeConnectionListener() {
//            @Override
//            public void onConnectionConnected(StringeeClient stringeeClient, boolean b) {
//                runOnUiThread(() -> {
//                    FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
//                        @Override
//                        public void onComplete(@NonNull Task<String> task) {
//                            if (task.isSuccessful()){
//                                String deviceToken= task.getResult();
//                                client.registerPushToken(deviceToken, new StatusListener() {
//                                    @Override
//                                    public void onSuccess() {
//                                        android.util.Log.d("SampleCall","register push success");
//                                    }
//                                });
//                            }
//                        }
//                    });
//                });
//
//            }
//
//            @Override
//            public void onConnectionDisconnected(StringeeClient stringeeClient, boolean b) {
//                runOnUiThread(()->{
//                    Toast.makeText(ChatScreenActivity.this, "The connection is disconnected", Toast.LENGTH_SHORT).show();
//                });
//            }
//
//            @Override
//            public void onIncomingCall(StringeeCall stringeeCall) {
//                runOnUiThread(() -> {
//                    callMap.put(stringeeCall.getCallId(),stringeeCall);
//                    Intent intent=new Intent(ChatScreenActivity.this, CallActivity.class);
//                    intent.putExtra("callId",stringeeCall.getCallId());
//                    intent.putExtra("isInComingCall",true);
//                    startActivity(intent);
//                });
//            }
//
//            @Override
//            public void onIncomingCall2(StringeeCall2 stringeeCall2) {
//                runOnUiThread(() -> {
//                    videocallMap.put(stringeeCall2.getCallId(),stringeeCall2);
//                    Intent intent=new Intent(ChatScreenActivity.this, videoCallActivity.class);
//                    intent.putExtra("callId",stringeeCall2.getCallId());
//                    intent.putExtra("isInComingCall",true);
//                    startActivity(intent);
//                });
//            }
//
//            @Override
//            public void onConnectionError(StringeeClient stringeeClient, StringeeError stringeeError) {
//                runOnUiThread(()->{
//                    Toast.makeText(ChatScreenActivity.this, stringeeError.getMessage(), Toast.LENGTH_SHORT).show();
//                });
//            }
//
//            @Override
//            public void onRequestNewToken(StringeeClient stringeeClient) {
//
//            }
//
//            @Override
//            public void onCustomMessage(String s, JSONObject jsonObject) {
//
//            }
//
//            @Override
//            public void onTopicMessage(String s, JSONObject jsonObject) {
//
//            }
//        });
//        client.connect(token);
//    }


    private void setUpRecording() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
//        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "friendverse/Media/Recording");
//            boolean a;
//        if (!file.exists())
//            a= file.mkdirs();
        audioPath = folder.getAbsolutePath() + File.separator + System.currentTimeMillis() + ".3gp";

        mediaRecorder.setOutputFile(audioPath);
    }

    private void sendRecordingMessage(String audioPath) {
        final StorageReference storageReference = FirebaseStorage.getInstance().getReference("chatRecords");

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sending");
        progressDialog.show();

        if (audioPath != null) {
            final StorageReference filereference = storageReference.child(System.currentTimeMillis() + "");
            Uri audioFile = Uri.fromFile(new File(audioPath));
            uploadTask = filereference.putFile(audioFile);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    return filereference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        myUrl = downloadUri.toString();

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");

                        String postid = reference.push().getKey();
                        String key = reference.push().getKey();

                        HashMap<String, Object> message = new HashMap<>();
                        message.put("id", key);
                        message.put("senderId", firebaseUser.getUid());
                        message.put("receiverId", receiverUser.getId());
                        message.put("message", myUrl);
                        message.put("dateObject", new Date());
                        message.put("messageType", "audio");
                        reference.child(key).setValue(message);
                        getToken("audio", senderUser.getUsername(), senderUser.getFullname(), senderUser.getId(), receiverUser.getId(), senderUser.getImageurl());

                        if (conversationId != null) {
                            updateConversion("audio");
                        } else {
                            HashMap<String, Object> conversion = new HashMap<>();
                            conversion.put(ChatMessage.SENDERIDKEY, firebaseUser.getUid());
                            conversion.put(ChatMessage.KEY_SENDER_NAME, senderUser.getFullname());
                            conversion.put(ChatMessage.KEY_SENDER_IMAGE, senderUser.getImageurl());
                            conversion.put(ChatMessage.RECEIVERIDKEY, receiverUser.getId());
                            conversion.put(ChatMessage.KEY_RECEIVER_NAME, receiverUser.getFullname());
                            conversion.put(ChatMessage.KEY_RECEIVER_IMAGE, receiverUser.getImageurl());
                            conversion.put(ChatMessage.KEY_LAST_MESSAGE, "audio");
                            conversion.put(ChatMessage.DATETIMEKEY, new Date());
                            DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference(ChatMessage.KEY_COLLECTION_CONVERSATION);
                            conversationId = reference1.push().getKey();
                            if (chatAdapter.conversionID == null)
                                chatAdapter.conversionID = conversationId;
                            chatAdapter.lastConversion = activityChatScreenBinding.inputMessage.getText().toString();

                            reference1.child(conversationId).setValue(conversion);

                        }
                        activityChatScreenBinding.inputMessage.setText(null);

                        progressDialog.dismiss();

                    } else {
                        Toast.makeText(ChatScreenActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ChatScreenActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "No audio selected!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, CAMERA_REQUEST_CODE);
            } else {
                Toast.makeText(this, "The camera is not permission", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == GALLERY_REQ_CODE) {
                imageUri = data.getData();
                activityChatScreenBinding.imageReview.setImageURI(imageUri);
                activityChatScreenBinding.imageReviewLayout.setVisibility(View.VISIBLE);

                activityChatScreenBinding.layoutSendImage.setVisibility(View.VISIBLE);
                activityChatScreenBinding.layoutSend.setVisibility(View.GONE);
            }
            if (requestCode == CAMERA_REQUEST_CODE) {
                Bitmap image = (Bitmap) data.getExtras().get("data");

                WeakReference<Bitmap> result1 = new WeakReference<>(Bitmap.createScaledBitmap(
                        image,
                        image.getHeight(),
                        image.getWidth(),
                        false
                ).copy(Bitmap.Config.RGB_565, true));

                Bitmap bn = result1.get();
                imageUri = saveImage(bn, ChatScreenActivity.this);
                activityChatScreenBinding.imageReview.setImageURI(imageUri);
                activityChatScreenBinding.imageReviewLayout.setVisibility(View.VISIBLE);

                activityChatScreenBinding.layoutSendImage.setVisibility(View.VISIBLE);
                activityChatScreenBinding.layoutSend.setVisibility(View.GONE);
            }

        }
    }


    //Convert bitMap to URI
    private Uri saveImage(Bitmap image, Context context) {
        File imagesFolder = new File(context.getCacheDir(), "images");
        Uri uri = null;
        try {
            imagesFolder.mkdir();
            File file = new File(imagesFolder, "capture_image.jpg");
            FileOutputStream stream = new FileOutputStream(file);
            image.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            stream.flush();
            stream.close();
            uri = FileProvider.getUriForFile(context.getApplicationContext(), "com.example.friendverse" + ".provider", file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return uri;
    }

    private String getDateTime(Date date) {
        return new SimpleDateFormat("MMMM dd,yyyy - hh:mm a", Locale.getDefault()).format(date);
    }

}