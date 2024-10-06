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
import android.widget.Toast
import androidx.appcompat.widget.SearchView
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
import com.dukeKart.android.common.GridSpacingItemDecoration
import com.dukeKart.android.database.AppConstants
import com.dukeKart.android.database.Preferences
import com.dukeKart.android.views.BaseActivity
import org.json.JSONObject

class SearchProducts : BaseActivity(), View.OnClickListener {
    var rv_search: RecyclerView? = null
    var itemCount = 0
    var arrayListProduct = ArrayList<HashMap<String, String>>()
    var layoutManager: LinearLayoutManager? = null
    var scrollView: NestedScrollView? = null
    var rl_progressBar: RelativeLayout? = null
    var rlCart: RelativeLayout? = null
    var searchAdapter: SearchAdapter? = null
    var categoryId: String? = ""
    var bannerUrl: String? = ""
    var searchtext = ""
    var iv_back: ImageView? = null
    var iv_search: ImageView? = null
    var tv_title: TextView? = null
    var tvCart: TextView? = null
    var prefs: Preferences? = null
    var search_view: SearchView? = null
    private var isLast = false
    private var isLoading = false
    private var pageIndex = "0"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_products)
        setIds()
        search_view!!.setIconifiedByDefault(true)
        search_view!!.setFocusable(true)
        search_view!!.setIconified(false)

        setListeners()
        setAdapters()
        try {
            setHeaders()
//            categoryId = intent.getStringExtra(AppConstants.categoryId)
//            bannerUrl = intent.getStringExtra(AppConstants.categoryBanner)
//            searchProductApi
        } catch (e: Exception) {
            AppUtils.showToast(this@SearchProducts, AppsStrings.defResponse)
            e.printStackTrace()
        }
    }

    private fun setHeaders() {
        tv_title!!.text = "Search Product"
        iv_search!!.visibility = View.GONE
        rlCart!!.visibility = View.VISIBLE
        tvCart!!.text = prefs!![AppConstants.cartQuantity]
    }

    private fun setIds() {
        prefs = Preferences(this@SearchProducts)
        rv_search = findViewById(R.id.rv_search)
        scrollView = findViewById(R.id.scrollView)
        rl_progressBar = findViewById(R.id.rl_progressBar)
        rlCart = findViewById(R.id.rlCart)
        iv_back = findViewById(R.id.iv_back)
        iv_search = findViewById(R.id.iv_search)
        tv_title = findViewById(R.id.tv_title)
        tvCart = findViewById(R.id.tvCart)
        search_view = findViewById(R.id.search_view)
        }

    private fun setListeners() {
        // use a linear layout manager
        layoutManager = LinearLayoutManager(this)
        iv_back!!.setOnClickListener(this)
        iv_search!!.setOnClickListener(this)
        rlCart!!.setOnClickListener(this)
        // perform set on query text listener event
        search_view!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                // do something on text submit
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                // do something when text changes
                searchtext = newText
                pageIndex = "0"
                isLast = false
                arrayListProduct.clear()
                searchProductApi
                return false
            }
        })
        rv_search!!.viewTreeObserver.addOnScrollChangedListener {
            //                int scrollY = rootScrollView.getScrollY(); // For ScrollView
//                int scrollX = rootScrollView.getScrollX(); // For HorizontalScrollView
//                // DO SOMETHING WITH THE SCROLL COORDINATES
            if (scrollView!!.getChildAt(0).bottom <= scrollView!!.height + scrollView!!.scrollY) {
                //scroll view is at bottom
                Log.v("Pager", "Scrolling Not")
                if (isLoading == false && isLast == false) {
                    searchProductApi
                }
            } else {
                Log.v("Pager", "Scrolling")
            }
        }
    }

    // handle error
    private val searchProductApi: Unit
        private get() {
            rl_progressBar!!.visibility = View.VISIBLE
            isLoading = true
            AppsStrings.setApiString("SearchProduct")
            val url = AppUrls.SearchData
            Log.v(AppsStrings.apiUrl, url)
            val json = JSONObject()
            val json_data = JSONObject()
            try {
                if (prefs!![AppConstants.userId].equals(
                        "",
                        ignoreCase = true
                    )
                ) json_data.put("user_id", "1") else json_data.put(
                    "user_id",
                    prefs!![AppConstants.userId]
                )
                json_data.put("keyword", searchtext)
                json_data.put("pageindex", pageIndex)
                json.put(AppsStrings.BASEJSON, json_data)
                Log.v(AppsStrings.apiJson, json.toString())
            } catch (e: Exception) {
                AppUtils.showToast(this@SearchProducts, AppsStrings.defResponse)
                Log.v(AppsStrings.apiErrorException, e.message!!)
                e.printStackTrace()
            }
            AndroidNetworking.post(url)
                    .addJSONObjectBody(json_data)
                    .setPriority(Priority.HIGH)
                    .build()
                    .getAsJSONObject(object : JSONObjectRequestListener {
                        override fun onResponse(response: JSONObject) {
                            parseSearchProductApi(response)
                        }

                        override fun onError(error: ANError) {
                            isLoading = false
                            // handle error
                            if (error.errorCode != 0) {
                                AppUtils.showToast(this@SearchProducts, AppsStrings.defResponse)
                                Log.v(AppsStrings.apiErrorCode, "onError errorCode : " + error.errorCode)
                                Log.v(AppsStrings.apiErrorBody, "onError errorBody : " + error.errorBody)
                                Log.v(AppsStrings.apiErrorDetail, "onError errorDetail : " + error.errorDetail)
                            } else {
                                AppUtils.showToast(this@SearchProducts, AppsStrings.defResponse)
                                Log.v(AppsStrings.apiErrorException, error.message!!)
                            }
                        }
                    })
        }

    private fun parseSearchProductApi(response: JSONObject) {
        isLoading = false

        Log.d(AppsStrings.apiResponse, response.toString())
        try {
            val jsonObject = response.getJSONObject("ecommerce")
            //            hideLoading();
            if (jsonObject.getString("res_code").equals("1", ignoreCase = true)) {
                if (pageIndex.equals("0", ignoreCase = true)) arrayListProduct.clear()
                pageIndex = jsonObject.getString("pageindex")
                val productList = jsonObject.getJSONArray("SearchProductList")
                if (productList.length() < 5) {
                    rl_progressBar!!.visibility = View.GONE
                } else {
                    rl_progressBar!!.visibility = View.VISIBLE
                }
                for (i in 0 until productList.length()) {
                    val hashProduct = HashMap<String, String>()
                    hashProduct["CartStatus"] = productList.getJSONObject(i).getString("CartStatus")
                    hashProduct["Id"] = productList.getJSONObject(i).getString("Id")
                    hashProduct["Category"] = productList.getJSONObject(i).getString("Category")
                    hashProduct["SKU"] = productList.getJSONObject(i).getString("SKU")
                    hashProduct["Color"] = productList.getJSONObject(i).getString("Color")
                    hashProduct["Size"] = productList.getJSONObject(i).getString("Size")
                    hashProduct["ProductName"] = productList.getJSONObject(i).getString("ProductName")
                    hashProduct["RegularPrice"] = productList.getJSONObject(i).getString("RegularPrice")
                    hashProduct["MRP"] = productList.getJSONObject(i).getString("MRP")
                    hashProduct["ImagePrimary"] = productList.getJSONObject(i).getString("ImagePrimary")
                    hashProduct["WishlistStatus"] = productList.getJSONObject(i).getString("WishlistStatus")
                    hashProduct["WishlistId"] = productList.getJSONObject(i).getString("WishlistId")
                    hashProduct["discount_percentage"] = productList.getJSONObject(i).getString("discount_percentage")
                    arrayListProduct.add(hashProduct)
                }
            } else {
                isLast = true
                if (pageIndex.equals("0", ignoreCase = true)) AppUtils.showToast(this@SearchProducts, jsonObject.getString("res_msg"))
                rl_progressBar!!.visibility = View.GONE
            }
            searchAdapter!!.notifyDataSetChanged()
        } catch (e: Exception) {
//            Log.v(AppsStrings.apiErrorException, e.message!!)
//            AppUtils.showToast(this@SearchProducts, AppsStrings.defResponse)
            Toast.makeText(this@SearchProducts, AppsStrings.defResponse, Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun setAdapters() {
        rv_search!!.setHasFixedSize(false)
        rv_search!!.layoutManager = layoutManager
        val mLayoutManager: RecyclerView.LayoutManager = GridLayoutManager(this@SearchProducts, 2)
        rv_search!!.layoutManager = mLayoutManager
        rv_search!!.addItemDecoration(GridSpacingItemDecoration(1, AppUtils.dpToPx(1, this@SearchProducts), true))
        rv_search!!.itemAnimator = DefaultItemAnimator()
        searchAdapter = SearchAdapter(arrayListProduct)
        rv_search!!.adapter = searchAdapter
    }

    private fun AddWishListApi(arrayList: ArrayList<HashMap<String, String>>, textView: TextView, i: Int) {
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
            AppUtils.showToast(this@SearchProducts, AppsStrings.defResponse)
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
                            AppUtils.showToast(this@SearchProducts, AppsStrings.defResponse)
                            Log.v(AppsStrings.apiErrorCode, "onError errorCode : " + error.errorCode)
                            Log.v(AppsStrings.apiErrorBody, "onError errorBody : " + error.errorBody)
                            Log.v(AppsStrings.apiErrorDetail, "onError errorDetail : " + error.errorDetail)
                        } else {
                            AppUtils.showToast(this@SearchProducts, AppsStrings.defResponse)
                            Log.v(AppsStrings.apiErrorException, error.message!!)
                        }
                    }
                })
    }

    private fun parseAddWishListApi(response: JSONObject, textView: TextView, i: Int, arrayList: ArrayList<HashMap<String, String>>) {
        Log.d(AppsStrings.apiResponse, response.toString())
        try {
            val jsonObject = response.getJSONObject("ecommerce")
            hideLoading()
            if (jsonObject.getString("res_code").equals("1", ignoreCase = true)) {
                arrayList[i].replace("WishlistStatus", "false", "true")
                textView.setBackgroundResource(R.drawable.ic_wishlist_fill)
            }
            AppUtils.showToast(this@SearchProducts, jsonObject.getString("res_msg"))
        } catch (e: Exception) {
            Log.v(AppsStrings.apiErrorException, e.message!!)
            AppUtils.showToast(this@SearchProducts, AppsStrings.defResponse)
        }
    }

    private fun removeWishListApi(arrayList: ArrayList<HashMap<String, String>>, textView: TextView, i: Int) {
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
            AppUtils.showToast(this@SearchProducts, AppsStrings.defResponse)
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
                            AppUtils.showToast(this@SearchProducts, AppsStrings.defResponse)
                            Log.v(AppsStrings.apiErrorCode, "onError errorCode : " + error.errorCode)
                            Log.v(AppsStrings.apiErrorBody, "onError errorBody : " + error.errorBody)
                            Log.v(AppsStrings.apiErrorDetail, "onError errorDetail : " + error.errorDetail)
                        } else {
                            AppUtils.showToast(this@SearchProducts, AppsStrings.defResponse)
                            Log.v(AppsStrings.apiErrorException, error.message!!)
                        }
                    }
                })
    }

    private fun parseRemoveWishListApi(response: JSONObject, textView: TextView, i: Int, arrayList: ArrayList<HashMap<String, String>>) {
        Log.d(AppsStrings.apiResponse, response.toString())
        hideLoading()
        try {
            val jsonObject = response.getJSONObject("ecommerce")
            if (jsonObject.getString("res_code").equals("1", ignoreCase = true)) {
                arrayList[i].replace("WishlistStatus", "true", "false")
                textView.setBackgroundResource(R.drawable.ic_whishlist_blank)
            }
            AppUtils.showToast(this@SearchProducts, jsonObject.getString("res_msg"))
        } catch (e: Exception) {
            hideLoading()
            Log.v(AppsStrings.apiErrorException, e.message!!)
            AppUtils.showToast(this@SearchProducts, AppsStrings.defResponse)
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.iv_back -> onBackPressed()
            R.id.iv_search -> AppUtils.showToast(this@SearchProducts, "Coming Soon")
            R.id.rlCart -> if (prefs!![AppConstants.loginCheck].equals("true", ignoreCase = true)) {
                startActivity(Intent(this@SearchProducts, AddToCart::class.java))
            } else {
                startActivity(Intent(this@SearchProducts, Login::class.java))
            }
        }
    }

    inner class SearchAdapter(var arrayList1: ArrayList<HashMap<String, String>>) : RecyclerView.Adapter<SearchAdapter.MyViewHolder>() {
        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): MyViewHolder {
            val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.adapter_product, viewGroup, false)
            return MyViewHolder(view)
        }

        override fun onBindViewHolder(myViewHolder: MyViewHolder, i: Int) {
            val imgUrl = arrayList1[i]["ImagePrimary"]
            Log.v("MyProductImages", "a $imgUrl")
            Log.v("MyProductImages", "b $imgUrl")
            AppUtils.setImgPicasso(imgUrl, this@SearchProducts, myViewHolder.img_Product)
            myViewHolder.tv_prodTitle.text = arrayList1[i]["ProductName"]
            myViewHolder.tv_prodPrice.text = AppUtils.setPrice(arrayList1[i]["RegularPrice"])
            myViewHolder.tv_prodweight.setText(arrayList1[myViewHolder.adapterPosition]["Color"] + " " + arrayList1[myViewHolder.adapterPosition]["Size"])
            myViewHolder.tv_proddiscount.setText(arrayList1[myViewHolder.adapterPosition]["discount_percentage"]+"% Off")
            myViewHolder.tv_prodOldPrice.text = AppUtils.setPrice(arrayList1[i]["MRP"])
            myViewHolder.tv_prodOldPrice.paintFlags = myViewHolder.tv_prodOldPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            if (arrayList1[i]["WishlistStatus"].equals("true", ignoreCase = true)) {
                myViewHolder.tv_wishlist.setBackgroundResource(R.drawable.ic_wishlist_fill)
            } else {
                myViewHolder.tv_wishlist.setBackgroundResource(R.drawable.ic_whishlist_blank)
            }
            myViewHolder.rl_product.setOnClickListener {
                val intent = Intent(this@SearchProducts, ProductDetails::class.java)
                intent.putExtra(AppConstants.productId, arrayList1[i]["Id"])
                startActivity(intent)
            }
            myViewHolder.rl_wishlist.setOnClickListener {
                Log.v("Clicked", "WishItem")
                if (prefs!![AppConstants.userId].equals("", ignoreCase = true)) {
                    startActivity(Intent(this@SearchProducts, Login::class.java))
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

            myViewHolder.addcart_btn!!.setOnClickListener(View.OnClickListener {
                Log.v("Clicked", "WishItem")
                if (prefs!![AppConstants.userId].equals("", ignoreCase = true)) {
                    startActivity(Intent(mActivity, Login::class.java))
                } else {
                    myViewHolder.addcart_btn!!.visibility = View.GONE
                    myViewHolder.buy_btn!!.visibility = View.VISIBLE
                    arrayList1[i]["Id"]?.let { it1 -> AddToCardApi(it1) }
                }
            })
            myViewHolder.buy_btn!!.setOnClickListener (View.OnClickListener {
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
            var tv_prodweight: TextView
            var tv_prodOldPrice: TextView
            var tv_wishlist: TextView
            var tv_proddiscount: TextView
            var buy_btn: Button? = null
            var addcart_btn: Button? = null
            init {
                img_Product = itemView.findViewById(R.id.img_Product)
                rl_wishlist = itemView.findViewById(R.id.rl_wishlist)
                rl_product = itemView.findViewById(R.id.rl_product)
                tv_prodTitle = itemView.findViewById(R.id.tv_prodTitle)
                tv_prodPrice = itemView.findViewById(R.id.tv_prodPrice)
                tv_prodweight = itemView.findViewById(R.id.tv_prodweight)
                tv_prodOldPrice = itemView.findViewById(R.id.tv_prodOldPrice)
                tv_wishlist = itemView.findViewById(R.id.tv_wishlist)
                buy_btn = itemView.findViewById<Button>(R.id.buy_btn)
                tv_proddiscount = itemView.findViewById<TextView>(R.id.tv_proddiscount)

                addcart_btn = itemView.findViewById<Button>(R.id.addcart_btn)
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
                            Log.v(
                                AppsStrings.apiErrorCode,
                                "onError errorCode : " + error.errorCode
                            )
                            Log.v(
                                AppsStrings.apiErrorBody,
                                "onError errorBody : " + error.errorBody
                            )
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
}