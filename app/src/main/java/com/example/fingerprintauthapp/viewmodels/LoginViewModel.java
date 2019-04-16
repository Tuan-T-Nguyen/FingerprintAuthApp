package com.example.fingerprintauthapp.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;


public class LoginViewModel extends AndroidViewModel {

    private LoginForm loginForm;
    private MutableLiveData<LoginForm> btnLogin = new MutableLiveData<>();

    public LoginViewModel(@NonNull Application application) {
        super(application);
        loginForm = new LoginForm("", "");
    }

    public void onLogin() {
        // Todo check valid
        btnLogin.setValue(loginForm);
    }

    public LoginForm getLoginForm() {
        return loginForm;
    }

    public MutableLiveData<LoginForm> getBtnLogin() {
        return btnLogin;
    }
}
