package com.inihood.funspace.android.me.model;

import com.inihood.funspace.android.me.helper.BlogPostId;

import java.util.Date;

public class Notification extends BlogPostId{

    private String user_id_who_trigger;
    private String user_image_who_trigger;
    private String id_of_the_thing_that_was_triggerd;
    private String the_text;
    private Date timestamp;
    private String type;
    private String time;

    public Notification(String user_id_who_trigger, String user_image_who_trigger,
                        String id_of_the_thing_that_was_triggerd, String the_text ,
                        Date timestamp, String type, String time) {
        this.user_id_who_trigger = user_id_who_trigger;
        this.user_image_who_trigger = user_image_who_trigger;
        this.id_of_the_thing_that_was_triggerd = id_of_the_thing_that_was_triggerd;
        this.the_text = the_text;
        this.timestamp = timestamp;
        this.type = type;
        this.time = time;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getThe_text() {
        return the_text;
    }

    public void setThe_text(String the_text) {
        this.the_text = the_text;
    }

    public String getUser_id_who_trigger() {
        return user_id_who_trigger;
    }

    public void setUser_id_who_trigger(String user_id_who_trigger) {
        this.user_id_who_trigger = user_id_who_trigger;
    }

    public String getUser_image_who_trigger() {
        return user_image_who_trigger;
    }

    public void setUser_image_who_trigger(String user_image_who_trigger) {
        this.user_image_who_trigger = user_image_who_trigger;
    }

    public String getId_of_the_thing_that_was_triggerd() {
        return id_of_the_thing_that_was_triggerd;
    }


    public String getTime() {
        return time;
    }

    public void setTime(Date timestamp) {
        this.timestamp = timestamp;
    }
}
