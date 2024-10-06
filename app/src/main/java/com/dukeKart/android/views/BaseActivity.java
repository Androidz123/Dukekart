package com.dukeKart.android.views;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.anshulthakur.networkstatechecker.InternetStateChecker;
import com.dukeKart.android.R;
import com.dukeKart.android.common.DialogUtil;


public class BaseActivity extends AppCompatActivity {

    private ProgressDialog mProgressDialog;
    @SuppressLint("StaticFieldLeak")
    protected static Activity mActivity;
    InternetStateChecker internetStateChecker;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        mActivity = this;
        checkInterNet();
    }

    private void checkInterNet() {
        internetStateChecker = new InternetStateChecker.Builder(this)
                .setDialogTitle("No Internet")
                .setCancelable(false)
                .setDialogBgColor(R.color.colorRed)
                .setDialogTextColor(R.color.colorWhite)
                .setDialogIcon(R.drawable.ic_no_wifi)
                .setDialogMessage("Internet connection lost")
                .build();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        internetStateChecker.stop();
    }

    public void showLoading() {
        mProgressDialog = DialogUtil.showLoadingDialog(mActivity, "Base Activity");
        mProgressDialog.setCancelable(false);
    }


    public void hideLoading() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.cancel();
        }
    }
}
