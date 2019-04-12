package com.example.fingerprintauthapp;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;

@TargetApi(Build.VERSION_CODES.M)
public interface FingerAuthenticationCallback {


    void onAuthenticationError(int errorCode, CharSequence errString);


    void onAuthenticationFailed() ;


    void onAuthenticationHelp(int helpCode, CharSequence helpString) ;


    void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result);
}
