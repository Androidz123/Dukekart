package com.dukeKart.android.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.bumptech.glide.Glide
import com.chaos.view.PinView
import com.dukeKart.android.R
import com.dukeKart.android.common.AppUrls
import com.dukeKart.android.common.AppUtils
import com.dukeKart.android.common.AppsStrings
import com.dukeKart.android.database.AppConstants
import com.dukeKart.android.database.Preferences
import com.dukeKart.android.views.BaseActivity
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONObject
import java.util.*

class AddToCart : BaseActivity(), View.OnClickListener {
    var tv_title: TextView? = null
    var iv_search: ImageView? = null
    var iv_back: ImageView? = null
    var rlCart: RelativeLayout? = null
    var rl_emptyCart: RelativeLayout? = null
    var rl_cartList: RelativeLayout? = null
    var tv_itemCount: TextView? = null
    var tv_goToShopping: TextView? = null
    var tv_cartSubTotal: TextView? = null
    var tv_shippingCost: TextView? = null
    var tv_totalAmount: TextView? = null
    var tv_discount: TextView? = null
    var tv_GSTAmount: TextView? = null

    var tv_payableAmount: TextView? = null
    var prefs: Preferences? = null
    var rv_CartList: RecyclerView? = null
    var cartList = ArrayList<HashMap<String, String>>()
    var btn_checkOut: Button? = null
    var isDelivered =""
    var cartSubTotal = 0.0
    var cartgst = 0.0
    var cartshipping = 0.0
    var cartTotal = 0.0
    var cartDisc = 0.0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_to_cart)
        setIds()
        setListeners()
        setHeaders()
        showLoading()
        resetPrefs()
        GetCartListApi()
    }

    private fun resetPrefs() {
        prefs!![AppConstants.cartPayable] = "0"
        prefs!![AppConstants.cartShipping] = "0"
        prefs!![AppConstants.cartTotal] = "0"
        prefs!![AppConstants.cartSubTotal] = "0"
        prefs!![AppConstants.cartGST] = "0"
        prefs!!.commit()
    }

    private fun setIds() {
        tv_title = findViewById(R.id.tv_title)

        tv_goToShopping = findViewById(R.id.tv_goToShopping)
        iv_back = findViewById(R.id.iv_back)
        iv_search = findViewById(R.id.iv_search)
        rlCart = findViewById(R.id.rlCart)
        rl_emptyCart = findViewById(R.id.rl_emptyCart)
        rl_cartList = findViewById(R.id.rl_cartList)
        tv_itemCount = findViewById(R.id.tv_itemCount)
        rv_CartList = findViewById(R.id.rv_CartList)
        btn_checkOut = findViewById(R.id.btn_checkOut)
        tv_cartSubTotal = findViewById(R.id.tv_cartSubTotal)
        tv_shippingCost = findViewById(R.id.tv_shippingCost)
        tv_totalAmount = findViewById(R.id.tv_totalAmount)
        tv_discount = findViewById(R.id.tv_discount)
        tv_GSTAmount = findViewById(R.id.tv_GSTAmount)
        tv_payableAmount = findViewById(R.id.tv_payableAmount)
        prefs = Preferences(mActivity)
    }

    private fun setHeaders() {
        tv_title!!.text = "My Cart"
        iv_search!!.visibility = View.GONE
        rlCart!!.visibility = View.GONE
        rl_emptyCart!!.visibility = View.VISIBLE
        rl_cartList!!.visibility = View.GONE
    }

    private fun setListeners() {
        tv_goToShopping!!.setOnClickListener(this)
        iv_back!!.setOnClickListener(this)
        btn_checkOut!!.setOnClickListener(this)
    }

    private fun GetCartListApi() {
        AppsStrings.setApiString("GetCartList")
        val url = AppUrls.GetCartList
        Log.v(AppsStrings.apiUrl, url)
        val json = JSONObject()
        val json_data = JSONObject()
        try {
            json_data.put("user_id", prefs!![AppConstants.userId])
            json.put(AppsStrings.BASEJSON, json_data)
            Log.v(AppsStrings.apiJson, json.toString())
        } catch (e: Exception) {
            AppUtils.showToast(mActivity, AppsStrings.defResponse)
            Log.v(AppsStrings.apiErrorException, e.message!!)
            e.printStackTrace()
        }
        AndroidNetworking.post(url)
                .addJSONObjectBody(json_data)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        parseGetCartListApi(response)
                    }

                    override fun onError(error: ANError) {
                        rl_cartList!!.visibility = View.INVISIBLE
                        rl_emptyCart!!.visibility = View.VISIBLE
                        hideLoading()
                        // handle error
                        if (error.errorCode != 0) {
                            AppUtils.showToast(mActivity, AppsStrings.defResponse)
                            Log.v(AppsStrings.apiErrorCode, "onError errorCode : " + error.errorCode)
                            Log.v(AppsStrings.apiErrorBody, "onError errorBody : " + error.errorBody)
                            Log.v(AppsStrings.apiErrorDetail, "onError errorDetail : " + error.errorDetail)
                        } else {
                            AppUtils.showToast(mActivity, AppsStrings.defResponse)
                            Log.v(AppsStrings.apiErrorException, error.message!!)
                        }
                    }
                })
    }

    private fun parseGetCartListApi(response: JSONObject) {
        Log.d(AppsStrings.apiResponse, response.toString())
        try {
            cartList.clear()
            val jsonObject = response.getJSONObject("ecommerce")
            hideLoading()

            cartSubTotal = 0.0
         //   cartshipping = 50
            cartTotal = 0.0
            if (jsonObject.getString("res_code").equals("1", ignoreCase = true)) {
                rl_cartList!!.visibility = View.VISIBLE
                rl_emptyCart!!.visibility = View.GONE

                isDelivered=  jsonObject.getString("Delivered")
                jsonObject.getString("CartSubTotal")
                jsonObject.getString("shippingCharge")
                jsonObject.getString("gstCharges")
                jsonObject.getString("totalPaybleAmount")


                cartSubTotal +=  jsonObject.getString("CartSubTotal")!!.toDouble()
                cartgst =  jsonObject.getString("gstCharges")!!.toDouble()
                if (!jsonObject.getString("shippingCharge")!!.isEmpty()) {
                    cartshipping = jsonObject.getString("shippingCharge")!!.toDouble()
                }
                cartTotal = jsonObject.getString("totalPaybleAmount")!!.toDouble()

                /*if (cartSubTotal.toInt() <=199) {*/
                    //cartshipping = 50
                    tv_shippingCost!!.text = AppUtils.setPriceTotal(cartshipping.toInt())
              /*  }else{
                    cartshipping = 0.0
                    tv_shippingCost!!.text = "Free   "
                }*/
                cartDisc = cartshipping
                tv_cartSubTotal!!.text = AppUtils.setPriceTotal(cartSubTotal.toInt())
                tv_discount!!.text = "- " + AppUtils.setPriceTotal(cartshipping.toInt())
                tv_GSTAmount!!.text = "- " + AppUtils.setPriceTotal(jsonObject.getString("gstCharges"))
               // tv_payableAmount!!.text = AppUtils.setPriceTotal(cartTotal.toInt()+ cartshipping.toInt() + cartDisc.toInt()+cartgst.toInt() )
                tv_payableAmount!!.text = AppUtils.setPriceTotal(jsonObject.getString("totalPaybleAmount").toString() )
                tv_totalAmount!!.text = AppUtils.setPriceTotal(cartTotal.toInt() )//+ cartshipping.toInt()



                val jsCartList = jsonObject.getJSONArray("CartList")
                for (i in 0 until jsCartList.length()) {

                    val hashCartList = HashMap<String, String>()
                    hashCartList.clear()
                    hashCartList["id"] = jsCartList.getJSONObject(i).getString("id")
                    hashCartList["product_id"] = jsCartList.getJSONObject(i).getString("product_id")
                    hashCartList["productName"] = jsCartList.getJSONObject(i).getString("productName")
                    hashCartList["size"] = jsCartList.getJSONObject(i).getString("size")
                    hashCartList["color"] = jsCartList.getJSONObject(i).getString("color")
                    hashCartList["gst"] = jsCartList.getJSONObject(i).getString("gst")
                    hashCartList["quantity"] = jsCartList.getJSONObject(i).getString("quantity")
                    hashCartList["price"] = jsCartList.getJSONObject(i).getString("price")
                    hashCartList["final_price"] = jsCartList.getJSONObject(i).getString("final_price")
                  //  hashCartList["shippingCharge"] = jsCartList.getJSONObject(i).getString("shippingCharge")
                    hashCartList["main_image"] = jsCartList.getJSONObject(i).getString("main_image")
                    hashCartList["totalProductPrice"] = jsCartList.getJSONObject(i).getString("totalProductPrice")
                    cartList.add(hashCartList)
                }
                tv_itemCount!!.text = "ITEMS (" + cartList.size + ")"
                prefs!!.set(AppConstants.cartQuantity, Integer.toString(cartList.size)).commit()
                rv_CartList!!.setHasFixedSize(true)
                rv_CartList!!.layoutManager = LinearLayoutManager(mActivity)
                rv_CartList!!.isNestedScrollingEnabled = false
                val cartAdapter = CartAdapter(cartList)
                rv_CartList!!.adapter = cartAdapter
            } else {
                rl_cartList!!.visibility = View.INVISIBLE
                rl_emptyCart!!.visibility = View.VISIBLE
                AppUtils.showToast(mActivity, jsonObject.getString("res_msg"))
                prefs!!.set(AppConstants.cartQuantity, "0").commit()
            }
        } catch (e: Exception) {
            rl_cartList!!.visibility = View.INVISIBLE
            rl_emptyCart!!.visibility = View.VISIBLE
            Log.v(AppsStrings.apiErrorException, e.message!!)
            AppUtils.showToast(mActivity, AppsStrings.defResponse)
        }
    }


    private inner class CartAdapter(var arrayList1: ArrayList<HashMap<String, String>>) : RecyclerView.Adapter<CartAdapter.MyViewHolder>() {
        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): MyViewHolder {
            val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.adapter_addtocart, viewGroup, false)
            return MyViewHolder(view)
        }

        override fun onBindViewHolder(myViewHolder: MyViewHolder, i: Int) {
            val cartQuantity = arrayList1[i]["quantity"]
            myViewHolder.tv_productName.text = arrayList1[i]["productName"]
            myViewHolder.tv_size.text =  arrayList1[i]["color"]+" "+arrayList1[i]["size"]
            myViewHolder.tv_color.text = "Color : " + arrayList1[i]["color"]
            myViewHolder.tv_prodMrp.text = AppUtils.setPrice(arrayList1[i]["totalProductPrice"])
            myViewHolder.tv_prodMrpPrice.text = AppUtils.setPrice(arrayList1[i]["final_price"])
            /*cartSubTotal += arrayList1[i]["final_price"]!!.toDouble()
            cartgst = arrayList1[i]["gst"]!!.toDouble()
            if (!arrayList1[i]["shippingCharge"]!!.isEmpty()) {
                cartshipping = arrayList1[i]["shippingCharge"]!!.toDouble()
            }
            cartTotal = cartSubTotal

            if (cartSubTotal.toInt() <=199) {
                 //cartshipping = 50
                tv_shippingCost!!.text = AppUtils.setPriceTotal(cartshipping.toInt())
            }else{
                 cartshipping = 0.0
                tv_shippingCost!!.text = "Free   "
            }
            cartDisc = cartshipping
            tv_cartSubTotal!!.text = AppUtils.setPriceTotal(cartSubTotal.toInt())
            tv_discount!!.text = "- " + AppUtils.setPriceTotal(cartshipping.toInt())
            tv_GSTAmount!!.text = "- " + AppUtils.setPriceTotal(arrayList1[i]["gst"])
            tv_payableAmount!!.text = AppUtils.setPriceTotal(cartTotal.toInt()+ cartshipping.toInt() + cartDisc.toInt()+cartgst.toInt() )
            tv_totalAmount!!.text = AppUtils.setPriceTotal(cartTotal.toInt() )//+ cartshipping.toInt()*/
            arrayList1[i]["main_image"]?.let { AppUtils.setImgPicasso(it, mActivity, myViewHolder.iv_product_image) }
            myViewHolder.tv_cartQuantity.text = cartQuantity
            myViewHolder.iv_delete.setOnClickListener {
                /* if (prefs!![AppConstants.userId].equals("", ignoreCase = true)) {
                     startActivity(Intent(mActivity, Login::class.java))
                 } else {*/
                RemoveProductFromCartApi(arrayList1[i]["product_id"])
                /*}*/
            }
            myViewHolder.iv_Minus.setOnClickListener {
                /*if (prefs!![AppConstants.userId].equals("", ignoreCase = true)) {
                    startActivity(Intent(mActivity, Login::class.java))
                } else */if (cartQuantity.equals("1", ignoreCase = true)) {
                RemoveProductFromCartApi(arrayList1[i]["product_id"])
            } else {
                UpdateQantityApi(arrayList1[i]["product_id"], Integer.toString(cartQuantity!!.toInt() - 1))
            }
            }
            myViewHolder.iv_Add.setOnClickListener {
                /* if (prefs!![AppConstants.userId].equals("", ignoreCase = true)) {
                     startActivity(Intent(mActivity, Login::class.java))
                 } else {*/
                UpdateQantityApi(arrayList1[i]["product_id"], Integer.toString(cartQuantity!!.toInt() + 1))
                /*  }*/
            }
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
            var iv_delete: ImageView
            var iv_Minus: ImageView
            var iv_Add: ImageView
            var iv_product_image: ImageView

            init {
                tv_productName = itemView.findViewById(R.id.tv_productName)
                tv_prodMrp = itemView.findViewById(R.id.tv_prodMrp)
                tv_prodMrpPrice = itemView.findViewById(R.id.tv_prodMrpPrice)
                tv_size = itemView.findViewById(R.id.tv_size)
                tv_color = itemView.findViewById(R.id.tv_color)
                tv_cartQuantity = itemView.findViewById(R.id.tv_cartQuantity)
                iv_delete = itemView.findViewById(R.id.iv_delete)
                iv_Minus = itemView.findViewById(R.id.iv_Minus)
                iv_Add = itemView.findViewById(R.id.iv_Add)
                iv_product_image = itemView.findViewById(R.id.iv_product_image)
            }
        }
    }



    private fun RemoveProductFromCartApi(prodId: String?) {
        showLoading()
        AppsStrings.setApiString("RemoveProductFromCart")
        val url = AppUrls.RemoveProductFromCart
        Log.v(AppsStrings.apiUrl, url)
        val json = JSONObject()
        val json_data = JSONObject()
        try {
            json_data.put("user_id", prefs!![AppConstants.userId])
            json_data.put("product_id", prodId)
            json.put(AppsStrings.BASEJSON, json_data)
            Log.v(AppsStrings.apiJson, json.toString())
        } catch (e: Exception) {
            AppUtils.showToast(mActivity, AppsStrings.defResponse)
            Log.v(AppsStrings.apiErrorException, e.message!!)
            e.printStackTrace()
        }
        AndroidNetworking.post(url)
            .addJSONObjectBody(json_data)
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    parseRemoveProductFromCartApi(response)
                }

                override fun onError(error: ANError) {
                    hideLoading()
                    // handle error
                    if (error.errorCode != 0) {
                        AppUtils.showToast(mActivity, AppsStrings.defResponse)
                        Log.v(AppsStrings.apiErrorCode, "onError errorCode : " + error.errorCode)
                        Log.v(AppsStrings.apiErrorBody, "onError errorBody : " + error.errorBody)
                        Log.v(AppsStrings.apiErrorDetail, "onError errorDetail : " + error.errorDetail)
                    } else {
                        AppUtils.showToast(mActivity, AppsStrings.defResponse)
                        Log.v(AppsStrings.apiErrorException, error.message!!)
                    }
                }
            })
    }

    private fun parseRemoveProductFromCartApi(response: JSONObject) {
        Log.d(AppsStrings.apiResponse, response.toString())
        try {
            val jsonObject = response.getJSONObject("ecommerce")
            if (jsonObject.getString("res_code").equals("1", ignoreCase = true)) {
                GetCartListApi()
            } else {
                hideLoading()
            }
            AppUtils.showToast(mActivity, jsonObject.getString("res_msg"))
        } catch (e: Exception) {
            Log.v(AppsStrings.apiErrorException, e.message!!)
            AppUtils.showToast(mActivity, AppsStrings.defResponse)
        }
    }

    private fun UpdateQantityApi(prodId: String?, quantity: String) {
        hideLoading()
        AppsStrings.setApiString("UpdateQantity")
        val url = AppUrls.UpdateQantity
        Log.v(AppsStrings.apiUrl, url)
        val json = JSONObject()
        val json_data = JSONObject()
        try {
            json_data.put("user_id", prefs!![AppConstants.userId])
            json_data.put("product_id", prodId)
            json_data.put("quantity", quantity)
            json.put(AppsStrings.BASEJSON, json_data)
            Log.v(AppsStrings.apiJson, json.toString())
        } catch (e: Exception) {
            AppUtils.showToast(mActivity, AppsStrings.defResponse)
            Log.v(AppsStrings.apiErrorException, e.message!!)
            e.printStackTrace()
        }
        AndroidNetworking.post(url)
            .addJSONObjectBody(json_data)
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    parseUpdateQantityApi(response)
                }

                override fun onError(error: ANError) {
                    hideLoading()
                    // handle error
                    if (error.errorCode != 0) {
                        AppUtils.showToast(mActivity, AppsStrings.defResponse)
                        Log.v(AppsStrings.apiErrorCode, "onError errorCode : " + error.errorCode)
                        Log.v(AppsStrings.apiErrorBody, "onError errorBody : " + error.errorBody)
                        Log.v(AppsStrings.apiErrorDetail, "onError errorDetail : " + error.errorDetail)
                    } else {
                        AppUtils.showToast(mActivity, AppsStrings.defResponse)
                        Log.v(AppsStrings.apiErrorException, error.message!!)
                    }
                }
            })
    }

    private fun parseUpdateQantityApi(response: JSONObject) {
        Log.d(AppsStrings.apiResponse, response.toString())
        try {
            val jsonObject = response.getJSONObject("ecommerce")
            if (jsonObject.getString("res_code").equals("1", ignoreCase = true)) {
                GetCartListApi()
            } else {
                hideLoading()
            }
            AppUtils.showToast(mActivity, jsonObject.getString("res_msg"))
        } catch (e: Exception) {
            Log.v(AppsStrings.apiErrorException, e.message!!)
            AppUtils.showToast(mActivity, AppsStrings.defResponse)
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.iv_back, R.id.tv_goToShopping -> onBackPressed()
            R.id.btn_checkOut ->
               if(isDelivered.equals("Yes")) {
                prefs!![AppConstants.cartPayable] = Integer.toString(cartTotal.toInt())
                prefs!![AppConstants.cartShipping] = Integer.toString(cartshipping.toInt())
                prefs!![AppConstants.cartTotal] = Integer.toString(cartTotal.toInt())
                prefs!![AppConstants.cartSubTotal] = Integer.toString(cartSubTotal.toInt())
                prefs!![AppConstants.cartGST] = Integer.toString(cartgst.toInt())
                prefs!![AppConstants.cartDiscount] = Integer.toString(cartDisc.toInt())
                prefs!!.commit()
                startActivity(Intent(mActivity, CheckOut::class.java))
            }
            else {
                   alertmsg();
                   // AppUtils.showToast(mActivity, "Seller cannot be shipped to your Pincode **")
                   //AppUtils.showToast(mActivity, "Comming Soon")
               }
        }
    }




    private fun  alertmsg(){
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.pincode_dialog, null)
        //AlertDialogBuilder
        val mBuilder = AlertDialog.Builder(this)
            .setView(mDialogView)

        //show dialog

        val  mAlertDialog = mBuilder.show()
        var tv_msg: TextView? = null
        var iv_msg: ImageView? = null
        var btnok: TextView? = null

        iv_msg = mAlertDialog.findViewById(R.id.iv_msg)
        tv_msg = mAlertDialog.findViewById(R.id.tv_msg)
        btnok = mAlertDialog.findViewById(R.id.btnok)
        tv_msg!!.setText("Currently, we are not delivering our product to this pincode. For more details, please contact the admin at 7460833766.")
        Glide.with(this)
            .load("https://img.freepik.com/premium-vector/delivery-location-flat-vector-illustration_203633-2826.jpg")
            .into(iv_msg);
        btnok!!.setOnClickListener {
            //dismiss dialog
            mAlertDialog.dismiss()
            //get text from EditTexts of custom layout





            //set the input text in TextView

        }
    }
    override fun onBackPressed() {
        //startActivity(Intent(mActivity, Dashboard::class.java))
        finish()
    }

}