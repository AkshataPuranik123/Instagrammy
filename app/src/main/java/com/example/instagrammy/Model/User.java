package com.example.instagrammy.Model;

public class User {

    public String username, password, email, bio;

    //Constructor 1
    public User(){}

    //Constructor 2
    public User(String email, String password, String username, String bio){

        this.email = email;
        this.password = password;
        this.username = username;
        this.bio = bio;

    }

}
