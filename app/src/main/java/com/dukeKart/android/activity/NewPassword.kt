package com.dukeKart.android.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.android.material.textfield.TextInputEditText
import com.dukeKart.android.R
import com.dukeKart.android.common.AppUrls
import com.dukeKart.android.common.AppUtils
import com.dukeKart.android.common.AppsStrings
import com.dukeKart.android.database.AppConstants
import com.dukeKart.android.views.BaseActivity
import org.json.JSONObject

class NewPassword : BaseActivity(), View.OnClickListener {
    var tif_newPassword: TextInputEditText? = null
    var tif_confPassword: TextInputEditText? = null
    var btn_subPassword: Button? = null
    var regMobile: String? = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_password)
        setIDs()
        setListeners()
        if (intent.hasExtra(AppConstants.userMobile)) regMobile = intent.getStringExtra(AppConstants.userMobile)
    }

    fun setIDs() {
        tif_newPassword = findViewById(R.id.tif_newPassword)
        tif_confPassword = findViewById(R.id.tif_confPassword)
        btn_subPassword = findViewById(R.id.btn_subPassword)
    }

    fun setListeners() {
        btn_subPassword!!.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_subPassword -> if (validate()) {
                changePasswordApi()
            }
        }
    }

    private fun validate(): Boolean {
        if (AppUtils.getTextInputEditTextData(tif_newPassword).length < 8) {
            AppUtils.showToast(this@NewPassword, "New Password must be of 8 characters")
        } else if (!AppUtils.getTextInputEditTextData(tif_confPassword).equals(AppUtils.getTextInputEditTextData(tif_newPassword), ignoreCase = true)) {
            AppUtils.showToast(this@NewPassword, "Password mismatch")
        } else {
            return true
        }
        return false
    }

    private fun changePasswordApi() {
        showLoading()
        AppsStrings.setApiString("ForgotPassword")
        val url = AppUrls.ForgotPassword
        Log.v(AppsStrings.apiUrl, url)
        val json = JSONObject()
        val json_data = JSONObject()
        try {
            json_data.put("mobile", regMobile)
            json_data.put("newPassword", AppUtils.getTextInputEditTextData(tif_newPassword))
            json_data.put("confPassword", AppUtils.getTextInputEditTextData(tif_confPassword))
            json.put(AppsStrings.BASEJSON, json_data)
            Log.v(AppsStrings.apiJson, json.toString())
        } catch (e: Exception) {
            AppUtils.showToast(this@NewPassword, AppsStrings.defResponse)
            Log.v(AppsStrings.apiErrorException, e.message!!)
            e.printStackTrace()
        }
        AndroidNetworking.post(url)
                .addJSONObjectBody(json_data)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        parseChangePasswordApi(response)
                    }

                    override fun onError(error: ANError) {
                        hideLoading()
                        // handle error
                        if (error.errorCode != 0) {
                            AppUtils.showToast(this@NewPassword, AppsStrings.defResponse)
                            Log.v(AppsStrings.apiErrorCode, "onError errorCode : " + error.errorCode)
                            Log.v(AppsStrings.apiErrorBody, "onError errorBody : " + error.errorBody)
                            Log.v(AppsStrings.apiErrorDetail, "onError errorDetail : " + error.errorDetail)
                        } else {
                            AppUtils.showToast(this@NewPassword, AppsStrings.defResponse)
                            Log.v(AppsStrings.apiErrorException, error.message!!)
                        }
                    }
                })
    }

    private fun parseChangePasswordApi(response: JSONObject) {
        Log.d(AppsStrings.apiResponse, response.toString())
        try {
            val jsonObject = response.getJSONObject("ecommerce")
            hideLoading()
            AppUtils.showToast(this@NewPassword, jsonObject.getString("res_msg"))
            if (jsonObject.getString("res_code").equals("1", ignoreCase = true)) {
                startActivity(Intent(this@NewPassword, Login::class.java))
                finish()
            }
            tif_newPassword!!.setText("")
            tif_confPassword!!.setText("")
        } catch (e: Exception) {
            Log.v(AppsStrings.apiErrorException, e.message!!)
            AppUtils.showToast(this@NewPassword, AppsStrings.defResponse)
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this@NewPassword, Login::class.java))
        finish()
    }
}