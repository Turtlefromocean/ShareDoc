package com.example.sharedoc.DB;

import java.util.Date;

public class DB_Comment {
    private String 작성자;
    private String 댓글;
    private Date createdAt;
    private String 문서ID;
    private String uid;
    private String 댓글ID;

    public DB_Comment(String 작성자, String 댓글, Date createdAt, String 문서ID, String uid, String 댓글ID) {
        this.작성자 = 작성자;
        this.댓글 = 댓글;
        this.createdAt = createdAt;
        this.문서ID = 문서ID;
        this.uid = uid;
        this.댓글ID = 댓글ID;
    }

    public String get작성자() {
        return 작성자;
    }

    public void set작성자(String 작성자) {
        this.작성자 = 작성자;
    }


    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String get문서ID() {
        return 문서ID;
    }

    public void set문서ID(String 문서ID) {
        this.문서ID = 문서ID;
    }

    public String get댓글() {
        return 댓글;
    }

    public void set댓글(String 댓글) {
        this.댓글 = 댓글;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String get댓글ID() {
        return 댓글ID;
    }

    public void set댓글ID(String 댓글ID) {
        this.댓글ID = 댓글ID;
    }
}
