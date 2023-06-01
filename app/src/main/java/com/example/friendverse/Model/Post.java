package com.example.friendverse.Model;

public class Post {
    private String postid;
    private String postimage;
    private String postvid;
    private String description;
    private String publisher;

    public Post(String postid, String postimage, String postvid, String description, String publisher) {
        this.postid = postid;
        this.postimage = postimage;
        this.postvid = postvid;
        this.description = description;
        this.publisher = publisher;
    }

    public Post() {
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
        if (postimage != null && postvid == null) {
            return "image";
        } else if (postimage == null && postvid != null) {
            return "video";
        }
            return "";
        }
    }


