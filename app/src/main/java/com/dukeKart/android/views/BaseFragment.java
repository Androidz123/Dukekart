package com.dukeKart.android.views;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.dukeKart.android.common.DialogUtil;

public class BaseFragment extends Fragment {
    protected Activity mActivity;
    private ProgressDialog mProgressDialog;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mActivity = getActivity();
    }

    public void showLoading() {
        mProgressDialog = DialogUtil.showLoadingDialog(mActivity, "Base Fragment");
        mProgressDialog.setCancelable(false);
    }



    public void hideLoading() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.cancel();
        }
    }
}
