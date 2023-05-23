package com.example.friendverse.Model;

public class Story {
    String storyID;
    String userID;
    String imageURL;
    long timeCreated;
    long  after1day;
    public Story(){

    }
    public Story(String storyID, String uID, String URL, long timeStart, long timeEnd){
        this.storyID = storyID;
        userID = uID;
        imageURL = URL;
        timeCreated = timeStart;
        after1day = timeEnd;
    }
    public String getStoryID(){
        return storyID;
    }
    public void setStoryID(String id){
        storyID = id;
    }
    public String getUserID(){
        return userID;
    }
    public void setUserID(String id){
        userID = id;
    }
    public String getImageURL(){
        return imageURL;
    }
    public void setImageURL(String url){
        imageURL = url;
    }
    public long getTimeCreated(){
        return timeCreated;
    }
    public void setTimeCreated(long i){
        timeCreated = i;
    }
    public long getAfter1day(){
        return after1day;
    }
    public void setAfter1day(long timeCreated){
        after1day = timeCreated;
    }


}
