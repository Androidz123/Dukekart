package com.dukeKart.android.fragment

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.dukeKart.android.R
import com.dukeKart.android.activity.Account
import com.dukeKart.android.activity.AddToCart
import com.dukeKart.android.activity.AllCategory
import com.dukeKart.android.activity.Dashboard
import com.dukeKart.android.activity.Login
import com.dukeKart.android.activity.OrderList
import com.dukeKart.android.activity.SearchProducts
import com.dukeKart.android.common.AppUrls
import com.dukeKart.android.common.AppUtils
import com.dukeKart.android.common.AppsStrings
import com.dukeKart.android.database.AppConstants
import com.dukeKart.android.database.Preferences
import com.dukeKart.android.views.BaseFragment
import de.hdodenhof.circleimageview.CircleImageView
import org.json.JSONObject

class Settings : BaseFragment(), View.OnClickListener {
    var svSideMenu: ScrollView? = null
   //var ll_Home: LinearLayout? = null
   //var ll_wishlist: LinearLayout? = null
   //var ll_OrderHistory: LinearLayout? = null
   //var ll_Account: LinearLayout? = null
   //var ll_DrawerMenu: LinearLayout? = null
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
    //var iv_search: ImageView? = null
    var tv_Home: TextView? = null
    //var tv_wishlist: TextView? = null
    var tv_OrderHistory: TextView? = null
    var tv_Account: TextView? = null

    var tv_userName: TextView? = null
    var view_login: View? = null
    var view_Account: View? = null
    var view_logout: View? = null
    var prefs: Preferences? = null
    var isLoggedIn: Boolean? = null
    var mHandler: Handler? = null
    var userPhotoImageView: CircleImageView? = null

    private var backFrag = 0
    private var fragment: Fragment? = null
    private var view: View? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        view = inflater.inflate(R.layout.settings_frag, container, false)
        setIDs()
        setListeners()
        return view
    }

    fun setIDs() {
        prefs = Preferences(mActivity)
        mHandler = Handler()
       // ll_Home = view?.findViewById(R.id.ll_Home)
        tv_userName = view?.findViewById(R.id.tv_userName)
        //ll_wishlist = view?.findViewById(R.id.ll_wishlist)
       // ll_OrderHistory = view?.findViewById(R.id.ll_OrderHistory)
       // ll_Account = view?.findViewById(R.id.ll_Account)
       // iv_Home = view?.findViewById(R.id.iv_Home)
       // iv_wishlist = view?.findViewById(R.id.iv_wishlist)
       // iv_OrderHistory = view?.findViewById(R.id.iv_OrderHistory)
       // iv_Account = view?.findViewById(R.id.iv_Account)
       // tv_Home = view?.findViewById(R.id.tv_Home)
       // tv_wishlist = view?.findViewById(R.id.tv_wishlist)
       // tv_OrderHistory = view?.findViewById(R.id.tv_OrderHistory)
       // tv_Account = view?.findViewById(R.id.tv_Account)
      //  ll_DrawerMenu = view?.findViewById(R.id.ll_DrawerMenu)
        //userPhotoImageView = view?.findViewById(R.id.userPhotoImageView)

        svSideMenu = view?.findViewById(R.id.svSideMenu)
        ll_sideHome = view?.findViewById(R.id.ll_sideHome)
        ll_sideLogin = view?.findViewById(R.id.ll_sideLogin)
        ll_sideWishlist = view?.findViewById(R.id.ll_sideWishlist)
        ll_sideOrderhistory = view?.findViewById(R.id.ll_sideOrderhistory)
        ll_categories = view?.findViewById(R.id.ll_categories)
        ll_categories!!.visibility = View.GONE
        view_login = view?.findViewById(R.id.view_login)
        view_Account = view?.findViewById(R.id.view_Account)
        view_logout = view?.findViewById(R.id.view_logout)
        ll_contactus = view?.findViewById(R.id.ll_contactus)
        ll_share = view?.findViewById(R.id.ll_share)
        ll_rate = view?.findViewById(R.id.ll_rate)
        ll_Logout = view?.findViewById(R.id.ll_Logout)
      //  iv_search = view?.findViewById(R.id.iv_search)

    }

    fun setListeners() {
       // ll_Home!!.setOnClickListener(this)
       // ll_wishlist!!.setOnClickListener(this)
       // ll_OrderHistory!!.setOnClickListener(this)
       // ll_Account!!.setOnClickListener(this)
       // ll_DrawerMenu!!.setOnClickListener(this)
        //iv_search!!.setOnClickListener(this)
     
        ll_sideHome!!.setOnClickListener(this)
        ll_sideLogin!!.setOnClickListener(this)
        ll_sideWishlist!!.setOnClickListener(this)
        ll_sideOrderhistory!!.setOnClickListener(this)
       // ll_sideAccount!!.setOnClickListener(this)
        ll_categories!!.setOnClickListener(this)
        ll_contactus!!.setOnClickListener(this)
        ll_share!!.setOnClickListener(this)
        ll_rate!!.setOnClickListener(this)
        ll_Logout!!.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when (view.id) {

            R.id.ll_Logout -> {

                exitLogout()
            }
            R.id.ll_categories -> {

                startActivity(Intent(mActivity, AllCategory::class.java))
            }
            R.id.ll_share -> {

                AppUtils.shareApplication(mActivity)
            }
            R.id.ll_rate -> {

                AppUtils.rateApplication(mActivity)
            }
            R.id.ll_contactus -> {

                AppUtils.shareToBrowser(mActivity, "https://dukekart.in/contact-us")
            }
            R.id.ll_sideHome -> {
                startActivity(Intent(mActivity, Dashboard::class.java))

            }
            //R.id.ll_Home -> displayView(1)
            R.id.ll_sideWishlist -> {

                if (isLoggedIn!!) {
                    startActivity(Intent(mActivity, Wishlist::class.java))
                } else {
                    startActivity(Intent(mActivity, Login::class.java))
                }
            }
           /* R.id.ll_wishlist -> if (isLoggedIn!!) {


            } else {
                startActivity(Intent(mActivity, Login::class.java))
            }*/
            R.id.ll_sideOrderhistory -> {

                if (isLoggedIn!!) {

                    startActivity(Intent(mActivity, OrderList::class.java))
                } else {
                    startActivity(Intent(mActivity, Login::class.java))
                }
            }
            /*R.id.ll_OrderHistory -> if (isLoggedIn!!) {

                startActivity(Intent(mActivity, OrderList::class.java))
            } else {
                startActivity(Intent(mActivity, Login::class.java))
            }*/
            R.id.ll_sideLogin -> {

                startActivity(Intent(mActivity, Login::class.java))
            }
          //  R.id.iv_search -> startActivity(Intent(mActivity, SearchProducts::class.java))
         /*   R.id.ll_Account,-> if (isLoggedIn!!) {

                startActivity(Intent(mActivity, Account::class.java))
            } else {
                startActivity(Intent(mActivity, Login::class.java))
            }*/
           /* R.id.rlCart -> if (isLoggedIn!!) {
                startActivity(Intent(mActivity, AddToCart::class.java))
            } else {
                startActivity(Intent(mActivity, Login::class.java))
            }*/
        }
    }

    fun exitLogout() {
        val dialog = Dialog(mActivity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.confirm_popup)
        dialog.setCancelable(false)
        val window = dialog.window
        val wlp = window!!.attributes
        wlp.gravity = Gravity.CENTER
        wlp.flags = wlp.flags and WindowManager.LayoutParams.FLAG_BLUR_BEHIND.inv()
        window.attributes = wlp
        if (!mActivity.isFinishing) {
            dialog.show()
            //show dialog
        } else {
           // displayView(1)
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
            json_data.put("device_id", AppUtils.getDeviceID(mActivity))
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
                    parseLogoutApi(response)
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
                startActivity(Intent(mActivity, Login::class.java))
               // finish()
            }
            AppUtils.showToast(mActivity, jsonObject.getString("res_msg"))
        } catch (e: Exception) {
            Log.v(AppsStrings.apiErrorException, e.message!!)
            AppUtils.showToast(mActivity, AppsStrings.defResponse)
        }
    }
}