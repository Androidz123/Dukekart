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

class AllCategory : BaseActivity(), View.OnClickListener {
    var rv_Category: RecyclerView? = null
    var arrayMenListProduct = ArrayList<HashMap<String, String>>()
    var arrayWomenListProduct = ArrayList<HashMap<String, String>>()
    var arrayListProduct = ArrayList<HashMap<String, String>>()
    var layoutManager: LinearLayoutManager? = null
    var rlCart: RelativeLayout? = null
    var categoryAdapter: CategoryAdapter? = null
    var iv_back: ImageView? = null
    var iv_search: ImageView? = null
    var tvCart: TextView? = null
    var tv_title: TextView? = null
    var prefs: Preferences? = null
    
    var view_Women: View? = null
    var view_men: View? = null
    var rl_Men: RelativeLayout? = null
    var rl_Women: RelativeLayout? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_category)
        setIds()
        setListeners()
        setAdapters()
        try {
            setHeaders()
            getAllCategoryApi
        } catch (e: Exception) {
            AppUtils.showToast(mActivity, AppsStrings.defResponse)
            e.printStackTrace()
        }
    }

    private fun setHeaders() {
        tv_title!!.text = "All Categories"
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
        tv_title = findViewById(R.id.tv_title)
        tvCart = findViewById(R.id.tvCart)
        view_Women = findViewById(R.id.view_Women)
        view_men = findViewById(R.id.view_men)
        rl_Men = findViewById(R.id.rl_Men)
        rl_Women = findViewById(R.id.rl_Women)
    }

    private fun setListeners() {
        // use a linear layout manager
        layoutManager = LinearLayoutManager(this)
        iv_back!!.setOnClickListener(this)
        iv_search!!.setOnClickListener(this)
        rlCart!!.setOnClickListener(this)
        rl_Men!!.setOnClickListener(this)
        rl_Women!!.setOnClickListener(this)
    }

    // handle error
    private val getAllCategoryApi: Unit
        private get() {
            showLoading()
            AppsStrings.setApiString("GetAllCategory")
            val url = AppUrls.GetAllCategory
            Log.v(AppsStrings.apiUrl, url)
            AndroidNetworking.get(url)
                    .setPriority(Priority.HIGH)
                    .build()
                    .getAsJSONObject(object : JSONObjectRequestListener {
                        override fun onResponse(response: JSONObject) {
                            parseGetAllCategoryApi(response)
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

    private fun parseGetAllCategoryApi(response: JSONObject) {
        hideLoading()
        Log.d(AppsStrings.apiResponse, response.toString())
        try {
            val jsonObject = response.getJSONObject("ecommerce")
            //            hideLoading();
            if (jsonObject.getString("res_code").equals("1", ignoreCase = true)) {
                arrayMenListProduct.clear()
                arrayWomenListProduct.clear()
                val getAllCategory = jsonObject.getJSONArray("MaincategoryList")
                for (j in 0 until getAllCategory.length()) {
                    if (getAllCategory.getJSONObject(j).getString("Name").equals("Men", ignoreCase = true)) {
                        val getSubCategory = getAllCategory.getJSONObject(j).getJSONArray("categoryList")
                        for (i in 0 until getSubCategory.length()) {
                            val categoryValue = HashMap<String, String>()
                            categoryValue["Id"] = getSubCategory.getJSONObject(i).getString("Id")
                            categoryValue["Name"] = getSubCategory.getJSONObject(i).getString("Name")
                            categoryValue["Thumb"] = getSubCategory.getJSONObject(i).getString("Thumb")
                            arrayMenListProduct.add(categoryValue)
                        }
                    }
                    if (getAllCategory.getJSONObject(j).getString("Name").equals("Women", ignoreCase = true)) {
                        val getSubCategory = getAllCategory.getJSONObject(j).getJSONArray("categoryList")
                        for (i in 0 until getSubCategory.length()) {
                            val categoryValue = HashMap<String, String>()
                            categoryValue["Id"] = getSubCategory.getJSONObject(i).getString("Id")
                            categoryValue["Name"] = getSubCategory.getJSONObject(i).getString("Name")
                            categoryValue["Thumb"] = getSubCategory.getJSONObject(i).getString("Thumb")
                            arrayWomenListProduct.add(categoryValue)
                        }
                    }
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
        view_men!!.visibility = View.INVISIBLE
        view_Women!!.visibility = View.INVISIBLE
        categoryAdapter = CategoryAdapter(arrayListProduct)
        rv_Category!!.adapter = categoryAdapter
    }

    fun fillAdapter(type: String) {
        view_Women!!.visibility = View.INVISIBLE
        view_men!!.visibility = View.INVISIBLE
        if (type.equals("Men", ignoreCase = true)) {
            arrayListProduct = arrayMenListProduct
            view_men!!.visibility = View.VISIBLE
        } else {
            view_Women!!.visibility = View.VISIBLE
            arrayListProduct = arrayWomenListProduct
        }
        categoryAdapter = CategoryAdapter(arrayListProduct)
        rv_Category!!.adapter = categoryAdapter
        categoryAdapter!!.notifyDataSetChanged()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.rl_Men -> fillAdapter("men")
            R.id.rl_Women -> fillAdapter("women")
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
        startActivity(Intent(mActivity, Dashboard::class.java))
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
                val intent = Intent(mActivity, ProductCategory::class.java)
                intent.putExtra(AppConstants.categoryId, arrayList1[i]["Id"])
                intent.putExtra(AppConstants.categoryName, arrayList1[i]["Name"])
                intent.putExtra(AppConstants.categoryBanner, imgUrl)
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