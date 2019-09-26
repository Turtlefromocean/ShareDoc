package com.example.sharedoc.DB;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DB_Post_info {
    private String 제목;
    private String 내용;
    private String 작성자;  //uid
    private String 파일이름;
    private String 파일url;
    private Date createdAt;
    private String 문서ID;
    private String 공감;
    private String 본횟수;
    private String 태그;
    private String id;
    private String 소속;
    private String 주소;
    private int 가격;


    public DB_Post_info(String 제목, String 내용, String 작성자, Date createdAt, String 문서ID, String 공감, String 본횟수, String 태그, String id, String 소속, String 주소, int 가격) {
        this.제목 = 제목;
        this.내용 = 내용;
        this.작성자 = 작성자;
        this.createdAt = createdAt;
        this.문서ID = 문서ID;
        this.공감 = 공감;
        this.본횟수 = 본횟수;
        this.태그 = 태그;
        this.id = id;
        this.소속 = 소속;
        this.주소 = 주소;
        this.가격 = 가격;
    }

   

    public DB_Post_info(String 제목, String 내용, String 작성자, String 파일이름, String 파일url, Date createdAt, String 문서ID, String 공감, String 본횟수, String 태그, String id, String 소속, String 주소, int 가격) {
            this.제목 = 제목;
        this.내용 = 내용;
        this.작성자 = 작성자;
        this.파일이름 = 파일이름;
        this.파일url = 파일url;
        this.createdAt = createdAt;
        this.문서ID = 문서ID;
        this.공감 = 공감;
        this.본횟수 = 본횟수;
        this.태그 = 태그;
        this.id = id;
        this.소속 = 소속;
        this.주소 = 주소;
        this.가격 = 가격;
    }

    public String get제목() {
        return 제목;
    }

    public void set제목(String 제목) {
        this.제목 = 제목;
    }

    public String get내용() {
        return 내용;
    }

    public void set내용(String 내용) {
        this.내용 = 내용;
    }

    public String get작성자() {
        return 작성자;
    }

    public void set작성자(String 작성자) {
        this.작성자 = 작성자;
    }

    public String get파일이름() {
        return 파일이름;
    }

    public void set파일이름(String 파일이름) {
        this.파일이름 = 파일이름;
    }

    public String get파일url() {
        return 파일url;
    }

    public void set파일url(String 파일url) {
        this.파일url = 파일url;
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


    public String get공감() {
        return 공감;
    }

    public void set공감(String 공감) {
        this.공감 = 공감;
    }

    public String get본횟수() {
        return 본횟수;
    }

    public void set본횟수(String 본횟수) {
        this.본횟수 = 본횟수;
    }

    public String get태그() {
        return 태그;
    }

    public void set태그(String 태그) {
        this.태그 = 태그;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String get소속() {
        return 소속;
    }

    public void set소속(String 소속) {
        this.소속 = 소속;
    }

    public String get주소() {
        return 주소;
    }

    public void set주소(String 주소) {
        this.주소 = 주소;
    }

    public int get가격() {
        return 가격;
    }

    public void set가격(int 가격) {
        this.가격 = 가격;
    }
}
