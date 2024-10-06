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

class OrderList : BaseActivity(), View.OnClickListener {
    var tv_title: TextView? = null
    var iv_search: ImageView? = null
    var iv_back: ImageView? = null
    var rl_orderList: RelativeLayout? = null
    var rlCart: RelativeLayout? = null
    var prefs: Preferences? = null
    var rv_OrderList: RecyclerView? = null
    var orderList = ArrayList<HashMap<String, String>>()
    var reasonList = ArrayList<HashMap<String, String>>()
    var reasonId: String? = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_list)
        setIds()
        setListeners()
        setHeaders()
        showLoading()
        OrderListApi()
    }

    private fun setIds() {
        prefs = Preferences(this@OrderList)
        tv_title = findViewById(R.id.tv_title)
        rl_orderList = findViewById(R.id.rl_orderList)
        iv_back = findViewById(R.id.iv_back)
        iv_search = findViewById(R.id.iv_search)
        rv_OrderList = findViewById(R.id.rv_OrderList)
        rlCart = findViewById(R.id.rlCart)
    }

    private fun setHeaders() {
        tv_title!!.text = "Order List"
        iv_search!!.visibility = View.GONE
        rlCart!!.visibility = View.GONE
    }

    private fun setListeners() {
        iv_back!!.setOnClickListener(this)
    }

    private fun OrderListApi() {
        AppsStrings.setApiString("OrderList")
        val url = AppUrls.OrderList
        Log.v(AppsStrings.apiUrl, url)
        val json = JSONObject()
        val json_data = JSONObject()
        try {
            json_data.put("user_id", prefs!![AppConstants.userId])
            json.put(AppsStrings.BASEJSON, json_data)
            Log.v(AppsStrings.apiJson, json.toString())
        } catch (e: Exception) {
            hideLoading()
            AppUtils.showToast(this@OrderList, AppsStrings.defResponse)
            Log.v(AppsStrings.apiErrorException, e.message!!)
            e.printStackTrace()
        }
        AndroidNetworking.post(url)
                .addJSONObjectBody(json_data)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        parseOrderListApi(response)
                    }

                    override fun onError(error: ANError) {
                        rl_orderList!!.visibility = View.INVISIBLE
                        hideLoading()
                        // handle error
                        if (error.errorCode != 0) {
                            AppUtils.showToast(this@OrderList, AppsStrings.defResponse)
                            Log.v(AppsStrings.apiErrorCode, "onError errorCode : " + error.errorCode)
                            Log.v(AppsStrings.apiErrorBody, "onError errorBody : " + error.errorBody)
                            Log.v(AppsStrings.apiErrorDetail, "onError errorDetail : " + error.errorDetail)
                        } else {
                            AppUtils.showToast(this@OrderList, AppsStrings.defResponse)
                            Log.v(AppsStrings.apiErrorException, error.message!!)
                        }
                    }
                })
    }

    private fun parseOrderListApi(response: JSONObject) {
        Log.d(AppsStrings.apiResponse, response.toString())
        try {
            val jsonObject = response.getJSONObject("ecommerce")
            orderList.clear()
            if (jsonObject.getString("res_code").equals("1", ignoreCase = true)) {
                rl_orderList!!.visibility = View.VISIBLE
                val jsCartList = jsonObject.getJSONArray("OrderList")
                for (i in 0 until jsCartList.length()) {
                    val hashOrderList = HashMap<String, String>()
                    hashOrderList["id"] = jsCartList.getJSONObject(i).getString("id")
                    hashOrderList["order_id"] = jsCartList.getJSONObject(i).getString("order_id")
                    hashOrderList["total_price"] = jsCartList.getJSONObject(i).getString("total_price")
                    hashOrderList["status"] = jsCartList.getJSONObject(i).getString("status")
                    hashOrderList["add_date"] = jsCartList.getJSONObject(i).getString("add_date")
                    hashOrderList["modify_date"] = jsCartList.getJSONObject(i).getString("modify_date")
                    orderList.add(hashOrderList)
                }
                rv_OrderList!!.setHasFixedSize(true)
                rv_OrderList!!.layoutManager = LinearLayoutManager(this@OrderList)
                rv_OrderList!!.isNestedScrollingEnabled = false
                val orderListAdapter = OrderListAdapter(orderList)
                rv_OrderList!!.adapter = orderListAdapter
                reasonListApi()
            } else {
                hideLoading()
                rl_orderList!!.visibility = View.INVISIBLE
                AppUtils.showToast(this@OrderList, jsonObject.getString("res_msg"))
            }
        } catch (e: Exception) {
            hideLoading()
            rl_orderList!!.visibility = View.INVISIBLE
            Log.v(AppsStrings.apiErrorException, e.message!!)
            AppUtils.showToast(this@OrderList, AppsStrings.defResponse)
        }
    }

    private fun reasonListApi() {
        AppsStrings.setApiString("ReasonList")
        val url = AppUrls.GetAllReason
        Log.v(AppsStrings.apiUrl, url)
        AndroidNetworking.get(url)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        parseReasonListApi(response)
                    }

                    override fun onError(error: ANError) {
                        hideLoading()
                        // handle error
                        if (error.errorCode != 0) {
                            AppUtils.showToast(this@OrderList, AppsStrings.defResponse)
                            Log.v(AppsStrings.apiErrorCode, "onError errorCode : " + error.errorCode)
                            Log.v(AppsStrings.apiErrorBody, "onError errorBody : " + error.errorBody)
                            Log.v(AppsStrings.apiErrorDetail, "onError errorDetail : " + error.errorDetail)
                        } else {
                            AppUtils.showToast(this@OrderList, AppsStrings.defResponse)
                            Log.v(AppsStrings.apiErrorException, error.message!!)
                        }
                    }
                })
    }

    private fun parseReasonListApi(response: JSONObject) {
        Log.d(AppsStrings.apiResponse, response.toString())
        try {
            val jsonObject = response.getJSONObject("ecommerce")
            hideLoading()
            reasonList.clear()
            if (jsonObject.getString("res_code").equals("1", ignoreCase = true)) {
                val jsCartList = jsonObject.getJSONArray("reasonList")
                for (i in 0 until jsCartList.length()) {
                    val hashOrderList = HashMap<String, String>()
                    hashOrderList["id"] = jsCartList.getJSONObject(i).getString("id")
                    hashOrderList["reason"] = jsCartList.getJSONObject(i).getString("reason")
                    hashOrderList["status"] = "false"
                    reasonList.add(hashOrderList)
                }
            } else {
                rl_orderList!!.visibility = View.INVISIBLE
                AppUtils.showToast(this@OrderList, jsonObject.getString("res_msg"))
            }
        } catch (e: Exception) {
            rl_orderList!!.visibility = View.INVISIBLE
            Log.v(AppsStrings.apiErrorException, e.message!!)
            AppUtils.showToast(this@OrderList, AppsStrings.defResponse)
        }
    }

    private fun cancelReturnApi(url: String, orderId: String?) {
        showLoading()
        AppsStrings.setApiString("cancelReturn")
        Log.v(AppsStrings.apiUrl, url)
        val json = JSONObject()
        val json_data = JSONObject()
        try {
            json_data.put("orderId", orderId)
            json_data.put("reasonId", reasonId)
            json_data.put("remark", prefs!![AppConstants.userId])
            json.put(AppsStrings.BASEJSON, json_data)
            Log.v(AppsStrings.apiJson, json.toString())
        } catch (e: Exception) {
            AppUtils.showToast(this@OrderList, AppsStrings.defResponse)
            Log.v(AppsStrings.apiErrorException, e.message!!)
            e.printStackTrace()
        }
        AndroidNetworking.post(url)
                .addJSONObjectBody(json_data)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        parseCancelReturnApi(response)
                    }

                    override fun onError(error: ANError) {
                        rl_orderList!!.visibility = View.INVISIBLE
                        hideLoading()
                        // handle error
                        if (error.errorCode != 0) {
                            AppUtils.showToast(this@OrderList, AppsStrings.defResponse)
                            Log.v(AppsStrings.apiErrorCode, "onError errorCode : " + error.errorCode)
                            Log.v(AppsStrings.apiErrorBody, "onError errorBody : " + error.errorBody)
                            Log.v(AppsStrings.apiErrorDetail, "onError errorDetail : " + error.errorDetail)
                        } else {
                            AppUtils.showToast(this@OrderList, AppsStrings.defResponse)
                            Log.v(AppsStrings.apiErrorException, error.message!!)
                        }
                    }
                })
    }

    private fun parseCancelReturnApi(response: JSONObject) {
        Log.d(AppsStrings.apiResponse, response.toString())
        try {
            val jsonObject = response.getJSONObject("ecommerce")
            hideLoading()
            AppUtils.showToast(this@OrderList, jsonObject.getString("res_msg"))
            if (jsonObject.getString("res_code").equals("1", ignoreCase = true)) {
                OrderListApi()
            } /*else {
                rl_orderList!!.visibility = View.INVISIBLE
            }*/
        } catch (e: Exception) {
            rl_orderList!!.visibility = View.INVISIBLE
            Log.v(AppsStrings.apiErrorException, e.message!!)
            AppUtils.showToast(this@OrderList, AppsStrings.defResponse)
        }
    }

    fun confirmDeleteDialog(url: String, orderId: String?) {
        reasonId = ""
        val builder = AlertDialog.Builder(this@OrderList)
        val inflater = LayoutInflater.from(this@OrderList)
        val dialogView = inflater.inflate(R.layout.reason_popup, null)
        builder.setView(dialogView)
        val rrSubmit = dialogView.findViewById<RelativeLayout>(R.id.rr_submit)
        val rr_lg_cancel = dialogView.findViewById<RelativeLayout>(R.id.rr_lg_cancel)
        val rv_Reasons: RecyclerView = dialogView.findViewById(R.id.rv_Reasons)
        rv_Reasons.setHasFixedSize(true)
        rv_Reasons.layoutManager = LinearLayoutManager(this@OrderList)
        rv_Reasons.isNestedScrollingEnabled = false
        val reasonListAdapter: ReasonListAdapter = ReasonListAdapter(reasonList)
        rv_Reasons.adapter = reasonListAdapter
        val alert = builder.create()
        rrSubmit.setOnClickListener {
            alert.dismiss()
            if (reasonId.equals("", ignoreCase = true)) {
                AppUtils.showToast(this@OrderList, "Select a reason")
            } else {
                cancelReturnApi(url, orderId)

            }
        }
        val rrCancel = dialogView.findViewById<RelativeLayout>(R.id.rr_cancel)
        rr_lg_cancel.setOnClickListener { alert.dismiss() }
        rrCancel.setOnClickListener { alert.dismiss() }
        alert.show()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.iv_back -> onBackPressed()
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this@OrderList, Dashboard::class.java))
    }

    private inner class OrderListAdapter(var arrayList1: ArrayList<HashMap<String, String>>) : RecyclerView.Adapter<OrderListAdapter.MyViewHolder>() {
        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): MyViewHolder {
            val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.adapter_orderlist, viewGroup, false)
            return MyViewHolder(view)
        }

        override fun onBindViewHolder(myViewHolder: MyViewHolder, i: Int) {
            var canReturn = "false"
            myViewHolder.tv_orderId.text = arrayList1[i]["order_id"]
            myViewHolder.tv_orderPlacedDate.text = arrayList1[i]["add_date"]
            myViewHolder.tv_orderPrice.text = AppUtils.setPriceTotal(arrayList1[i]["total_price"])
            myViewHolder.btn_cancelOrder.visibility = View.GONE
            myViewHolder.btn_returnOrder.visibility = View.GONE
            when (arrayList1[i]["status"]) {
                "1" -> {
                    myViewHolder.tv_status.text = "Order waiting for seller's approval"
                    myViewHolder.tv_status.setTextColor(resources.getColor(R.color.orange))
                    myViewHolder.iv_status.setImageResource(R.drawable.ic_new)
                    myViewHolder.ll_delivered.visibility = View.GONE
                    myViewHolder.ll_returned.visibility = View.GONE
                    myViewHolder.btn_cancelOrder.visibility = View.VISIBLE
                }
                "2" -> {
                    myViewHolder.tv_status.text = "Order Shipped"
                    myViewHolder.tv_status.setTextColor(resources.getColor(R.color.orange))
                    myViewHolder.iv_status.setImageResource(R.drawable.ic_process)
                    myViewHolder.ll_delivered.visibility = View.GONE
                    myViewHolder.ll_returned.visibility = View.GONE
                    myViewHolder.btn_cancelOrder.visibility = View.VISIBLE
                }
                "3" -> {
                    myViewHolder.tv_status.text = "Order Delivered"
                    myViewHolder.tv_status.setTextColor(resources.getColor(R.color.green))
                    myViewHolder.iv_status.setImageResource(R.drawable.ic_delivered)
                    myViewHolder.ll_delivered.visibility = View.VISIBLE
                    myViewHolder.ll_returned.visibility = View.VISIBLE
                    myViewHolder.tv_deliveryDate.text = AppUtils.getDateTimeFromTimestamp(arrayList1[i]["modify_date"]!!.toLong())
                    if (AppUtils.getDays(AppUtils.getDate(arrayList1[i]["modify_date"]!!.toLong())) <= 10) {
                        myViewHolder.btn_returnOrder.visibility = View.VISIBLE
                        myViewHolder.tv_returnedOn.text = "Return available till: "
                        myViewHolder.tv_returnDate.text = AppUtils.addDays(AppUtils.getDate(arrayList1[i]["modify_date"]!!.toLong()), 10)
                        canReturn = "true"
                    }
                }
                "4" -> {
                    myViewHolder.tv_status.text = "Order cancelled by customer"
                    myViewHolder.tv_status.setTextColor(resources.getColor(R.color.colorRed))
                    myViewHolder.iv_status.setImageResource(R.drawable.ic_cancelled)
                    myViewHolder.ll_delivered.visibility = View.GONE
                    myViewHolder.ll_returned.visibility = View.GONE
                }
                "5" -> {
                    myViewHolder.tv_status.text = "Order confirmed by seller"
                    myViewHolder.tv_status.setTextColor(resources.getColor(R.color.orange))
                    myViewHolder.iv_status.setImageResource(R.drawable.ic_transit)
                    myViewHolder.ll_delivered.visibility = View.GONE
                    myViewHolder.ll_returned.visibility = View.GONE
                    myViewHolder.btn_cancelOrder.visibility = View.VISIBLE
                }
                "6" -> {
                    myViewHolder.tv_status.text = "Order Rejected by seller"
                    myViewHolder.tv_status.setTextColor(resources.getColor(R.color.colorRed))
                    myViewHolder.iv_status.setImageResource(R.drawable.ic_cancelled)
                    myViewHolder.ll_delivered.visibility = View.GONE
                    myViewHolder.ll_returned.visibility = View.GONE
                }
                "7" -> {
                    myViewHolder.tv_status.text = "Return Requested"
                    myViewHolder.tv_status.setTextColor(resources.getColor(R.color.orange))
                    myViewHolder.iv_status.setImageResource(R.drawable.ic_transit)
                    myViewHolder.ll_delivered.visibility = View.VISIBLE
                    myViewHolder.tv_deliveryDate.text = AppUtils.getDateTimeFromTimestamp(arrayList1[i]["modify_date"]!!.toLong())
                    myViewHolder.ll_returned.visibility = View.GONE
                    myViewHolder.ll_delivered.visibility = View.VISIBLE
                    myViewHolder.ll_returned.visibility = View.VISIBLE
                    myViewHolder.tv_deliveryDate.text = AppUtils.getDateTimeFromTimestamp(arrayList1[i]["modify_date"]!!.toLong())
                }
                "8" -> {
                    myViewHolder.tv_status.text = "Return Completed"
                    myViewHolder.tv_status.setTextColor(resources.getColor(R.color.green))
                    myViewHolder.iv_status.setImageResource(R.drawable.ic_delivered)
                    myViewHolder.ll_delivered.visibility = View.GONE
                    myViewHolder.ll_returned.visibility = View.VISIBLE
                    myViewHolder.tv_returnDate.text = AppUtils.getDateTimeFromTimestamp(arrayList1[i]["modify_date"]!!.toLong())
                }
            }
            val finalCanReturn = canReturn
            myViewHolder.rl_orderList.setOnClickListener {
                startActivity(Intent(this@OrderList, OrderDetails::class.java)
                    .putExtra(AppConstants.orderId, arrayList1[i]["order_id"])
                    .putExtra(AppConstants.canReturn, finalCanReturn))
            }
            myViewHolder.btn_cancelOrder.setOnClickListener { confirmDeleteDialog(AppUrls.CancelOrder, arrayList1[i]["order_id"]) }
            myViewHolder.btn_returnOrder.setOnClickListener { confirmDeleteDialog(AppUrls.ReturnFullyOrder, arrayList1[i]["order_id"]) }
        }

        override fun getItemCount(): Int {
            return arrayList1.size
        }

        inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var tv_orderId: TextView
            var tv_orderPlacedDate: TextView
            var tv_orderPrice: TextView
            var tv_status: TextView
            var tv_deliveryDate: TextView
            var tv_returnDate: TextView
            var tv_returnedOn: TextView
            var iv_status: ImageView
            var rl_orderList: RelativeLayout
            var ll_delivered: LinearLayout
            var ll_returned: LinearLayout
            var btn_cancelOrder: Button
            var btn_returnOrder: Button

            init {
                rl_orderList = itemView.findViewById(R.id.rl_orderList)
                tv_orderId = itemView.findViewById(R.id.tv_orderId)
                tv_orderPlacedDate = itemView.findViewById(R.id.tv_orderPlacedDate)
                tv_orderPrice = itemView.findViewById(R.id.tv_orderPrice)
                iv_status = itemView.findViewById(R.id.iv_status)
                tv_status = itemView.findViewById(R.id.tv_status)
                ll_delivered = itemView.findViewById(R.id.ll_delivered)
                ll_returned = itemView.findViewById(R.id.ll_returned)
                tv_deliveryDate = itemView.findViewById(R.id.tv_deliveryDate)
                tv_returnDate = itemView.findViewById(R.id.tv_returnDate)
                btn_cancelOrder = itemView.findViewById(R.id.btn_cancelOrder)
                btn_returnOrder = itemView.findViewById(R.id.btn_returnOrder)
                tv_returnedOn = itemView.findViewById(R.id.tv_returnedOn)
            }
        }
    }

    private inner class ReasonListAdapter(var arrayList1: ArrayList<HashMap<String, String>>) : RecyclerView.Adapter<ReasonListAdapter.MyViewHolder>() {
        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): MyViewHolder {
            val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.adapter_reasons, viewGroup, false)
            return MyViewHolder(view)
        }

        override fun onBindViewHolder(myViewHolder: MyViewHolder, i: Int) {
            myViewHolder.tv_reason.text = arrayList1[i]["reason"]
            if (arrayList1[i]["status"].equals("true", ignoreCase = true)) {
                myViewHolder.iv_reason.visibility = View.VISIBLE
            } else {
                myViewHolder.iv_reason.visibility = View.INVISIBLE
            }
            myViewHolder.rl_reasons.setOnClickListener {
                for (j in arrayList1.indices) {
                    arrayList1[j].replace("status", "true", "false")
                }
                arrayList1[i].replace("status", "false", "true")
                reasonId = arrayList1[i]["id"]
                notifyDataSetChanged()
            }
        }

        override fun getItemCount(): Int {
            return arrayList1.size
        }

        inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var tv_reason: TextView
            var iv_reason: ImageView
            var rl_reasons: RelativeLayout

            init {
                rl_reasons = itemView.findViewById(R.id.rl_reasons)
                iv_reason = itemView.findViewById(R.id.iv_reason)
                tv_reason = itemView.findViewById(R.id.tv_reason)
            }
        }
    }
}