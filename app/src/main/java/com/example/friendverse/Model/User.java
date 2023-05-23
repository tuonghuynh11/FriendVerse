package com.example.friendverse.Model;

import java.util.List;

public class User {
    private Long activity;
    private String bio;
    private String email;
    private String fullname;
    private String id;
    private String imageurl;
    private String phonenumber;
    private String username;
    private String website;

    public User(Long activity, String bio, String email, String fullname, String id, String imageurl, String phonenumber, String username, String website) {
        this.activity = activity;
        this.bio = bio;
        this.email = email;
        this.fullname = fullname;
        this.id = id;
        this.imageurl = imageurl;
        this.phonenumber = phonenumber;
        this.username = username;
        this.website = website;
    }
    public User() {
    }

    public Long isActivity() {
        return activity;
    }

    public void setActivity(Long activity) {
        this.activity = activity;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }
}
