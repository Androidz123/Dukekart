package com.dukeKart.android.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.bumptech.glide.Glide
import com.dukeKart.android.R
import com.dukeKart.android.common.AppUtils
import com.dukeKart.android.common.AppsStrings
import com.dukeKart.android.common.GridSpacingItemDecoration
import com.dukeKart.android.database.AppConstants
import com.dukeKart.android.database.Preferences
import com.dukeKart.android.views.BaseActivity
import org.json.JSONObject

class SubSubProductCategory : BaseActivity(), View.OnClickListener {
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
    //private var pageIndex = "0"
    var subcategory_id: String? = ""
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


            productListApi
        } catch (e: Exception) {
            AppUtils.showToast(this@SubSubProductCategory, AppsStrings.defResponse)
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
        prefs = Preferences(this@SubSubProductCategory)
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
            val url = "https://dukekart.in/api/GetCategory"
            Log.v(AppsStrings.apiUrl, url)
            val json = JSONObject()
            val json_data = JSONObject()
            try {

//            json_data.put("user_id", prefs.get(AppConstants.userId));
                json_data.put("parent_category_id", "2")
                json.put("Shhopon", json_data)
                Log.v(AppsStrings.apiJson, json.toString())
            } catch (e: Exception) {
                AppUtils.showToast(this@SubSubProductCategory, AppsStrings.defResponse)
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
                            AppUtils.showToast(this@SubSubProductCategory, AppsStrings.defResponse)
                            Log.v(AppsStrings.apiErrorCode, "onError errorCode : " + error.errorCode)
                            Log.v(AppsStrings.apiErrorBody, "onError errorBody : " + error.errorBody)
                            Log.v(AppsStrings.apiErrorDetail, "onError errorDetail : " + error.errorDetail)
                        } else {
                            AppUtils.showToast(this@SubSubProductCategory, AppsStrings.defResponse)
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
            arrayListProduct.clear()
            if (jsonObject.getString("res_code").equals("1", ignoreCase = true)) {
                //if (pageIndex.equals("0", ignoreCase = true)) //
                // pageIndex = jsonObject.getString("pageindex")
                val productList = jsonObject.getJSONArray("Category")
                for (i in 0 until productList.length()) {
                    val hashProduct = HashMap<String, String>()
                    hashProduct["Id"] = productList.getJSONObject(i).getString("Id")

                    hashProduct["Name"] = productList.getJSONObject(i).getString("Name")

                    hashProduct["Thumb"] = productList.getJSONObject(i).getString("Thumb")

                    arrayListProduct.add(hashProduct)
                }
                categoryAdapter!!.notifyDataSetChanged()
            } else {
                isLast = true
                /*if (pageIndex.equals("0", ignoreCase = true))
                    Toast.makeText(this@SubSubProductCategory, jsonObject.getString("res_msg"),Toast.LENGTH_LONG)
                        .show()*/
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
        val mLayoutManager: RecyclerView.LayoutManager = GridLayoutManager(this@SubSubProductCategory, 2)
        rv_Category!!.layoutManager = mLayoutManager
        rv_Category!!.addItemDecoration(GridSpacingItemDecoration(1, AppUtils.dpToPx(1, this@SubSubProductCategory), true))
        rv_Category!!.itemAnimator = DefaultItemAnimator()
        categoryAdapter = CategoryAdapter(arrayListProduct)
        rv_Category!!.adapter = categoryAdapter
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.iv_back -> onBackPressed()
            R.id.iv_search -> startActivity(Intent(this@SubSubProductCategory, SearchProducts::class.java))
            R.id.rlCart -> /*if (prefs!![AppConstants.loginCheck].equals("true", ignoreCase = true)) {*/
                startActivity(Intent(this@SubSubProductCategory, AddToCart::class.java))
            /* } else {
                 startActivity(Intent(this@SubSubProductCategory, Login::class.java))
             }*/
        }
    }

    override fun onBackPressed() {
        // startActivity(Intent(this@SubProductCategory, Dashboard::class.java))
        finish()
    }

    inner class CategoryAdapter(var arrayList1: ArrayList<HashMap<String, String>>) : RecyclerView.Adapter<CategoryAdapter.MyViewHolder>() {
        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): MyViewHolder {
            val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.adapter_subsubcategory, viewGroup, false)
            return MyViewHolder(view)
        }

        override fun onBindViewHolder(myViewHolder: MyViewHolder, i: Int) {
            var imgUrl = arrayList1[myViewHolder.adapterPosition]["Thumb"]
            if (imgUrl != null) {
                if (imgUrl.contains("dl=0"))
                    imgUrl = imgUrl.replace("dl=0", "raw=1")
            }
            Log.d("TestApi",imgUrl!!.substring(10,imgUrl!!.length))
            AppUtils.setImgPicasso("https://dukekart.in//assets/product_images/"+imgUrl, this@SubSubProductCategory, myViewHolder.circularImageView)
            Glide.with(myViewHolder.circularImageView.context)
                .load(imgUrl)
                .placeholder(R.mipmap.placeholder)
//                .apply(RequestOptions.bitmapTransform(RoundedCorners(20)))
                .into(myViewHolder.circularImageView)
            myViewHolder.tv_proTitle.text = arrayList1[myViewHolder.adapterPosition]["Name"]
            // myViewHolder.tv_prodPrice.text = AppUtils.setPrice(arrayList1[myViewHolder.adapterPosition]["MRP"])
            myViewHolder.rl_product.setOnClickListener {
                val intent = Intent(mActivity, SubProductCategory::class.java)
                intent.putExtra(
                    AppConstants.subsubcategory_id,
                    arrayList1[myViewHolder.adapterPosition]["Id"]
                )
                intent.putExtra(
                    AppConstants.categoryName,
                    arrayList1[myViewHolder.adapterPosition]["Name"]
                )
                intent.putExtra(AppConstants.categoryBanner, imgUrl)
                startActivity(intent)
            }
        }

        override fun getItemCount(): Int {
            return arrayList1.size
        }

        inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var circularImageView: ImageView
            var rl_product: RelativeLayout
            var tv_proTitle: TextView
            /*  var tv_prodPrice: TextView*/

            init {
                circularImageView = itemView.findViewById(R.id.circularImageView)
                rl_product = itemView.findViewById(R.id.rl_product)
                tv_proTitle = itemView.findViewById(R.id.tv_proTitle)
                /* tv_prodPrice = itemView.findViewById(R.id.tv_prodPrice)*/
            }
        }
    }
}