package com.knotri.bridge;

import com.badlogic.gdx.Gdx;

import java.util.HashMap;

/**
 * Created by k on 09.08.15.
 */
public class SLogger {

    public static String getLineNumberAndFileName() {
        // \033[1;31m and the like allows paint output on linux (and maybe Mac Os X)
        return "\033[1;31m" +
                " lines: " + Thread.currentThread().getStackTrace()[3].getLineNumber() +
                ", file: " + Thread.currentThread().getStackTrace()[3].getFileName() + // TODO 'file' instead 'file.java'
                "\033[0m";
    }

//    public static String getColorLineNumber(){
//        return "\033[1;31m" + " lines: " + getLineNumber() + ", file: " + Thread.currentThread().getStackTrace()[5].getFileName() +  "\033[0m";
//    }

    public static void log(String log, Object obj){
        String fullLog = log + " " + obj.getClass().toString()  + getLineNumberAndFileName();
        Gdx.app.log("SLogger", fullLog);
    }

    public static void log(String log){
        Gdx.app.log("TAG", log );
    }

//    private static HashMap<String, String> messages = new HashMap<String, String>();
//    public static void logAtTimes(String log){
//        String key = getLineNumberAndFileName();
//        messages.put(key, log);
//    }


}