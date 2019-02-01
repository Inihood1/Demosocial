package com.inihood.funspace.android.me.model;

import java.util.Date;

public class Comments extends com.inihood.funspace.android.me.helper.BlogPostId{

    private String message, user_id, name, image, time;
    private Date timestamp;

    public Comments(){

    }

    public Comments(String message, String user_id, Date timestamp, String name, String image, String time) {
        this.message = message;
        this.user_id = user_id;
        this.timestamp = timestamp;
        this.name = name;
        this.image = image;
        this.time = time;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
