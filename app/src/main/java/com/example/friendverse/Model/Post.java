package com.example.friendverse.Model;

import java.util.Date;
import java.util.List;

public class Post {
    private String description;
    private String postid;
    private String postimage;
    private String publisher;

    public Post() {
    }

    public Post(String description, String postid, String postimage, String publisher) {
        this.description = description;
        this.postid = postid;
        this.postimage = postimage;
        this.publisher = publisher;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPostid() {
        return postid;
    }

    public void setPostid(String postid) {
        this.postid = postid;
    }

    public String getPostimage() {
        return postimage;
    }

    public void setPostimage(String postimage) {
        this.postimage = postimage;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }
}
