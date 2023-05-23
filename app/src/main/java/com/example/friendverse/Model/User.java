package com.example.friendverse.Model;

import java.io.Serializable;

public class User implements Serializable {
    public static final String USERKEY="Users";
    public static final String IDKEY="id";
    public static final String USERNAMEKEY="username";
    public static final String FULLNAMEKEY="fullname";
    public static final String IMAGEKEY="imageurl";
    public static final String BIOKEY="bio";
    public static final String EMAILOKEY="email";
    public static final String TOKENCALLKEY="tokenCall";
    public static final String TOKENKEY="token";
    public static final String ACTIVITYKEY="activity";
    private String id;
    private String username;
    private String email;
    private String fullname;
    private String imageurl;
    private String bio;

    private String tokenCall;
    private String token;


    private int activity;
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public User() {
    }

    public User(String id, String username, String fullname, String imageurl, String bio,String email, int activity, String tokenCall,String token) {
        this.id = id;
        this.username = username;
        this.fullname = fullname;
        this.imageurl = imageurl;
        this.bio = bio;
        this.email=email;
        this.activity=activity;
        this.tokenCall=tokenCall;
        this.token=token;
    }

    public String getTokenCall() {
        return tokenCall;
    }

    public void setTokenCall(String tokenCall) {
        this.tokenCall = tokenCall;
    }

    public int getActivity() {
        return activity;
    }

    public void setAvailability(int activity) {
        this.activity = activity;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}
