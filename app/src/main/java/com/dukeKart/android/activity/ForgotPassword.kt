package com.dukeKart.android.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
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
import java.util.regex.Pattern

class ForgotPassword : BaseActivity(), View.OnClickListener {
    var tv_title: TextView? = null
    var iv_back: ImageView? = null
    var iv_search: ImageView? = null
    var tif_reg_mob_no: TextInputEditText? = null
    var btn_confirm: Button? = null
    var rlCart: RelativeLayout? = null

    fun phoeNumberWithOutCountryCode(phoneNumberWithCountryCode: String?): String? {
        val complie: Pattern = Pattern.compile(" ")
        val phonenUmber: Array<String> = complie.split(phoneNumberWithCountryCode)
        Log.e("number is", phonenUmber[1])
        return phonenUmber[1]
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)
        setIds()
        setListeners()
        setData()
    }

    private fun setData() {
        tv_title!!.text = "Forgot Password"
        iv_search!!.visibility = View.INVISIBLE
        rlCart!!.visibility = View.INVISIBLE
    }

    private fun setIds() {
        tv_title = findViewById(R.id.tv_title)
        iv_back = findViewById(R.id.iv_back)
        tif_reg_mob_no = findViewById(R.id.tif_reg_mob_no)
        btn_confirm = findViewById(R.id.btn_confirm)
        iv_search = findViewById(R.id.iv_search)
        rlCart = findViewById(R.id.rlCart)


    }

    private fun setListeners() {
        iv_back!!.setOnClickListener(this)
        btn_confirm!!.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.iv_back -> onBackPressed()
            R.id.btn_confirm -> if (validate()) forgotPasswordApi()
        }
    }

    private fun validate(): Boolean {
        if (!AppUtils.isNumberValid(AppUtils.getTextInputEditTextData(tif_reg_mob_no))) {
            AppUtils.showToast(this@ForgotPassword, "Enter a valid mobile number")
        } else {
            return true
        }
        return false
    }

    private fun forgotPasswordApi() {
        showLoading()
        AppsStrings.setApiString("forgotPassword")
        val url = AppUrls.ForgotPasswordSendOtp
        Log.v(AppsStrings.apiUrl, url)
        val json = JSONObject()
        val json_data = JSONObject()
        try {
            json_data.put("mobile", AppUtils.getTextInputEditTextData(tif_reg_mob_no))
            json.put(AppsStrings.BASEJSON, json_data)
            Log.v(AppsStrings.apiJson, json.toString())
        } catch (e: Exception) {
            AppUtils.showToast(this@ForgotPassword, AppsStrings.defResponse)
            Log.v(AppsStrings.apiErrorException, e.message!!)
            e.printStackTrace()
        }
        AndroidNetworking.post(url)
                .addJSONObjectBody(json_data)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        parseforgotPasswordApi(response)
                    }

                    override fun onError(error: ANError) {
                        hideLoading()
                        // handle error
                        if (error.errorCode != 0) {
                            AppUtils.showToast(this@ForgotPassword, AppsStrings.defResponse)
                            Log.v(AppsStrings.apiErrorCode, "onError errorCode : " + error.errorCode)
                            Log.v(AppsStrings.apiErrorBody, "onError errorBody : " + error.errorBody)
                            Log.v(AppsStrings.apiErrorDetail, "onError errorDetail : " + error.errorDetail)
                        } else {
                            AppUtils.showToast(this@ForgotPassword, AppsStrings.defResponse)
                            Log.v(AppsStrings.apiErrorException, error.message!!)
                        }
                    }
                })
    }

    private fun parseforgotPasswordApi(response: JSONObject) {
        Log.d(AppsStrings.apiResponse, response.toString())
        try {
            val jsonObject = response.getJSONObject("ecommerce")
            hideLoading()
            AppUtils.showToast(this@ForgotPassword, jsonObject.getString("res_msg"))
            if (jsonObject.getString("res_code").equals("1", ignoreCase = true)) startActivity(Intent(this@ForgotPassword, OtpVerification::class.java).putExtra(AppConstants.userMobile, AppUtils.getTextInputEditTextData(tif_reg_mob_no)))
        } catch (e: Exception) {
            Log.v(AppsStrings.apiErrorException, e.message!!)
            AppUtils.showToast(this@ForgotPassword, AppsStrings.defResponse)
        }
    }
}