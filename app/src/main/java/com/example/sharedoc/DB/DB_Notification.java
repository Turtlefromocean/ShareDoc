package com.example.sharedoc.DB;


public class DB_Notification {

    public String to;
    public Notification notification = new Notification();

    public class Notification {
        public String title;
        public String text;
    }

}
