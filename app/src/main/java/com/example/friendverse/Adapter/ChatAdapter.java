package com.example.friendverse.Adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.friendverse.Model.ChatMessage;
import com.example.friendverse.ChatApp.PreviewImageActivity;
import com.example.friendverse.Model.User;
import com.example.friendverse.R;
import com.example.friendverse.databinding.ItemContainerReceivedAudioBinding;
import com.example.friendverse.databinding.ItemContainerReceivedImageBinding;
import com.example.friendverse.databinding.ItemContainerReceivedMessageBinding;
import com.example.friendverse.databinding.ItemContainerSendAudioBinding;
import com.example.friendverse.databinding.ItemContainerSendImageBinding;
import com.example.friendverse.databinding.ItemContainerSendMessageBinding;
import com.example.friendverse.databinding.ItemContainerUnsedSenderBinding;
import com.example.friendverse.databinding.ItemContainerUnsendReceiverBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private  String receiverProfileImageLink;
    private final List<ChatMessage> chatMessages;
    private final String senderId;
    public String conversionID;
    public String lastConversion;

    private List<User> allUsers;

    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;
    public static final int VIEW_TYPE_RECEIVED_IMAGE = 3;
    public static final int VIEW_TYPE_SENT_IMAGE = 4;

    public static final int VIEW_TYPE_RECEIVED_AUDIO = 5;
    public static final int VIEW_TYPE_SENT_AUDIO = 6;

    public static final int VIEW_TYPE_UNSENT_RECEIVED = 7;
    public static final int VIEW_TYPE_UNSENT_SENDER = 8;

    private Activity activity;

    public ChatAdapter(String receiverProfileImageLink, List<ChatMessage> chatMessages, String senderId, Activity activity) {
        this.receiverProfileImageLink = receiverProfileImageLink;
        this.chatMessages = chatMessages;
        this.senderId = senderId;
        this.activity=activity;
        this.allUsers= new ArrayList<>();
        addAllUser();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            return new SentMessageViewHolder(
                    ItemContainerSendMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        } else if (viewType == VIEW_TYPE_RECEIVED) {
            return new ReceivedMessageViewHolder(ItemContainerReceivedMessageBinding.inflate(
                    LayoutInflater.from(parent.getContext()),
                    parent,
                    false
            ));

        } else if (viewType == VIEW_TYPE_SENT_AUDIO) {
            return new SentAudioViewHolder(ItemContainerSendAudioBinding.inflate(
                    LayoutInflater.from(parent.getContext()),
                    parent,
                    false
            ));
        } else if (viewType == VIEW_TYPE_RECEIVED_AUDIO) {
            return new ReceivedAudioViewHolder(ItemContainerReceivedAudioBinding.inflate(
                    LayoutInflater.from(parent.getContext()),
                    parent,
                    false
            ));

        } else if (viewType == VIEW_TYPE_UNSENT_SENDER) {
            return new UnSentSenderMessageViewHolder(ItemContainerUnsedSenderBinding.inflate(
                    LayoutInflater.from(parent.getContext()),
                    parent,
                    false
            ));

        } else if (viewType == VIEW_TYPE_UNSENT_RECEIVED) {
            return new UnSentReceiverMessageViewHolder(ItemContainerUnsendReceiverBinding.inflate(
                    LayoutInflater.from(parent.getContext()),
                    parent,
                    false
            ));

        } else if (viewType == VIEW_TYPE_SENT_IMAGE) {
            return new SentImageViewHolder(ItemContainerSendImageBinding.inflate(
                    LayoutInflater.from(parent.getContext()),
                    parent,
                    false
            ));

        } else {//(viewType==VIEW_TYPE_RECEIVED_IMAGE)
            return new ReceivedImageViewHolder(ItemContainerReceivedImageBinding.inflate(
                    LayoutInflater.from(parent.getContext()),
                    parent,
                    false
            ));
        }

    }

    //Add allUser
    void addAllUser(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(User.USERKEY);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1:snapshot.getChildren()){
                    User user= snapshot1.getValue(User.class);
                    allUsers.add(user);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    //find UserImageForGroup
    void updateReceiverProfileImageLink(String id){
        for (User user:allUsers){
            if (user.getId().equals(id)){
                receiverProfileImageLink= user.getImageurl();
                return;
            }
        }
    }
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        updateReceiverProfileImageLink(chatMessages.get(position).getSenderId());
        if (getItemViewType(position) == VIEW_TYPE_SENT) {
            ((SentMessageViewHolder) holder).setData(chatMessages.get(position));
            if (chatMessages.get(position).getSenderId().equals(senderId)) {
                ((SentMessageViewHolder) holder).binding.getRoot().setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        deleteOption(((SentMessageViewHolder) holder).binding.getRoot().getContext(), chatMessages.get(position));
                        return false;
                    }
                });
            }
        } else if (getItemViewType(position) == VIEW_TYPE_RECEIVED) {
            ((ReceivedMessageViewHolder) holder).setData(chatMessages.get(position), receiverProfileImageLink);
        } else if (getItemViewType(position) == VIEW_TYPE_UNSENT_SENDER) {
            ((UnSentSenderMessageViewHolder) holder).setData(chatMessages.get(position));
        } else if (getItemViewType(position) == VIEW_TYPE_UNSENT_RECEIVED) {
            ((UnSentReceiverMessageViewHolder) holder).setData(chatMessages.get(position), receiverProfileImageLink);
        } else if (getItemViewType(position) == VIEW_TYPE_SENT_IMAGE) {
            ((SentImageViewHolder) holder).setData(chatMessages.get(position));
            ((SentImageViewHolder) holder).binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(activity, PreviewImageActivity.class);
                    i.putExtra("image",chatMessages.get(position).getMessage());
                    activity.startActivity(i);
                }
            });
            if (chatMessages.get(position).getSenderId().equals(senderId)) {
                ((SentImageViewHolder) holder).binding.getRoot().setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        deleteOption(((SentImageViewHolder) holder).binding.getRoot().getContext(), chatMessages.get(position));
                        return false;
                    }
                });
            }
        } else if (getItemViewType(position) == VIEW_TYPE_RECEIVED_IMAGE) {
            ((ReceivedImageViewHolder) holder).setData(chatMessages.get(position), receiverProfileImageLink);
            ((ReceivedImageViewHolder) holder).binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(activity, PreviewImageActivity.class);
                    i.putExtra("image",chatMessages.get(position).getMessage());
                    activity.startActivity(i);
                }
            });
        } else if (getItemViewType(position) == VIEW_TYPE_RECEIVED_AUDIO) {
            ((ReceivedAudioViewHolder) holder).setData(chatMessages.get(position), receiverProfileImageLink);
        } else if (getItemViewType(position) == VIEW_TYPE_SENT_AUDIO) {
            ((SentAudioViewHolder) holder).setData(chatMessages.get(position));
            if (chatMessages.get(position).getSenderId().equals(senderId)) {
                ((SentAudioViewHolder) holder).binding.getRoot().setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        deleteOption(((SentAudioViewHolder) holder).binding.getRoot().getContext(), chatMessages.get(position));
                        return false;
                    }
                });
            }
        }


    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (chatMessages.get(position).getSenderId().equals(senderId)) {
            if (chatMessages.get(position).getMessageType().equals("image")) {
                return VIEW_TYPE_SENT_IMAGE;
            } else if (chatMessages.get(position).getMessageType().equals("audio")) {
                return VIEW_TYPE_SENT_AUDIO;
            } else if (chatMessages.get(position).getMessageType().equals("unsent")) {
                return VIEW_TYPE_UNSENT_SENDER;
            } else
                return VIEW_TYPE_SENT;
        } else {
            if (chatMessages.get(position).getMessageType().equals("image"))
                return VIEW_TYPE_RECEIVED_IMAGE;
            else if (chatMessages.get(position).getMessageType().equals("audio")) {
                return VIEW_TYPE_RECEIVED_AUDIO;
            } else if (chatMessages.get(position).getMessageType().equals("unsent")) {
                return VIEW_TYPE_UNSENT_RECEIVED;
            } else
                return VIEW_TYPE_RECEIVED;
        }

    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerSendMessageBinding binding;

        public SentMessageViewHolder(ItemContainerSendMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void setData(ChatMessage chatMessage) {
            binding.textMessage.setText(chatMessage.getMessage());
            binding.textDateTime.setText(chatMessage.getDateTime());
        }
    }

    static class UnSentSenderMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerUnsedSenderBinding binding;

        public UnSentSenderMessageViewHolder(ItemContainerUnsedSenderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void setData(ChatMessage chatMessage) {
            binding.textDateTime.setText(chatMessage.getDateTime());
        }
    }

    static class UnSentReceiverMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerUnsendReceiverBinding binding;

        public UnSentReceiverMessageViewHolder(ItemContainerUnsendReceiverBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void setData(ChatMessage chatMessage, String receiverProfileImageLink) {
            binding.textDateTime.setText(chatMessage.getDateTime());
            Picasso.get().load(receiverProfileImageLink).placeholder(R.drawable.default_avatar).into(binding.imageProfile);
        }
    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerReceivedMessageBinding binding;

        public ReceivedMessageViewHolder(ItemContainerReceivedMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void setData(ChatMessage chatMessage, String receiverProfileImageLink) {
            binding.textMessage.setText(chatMessage.getMessage());
            binding.textDateTime.setText(chatMessage.getDateTime());
            Picasso.get().load(receiverProfileImageLink).placeholder(R.drawable.default_avatar).into(binding.imageProfile);
        }
    }


    static class SentImageViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerSendImageBinding binding;

        public SentImageViewHolder(ItemContainerSendImageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void setData(ChatMessage chatMessage) {
            Picasso.get().load(chatMessage.getMessage()).placeholder(R.drawable.default_avatar).into(binding.image);
            binding.textDateTime.setText(chatMessage.getDateTime());
        }
    }

    static class ReceivedImageViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerReceivedImageBinding binding;

        public ReceivedImageViewHolder(ItemContainerReceivedImageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void setData(ChatMessage chatMessage, String receiverProfileImageLink) {
            Picasso.get().load(chatMessage.getMessage()).placeholder(R.drawable.default_avatar).into(binding.image);
            binding.textDateTime.setText(chatMessage.getDateTime());
            Picasso.get().load(receiverProfileImageLink).placeholder(R.drawable.default_avatar).into(binding.imageProfile);
        }
    }

    static class SentAudioViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerSendAudioBinding binding;

        public SentAudioViewHolder(ItemContainerSendAudioBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void setData(ChatMessage chatMessage) {
            binding.voicePlayerView.setAudio(chatMessage.getMessage());
            binding.textDateTime.setText(chatMessage.getDateTime());
        }
    }

    static class ReceivedAudioViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerReceivedAudioBinding binding;

        public ReceivedAudioViewHolder(ItemContainerReceivedAudioBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void setData(ChatMessage chatMessage, String receiverProfileImageLink) {
            binding.voicePlayerView.setAudio(chatMessage.getMessage());
            binding.textDateTime.setText(chatMessage.getDateTime());
            Picasso.get().load(receiverProfileImageLink).placeholder(R.drawable.default_avatar).into(binding.imageProfile);
        }
    }

    private void deleteOption(Context context, ChatMessage chatMessage) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats").child(chatMessage.getId());
//        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference().child("Chats").child(chatMessage.getId());

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        Map<String, Object> map = new HashMap<>();
                        map.put(ChatMessage.MESSAGEKEY, "");
                        map.put(ChatMessage.KEY_MESSAGE_TYPE, "unsent");
                        reference.updateChildren(map);
                        updateLastMessage(chatMessage.getMessage());
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Do you want to delete message ?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    public void updateLastMessage(String message) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(ChatMessage.KEY_COLLECTION_CONVERSATION).child(conversionID);
        if (message.equals(lastConversion))
            reference.child(ChatMessage.KEY_LAST_MESSAGE).setValue("Message is unsent");

    }
}
