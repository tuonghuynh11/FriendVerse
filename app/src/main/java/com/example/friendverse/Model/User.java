package com.example.friendverse.Model;

public class User {
    private String id;
    private String username;
    private String fullname;
    private String imageurl;
    private String bio;
    public User(){

    }
    public User(String ID, String USERNAME, String fullname, String imageurl, String bio){
        id = ID;
        username = USERNAME;
        this.fullname = fullname;
        this.imageurl = imageurl;
        this.bio = bio;
    }
    public String getId(){
        return id;
    }
    public String getUsername(){
        return username;
    }
    public String getImageurl(){
        return imageurl;
    }
    public String getBio(){
        return bio;
    }


}
