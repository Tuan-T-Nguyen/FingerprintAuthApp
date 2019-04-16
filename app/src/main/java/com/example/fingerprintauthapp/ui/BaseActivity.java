package com.example.fingerprintauthapp.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

@SuppressLint("Registered")
public class BaseActivity extends AppCompatActivity {

    private AlertDialog mAlertDialog;
    private String loadTitle;

    public String getLoadTitle() {
        return loadTitle;
    }

    public void setLoadTitle(String title) {
        loadTitle = title;
    }

    private void initProgress() {
        int llPadding = 30;
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setPadding(llPadding, llPadding, llPadding, llPadding);
        ll.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams llParam = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        llParam.gravity = Gravity.CENTER;
        ll.setLayoutParams(llParam);

        ProgressBar progressBar = new ProgressBar(this);
        progressBar.setIndeterminate(true);
        progressBar.setPadding(0, 0, llPadding, 0);
        progressBar.setLayoutParams(llParam);

        ll.addView(progressBar);
        if (getLoadTitle() != null) {
            llParam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            llParam.gravity = Gravity.CENTER;
            TextView tvText = new TextView(this);
            llParam.topMargin = 20;

            tvText.setTextColor(Color.parseColor("#000000"));
            tvText.setTextSize(22);
            tvText.setTextColor(Color.WHITE);
            tvText.setLayoutParams(llParam);
            tvText.setText(getLoadTitle());
            ll.addView(tvText);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setView(ll);

        mAlertDialog = builder.create();
    }

    public void hideLoading() {
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
        }
    }

    public void showLoading() {
        initProgress();
        hideLoading();
        mAlertDialog.show();
        Window window = mAlertDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }
}
