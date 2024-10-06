package com.dukeKart.android.activity

import android.app.Dialog
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.widget.*
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
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
import com.dukeKart.android.fragment.Home
import com.dukeKart.android.fragment.Wishlist
import com.dukeKart.android.views.BaseActivity
import de.hdodenhof.circleimageview.CircleImageView
import org.json.JSONObject

class Dashboard : BaseActivity(), View.OnClickListener {
    var mDrawerLayout: DrawerLayout? = null
    var svSideMenu: ScrollView? = null
    var ll_Home: LinearLayout? = null
    var ll_wishlist: LinearLayout? = null
    var ll_OrderHistory: LinearLayout? = null
    var ll_Account: LinearLayout? = null
    var ll_DrawerMenu: LinearLayout? = null
    var ll_sideHome: LinearLayout? = null
    var ll_sideWishlist: LinearLayout? = null
    var ll_sideLogin: LinearLayout? = null
    var ll_sideOrderhistory: LinearLayout? = null
    var ll_categories: LinearLayout? = null
    var ll_sideAccount: LinearLayout? = null
    var ll_contactus: LinearLayout? = null
    var ll_share: LinearLayout? = null
    var ll_rate: LinearLayout? = null
    var ll_Logout: LinearLayout? = null
    var iv_Home: ImageView? = null
    var iv_wishlist: ImageView? = null
    var iv_OrderHistory: ImageView? = null
    var iv_Account: ImageView? = null
    var iv_search: ImageView? = null
    var tv_Home: TextView? = null
    var tv_wishlist: TextView? = null
    var tv_OrderHistory: TextView? = null
    var tv_Account: TextView? = null
    var tvCart: TextView? = null
    var tv_userName: TextView? = null
    var view_login: View? = null
    var view_Account: View? = null
    var view_logout: View? = null
    var prefs: Preferences? = null
    var isLoggedIn: Boolean? = null
    var mHandler: Handler? = null
    var userPhotoImageView: CircleImageView? = null
    var rlCart: RelativeLayout? = null
    private var backFrag = 0
    private var fragment: Fragment? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        setIds()
        setListeners()
        displayView(1)
    }

    private fun setUi() {

        if (prefs!![AppConstants.cartQuantity].isEmpty()) {
            prefs!![AppConstants.cartQuantity] = "0"
            prefs!!.commit()
            tvCart!!.text = prefs!![AppConstants.cartQuantity]
        }else{
            tvCart!!.text = prefs!![AppConstants.cartQuantity]
        }

        if (prefs!![AppConstants.loginCheck].equals("true", ignoreCase = true)) {
            isLoggedIn = true
            ll_sideLogin!!.visibility = View.GONE
            view_login!!.visibility = View.GONE
            ll_sideAccount!!.visibility = View.VISIBLE
            view_Account!!.visibility = View.VISIBLE
            ll_Logout!!.visibility = View.VISIBLE
            view_logout!!.visibility = View.VISIBLE
            AppUtils.setImgPicasso(prefs!![AppConstants.userProfPic], this@Dashboard, userPhotoImageView)
            tv_userName!!.text = prefs!![AppConstants.userName]
        } else {
            isLoggedIn = false
            ll_sideLogin!!.visibility = View.VISIBLE
            view_login!!.visibility = View.VISIBLE
            ll_sideAccount!!.visibility = View.GONE
            view_Account!!.visibility = View.GONE
            ll_Logout!!.visibility = View.GONE
            view_logout!!.visibility = View.GONE
        }
    }

    private fun setIds() {
        prefs = Preferences(this@Dashboard)
        mHandler = Handler()
        ll_Home = findViewById(R.id.ll_Home)
        tv_userName = findViewById(R.id.tv_userName)
        ll_wishlist = findViewById(R.id.ll_wishlist)
        ll_OrderHistory = findViewById(R.id.ll_OrderHistory)
        ll_Account = findViewById(R.id.ll_Account)
        iv_Home = findViewById(R.id.iv_Home)
        iv_wishlist = findViewById(R.id.iv_wishlist)
        iv_OrderHistory = findViewById(R.id.iv_OrderHistory)
        iv_Account = findViewById(R.id.iv_Account)
        tv_Home = findViewById(R.id.tv_Home)
        tv_wishlist = findViewById(R.id.tv_wishlist)
        tv_OrderHistory = findViewById(R.id.tv_OrderHistory)
        tv_Account = findViewById(R.id.tv_Account)
        ll_DrawerMenu = findViewById(R.id.ll_DrawerMenu)
        userPhotoImageView = findViewById(R.id.userPhotoImageView)
        mDrawerLayout = findViewById(R.id.mDrawerLayout)
        svSideMenu = findViewById(R.id.svSideMenu)
        ll_sideHome = findViewById(R.id.ll_sideHome)
        ll_sideLogin = findViewById(R.id.ll_sideLogin)
        ll_sideWishlist = findViewById(R.id.ll_sideWishlist)
        ll_sideOrderhistory = findViewById(R.id.ll_sideOrderhistory)
        ll_categories = findViewById(R.id.ll_categories)
        ll_sideAccount = findViewById(R.id.ll_sideAccount)
        view_login = findViewById(R.id.view_login)
        view_Account = findViewById(R.id.view_Account)
        view_logout = findViewById(R.id.view_logout)
        ll_contactus = findViewById(R.id.ll_contactus)
        ll_share = findViewById(R.id.ll_share)
        ll_rate = findViewById(R.id.ll_rate)
        ll_Logout = findViewById(R.id.ll_Logout)
        iv_search = findViewById(R.id.iv_search)
        rlCart = findViewById(R.id.rlCart)
        tvCart = findViewById(R.id.tvCart)
    }

    private fun setListeners() {
        ll_Home!!.setOnClickListener(this)
        ll_wishlist!!.setOnClickListener(this)
        ll_OrderHistory!!.setOnClickListener(this)
        ll_Account!!.setOnClickListener(this)
        ll_DrawerMenu!!.setOnClickListener(this)
        iv_search!!.setOnClickListener(this)
        rlCart!!.setOnClickListener(this)
        ll_sideHome!!.setOnClickListener(this)
        ll_sideLogin!!.setOnClickListener(this)
        ll_sideWishlist!!.setOnClickListener(this)
        ll_sideOrderhistory!!.setOnClickListener(this)
        ll_sideAccount!!.setOnClickListener(this)
        ll_categories!!.setOnClickListener(this)
        ll_contactus!!.setOnClickListener(this)
        ll_share!!.setOnClickListener(this)
        ll_rate!!.setOnClickListener(this)
        ll_Logout!!.setOnClickListener(this)
    }

    //openDrawer
    fun openDrawer() {
        mDrawerLayout!!.openDrawer(svSideMenu!!)
    }

    //closeDrawer
    fun closeDrawer() {
        mDrawerLayout!!.closeDrawer(svSideMenu!!)
    }

    fun displayView(position: Int) {
        when (position) {
            1 -> {
                backFrag = 0
                setDefaultNav(iv_Home, tv_Home)
                fragment = Home()
            }
            2 -> {
                backFrag = 1
                setDefaultNav(iv_wishlist, tv_wishlist)
                fragment = Wishlist()
            }
            else -> Toast.makeText(this@Dashboard, "Comming Soon", Toast.LENGTH_LONG).show()
        }
        if (fragment != null) {
            AppUtils.hideSoftKeyboard(this@Dashboard)
            val fragmentManager = supportFragmentManager

            //  fragmentManager.popBackStack(fragment.toString(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
            val tx = fragmentManager.beginTransaction()

            // tx.add(R.id.container,fragment).addToBackStack(fragment.toString());
            tx.replace(R.id.frameDash, fragment!!)
            tx.commit()
            // ====to clear unused memory==
            System.gc()
        } else {
            // error in creating fragment
            Log.e("ImageDataActivity", "Error in creating fragment")
        }
    }

    fun setDefaultNav(imageView: ImageView?, textView: TextView?) {
        iv_Home!!.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.dark_grey))
        iv_wishlist!!.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.dark_grey))
        iv_OrderHistory!!.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.dark_grey))
        iv_Account!!.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.dark_grey))
        tv_Home!!.setTextColor(ColorStateList.valueOf(resources.getColor(R.color.dark_grey)))
        tv_wishlist!!.setTextColor(ColorStateList.valueOf(resources.getColor(R.color.dark_grey)))
        tv_OrderHistory!!.setTextColor(ColorStateList.valueOf(resources.getColor(R.color.dark_grey)))
        tv_Account!!.setTextColor(ColorStateList.valueOf(resources.getColor(R.color.dark_grey)))
        imageView!!.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.colorPrimary))
        textView!!.setTextColor(ColorStateList.valueOf(resources.getColor(R.color.colorPrimary)))
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.ll_DrawerMenu -> openDrawer()
            R.id.ll_Logout -> {
                closeDrawer()
                exitLogout()
            }
            R.id.ll_categories -> {
                closeDrawer()
                startActivity(Intent(this@Dashboard, AllCategory::class.java))
            }
            R.id.ll_share -> {
                closeDrawer()
                AppUtils.shareApplication(this@Dashboard)
            }
            R.id.ll_rate -> {
                closeDrawer()
                AppUtils.rateApplication(this@Dashboard)
            }
            R.id.ll_contactus -> {
                closeDrawer()
                AppUtils.shareToBrowser(this@Dashboard, "http://shhopon.com/contact-us")
            }
            R.id.ll_sideHome -> {
                closeDrawer()
                displayView(1)
            }
            R.id.ll_Home -> displayView(1)
            R.id.ll_sideWishlist -> {
                closeDrawer()
                if (isLoggedIn!!) {
                    setDefaultNav(iv_wishlist, tv_wishlist)
                    displayView(2)
                } else {
                    startActivity(Intent(this@Dashboard, Login::class.java))
                }
            }
            R.id.ll_wishlist -> if (isLoggedIn!!) {
                setDefaultNav(iv_wishlist, tv_wishlist)
                displayView(2)
            } else {
                startActivity(Intent(this@Dashboard, Login::class.java))
            }
            R.id.ll_sideOrderhistory -> {
                closeDrawer()
                if (isLoggedIn!!) {
                    setDefaultNav(iv_OrderHistory, tv_OrderHistory)
                    startActivity(Intent(this@Dashboard, OrderList::class.java))
                } else {
                    startActivity(Intent(this@Dashboard, Login::class.java))
                }
            }
            R.id.ll_OrderHistory -> if (isLoggedIn!!) {
                setDefaultNav(iv_OrderHistory, tv_OrderHistory)
                startActivity(Intent(this@Dashboard, OrderList::class.java))
            } else {
                startActivity(Intent(this@Dashboard, Login::class.java))
            }
            R.id.ll_sideLogin -> {
                closeDrawer()
                startActivity(Intent(this@Dashboard, Login::class.java))
            }
            R.id.iv_search -> startActivity(Intent(this@Dashboard, SearchProducts::class.java))
            R.id.ll_Account, R.id.ll_sideAccount -> if (isLoggedIn!!) {
                setDefaultNav(iv_Account, tv_Account)
                startActivity(Intent(this@Dashboard, Account::class.java))
            } else {
                startActivity(Intent(this@Dashboard, Login::class.java))
            }
            R.id.rlCart -> if (isLoggedIn!!) {
                startActivity(Intent(this@Dashboard, AddToCart::class.java))
            } else {
                startActivity(Intent(this@Dashboard, Login::class.java))
            }
        }
    }

    fun exitLogout() {
        val dialog = Dialog(this@Dashboard)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.confirm_popup)
        dialog.setCancelable(false)
        val window = dialog.window
        val wlp = window!!.attributes
        wlp.gravity = Gravity.CENTER
        wlp.flags = wlp.flags and WindowManager.LayoutParams.FLAG_BLUR_BEHIND.inv()
        window.attributes = wlp
        if (!this@Dashboard.isFinishing) {
            dialog.show()
            //show dialog
        } else {
            displayView(1)
        }
        val rrSubmit = dialog.findViewById<RelativeLayout>(R.id.rr_submit)
        val rr_lg_cancel = dialog.findViewById<RelativeLayout>(R.id.rr_lg_cancel)
        rrSubmit.setOnClickListener {
            LogoutApi()
            dialog.dismiss()
        }
        val rrCancel = dialog.findViewById<RelativeLayout>(R.id.rr_cancel)
        rr_lg_cancel.setOnClickListener { dialog.dismiss() }
        rrCancel.setOnClickListener { dialog.dismiss() }
    }
    private fun LogoutApi() {
        showLoading()
        AppsStrings.setApiString("Logout")
        val url = AppUrls.Logout
        Log.v(AppsStrings.apiUrl, url)
        val json = JSONObject()
        val json_data = JSONObject()
        try {
            json_data.put("user_id", prefs!![AppConstants.userId])
            json_data.put("device_id", AppUtils.getDeviceID(this@Dashboard))
            json.put(AppsStrings.BASEJSON, json_data)
            Log.v(AppsStrings.apiJson, json.toString())
        } catch (e: Exception) {
            AppUtils.showToast(this@Dashboard, AppsStrings.defResponse)
            Log.v(AppsStrings.apiErrorException, e.message!!)
            e.printStackTrace()
        }
        AndroidNetworking.post(url)
                .addJSONObjectBody(json_data)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        parseLogoutApi(response)
                    }

                    override fun onError(error: ANError) {
                        hideLoading()
                        // handle error
                        if (error.errorCode != 0) {
                            AppUtils.showToast(this@Dashboard, AppsStrings.defResponse)
                            Log.v(AppsStrings.apiErrorCode, "onError errorCode : " + error.errorCode)
                            Log.v(AppsStrings.apiErrorBody, "onError errorBody : " + error.errorBody)
                            Log.v(AppsStrings.apiErrorDetail, "onError errorDetail : " + error.errorDetail)
                        } else {
                            AppUtils.showToast(this@Dashboard, AppsStrings.defResponse)
                            Log.v(AppsStrings.apiErrorException, error.message!!)
                        }
                    }
                })
    }
    private fun parseLogoutApi(response: JSONObject) {
        Log.d(AppsStrings.apiResponse, response.toString())
        try {
            val jsonObject = response.getJSONObject("ecommerce")
            hideLoading()
            if (jsonObject.getString("res_code").equals("1", ignoreCase = true)) {
                prefs!![AppConstants.userProfPic] = ""
                prefs!![AppConstants.userAlternateNumber] = ""
                prefs!![AppConstants.userState] = ""
                prefs!![AppConstants.userCity] = ""
                prefs!![AppConstants.userPincode] = ""
                prefs!![AppConstants.userLocality] = ""
                prefs!![AppConstants.userWhatsapNumber] = ""
                prefs!![AppConstants.userMobile] = ""
                prefs!![AppConstants.userEmail] = ""
                prefs!![AppConstants.userName] = ""
                prefs!![AppConstants.userId] = ""
                prefs!![AppConstants.userAddress] = ""
                prefs!![AppConstants.userWalletAmount] = ""
                prefs!![AppConstants.loginCheck] = ""
                prefs!![AppConstants.loginId] = ""
                prefs!![AppConstants.cartSubTotal] = ""
                prefs!![AppConstants.cartQuantity] = "0"
                prefs!![AppConstants.cartSubTotal] = ""
                prefs!![AppConstants.cartShipping] = ""
                prefs!![AppConstants.cartTotal] = ""
                prefs!![AppConstants.cartPayable] = ""
                prefs!![AppConstants.cartDiscount] = ""
                prefs!![AppConstants.primaryAddress] = ""
                prefs!![AppConstants.primaryAddressId] = ""
                prefs!![AppConstants.primaryAddressTitle] = ""
                prefs!!.commit()
                startActivity(Intent(this@Dashboard, Login::class.java))
                finish()
            }
            AppUtils.showToast(this@Dashboard, jsonObject.getString("res_msg"))
        } catch (e: Exception) {
            Log.v(AppsStrings.apiErrorException, e.message!!)
            AppUtils.showToast(this@Dashboard, AppsStrings.defResponse)
        }
    }
    override fun onBackPressed() {
        if (backFrag == 1) {
            displayView(1)
        } else AppUtils.onBackPressed(this@Dashboard)
    }

    override fun onPostResume() {
        super.onPostResume()
        setUi()
    }
}