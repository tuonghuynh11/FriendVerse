package com.example.friendverse.Model;
import androidx.annotation.NonNull;

import com.hendraanggrian.appcompat.socialview.Hashtagable;
public class Hashtag implements Hashtagable {

    private String tag;
    private String postid;
    private String text;
    public Hashtag() {
    }
    public Hashtag(String text) {
        this.text = text;
    }


    public String getText() {
        return text;
    }

    public Hashtag(String tag, String postid) {
        this.tag = tag;
        this.postid = postid;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getPostid() {
        return postid;
    }

    public void setPostid(String postid) {
        this.postid = postid;
    }

    @NonNull
    @Override
    public CharSequence getId() {
        return null;
    }

    @Override
    public int getCount() {
        return 0;
    }
}