package com.example.friendverse.Model;

public class Post {
    private String postid;
    private boolean isShared;
    private String postimage;
    private String postvid;
    private String sharer;
    private String description;
    private String publisher;
    private String repostText;
    private String postType;
    private String Imagine;
    private String username;
    private int repostCount;
    public Post(String postid,String Imagine,String username, String postType, String sharer,boolean isShared, String postimage, String postvid, String description, String publisher) {
        this.postid = postid;
        this.sharer = sharer;
        this.postType = postType;
        this.postimage = postimage;
        this.postvid = postvid;
        this.isShared = true;
        this.description = description;
        this.Imagine =Imagine;
        this.publisher = publisher;
        this.username = username;
    }

    public Post() {
    }
    public String getRepostText() {
        return repostText;
    }

    public void setRepostText(String repostText) {
        this.repostText = repostText;
    }
    public String getSharer() {
        return sharer;
    }

    public void setSharer(String sharer) {
        this.sharer = sharer;
    }

    public boolean isShared() {
        return isShared;
    }

    public void setShared(boolean shared) {
        isShared = shared;
    }
    public int getRepostCount() {
        return repostCount;
    }

    public void setRepostCount(int repostCount) {
        this.repostCount = repostCount;
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

    public String getPostvid() {
        return postvid;
    }

    public void setPostvid(String postvid) {
        this.postvid = postvid;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getPostType() {
     return  postType;}

        public void setPostType( String postType){
            this.postType = postType;
        }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setImage_profile(String toString) {

    }

    public String getImagine() {
        return Imagine;
    }

    public void setImagine(String imagine) {
        Imagine = imagine;
    }
}


