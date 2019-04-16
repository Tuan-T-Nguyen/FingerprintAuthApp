package com.example.fingerprintauthapp.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;

import com.example.fingerprintauthapp.R;
import com.example.fingerprintauthapp.SharedPrefs;

public class FingerprintViewModel extends AndroidViewModel {

    public boolean isTurnOnFinger;

    public FingerprintViewModel(@NonNull Application application) {
        super(application);
        int fingerStatus = SharedPrefs.getInstance().get(getApplication().getString(R.string.shared_turn_on_fingerprint_function), Integer.class);
        isTurnOnFinger = fingerStatus != 0;
    }

}
