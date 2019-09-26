package com.example.sharedoc.DB;

import android.widget.EditText;

public class DB_Member_info {

    private String id;
    private String name;
    private String introduce;
    private String school;
    private String major;
    private String photoUrl;
    private String uid;
    private String followers;
    private String following;
    private String address;
    public String pushToken;


    public DB_Member_info(String id, String name, String introduce, String school, String major, String photoUrl, String uid, String followers, String following, String address) {
        this.id = id;
        this.name = name;
        this.introduce = introduce;
        this.school = school;
        this.major = major;
        this.photoUrl = photoUrl;
        this.uid = uid;
        this.followers = followers;
        this.following = following;
        this.address = address;
    }

    public DB_Member_info(String id, String name, String introduce, String school, String major, String uid, String followers, String following, String address) {
        this.id = id;
        this.name = name;
        this.introduce = introduce;
        this.school = school;
        this.major = major;
        this.uid = uid;
        this.followers = followers;
        this.following = following;
        this.address = address;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIntroduce() {
        return introduce;
    }

    public void setIntroduce(String introduce) {
        this.introduce = introduce;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getFollowers() {
        return followers;
    }

    public void setFollowers(String followers) {
        this.followers = followers;
    }

    public String getFollowing() {
        return following;
    }

    public void setFollowing(String following) {
        this.following = following;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
