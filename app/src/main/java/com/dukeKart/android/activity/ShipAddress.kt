package com.dukeKart.android.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import org.json.JSONObject
import java.util.*

class ShipAddress : BaseActivity(), View.OnClickListener {
    var tv_title: TextView? = null
    var rlCart: RelativeLayout? = null
    var iv_search: ImageView? = null
    var addressList = ArrayList<HashMap<String, String>>()
    var pref: Preferences? = null
    var iv_back: ImageView? = null
    var rv_ShippingAddress: RecyclerView? = null
    var btnAddNew: Button? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ship_address)
        setPage()
    }

    private fun setPage() {
        setIDs()
        rv_ShippingAddress!!.setHasFixedSize(true)
        rv_ShippingAddress!!.setLayoutManager(LinearLayoutManager(this@ShipAddress))
        setListeners()
        setHeaders()
        addressListApi
    }

    private fun setListeners() {
        iv_back!!.setOnClickListener(this)
        btnAddNew!!.setOnClickListener(this)
    }

    private fun setHeaders() {
        tv_title!!.text = "Shipping Address"
        iv_search!!.visibility = View.INVISIBLE
        rlCart!!.visibility = View.INVISIBLE
    }

    fun setIDs() {
        pref = Preferences(this@ShipAddress)
        rv_ShippingAddress = findViewById(R.id.rv_ShippingAddress)
        btnAddNew = findViewById(R.id.btnAddNew)
        iv_search = findViewById(R.id.iv_search)
        iv_back = findViewById(R.id.iv_back)
        tv_title = findViewById(R.id.tv_title)
        rlCart = findViewById(R.id.rlCart)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.iv_back -> onBackPressed()
            R.id.btnAddNew -> {
                val intent = Intent(this@ShipAddress, AddAddress::class.java).putExtra(AppConstants.pagePath, "1")
                startActivity(intent)
            }
        }
    }

    override fun onBackPressed() {
        finish()
    }// handle error

    //            json_data.put("user_id", "2");
    private val addressListApi: Unit
        private get() {
            AppsStrings.setApiString("GetAddressList")
            val url = AppUrls.GetAddressList
            Log.v(AppsStrings.apiUrl, url)
            val json = JSONObject()
            val json_data = JSONObject()
            try {
                json_data.put("user_id", pref!![AppConstants.userId])
                //            json_data.put("user_id", "2");
                json.put(AppsStrings.BASEJSON, json_data)
                Log.v(AppsStrings.apiJson, json.toString())
            } catch (e: Exception) {
                AppUtils.showToast(this@ShipAddress, AppsStrings.defResponse)
                Log.v(AppsStrings.apiErrorException, e.message!!)
                e.printStackTrace()
            }
            AndroidNetworking.post(url)
                    .addJSONObjectBody(json_data)
                    .setPriority(Priority.HIGH)
                    .build()
                    .getAsJSONObject(object : JSONObjectRequestListener {
                        override fun onResponse(response: JSONObject) {
                            parseGetAddressListApi(response)
                        }

                        override fun onError(error: ANError) {
                            hideLoading()
                            // handle error
                            if (error.errorCode != 0) {
                                AppUtils.showToast(this@ShipAddress, AppsStrings.defResponse)
                                Log.v(AppsStrings.apiErrorCode, "onError errorCode : " + error.errorCode)
                                Log.v(AppsStrings.apiErrorBody, "onError errorBody : " + error.errorBody)
                                Log.v(AppsStrings.apiErrorDetail, "onError errorDetail : " + error.errorDetail)
                            } else {
                                AppUtils.showToast(this@ShipAddress, AppsStrings.defResponse)
                                Log.v(AppsStrings.apiErrorException, error.message!!)
                            }
                        }
                    })
        }

    private fun parseGetAddressListApi(response: JSONObject) {
        hideLoading()
        Log.d(AppsStrings.apiResponse, response.toString())
        try {
            val jsonObject = response.getJSONObject("ecommerce")
            addressList.clear()
            if (jsonObject.getString("res_code").equals("1", ignoreCase = true)) {
                val getAddressList = jsonObject.getJSONArray("AddressList")
                for (i in 0 until getAddressList.length()) {
                    val hashProduct = HashMap<String, String>()
                    hashProduct["address_id"] = getAddressList.getJSONObject(i).getString("address_id")
                    hashProduct["user_master_id"] = getAddressList.getJSONObject(i).getString("user_master_id")
                    hashProduct["title"] = getAddressList.getJSONObject(i).getString("title")
                    hashProduct["contact_person"] = getAddressList.getJSONObject(i).getString("contact_person")
                    hashProduct["mobile_number"] = getAddressList.getJSONObject(i).getString("mobile_number")
                    hashProduct["alternate_number"] = getAddressList.getJSONObject(i).getString("alternate_number")
                    hashProduct["address"] = getAddressList.getJSONObject(i).getString("address")
                    hashProduct["localty"] = getAddressList.getJSONObject(i).getString("localty")
                    hashProduct["landmark"] = getAddressList.getJSONObject(i).getString("landmark")
                    hashProduct["pincode"] = getAddressList.getJSONObject(i).getString("pincode")
                    hashProduct["state"] = getAddressList.getJSONObject(i).getString("state")
                    hashProduct["city"] = getAddressList.getJSONObject(i).getString("city")
                    addressList.add(hashProduct)
                }
            } else {
                AppUtils.showToast(this@ShipAddress, jsonObject.getString("res_msg"))
            }
            val shippingAdapter: ShippingAdapter = ShippingAdapter(addressList)
            rv_ShippingAddress!!.adapter = shippingAdapter
        } catch (e: Exception) {
            Log.v(AppsStrings.apiErrorException, e.message!!)
            AppUtils.showToast(this@ShipAddress, AppsStrings.defResponse)
        }
    }

    public override fun onResume() {
        setPage()

        super.onResume()
    }

    fun confirmDeleteDialog(addressId: String?) {
        val builder = AlertDialog.Builder(this@ShipAddress)
        val inflater = LayoutInflater.from(this@ShipAddress)
        val dialogView = inflater.inflate(R.layout.confirm_popup, null)
        builder.setView(dialogView)
        val tv_Header = dialogView.findViewById<TextView>(R.id.tv_Header)
        tv_Header.text = "Confirm Delete"
        val tv_Message = dialogView.findViewById<TextView>(R.id.tv_Message)
        tv_Message.text = "Are you sure you want to delete?"
        val rrSubmit = dialogView.findViewById<RelativeLayout>(R.id.rr_submit)
        val rr_lg_cancel = dialogView.findViewById<RelativeLayout>(R.id.rr_lg_cancel)
        val alert = builder.create()
        rrSubmit.setOnClickListener {
            removeAddressApi(addressId)
            alert.dismiss()
        }
        val rrCancel = dialogView.findViewById<RelativeLayout>(R.id.rr_cancel)
        rr_lg_cancel.setOnClickListener { alert.dismiss() }
        rrCancel.setOnClickListener { alert.dismiss() }
        alert.show()
    }

    private fun removeAddressApi(address_id: String?) {
        showLoading()
        AppsStrings.setApiString("DeleteAddress")
        val url = AppUrls.DeleteAddress
        Log.v(AppsStrings.apiUrl, url)
        val json = JSONObject()
        val json_data = JSONObject()
        try {
//            json_data.put("user_id", prefs.get(AppConstants.userId));
            json_data.put("address_id", address_id)
            json.put(AppsStrings.BASEJSON, json_data)
            Log.v(AppsStrings.apiJson, json.toString())
        } catch (e: Exception) {
            AppUtils.showToast(this@ShipAddress, AppsStrings.defResponse)
            Log.v(AppsStrings.apiErrorException, e.message!!)
            e.printStackTrace()
        }
        AndroidNetworking.post(url)
                .addJSONObjectBody(json_data)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        parseRemoveAddressApi(response)
                    }

                    override fun onError(error: ANError) {
                        hideLoading()
                        // handle error
                        if (error.errorCode != 0) {
                            AppUtils.showToast(this@ShipAddress, AppsStrings.defResponse)
                            Log.v(AppsStrings.apiErrorCode, "onError errorCode : " + error.errorCode)
                            Log.v(AppsStrings.apiErrorBody, "onError errorBody : " + error.errorBody)
                            Log.v(AppsStrings.apiErrorDetail, "onError errorDetail : " + error.errorDetail)
                        } else {
                            AppUtils.showToast(this@ShipAddress, AppsStrings.defResponse)
                            Log.v(AppsStrings.apiErrorException, error.message!!)
                        }
                    }
                })
    }

    private fun parseRemoveAddressApi(response: JSONObject) {
        Log.d(AppsStrings.apiResponse, response.toString())
        try {
            val jsonObject = response.getJSONObject("ecommerce")
            if (jsonObject.getString("res_code").equals("1", ignoreCase = true)) addressListApi else {
                hideLoading()
                AppUtils.showToast(this@ShipAddress, jsonObject.getString("res_msg"))
            }
        } catch (e: Exception) {
            hideLoading()
            Log.v(AppsStrings.apiErrorException, e.message!!)
            AppUtils.showToast(this@ShipAddress, AppsStrings.defResponse)
        }
    }

    private inner class ShippingAdapter(var arrayList1: ArrayList<HashMap<String, String>>) : RecyclerView.Adapter<ShippingAdapter.MyViewHolder>() {
        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): MyViewHolder {
            val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.adapter_shipping, viewGroup, false)
            return MyViewHolder(view)
        }

        override fun onBindViewHolder(myViewHolder: MyViewHolder, i: Int) {
            try {
                myViewHolder.tv_shippingtitle.text = arrayList1[i]["title"]
                myViewHolder.tv_contactPerson.text = arrayList1[i]["contact_person"]
                myViewHolder.tv_contactNumber.text = arrayList1[i]["mobile_number"]
                myViewHolder.tv_altNumber.text = arrayList1[i]["alternate_number"]
                myViewHolder.tv_shippingAddress.text = arrayList1[i]["address"].toString() + "," + arrayList1[i]["localty"] + "," + arrayList1[i]["city"] + "," + arrayList1[i]["state"] + "-" + arrayList1[i]["pincode"]
                myViewHolder.btn_update.setOnClickListener {
                    val intent = Intent(this@ShipAddress, AddAddress::class.java).putExtra(AppConstants.pagePath, "1")
                    intent.putExtra(AppConstants.addressArray, arrayList1[i])
                    startActivity(intent)
                }
                myViewHolder.btn_delete.setOnClickListener { confirmDeleteDialog(arrayList1[i]["address_id"]) }
                myViewHolder.btn_chooseAddress.setOnClickListener {
                    pref!![AppConstants.primaryAddressId] = arrayList1[i]["address_id"]
                    pref!![AppConstants.primaryAddressTitle] = arrayList1[i]["title"]
                    pref!![AppConstants.primaryAddress] = arrayList1[i]["address"].toString() + "," + arrayList1[i]["localty"] + "," + arrayList1[i]["city"] + "," + arrayList1[i]["state"] + "-" + arrayList1[i]["pincode"]
                    pref!!.commit()
                    startActivity(Intent(this@ShipAddress, CheckOut::class.java))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                AppUtils.showToast(this@ShipAddress, AppsStrings.defResponse)
            }
        }

        override fun getItemCount(): Int {
            return arrayList1.size
        }

        inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var btn_update: Button
            var btn_delete: Button
            var btn_chooseAddress: Button
            var tv_shippingtitle: TextView
            var tv_contactPerson: TextView
            var tv_contactNumber: TextView
            var tv_altNumber: TextView
            var tv_shippingAddress: TextView
            var ll_AddFooter: LinearLayout

            init {
                tv_shippingtitle = itemView.findViewById(R.id.tv_shippingtitle)
                tv_contactPerson = itemView.findViewById(R.id.tv_contactPerson)
                tv_contactNumber = itemView.findViewById(R.id.tv_contactNumber)
                tv_altNumber = itemView.findViewById(R.id.tv_altNumber)
                tv_shippingAddress = itemView.findViewById(R.id.tv_shippingAddress)
                btn_update = itemView.findViewById(R.id.btn_update)
                btn_delete = itemView.findViewById(R.id.btn_delete)
                btn_chooseAddress = itemView.findViewById(R.id.btn_chooseAddress)
                ll_AddFooter = itemView.findViewById(R.id.ll_AddFooter)
                ll_AddFooter.weightSum = 3f
                btn_chooseAddress.visibility = View.VISIBLE
            }
        }
    }



}