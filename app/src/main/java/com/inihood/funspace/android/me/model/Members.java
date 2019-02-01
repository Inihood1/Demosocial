package com.inihood.funspace.android.me.model;

import java.util.Date;

public class Members extends com.inihood.funspace.android.me.helper.BlogPostId{
   private String member_id;
   private String thumb;
   private String name;
   private Date timestamp;
   private String  interest_id;

    public Members() {

    }

    public Members(String member_id, String thumb, String name, Date timestamp, String  interest_id) {
        this.member_id = member_id;
        this.thumb = thumb;
        this.name = name;
        this.timestamp = timestamp;
        this.interest_id = interest_id;
    }

    public String getInterest_id() {
        return interest_id;
    }

    public void setInterest_id(String interest_id) {
        this.interest_id = interest_id;
    }

    public String getMember_id() {
        return member_id;
    }

    public void setMember_id(String member_id) {
        this.member_id = member_id;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
