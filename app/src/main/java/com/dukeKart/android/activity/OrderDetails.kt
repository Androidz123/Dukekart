package com.dukeKart.android.activity

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
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
import com.transferwise.sequencelayout.SequenceStep
import org.json.JSONObject
import java.util.*


class OrderDetails : BaseActivity(), View.OnClickListener {
    var tv_orderId: TextView? = null
    var tv_quantity: TextView? = null
    var tv_orderDate: TextView? = null
    var tv_finalPrice: TextView? = null
    var tv_totalPrice: TextView? = null
    var tv_shippingAddress: TextView? = null
    var tv_title: TextView? = null
    var tv_status: TextView? = null
    var iv_orderStatus: ImageView? = null
    var iv_search: ImageView? = null
    var iv_back: ImageView? = null
    var rv_productList: RecyclerView? = null

    var rlCart: RelativeLayout? = null
    var prefs: Preferences? = null
    var ll_OrderDetails: LinearLayout? = null
    var btn_cancelOrder: Button? = null
    var btn_invoice: Button? = null
    var btn_returnOrder: Button? = null
    var orderList = ArrayList<HashMap<String, String>>()

    var reasonList = ArrayList<HashMap<String, String>>()
    var reasonId: String? = ""
    var orderId = ""
    var awbnum = ""
    var canReturn = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_details)
        setIds()
        setHeaders()
        setListeners()
        showLoading()
        orderDetailsApi()
    }

    private fun setIds() {
        prefs = Preferences(this@OrderDetails)
        tv_status = findViewById(R.id.tv_status)
        ll_OrderDetails = findViewById(R.id.ll_OrderDetails)
        tv_title = findViewById(R.id.tv_title)
        tv_orderId = findViewById(R.id.tv_orderId)
        tv_quantity = findViewById(R.id.tv_quantity)
        tv_orderDate = findViewById(R.id.tv_orderDate)
        tv_finalPrice = findViewById(R.id.tv_finalPrice)
        tv_totalPrice = findViewById(R.id.tv_totalPrice)
        tv_shippingAddress = findViewById(R.id.tv_shippingAddress)
        btn_cancelOrder = findViewById(R.id.btn_cancelOrder)
        btn_invoice = findViewById(R.id.btn_invoice)
        btn_returnOrder = findViewById(R.id.btn_returnOrder)
        iv_search = findViewById(R.id.iv_search)
        iv_back = findViewById(R.id.iv_back)
        iv_orderStatus = findViewById(R.id.iv_orderStatus)
        rv_productList = findViewById(R.id.rv_productList)
        rlCart = findViewById(R.id.rlCart)
    }

    private fun setHeaders() {
        tv_title!!.text = "Order Details"
        iv_search!!.visibility = View.GONE
        rlCart!!.visibility = View.GONE
        try {
            if (intent.getStringExtra(AppConstants.canReturn).equals("true", ignoreCase = true)) {
                canReturn = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setListeners() {
        iv_back!!.setOnClickListener(this)
        btn_cancelOrder!!.setOnClickListener(this)
        btn_returnOrder!!.setOnClickListener(this)
        btn_invoice!!.setOnClickListener(this)
    }

    private fun orderDetailsApi() {
        AppsStrings.setApiString("OrderDetailsApi")
        val url = AppUrls.OrderDetails
        Log.v(AppsStrings.apiUrl, url)
        val json = JSONObject()
        val json_data = JSONObject()
        try {
            json_data.put("orderId", intent.getStringExtra(AppConstants.orderId))
            json.put(AppsStrings.BASEJSON, json_data)
            Log.v(AppsStrings.apiJson, json.toString())
        } catch (e: Exception) {
            AppUtils.showToast(this@OrderDetails, AppsStrings.defResponse)
            Log.v(AppsStrings.apiErrorException, e.message!!)
            e.printStackTrace()
        }
        AndroidNetworking.post(url)
                .addJSONObjectBody(json_data)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        parseOrderDetailsApi(response)
                    }

                    override fun onError(error: ANError) {
                        ll_OrderDetails!!.visibility = View.INVISIBLE
                        hideLoading()
                        // handle error
                        if (error.errorCode != 0) {
                            AppUtils.showToast(this@OrderDetails, AppsStrings.defResponse)
                            Log.v(AppsStrings.apiErrorCode, "onError errorCode : " + error.errorCode)
                            Log.v(AppsStrings.apiErrorBody, "onError errorBody : " + error.errorBody)
                            Log.v(AppsStrings.apiErrorDetail, "onError errorDetail : " + error.errorDetail)
                        } else {
                            AppUtils.showToast(this@OrderDetails, AppsStrings.defResponse)
                            Log.v(AppsStrings.apiErrorException, error.message!!)
                        }
                    }
                })
    }

    private fun parseOrderDetailsApi(response: JSONObject) {
        Log.d(AppsStrings.apiResponse, response.toString())
        try {
            val jsonObject = response.getJSONObject("ecommerce")
            reasonListApi()

            orderList.clear()
            if (jsonObject.getString("res_code").equals("1", ignoreCase = true)) {
                ll_OrderDetails!!.visibility = View.VISIBLE
                val jsProdList = jsonObject.getJSONArray("ProductList")
                val jsShipping = jsonObject.getJSONArray("ShippingAddress")
                val shipObject = jsShipping.getJSONObject(0)
                // jsonObject.getString("invoice_link")

                btn_invoice?.setOnClickListener {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(jsonObject.getString("invoice_link")))
                    startActivity(browserIntent) }
                orderId = jsonObject.getString("order_number")
                tv_orderId!!.text = jsonObject.getString("order_number")
                tv_totalPrice!!.text = AppUtils.setPrice(jsonObject.getString("total_price"))
                tv_finalPrice!!.text = AppUtils.setPrice(jsonObject.getString("final_price"))
                tv_orderDate!!.text = /*AppUtils.getDateTimeFromTimestamp(*/jsonObject["order_date"].toString()/*.toLong()*/
                tv_quantity!!.text = jsonObject.getString("total_Items")
                tv_shippingAddress!!.text = """${shipObject.getString("contact_person")}
            ${shipObject.getString("address")} ${shipObject.getString("localty")} ${shipObject.getString("city")} ${shipObject.getString("state")} ${shipObject.getString("pincode")}"""
                btn_cancelOrder!!.visibility = View.GONE
                btn_returnOrder!!.visibility = View.GONE
                when (jsonObject.getString("order_satatus")) {
                    "1" -> {
                        tv_status!!.text = "Order waiting for seller's approval"
                        tv_status!!.setTextColor(resources.getColor(R.color.orange))
                        iv_orderStatus!!.setImageResource(R.drawable.ic_new)
                        btn_cancelOrder!!.visibility = View.VISIBLE
                    }

                    "2" -> {
                        tv_status!!.text = "Order Shipped"
                        tv_status!!.setTextColor(resources.getColor(R.color.orange))
                        iv_orderStatus!!.setImageResource(R.drawable.ic_process)
                        btn_cancelOrder!!.visibility = View.VISIBLE
                    }

                    "3" -> {
                        tv_status!!.text = "Order Delivered"
                        tv_status!!.setTextColor(resources.getColor(R.color.green))
                        iv_orderStatus!!.setImageResource(R.drawable.ic_delivered)
                        if (canReturn) btn_returnOrder!!.visibility = View.VISIBLE
                    }

                    "4" -> {
                        tv_status!!.text = "Order cancelled"
                        tv_status!!.setTextColor(resources.getColor(R.color.colorRed))
                        iv_orderStatus!!.setImageResource(R.drawable.ic_cancelled)
                    }

                    "5" -> {
                        tv_status!!.text = "Order confirmed by seller"
                        tv_status!!.setTextColor(resources.getColor(R.color.orange))
                        iv_orderStatus!!.setImageResource(R.drawable.ic_transit)
                        btn_cancelOrder!!.visibility = View.VISIBLE
                    }

                    "6" -> {
                        tv_status!!.text = "Order Rejected by seller"
                        tv_status!!.setTextColor(resources.getColor(R.color.colorRed))
                        iv_orderStatus!!.setImageResource(R.drawable.ic_cancelled)
                    }

                    "7" -> {
                        tv_status!!.text = "Return Requested"
                        tv_status!!.setTextColor(resources.getColor(R.color.orange))
                        iv_orderStatus!!.setImageResource(R.drawable.ic_transit)
                    }

                    "8" -> {
                        tv_status!!.text = "Return Completed"
                        tv_status!!.setTextColor(resources.getColor(R.color.green))
                        iv_orderStatus!!.setImageResource(R.drawable.ic_delivered)
                    }
                }
                for (i in 0 until jsProdList.length()) {
                    val hashOrderDetails = HashMap<String, String>()
                    hashOrderDetails["product_id"] = jsProdList.getJSONObject(i).getString("product_id")
                    hashOrderDetails["sku_code"] = jsProdList.getJSONObject(i).getString("sku_code")
                    hashOrderDetails["product_name"] = jsProdList.getJSONObject(i).getString("product_name")
                    hashOrderDetails["size"] = jsProdList.getJSONObject(i).getString("size")
                    hashOrderDetails["color"] = jsProdList.getJSONObject(i).getString("color")
                    hashOrderDetails["brand"] = jsProdList.getJSONObject(i).getString("brand")
                    hashOrderDetails["price"] = jsProdList.getJSONObject(i).getString("price")
                    hashOrderDetails["final_price"] = jsProdList.getJSONObject(i).getString("final_price")
                    hashOrderDetails["quantity"] = jsProdList.getJSONObject(i).getString("quantity")
                    hashOrderDetails["main_image"] = jsProdList.getJSONObject(i).getString("main_image")
                    hashOrderDetails["image1"] = jsProdList.getJSONObject(i).getString("image1")
                    hashOrderDetails["waybill"] = jsProdList.getJSONObject(i).getString("waybill")
                    orderList.add(hashOrderDetails)
                }
                rv_productList!!.setHasFixedSize(true)
                rv_productList!!.layoutManager = LinearLayoutManager(this@OrderDetails)
                rv_productList!!.isNestedScrollingEnabled = false
                val orderDetailsAdapter = OrderDetailsAdapter(orderList)
                rv_productList!!.adapter = orderDetailsAdapter
            } else {
                ll_OrderDetails!!.visibility = View.INVISIBLE
                AppUtils.showToast(this@OrderDetails, jsonObject.getString("res_msg"))
            }
        } catch (e: Exception) {
            ll_OrderDetails!!.visibility = View.INVISIBLE
            Log.v(AppsStrings.apiErrorException, e.message!!)
            AppUtils.showToast(this@OrderDetails, AppsStrings.defResponse)
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
                            AppUtils.showToast(this@OrderDetails, AppsStrings.defResponse)
                            Log.v(AppsStrings.apiErrorCode, "onError errorCode : " + error.errorCode)
                            Log.v(AppsStrings.apiErrorBody, "onError errorBody : " + error.errorBody)
                            Log.v(AppsStrings.apiErrorDetail, "onError errorDetail : " + error.errorDetail)
                        } else {
                            AppUtils.showToast(this@OrderDetails, AppsStrings.defResponse)
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
                AppUtils.showToast(this@OrderDetails, jsonObject.getString("res_msg"))
            }
        } catch (e: Exception) {
            Log.v(AppsStrings.apiErrorException, e.message!!)
            AppUtils.showToast(this@OrderDetails, AppsStrings.defResponse)
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.iv_back -> onBackPressed()
            R.id.btn_cancelOrder -> confirmDeleteDialog(AppUrls.CancelOrder, orderId)
            R.id.btn_returnOrder -> confirmDeleteDialog(AppUrls.ReturnFullyOrder, orderId)
        }
    }

    fun confirmDeleteDialog(url: String, orderId: String) {
        reasonId = ""
        val builder = AlertDialog.Builder(this@OrderDetails)
        val inflater = LayoutInflater.from(this@OrderDetails)
        val dialogView = inflater.inflate(R.layout.reason_popup, null)
        builder.setView(dialogView)
        val rrSubmit = dialogView.findViewById<RelativeLayout>(R.id.rr_submit)
        val rr_lg_cancel = dialogView.findViewById<RelativeLayout>(R.id.rr_lg_cancel)
        val rv_Reasons: RecyclerView = dialogView.findViewById(R.id.rv_Reasons)
        rv_Reasons.setHasFixedSize(true)
        rv_Reasons.layoutManager = LinearLayoutManager(this@OrderDetails)
        rv_Reasons.isNestedScrollingEnabled = false
        val reasonListAdapter: ReasonListAdapter = ReasonListAdapter(reasonList)
        rv_Reasons.adapter = reasonListAdapter
        val alert = builder.create()
        rrSubmit.setOnClickListener {
            if (reasonId.equals("", ignoreCase = true)) {
                AppUtils.showToast(this@OrderDetails, "Select a reason")
            } else {
                cancelReturnApi(url, orderId)
                alert.dismiss()
            }
        }
        val rrCancel = dialogView.findViewById<RelativeLayout>(R.id.rr_cancel)
        rr_lg_cancel.setOnClickListener { alert.dismiss() }
        rrCancel.setOnClickListener { alert.dismiss() }
        alert.show()
    }

    private fun cancelReturnApi(url: String, orderId: String) {
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
            AppUtils.showToast(this@OrderDetails, AppsStrings.defResponse)
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
                        hideLoading()
                        // handle error
                        if (error.errorCode != 0) {
                            AppUtils.showToast(this@OrderDetails, AppsStrings.defResponse)
                            Log.v(AppsStrings.apiErrorCode, "onError errorCode : " + error.errorCode)
                            Log.v(AppsStrings.apiErrorBody, "onError errorBody : " + error.errorBody)
                            Log.v(AppsStrings.apiErrorDetail, "onError errorDetail : " + error.errorDetail)
                        } else {
                            AppUtils.showToast(this@OrderDetails, AppsStrings.defResponse)
                            Log.v(AppsStrings.apiErrorException, error.message!!)
                        }
                    }
                })
    }

    private fun parseCancelReturnApi(response: JSONObject) {
        Log.d(AppsStrings.apiResponse, response.toString())
        try {
            val jsonObject = response.getJSONObject("ecommerce")
            AppUtils.showToast(this@OrderDetails, jsonObject.getString("res_msg"))
            if (jsonObject.getString("res_code").equals("1", ignoreCase = true)) {
                orderDetailsApi()
            }
        } catch (e: Exception) {
            Log.v(AppsStrings.apiErrorException, e.message!!)
            AppUtils.showToast(this@OrderDetails, AppsStrings.defResponse)
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this@OrderDetails, OrderList::class.java))
    }

    private inner class OrderDetailsAdapter(var arrayList1: ArrayList<HashMap<String, String>>) : RecyclerView.Adapter<OrderDetailsAdapter.MyViewHolder>() {
        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): MyViewHolder {
            val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.adapter_order_detail, viewGroup, false)
            return MyViewHolder(view)
        }

        override fun onBindViewHolder(myViewHolder: MyViewHolder, i: Int) {
            val cartQuantity = arrayList1[i]["quantity"]
            myViewHolder.tv_productName.text = arrayList1[i]["product_name"]
            myViewHolder.tv_size.text = "Size : " + arrayList1[i]["size"]
            myViewHolder.tv_color.text = "Color : " + arrayList1[i]["color"]
            myViewHolder.tv_prodMrpPrice.text = AppUtils.setPrice(arrayList1[i]["price"])
            myViewHolder.tv_prodMrp.text = AppUtils.setPrice(arrayList1[i]["final_price"])
            AppUtils.setImgPicasso(arrayList1[i]["main_image"], this@OrderDetails, myViewHolder.iv_product_image)
            myViewHolder.tv_cartQuantity.text = cartQuantity
            awbnum = arrayList1[i]["waybill"].toString()

           if (!arrayList1[i]["waybill"]!!.isEmpty()){
                myViewHolder.ll_trackorder!!.visibility = View.VISIBLE
            }else{
                myViewHolder.ll_trackorder!!.visibility = View.GONE
            }



            myViewHolder.ll_trackorder.setOnClickListener(View.OnClickListener { view ->

                orderTrack1Api(intent.getStringExtra(AppConstants.orderId))

                // Do some work here


            })
        }

        override fun getItemCount(): Int {
            return arrayList1.size
        }

        inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var tv_productName: TextView
            var tv_prodMrp: TextView
            var tv_prodMrpPrice: TextView
            var tv_size: TextView
            var tv_color: TextView
            var tv_cartQuantity: TextView
            var iv_product_image: ImageView
            var ll_trackorder: LinearLayout

            init {
                tv_productName = itemView.findViewById(R.id.tv_productName)
                tv_prodMrp = itemView.findViewById(R.id.tv_prodMrp)
                tv_prodMrpPrice = itemView.findViewById(R.id.tv_prodMrpPrice)
                tv_size = itemView.findViewById(R.id.tv_size)
                tv_color = itemView.findViewById(R.id.tv_color)
                tv_cartQuantity = itemView.findViewById(R.id.tv_cartQuantity)
                iv_product_image = itemView.findViewById(R.id.iv_product_image)
                ll_trackorder = itemView.findViewById(R.id.ll_trackorder)
            }
        }
    }

    private inner class ReasonListAdapter(var arrayList1: ArrayList<HashMap<String, String>>) : RecyclerView.Adapter<ReasonListAdapter.MyViewHolder>() {
        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): MyViewHolder {
            val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.adapter_reasons, viewGroup, false)
            return MyViewHolder(view)
        }

        @RequiresApi(Build.VERSION_CODES.N)
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

    private fun orderTrack1Api(orderId: String?) {
        AppsStrings.setApiString("TrackOrders")
        val url = AppUrls.TrackOrders
        Log.v(AppsStrings.apiUrl, url)
        val json = JSONObject()
        val json_data = JSONObject()
        try {
            json_data.put("orderId",orderId )
            json.put(AppsStrings.BASEJSON, json_data)
            Log.v(AppsStrings.apiJson, json.toString())
        } catch (e: Exception) {
            AppUtils.showToast(this@OrderDetails, AppsStrings.defResponse)
            Log.v(AppsStrings.apiErrorException, e.message!!)
            e.printStackTrace()
        }
        AndroidNetworking.post(url)
                .addJSONObjectBody(json_data)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        Log.d(AppsStrings.apiResponse, response.toString())
                        parseLoginApi(response)
                    }

                    override fun onError(error: ANError){
                        hideLoading()
                        // handle error
                        if (error.errorCode != 0) {
                            AppUtils.showToast(this@OrderDetails, AppsStrings.defResponse)
                            Log.v(AppsStrings.apiErrorCode, "onError errorCode : " + error.errorCode)
                            Log.v(AppsStrings.apiErrorBody, "onError errorBody : " + error.errorBody)
                            Log.v(AppsStrings.apiErrorDetail, "onError errorDetail : " + error.errorDetail)
                        } else {
                            AppUtils.showToast(this@OrderDetails, AppsStrings.defResponse)
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
            AppUtils.showToast(this@OrderDetails, jsonObject.getString("res_msg"))


            if (jsonObject.getString("res_code").equals("1", ignoreCase = true)) {
                var order_status = jsonObject.getString("order_status")
                Log.v(AppsStrings.apiErrorException, awbnum)
                val finalCanReturn = canReturn
                startActivity(Intent(this@OrderDetails, OrderTrack::class.java)
                        .putExtra(AppConstants.order_status, order_status)
                        .putExtra(AppConstants.awb_number, awbnum)
                        .putExtra(AppConstants.canReturn, finalCanReturn))


            }
        } catch (e: Exception) {
            Log.v(AppsStrings.apiErrorException, e.message!!)
            AppUtils.showToast(this@OrderDetails, AppsStrings.defResponse)

        }
    }



     fun showTrackDialog() {
        val dialog = Dialog(mActivity)
        //dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.custom_layout)
        val window = dialog.window
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        val wlp = window!!.attributes
        wlp.flags = wlp.flags and WindowManager.LayoutParams.FLAG_BLUR_BEHIND.inv()
        window!!.attributes = wlp
        dialog.window!!.setLayout(WindowManager.LayoutParams.FILL_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        dialog.setContentView(R.layout.custom_layout)
        var step1: SequenceStep? = null
        var step2: SequenceStep? = null
        var step3: SequenceStep? = null
        var step4: SequenceStep? = null
        var step5: SequenceStep? = null

        step1 = dialog.findViewById(R.id.step1) as SequenceStep
        step2 = dialog.findViewById(R.id.step2) as SequenceStep
        step3 = dialog.findViewById(R.id.step3) as SequenceStep
        step4 = dialog.findViewById(R.id.step4) as SequenceStep
        step5 = dialog.findViewById(R.id.step5) as SequenceStep

        //programatically activating
        step2!!.setActive(true)
        step4!!.setTitle("Active Step")
        step4!!.setAnchor("Date...")
        step4!!.setSubtitle("Subtitle of this step third.")
        //programatically seting style to Title
        step4!!.setTitleTextAppearance(R.style.TextAppearance_AppCompat_Title)

        // if ()

        dialog.show()


    }
}