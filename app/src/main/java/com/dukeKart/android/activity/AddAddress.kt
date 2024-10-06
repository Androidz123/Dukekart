package com.dukeKart.android.activity

import android.accounts.Account
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
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
import com.dukeKart.android.activity.ShipAddress
import org.json.JSONObject
import java.util.*

class AddAddress : BaseActivity(), View.OnClickListener {
    var et_addressTitle: EditText? = null
    var et_contactPerson: EditText? = null
    var et_mobileNumber: EditText? = null
    var et_altMobileNumber: EditText? = null
    var et_address: EditText? = null
    var et_locality: EditText? = null
    var et_city: EditText? = null
    var et_state: EditText? = null
    var et_pincode: EditText? = null
    var et_landmark: EditText? = null
    var prefs: Preferences? = null
    var btn_Submit: Button? = null
    var address_id: String? = ""
    var tv_title: TextView? = null
    var iv_search: ImageView? = null
    var iv_back: ImageView? = null
    var rlCart: RelativeLayout? = null
    var pagePath: String? = "3" //0= Accounts, 1= ShipAddress
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_address)
        setIds()
        setListeners()
        fillData()
        setHeaders()
    }

    private fun setHeaders() {
        tv_title!!.text = "Add Address"
        iv_search!!.visibility = View.INVISIBLE
        rlCart!!.visibility = View.INVISIBLE
    }

    private fun setIds() {
        prefs = Preferences(this@AddAddress)
        tv_title = findViewById(R.id.tv_title)
        iv_back = findViewById(R.id.iv_back)
        iv_search = findViewById(R.id.iv_search)
        rlCart = findViewById(R.id.rlCart)
        et_addressTitle = findViewById(R.id.et_addressTitle)
        et_contactPerson = findViewById(R.id.et_contactPerson)
        et_mobileNumber = findViewById(R.id.et_mobileNumber)
        et_altMobileNumber = findViewById(R.id.et_altMobileNumber)
        et_address = findViewById(R.id.et_address)
        et_locality = findViewById(R.id.et_locality)
        et_city = findViewById(R.id.et_city)
        et_state = findViewById(R.id.et_state)
        et_pincode = findViewById(R.id.et_pincode)
        et_landmark = findViewById(R.id.et_landmark)
        btn_Submit = findViewById(R.id.btn_Submit)
    }

    private fun setListeners() {
        btn_Submit!!.setOnClickListener(this)
        iv_back!!.setOnClickListener(this)
    }

    private fun fillData() {
        try {
            val intent = intent
            pagePath = intent.getStringExtra(AppConstants.pagePath)
            val hashMap = intent.getSerializableExtra(AppConstants.addressArray)
                    as HashMap<String, String>?
            et_addressTitle!!.setText(hashMap!!["title"])
            et_contactPerson!!.setText(hashMap["contact_person"])
            et_mobileNumber!!.setText(hashMap["mobile_number"])
            et_altMobileNumber!!.setText(hashMap["alternate_number"])
            et_address!!.setText(hashMap["address"])
            et_locality!!.setText(hashMap["localty"])
            et_landmark!!.setText(hashMap["landmark"])
            et_pincode!!.setText(hashMap["pincode"])
            et_state!!.setText(hashMap["state"])
            et_city!!.setText(hashMap["city"])
            address_id = if (hashMap.containsKey("address_id")) {
                hashMap["address_id"]
            } else {
                ""
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun validate(): Boolean {
        if (AppUtils.getTextInputEditTextData(et_addressTitle).isEmpty()) {
            AppUtils.showToast(this@AddAddress, "Title cannot be blank")
        } else if (AppUtils.getTextInputEditTextData(et_contactPerson).isEmpty()) {
            AppUtils.showToast(this@AddAddress, "Contact person cannot be blank")
        } else if (!AppUtils.isNumberValid(AppUtils.getTextInputEditTextData(et_mobileNumber))) {
            AppUtils.showToast(this@AddAddress, "Enter a valid mobile number")
        } else if (!AppUtils.isNumberValid(AppUtils.getTextInputEditTextData(et_altMobileNumber))) {
            AppUtils.showToast(this@AddAddress, "Enter a valid alternate mobile number")
        } else if (AppUtils.getTextInputEditTextData(et_address).isEmpty()) {
            AppUtils.showToast(this@AddAddress, "Address cannot be blank")
        } else if (AppUtils.getTextInputEditTextData(et_locality).isEmpty()) {
            AppUtils.showToast(this@AddAddress, "Locality cannot be blank")
        } else if (AppUtils.getTextInputEditTextData(et_city).isEmpty()) {
            AppUtils.showToast(this@AddAddress, "City cannot be blank")
        } else if (AppUtils.getTextInputEditTextData(et_state).isEmpty()) {
            AppUtils.showToast(this@AddAddress, "State cannot be blank")
        } else if (AppUtils.getTextInputEditTextData(et_pincode).length < 6) {
            AppUtils.showToast(this@AddAddress, "Invalid Pincode")
        } else if (AppUtils.getTextInputEditTextData(et_landmark).isEmpty()) {
            AppUtils.showToast(this@AddAddress, "Landmark cannot be blank")
        } else {
            return true
        }
        return false
    }

    private fun addUpdateApi() {
        val json = JSONObject()
        val json_data = JSONObject()
        var url = ""
        try {
            showLoading()
            AppsStrings.setApiString("addUpdateAddress")
            if (address_id.equals("", ignoreCase = true)) {
                url = AppUrls.AddAddress
                json_data.put("user_id", prefs!![AppConstants.userId])
            } else {
                url = AppUrls.UpdateAddress
                json_data.put("address_id", address_id)
            }
            Log.v(AppsStrings.apiUrl, url)
            json_data.put("title", AppUtils.getTextInputEditTextData(et_addressTitle))
            json_data.put("contact_person", AppUtils.getTextInputEditTextData(et_contactPerson))
            json_data.put("mobile_number", AppUtils.getTextInputEditTextData(et_mobileNumber))
            json_data.put("alternate_number", AppUtils.getTextInputEditTextData(et_altMobileNumber))
            json_data.put("address", AppUtils.getTextInputEditTextData(et_address))
            json_data.put("localty", AppUtils.getTextInputEditTextData(et_locality))
            json_data.put("landmark", AppUtils.getTextInputEditTextData(et_landmark))
            json_data.put("pincode", AppUtils.getTextInputEditTextData(et_pincode))
            json_data.put("state", AppUtils.getTextInputEditTextData(et_state))
            json_data.put("city", AppUtils.getTextInputEditTextData(et_city))
            json.put(AppConstants.appName, json_data)
            Log.v(AppsStrings.apiJson, json.toString())
        } catch (e: Exception) {
            //AppUtils.showToast(this@AddAddress, AppsStrings.defResponse)
            Log.v(AppsStrings.apiErrorException, e.message!!)
            e.printStackTrace()
        }
        AndroidNetworking.post(url)
                .addJSONObjectBody(json_data)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        parseAddUpdateApi(response)
                    }

                    override fun onError(error: ANError) {
                        hideLoading()
                        // handle error
                        if (error.errorCode != 0) {
                           // AppUtils.showToast(this@AddAddress, AppsStrings.defResponse)
                            Log.v(AppsStrings.apiErrorCode, "onError errorCode : " + error.errorCode)
                            Log.v(AppsStrings.apiErrorBody, "onError errorBody : " + error.errorBody)
                            Log.v(AppsStrings.apiErrorDetail, "onError errorDetail : " + error.errorDetail)
                        } else {
                           // AppUtils.showToast(this@AddAddress, AppsStrings.defResponse)
                           Log.v(AppsStrings.apiErrorException, error.message!!)
                        }
                    }
                })
    }

    private fun parseAddUpdateApi(response: JSONObject) {
        Log.d(AppsStrings.apiResponse, response.toString())
        try {
            val jsonObject = response.getJSONObject("ecommerce")
            hideLoading()
            AppUtils.showToast(this@AddAddress, jsonObject.getString("res_msg"))
            if (jsonObject.getString("res_code").equals("1", ignoreCase = true)) {
                if (pagePath.equals("0", ignoreCase = true)) {
                    val intent = Intent(this@AddAddress, Account::class.java)
                    intent.putExtra(AppConstants.pagePath, "1")
                    startActivity(intent)
                } else {
                    val intent = Intent(this@AddAddress, ShipAddress::class.java)
                    startActivity(intent)
                }
                //                startActivity(new Intent(this@AddAddress, Login.class));
                /*  JSONObject userObject = jsonObject.getJSONObject("data");
                prefs.set(AppConstants.minimumOrderBalance,userObject.getString("min_order_bal"));
                prefs.set(AppConstants.shippingCharge,userObject.getString("shipping _amount"));
                prefs.commit();*/
            }
        } catch (e: Exception) {
            Log.v(AppsStrings.apiErrorException, e.message!!)
          //  AppUtils.showToast(this@AddAddress, AppsStrings.defResponse)
        }
        //        finish();
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btn_Submit -> if (validate()) {
                addUpdateApi()
            }
            R.id.iv_back -> onBackPressed()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()

        val intent = Intent(this@AddAddress, ShipAddress::class.java)

        startActivity(intent)
        finish()

    }
}