package com.dukeKart.android.activity

import android.app.Dialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
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
import com.dukeKart.android.database.Preferences
import com.dukeKart.android.views.BaseActivity
import com.transferwise.sequencelayout.SequenceStep
import org.json.JSONObject
import java.util.ArrayList
import java.util.HashMap

class TrackOrders : BaseActivity(), View.OnClickListener{
    var prefs: Preferences? = null
    var tv_title: TextView? = null
    var iv_search: ImageView? = null
    var iv_back: ImageView? = null
    var rlCart: RelativeLayout? = null
    var rc_tracklist: RecyclerView? = null
    var trackList = ArrayList<HashMap<String, String>>()

    var step1: SequenceStep? = null
    var step2: SequenceStep? = null
    var step3: SequenceStep? = null
    var step4: SequenceStep? = null
    var step5: SequenceStep? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trackorders)
        setIds()
        setHeaders()
        setListeners()
       /* orderTrackApi("2250115885655")*/
        orderTrack1Api("ORD1612190065")
    }

    private fun setIds() {
        prefs = Preferences(this@TrackOrders)

        tv_title = findViewById(R.id.tv_title)
        iv_search = findViewById(R.id.iv_search)
        iv_back = findViewById(R.id.iv_back)
        rlCart = findViewById(R.id.rlCart)



    }
    private fun setHeaders() {
        tv_title!!.text = "Track Order"
        iv_search!!.visibility = View.GONE
        rlCart!!.visibility = View.GONE
       /* try {
            if (intent.getStringExtra(AppConstants.canReturn).equals("true", ignoreCase = true)) {
                canReturn = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }*/
    }
    private fun setListeners() {
        iv_back!!.setOnClickListener(this)
    }
    override fun onClick(view: View) {
        when (view.id) {
            R.id.iv_back -> onBackPressed()

        }
    }
    override fun onBackPressed() {
        startActivity(Intent(this@TrackOrders, OrderDetails::class.java))
    }
    private fun orderTrackApi(awb_number: String?) {
        AppsStrings.setApiString("TrackOrder")
        val url = AppUrls.TrackOrder
        Log.v(AppsStrings.apiUrl, url)
        val json = JSONObject()
        val json_data = JSONObject()
        try {
            json_data.put("awb_number",awb_number )
            json.put(AppsStrings.BASEJSON, json_data)
            Log.v(AppsStrings.apiJson, json.toString())
        } catch (e: Exception) {
            AppUtils.showToast(this@TrackOrders, AppsStrings.defResponse)
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
                        parseOrderTrackApi(response)
                    }

                    override fun onError(error: ANError) {
                        hideLoading()
                        // handle error
                        if (error.errorCode != 0) {
                            AppUtils.showToast(this@TrackOrders, AppsStrings.defResponse)
                            Log.v(AppsStrings.apiErrorCode, "onError errorCode : " + error.errorCode)
                            Log.v(AppsStrings.apiErrorBody, "onError errorBody : " + error.errorBody)
                            Log.v(AppsStrings.apiErrorDetail, "onError errorDetail : " + error.errorDetail)
                        } else {
                            AppUtils.showToast(this@TrackOrders, AppsStrings.defResponse)
                            Log.v(AppsStrings.apiErrorException, error.message!!)
                        }
                    }
                })
    }

    private fun parseOrderTrackApi(response: JSONObject) {
        showTracListkDialog()


        Log.d(AppsStrings.apiResponse, response.toString())
        try {
            val jsonObject = response.getJSONObject("ecommerce")
            trackList.clear()
            if (jsonObject.getString("res_code").equals("1", ignoreCase = true)) {
                //ll_OrderDetails!!.visibility = View.VISIBLE
                val jsProdList = jsonObject.getJSONArray("Data")
                for (i in 0 until jsProdList.length()) {
                    val hashOrderDetails = HashMap<String, String>()
                    hashOrderDetails["status_code"] = jsProdList.getJSONObject(i).getString("status_code")
                    hashOrderDetails["status"] = jsProdList.getJSONObject(i).getString("status")
                    hashOrderDetails["status_date_time"] = jsProdList.getJSONObject(i).getString("status_date_time")
                    hashOrderDetails["status_location"] = jsProdList.getJSONObject(i).getString("status_location")
                    hashOrderDetails["status_remark"] = jsProdList.getJSONObject(i).getString("status_remark")
                    trackList.add(hashOrderDetails)

                }
                rc_tracklist!!.setHasFixedSize(true)
                rc_tracklist!!.layoutManager = LinearLayoutManager(this@TrackOrders)
                rc_tracklist!!.isNestedScrollingEnabled = false
                val trackListAdapter = TrackListAdapter(trackList)
                rc_tracklist!!.adapter = trackListAdapter
            } else {
                AppUtils.showToast(this@TrackOrders, jsonObject.getString("res_msg"))
            }
        } catch (e: Exception) {
            Log.v(AppsStrings.apiErrorException, e.message!!)
            AppUtils.showToast(this@TrackOrders, AppsStrings.defResponse)
        }
    }

    fun showTracListkDialog() {
        val dialog = Dialog(mActivity)
        //dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.popup_tracklist)
        val window = dialog.window
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        val wlp = window!!.attributes
        wlp.flags = wlp.flags and WindowManager.LayoutParams.FLAG_BLUR_BEHIND.inv()
        window!!.attributes = wlp
        dialog.window!!.setLayout(WindowManager.LayoutParams.FILL_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        dialog.setContentView(R.layout.popup_tracklist)

        var rl_close: RelativeLayout? = null
        rl_close = dialog.findViewById(R.id.rl_close) as RelativeLayout
        rc_tracklist = dialog.findViewById(R.id.rc_tracklist) as RecyclerView
        rl_close.setOnClickListener {
            dialog.cancel()
        }



        dialog.show()


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
            AppUtils.showToast(this@TrackOrders, AppsStrings.defResponse)
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
                            AppUtils.showToast(this@TrackOrders, AppsStrings.defResponse)
                            Log.v(AppsStrings.apiErrorCode, "onError errorCode : " + error.errorCode)
                            Log.v(AppsStrings.apiErrorBody, "onError errorBody : " + error.errorBody)
                            Log.v(AppsStrings.apiErrorDetail, "onError errorDetail : " + error.errorDetail)
                        } else {
                            AppUtils.showToast(this@TrackOrders, AppsStrings.defResponse)
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
            AppUtils.showToast(this@TrackOrders, jsonObject.getString("res_msg"))
            if (jsonObject.getString("res_code").equals("1", ignoreCase = true)) {
                var order_status = jsonObject.getString("order_status")
                var remark = jsonObject.getString("remark")
                showTrack(order_status, remark)
               // showTrackDialog(order_status, remark)


            }
        } catch (e: Exception) {
            Log.v(AppsStrings.apiErrorException, e.message!!)
            AppUtils.showToast(this@TrackOrders, AppsStrings.defResponse)
        }
    }

    private inner class TrackListAdapter(var arrayList1: ArrayList<HashMap<String, String>>) : RecyclerView.Adapter<TrackListAdapter.MyViewHolder>() {
        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): MyViewHolder {
            val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.adapter_tracklist, viewGroup, false)
            return MyViewHolder(view)
        }

        @RequiresApi(Build.VERSION_CODES.N)
        override fun onBindViewHolder(myViewHolder: MyViewHolder, i: Int) {
            myViewHolder.tv_trackDate.text = arrayList1[i]["status_date_time"]
            myViewHolder.tv_trackStatus.text = arrayList1[i]["status_code"]
            myViewHolder.tv_tracklocation.text = arrayList1[i]["status_location"]
            myViewHolder.tv_trackStatus2.text = arrayList1[i]["status_remark"]


        }

        override fun getItemCount(): Int {
            return arrayList1.size
        }

        inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var tv_trackDate: TextView
            var tv_trackStatus: TextView
            var tv_tracklocation: TextView
            var tv_trackStatus2: TextView


            init {
                tv_trackDate = itemView.findViewById(R.id.tv_trackDate)
                tv_trackStatus = itemView.findViewById(R.id.tv_trackStatus)
                tv_tracklocation = itemView.findViewById(R.id.tv_tracklocation)
                tv_trackStatus2 = itemView.findViewById(R.id.tv_trackStatus2)
            }
        }
    }

    fun showTrackDialog(order_status: String?,remark: String?) {
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

        if (order_status.equals("1")){
            step1!!.setActive(true)
            step1!!.setTitle("Order Placed")
            step1!!.setAnchor("Date...")
            //programatically seting style to Title
            step1!!.setTitleTextAppearance(R.style.TextAppearance_AppCompat_Title)
        }else if (order_status.equals("2")){
            step1!!.setActive(false)
            step2!!.setActive(true)
            step3!!.setActive(false)
            step4!!.setActive(false)
            step2!!.setTitle(remark)
            step2!!.setAnchor("Date...")
            step2!!.setTitleTextAppearance(R.style.TextAppearance_AppCompat_Title)
            //programatically seting style to Title
        }else if (order_status.equals("3")){
            step3!!.setActive(true)
            step3!!.setTitle(remark)
            //step1!!.setAnchor("Date...")
            //programatically seting style to Title
            step3!!.setTitleTextAppearance(R.style.TextAppearance_AppCompat_Title)
        }else if (order_status.equals("4")){
            step4!!.setActive(true)
            step4!!.setTitle(remark)
            //step1!!.setAnchor("Date...")
            //programatically seting style to Title
          //  step4!!.
        // if ()
        }

        dialog.show()


    }

    fun showTrack(order_status: String?,remark: String?){


        step1 = findViewById(R.id.step1) as SequenceStep
        step2 = findViewById(R.id.step2) as SequenceStep
        step3 = findViewById(R.id.step3) as SequenceStep
        step4 = findViewById(R.id.step4) as SequenceStep
        step5 = findViewById(R.id.step5) as SequenceStep

        if (order_status.equals("1")){
            step1!!.setActive(true)
            step1!!.setTitle("Order Placed")
            step1!!.setAnchor("Date...")
            //programatically seting style to Title
            step1!!.setTitleTextAppearance(R.style.TextAppearance_AppCompat_Title)
        }else if (order_status.equals("2")){
            step1!!.setActive(false)
            step2!!.setActive(true)
            step3!!.setActive(false)
            step4!!.setActive(false)
            step2!!.setTitle(remark)
            step2!!.setAnchor("Date...")
            step2!!.setTitleTextAppearance(R.style.TextAppearance_AppCompat_Title)
            //programatically seting style to Title
        }else if (order_status.equals("3")){
            step3!!.setActive(true)
            step3!!.setTitle(remark)
            //step1!!.setAnchor("Date...")
            //programatically seting style to Title
            step3!!.setTitleTextAppearance(R.style.TextAppearance_AppCompat_Title)
        }else if (order_status.equals("4")){
            step4!!.setActive(true)
            step4!!.setTitle(remark)
            //step1!!.setAnchor("Date...")
            //programatically seting style to Title
            //  step4!!.
            // if ()

        }
    }
}