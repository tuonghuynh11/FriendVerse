package com.example.friendverse.Service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.content.res.ResourcesCompat;

import com.example.friendverse.ChatApp.ChatActivity;
import com.example.friendverse.ChatApp.ChatScreenActivity;
import com.example.friendverse.ChatApp.GroupChatScreenActivity;
import com.example.friendverse.Model.User;
import com.example.friendverse.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private User user = new User();

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        android.util.Log.d("CallSample", "onMessageReceived: " + message.getData().toString());
        if (message.getData().size() > 0) {
            String pushFromStringee = message.getData().get("stringeePushNotification");
            if (pushFromStringee != null) {
                String data = message.getData().get("data");
                if (data != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(data);
                        JSONObject fromObject = jsonObject.optJSONObject("from");
                        String from = fromObject.optString("alias", "");
                        String UserName = fromObject.optString("number", "");

                        if (from.length() == 0) {
                            from = fromObject.optString("from");
                        }
                        String callStatus = jsonObject.optString("callStatus", "");
                        if (callStatus.length() > 0) {
                            if (callStatus.equals("started")) {
                                showNotification(from, UserName);
                            }
                            if (callStatus.equals("ended") || callStatus.equals("answered")) {
                                cancelNotification(this);
                            }
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            //Message Notification
            Map<String, String> map = message.getData();
            String title = map.get("title");
            String fullName = map.get("fullname");
            String username = map.get("username");
            String messages = map.get("message");
            String hisID = map.get("hisID");
            String hisImage = map.get("hisImage");
            String chatID = map.get("chatID");
            String type = map.get("type");
            String senderId = map.get("senderId");
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
                createOreoNotification(title, username, fullName, messages, hisID, hisImage, chatID, type,senderId);
            else
                createNormalNotification(title, username, fullName, messages, hisID, hisImage, chatID, type,senderId);

        }
    }

    public static void cancelNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(0123456);
    }

    private void showNotification(String from, String userName) {
        //channel
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel("Channel id", "Channel name", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription("Channel Description");
            notificationManager.createNotificationChannel(notificationChannel);
        }
        //DatabaseReference reference=FirebaseDatabase.getInstance().getReference().child(User.USERKEY).child(from).child(User.FULLNAMEKEY);
        Intent intent = new Intent(this, ChatActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "Channel id");
        builder.setContentTitle("Coming call from " + from);
        builder.setContentText("FriendVerse");
        builder.setSmallIcon(R.mipmap.ic_launcher);///icon thong bao
        builder.setOngoing(true);//Nguoi dung khong the tu tat notification
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        builder.setCategory(NotificationCompat.CATEGORY_CALL);
        builder.setContentIntent(pendingIntent);
        builder.setShowWhen(false);
        notificationManager.notify(0123456, builder.build());


    }

    //push Message Notification

    @Override
    public void onNewToken(@NonNull String token) {
        updateToken(token);
        super.onNewToken(token);
    }

    private void updateToken(String token) {
        try {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference(User.USERKEY).child(user.getId());
            Map<String, Object> map = new HashMap<>();
            map.put("token", token);
            reference.updateChildren(map);
        } catch (Exception e) {
            System.out.println("User Not Found");
        }

    }

    private void createNormalNotification(String title, String username, String fullName, String message, String hisID, String hisImage, String chatID, String type,String senderId) {

        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "1000");
        builder.setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setAutoCancel(true)
                .setColor(ResourcesCompat.getColor(getResources(), R.color.primary, null))
                .setSound(uri);

        Intent intent;
        if (type.equals("normal")) {
            intent = new Intent(this, ChatScreenActivity.class);
            intent.putExtra("notification","1");
        } else {
            intent = new Intent(this, GroupChatScreenActivity.class);
            intent.putExtra("notification","1");
        }

        User user1 = new User();
        user1.setUsername(username);
        user1.setId(hisID);
        user1.setFullname(fullName);
        user1.setImageurl(hisImage);
        intent.putExtra(User.USERKEY, user1);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        builder.setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        try {
            FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
            if (type.equals("normal")){
                if (!hisID.equals(firebaseUser.getUid()))
                    manager.notify(new Random().nextInt(85 - 65), builder.build());
            }
            else{
                if (!senderId.equals(firebaseUser.getUid()))
                    manager.notify(new Random().nextInt(85 - 65), builder.build());
            }

        } catch (Exception e) {
            Log.i("TAG", "user is empty");
        }


    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createOreoNotification(String title, String username, String fullName, String message, String hisID, String hisImage, String chatID, String type,String senderId) {

        NotificationChannel channel = new NotificationChannel("1000", "Message", NotificationManager.IMPORTANCE_HIGH);
        channel.setShowBadge(true);
        channel.enableLights(true);
        channel.enableVibration(true);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(channel);

        Intent intent;
        if (type.equals("normal")) {
            intent = new Intent(this, ChatScreenActivity.class);
            intent.putExtra("notification","1");
        } else {
            intent = new Intent(this, GroupChatScreenActivity.class);
            intent.putExtra("notification","1");
        }
        User user1 = new User();
        user1.setUsername(username);
        user1.setId(hisID);
        user1.setFullname(fullName);
        user1.setImageurl(hisImage);
        intent.putExtra(User.USERKEY, user1);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        Notification notification = new Notification.Builder(this, "1000")
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();
        try {
            FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
            if (type.equals("normal")){
                if (!hisID.equals(firebaseUser.getUid()))
                    manager.notify(100, notification);
            }
            else{
                if (!senderId.equals(firebaseUser.getUid()))
                    manager.notify(100, notification);
            }


        } catch (Exception e) {
            Log.i("TAG", "user is empty");
        }
    }
}
