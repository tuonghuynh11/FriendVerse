package com.example.friendverse.Model;

public class User {

    private String id;
    private String imageurl;
    private String bio;
    private String username;
    private String fullname;
    private String email;

    public User() {
    }


    public User(String id, String username, String fullname, String imageurl, String bio, String email){
        this.id = id;
        this.imageurl = imageurl;
        this.bio =bio;
        this.username = username;
        this.fullname = fullname;
        this.email = email;

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
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
