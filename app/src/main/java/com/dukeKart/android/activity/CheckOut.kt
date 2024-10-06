package com.dukeKart.android.activity

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import com.airbnb.lottie.LottieAnimationView
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.dukeKart.android.Payment.NewWebViewActivity
import com.dukeKart.android.R
import com.dukeKart.android.common.AppUrls
import com.dukeKart.android.common.AppUtils
import com.dukeKart.android.common.AppsStrings
import com.dukeKart.android.database.AppConstants
import com.dukeKart.android.database.Preferences
import com.dukeKart.android.utility.AvenuesParams
import com.dukeKart.android.utility.Constants
import com.dukeKart.android.views.BaseActivity
import com.nandroidex.upipayments.listener.PaymentStatusListener
import com.nandroidex.upipayments.models.TransactionDetails
import com.nandroidex.upipayments.utils.UPIPayment
import com.payu.base.models.ErrorResponse
import com.payu.base.models.PayUPaymentParams
import com.payu.base.models.PaymentMode
import com.payu.base.models.PaymentType
import com.payu.checkoutpro.PayUCheckoutPro
import com.payu.checkoutpro.models.PayUCheckoutProConfig
import com.payu.checkoutpro.utils.PayUCheckoutProConstants
import com.payu.checkoutpro.utils.PayUCheckoutProConstants.CP_HASH_NAME
import com.payu.checkoutpro.utils.PayUCheckoutProConstants.CP_HASH_STRING
import com.payu.ui.model.listeners.PayUCheckoutProListener
import com.payu.ui.model.listeners.PayUHashGenerationListener
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import com.swapnil.payupayment.HashGenerationUtils
import org.json.JSONException
import org.json.JSONObject


class CheckOut : BaseActivity(), View.OnClickListener, PaymentResultListener,
    PaymentStatusListener {
    var iv_search: ImageView? = null
    var iv_back: ImageView? = null
    var iv_changeBillAddress: ImageView? = null
    var iv_changeShipAddress: ImageView? = null
    var rlCart: RelativeLayout? = null
    var tv_title: TextView? = null
    var tv_billingTitle: TextView? = null
    var tv_billingAddress: TextView? = null
    var tv_shippingTitle: TextView? = null
    var tv_payableAmount: TextView? = null
    var tv_shppingAddress: TextView? = null
    var tv_cartSubTotal: TextView? = null
    var tv_cartShipCharge: TextView? = null
    var tv_discAmount: TextView? = null
    var tv_totalAmount: TextView? = null
    var rb_onlinePayment: RadioButton? = null
    var rb_cashOnDelivery: RadioButton? = null
    var rb_upiPayment: RadioButton? = null
    var ll_onlinePayment: LinearLayout? = null
    var ll_cashOnDelivery: LinearLayout? = null
    var ll_Apply_Coupon: LinearLayout? = null
    var ll_Remove_Coupon: LinearLayout? = null
    var rl_Remove_Coupon: RelativeLayout? = null
    var et_couponCode: EditText? = null
    var tv_coupon_code: TextView? = null
    var ll_upiPayment: LinearLayout? = null
    var ll_shipping: LinearLayout? = null
    var ll_Coupon: LinearLayout? = null
    var btn_placeOrder: Button? = null
    var btn_cancelOrder: Button? = null
    var btn_addAddress: Button? = null
    var prefs: Preferences? = null
    var shippingId = ""
    var applied = "0"
    var paymentType = 0
    var payableAmount = 0.0 // 1= Cod 2= Online 3= Upi
    var IntValue = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_out)
        setIds()
        setHeaders()
        if (shippingId.isEmpty()) {
            btn_addAddress!!.visibility = View.VISIBLE
            ll_shipping!!.visibility = View.GONE
        } else {
            btn_addAddress!!.visibility = View.GONE
            ll_shipping!!.visibility = View.VISIBLE
        }
        if (paymentType == 0) {
            ll_Coupon!!.visibility = View.GONE
            ll_Remove_Coupon!!.visibility = View.GONE
        } else {
            ll_Coupon!!.visibility = View.VISIBLE
            if (applied.equals("0", ignoreCase = true)) {
                ll_Remove_Coupon!!.visibility = View.VISIBLE
            }
        }
        setListeners()
        setPrice()
        Checkout.preload(this@CheckOut)
    }

    private fun setPrice() {
        try {
            setPrice(
                prefs!![AppConstants.cartPayable].toDouble(),
                prefs!![AppConstants.cartSubTotal].toInt(),
                prefs!![AppConstants.cartShipping].toInt(),
                prefs!![AppConstants.cartDiscount].toInt(),
                prefs!![AppConstants.cartGST].toDouble()


            )
            Log.v("check Price", "check Price  " + prefs!![AppConstants.cartDiscount].toInt())
            Log.v(
                "check Price",
                "check Price 2  " + Integer.toString(prefs!![AppConstants.cartDiscount].toInt() + 50)
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setPrice(pricePayable: Double, priceSubTotal: Int, priceShipping: Int, priceDiscount: Int, pricegst: Double) {
        try {
            payableAmount = pricePayable.toDouble()/* + priceShipping*/
            tv_payableAmount!!.text = AppUtils.setPriceTotal(payableAmount.toString())
            tv_cartSubTotal!!.text = AppUtils.setPriceTotal(Integer.toString(priceSubTotal /*+ priceShipping*/))
            /*if (priceSubTotal <= 500) {*/
                tv_cartShipCharge!!.text = AppUtils.setPriceTotal(Integer.toString(priceShipping))
        /*    } else {
                tv_cartShipCharge!!.text = "Free  "
            }*/
            tv_discAmount!!.text = AppUtils.setPriceTotal(pricegst.toString())
            tv_totalAmount!!.text =
                AppUtils.setPriceTotal(Integer.toString(priceSubTotal + priceShipping))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setHeaders() {
        try {
            iv_search!!.visibility = View.GONE
            rlCart!!.visibility = View.GONE
            tv_title!!.text = "Check Out"
            tv_shippingTitle!!.text = prefs!![AppConstants.primaryAddressTitle]
            tv_shppingAddress!!.text = prefs!![AppConstants.primaryAddress]
            shippingId = prefs!![AppConstants.primaryAddressId]
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setIds() {
        prefs = Preferences(this@CheckOut)
        iv_back = findViewById(R.id.iv_back)
        iv_search = findViewById(R.id.iv_search)
        rlCart = findViewById(R.id.rlCart)
        tv_title = findViewById(R.id.tv_title)
        tv_billingTitle = findViewById(R.id.tv_billingTitle)
        tv_shippingTitle = findViewById(R.id.tv_shippingTitle)
        iv_changeBillAddress = findViewById(R.id.iv_changeBillAddress)
        iv_changeShipAddress = findViewById(R.id.iv_changeShipAddress)
        tv_billingAddress = findViewById(R.id.tv_billingAddress)
        tv_shppingAddress = findViewById(R.id.tv_shppingAddress)
        rb_onlinePayment = findViewById(R.id.rb_onlinePayment)
        ll_onlinePayment = findViewById(R.id.ll_onlinePayment)
        ll_cashOnDelivery = findViewById(R.id.ll_cashOnDelivery)
        rb_cashOnDelivery = findViewById(R.id.rb_cashOnDelivery)
        ll_upiPayment = findViewById(R.id.ll_upiPayment)
        rb_upiPayment = findViewById(R.id.rb_upiPayment)
        tv_cartSubTotal = findViewById(R.id.tv_cartSubTotal)
        tv_cartShipCharge = findViewById(R.id.tv_cartShipCharge)
        tv_discAmount = findViewById(R.id.tv_discAmount)
        tv_payableAmount = findViewById(R.id.tv_payableAmount)
        btn_placeOrder = findViewById(R.id.btn_placeOrder)
        btn_cancelOrder = findViewById(R.id.btn_cancelOrder)
        btn_addAddress = findViewById(R.id.btn_addAddress)
        tv_totalAmount = findViewById(R.id.tv_totalAmount)
        ll_shipping = findViewById(R.id.ll_shipping)
        ll_Apply_Coupon = findViewById(R.id.ll_Apply_Coupon)
        ll_Coupon = findViewById(R.id.ll_Coupon)
        ll_Remove_Coupon = findViewById(R.id.ll_Remove_Coupon)
        rl_Remove_Coupon = findViewById(R.id.rl_Remove_Coupon)
        et_couponCode = findViewById(R.id.et_couponCode)
        tv_coupon_code = findViewById(R.id.tv_coupon_code)
    }

    private fun setListeners() {
        iv_back!!.setOnClickListener(this)
        iv_changeBillAddress!!.setOnClickListener(this)
        iv_changeShipAddress!!.setOnClickListener(this)
        ll_onlinePayment!!.setOnClickListener(this)
        ll_cashOnDelivery!!.setOnClickListener(this)
        ll_upiPayment!!.setOnClickListener(this)
        btn_placeOrder!!.setOnClickListener(this)
        btn_cancelOrder!!.setOnClickListener(this)
        btn_addAddress!!.setOnClickListener(this)
        ll_Apply_Coupon!!.setOnClickListener(this)
        rl_Remove_Coupon!!.setOnClickListener(this)
    }

    fun setRadioButtons(radioButton: RadioButton?) {
        rb_onlinePayment!!.isChecked = false
        rb_cashOnDelivery!!.isChecked = false
        rb_upiPayment!!.isChecked = false
        radioButton!!.isChecked = true
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.iv_back -> onBackPressed()
            R.id.iv_changeBillAddress -> {
            }

            R.id.iv_changeShipAddress,
            R.id.btn_addAddress -> startActivity(
                Intent(
                    this@CheckOut, ShipAddress::class.java
                )
            )

            R.id.btn_placeOrder -> if (shippingId.equals("", ignoreCase = true)) {
                AppUtils.showToast(this@CheckOut, "Shipping Address cannot be empty")
            } else if (paymentType == 1) {
                placeOrderApi("")
            } else if (paymentType == 2) {
                placeOrderApi("")

            } else if (paymentType == 3) {
                startUpiPayment("1.0") //Integer.toString(payableAmount
            } else {
                AppUtils.showToast(this@CheckOut, "Payment Mode not selected")
            }

            R.id.btn_cancelOrder -> {
            }

            R.id.ll_Apply_Coupon -> {
                if (validate()) applyCouponApi()
            }

            R.id.rl_Remove_Coupon -> {
                removeCouponApi()
            }

            R.id.ll_onlinePayment -> {
                // Toast.makeText(this@CheckOut, "Coming soon", Toast.LENGTH_SHORT).show();

                paymentType = 2
                setPrice(
                    prefs!![AppConstants.cartPayable].toDouble(),
                    prefs!![AppConstants.cartSubTotal].toInt(),
                    prefs!![AppConstants.cartShipping].toInt(),
                    prefs!![AppConstants.cartDiscount].toInt(),
                    prefs!![AppConstants.cartGST].toDouble()
                )
                setRadioButtons(rb_onlinePayment)
                setCouponVal()
                return
            }

            R.id.ll_cashOnDelivery -> {
                paymentType = 1
                setPrice(
                    prefs!![AppConstants.cartPayable].toDouble(),
                    prefs!![AppConstants.cartSubTotal].toInt(),
                    prefs!![AppConstants.cartShipping].toInt(),
                    prefs!![AppConstants.cartDiscount].toInt(),
                    prefs!![AppConstants.cartGST].toDouble()
                )
                setRadioButtons(rb_cashOnDelivery)
                setCouponVal()
            }

            R.id.ll_upiPayment -> {
//                Toast.makeText(this@CheckOut, "Coming soon", Toast.LENGTH_SHORT).show();
//                return;
                paymentType = 3
                tenp(prefs!![AppConstants.cartSubTotal].toInt())
                IntValue = Math.round(tenp(prefs!![AppConstants.cartSubTotal].toInt())).toInt()
                IntValue = if (IntValue < 50) {
                    IntValue
                } else {
                    50
                }
                Log.v("discount", "discount $IntValue")
                Toast.makeText(
                    applicationContext,
                    "Additional discount ₹" + IntValue + " applied successfully",
                    Toast.LENGTH_SHORT
                ).show()
                setPrice(
                    prefs!![AppConstants.cartPayable].toDouble(),
                    prefs!![AppConstants.cartSubTotal].toInt(),
                    prefs!![AppConstants.cartShipping].toInt(),
                    prefs!![AppConstants.cartGST].toInt(),
                    prefs!![AppConstants.cartDiscount].toDouble()+ IntValue
                )
                setRadioButtons(rb_upiPayment)
                setCouponVal()
            }

            else -> AppUtils.showToast(this@CheckOut, "Comming Soon")
        }
    }

    fun start(price: Int) {/*
          You need to pass Checkout.preload in oncreate() in order to let Razorpay create CheckoutActivity
          and also declare ApiKey in manifest
         *//*  val co = Checkout()
          try {
              val options = JSONObject()
              options.put("name", "DukeKart")
              options.put("description", "Your Cart Payment")
              //You can omit the image option to fetch the image from dashboard
              options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.png")
              options.put("currency", "INR")
              options.put("amount", price * 100)
              val preFill = JSONObject()
              preFill.put("email", prefs!![AppConstants.userEmail])
              preFill.put("contact", prefs!![AppConstants.userMobile])
              options.put("prefill", preFill)
              co.open(this@CheckOut, options)
          } catch (e: Exception) {
              AppUtils.showToast(this@CheckOut, "Error in payment: " + e.message)
              e.printStackTrace()
          }*/
    }

    private fun startPayment(price: Double, orderId: String) {
        showLoading()

        val json = JSONObject()
        val json_data = JSONObject()
        try {
            json_data.put("user_id", prefs!!.get(AppConstants.userId))
            json_data.put("amount", price)
            json_data.put("order_id", orderId)


        } catch (e: JSONException) {
            e.printStackTrace()
        }
        AndroidNetworking.post("https://dukekart.in/api/PlaceOnlineOrder")
            .addJSONObjectBody(json_data).setPriority(Priority.HIGH).build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    parsedata(response)
                }

                override fun onError(error: ANError) {
                    hideLoading()
                    // handle error
                    if (error.errorCode != 0) {
                        // AppUtils.showErrorMessage(edAmount, error.errorCode.toString(), mActivity)
                        Log.d("onError errorCode ", "onError errorCode : " + error.errorCode)
                        Log.d("onError errorBody", "onError errorBody : " + error.errorBody)
                        Log.d("onError errorDetail", "onError errorDetail : " + error.errorDetail)
                    } else {
                        //   AppUtils.showErrorMessage(edAmount, error.errorDetail.toString(), mActivity)
                    }
                }
            })
    }

    private fun parsedata(response: JSONObject) {
        Log.d("response ", response.toString())
        hideLoading()
        try {
            val jsonObject = response.getJSONObject("ecommerce")


            val intent = Intent(this, NewWebViewActivity::class.java)
            intent.putExtra(AvenuesParams.ACCESS_CODE, Constants.ACCESS_CODE)
            intent.putExtra(AvenuesParams.MERCHANT_ID, Constants.MERCHANT_ID)
            intent.putExtra(AvenuesParams.ORDER_ID, jsonObject.getString("order_id"))
            intent.putExtra(AvenuesParams.CURRENCY, "INR")
            intent.putExtra(AvenuesParams.BILLING_COUNTRY, "India")
            intent.putExtra(AvenuesParams.ENC_VAL, jsonObject.getString("enc_val"))
            intent.putExtra(AvenuesParams.BILLING_TEL, prefs!![AppConstants.userMobile])
            intent.putExtra(AvenuesParams.BILLING_EMAIL, prefs!![AppConstants.userEmail])
            intent.putExtra(AvenuesParams.AMOUNT, payableAmount)
            intent.putExtra(AvenuesParams.REDIRECT_URL, Constants.REDIRECT_URL)
            intent.putExtra(AvenuesParams.CANCEL_URL, Constants.CANCEL_URL)
            intent.putExtra(AvenuesParams.RSA_KEY_URL, Constants.RSA_KEY_URL)
            startActivity(intent)
        } catch (e: java.lang.Exception) {
            AppUtils.showToast(this@CheckOut, e.toString())

        }
    }

    override fun onPaymentSuccess(razorPaymentId: String) {
        try {
            AppUtils.showToast(this, "Payment Successful")
            placeOrderApi(razorPaymentId)
        } catch (e: Exception) {
            Log.e("razorPayment", "Exception in onPaymentSuccess", e)
        }
    }

    /**
     * The name of the function has to be
     * onPaymentError
     * Wrap your code in try catch, as shown, to ensure that this method runs correctly
     */
    override fun onPaymentError(code: Int, response: String) {
        try {
            AppUtils.showToast(this, "Payment failed: $response")
            Log.e("razorPayment", "Payment failed: $code $response")
        } catch (e: Exception) {
            Log.e("razorPayment", "Exception in onPaymentError", e)
        }
    }

    override fun onBackPressed() {
        //startActivity(Intent(this@CheckOut, AddToCart::class.java))
        finish()
    }

    private fun placeOrderApi(transactionId: String) {
        showLoading()
        AppsStrings.setApiString("PlaceOrder")
        val url = AppUrls.PlaceOrder
        Log.v(AppsStrings.apiUrl, url)
        val json = JSONObject()
        val json_data = JSONObject()
        try {
            json_data.put("userId", prefs!![AppConstants.userId])
            json_data.put("paymentType", paymentType)
            json_data.put("address_id", shippingId)
            json_data.put("transaction_id", transactionId)
            json.put(AppsStrings.BASEJSON, json_data)
            Log.v(AppsStrings.apiJson, json.toString())
        } catch (e: Exception) {
            AppUtils.showToast(this@CheckOut, AppsStrings.defResponse)
            Log.v(AppsStrings.apiErrorException, e.message!!)
            e.printStackTrace()
        }
        AndroidNetworking.post(url)
            .addJSONObjectBody(json_data).setPriority(Priority.HIGH).build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    parsePlaceOrderApi(response)
                }

                override fun onError(error: ANError) {
                    hideLoading()
                    // handle error
                    if (error.errorCode != 0) {
                        AppUtils.showToast(this@CheckOut, AppsStrings.defResponse)
                        Log.v(AppsStrings.apiErrorCode, "onError errorCode : " + error.errorCode)
                        Log.v(AppsStrings.apiErrorBody, "onError errorBody : " + error.errorBody)
                        Log.v(
                            AppsStrings.apiErrorDetail, "onError errorDetail : " + error.errorDetail
                        )
                    } else {
                        AppUtils.showToast(this@CheckOut, AppsStrings.defResponse)
                        Log.v(AppsStrings.apiErrorException, error.message!!)
                    }
                }
            })
    }


    private fun parsePlaceOrderApi(response: JSONObject) {
        Log.d(AppsStrings.apiResponse, response.toString())
        try {
            val jsonObject = response.getJSONObject("ecommerce")
            hideLoading()
            //AppUtils.showToast(this@CheckOut, jsonObject.getString("res_msg"))
            if (jsonObject.getString("res_code").equals("1", ignoreCase = true)) {
                prefs!!.set(AppConstants.cartQuantity, "0").commit()

                if (paymentType == 2) {
                    startPayment(payableAmount,jsonObject.getString("order_id"))
                }else {

                    val intent = Intent(this@CheckOut, OrderPlaced::class.java)
                    intent.putExtra(AppConstants.orderId, jsonObject.getString("order_id"))
                    intent.putExtra(AppConstants.orderDate, jsonObject.getString("add_date"))
                    if (paymentType == 3) {
                        prefs!!.set(
                            AppConstants.cartDiscount,
                            Integer.toString(prefs!![AppConstants.cartDiscount].toInt() + 50)
                        ).commit()
                        prefs!!.set(
                            AppConstants.cartPayable,
                            Integer.toString(prefs!![AppConstants.cartPayable].toInt() - 50)
                        ).commit()
                    }
                    startActivity(intent)
                }
            }
        } catch (e: Exception) {
            Log.v(AppsStrings.apiErrorException, e.message!!)
            AppUtils.showToast(this@CheckOut, AppsStrings.defResponse)
        }
    }

    private val surl = "https://payu.herokuapp.com/success"
    private val furl = "https://payu.herokuapp.com/failure"
    private var upiPayment: UPIPayment? = null
    private fun startUpiPayment(amount: String) {

        val payUPaymentParams =
            PayUPaymentParams.Builder().setAmount("1.0").setIsProduction(false).setKey("DbZdWn")
                .setProductInfo("cart item").setPhone("7007158611")
                .setTransactionId(System.currentTimeMillis().toString()).setFirstName("Shhopon")
                .setEmail("play.shhopon@gmail.com").setSurl(surl).setFurl(furl)
                .setUserCredential("DbZdWn:sleeban162@gmail.com").build()

        //client id
        //bfb3af27abfff822d01ef8ff349c0e4715301bd1e783db83c8cc5c7e7b001fd6

        //client scret
        //63344db5163762618e7c76d70011d3c71cf99555f90905d117f1e2f6b7f861f2

        //This will show the listed application on top of gateway (it won't show until we have production key)
        val checkoutOrderList = ArrayList<PaymentMode>()
        checkoutOrderList.add(PaymentMode(PaymentType.UPI, PayUCheckoutProConstants.CP_GOOGLE_PAY))
        checkoutOrderList.add(PaymentMode(PaymentType.UPI, PayUCheckoutProConstants.CP_PHONEPE))
        checkoutOrderList.add(PaymentMode(PaymentType.UPI, PayUCheckoutProConstants.CP_PAYTM))
        checkoutOrderList.add(PaymentMode(PaymentType.WALLET, PayUCheckoutProConstants.CP_PAYTM))
        checkoutOrderList.add(PaymentMode(PaymentType.WALLET, PayUCheckoutProConstants.CP_PHONEPE))
//        checkoutOrderList.add(PaymentMode(PaymentType.UPI_INTENT, PayUCheckoutProConstants.CP_PHONEPE_CLASS_NAME))
//        checkoutOrderList.add(PaymentMode(PaymentType.UPI_INTENT, PayUCheckoutProConstants.CP_PAYTM_NAME))
        val payUCheckoutProConfig = PayUCheckoutProConfig()
        payUCheckoutProConfig.paymentModesOrder = checkoutOrderList

        /*
        //This is for forced transaction, if upi is set no other option will be available

        val enforceList = ArrayList<HashMap<String,String>>()
        enforceList.add(HashMap<String,String>().apply {
            put(PayUCheckoutProConstants.CP_PAYMENT_TYPE,PaymentType.UPI.name)
            put(PayUCheckoutProConstants.CP_PAYMENT_TYPE,PaymentType.UPI_INTENT.name)
            put(PayUCheckoutProConstants.ENFORCED_IBIBOCODE,"AXIB")
        })
        payUCheckoutProConfig.enforcePaymentList = enforceList
*/

        PayUCheckoutPro.open(this,
            payUPaymentParams,
            payUCheckoutProConfig,
            object : PayUCheckoutProListener {


                override fun onPaymentSuccess(response: Any) {
                    response as HashMap<*, *>
                    val payUResponse = response[PayUCheckoutProConstants.CP_PAYU_RESPONSE]
                    val merchantResponse = response[PayUCheckoutProConstants.CP_MERCHANT_RESPONSE]

                    Log.d(
                        "PayUTesting",
                        payUResponse.toString() + " and " + merchantResponse.toString()
                    )
                }
//{"id":403993715529845058,"mode":"CASH","status":"success","unmappedstatus":"captured","key":"DbZdWn","txnid":"1691919519070","transaction_fee":"1.00","amount":"1.00","discount":"0.00","addedon":"2023-08-13 15:09:10","productinfo":"cart item","firstname":"Shhopon","email":"**********************","phone":"7007158611","hash":"a1b24f0f3d8ff1b4f7520f9c35c415001d042943929203c2d10a4d5500f3e41d19bbc84bc3270191d5375bbedc65e1aff3bf2108e85f848c0c2fcaaadce1c625","field9":"Transaction Completed Successfully","payment_source":"payu","PG_TYPE":"CASH-PG","bank_ref_no":"2537ea2b-b714-450f-b948-7162ae72f866","ibibo_code":"CASH","error_code":"E000","Error_Message":"No Error","is_seamless":1,"surl":"********************************************************************************
                //and mihpayid=403993715529845058&mode=CASH&status=success&unmappedstatus=captured&key=DbZdWn&txnid=1691919519070&amount=1.00&discount=0.00&net_amount_debit=1&addedon=2023-08-13+15%3A09%3A10&productinfo=cart+item&firstname=Shhopon&lastname=&address1=&address2=&city=&state=&country=&zipcode=&email=play.shhopon%40gmail.com&phone=7007158611&udf1=&udf2=&udf3=&udf4=&udf5=&udf6=&udf7=&udf8=&udf9=&udf10=&hash=a1b24f0f3d8ff1b4f7520f9c35c415001d042943929203c2d10a4d5500f3e41d19bbc84bc3270191d5375bbedc65e1aff3bf2108e85f848c0c2fcaaadce1c625&field1=&field2=&field3=&field4=&field5=&field6=&field7=&field8=&field9=Transaction+Completed+Successfully&payment_source=payu&meCode=%7B%22%22%3Anull%7D&PG_TYPE=CASH-PG&bank_ref_num=2537ea2b-b714-450f-b948-7162ae72f866&bankcode=CASH&error=E000&error_Message=No+Error&device_type=1

                override fun onPaymentFailure(response: Any) {
                    response as HashMap<*, *>
                    val payUResponse = response[PayUCheckoutProConstants.CP_PAYU_RESPONSE]
                    val merchantResponse = response[PayUCheckoutProConstants.CP_MERCHANT_RESPONSE]

                    Log.d(
                        "PayUTesting",
                        payUResponse.toString() + " and " + merchantResponse.toString()
                    )

                }


                override fun onPaymentCancel(isTxnInitiated: Boolean) {
                    Log.d("PayUTesting", "Payment canceled by user")

                }


                override fun onError(errorResponse: ErrorResponse) {
                    val errorMessage: String
                    if (errorResponse != null && errorResponse.errorMessage != null && errorResponse.errorMessage!!.isNotEmpty()) errorMessage =
                        errorResponse.errorMessage!!
                    else errorMessage = resources.getString(R.string.applicationError)
                    Log.d("PayUTesting", "Error: ${errorResponse.errorCode}  == " + errorMessage)

                }

                override fun setWebViewProperties(webView: WebView?, bank: Any?) {
                    //For setting webview properties, if any. Check Customized Integration section for more details on this
                }

                override fun generateHash(
                    map: HashMap<String, String?>,
                    hashGenerationListener: PayUHashGenerationListener
                ) {
                    if (map.containsKey(CP_HASH_STRING) && map.containsKey(CP_HASH_STRING) != null && map.containsKey(
                            CP_HASH_NAME
                        ) && map.containsKey(CP_HASH_NAME) != null
                    ) {

                        val hashData = map[CP_HASH_STRING]
                        val hashName = map[CP_HASH_NAME]

                        //Do not generate hash from local, it needs to be calculated from server side only. Here, hashString contains hash created from your server side.
                        val hash: String? = HashGenerationUtils.generateHashFromSDK(
                            hashData!!, "eARSjo22DwRRZvpULCnQFthiP2GnKCdP"
                        )
                        if (!TextUtils.isEmpty(hash)) {
                            val dataMap: HashMap<String, String?> = HashMap()
                            dataMap[hashName!!] = hash
                            hashGenerationListener.onHashGenerated(dataMap)
                        }
                    }
                }
            })

//        val uri = Uri.Builder()
//            .scheme("upi")
//            .authority("pay")
//            .appendQueryParameter("pa", "7007158611@okbizaxis")
//            .appendQueryParameter("pn", "MOHD NABEEL")
//            .appendQueryParameter("tn", "")
//            .appendQueryParameter("tr", "BCR2DN4TRKGOFBJK")
//            .appendQueryParameter("aid", "uGICAgMCypsCrEQ")
//            .appendQueryParameter("am", amount)
//            .appendQueryParameter("mc","5137")
//            .appendQueryParameter("cu", "INR")
//            .appendQueryParameter("url", "")
//            .build()
//
//
//        val upiPayIntent = Intent(Intent.ACTION_VIEW)
//        upiPayIntent.data = uri
//
//        // will always show a dialog to user to choose an app
//        val chooser = Intent.createChooser(upiPayIntent, "Pay with")
//
//        // check if intent resolves
//        if (null != chooser.resolveActivity(packageManager)) {
//            startActivityForResult(chooser, 751)
//        } else {
//            Toast.makeText(this@CheckOut, "No UPI app found, please install one to continue", Toast.LENGTH_SHORT).show()
//        }


//        val millis = System.currentTimeMillis()
//        upiPayment = UPIPayment.Builder()
//                .with(this@CheckOut)
//                .setPayeeVpa("7007158611@okbizaxis") //                .setPayeeMerchantCode("ozFooA08198864791578")
//                .setPayeeName("Mohd Nabeel")
//                .setPayeeMerchantCode("5137")
//                .setTransactionId(millis.toString())
//                .setTransactionRefId(millis.toString())
//                .setDescription("Cart")
//                .setAmount(amount.toDouble().toString())
//                .build()
//        upiPayment!!.setPaymentStatusListener(this)
//        if (upiPayment!!.isDefaultAppExist()) {
//            onAppNotFound()
//            return
//        }
//        upiPayment!!.startPayment()

    }

    override fun onAppNotFound() {}
    override fun onTransactionCancelled() {}
    override fun onTransactionFailed() {}
    override fun onTransactionSubmitted() {}
    override fun onTransactionSuccess() {}
    override fun onTransactionCompleted(transactionDetails: TransactionDetails?) {
        var status: String? = null
        var approvalRefNo: String? = null
        var remark = ""
        if (transactionDetails != null) {
            status = transactionDetails.status
            approvalRefNo = transactionDetails.approvalRefNo
            Log.v("UpiTransDetails", "details : $transactionDetails")
            Log.v("UpiTransDetails", "1" + transactionDetails.transactionId)
            Log.v("UpiTransDetails", "1" + transactionDetails.transactionRefId)
            Log.v("UpiTransDetails", "1" + transactionDetails.approvalRefNo)
            Log.v("UpiTransDetails", "1" + transactionDetails.approvalRefNo)
            remark = transactionDetails.transactionId!!.trim { it <= ' ' }
        }
        var success = false
        if (status != null) {
            success = status.equals("success", ignoreCase = true) || status.equals(
                "submitted", ignoreCase = true
            )
            if (success) placeOrderApi(remark) else Toast.makeText(
                this@CheckOut, "Transaction was Failed", Toast.LENGTH_SHORT
            ).show()
        }
        upiPayment!!.detachListener()
    }

    fun tenp(ammount: Int): Double {
        val amount = ammount.toString().toDouble()
        return amount / 100.0f * 2
    }

    private fun applyCouponApi() {
        showLoading()
        AppsStrings.setApiString("ApplyCoupon")
        val url = AppUrls.ApplyCoupon
        Log.v(AppsStrings.apiUrl, url)
        val json = JSONObject()
        val json_data = JSONObject()
        try {
            json_data.put("user_id", prefs!![AppConstants.userId])
            json_data.put("coupon_code", AppUtils.getTextInputEditTextData(et_couponCode))
            json_data.put("payment_mode", paymentType)
            json_data.put("amount", prefs!![AppConstants.cartPayable].toInt())
            json.put(AppsStrings.BASEJSON, json_data)
            Log.v(AppsStrings.apiJson, json.toString())
        } catch (e: Exception) {
            AppUtils.showToast(this@CheckOut, AppsStrings.defResponse)
            Log.v(AppsStrings.apiErrorException, e.message!!)
            e.printStackTrace()
        }
        AndroidNetworking.post(url).addJSONObjectBody(json_data).setPriority(Priority.HIGH).build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    parseApplyCouponApi(response)
                }

                override fun onError(error: ANError) {
                    hideLoading()
                    // handle error
                    if (error.errorCode != 0) {
                        AppUtils.showToast(this@CheckOut, AppsStrings.defResponse)
                        Log.v(AppsStrings.apiErrorCode, "onError errorCode : " + error.errorCode)
                        Log.v(AppsStrings.apiErrorBody, "onError errorBody : " + error.errorBody)
                        Log.v(
                            AppsStrings.apiErrorDetail, "onError errorDetail : " + error.errorDetail
                        )
                    } else {
                        AppUtils.showToast(this@CheckOut, AppsStrings.defResponse)
                        Log.v(AppsStrings.apiErrorException, error.message!!)
                    }
                }
            })
    }

    private fun parseApplyCouponApi(response: JSONObject) {
        Log.d(AppsStrings.apiResponse, response.toString())
        try {
            val jsonObject = response.getJSONObject("ecommerce")
            hideLoading()
            //AppUtils.showToast(this@CheckOut, jsonObject.getString("res_msg"))
            if (jsonObject.getString("res_code").equals("1", ignoreCase = true)) {
                var discount_type = jsonObject.getString("discount_type")
                var discount = jsonObject.getString("discount")
                var amount = jsonObject.getString("amount")
                var coupon_code = jsonObject.getString("coupon_code")
                applied = "1"
                tv_coupon_code!!.text = coupon_code
                if (paymentType == 3) {
                    setPrice(
                        amount.toDouble(),
                        prefs!![AppConstants.cartSubTotal].toInt(),
                        prefs!![AppConstants.cartShipping].toInt(),
                        prefs!![AppConstants.cartGST].toInt(),
                        (discount.toString().toDouble().toInt() + IntValue).toDouble()
                    )
                } else {
                    setPrice(
                        amount.toDouble(),
                        prefs!![AppConstants.cartSubTotal].toInt(),
                        prefs!![AppConstants.cartShipping].toInt(),
                        prefs!![AppConstants.cartGST].toInt(),
                        discount.toDouble()
                    )

                }
                showResult()
                AppUtils.showToast(this@CheckOut, jsonObject.getString("res_msg"))
            } else AppUtils.showToast(this@CheckOut, jsonObject.getString("res_msg"))
        } catch (e: Exception) {
            Log.v(AppsStrings.apiErrorException, e.message!!)
            AppUtils.showToast(this@CheckOut, AppsStrings.defResponse)
        }
    }

    private fun removeCouponApi() {
        showLoading()
        AppsStrings.setApiString("RemoveCoupon")
        val url = AppUrls.RemoveCoupon
        Log.v(AppsStrings.apiUrl, url)
        val json = JSONObject()
        val json_data = JSONObject()
        try {
            json_data.put("user_id", prefs!![AppConstants.userId])
            json.put(AppsStrings.BASEJSON, json_data)
            Log.v(AppsStrings.apiJson, json.toString())
        } catch (e: Exception) {
            AppUtils.showToast(this@CheckOut, AppsStrings.defResponse)
            Log.v(AppsStrings.apiErrorException, e.message!!)
            e.printStackTrace()
        }
        AndroidNetworking.post(url).addJSONObjectBody(json_data).setPriority(Priority.HIGH).build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    parseRemoveCouponApi(response)
                }

                override fun onError(error: ANError) {
                    hideLoading()
                    // handle error
                    if (error.errorCode != 0) {
                        AppUtils.showToast(this@CheckOut, AppsStrings.defResponse)
                        Log.v(AppsStrings.apiErrorCode, "onError errorCode : " + error.errorCode)
                        Log.v(AppsStrings.apiErrorBody, "onError errorBody : " + error.errorBody)
                        Log.v(
                            AppsStrings.apiErrorDetail, "onError errorDetail : " + error.errorDetail
                        )
                    } else {
                        AppUtils.showToast(this@CheckOut, AppsStrings.defResponse)
                        Log.v(AppsStrings.apiErrorException, error.message!!)
                    }
                }
            })
    }

    private fun parseRemoveCouponApi(response: JSONObject) {
        Log.d(AppsStrings.apiResponse, response.toString())
        try {
            val jsonObject = response.getJSONObject("ecommerce")
            hideLoading()
            //AppUtils.showToast(this@CheckOut, jsonObject.getString("res_msg"))
            if (jsonObject.getString("res_code").equals("1", ignoreCase = true)) {
                var discount_type = jsonObject.getString("discount_type")
                var discount = jsonObject.getString("discount")
                var amount = jsonObject.getString("amount")
                applied = "0"
                if (paymentType == 3) {
                    setPrice(
                        amount.toDouble(),
                        prefs!![AppConstants.cartSubTotal].toInt(),
                        prefs!![AppConstants.cartGST].toInt(),
                        prefs!![AppConstants.cartShipping].toInt(),
                        discount.toDouble()
                    )
                } else {
                    setPrice(
                        amount.toDouble(),
                        prefs!![AppConstants.cartSubTotal].toInt(),
                        prefs!![AppConstants.cartGST].toInt(),
                        prefs!![AppConstants.cartShipping].toInt(),
                        discount.toDouble()
                    )

                }
                showResult()
                AppUtils.showToast(this@CheckOut, jsonObject.getString("res_msg"))
            } else AppUtils.showToast(this@CheckOut, jsonObject.getString("res_msg"))
        } catch (e: Exception) {
            Log.v(AppsStrings.apiErrorException, e.message!!)
            AppUtils.showToast(this@CheckOut, AppsStrings.defResponse)
        }
    }

    fun validate(): Boolean {
        if (AppUtils.getTextInputEditTextData(et_couponCode).isEmpty()) {
            AppUtils.showToast(this@CheckOut, "Enter a Coupon Code")
        } else {
            return true
        }
        return false
    }

    private fun showResult() {
        val dialog = Dialog(mActivity, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        try {
            dialog.setContentView(R.layout.result_layout)
            dialog.setCancelable(true)
            dialog.closeOptionsMenu()
            dialog.setCanceledOnTouchOutside(true)
            val lav_result: LottieAnimationView = dialog.findViewById(R.id.lav_result)

            setCouponVal()

            dialog.show()
            lav_result.enableMergePathsForKitKatAndAbove(true)
            Handler().postDelayed({ dialog.dismiss() }, 2000)

            //lav_result.setAnimation("success_check.json")

            dialog.show()
        } catch (e: java.lang.Exception) {
            dialog.dismiss()
        }
    }

    private fun setCouponVal() {
        return
        if (applied.equals("0", ignoreCase = true)) {
            ll_Coupon!!.visibility = View.VISIBLE
            ll_Remove_Coupon!!.visibility = View.GONE
            ll_onlinePayment!!.isClickable = true
            ll_cashOnDelivery!!.isClickable = true
            ll_upiPayment!!.isClickable = true
        } else {
            ll_Coupon!!.visibility = View.GONE
            ll_Remove_Coupon!!.visibility = View.VISIBLE
            ll_onlinePayment!!.isClickable = false
            ll_cashOnDelivery!!.isClickable = false
            ll_upiPayment!!.isClickable = false

        }
    }
}