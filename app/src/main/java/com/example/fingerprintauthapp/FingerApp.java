package com.example.fingerprintauthapp;

import android.app.Application;

public class FingerApp extends Application {
    private static FingerApp instance;

    public static FingerApp getInstance() {
        return instance;
    }

    public static FingerApp getContext(){
        return instance;
    }

    @Override
    public void onCreate() {
        instance = this;
        super.onCreate();
    }
}
