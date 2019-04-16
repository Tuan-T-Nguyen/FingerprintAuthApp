package com.example.fingerprintauthapp.ui;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;

import com.example.fingerprintauthapp.R;
import com.example.fingerprintauthapp.SharedPrefs;
import com.example.fingerprintauthapp.databinding.ActivityMainBinding;
import com.example.fingerprintauthapp.databinding.ActivitySecondBinding;
import com.example.fingerprintauthapp.viewmodels.FingerprintViewModel;

public class SecondActivity extends BaseActivity {

    private ActivitySecondBinding mBinding;
    private FingerprintViewModel fingerprintViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupBinding(savedInstanceState);

        mBinding.swFinger.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPrefs.getInstance().put(getString(R.string.shared_turn_on_fingerprint_function), isChecked ? 1 : 0);
            }
        });

        mBinding.btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
    }

    private void setupBinding(Bundle savedInstanceState) {
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_second);

        fingerprintViewModel = ViewModelProviders.of(this).get(FingerprintViewModel.class);
        mBinding.setViewModel(fingerprintViewModel);
    }

    private void logout() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
    }
}
