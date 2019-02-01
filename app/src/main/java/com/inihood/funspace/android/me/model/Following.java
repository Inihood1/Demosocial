package com.inihood.funspace.android.me.model;

import com.inihood.funspace.android.me.helper.BlogPostId;

import java.util.Date;

public class Following extends com.inihood.funspace.android.me.helper.BlogPostId{
    private String thumb;
    private String image;
    private String first;
    private String last;
    private String id;
    private Date timestamp;
    private String nick;

    public Following() { }

    public Following(String thumb, String image, String first, String last, String id, Date timestamp, String nick) {
        this.thumb = thumb;
        this.image = image;
        this.first = first;
        this.last = last;
        this.id = id;
        this.timestamp = timestamp;
        this.nick = nick;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getFirst() {
        return first;
    }

    public void setFirst(String first) {
        this.first = first;
    }

    public String getLast() {
        return last;
    }

    public void setLast(String last) {
        this.last = last;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }
}
