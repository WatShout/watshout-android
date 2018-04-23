package com.watshout.face;

public class CurrentID {
    public static String id;

    public static String getCurrent(){
        return id;
    }

    public static void setCurrent(String c){
        id = c;
    }
}