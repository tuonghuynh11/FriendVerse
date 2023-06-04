package com.example.friendverse.listeners;

import com.example.friendverse.Model.Post;
import com.example.friendverse.Model.User;

public interface ConversionListener {
    void onConversionClicked(User user);
    void onConversionClicked(Post post);
}
