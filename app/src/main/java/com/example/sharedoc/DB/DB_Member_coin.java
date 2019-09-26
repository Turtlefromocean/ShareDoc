package com.example.sharedoc.DB;

public class DB_Member_coin {

    private String uid;
    private int coin;

    public DB_Member_coin(String uid, int coin) {
        this.uid = uid;
        this.coin = coin;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public int getCoin() {
        return coin;
    }

    public void setCoin(int coin) {
        this.coin = coin;
    }
}
