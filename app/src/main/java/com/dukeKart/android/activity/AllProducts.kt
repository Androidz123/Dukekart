package com.dukeKart.android.activity

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
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

class AllProducts : BaseActivity(), View.OnClickListener {
    var tv_ListTitle: TextView? = null
    var tv_title: TextView? = null
    var tvCart: TextView? = null
    var rv_AllProducts: RecyclerView? = null
    var prefs: Preferences? = null
    var iv_search: ImageView? = null
    var iv_back: ImageView? = null
    var rlCart: RelativeLayout? = null
    var arrayAllProduct = ArrayList<HashMap<String, String>>()
    var sv_AllProductList: NestedScrollView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_products)
        setIds()
        setListeners()
        setHeaders()
        try {
            when (intent.getStringExtra(AppConstants.proTags)) {
                "1" -> {
                    getShowAllProduct("1")
                    tv_ListTitle!!.text = intent.getStringExtra(AppConstants.proHead)
                }

                "2" -> {
                    getShowAllProduct("2")
                    tv_ListTitle!!.text = intent.getStringExtra(AppConstants.proHead)
                }

                "3" -> {
                    getShowAllProduct("3")
                    tv_ListTitle!!.text = intent.getStringExtra(AppConstants.proHead)
                }

                "4" -> {
                    getShowAllProduct("4")
                    tv_ListTitle!!.text = intent.getStringExtra(AppConstants.proHead)
                }

                "5" -> {
                    getShowAllProduct("5")
                    tv_ListTitle!!.text = intent.getStringExtra(AppConstants.proHead)
                }

                else -> sv_AllProductList!!.visibility = View.GONE
            }
        } catch (e: Exception) {
            e.printStackTrace()
            AppUtils.showToast(this@AllProducts, AppsStrings.defResponse)
            sv_AllProductList!!.visibility = View.GONE
        }
    }

    private fun setHeaders() {
        tv_title!!.text = "All Products"
        iv_search!!.visibility = View.VISIBLE
        rlCart!!.visibility = View.VISIBLE
        tvCart!!.text = prefs!![AppConstants.cartQuantity]
    }

    private fun setIds() {
        prefs = Preferences(this@AllProducts)
        tv_ListTitle = findViewById(R.id.tv_ListTitle)
        tv_title = findViewById(R.id.tv_title)
        rv_AllProducts = findViewById(R.id.rv_AllProducts)
        iv_search = findViewById(R.id.iv_search)
        iv_back = findViewById(R.id.iv_back)
        rlCart = findViewById(R.id.rlCart)
        tvCart = findViewById(R.id.tvCart)
        sv_AllProductList = findViewById(R.id.sv_AllProductList)
    }

    private fun setListeners() {
        iv_search!!.setOnClickListener(this)
        rlCart!!.setOnClickListener(this)
        iv_back!!.setOnClickListener(this)
    }

    private fun setAdapter() {
        rv_AllProducts!!.setHasFixedSize(true)
        rv_AllProducts!!.layoutManager = LinearLayoutManager(this@AllProducts)
        rv_AllProducts!!.isNestedScrollingEnabled = false
        val mLayoutManager: RecyclerView.LayoutManager = GridLayoutManager(this@AllProducts, 2)
        rv_AllProducts!!.layoutManager = mLayoutManager
//        rv_AllProducts!!.addItemDecoration(
//            GridSpacingItemDecoration(
//                1,
//                AppUtils.dpToPx(1, this@AllProducts),
//                true
//            )
//        )
        rv_AllProducts!!.itemAnimator = DefaultItemAnimator()
        val allProductAdapter = AllProductAdapter(arrayAllProduct)
        rv_AllProducts!!.adapter = allProductAdapter
    }

    private fun getShowAllProduct(tagsId: String) {
        showLoading()
        AppsStrings.setApiString("ShowAllProduct")
        val url = AppUrls.ShowAllProduct
        Log.v(AppsStrings.apiUrl, url)
        val json = JSONObject()
        val json_data = JSONObject()
        try {
            if (prefs!![AppConstants.userId].equals("", ignoreCase = true)) json_data.put(
                "user_id",
                "1"
            ) else json_data.put("user_id", prefs!![AppConstants.userId])
            json_data.put("tag", 2)/*  json_data.put("tag", tagsId)*/
            json.put(AppsStrings.BASEJSON, json_data)
            Log.v(AppsStrings.apiJson, json.toString())
        } catch (e: Exception) {
            AppUtils.showToast(this@AllProducts, AppsStrings.defResponse)
            Log.v(AppsStrings.apiErrorException, e.message!!)
            e.printStackTrace()
        }
        AndroidNetworking.post(url)
            .addJSONObjectBody(json_data)
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    parseShowAllProduct(response)
                }

                override fun onError(error: ANError) {
                    sv_AllProductList!!.visibility = View.GONE
                    hideLoading()
                    // handle error
                    if (error.errorCode != 0) {
                        AppUtils.showToast(this@AllProducts, AppsStrings.defResponse)
                        Log.v(AppsStrings.apiErrorCode, "onError errorCode : " + error.errorCode)
                        Log.v(AppsStrings.apiErrorBody, "onError errorBody : " + error.errorBody)
                        Log.v(
                            AppsStrings.apiErrorDetail,
                            "onError errorDetail : " + error.errorDetail
                        )
                    } else {
                        AppUtils.showToast(this@AllProducts, AppsStrings.defResponse)
                        Log.v(AppsStrings.apiErrorException, error.message!!)
                    }
                }
            })
    }

    private fun parseShowAllProduct(response: JSONObject) {
        Log.d(AppsStrings.apiResponse, response.toString())
        try {
            val jsonObject = response.getJSONObject("ecommerce")
            hideLoading()
            if (jsonObject.getString("res_code").equals("1", ignoreCase = true)) {
                arrayAllProduct.clear()
                val jsonArray = jsonObject.getJSONArray("Products")
                try {
                    for (i in 0 until jsonArray.length()) {
                        val hashMap = HashMap<String, String>()
                        hashMap["CartStatus"] = jsonArray.getJSONObject(i).getString("CartStatus")
                        hashMap["Id"] = jsonArray.getJSONObject(i).getString("Id")
                        hashMap["Category"] = jsonArray.getJSONObject(i).getString("Category")
                        hashMap["SKU"] = jsonArray.getJSONObject(i).getString("SKU")
                        hashMap["Color"] = jsonArray.getJSONObject(i).getString("Color")
                        hashMap["Size"] = jsonArray.getJSONObject(i).getString("Size")
                        hashMap["ProductName"] = jsonArray.getJSONObject(i).getString("ProductName")
                        hashMap["RegularPrice"] =
                            jsonArray.getJSONObject(i).getString("RegularPrice")
                        hashMap["MRP"] = jsonArray.getJSONObject(i).getString("MRP")
                        hashMap["ImagePrimary"] =
                            jsonArray.getJSONObject(i).getString("ImagePrimary")
                        hashMap["Tags"] = jsonArray.getJSONObject(i).getString("Tags")
                        hashMap["WishlistStatus"] =
                            jsonArray.getJSONObject(i).getString("WishlistStatus")
                        hashMap["WishlistId"] = jsonArray.getJSONObject(i).getString("WishlistId")
                        hashMap["discount_percentage"] = jsonArray.getJSONObject(i).getString("discount_percentage")
                        arrayAllProduct.add(hashMap)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    AppUtils.showToast(this@AllProducts, AppsStrings.defResponse)
                }
            } else {
                sv_AllProductList!!.visibility = View.GONE
                AppUtils.showToast(this@AllProducts, jsonObject.getString("res_msg"))
            }
        } catch (e: Exception) {
            Log.v(AppsStrings.apiErrorException, e.message!!)
            AppUtils.showToast(this@AllProducts, AppsStrings.defResponse)
            sv_AllProductList!!.visibility = View.GONE
        }
        setAdapter()
    }

    private fun AddWishListApi(
        arrayList: ArrayList<HashMap<String, String>>,
        textView: TextView,
        i: Int
    ) {
        showLoading()
        AppsStrings.setApiString("AddWishList")
        val url = AppUrls.AddWishList
        Log.v(AppsStrings.apiUrl, url)
        val json = JSONObject()
        val json_data = JSONObject()
        try {
            json_data.put("user_id", prefs!![AppConstants.userId])
            json_data.put("product_id", arrayList[i]["Id"])
            json.put(AppsStrings.BASEJSON, json_data)
            Log.v(AppsStrings.apiJson, json.toString())
        } catch (e: Exception) {
            AppUtils.showToast(this@AllProducts, AppsStrings.defResponse)
            Log.v(AppsStrings.apiErrorException, e.message!!)
            e.printStackTrace()
        }
        AndroidNetworking.post(url)
            .addJSONObjectBody(json_data)
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    parseAddWishListApi(response, textView, i, arrayList)
                }

                override fun onError(error: ANError) {
                    hideLoading()
                    // handle error
                    if (error.errorCode != 0) {
                        AppUtils.showToast(this@AllProducts, AppsStrings.defResponse)
                        Log.v(AppsStrings.apiErrorCode, "onError errorCode : " + error.errorCode)
                        Log.v(AppsStrings.apiErrorBody, "onError errorBody : " + error.errorBody)
                        Log.v(
                            AppsStrings.apiErrorDetail,
                            "onError errorDetail : " + error.errorDetail
                        )
                    } else {
                        AppUtils.showToast(this@AllProducts, AppsStrings.defResponse)
                        Log.v(AppsStrings.apiErrorException, error.message!!)
                    }
                }
            })
    }

    private fun parseAddWishListApi(
        response: JSONObject,
        textView: TextView,
        i: Int,
        arrayList: ArrayList<HashMap<String, String>>
    ) {
        Log.d(AppsStrings.apiResponse, response.toString())
        try {
            val jsonObject = response.getJSONObject("ecommerce")
            hideLoading()
            if (jsonObject.getString("res_code").equals("1", ignoreCase = true)) {
                arrayList[i].replace("WishlistStatus", "false", "true")
                textView.setBackgroundResource(R.drawable.ic_wishlist_fill)
            }
            AppUtils.showToast(this@AllProducts, jsonObject.getString("res_msg"))
        } catch (e: Exception) {
            Log.v(AppsStrings.apiErrorException, e.message!!)
            AppUtils.showToast(this@AllProducts, AppsStrings.defResponse)
        }
    }

    private fun removeWishListApi(
        arrayList: ArrayList<HashMap<String, String>>,
        textView: TextView,
        i: Int
    ) {
        showLoading()
        AppsStrings.setApiString("RemoveWishList")
        val url = AppUrls.RemoveWishList
        Log.v(AppsStrings.apiUrl, url)
        val json = JSONObject()
        val json_data = JSONObject()
        try {
//            json_data.put("user_id", prefs.get(AppConstants.userId));
            json_data.put("wishlist_id", arrayList[i]["WishlistId"])
            json.put(AppsStrings.BASEJSON, json_data)
            Log.v(AppsStrings.apiJson, json.toString())
        } catch (e: Exception) {
            AppUtils.showToast(this@AllProducts, AppsStrings.defResponse)
            Log.v(AppsStrings.apiErrorException, e.message!!)
            e.printStackTrace()
        }
        AndroidNetworking.post(url)
            .addJSONObjectBody(json_data)
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    parseRemoveWishListApi(response, textView, i, arrayList)
                }

                override fun onError(error: ANError) {
                    hideLoading()
                    // handle error
                    if (error.errorCode != 0) {
                        AppUtils.showToast(this@AllProducts, AppsStrings.defResponse)
                        Log.v(AppsStrings.apiErrorCode, "onError errorCode : " + error.errorCode)
                        Log.v(AppsStrings.apiErrorBody, "onError errorBody : " + error.errorBody)
                        Log.v(
                            AppsStrings.apiErrorDetail,
                            "onError errorDetail : " + error.errorDetail
                        )
                    } else {
                        AppUtils.showToast(this@AllProducts, AppsStrings.defResponse)
                        Log.v(AppsStrings.apiErrorException, error.message!!)
                    }
                }
            })
    }

    private fun parseRemoveWishListApi(
        response: JSONObject,
        textView: TextView,
        i: Int,
        arrayList: ArrayList<HashMap<String, String>>
    ) {
        Log.d(AppsStrings.apiResponse, response.toString())
        hideLoading()
        try {
            val jsonObject = response.getJSONObject("ecommerce")
            if (jsonObject.getString("res_code").equals("1", ignoreCase = true)) {
                arrayList[i].replace("WishlistStatus", "true", "false")
                textView.setBackgroundResource(R.drawable.ic_whishlist_blank)
            }
            AppUtils.showToast(this@AllProducts, jsonObject.getString("res_msg"))
        } catch (e: Exception) {
            hideLoading()
            Log.v(AppsStrings.apiErrorException, e.message!!)
            AppUtils.showToast(this@AllProducts, AppsStrings.defResponse)
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.iv_search -> startActivity(Intent(this@AllProducts, SearchProducts::class.java))
            R.id.rlCart -> if (prefs!![AppConstants.loginCheck].equals("true", ignoreCase = true)) {
                startActivity(Intent(this@AllProducts, AddToCart::class.java))
            } else {
                startActivity(Intent(this@AllProducts, Login::class.java))
            }

            R.id.iv_back -> onBackPressed()
        }
    }

    private inner class AllProductAdapter(var arrayList1: ArrayList<HashMap<String, String>>) :
        RecyclerView.Adapter<AllProductAdapter.MyViewHolder>() {
        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): MyViewHolder {
            val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.adapter_product, viewGroup, false)
            return MyViewHolder(view)
        }

        override fun onBindViewHolder(myViewHolder: MyViewHolder, i: Int) {
            val imgUrl = arrayList1[i]["ImagePrimary"]
            Log.v("MyProductImages", "a $imgUrl")
            Log.v("MyProductImages", "b $imgUrl")
            AppUtils.setImgPicasso(imgUrl, this@AllProducts, myViewHolder.img_Product)
            myViewHolder.tv_prodTitle.text = arrayList1[i]["ProductName"]
            myViewHolder.tv_proddiscount.setText(arrayList1[myViewHolder.adapterPosition]["discount_percentage"]+"% Off")
            myViewHolder.tv_prodPrice.text = AppUtils.setPrice(arrayList1[i]["RegularPrice"])
            myViewHolder.tv_prodOldPrice.text = AppUtils.setPrice(arrayList1[i]["MRP"])
            myViewHolder.tv_prodOldPrice.paintFlags =
                myViewHolder.tv_prodOldPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            if (arrayList1[i]["WishlistStatus"].equals("true", ignoreCase = true)) {
                myViewHolder.tv_wishlist.setBackgroundResource(R.drawable.ic_wishlist_fill)
            } else {
                myViewHolder.tv_wishlist.setBackgroundResource(R.drawable.ic_whishlist_blank)
            }
            myViewHolder.tv_prodweight.setText(arrayList1[myViewHolder.adapterPosition]["Color"] + " " + arrayList1[myViewHolder.adapterPosition]["Size"])

            myViewHolder.rl_product.setOnClickListener {
                val intent = Intent(this@AllProducts, ProductDetails::class.java)
                intent.putExtra(AppConstants.productId, arrayList1[i]["Id"])
                startActivity(intent)
            }
            myViewHolder.rl_wishlist.setOnClickListener {
                Log.v("Clicked", "WishItem")
                if (prefs!![AppConstants.userId].equals("", ignoreCase = true)) {
                    startActivity(Intent(this@AllProducts, Login::class.java))
                } else if (arrayList1[i]["WishlistStatus"].equals("false", ignoreCase = true)) {
                    AddWishListApi(arrayList1, myViewHolder.tv_wishlist, i)
                } else {
                    removeWishListApi(arrayList1, myViewHolder.tv_wishlist, i)
                }
            }


            if (arrayList1[myViewHolder.adapterPosition]["CartStatus"] == "false") {
                myViewHolder.addcart_btn!!.visibility = View.VISIBLE
                myViewHolder.buy_btn!!.visibility = View.GONE
            } else {
                myViewHolder.addcart_btn!!.visibility = View.GONE
                myViewHolder.buy_btn!!.visibility = View.VISIBLE
            }


            myViewHolder.addcart_btn?.setOnClickListener(View.OnClickListener {
                Log.v("Clicked", "WishItem")
                if (prefs!![AppConstants.userId].equals("", ignoreCase = true)) {
                    startActivity(Intent(mActivity, Login::class.java))
                } else {
                    myViewHolder.addcart_btn!!.visibility = View.GONE
                    myViewHolder.buy_btn!!.visibility = View.VISIBLE
                    arrayList1[myViewHolder.adapterPosition]["Id"]?.let { it1 -> AddToCardApi(it1) }
                }
            })
            myViewHolder. buy_btn?.setOnClickListener (View.OnClickListener {
                Log.v("Clicked", "WishItem")
                startActivity(Intent(mActivity, AddToCart::class.java))
            })

        }

        override fun getItemCount(): Int {
            return arrayList1.size
        }

        inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var img_Product: ImageView
            var rl_wishlist: RelativeLayout
            var rl_product: RelativeLayout
            var tv_prodTitle: TextView
            var tv_prodPrice: TextView
            var tv_prodOldPrice: TextView
            var tv_wishlist: TextView
            var  tv_prodweight: TextView
            var  tv_proddiscount: TextView
            var addcart_btn: Button? = null
            var buy_btn:Button? = null
            init {
                img_Product = itemView.findViewById(R.id.img_Product)
                rl_wishlist = itemView.findViewById(R.id.rl_wishlist)
                rl_product = itemView.findViewById(R.id.rl_product)
                tv_prodTitle = itemView.findViewById(R.id.tv_prodTitle)
                tv_prodPrice = itemView.findViewById(R.id.tv_prodPrice)
                tv_prodOldPrice = itemView.findViewById(R.id.tv_prodOldPrice)
                 tv_prodweight= itemView.findViewById(R.id.tv_prodweight)
                tv_proddiscount= itemView.findViewById(R.id.tv_proddiscount)
                tv_wishlist = itemView.findViewById(R.id.tv_wishlist)
                addcart_btn = itemView.findViewById(R.id.addcart_btn)
                buy_btn = itemView.findViewById(R.id.buy_btn)
            }
        }
    }


    private fun AddToCardApi(prodId: String) {
        showLoading()
        AppsStrings.setApiString("AddToCard")
        val url = AppUrls.AddToCard
        Log.v(AppsStrings.apiUrl, url)
        val json = JSONObject()
        val json_data = JSONObject()
        try {
            json_data.put("user_id", prefs!![AppConstants.userId])
            json_data.put("product_id", prodId)
            json.put(AppsStrings.BASEJSON, json_data)
            Log.v(AppsStrings.apiJson, json.toString())
        } catch (e: java.lang.Exception) {
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
                    parseAddToCardApi(response)
                }

                override fun onError(error: ANError) {
                    hideLoading()
                    // handle error
                    if (error.errorCode != 0) {
                        AppUtils.showToast(mActivity, AppsStrings.defResponse)
                        Log.v(AppsStrings.apiErrorCode, "onError errorCode : " + error.errorCode)
                        Log.v(AppsStrings.apiErrorBody, "onError errorBody : " + error.errorBody)
                        Log.v(
                            AppsStrings.apiErrorDetail,
                            "onError errorDetail : " + error.errorDetail
                        )
                    } else {
                        AppUtils.showToast(mActivity, AppsStrings.defResponse)
                        Log.v(AppsStrings.apiErrorException, error.message!!)
                    }
                }
            })
    }

    private fun parseAddToCardApi(response: JSONObject) {
        Log.d(AppsStrings.apiResponse, response.toString())
        try {
            val jsonObject = response.getJSONObject("ecommerce")
            hideLoading()
            if (jsonObject.getString("res_code").equals("1", ignoreCase = true)) {

                // cartQuantity = "1";
                prefs!!.set(
                    AppConstants.cartQuantity,
                    AppUtils.updateQuantity(prefs!![AppConstants.cartQuantity], 1)
                ).commit()
                /* tv_cartQuantity.setText(cartQuantity);
                tvCart.setText(prefs.get(AppConstants.cartQuantity));*/
            }
            AppUtils.showToast(mActivity, jsonObject.getString("res_msg"))
        } catch (e: java.lang.Exception) {
            Log.v("res_msg", "res_msg")
            Log.v(AppsStrings.apiErrorException, e.message!!)
            AppUtils.showToast(mActivity, AppsStrings.defResponse)
        }
    }
}