package com.dukeKart.android.Payment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.dukeKart.android.R
import com.dukeKart.android.activity.OrderPlaced
import com.dukeKart.android.common.AppUrls
import com.dukeKart.android.common.AppUtils
import com.dukeKart.android.common.AppsStrings
import com.dukeKart.android.database.AppConstants
import com.dukeKart.android.database.Preferences
import com.dukeKart.android.utility.AvenuesParams
import com.dukeKart.android.utility.Constants
import com.dukeKart.android.views.BaseActivity
import org.json.JSONObject

class NewWebViewActivity : BaseActivity() {
    var webview: WebView? = null
    var orderId = ""
    var orderId_cc = ""
    var bankRefNum = ""
    var trackingId = ""
    var payMode = ""
    var prefs: Preferences? = null
    var paymentType = 2
    var shippingId = ""

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        setContentView(R.layout.activity_webview)
        prefs = Preferences(this@NewWebViewActivity)
        orderId = intent.getStringExtra(AvenuesParams.ORDER_ID).toString()
        webview = findViewById<View>(R.id.webview) as WebView;
        webview!!.settings.javaScriptEnabled = true
        webview!!.settings.setSupportMultipleWindows(true)
        webview!!.settings.javaScriptCanOpenWindowsAutomatically = true
        webview!!.addJavascriptInterface(MyJavaScriptInterface(), "HTMLOUT")
        webview!!.webChromeClient = WebChromeClient()
        webview!!.webViewClient = MyWebViewClient()
        val data =
            "encRequest=" + intent.getStringExtra(AvenuesParams.ENC_VAL) + "&access_code=" + intent.getStringExtra(
                AvenuesParams.ACCESS_CODE
            )// yaha pr dekho
        webview!!.postUrl(Constants.TRANS_URL, data.toByteArray())
    }

    inner class MyWebViewClient : WebViewClient() {
        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(webview, url)
            if (url.equals(
                    intent.getStringExtra(AvenuesParams.REDIRECT_URL),
                    ignoreCase = true
                ) || url.equals(intent.getStringExtra(AvenuesParams.CANCEL_URL), ignoreCase = true)
            ) {
                val url2 =
                    "javascript:window.HTMLOUT.processHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');"
                webview!!.loadUrl(url2)
            }
        }
    }

    inner class MyJavaScriptInterface {
        @JavascriptInterface
        fun processHTML(html: String) {
            // process the html source code to get final status of transaction
            var status: String? = null
            if (html.indexOf("Failure") != -1) {
                status = "Transaction Declined!"
                getStringFromHtml(html)
                Toast.makeText(this@NewWebViewActivity, status, Toast.LENGTH_SHORT).show()
            } else if (html.indexOf("Success") != -1) {
                status = "Order Successful!"
                getStringFromHtml(html)
                Toast.makeText(this@NewWebViewActivity, status, Toast.LENGTH_SHORT).show()

                //                updatePayment(status, orderId_cc);
            } else if (html.indexOf("Aborted") != -1) {
                status = "Transaction Cancelled!"
                Toast.makeText(this@NewWebViewActivity, status, Toast.LENGTH_SHORT).show()

                getStringFromHtml(html)
            } else if (html.indexOf("Initiated") != -1) {
                status = "Transaction Cancelled!"
                Toast.makeText(this@NewWebViewActivity, status, Toast.LENGTH_SHORT).show()

                getStringFromHtml(html)
            } else {
                status = "Status Not Known!"
                Toast.makeText(this@NewWebViewActivity, status, Toast.LENGTH_SHORT).show()

            }
        }
    }

    private fun getStringFromHtml(html: String?) {
        if (html == null || html.length == 0) {
            return
        }
        var htmlSt = html.trim { it <= ' ' }
        htmlSt = htmlSt.replace("<head>", "")
        htmlSt = htmlSt.replace("</head>", "")
        htmlSt = htmlSt.replace("</body>", "")
        htmlSt = htmlSt.replace("<body>", "")
        htmlSt = htmlSt.replace("amp;", "")
        val spliString = htmlSt.split("&".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()
        for (st in spliString) {
            if (st.contains("order_id")) {
                orderId_cc =
                    st.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
            } else if (st.contains("tracking_id")) {
                trackingId =
                    st.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
            } else if (st.contains("bank_ref_no")) {
                bankRefNum =
                    st.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
            } else if (st.contains("payment_mode")) {
                payMode = st.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
            }
        }
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
            AppUtils.showToast(this@NewWebViewActivity, AppsStrings.defResponse)
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
                        AppUtils.showToast(this@NewWebViewActivity, AppsStrings.defResponse)
                        Log.v(AppsStrings.apiErrorCode, "onError errorCode : " + error.errorCode)
                        Log.v(AppsStrings.apiErrorBody, "onError errorBody : " + error.errorBody)
                        Log.v(
                            AppsStrings.apiErrorDetail, "onError errorDetail : " + error.errorDetail
                        )
                    } else {
                        AppUtils.showToast(this@NewWebViewActivity, AppsStrings.defResponse)
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

               /* if (paymentType == 2) {
                    startPayment(payableAmount,jsonObject.getString("order_id"))
                }else {*/

                    val intent = Intent(this@NewWebViewActivity, OrderPlaced::class.java)
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
                   /* }*/
                    startActivity(intent)
                }
            }
        } catch (e: Exception) {
            Log.v(AppsStrings.apiErrorException, e.message!!)
            AppUtils.showToast(this@NewWebViewActivity, AppsStrings.defResponse)
        }
    }
}