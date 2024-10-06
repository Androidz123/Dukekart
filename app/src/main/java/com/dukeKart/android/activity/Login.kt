package com.dukeKart.android.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.chaos.view.PinView
import com.dukeKart.android.R
import com.dukeKart.android.common.AppUrls
import com.dukeKart.android.common.AppUtils
import com.dukeKart.android.common.AppsStrings
import com.dukeKart.android.database.AppConstants
import com.dukeKart.android.database.Preferences
import com.dukeKart.android.views.BaseActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import org.json.JSONObject
import java.util.regex.Pattern


class Login : BaseActivity(), View.OnClickListener {
    var  login_singup: TextView? = null
    var  login_btn: Button? = null
    var  const_reg: ConstraintLayout? = null
    var  const_layout_login: ConstraintLayout? = null
    var  pinview: PinView? = null
    var  login_btn1      : Button? = null
    var  pinview1        : PinView? = null
    var  et_cont_mob_no1 : TextInputEditText? = null

    var resendotp: TextView? = null
    var tv_title: TextView? = null
    var tv_Forgot_Password: TextView? = null
    var iv_back: ImageView? = null
    var iv_search: ImageView? = null
    var et_reg_mob_no: TextInputEditText? = null
    var et_cont_mob_no: TextInputEditText? = null
    var tif_password: TextInputEditText? = null
    var prefs: Preferences? = null
    var timeoutSeconds = 60L
    var verificationcode: String? = null
    var fcmToken: String? = ""
    var rlCart: RelativeLayout? = null
    // var pinView: PinView? = null
    var pin:String?=""
    var mobno: String =""


    var contno: String =""
    var pinno: String =""

    fun phoeNumberWithOutCountryCode(phoneNumberWithCountryCode: String?): String? {
        val complie: Pattern = Pattern.compile(" ")
        val phonenUmber: Array<String> = complie.split(phoneNumberWithCountryCode)
        Log.e("number is", phonenUmber[1])
        return phonenUmber[1]
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        FirebaseApp.initializeApp(this)
        setIds()
        setListeners()
        setData()
        getFcmToken()
    }

    private fun setData() {
        prefs = Preferences(this@Login)
        tv_title!!.text = resources.getString(R.string.app_name) + " Login"
        iv_search!!.visibility = View.INVISIBLE
        rlCart!!.visibility = View.INVISIBLE
    }

    private fun setIds() {
        login_singup = findViewById(R.id.login_singup)
        tv_title = findViewById(R.id.tv_title)
        resendotp= findViewById(R.id.resendotp)

        iv_search = findViewById(R.id.iv_search)
        rlCart = findViewById(R.id.rlCart)
        iv_back = findViewById(R.id.iv_back)
        et_cont_mob_no = findViewById(R.id.et_cont_mob_no)
        login_btn = findViewById(R.id.login_btn)
        pinview = findViewById(R.id.pinview)

        contno = et_cont_mob_no!!.getText().toString()
        pinno= pinview!!.getText().toString()
        et_cont_mob_no!!.addTextChangedListener(textWatcher);
        const_layout_login = findViewById(R.id.const_layout_login)
        tv_Forgot_Password = findViewById(R.id.tv_Forgot_Password)
        et_reg_mob_no = findViewById(R.id.et_reg_mob_no)
        tif_password = findViewById(R.id.tif_password)
    }

    private val textWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            // get the content of both the edit text
            contno = et_cont_mob_no!!.getText().toString()
            pinno= pinview!!.getText().toString()
            if(contno.length==10){

                SendOTPApi(contno,"Login")


            }


            // check whether both the fields are empty or not

        }

        override fun afterTextChanged(s: Editable) {}
    }
    private fun setListeners() {
        login_singup!!.setOnClickListener(this)
        tv_Forgot_Password!!.setOnClickListener(this)
        iv_back!!.setOnClickListener(this)
        resendotp!!.setOnClickListener(this)
        login_btn!!.setOnClickListener(this)
    }

    private val textWatcher1: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            mobno=""
        }
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            // get the content of both the edit text
            mobno=""
            mobno= et_cont_mob_no1!!.getText().toString()
            pin = pinview1!!.getText().toString()
            if(mobno.length==10){

                SendOTPApi(mobno,"Registration")


            }


            // check whether both the fields are empty or not

        }

        override fun afterTextChanged(s: Editable) {}
    }
    override fun onClick(view: View) {
        when (view.id) {
            R.id.tv_Forgot_Password -> startActivity(Intent(this@Login, ForgotPassword::class.java))
            R.id.login_singup ->alertreg()
            R.id.resendotp -> if(contno.length==10){
                object : CountDownTimer(30000, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        resendotp?.setText("seconds remaining: " + millisUntilFinished / 1000)
                        // logic to set the EditText could go here
                    }

                    override fun onFinish() {
                        resendotp?.setText("Resend Again !")
                    }
                }.start()
                SendOTPApi(contno,"Login")


            }

            /*      const_reg !!.visibility = View.VISIBLE
              const_layout_login !!.visibility = View.GONE*/
            /* startActivity(Intent(mActivity, Login::class.java))*//*startActivity(Intent(this@Login, Register::class.java))*/
            R.id.iv_back -> onBackPressed()
            R.id.login_btn ->{
                contno = et_cont_mob_no!!.getText().toString()
                pinno= pinview!!.getText().toString()

                VerifyOTPApi(contno, pinno)

            }



        }
    }

    private fun  alertreg(){
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.phone_dialog, null)
        //AlertDialogBuilder
        val mBuilder = AlertDialog.Builder(this)
            .setView(mDialogView)

        //show dialog

        val  mAlertDialog = mBuilder.show()
        login_btn1        =mAlertDialog.findViewById<Button>(R.id.login_btn1)
        pinview1           =mAlertDialog.findViewById<PinView>(R.id.pinview1)
        et_cont_mob_no1  =mAlertDialog.findViewById<TextInputEditText>(R.id.et_cont_mob_no1)

        et_cont_mob_no1!!.addTextChangedListener(textWatcher1);
        /*  val mobno1: String = et_cont_mob_no1!!.getText().toString()
          val pin1: String = pinview1!!.getText().toString()*/

        //login button click of custom layout
        login_btn1!!.setOnClickListener {
            //dismiss dialog
            mAlertDialog.dismiss()
            //get text from EditTexts of custom layout
            val pin = pinview1!!.text.toString()
            val contactno = et_cont_mob_no1!!.text.toString()



            VerifyOTPApi(contactno,pin)


            //set the input text in TextView

        }
        //cancel button click of custom layout

    }
    fun validate(): Boolean {
        if (!AppUtils.isNumberValid(AppUtils.getTextInputEditTextData(et_reg_mob_no))) {
            AppUtils.showToast(this@Login, "Enter a valid mobile number")
        } else if (AppUtils.getTextInputEditTextData(tif_password).isEmpty()) {
            AppUtils.showToast(this@Login, "Enter a password")
        } else {
            return true
        }
        return false
    }

    private fun getFcmToken() {

        FirebaseMessaging.getInstance().token.addOnSuccessListener { result ->
            if(result != null){
                fcmToken = result
                prefs!![AppConstants.fcmToken] = fcmToken
                prefs!!.commit()
            }
        }

//        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener(this) { instanceIdResult ->
//            fcmToken = instanceIdResult.token
//            prefs!![AppConstants.fcmToken] = fcmToken
//            prefs!!.commit()
//            Log.e("fcmoken", fcmToken!!)
//        }
    }

    private fun LoginApi() {
        showLoading()
        AppsStrings.setApiString("LogIndeviceId")
        val url = AppUrls.Login
        Log.v(AppsStrings.apiUrl, url)
        val json = JSONObject()
        val json_data = JSONObject()
        try {
            json_data.put("mobile", AppUtils.getTextInputEditTextData(et_reg_mob_no))
            json_data.put("deviceId", AppUtils.getDeviceID(this@Login))
            json_data.put("fcmId", fcmToken)
            json_data.put("password", AppUtils.getTextInputEditTextData(tif_password))
            json.put(AppsStrings.BASEJSON, json_data)
            Log.v("TestApi", json_data.getString("deviceId")+"  --  "+ json_data.getString("fcmId"));
            AppUtils.showToast(this@Login, json_data.getString("deviceId")+"  --  "+ json_data.getString("fcmId"))
        } catch (e: Exception) {
            AppUtils.showToast(this@Login, AppsStrings.defResponse)
            Log.v(AppsStrings.apiErrorException, e.message!!)
            e.printStackTrace()
        }
        AndroidNetworking.post(url)
            .addJSONObjectBody(json_data)
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    parseLoginApi(response)
                }

                override fun onError(error: ANError) {
                    hideLoading()
                    // handle error
                    if (error.errorCode != 0) {
                        AppUtils.showToast(this@Login, AppsStrings.defResponse)
                        Log.v(AppsStrings.apiErrorCode, "onError errorCode : " + error.errorCode)
                        Log.v(AppsStrings.apiErrorBody, "onError errorBody : " + error.errorBody)
                        Log.v(AppsStrings.apiErrorDetail, "onError errorDetail : " + error.errorDetail)
                    } else {
                        AppUtils.showToast(this@Login, AppsStrings.defResponse)
                        Log.v(AppsStrings.apiErrorException, error.message!!)
                    }
                }
            })
    }

    private fun parseLoginApi(response: JSONObject) {
        Log.d(AppsStrings.apiResponse, response.toString())
        try {
            val jsonObject = response.getJSONObject("ecommerce")
            hideLoading()
            AppUtils.showToast(this@Login, jsonObject.getString("res_msg"))
            if (jsonObject.getString("res_code").equals("1", ignoreCase = true)) {
                prefs!![AppConstants.userName] = jsonObject.getString("userName")
                prefs!![AppConstants.userId] = jsonObject.getString("userId")
                prefs!![AppConstants.loginId] = jsonObject.getString("loginId")
                prefs!![AppConstants.userEmail] = jsonObject.getString("email")
                prefs!![AppConstants.userMobile] = jsonObject.getString("mobile")
                prefs!![AppConstants.userWhatsapNumber] = jsonObject.getString("whatsaap_number")
                prefs!![AppConstants.userAlternateNumber] = jsonObject.getString("alternate_number")
                prefs!![AppConstants.userAddress] = jsonObject.getString("address")
                prefs!![AppConstants.userLocality] = jsonObject.getString("locality")
                prefs!![AppConstants.userCity] = jsonObject.getString("city")
                prefs!![AppConstants.userState] = jsonObject.getString("state")
                prefs!![AppConstants.userPincode] = jsonObject.getString("pincode")
                prefs!![AppConstants.userWalletAmount] = jsonObject.getString("wallet_amount")
                prefs!![AppConstants.userProfPic] = jsonObject.getString("profile_pic")
                prefs!![AppConstants.loginCheck] = "true"
                prefs!!.commit()
                startActivity(Intent(this@Login, Dashboard::class.java))
                /*  JSONObject userObject = jsonObject.getJSONObject("data");
                prefs.set(AppConstants.minimumOrderBalance,userObject.getString("min_order_bal"));
                prefs.set(AppConstants.shippingCharge,userObject.getString("shipping _amount"));
                prefs.commit();*/
            }
        } catch (e: Exception) {
            Log.v(AppsStrings.apiErrorException, e.message!!)
            AppUtils.showToast(this@Login, AppsStrings.defResponse)
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this@Login, Dashboard::class.java))
    }




    private fun SendOTPApi(string: String, s: String) {
        showLoading()
        //  AppsStrings.setApiString("LogIndeviceId")
        //val url = AppUrls.Login
        val url = "https://dukekart.in/api/sendOtp"
        Log.v(AppsStrings.apiUrl, url)
        val json = JSONObject()
        val json_data = JSONObject()
        try {
            json_data.put("mobile", string)
            json_data.put("type",s )
            /*
                       json.put( json_data)
            */
            // Log.v("TestApi", json_data.getString("deviceId")+"  --  "+ json_data.getString("fcmId"));
            //  AppUtils.showToast(this@Login, json_data.getString("deviceId")+"  --  "+ json_data.getString("fcmId"))
        } catch (e: Exception) {
            AppUtils.showToast(this@Login, AppsStrings.defResponse)
            Log.v(AppsStrings.apiErrorException, e.message!!)
            e.printStackTrace()
        }
        AndroidNetworking.post(url)
            .addJSONObjectBody(json_data)
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    parsesendotpApi(response)
                }

                override fun onError(error: ANError) {
                    hideLoading()
                    // handle error
                    if (error.errorCode != 0) {
                        AppUtils.showToast(this@Login, AppsStrings.defResponse)
                        Log.v(AppsStrings.apiErrorCode, "onError errorCode : " + error.errorCode)
                        Log.v(AppsStrings.apiErrorBody, "onError errorBody : " + error.errorBody)
                        Log.v(AppsStrings.apiErrorDetail, "onError errorDetail : " + error.errorDetail)
                    } else {
                        AppUtils.showToast(this@Login, AppsStrings.defResponse)
                        Log.v(AppsStrings.apiErrorException, error.message!!)
                    }
                }
            })
    }

    private fun parsesendotpApi(response: JSONObject) {
        Log.d(AppsStrings.apiResponse, response.toString())
        try {
            val jsonObject = response.getJSONObject("ecommerce")
            hideLoading()

            AppUtils.showToast(this@Login, jsonObject.getString("res_msg"))
            if (jsonObject.getString("res_code").equals("1", ignoreCase = true)) {

                prefs!![AppConstants.userMobile] = jsonObject.getString("mobile")

                prefs!!.commit()

                /*  JSONObject userObject = jsonObject.getJSONObject("data");
                prefs.set(AppConstants.minimumOrderBalance,userObject.getString("min_order_bal"));
                prefs.set(AppConstants.shippingCharge,userObject.getString("shipping _amount"));
                prefs.commit();*/
            }
        } catch (e: Exception) {
            Log.v(AppsStrings.apiErrorException, e.message!!)
            AppUtils.showToast(this@Login, AppsStrings.defResponse)
        }
    }/*
    fun startResendTimer() {
        resendotp?.setEnabled(false)
        val timer = Timer()
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                timeoutSeconds--
                resendotp.setText("Resend OTP in $timeoutSeconds seconds")
                if (timeoutSeconds == 0L) {
                    timeoutSeconds = 60L
                    timer.cancel()
                    runOnUiThread { resendotp?.setEnabled(true) }
                }
            }
        }, 0, 1000)
    }*/
    private fun VerifyOTPApi(mobno: String,pin: String) {
        showLoading()
        AppsStrings.setApiString("LogIndeviceId")
        //val url = AppUrls.Login
        val url = "https://dukekart.in/api/verifyOtp"
        Log.v(AppsStrings.apiUrl, url)
        val json = JSONObject()
        val json_data = JSONObject()
        try {
            json_data.put("mobile",mobno)
            json_data.put("otp", pin)
            json_data.put("deviceId", "deviceId")
            json_data.put("fcmId", "fcmId")


            json.put(AppsStrings.BASEJSON, json_data)
            Log.v("TestApi", json_data.getString("deviceId")+"  --  "+ json_data.getString("fcmId"));
            // AppUtils.showToast(this@Login, json_data.getString("deviceId")+"  --  "+ json_data.getString("fcmId"))
        } catch (e: Exception) {
            AppUtils.showToast(this@Login, AppsStrings.defResponse)
            Log.v(AppsStrings.apiErrorException, e.message!!)
            e.printStackTrace()
        }
        AndroidNetworking.post(url)
            .addJSONObjectBody(json_data)
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    parseVerifyOTPApi(response)
                }

                override fun onError(error: ANError) {
                    hideLoading()
                    // handle error
                    if (error.errorCode != 0) {
                        AppUtils.showToast(this@Login, AppsStrings.defResponse)
                        Log.v(AppsStrings.apiErrorCode, "onError errorCode : " + error.errorCode)
                        Log.v(AppsStrings.apiErrorBody, "onError errorBody : " + error.errorBody)
                        Log.v(AppsStrings.apiErrorDetail, "onError errorDetail : " + error.errorDetail)
                    } else {
                        AppUtils.showToast(this@Login, AppsStrings.defResponse)
                        Log.v(AppsStrings.apiErrorException, error.message!!)
                    }
                }
            })
    }

    private fun parseVerifyOTPApi(response: JSONObject) {
        Log.d(AppsStrings.apiResponse, response.toString())
        try {

            val jsonObject = response.getJSONObject("ecommerce")
            hideLoading()
            AppUtils.showToast(this@Login, jsonObject.getString("res_msg"))
            if (jsonObject.getString("res_msg").equals("Login successfully done.", ignoreCase = true)) {

                prefs!![AppConstants.userName] = jsonObject.getString("userName")
                prefs!![AppConstants.userId] = jsonObject.getString("userId")
                prefs!![AppConstants.loginId] = jsonObject.getString("loginId")
                prefs!![AppConstants.userEmail] = jsonObject.getString("email")
                prefs!![AppConstants.userMobile] = jsonObject.getString("mobile")
                prefs!![AppConstants.userWhatsapNumber] = jsonObject.getString("whatsaap_number")
                prefs!![AppConstants.userAlternateNumber] = jsonObject.getString("alternate_number")
                prefs!![AppConstants.userAddress] = jsonObject.getString("address")
                prefs!![AppConstants.userLocality] = jsonObject.getString("locality")
                prefs!![AppConstants.userCity] = jsonObject.getString("city")
                prefs!![AppConstants.userState] = jsonObject.getString("state")
                prefs!![AppConstants.userPincode] = jsonObject.getString("pincode")
                prefs!![AppConstants.userWalletAmount] = jsonObject.getString("wallet_amount")
                prefs!![AppConstants.userProfPic] = jsonObject.getString("profile_pic")
                prefs!![AppConstants.loginCheck] = "true"
                prefs!!.commit()
                startActivity(Intent(this@Login, Dashboard::class.java))


            }else{

                prefs!![AppConstants.userMobile] = jsonObject.getString("mobile")
                prefs!![AppConstants.userotp] = jsonObject.getString("otp")

                prefs!!.commit()
                if(jsonObject.getString("otp")== pinview1?.text.toString()) {
                    startActivity(Intent(this@Login, Register::class.java))
                }else{
                    AppUtils.showToast(this@Login, "Please enter correct otp")
                }
                val userObject = jsonObject.getJSONObject("data")
              /*  JSONObject userObject = jsonObject!!.getJSONObject("data");*/
                prefs!!.set(AppConstants.minimumOrderBalance,userObject.getString("min_order_bal"));
                prefs!!.set(AppConstants.shippingCharge,userObject.getString("shipping _amount"));
                prefs!!.commit();
            }
        } catch (e: Exception) {

            Log.v(AppsStrings.apiErrorException, e.message!!)
            // AppUtils.showToast(this@Login, AppsStrings.defResponse)
        }
    }



}
