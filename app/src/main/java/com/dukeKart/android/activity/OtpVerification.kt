package com.dukeKart.android.activity

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Html
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
import com.dukeKart.android.R
import com.dukeKart.android.common.AppUrls
import com.dukeKart.android.common.AppUtils
import com.dukeKart.android.common.AppsStrings
import com.dukeKart.android.database.AppConstants
import com.dukeKart.android.views.BaseActivity
import org.json.JSONObject

class OtpVerification : BaseActivity(), View.OnClickListener {
    var tv_mobiletext: TextView? = null
    var iv_back: ImageView? = null
    var button1: Button? = null
    var button2: Button? = null
    var button3: Button? = null
    var button4: Button? = null
    var button5: Button? = null
    var button6: Button? = null
    var button7: Button? = null
    var button8: Button? = null
    var button9: Button? = null
    var button0: Button? = null
    var btn_verify: Button? = null
    var buttonDeleteBack: RelativeLayout? = null
    var pinBox0: TextView? = null
    var pinBox1: TextView? = null
    var pinBox2: TextView? = null
    var pinBox3: TextView? = null
    var pinBox4: TextView? = null
    var pinBox5: TextView? = null
    var tvOtptimer: TextView? = null
    var tvResendcode: TextView? = null
    var dig0 = ""
    var dig1 = ""
    var dig2 = ""
    var dig3 = ""
    var dig4 = ""
    var dig5 = ""
    var iv_search: ImageView? = null
    var rlCart: RelativeLayout? = null
    var regMobile: String? = ""
    var finalOtp = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp_verification)
        setIds()
        setListeners()
        setData()
        CountreaderOTp()
    }

    private fun setData() {
        if (intent.hasExtra(AppConstants.userMobile)) regMobile = intent.getStringExtra(AppConstants.userMobile)
        tv_mobiletext!!.text = "Otp has been send to the registered mobile number $regMobile"
        iv_search!!.visibility = View.INVISIBLE
        rlCart!!.visibility = View.INVISIBLE
    }

    private fun setIds() {
        tv_mobiletext = findViewById(R.id.tv_mobiletext)
        iv_back = findViewById(R.id.iv_back)
        iv_search = findViewById(R.id.iv_search)
        rlCart = findViewById(R.id.rlCart)
        button0 = findViewById(R.id.button0)
        button1 = findViewById(R.id.button1)
        button2 = findViewById(R.id.button2)
        button3 = findViewById(R.id.button3)
        button4 = findViewById(R.id.button4)
        button5 = findViewById(R.id.button5)
        button6 = findViewById(R.id.button6)
        button7 = findViewById(R.id.button7)
        button8 = findViewById(R.id.button8)
        button9 = findViewById(R.id.button9)
        buttonDeleteBack = findViewById(R.id.buttonDeleteBack)
        pinBox0 = findViewById(R.id.pinBox0)
        pinBox1 = findViewById(R.id.pinBox1)
        pinBox2 = findViewById(R.id.pinBox2)
        pinBox3 = findViewById(R.id.pinBox3)
        pinBox4 = findViewById(R.id.pinBox4)
        pinBox5 = findViewById(R.id.pinBox5)
        btn_verify = findViewById(R.id.btn_verify)
        tvOtptimer = findViewById(R.id.tvOtptimer)
        tvResendcode = findViewById(R.id.tvResendcode)
    }

    private fun setListeners() {
        iv_back!!.setOnClickListener(this)
        button0!!.setOnClickListener(this)
        button1!!.setOnClickListener(this)
        button2!!.setOnClickListener(this)
        button3!!.setOnClickListener(this)
        button4!!.setOnClickListener(this)
        button5!!.setOnClickListener(this)
        button6!!.setOnClickListener(this)
        button7!!.setOnClickListener(this)
        button8!!.setOnClickListener(this)
        button9!!.setOnClickListener(this)
        buttonDeleteBack!!.setOnClickListener(this)
        btn_verify!!.setOnClickListener(this)
        tvResendcode!!.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.iv_back -> onBackPressed()
            R.id.button0 -> setOtp("0")
            R.id.button1 -> setOtp("1")
            R.id.button2 -> setOtp("2")
            R.id.button3 -> setOtp("3")
            R.id.button4 -> setOtp("4")
            R.id.button5 -> setOtp("5")
            R.id.button6 -> setOtp("6")
            R.id.button7 -> setOtp("7")
            R.id.button8 -> setOtp("8")
            R.id.button9 -> setOtp("9")
            R.id.buttonDeleteBack -> setOtp("null")
            R.id.tvResendcode -> forgotPasswordApi()
            R.id.btn_verify -> verifyOtpApi()
        }
    }

    fun setOtp(digit: String) {
        if (digit.equals("null", ignoreCase = true)) {
            btn_verify!!.visibility = View.GONE
            if (!dig5.equals("", ignoreCase = true)) {
                dig5 = ""
            } else if (!dig4.equals("", ignoreCase = true)) {
                dig4 = ""
            } else if (!dig3.equals("", ignoreCase = true)) {
                dig3 = ""
            } else if (!dig2.equals("", ignoreCase = true)) {
                dig2 = ""
            } else if (!dig1.equals("", ignoreCase = true)) {
                dig1 = ""
            } else if (!dig0.equals("", ignoreCase = true)) {
                dig0 = ""
            }
        } else {
            if (dig0.equals("", ignoreCase = true)) {
                dig0 = digit
            } else if (dig1.equals("", ignoreCase = true)) {
                dig1 = digit
            } else if (dig2.equals("", ignoreCase = true)) {
                dig2 = digit
            } else if (dig3.equals("", ignoreCase = true)) {
                dig3 = digit
            } else if (dig4.equals("", ignoreCase = true)) {
                dig4 = digit
            } else if (dig5.equals("", ignoreCase = true)) {
                dig5 = digit
                btn_verify!!.visibility = View.VISIBLE
            }
        }
        pinBox0!!.text = dig0
        pinBox1!!.text = dig1
        pinBox2!!.text = dig2
        pinBox3!!.text = dig3
        pinBox4!!.text = dig4
        pinBox5!!.text = dig5
        finalOtp = dig0 + dig1 + dig2 + dig3 + dig4 + dig5
        Log.v("MyOtp", "+$finalOtp")
    }

    private fun CountreaderOTp() {
        val millisInFuture: Long = 30000
        val countDownInterval: Long = 1000
        object : CountDownTimer(millisInFuture, countDownInterval) {
            override fun onTick(millisUntilFinished: Long) {
                tvOtptimer!!.visibility = View.VISIBLE
                tvOtptimer!!.text = "You will get OTP by SMS in " + millisUntilFinished / 1000 + " " + "Seconds"
                tvResendcode!!.visibility = View.GONE
            }

            override fun onFinish() {
                tvOtptimer!!.visibility = View.GONE
                val account = AppUtils.getColoredSpanned(resources.getString(R.string.didntRecieveTheCode), resources.getColor(R.color.grey).toString())
                val signup = AppUtils.getColoredSpanned("<b>" + resources.getString(R.string.resend) + "</b>", resources.getColor(R.color.white).toString())
                tvResendcode!!.text = Html.fromHtml("$account $signup")
                tvResendcode!!.visibility = View.VISIBLE
            }
        }.start()
    }

    private fun forgotPasswordApi() {
        showLoading()
        AppsStrings.setApiString("forgotPassword")
        val url = AppUrls.ForgotPasswordSendOtp
        Log.v(AppsStrings.apiUrl, url)
        val json = JSONObject()
        val json_data = JSONObject()
        try {
            json_data.put("mobile", regMobile)
            json.put(AppsStrings.BASEJSON, json_data)
            Log.v(AppsStrings.apiJson, json.toString())
        } catch (e: Exception) {
            AppUtils.showToast(this@OtpVerification, AppsStrings.defResponse)
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
                            AppUtils.showToast(this@OtpVerification, AppsStrings.defResponse)
                            Log.v(AppsStrings.apiErrorCode, "onError errorCode : " + error.errorCode)
                            Log.v(AppsStrings.apiErrorBody, "onError errorBody : " + error.errorBody)
                            Log.v(AppsStrings.apiErrorDetail, "onError errorDetail : " + error.errorDetail)
                        } else {
                            AppUtils.showToast(this@OtpVerification, AppsStrings.defResponse)
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
            AppUtils.showToast(this@OtpVerification, jsonObject.getString("res_msg"))
            if (jsonObject.getString("res_code").equals("1", ignoreCase = true)) CountreaderOTp()
        } catch (e: Exception) {
            Log.v(AppsStrings.apiErrorException, e.message!!)
            AppUtils.showToast(this@OtpVerification, AppsStrings.defResponse)
        }
    }

    private fun verifyOtpApi() {
        showLoading()
        AppsStrings.setApiString("VerifyOtp")
        val url = AppUrls.VerifyOtp
        Log.v(AppsStrings.apiUrl, url)
        val json = JSONObject()
        val json_data = JSONObject()
        try {
            json_data.put("mobile", regMobile)
            json_data.put("otp", finalOtp.trim { it <= ' ' })
            json.put(AppsStrings.BASEJSON, json_data)
            Log.v(AppsStrings.apiJson, json.toString())
        } catch (e: Exception) {
            AppUtils.showToast(this@OtpVerification, AppsStrings.defResponse)
            Log.v(AppsStrings.apiErrorException, e.message!!)
            e.printStackTrace()
        }
        AndroidNetworking.post(url)
                .addJSONObjectBody(json_data)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        parseVerifyOtpApi(response)
                    }

                    override fun onError(error: ANError) {
                        hideLoading()
                        // handle error
                        if (error.errorCode != 0) {
                            AppUtils.showToast(this@OtpVerification, AppsStrings.defResponse)
                            Log.v(AppsStrings.apiErrorCode, "onError errorCode : " + error.errorCode)
                            Log.v(AppsStrings.apiErrorBody, "onError errorBody : " + error.errorBody)
                            Log.v(AppsStrings.apiErrorDetail, "onError errorDetail : " + error.errorDetail)
                        } else {
                            AppUtils.showToast(this@OtpVerification, AppsStrings.defResponse)
                            Log.v(AppsStrings.apiErrorException, error.message!!)
                        }
                    }
                })
    }

    private fun parseVerifyOtpApi(response: JSONObject) {
        Log.d(AppsStrings.apiResponse, response.toString())
        try {
            val jsonObject = response.getJSONObject("ecommerce")
            hideLoading()
            AppUtils.showToast(this@OtpVerification, jsonObject.getString("res_msg"))
            if (jsonObject.getString("res_code").equals("1", ignoreCase = true)) startActivity(Intent(this@OtpVerification, NewPassword::class.java).putExtra(AppConstants.userMobile, regMobile))
        } catch (e: Exception) {
            Log.v(AppsStrings.apiErrorException, e.message!!)
            AppUtils.showToast(this@OtpVerification, AppsStrings.defResponse)
        }
    }
}