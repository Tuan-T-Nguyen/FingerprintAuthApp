package com.example.fingerprintauthapp.viewmodels;

import android.databinding.ObservableField;

public class LoginForm {
    public final ObservableField<String> email = new ObservableField<>();
    public final ObservableField<String> password = new ObservableField<>();

    public LoginForm(String email, String password) {
        this.email.set(email);
        this.password.set(password);
    }
}
