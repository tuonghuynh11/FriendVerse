package com.example.friendverse.Adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.friendverse.ChatApp.PreviewImageActivity;
import com.example.friendverse.R;

import java.util.List;

public class ImageSentAdapter extends RecyclerView.Adapter<ImageSentAdapter.ImageSentViewHolder> {
    private List<String> images;
    private Activity activity;

    public ImageSentAdapter(Activity activity, List<String> images) {
        this.images = images;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ImageSentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_card_item, parent, false);
        return new ImageSentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageSentViewHolder holder,  int position) {
        Glide.with(activity).load(images.get(position)).into(holder.imageView);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(activity, PreviewImageActivity.class);
                i.putExtra("image", images.get(position));
                activity.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public class ImageSentViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;

        public ImageSentViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.idIVImage);
        }
    }

}
