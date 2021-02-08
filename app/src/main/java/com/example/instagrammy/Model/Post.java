package com.example.instagrammy.Model;

public class Post {
    private String postId;
    private String postImage;
    private String publisher;

    public Post(String postId, String postImage, String publisher){
        this.postId = postId;
        this.postImage = postImage;
        this.publisher = publisher;
    }

    public Post() {
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getPostImage() {
        return postImage;
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }
}
