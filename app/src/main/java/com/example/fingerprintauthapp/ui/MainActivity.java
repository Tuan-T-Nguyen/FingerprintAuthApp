package com.example.fingerprintauthapp.ui;



import android.app.KeyguardManager;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.example.fingerprintauthapp.SharedPrefs;
import com.example.fingerprintauthapp.util.FingerprintHandler;
import com.example.fingerprintauthapp.R;
import com.example.fingerprintauthapp.databinding.ActivityMainBinding;
import com.example.fingerprintauthapp.util.Keyboard;
import com.example.fingerprintauthapp.viewmodels.LoginForm;
import com.example.fingerprintauthapp.viewmodels.LoginViewModel;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

// todo https://riptutorial.com/android/example/29719/how-to-use-android-fingerprint-api-to-save-user-passwords
//https://www.sitepoint.com/securing-your-android-apps-with-the-fingerprint-api/
public class MainActivity extends BaseActivity implements FingerprintHandler.Callback {

    private ActivityMainBinding mBinding;
    private LoginViewModel loginViewModel;

    private FingerprintManager fingerprintManager;
    private KeyguardManager keyguardManager;

    private static final String DIALOG_FRAGMENT_TAG = "myFragment";
    private static final String SECRET_MESSAGE = "Very secret message";
    private static final String KEY_NAME_NOT_INVALIDATED = "key_not_invalidated";
    static final String DEFAULT_KEY_NAME = "default_key";

    private KeyStore mKeyStore;
    private KeyGenerator mKeyGenerator;
    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupBinding(savedInstanceState);
        setupObserve();

        String emailLogin = SharedPrefs.getInstance().get(getString(R.string.shared_saved_email_login), String.class);
        int isTurnOnFinger = SharedPrefs.getInstance().get(getString(R.string.shared_turn_on_fingerprint_function), Integer.class);
        if (emailLogin != null && emailLogin.equals("")
            || isTurnOnFinger == 0) {
            mBinding.setIsSupportFinger(false);
        } else {
            try {
                mKeyStore = KeyStore.getInstance("AndroidKeyStore");
            } catch (KeyStoreException e) {
                throw new RuntimeException("Failed to get an instance of KeyStore", e);
            }
            try {
                mKeyGenerator = KeyGenerator
                        .getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
            } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
                throw new RuntimeException("Failed to get an instance of KeyGenerator", e);
            }
            Cipher defaultCipher;
            //Cipher cipherNotInvalidated;
            try {
                defaultCipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                        + KeyProperties.BLOCK_MODE_CBC + "/"
                        + KeyProperties.ENCRYPTION_PADDING_PKCS7);
//            cipherNotInvalidated = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
//                    + KeyProperties.BLOCK_MODE_CBC + "/"
//                    + KeyProperties.ENCRYPTION_PADDING_PKCS7);
            } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
                throw new RuntimeException("Failed to get an instance of Cipher", e);
            }
            mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                fingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);
                keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);

                if(!fingerprintManager.isHardwareDetected()){
                    mBinding.setIsSupportFinger(false);
                } else if (!keyguardManager.isKeyguardSecure()){
                    mBinding.setWarningText("Secure lock screen hasn't set up.\nGo to 'Settings -> Security -> Fingerprint' to set up a fingerprint");
                } else if (!fingerprintManager.hasEnrolledFingerprints()){
                    mBinding.setWarningText("Go to 'Settings -> Security -> Fingerprint' and register at least one fingerprint");
                } else {
                    mBinding.setIsSupportFinger(true);

                    createKey(DEFAULT_KEY_NAME, true);
                    //createKey(KEY_NAME_NOT_INVALIDATED, false);

                    if (initCipher(defaultCipher, DEFAULT_KEY_NAME)) {
                        FingerprintManager.CryptoObject cryptoObject = new FingerprintManager.CryptoObject(defaultCipher);

                        boolean useFingerprintPreference = mSharedPreferences
                                .getBoolean("use_fingerprint_to_authenticate_key",
                                        true);
                        if (useFingerprintPreference) {
                            FingerprintHandler fingerprintHandler = new FingerprintHandler(this, this);
                            fingerprintHandler.startAuth(fingerprintManager, cryptoObject);
                        }

                    }
                }
            }
        }
    }


    private void setupBinding(Bundle savedInstanceState) {
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        if (savedInstanceState == null) {

        }
        mBinding.setWarningText("");
        mBinding.setIsSupportFinger(false);

        loginViewModel = ViewModelProviders.of(this).get(LoginViewModel.class);
        mBinding.setLoginViewModel(loginViewModel);
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

    /**
     * Creates a symmetric key in the Android Key Store which can only be used after the user has
     * authenticated with fingerprint.
     *
     * @param keyName the name of the key to be created
     * @param invalidatedByBiometricEnrollment if {@code false} is passed, the created key will not
     *                                         be invalidated even if a new fingerprint is enrolled.
     *                                         The default value is {@code true}, so passing
     *                                         {@code true} doesn't change the behavior
     *                                         (the key will be invalidated if a new fingerprint is
     *                                         enrolled.). Note that this parameter is only valid if
     *                                         the app works on Android N developer preview.
     *
     */
    public void createKey(String keyName, boolean invalidatedByBiometricEnrollment) {
        // The enrolling flow for fingerprint. This is where you ask the user to set up fingerprint
        // for your flow. Use of keys is necessary if you need to know if the set of
        // enrolled fingerprints has changed.
        try {
            mKeyStore.load(null);
            // Set the alias of the entry in Android KeyStore where the key will appear
            // and the constrains (purposes) in the constructor of the Builder

            KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder(keyName,
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    // Require the user to authenticate with a fingerprint to authorize every use
                    // of the key
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7);

            // This is a workaround to avoid crashes on devices whose API level is < 24
            // because KeyGenParameterSpec.Builder#setInvalidatedByBiometricEnrollment is only
            // visible on API level +24.
            // Ideally there should be a compat library for KeyGenParameterSpec.Builder but
            // which isn't available yet.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                builder.setInvalidatedByBiometricEnrollment(invalidatedByBiometricEnrollment);
            }
            mKeyGenerator.init(builder.build());
            mKeyGenerator.generateKey();
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException
                | CertificateException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Initialize the {@link Cipher} instance with the created key in the
     * {@link #createKey(String, boolean)} method.
     *
     * @param keyName the key name to init the cipher
     * @return {@code true} if initialization is successful, {@code false} if the lock screen has
     * been disabled or reset after the key was generated, or if a fingerprint got enrolled after
     * the key was generated.
     */
    private boolean initCipher(Cipher cipher, String keyName) {
        try {
            mKeyStore.load(null);
            SecretKey key = (SecretKey) mKeyStore.getKey(keyName, null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return true;
        } catch (KeyPermanentlyInvalidatedException e) {
            return false;
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException | IOException
                | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }
    }

//    @TargetApi(Build.VERSION_CODES.M)
//    private void generateKey() {
//
//        try {
//
//            keyStore = KeyStore.getInstance("AndroidKeyStore");
//            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
//
//            keyStore.load(null);
//            keyGenerator.init(new
//                    KeyGenParameterSpec.Builder(KEY_NAME,
//                    KeyProperties.PURPOSE_ENCRYPT |
//                            KeyProperties.PURPOSE_DECRYPT)
//                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
//                    .setUserAuthenticationRequired(true)
//                    .setEncryptionPaddings(
//                            KeyProperties.ENCRYPTION_PADDING_PKCS7)
//                    .build());
//            keyGenerator.generateKey();
//
//        } catch (KeyStoreException | IOException | CertificateException
//                | NoSuchAlgorithmException | InvalidAlgorithmParameterException
//                | NoSuchProviderException e) {
//
//            e.printStackTrace();
//
//        }
//
//    }

//    @TargetApi(Build.VERSION_CODES.M)
//    public boolean cipherInit() {
//        try {
//            cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7);
//        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
//            throw new RuntimeException("Failed to get Cipher", e);
//        }
//
//
//        try {
//
//            keyStore.load(null);
//
//            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME,
//                    null);
//
//            cipher.init(Cipher.ENCRYPT_MODE, key);
//
//            return true;
//
//        } catch (KeyPermanentlyInvalidatedException e) {
//            return false;
//        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
//            throw new RuntimeException("Failed to init Cipher", e);
//        }
//
//    }


    private void goAfterLoginScreen() {
        Intent intent = new Intent(this, SecondActivity.class);
        startActivity(intent);
        finish();
    }
    @Override
    public void onFingerAuthenticated() {
        goAfterLoginScreen();
    }

    @Override
    public void onFingerError() {

    }
}
