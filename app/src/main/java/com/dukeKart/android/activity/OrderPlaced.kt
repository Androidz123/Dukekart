package com.dukeKart.android.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.dukeKart.android.R
import com.dukeKart.android.common.AppUtils
import com.dukeKart.android.database.AppConstants
import com.dukeKart.android.database.Preferences
import com.dukeKart.android.views.BaseActivity

class OrderPlaced : BaseActivity(), View.OnClickListener {
    var iv_search: ImageView? = null
    var iv_back: ImageView? = null
    var rlCart: RelativeLayout? = null
    var tv_title: TextView? = null
    var tv_orderId: TextView? = null
    var tv_orderDate: TextView? = null
    var tv_shippingAddress: TextView? = null
    var tv_discount: TextView? = null
    var tv_Amount: TextView? = null
    var tv_shippingCost: TextView? = null
    var tv_payableAmount: TextView? = null
    var prefs: Preferences? = null
    var back_btn: Button? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_placed)
        setIds()
        setHeaders()
        setListeners()
        fillData()
    }

    private fun setIds() {
        prefs = Preferences(this@OrderPlaced)
        iv_back = findViewById(R.id.iv_back)
        iv_search = findViewById(R.id.iv_search)
        rlCart = findViewById(R.id.rlCart)
        tv_title = findViewById(R.id.tv_title)
        tv_orderId = findViewById(R.id.tv_orderId)
        tv_orderDate = findViewById(R.id.tv_orderDate)
        tv_shippingAddress = findViewById(R.id.tv_shippingAddress)
        tv_discount = findViewById(R.id.tv_discount)
        tv_Amount = findViewById(R.id.tv_Amount)
        tv_shippingCost = findViewById(R.id.tv_shippingCost)
        tv_payableAmount = findViewById(R.id.tv_payableAmount)
        back_btn = findViewById(R.id.back_btn)
    }

    private fun setListeners() {
        iv_back!!.setOnClickListener(this)
        back_btn!!.setOnClickListener(this)
    }

    private fun setHeaders() {
        try {
            iv_search!!.visibility = View.GONE
            rlCart!!.visibility = View.GONE
            tv_title!!.text = "Order Placed"
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //  public void setPrice(int pricePayable, int priceSubTotal, int priceShipping, int priceDiscount) {
    //            setPrice(Integer.parseInt(prefs.get(AppConstants.cartPayable)), Integer.parseInt(prefs.get(AppConstants.cartSubTotal)),
    //            Integer.parseInt(prefs.get(AppConstants.cartShipping)), Integer.parseInt(prefs.get(AppConstants.cartDiscount)));
    //
    private fun fillData() {
        try {
            val intent = intent
            tv_orderId!!.text = intent.getStringExtra(AppConstants.orderId)
            tv_orderDate!!.text = intent.getStringExtra(AppConstants.orderDate)
            tv_shippingAddress!!.text = prefs!![AppConstants.primaryAddress]
            tv_discount!!.text = AppUtils.setPriceTotal(prefs!![AppConstants.cartGST])
            tv_Amount!!.text = AppUtils.setPriceTotal(prefs!![AppConstants.cartSubTotal])
            tv_shippingCost!!.text = AppUtils.setPriceTotal(prefs!![AppConstants.cartShipping])
            tv_payableAmount!!.text = AppUtils.setPriceTotal(prefs!![AppConstants.cartPayable])
        } catch (e: Exception) {
            AppUtils.handleCatch(this@OrderPlaced, e)
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.iv_back, R.id.back_btn -> onBackPressed()
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this@OrderPlaced, Dashboard::class.java))
    }
}