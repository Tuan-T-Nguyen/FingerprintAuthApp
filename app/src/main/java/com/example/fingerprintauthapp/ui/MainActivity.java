package com.example.fingerprintauthapp.ui;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Toast;

import com.example.fingerprintauthapp.SharedPrefs;
import com.example.fingerprintauthapp.biometricauth.BiometricCallback;
import com.example.fingerprintauthapp.biometricauth.BiometricManager;
import com.example.fingerprintauthapp.R;
import com.example.fingerprintauthapp.databinding.ActivityMainBinding;
import com.example.fingerprintauthapp.util.Keyboard;
import com.example.fingerprintauthapp.viewmodels.LoginForm;
import com.example.fingerprintauthapp.viewmodels.LoginViewModel;

public class MainActivity extends BaseActivity implements BiometricCallback {

    private ActivityMainBinding mBinding;
    private LoginViewModel loginViewModel;
    private String emailLogin = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupBinding(savedInstanceState);
        setupObserve();

        emailLogin = SharedPrefs.getInstance().get(getString(R.string.shared_saved_email_login), String.class);
        int isTurnOnFinger = SharedPrefs.getInstance().get(getString(R.string.shared_turn_on_fingerprint_function), Integer.class);
        if (emailLogin != null && !emailLogin.equals("") && isTurnOnFinger == 1) {
            mBinding.setIsSupportFinger(true);
        }
    }


    private void setupBinding(Bundle savedInstanceState) {
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        if (savedInstanceState == null) {

        }
        mBinding.setIsSupportFinger(false);

        loginViewModel = ViewModelProviders.of(this).get(LoginViewModel.class);
        mBinding.setLoginViewModel(loginViewModel);

        mBinding.btnBiometric.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new BiometricManager.BiometricBuilder(MainActivity.this)
                        .setTitle(getString(R.string.biometric_title))
                        .setSubtitle("Login to your " + emailLogin + " account")
                        .setDescription(getString(R.string.biometric_description))
                        .setNegativeButtonText(getString(R.string.biometric_negative_button_text))
                        .build()
                        .authenticate(MainActivity.this);
            }
        });
    }

    private void setupObserve() {
        loginViewModel.getBtnLogin().observe(this, new Observer<LoginForm>() {
            @Override
            public void onChanged(@Nullable final LoginForm loginForm) {
                if (loginForm != null) {
                    Keyboard.hideSoftKeyboard(MainActivity.this);
                    showLoading();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // Assume you login successfully
                            SharedPrefs.getInstance().put(getString(R.string.shared_saved_email_login), loginForm.email.get());
                            hideLoading();
                            Toast.makeText(MainActivity.this, loginForm.email.get(), Toast.LENGTH_SHORT).show();

                            goAfterLoginScreen();

                        }
                    }, 2000);

                }
            }
        });
    }

    private void goAfterLoginScreen() {
        Intent intent = new Intent(this, SecondActivity.class);
        startActivity(intent);
        finish();
    }


    @Override
    public void onSdkVersionNotSupported() {
        Toast.makeText(getApplicationContext(), getString(R.string.biometric_error_sdk_not_supported), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBiometricAuthenticationNotSupported() {
        Toast.makeText(getApplicationContext(), getString(R.string.biometric_error_hardware_not_supported), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBiometricAuthenticationNotAvailable() {
        Toast.makeText(getApplicationContext(), getString(R.string.biometric_error_fingerprint_not_available), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBiometricAuthenticationPermissionNotGranted() {
        Toast.makeText(getApplicationContext(), getString(R.string.biometric_error_permission_not_granted), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBiometricAuthenticationInternalError(String error) {
        Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAuthenticationFailed() {
//        Toast.makeText(getApplicationContext(), getString(R.string.biometric_failure), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAuthenticationCancelled() {
        Toast.makeText(getApplicationContext(), getString(R.string.biometric_cancelled), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAuthenticationSuccessful() {
        goAfterLoginScreen();
    }

    @Override
    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
//        Toast.makeText(getApplicationContext(), helpString, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAuthenticationError(int errorCode, CharSequence errString) {
//        Toast.makeText(getApplicationContext(), errString, Toast.LENGTH_LONG).show();
    }
}
