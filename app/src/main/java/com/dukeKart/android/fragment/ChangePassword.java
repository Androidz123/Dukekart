package com.dukeKart.android.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.android.material.textfield.TextInputEditText;
import com.dukeKart.android.R;
import com.dukeKart.android.common.AppUrls;
import com.dukeKart.android.common.AppsStrings;
import com.dukeKart.android.database.AppConstants;
import com.dukeKart.android.database.Preferences;
import com.dukeKart.android.views.BaseFragment;

import org.json.JSONObject;

import static com.dukeKart.android.common.AppUtils.getTextInputEditTextData;
import static com.dukeKart.android.common.AppUtils.showToast;
import static com.dukeKart.android.common.AppsStrings.apiErrorException;
import static com.dukeKart.android.common.AppsStrings.defResponse;

public class ChangePassword extends BaseFragment implements View.OnClickListener {
    TextInputEditText tif_oldPassword,tif_newPassword,tif_confPassword;
    Preferences pref;
    ImageView ivBack;
    View view;
    Button btn_subPassword;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.change_password_frag, container, false);
        setIDs();
        setListeners();

        return view;

    }

    public void setIDs() {


        pref = new Preferences(mActivity);
        tif_oldPassword = view.findViewById(R.id.tif_oldPassword);
        tif_newPassword = view.findViewById(R.id.tif_newPassword);
        tif_confPassword = view.findViewById(R.id.tif_confPassword);
        btn_subPassword = view.findViewById(R.id.btn_subPassword);
    }

    public void setListeners() {
        btn_subPassword.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_subPassword:
                if(validate()){
                    changePasswordApi();
                }

        }
    }

    private boolean validate() {
        if (getTextInputEditTextData(tif_oldPassword).isEmpty()) {
            showToast(mActivity, "Enter old password");
        } else if (getTextInputEditTextData(tif_newPassword).length() < 8) {
            showToast(mActivity, "New Password must be of 8 characters");
        } else if (!getTextInputEditTextData(tif_confPassword).equalsIgnoreCase(getTextInputEditTextData(tif_newPassword))) {
            showToast(mActivity, "Password mismatch");
        }else {
            return true;
        }
        return false;
    }

    private void changePasswordApi() {

        showLoading();
        AppsStrings.setApiString("changePassword");
        String url = AppUrls.changePassword;
        Log.v(AppsStrings.apiUrl, url);
        JSONObject json = new JSONObject();
        JSONObject json_data = new JSONObject();

        try {
            json_data.put("mobile", pref.get(AppConstants.userMobile));
            json_data.put("oldPassword", getTextInputEditTextData(tif_oldPassword));
            json_data.put("newPassword", getTextInputEditTextData(tif_newPassword));
            json_data.put("confirmPassword", getTextInputEditTextData(tif_confPassword));
            json.put(AppsStrings.BASEJSON, json_data);
            Log.v(AppsStrings.apiJson, json.toString());
        } catch (Exception e) {
            showToast(mActivity, defResponse);
            Log.v(apiErrorException, e.getMessage());
            e.printStackTrace();
        }


        AndroidNetworking.post(url)
                .addJSONObjectBody(json_data)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        parseChangePasswordApi(response);
                    }

                    @Override
                    public void onError(ANError error) {

                        hideLoading();
                        // handle error
                        if (error.getErrorCode() != 0) {
                            showToast(mActivity, defResponse);
                            Log.v(AppsStrings.apiErrorCode, "onError errorCode : " + error.getErrorCode());
                            Log.v(AppsStrings.apiErrorBody, "onError errorBody : " + error.getErrorBody());
                            Log.v(AppsStrings.apiErrorDetail, "onError errorDetail : " + error.getErrorDetail());
                        } else {
                            showToast(mActivity, defResponse);
                            Log.v(apiErrorException, error.getMessage());

                        }
                    }
                });
    }
    private void parseChangePasswordApi(JSONObject response) {

        Log.d(AppsStrings.apiResponse, response.toString());
        try {
            JSONObject jsonObject = response.getJSONObject("ecommerce");
            hideLoading();
            showToast(mActivity, jsonObject.getString("res_msg"));
            tif_oldPassword.setText("");
            tif_newPassword.setText("");
            tif_confPassword.setText("");

        } catch (Exception e) {
            Log.v(apiErrorException, e.getMessage());
            showToast(mActivity, defResponse);
        }
    }



}
