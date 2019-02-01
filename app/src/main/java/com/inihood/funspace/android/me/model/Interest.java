package com.inihood.funspace.android.me.model;

import java.util.Date;

public class Interest extends com.inihood.funspace.android.me.helper.BlogPostId{

    private String admin_user_id;
    private String admin_name;
    private String admin_image;
    private String members;
    private String title;
    private String post;
    private String cover_image;
    private Date timestamp;
    private String ask_to_join;
    private String visible_to_public;

    public Interest(){

    }

    public Interest(String admin_user_id, String admin_name, String admin_image,
                    String posts ,String members, String title, String cover_image,
                    Date timestamp, String ask_to_join, String visible_to_public) {
        this.admin_user_id = admin_user_id;
        this.admin_name = admin_name;
        this.admin_image = admin_image;
        this.members = members;
        this.title = title;
        this.post = posts;
        this.cover_image = cover_image;
        this.timestamp = timestamp;
        this.ask_to_join = ask_to_join;
        this.visible_to_public = visible_to_public;
    }

    public String getVisible_to_public() {
        return visible_to_public;
    }

    public void setVisible_to_public(String visible_to_public) {
        this.visible_to_public = visible_to_public;
    }

    public String getAsk_to_join() {
        return ask_to_join;
    }

    public void setAsk_to_join(String ask_to_join) {
        this.ask_to_join = ask_to_join;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getCover_image() {
        return cover_image;
    }

    public void setCover_image(String cover_image) {
        this.cover_image = cover_image;
    }

    public String getPosts() {
        return post;
    }

    public void setPosts(String posts) {
        this.post = posts;
    }

    public String getAdmin_user_id() {
        return admin_user_id;
    }

    public void setAdmin_user_id(String admin_user_id) {
        this.admin_user_id = admin_user_id;
    }

    public String getAdmin_name() {
        return admin_name;
    }

    public void setAdmin_name(String admin_name) {
        this.admin_name = admin_name;
    }

    public String getAdmin_image() {
        return admin_image;
    }

    public void setAddmin_image(String admin_image) {
        this.admin_image = admin_image;
    }

    public String getMembers() {
        return members;
    }

    public void setMembers(String members) {
        this.members = members;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
