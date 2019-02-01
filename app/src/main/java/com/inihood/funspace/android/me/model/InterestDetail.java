package com.inihood.funspace.android.me.model;

import com.inihood.funspace.android.me.helper.BlogPostId;

import java.util.Date;

public class InterestDetail extends com.inihood.funspace.android.me.helper.BlogPostId{
    public String user_id;
    public String image_url;
    public String desc;
    public String image_thumb;
    public String name;
    public String user_image;
    private Date timestamp;

    public InterestDetail(){

    }

    public InterestDetail(String user_id, String image_url,
                          String desc, String image_thumb, String name, String user_image, Date timestamp) {
        this.user_id = user_id;
        this.image_url = image_url;
        this.desc = desc;
        this.image_thumb = image_thumb;
        this.name = name;
        this.user_image = user_image;
        this.timestamp = timestamp;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getImage_thumb() {
        return image_thumb;
    }

    public void setImage_thumb(String image_thumb) {
        this.image_thumb = image_thumb;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUser_image() {
        return user_image;
    }

    public void setUser_image(String user_image) {
        this.user_image = user_image;
    }
}
