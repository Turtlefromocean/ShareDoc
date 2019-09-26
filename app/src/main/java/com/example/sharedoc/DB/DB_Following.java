package com.example.sharedoc.DB;

public class DB_Following {
    private String id;
    private String Uid;

    public DB_Following(String id, String uid) {
        this.id = id;
        Uid = uid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUid() {
        return Uid;
    }

    public void setUid(String uid) {
        Uid = uid;
    }

}
