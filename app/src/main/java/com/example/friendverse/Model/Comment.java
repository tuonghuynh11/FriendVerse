package com.example.friendverse.Model;

public class Comment {
    private String publisher;
    private String comment;
    private String commentID;
    public String getPublisher(){
        return publisher;
    }
    public void setPublisher(String id){
        publisher = id;
    }
    public String getComment(){
        return comment;
    }
    public void setComment(String cmt){
        comment = cmt;
    }
    public String getCommentID(){
        return commentID;
    }
    public void setCommentID(String id){
        commentID = id;
    }
}
