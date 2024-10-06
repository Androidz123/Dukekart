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
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.bumptech.glide.Glide
import com.dukeKart.android.R
import com.dukeKart.android.common.AppUrls
import com.dukeKart.android.common.AppUtils
import com.dukeKart.android.common.AppsStrings
import com.dukeKart.android.common.GridSpacingItemDecoration
import com.dukeKart.android.database.AppConstants
import com.dukeKart.android.database.Preferences
import com.dukeKart.android.views.BaseActivity
import org.json.JSONObject

class SubProductCategory : BaseActivity(), View.OnClickListener {
    private var isLast = false
    private var isLoading = false
    var rv_Category: RecyclerView? = null
    var itemCount = 0
    var arrayListProduct = ArrayList<HashMap<String, String>>()
    var layoutManager: LinearLayoutManager? = null
    var scrollView: NestedScrollView? = null
    var rl_progressBar: RelativeLayout? = null
    var rlCart: RelativeLayout? = null
    var categoryAdapter: CategoryAdapter? = null
    private var pageIndex = "0"
    var subcategory_id: String? = null
    var subsubcategory_id: String? = null
    var iv_back: ImageView? = null
    var img_ProductBanner: ImageView? = null
    var iv_search: ImageView? = null
    var tvCart: TextView? = null
    var tv_title: TextView? = null
    var prefs: Preferences? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sub_product_category)
        setIds()
        setListeners()
        setAdapters()
        try {
            setHeaders()
            subcategory_id = intent.getStringExtra(AppConstants.subcategory_id)
            subsubcategory_id = intent.getStringExtra(AppConstants.subsubcategory_id)
            //            bannerUrl=getIntent().getStringExtra(AppConstants.categoryBanner);
            productListApi
        } catch (e: Exception) {
            AppUtils.showToast(this@SubProductCategory, AppsStrings.defResponse)
            e.printStackTrace()
        }
    }

    private fun setHeaders() {
        tv_title!!.text = intent.getStringExtra(AppConstants.categoryName)
        iv_search!!.visibility = View.VISIBLE
        rlCart!!.visibility = View.VISIBLE
        tvCart!!.text = prefs!![AppConstants.cartQuantity]
    }

    private fun setIds() {
        prefs = Preferences(this@SubProductCategory)
        rv_Category = findViewById(R.id.rv_Category)
        tvCart = findViewById(R.id.tvCart)
        scrollView = findViewById(R.id.scrollView)
        rl_progressBar = findViewById(R.id.rl_progressBar)
        rlCart = findViewById(R.id.rlCart)
        iv_back = findViewById(R.id.iv_back)
        iv_search = findViewById(R.id.iv_search)
        img_ProductBanner = findViewById(R.id.img_ProductBanner)
        tv_title = findViewById(R.id.tv_title)
        tvCart = findViewById(R.id.tvCart)
    }

    private fun setListeners() {
        // use a linear layout manager
        layoutManager = LinearLayoutManager(this)
        iv_back!!.setOnClickListener(this)
        iv_search!!.setOnClickListener(this)
        rlCart!!.setOnClickListener(this)
        rv_Category!!.viewTreeObserver.addOnScrollChangedListener {
            //                int scrollY = rootScrollView.getScrollY(); // For ScrollView
//                int scrollX = rootScrollView.getScrollX(); // For HorizontalScrollView
//                // DO SOMETHING WITH THE SCROLL COORDINATES
            if (scrollView!!.getChildAt(0).bottom <= scrollView!!.height + scrollView!!.scrollY) {
                //scroll view is at bottom
                Log.v("Pager", "Scrolling Not")
                if (isLoading == false && isLast == false) {
                    productListApi
                }
            } else {
                Log.v("Pager", "Scrolling")
            }
        }
    }

    // handle error
    private val productListApi: Unit
        private get() {
            rl_progressBar!!.visibility = View.VISIBLE
            isLoading = true
            AppsStrings.setApiString("SubProductList")
            // val url = AppUrls.SubProductList
            var url=""
            if(subsubcategory_id?.isEmpty() == false) {
                url = "https://dukekart.in/api/CategoryProductList"
            }else

                url = "https://dukekart.in/api/ParentCategoryProductList"
            Log.v(AppsStrings.apiUrl, url)
            val json = JSONObject()
            val json_data = JSONObject()
            try {
                if(subsubcategory_id?.isEmpty() == false) {
                    json_data.put("category_id", subsubcategory_id)
                }else
                    json_data.put("parent_category_id", subcategory_id)
                json_data.put("pageindex", pageIndex)
                if(subsubcategory_id?.isEmpty() == false) {

                }else
                    if (prefs!![AppConstants.userId].equals("", ignoreCase = true  ))
                        json_data.put("user_id", "1")
                    else json_data.put("user_id",prefs!![AppConstants.userId] )
                json.put("Shhopon", json_data)
                Log.v(AppsStrings.apiJson, json.toString())
            } catch (e: Exception) {
                AppUtils.showToast(this@SubProductCategory, AppsStrings.defResponse)
                Log.v(AppsStrings.apiErrorException, e.message!!)
                e.printStackTrace()
            }
            AndroidNetworking.post(url)
                .addJSONObjectBody(json_data)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        parseProductListApi(response)
                    }

                    override fun onError(error: ANError) {

                        // handle error
                        if (error.errorCode != 0) {
                            AppUtils.showToast(this@SubProductCategory, AppsStrings.defResponse)
                            Log.v(AppsStrings.apiErrorCode, "onError errorCode : " + error.errorCode)
                            Log.v(AppsStrings.apiErrorBody, "onError errorBody : " + error.errorBody)
                            Log.v(AppsStrings.apiErrorDetail, "onError errorDetail : " + error.errorDetail)
                        } else {
                            AppUtils.showToast(this@SubProductCategory, AppsStrings.defResponse)
                            Log.v(AppsStrings.apiErrorException, error.message!!)
                        }
                    }
                })
        }

    private fun parseProductListApi(response: JSONObject) {
        isLoading = false
        Log.d(AppsStrings.apiResponse, response.toString())
        try {
            val jsonObject = response.getJSONObject("ecommerce")
            //            hideLoading();
            if (jsonObject.getString("res_code").equals("1", ignoreCase = true)) {
                //if (pageIndex.equals("0", ignoreCase = true)) //
                pageIndex = jsonObject.getString("pageindex")
                val productList = jsonObject.getJSONArray("ProductList")
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
                    hashProduct["discount_percentage"] = productList.getJSONObject(i).getString("discount_percentage")
                    arrayListProduct.add(hashProduct)
                }
                categoryAdapter!!.notifyDataSetChanged()
            } else {
                isLast = true
                if (pageIndex.equals("0", ignoreCase = true))
                    Toast.makeText(this@SubProductCategory, jsonObject.getString("res_msg"),Toast.LENGTH_LONG)
                        .show()
                rl_progressBar!!.visibility = View.GONE
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("TestApp", e.message!!)
//            AppUtils.showToast(this@SubProductCategory, AppsStrings.defResponse)
        }
    }

    private fun setAdapters() {
        rv_Category!!.setHasFixedSize(false)
        rv_Category!!.layoutManager = layoutManager
        val mLayoutManager: RecyclerView.LayoutManager = GridLayoutManager(this@SubProductCategory, 2)
        rv_Category!!.layoutManager = mLayoutManager
        rv_Category!!.addItemDecoration(GridSpacingItemDecoration(1, AppUtils.dpToPx(1, this@SubProductCategory), true))
        rv_Category!!.itemAnimator = DefaultItemAnimator()
        categoryAdapter = CategoryAdapter(arrayListProduct)
        rv_Category!!.adapter = categoryAdapter
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.iv_back -> onBackPressed()
            R.id.iv_search -> startActivity(Intent(this@SubProductCategory, SearchProducts::class.java))
            R.id.rlCart -> /*if (prefs!![AppConstants.loginCheck].equals("true", ignoreCase = true)) {*/
                startActivity(Intent(this@SubProductCategory, AddToCart::class.java))
            /*  } else {
                  startActivity(Intent(this@SubProductCategory, Login::class.java))
              }*/
        }
    }

    override fun onBackPressed() {
        // startActivity(Intent(this@SubProductCategory, Dashboard::class.java))
        finish()
    }

    inner class CategoryAdapter(var arrayList1: ArrayList<HashMap<String, String>>) : RecyclerView.Adapter<CategoryAdapter.MyViewHolder>() {
        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): MyViewHolder {
            val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.adapter_subcategory, viewGroup, false)
            return MyViewHolder(view)
        }

        override fun onBindViewHolder(myViewHolder: MyViewHolder, i: Int) {
            var imgUrl = arrayList1[myViewHolder.adapterPosition]["ImagePrimary"]
            if (imgUrl != null) {
                if (imgUrl.contains("dl=0"))
                    imgUrl = imgUrl.replace("dl=0", "raw=1")
            }
            Log.d("TestApi",imgUrl!!.substring(10,imgUrl!!.length))
            AppUtils.setImgPicasso(imgUrl, this@SubProductCategory, myViewHolder.circularImageView)
            Glide.with(myViewHolder.circularImageView.context)
                .load(imgUrl)
                .placeholder(R.mipmap.placeholder)
//                .apply(RequestOptions.bitmapTransform(RoundedCorners(20)))
                .into(myViewHolder.circularImageView)
            myViewHolder.tv_prodweight.setText(arrayList1[myViewHolder.adapterPosition]["Color"] + " " + arrayList1[myViewHolder.adapterPosition]["Size"])
            myViewHolder.tv_proTitle.text = arrayList1[myViewHolder.adapterPosition]["ProductName"]
            myViewHolder.tv_prodPrice.text = AppUtils.setPrice(arrayList1[myViewHolder.adapterPosition]["RegularPrice"])
            myViewHolder.tv_proddiscount.setText(arrayList1[myViewHolder.adapterPosition]["discount_percentage"] + "% Off")

            myViewHolder.tv_prodOldPrice.setText(AppUtils.setPrice(arrayList1[myViewHolder.adapterPosition]["MRP"]))
            myViewHolder.tv_prodOldPrice.setPaintFlags(myViewHolder.tv_prodOldPrice.getPaintFlags() or Paint.STRIKE_THRU_TEXT_FLAG)


            myViewHolder.rl_product.setOnClickListener {
                val intent = Intent(this@SubProductCategory, ProductDetails::class.java)
                intent.putExtra(AppConstants.productId, arrayList1[myViewHolder.adapterPosition]["Id"])
                startActivity(intent)
            }

            /* if (arrayList1[myViewHolder.adapterPosition]["CartStatus"] == "false") {*/
            myViewHolder.addcart_btn!!.visibility = View.VISIBLE
            /*  myViewHolder.buy_btn!!.visibility = View.GONE
          } else {
              myViewHolder.addcart_btn!!.visibility = View.GONE
              myViewHolder.buy_btn!!.visibility = View.VISIBLE
          }*/



            myViewHolder.addcart_btn!!.setOnClickListener {
                Log.v("Clicked", "WishItem")
                /*if (prefs!![AppConstants.userId].equals("", ignoreCase = true)) {
                    startActivity(Intent(mActivity, Login::class.java))
                } else {*/
                myViewHolder.addcart_btn!!.visibility = View.VISIBLE
                // myViewHolder.buy_btn!!.visibility = View.VISIBLE
                arrayList1[myViewHolder.adapterPosition]["Id"]?.let { it1 -> AddToCardApi(it1) }
                /* }*/
            }
            /* myViewHolder.buy_btn!!.setOnClickListener (View.OnClickListener {
                 Log.v("Clicked", "WishItem")
                 startActivity(Intent(mActivity, AddToCart::class.java))
             })*/
        }

        override fun getItemCount(): Int {
            return arrayList1.size
        }

        inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var circularImageView: ImageView
            var rl_product: LinearLayout
            var tv_proTitle: TextView
            var tv_prodweight: TextView
            var tv_prodPrice: TextView
            var tv_prodOldPrice: TextView
            var  tv_proddiscount: TextView
            var addcart_btn: Button? = null
            var buy_btn:Button? = null
            init {
                circularImageView = itemView.findViewById(R.id.circularImageView)
                rl_product = itemView.findViewById(R.id.rl_product)
                tv_prodweight = itemView.findViewById(R.id.tv_prodweight)
                tv_proTitle = itemView.findViewById(R.id.tv_proTitle)
                tv_proddiscount = itemView.findViewById(R.id.tv_proddiscount)
                tv_prodPrice = itemView.findViewById(R.id.tv_prodPrice)
                tv_prodOldPrice = itemView.findViewById(R.id.tv_prodOldPrice)
                addcart_btn = itemView.findViewById<Button>(R.id.addcart_btn)
                buy_btn = itemView.findViewById<Button>(R.id.buy_btn)
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