package com.example.friendverse.ChatApp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.friendverse.R;
import com.stringee.call.StringeeCall2;
import com.stringee.common.StringeeAudioManager;
import com.stringee.listener.StatusListener;
import com.stringee.video.StringeeVideoTrack;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class videoCallActivity extends AppCompatActivity {

    private ImageButton rejectCall;
    private ImageButton acceptCall;
    private ImageButton cancelCall;
    private ImageButton speakerCall;
    private ImageButton voiceMuteCall;

    private ImageButton switchCamera;
    private ImageButton onCamerabtn;
    private View incoming;
    private TextView tvStatus;
    private StringeeCall2 call;

    private boolean isIncomingCall = false;
    private String to;
    private String callId;
    private StringeeCall2.SignalingState mSignalingState;
    private StringeeCall2.MediaState mMediaState;

    private StringeeAudioManager audioManager;

    private boolean isSpeaker = false;
    private boolean MicroOn = true;
    private boolean cameraOn = true;

    private FrameLayout smallView;
    private FrameLayout fullSizeView;

    private boolean isSwapView=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call);

        tvStatus = findViewById(R.id.textViewStatus);
        rejectCall = findViewById(R.id.btn_rejectCall);
        acceptCall = findViewById(R.id.btn_acceptCall);
        cancelCall = findViewById(R.id.btn_cancelCall);
        speakerCall = findViewById(R.id.btn_speaker);
        voiceMuteCall = findViewById(R.id.btn_voiceOff);




        onCamerabtn=findViewById(R.id.btn_Camera);
        switchCamera=findViewById(R.id.btn_switchCamera);
        smallView=findViewById(R.id.smallView);
        fullSizeView=findViewById(R.id.localView);

        onCamerabtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread(() -> {
                    if (call != null) {
                        call.enableVideo(!cameraOn);
                        cameraOn = !cameraOn;
                        onCamerabtn.setBackground(cameraOn?getResources().getDrawable(R.drawable.round_btn_on):getResources().getDrawable(R.drawable.round_btn));
                    }
                });
            }
        });
        switchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (call!=null){
                    call.switchCamera(new StatusListener() {
                        @Override
                        public void onSuccess() {

                        }
                    });
                }
            }
        });


        rejectCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread( () -> {
                    if (call!=null){
                        call.reject(new StatusListener() {
                            @Override
                            public void onSuccess() {

                            }
                        });
                        audioManager.stop();
                        finish();
                    }

                });
            }
        });

        acceptCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread( () -> {
                    if (call!=null){
                        call.answer(new StatusListener() {
                            @Override
                            public void onSuccess() {

                            }
                        });
                        acceptCall.setVisibility(View.GONE);
                        rejectCall.setVisibility(View.GONE);
                        cancelCall.setVisibility(View.VISIBLE);
                        speakerCall.setVisibility(View.VISIBLE);
                        voiceMuteCall.setVisibility(View.VISIBLE);

                        switchCamera.setVisibility(View.VISIBLE);
                        onCamerabtn.setVisibility(View.VISIBLE);
                        smallView.setVisibility(View.VISIBLE);
                        fullSizeView.setVisibility(View.VISIBLE);
                    }

                });

            }
        });
        cancelCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (call != null) {
                    call.hangup(new StatusListener() {
                        @Override
                        public void onSuccess() {

                        }
                    });
                    audioManager.stop();
                    finish();
                }
            }
        });
        speakerCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread(() -> {
                    if (audioManager != null) {
                        audioManager.setSpeakerphoneOn(!isSpeaker);
                        isSpeaker = !isSpeaker;
//                        speakerCall.setBackgroundColor(isSpeaker?R.color.gray:getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral30));
                        speakerCall.setBackground(isSpeaker?getResources().getDrawable(R.drawable.round_btn_on):getResources().getDrawable(R.drawable.round_btn));
                    }
                });
            }
        });
        voiceMuteCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread(() -> {
                    if (call != null) {
                        call.mute(!MicroOn);
                        MicroOn = !MicroOn;
//                        voiceMuteCall.setBackgroundColor(MicroOn?getResources().getColor(R.color.gray):getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral30));
                        voiceMuteCall.setBackground(MicroOn?getResources().getDrawable(R.drawable.round_btn_on):getResources().getDrawable(R.drawable.round_btn));

                    }
                });
            }
        });

        if (getIntent() != null) {
            isIncomingCall = getIntent().getBooleanExtra("isInComingCall", false);
            to = getIntent().getStringExtra("to");
            callId=getIntent().getStringExtra("callId");
        }
        cancelCall.setVisibility(!isIncomingCall?View.VISIBLE:View.GONE);
        speakerCall.setVisibility(!isIncomingCall?View.VISIBLE:View.GONE);
        voiceMuteCall.setVisibility(!isIncomingCall?View.VISIBLE:View.GONE);
        switchCamera.setVisibility(!isIncomingCall?View.VISIBLE:View.GONE);
        onCamerabtn.setVisibility(!isIncomingCall?View.VISIBLE:View.GONE);
        smallView.setVisibility(!isIncomingCall?View.VISIBLE:View.GONE);
        fullSizeView.setVisibility(!isIncomingCall?View.VISIBLE:View.GONE);


        acceptCall.setVisibility(isIncomingCall?View.VISIBLE:View.GONE);
        rejectCall.setVisibility(isIncomingCall?View.VISIBLE:View.GONE);

        List<String> listPermission=new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED){
            listPermission.add(android.Manifest.permission.RECORD_AUDIO);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            listPermission.add(Manifest.permission.CAMERA);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT)!=PackageManager.PERMISSION_GRANTED){
                listPermission.add(Manifest.permission.BLUETOOTH_CONNECT);
            }
        }
        if (listPermission.size()>0){
            String [] permissions= new String[listPermission.size()];
            for (int i=0; i< listPermission.size();i++){
                permissions[i]= listPermission.get(i);
            }
            ActivityCompat.requestPermissions(this, permissions,0 );
            return;
        }

        initCall();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean isGranted = false;
        if (grantResults.length > 0) {
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    isGranted = false;
                    break;
                } else {
                    isGranted = true;
                }
            }
        }
        if (requestCode == 0) {
            if (!isGranted) {
                finish();
            } else {
                initCall();
            }
        }
    }

    private void initCall() {
        if (isIncomingCall) {
            call= ChatActivity.videocallMap.get(callId)  ;
            if (call==null){
                finish();
                return;
            }
        } else {
            call = new StringeeCall2(ChatActivity.client, ChatActivity.client.getUserId(), to);
            call.setVideoCall(true);
        }
        call.setCallListener(new StringeeCall2.StringeeCallListener() {
            @Override
            public void onSignalingStateChange(StringeeCall2 stringeeCall2, StringeeCall2.SignalingState signalingState, String s, int i, String s1) {
                runOnUiThread(() -> {
                    mSignalingState = signalingState;
                    switch (signalingState) {
                        case CALLING:
                            tvStatus.setText("Calling");
                            break;
                        case RINGING:
                            tvStatus.setText("Ringing");
                            break;
                        case BUSY:
                            tvStatus.setText("Busy");
                            audioManager.stop();
                            finish();
                            break;
                        case ANSWERED:
                            tvStatus.setText("Starting");
                            if (mMediaState == StringeeCall2.MediaState.CONNECTED) {
                                tvStatus.setText("Started");
                            }

                            break;
                        case ENDED:
                            tvStatus.setText("Ended");
                            audioManager.stop();
                            finish();
                            break;
                    }
                });

            }

            @Override
            public void onError(StringeeCall2 stringeeCall2, int i, String s) {
                runOnUiThread(() -> {
                    finish();
                });
            }

            @Override
            public void onHandledOnAnotherDevice(StringeeCall2 stringeeCall2, StringeeCall2.SignalingState signalingState, String s) {

            }

            @Override
            public void onMediaStateChange(StringeeCall2 stringeeCall2, StringeeCall2.MediaState mediaState) {
                runOnUiThread(() -> {
                    mMediaState = mediaState;
                    if (mediaState == StringeeCall2.MediaState.CONNECTED) {
                        if (mSignalingState == StringeeCall2.SignalingState.ANSWERED) {
                            tvStatus.setText("Started");
                        }
                    } else {
                        tvStatus.setText("Retry to connect");
                    }
                });
            }

            @Override
            public void onLocalStream(StringeeCall2 stringeeCall2) {
                runOnUiThread( () -> {
                    smallView.removeAllViews();
                    smallView.addView(stringeeCall2.getLocalView());
                    stringeeCall2.renderLocalView(true);
                });
            }

            @Override
            public void onRemoteStream(StringeeCall2 stringeeCall2) {
                runOnUiThread( () -> {
                    fullSizeView.removeAllViews();
                    fullSizeView.addView(stringeeCall2.getRemoteView());
                    stringeeCall2.renderRemoteView(false);
                });
            }

            @Override
            public void onVideoTrackAdded(StringeeVideoTrack stringeeVideoTrack) {

            }

            @Override
            public void onVideoTrackRemoved(StringeeVideoTrack stringeeVideoTrack) {

            }

            @Override
            public void onCallInfo(StringeeCall2 stringeeCall2, JSONObject jsonObject) {

            }

            @Override
            public void onTrackMediaStateChange(String s, StringeeVideoTrack.MediaType mediaType, boolean b) {

            }
        });

        audioManager = new StringeeAudioManager(this);
        audioManager.start((audioDevice, set) -> {

        });
        audioManager.setSpeakerphoneOn(true);

        if (isIncomingCall) {
            call.ringing(new StatusListener() {
                @Override
                public void onSuccess() {

                }
            });
        } else {
            call.makeCall(new StatusListener() {
                @Override
                public void onSuccess() {

                }
            });
        }
    }
}