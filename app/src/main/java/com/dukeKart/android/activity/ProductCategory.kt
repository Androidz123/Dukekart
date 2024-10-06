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
import de.hdodenhof.circleimageview.CircleImageView
import org.json.JSONObject
import java.util.*

class ProductCategory : BaseActivity(), View.OnClickListener {
    var rv_Category: RecyclerView? = null
    var itemCount = 0
    var arrayListProduct = ArrayList<HashMap<String, String>>()
    var layoutManager: LinearLayoutManager? = null
    var rlCart: RelativeLayout? = null
    var categoryAdapter: CategoryAdapter? = null
    var categoryId: String? = ""
    var bannerUrl: String? = ""
    var iv_back: ImageView? = null
    var img_ProductBanner: ImageView? = null
    var iv_search: ImageView? = null
    var tvCart: TextView? = null
    var tv_title: TextView? = null
    var prefs: Preferences? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_category)
        setIds()
        setListeners()
        setAdapters()
        try {
            setHeaders()
            categoryId = intent.getStringExtra(AppConstants.categoryId)
            bannerUrl = intent.getStringExtra(AppConstants.categoryBanner)
            productListApi
        } catch (e: Exception) {
            AppUtils.showToast(mActivity, AppsStrings.defResponse)
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
        prefs = Preferences(mActivity)
        rv_Category = findViewById(R.id.rv_Category)
        tvCart = findViewById(R.id.tvCart)
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
    }// handle error

    //        http://shopmet.com/api/GetSubCategory
    private val productListApi: Unit
        private get() {
//        http://shopmet.com/api/GetSubCategory
            AppsStrings.setApiString("GetSubCategory")
            val url = AppUrls.GetSubCategory
            Log.v(AppsStrings.apiUrl, url)
            val json = JSONObject()
            val json_data = JSONObject()
            try {
                json_data.put("category_id", categoryId)
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
                            parseProductListApi(response)
                        }

                        override fun onError(error: ANError) {

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

    private fun parseProductListApi(response: JSONObject) {
        Log.d(AppsStrings.apiResponse, response.toString())
        try {
            val jsonObject = response.getJSONObject("ecommerce")
            //            hideLoading();
            if (jsonObject.getString("res_code").equals("1", ignoreCase = true)) {
//                arrayListProduct.clear();
                val productList = jsonObject.getJSONArray("SubCategory")
                for (i in 0 until productList.length()) {
                    val hashProduct = HashMap<String, String>()
                    hashProduct["Id"] = productList.getJSONObject(i).getString("Id")
                    hashProduct["Name"] = productList.getJSONObject(i).getString("Name")
                    hashProduct["Thumb"] = productList.getJSONObject(i).getString("Thumb")
                    arrayListProduct.add(hashProduct)
                    categoryAdapter!!.notifyDataSetChanged()
                }
            } else {
                AppUtils.showToast(mActivity, jsonObject.getString("res_msg"))
            }
        } catch (e: Exception) {
            Log.v(AppsStrings.apiErrorException, e.message!!)
            AppUtils.showToast(mActivity, AppsStrings.defResponse)
        }
    }

    private fun setAdapters() {
        rv_Category!!.setHasFixedSize(false)
        rv_Category!!.layoutManager = layoutManager
        val mLayoutManager: RecyclerView.LayoutManager = GridLayoutManager(mActivity, 3)
        rv_Category!!.layoutManager = mLayoutManager
        rv_Category!!.addItemDecoration(GridSpacingItemDecoration(1, AppUtils.dpToPx(1, mActivity), true))
        rv_Category!!.itemAnimator = DefaultItemAnimator()
        categoryAdapter = CategoryAdapter(arrayListProduct)
        rv_Category!!.adapter = categoryAdapter
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.iv_back -> onBackPressed()
            R.id.iv_search -> startActivity(Intent(mActivity, SearchProducts::class.java))
            R.id.rlCart -> if (prefs!![AppConstants.loginCheck].equals("true", ignoreCase = true)) {
                startActivity(Intent(mActivity, AddToCart::class.java))
            } else {
                startActivity(Intent(mActivity, Login::class.java))
            }
        }
    }

    override fun onBackPressed() {
        //startActivity(Intent(mActivity, Dashboard::class.java))
        finish()
    }

    inner class CategoryAdapter(var arrayList1: ArrayList<HashMap<String, String>>) : RecyclerView.Adapter<CategoryAdapter.MyViewHolder>() {
        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): MyViewHolder {
            val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.adapter_subcategory, viewGroup, false)
            return MyViewHolder(view)
        }

        override fun onBindViewHolder(myViewHolder: MyViewHolder, i: Int) {
            val imgUrl = arrayList1[i]["Thumb"]
            Log.v("MyCategImages", "a $imgUrl")
            AppUtils.setImgPicasso(imgUrl, mActivity, myViewHolder.circularImageView)
            myViewHolder.tv_proTitle.text = arrayList1[i]["Name"]
            myViewHolder.rl_product.setOnClickListener {
                val intent = Intent(mActivity, SubProductCategory::class.java)
                intent.putExtra(AppConstants.subcategory_id, arrayList1[i]["Id"])
                intent.putExtra(AppConstants.categoryName, arrayList1[i]["Name"])
                startActivity(intent)
            }
        }

        override fun getItemCount(): Int {
            return arrayList1.size
        }

        inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var circularImageView: CircleImageView
            var rl_product: RelativeLayout
            var tv_proTitle: TextView

            init {
                circularImageView = itemView.findViewById(R.id.circularImageView)
                rl_product = itemView.findViewById(R.id.rl_product)
                tv_proTitle = itemView.findViewById(R.id.tv_proTitle)
            }
        }
    }
}