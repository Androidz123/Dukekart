package com.dukeKart.android.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.dukeKart.android.R
import com.dukeKart.android.common.AppUrls
import com.dukeKart.android.common.AppUtils
import com.dukeKart.android.common.AppsStrings
import com.dukeKart.android.database.AppConstants
import com.dukeKart.android.database.Preferences
import com.dukeKart.android.views.BaseActivity
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONObject

class Register : BaseActivity(), View.OnClickListener {
    var login_btn: Button? = null
    var login_singup: Button? = null
    var tv_title: TextView? = null
    var iv_back: ImageView? = null
    var tif_fullname: TextInputEditText? = null
    var tif_mobileno: TextInputEditText? = null
    var tif_emailid: TextInputEditText? = null
    var tif_password: TextInputEditText? = null
    var prefs: Preferences? = null
    var tif_confirmPassword: TextInputEditText? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        setIds()
        setListeners()
        setData()
    }

    private fun setData() {
        tv_title!!.text = resources.getString(R.string.app_name) + " Registration"
    }

    private fun setIds() {
        prefs = Preferences(mActivity)
        login_btn = findViewById(R.id.login_btn)
        login_singup = findViewById(R.id.login_singup)
        tv_title = findViewById(R.id.tv_title)
        iv_back = findViewById(R.id.iv_back)
        tif_fullname = findViewById(R.id.tif_fullname)
        tif_mobileno = findViewById(R.id.tif_mobileno)
        val mobno:String=prefs!!.get(AppConstants.userMobile)
        //tif_mobileno.setText(prefs!!.get(AppConstants.userMobile))
        tif_mobileno!!.setText(mobno)
        tif_emailid = findViewById(R.id.tif_emailid)
        tif_password = findViewById(R.id.tif_password)
        tif_confirmPassword = findViewById(R.id.tif_confirmPassword)
    }

    private fun setListeners() {
        login_btn!!.setOnClickListener(this)
        login_singup!!.setOnClickListener(this)
        iv_back!!.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.login_singup -> if (validate()) {
                registerApi()
            }
            R.id.login_btn -> startActivity(Intent(this@Register, Login::class.java))
            R.id.iv_back -> onBackPressed()
        }
    }

    fun validate(): Boolean {
        if (AppUtils.getTextInputEditTextData(tif_fullname).isEmpty()) {
            AppUtils.showToast(this@Register, "Name cannot be blank")
        } else if (!AppUtils.isNumberValid(AppUtils.getTextInputEditTextData(tif_mobileno))) {
            AppUtils.showToast(this@Register, "Enter a valid mobile number")
        } else if (!AppUtils.isEmailValid(AppUtils.getTextInputEditTextData(tif_emailid))) {
            AppUtils.showToast(this@Register, "Enter a valid email Id")
        } else if (AppUtils.getTextInputEditTextData(tif_password).isEmpty()) {
            AppUtils.showToast(this@Register, "Enter a password")
        } else if (AppUtils.getTextInputEditTextData(tif_password).length < 8) {
            AppUtils.showToast(this@Register, "Password must be of 8 characters")
        } else if (!AppUtils.getTextInputEditTextData(tif_password).equals(AppUtils.getTextInputEditTextData(tif_confirmPassword), ignoreCase = true)) {
            AppUtils.showToast(this@Register, "Password mismatch")
        } else {
            return true
        }
        return false
    }

    private fun registerApi() {
        showLoading()
        AppsStrings.setApiString("Signup")
        Log.v(AppsStrings.apiUrl, AppUrls.signup)
        val json = JSONObject()
        val json_data = JSONObject()
        try {
            json_data.put("name", AppUtils.getTextInputEditTextData(tif_fullname))
            json_data.put("mobile", AppUtils.getTextInputEditTextData(tif_mobileno))
            json_data.put("email", AppUtils.getTextInputEditTextData(tif_emailid))
            json_data.put("password", AppUtils.getTextInputEditTextData(tif_password))
           // json.put(AppConstants.appName, json_data)
            Log.v(AppsStrings.apiJson, json.toString())
        } catch (e: Exception) {
            AppUtils.showToast(this@Register, AppsStrings.defResponse)
            Log.v(AppsStrings.apiErrorException, e.message!!)
            e.printStackTrace()
        }
        AndroidNetworking.post(AppUrls.signup)
                .addJSONObjectBody(json_data)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        parseRegisterApi(response)
                    }

                    override fun onError(error: ANError) {
                        hideLoading()
                        // handle error
                        if (error.errorCode != 0) {
                            AppUtils.showToast(this@Register, AppsStrings.defResponse)
                            Log.v(AppsStrings.apiErrorCode, "onError errorCode : " + error.errorCode)
                            Log.v(AppsStrings.apiErrorBody, "onError errorBody : " + error.errorBody)
                            Log.v(AppsStrings.apiErrorDetail, "onError errorDetail : " + error.errorDetail)
                        } else {
                            AppUtils.showToast(this@Register, AppsStrings.defResponse)
                            Log.v(AppsStrings.apiErrorException, error.message!!)
                        }
                    }
                })
    }

    private fun parseRegisterApi(response: JSONObject) {
        Log.d(AppsStrings.apiResponse, response.toString())
        try {
            val jsonObject = response.getJSONObject("ecommerce")
            hideLoading()
            AppUtils.showToast(this@Register, jsonObject.getString("res_msg"))
            if (jsonObject.getString("res_code").equals("1", ignoreCase = true)) {
                startActivity(Intent(this@Register, Login::class.java))
                /*  JSONObject userObject = jsonObject.getJSONObject("data");
                prefs.set(AppConstants.minimumOrderBalance,userObject.getString("min_order_bal"));
                prefs.set(AppConstants.shippingCharge,userObject.getString("shipping _amount"));
                prefs.commit();*/
            }
        } catch (e: Exception) {
            Log.v(AppsStrings.apiErrorException, e.message!!)
            AppUtils.showToast(this@Register, AppsStrings.defResponse)
        }
        //        finish();
    }
}