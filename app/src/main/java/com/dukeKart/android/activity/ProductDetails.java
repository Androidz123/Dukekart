package com.dukeKart.android.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.android.material.tabs.TabLayout;
import com.dukeKart.android.R;
import com.dukeKart.android.common.AppUrls;
import com.dukeKart.android.common.AppsStrings;
import com.dukeKart.android.database.AppConstants;
import com.dukeKart.android.database.Preferences;
import com.dukeKart.android.views.BaseActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import static com.dukeKart.android.common.AppUtils.setImgPicasso;
import static com.dukeKart.android.common.AppUtils.setPrice;
import static com.dukeKart.android.common.AppUtils.showToast;
import static com.dukeKart.android.common.AppUtils.updateQuantity;
import static com.dukeKart.android.common.AppsStrings.apiErrorException;
import static com.dukeKart.android.common.AppsStrings.defResponse;

public class ProductDetails extends BaseActivity implements View.OnClickListener {

    HashMap<String, String> productDetails;
    ArrayList<String> bannerList = new ArrayList<>();
    ArrayList<HashMap<String, String>> colorList = new ArrayList<>();
    ArrayList<HashMap<String, String>> sizeList = new ArrayList<>();
    ArrayList<HashMap<String, String>> keyFeatureList = new ArrayList<>();
    ScrollView sv_productDetail;
    ViewPager viewPager;
    TabLayout tabLayout;
    RelativeLayout rl_wishlist_add, rlCart;
    Preferences prefs;
    TextView tv_wishlist, tv_productname,tv_prodweight, tv_title, tv_sku, tv_categories, tv_brand,
            tv_productSellerName, tv_soldPriceHead, tv_productPriceDiscount, tv_description,
            tv_cartQuantity;
    ImageView iv_search, iv_back;
    RecyclerView rv_Color, rv_Size, rv_keyFeature;
    Button btn_addToCart,btn_buyNow;
    RelativeLayout rl_cartFooter;
    ImageView iv_Minus, iv_Add;
    String cartQuantity = "";
    TextView tvCart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);
        setIds();
        setListeners();
        setHeaders();
        try {
            if (!getIntent().getStringExtra(AppConstants.productId).isEmpty())
                productDetailApi(getIntent().getStringExtra(AppConstants.productId));
        } catch (Exception e) {
            e.printStackTrace();
            showToast(mActivity, defResponse);
        }
    }

    private void setIds() {
        prefs = new Preferences(mActivity);
        sv_productDetail = findViewById(R.id.sv_productDetail);
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        rl_wishlist_add = findViewById(R.id.rl_wishlist_add);
        tv_wishlist = findViewById(R.id.tv_wishlist);
        tv_productname = findViewById(R.id.tv_productname);
        tv_sku = findViewById(R.id.tv_sku);
        tv_categories = findViewById(R.id.tv_categories);
        tv_title = findViewById(R.id.tv_title);
        iv_search = findViewById(R.id.iv_search);
        iv_back = findViewById(R.id.iv_back);
        tv_prodweight = findViewById(R.id.tv_prodweight);
        rlCart = findViewById(R.id.rlCart);
        btn_buyNow = findViewById(R.id.btn_buyNow);
        tv_brand = findViewById(R.id.tv_brand);
        tv_description = findViewById(R.id.tv_description);
        tv_productSellerName = findViewById(R.id.tv_productSellerName);
        tv_soldPriceHead = findViewById(R.id.tv_soldPriceHead);
        tv_productPriceDiscount = findViewById(R.id.tv_productPriceDiscount);
        rv_Color = findViewById(R.id.rv_Color);
        tvCart = findViewById(R.id.tvCart);
        rv_Size = findViewById(R.id.rv_Size);
        rv_keyFeature = findViewById(R.id.rv_keyFeature);
        btn_addToCart = findViewById(R.id.btn_addToCart);
        rl_cartFooter = findViewById(R.id.rl_cartFooter);
        iv_Minus = findViewById(R.id.iv_Minus);
        iv_Add = findViewById(R.id.iv_Add);
        tv_cartQuantity = findViewById(R.id.tv_cartQuantity);
    }

    private void setHeaders() {
        iv_search.setVisibility(View.VISIBLE);
        rlCart.setVisibility(View.VISIBLE);
        sv_productDetail.setVisibility(View.INVISIBLE);
    }

    private void setListeners() {
        btn_addToCart.setOnClickListener(this);
        rl_wishlist_add.setOnClickListener(this);
        iv_back.setOnClickListener(this);
        iv_Minus.setOnClickListener(this);
        iv_Add.setOnClickListener(this);
        iv_search.setOnClickListener(this);
        rlCart.setOnClickListener(this);
        btn_buyNow.setOnClickListener(this);
    }

    private void setAdapters() {
        //setImageAdapter
        Log.v("MyProductBanners", bannerList.toString());
        ImageSliderAdapter imageSliderPagerAdapter = new ImageSliderAdapter(bannerList);
        viewPager.setAdapter(imageSliderPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        int length = tabLayout.getTabCount();
        for (int i = 0; i < length; i++) {
            View tab = ((ViewGroup) tabLayout.getChildAt(0)).getChildAt(i);
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) tab.getLayoutParams();
            p.setMargins(0, 0, 15, 0);
            tab.requestLayout();
            tabLayout.getTabAt(i).setCustomView(imageSliderPagerAdapter.getTabView(i));
        }

        rv_Color.setHasFixedSize(true);
        rv_Color.setLayoutManager(new LinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false));
        rv_Color.setNestedScrollingEnabled(false);
      /*  ColorsAdapter colorsAdapter = new ColorsAdapter(colorList);
        rv_Color.setAdapter(colorsAdapter);*/


        rv_Size.setHasFixedSize(true);
        rv_Size.setLayoutManager(new LinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false));
        rv_Size.setNestedScrollingEnabled(false);
       /* SizeAdapter sizeAdapter = new SizeAdapter(sizeList);
        rv_Size.setAdapter(sizeAdapter);*/


        rv_keyFeature.setHasFixedSize(true);
        rv_keyFeature.setLayoutManager(new LinearLayoutManager(mActivity));
        rv_keyFeature.setNestedScrollingEnabled(false);
        KeyAdapter keyAdapter = new KeyAdapter(keyFeatureList);
        rv_keyFeature.setAdapter(keyAdapter);


    }

    private void productDetailApi(String productId) {
        showLoading();
        AppsStrings.setApiString("ProductDetailsApi");
        String url = AppUrls.ProductDetails;
        Log.v(AppsStrings.apiUrl, url);
        JSONObject json = new JSONObject();
        JSONObject json_data = new JSONObject();

        try {
            json_data.put("user_id", prefs.get(AppConstants.userId));
            json_data.put("product_id", productId);
            json.put(AppsStrings.BASEJSON, json_data);
            Log.v(AppsStrings.apiJson, json.toString());
        } catch (Exception e) {
            showToast(mActivity, defResponse);
            Log.v(apiErrorException, e.getMessage());
            e.printStackTrace();
        }


        AndroidNetworking.post(url)
                .addJSONObjectBody(json_data)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {

                        parseProductDetailApi(response);
                    }

                    @Override
                    public void onError(ANError error) {
                        sv_productDetail.setVisibility(View.INVISIBLE);
                        hideLoading();
                        // handle error
                        if (error.getErrorCode() != 0) {
                            showToast(mActivity, defResponse);
                            Log.v(AppsStrings.apiErrorCode, "onError errorCode : " + error.getErrorCode());
                            Log.v(AppsStrings.apiErrorBody, "onError errorBody : " + error.getErrorBody());
                            Log.v(AppsStrings.apiErrorDetail, "onError errorDetail : " + error.getErrorDetail());
                        } else {
                            showToast(mActivity, defResponse);
                            Log.v(apiErrorException, error.getMessage());

                        }
                    }
                });
    }

    private void parseProductDetailApi(JSONObject response) {

        Log.d(AppsStrings.apiResponse, response.toString());
        try {
            String colorSelected="",sizeSelected="";
            tvCart.setText(prefs.get(AppConstants.cartQuantity));
            JSONObject jsonObject = response.getJSONObject("ecommerce");
            hideLoading();
            // sizeList.clear();
            // colorList.clear();
            bannerList.clear();
            if (jsonObject.getString("res_code").equalsIgnoreCase("1")) {
                productDetails = new HashMap<>();
                productDetails.put("id", jsonObject.getString("id"));
                //  colorSelected = jsonObject.getString("Color");
                //  sizeSelected = jsonObject.getString("Size");
                productDetails.put("Category", jsonObject.getString("Category"));
                productDetails.put("SKU", jsonObject.getString("SKU"));
                productDetails.put("Weight", jsonObject.getString("Weight"));
                productDetails.put("Unit", jsonObject.getString("Unit"));
                productDetails.put("ProductName", jsonObject.getString("ProductName"));
                productDetails.put("RegularPrice", jsonObject.getString("RegularPrice"));
                productDetails.put("MRP", jsonObject.getString("MRP"));
                productDetails.put("Highlights", jsonObject.getString("Highlights"));
                productDetails.put("Brand", jsonObject.getString("Brand"));
                productDetails.put("Seller", jsonObject.getString("Seller"));
                productDetails.put("Description", jsonObject.getString("Description"));
                productDetails.put("sleev_length", jsonObject.getString("sleev_length"));
                productDetails.put("neckline", jsonObject.getString("neckline"));
                productDetails.put("prints_patterns", jsonObject.getString("prints_patterns"));
                productDetails.put("blouse_piece", jsonObject.getString("blouse_piece"));
                productDetails.put("occasion", jsonObject.getString("occasion"));
                productDetails.put("fit", jsonObject.getString("fit"));
                productDetails.put("combo", jsonObject.getString("combo"));
                productDetails.put("collor", jsonObject.getString("collor"));
                productDetails.put("fabric", jsonObject.getString("fabric"));
                productDetails.put("fabric_care", jsonObject.getString("fabric_care"));
                productDetails.put("pack_of", jsonObject.getString("pack_of"));
                productDetails.put("type", jsonObject.getString("type"));
                productDetails.put("style", jsonObject.getString("style"));
                productDetails.put("length", jsonObject.getString("length"));
                productDetails.put("art_work", jsonObject.getString("art_work"));
                productDetails.put("stretchable", jsonObject.getString("stretchable"));
                productDetails.put("back_type", jsonObject.getString("back_type"));
                productDetails.put("ideal_for", jsonObject.getString("ideal_for"));
                productDetails.put("generic_name", jsonObject.getString("generic_name"));
                productDetails.put("WishlistId", jsonObject.getString("WishlistId"));
                productDetails.put("WishlistStatus", jsonObject.getString("WishlistStatus"));
                productDetails.put("CartStatus", jsonObject.getString("CartStatus"));
                productDetails.put("CartId", jsonObject.getString("CartId"));
                productDetails.put("quantity", jsonObject.getString("quantity"));
                if (!jsonObject.getString("main_image").equalsIgnoreCase("")) {
                    bannerList.add(jsonObject.getString("main_image"));
                }
                if (!jsonObject.getString("image1").equalsIgnoreCase("")) {
                    bannerList.add(jsonObject.getString("image1"));
                }
                if (!jsonObject.getString("image2").equalsIgnoreCase("")) {
                    bannerList.add(jsonObject.getString("image2"));
                }
                if (!jsonObject.getString("image3").equalsIgnoreCase("")) {
                    bannerList.add(jsonObject.getString("image3"));
                }
                if (!jsonObject.getString("image4").equalsIgnoreCase("")) {
                    bannerList.add(jsonObject.getString("image4"));
                }
                if (!jsonObject.getString("image5").equalsIgnoreCase("")) {
                    bannerList.add(jsonObject.getString("image5"));
                }

              /*  JSONArray jsColors = jsonObject.getJSONArray("ColorList");
                for (int i = 0; i < jsColors.length(); i++) {
                    HashMap<String, String> hashColor = new HashMap<>();
                    hashColor.put("product_id", jsColors.getJSONObject(i).getString("product_id"));
                    if (jsColors.getJSONObject(i).getString("color").equalsIgnoreCase(colorSelected))
                        hashColor.put("isActive", "true");
                    else
                        hashColor.put("isActive", "false");

                    hashColor.put("color", jsColors.getJSONObject(i).getString("color"));
                    hashColor.put("color_code", jsColors.getJSONObject(i).getString("color_code"));
                    hashColor.put("main_image", jsColors.getJSONObject(i).getString("main_image"));
                    colorList.add(hashColor);
                }
                JSONArray jsSize = jsonObject.getJSONArray("sizeList");
                for (int i = 0; i < jsSize.length(); i++) {
                    HashMap<String, String> hashSize = new HashMap<>();
                    hashSize.put("product_id", jsSize.getJSONObject(i).getString("product_id"));
                   // hashSize.put("size", jsSize.getJSONObject(i).getString("size"));
                    *//*if (jsSize.getJSONObject(i).getString("size").equalsIgnoreCase(sizeSelected))
                        hashSize.put("isActive", "true");
                    else
                        hashSize.put("isActive", "false");
                    sizeList.add(hashSize);*//*
                }*/
                fillData();
            } else {
                showToast(mActivity, jsonObject.getString("res_msg"));
            }
        } catch (Exception e) {
            sv_productDetail.setVisibility(View.INVISIBLE);
            Log.v(apiErrorException, e.getMessage());
            showToast(mActivity, defResponse);
        }
    }

    private void fillData() {
        try {
            sv_productDetail.setVisibility(View.VISIBLE);
            tv_productname.setText(productDetails.get("ProductName"));
            tv_sku.setText(productDetails.get("SKU"));
            tv_categories.setText(productDetails.get("Category"));
            tv_prodweight.setText(productDetails.get("Weight")+" "+productDetails.get("Unit"));
            tv_brand.setText(productDetails.get("Brand"));
            tv_productSellerName.setText(productDetails.get("Seller"));
            //tv_description.setText(productDetails.get("Description"));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                tv_description.setText(Html.fromHtml(productDetails.get("Description"), Html.FROM_HTML_MODE_COMPACT));
            } else {
                tv_description.setText(Html.fromHtml(productDetails.get("Description")));
            }
            tv_soldPriceHead.setText(setPrice(productDetails.get("RegularPrice")));
            tv_productPriceDiscount.setText(setPrice(productDetails.get("MRP")));
            tv_productPriceDiscount.setPaintFlags(tv_productPriceDiscount.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

            if (productDetails.get("WishlistStatus").equalsIgnoreCase("true")) {
                tv_wishlist.setBackgroundResource(R.drawable.ic_wishlist_fill);
            }
            if (productDetails.get("CartStatus").equalsIgnoreCase("true")) {
                btn_addToCart.setVisibility(View.GONE);
                rl_cartFooter.setVisibility(View.VISIBLE);
                cartQuantity = productDetails.get("quantity");
                tv_cartQuantity.setText(cartQuantity);

            } else {
                rl_cartFooter.setVisibility(View.GONE);
                btn_addToCart.setVisibility(View.VISIBLE);
            }
            keyFeatureList.clear();
/*
            setKeyHashes("Color");
*/
            //setKeyHashes("Size");
            setKeyHashes("Highlights");
            setKeyHashes("sleev_length");
            setKeyHashes("neckline");
            setKeyHashes("prints_patterns");
            setKeyHashes("blouse_piece");
            setKeyHashes("occasion");
            setKeyHashes("fit");
            setKeyHashes("combo");
            setKeyHashes("collor");
            setKeyHashes("fabric");
            setKeyHashes("fabric_care");
            setKeyHashes("pack_of");
            setKeyHashes("type");
            setKeyHashes("style");
            setKeyHashes("length");
            setKeyHashes("art_work");
            setKeyHashes("stretchable");
            setKeyHashes("back_type");
            setKeyHashes("ideal_for");
            setKeyHashes("generic_name");
        } catch (Exception e) {
            e.printStackTrace();
            showToast(mActivity, defResponse);
        }
        setAdapters();
    }

    public void setKeyHashes(String title) {
        String value = "";
        if (productDetails.containsKey(title)) {
            value = productDetails.get(title);
            if ((!value.equalsIgnoreCase("")) && (!value.equalsIgnoreCase(""))) {
                title = title.replaceFirst(title.substring(0, 1), title.substring(0, 1).toUpperCase());
                HashMap hashMap = new HashMap<String, String>();
                hashMap.put("Title", title);
                hashMap.put("Value", value);
                keyFeatureList.add(hashMap);
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_wishlist_add:
                if (prefs.get(AppConstants.userId).equalsIgnoreCase("")) {
                    startActivity(new Intent(mActivity, Login.class));
                } else if (productDetails.get("WishlistStatus").equalsIgnoreCase("true"))
                    removeWishListApi(productDetails.get("WishlistId"));
                else
                    AddWishListApi(productDetails.get("id"));
                break;
            case R.id.iv_back:
                onBackPressed();
                break;
            case R.id.btn_addToCart:
                /*if (prefs.get(AppConstants.userId).equalsIgnoreCase("")) {
                    startActivity(new Intent(mActivity, Login.class));
                } else {*/
                AddToCardApi(productDetails.get("id"));
                /*  }*/
                break;
            case R.id.iv_Minus:
                if (prefs.get(AppConstants.userId).equalsIgnoreCase("")) {
                    startActivity(new Intent(mActivity, Login.class));
                } else if (cartQuantity.equalsIgnoreCase("1")) {
                    RemoveProductFromCartApi(productDetails.get("id"));
                } else {
                    UpdateQantityApi(productDetails.get("id"), Integer.toString(Integer.parseInt(cartQuantity) - 1));
                }
                break;
            case R.id.rlCart:
            case R.id.btn_buyNow:
                if (prefs.get(AppConstants.loginCheck).equalsIgnoreCase("true")) {
                    startActivity(new Intent(mActivity, AddToCart.class));
                } else {
                    startActivity(new Intent(mActivity, Login.class));
                }
                break;
            case R.id.iv_search:
                startActivity(new Intent(mActivity, SearchProducts.class));
                break;
            case R.id.iv_Add:
                if (prefs.get(AppConstants.userId).equalsIgnoreCase("")) {
                    startActivity(new Intent(mActivity, Login.class));
                } else {
                    UpdateQantityApi(productDetails.get("id"), Integer.toString(Integer.parseInt(cartQuantity) + 1));
                }
                break;
        }
    }

    private void UpdateQantityApi(String prodId, String quantity) {
        showLoading();
        AppsStrings.setApiString("UpdateQantity");
        String url = AppUrls.UpdateQantity;
        Log.v(AppsStrings.apiUrl, url);
        JSONObject json = new JSONObject();
        JSONObject json_data = new JSONObject();

        try {
            json_data.put("user_id", prefs.get(AppConstants.userId));
            json_data.put("product_id", prodId);
            json_data.put("quantity", quantity);
            json.put(AppsStrings.BASEJSON, json_data);
            Log.v(AppsStrings.apiJson, json.toString());
        } catch (Exception e) {
            showToast(mActivity, defResponse);
            Log.v(apiErrorException, e.getMessage());
            e.printStackTrace();
        }


        AndroidNetworking.post(url)
                .addJSONObjectBody(json_data)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        parseUpdateQantityApi(response);
                    }

                    @Override
                    public void onError(ANError error) {

                        hideLoading();
                        // handle error
                        if (error.getErrorCode() != 0) {
                            showToast(mActivity, defResponse);
                            Log.v(AppsStrings.apiErrorCode, "onError errorCode : " + error.getErrorCode());
                            Log.v(AppsStrings.apiErrorBody, "onError errorBody : " + error.getErrorBody());
                            Log.v(AppsStrings.apiErrorDetail, "onError errorDetail : " + error.getErrorDetail());
                        } else {
                            showToast(mActivity, defResponse);
                            Log.v(apiErrorException, error.getMessage());

                        }
                    }
                });
    }

    private void parseUpdateQantityApi(JSONObject response) {

        Log.d(AppsStrings.apiResponse, response.toString());
        try {
            JSONObject jsonObject = response.getJSONObject("ecommerce");
            hideLoading();
            if (jsonObject.getString("res_code").equalsIgnoreCase("1")) {
                cartQuantity = jsonObject.getString("quantity");
                tv_cartQuantity.setText(cartQuantity);
            }
            showToast(mActivity, jsonObject.getString("res_msg"));
        } catch (Exception e) {
            Log.v(apiErrorException, e.getMessage());
            showToast(mActivity, defResponse);
        }
    }

    private void AddToCardApi(String prodId) {
        showLoading();
        AppsStrings.setApiString("AddToCard");
        String url = AppUrls.AddToCard;
        Log.v(AppsStrings.apiUrl, url);
        JSONObject json = new JSONObject();
        JSONObject json_data = new JSONObject();

        try {
            json_data.put("user_id", prefs.get(AppConstants.userId));
            json_data.put("product_id", prodId);
            json.put(AppsStrings.BASEJSON, json_data);
            Log.v(AppsStrings.apiJson, json.toString());
        } catch (Exception e) {
            showToast(mActivity, defResponse);
            Log.v(apiErrorException, e.getMessage());
            e.printStackTrace();
        }


        AndroidNetworking.post(url)
                .addJSONObjectBody(json_data)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {

                        parseAddToCardApi(response);
                    }

                    @Override
                    public void onError(ANError error) {

                        hideLoading();
                        // handle error
                        if (error.getErrorCode() != 0) {
                            showToast(mActivity, defResponse);
                            Log.v(AppsStrings.apiErrorCode, "onError errorCode : " + error.getErrorCode());
                            Log.v(AppsStrings.apiErrorBody, "onError errorBody : " + error.getErrorBody());
                            Log.v(AppsStrings.apiErrorDetail, "onError errorDetail : " + error.getErrorDetail());
                        } else {
                            showToast(mActivity, defResponse);
                            Log.v(apiErrorException, error.getMessage());

                        }
                    }
                });
    }

    private void parseAddToCardApi(JSONObject response) {

        Log.d(AppsStrings.apiResponse, response.toString());
        try {
            JSONObject jsonObject = response.getJSONObject("ecommerce");
            hideLoading();
            if (jsonObject.getString("res_code").equalsIgnoreCase("1")) {
                btn_addToCart.setVisibility(View.GONE);
                rl_cartFooter.setVisibility(View.VISIBLE);
                cartQuantity = "1";
                prefs.set(AppConstants.cartQuantity, updateQuantity(prefs.get(AppConstants.cartQuantity), 1)).commit();
                tv_cartQuantity.setText(cartQuantity);
                tvCart.setText(prefs.get(AppConstants.cartQuantity));
            }
            showToast(mActivity, jsonObject.getString("res_msg"));
        } catch (Exception e) {
            Log.v("res_msg","res_msg");
            Log.v(apiErrorException, e.getMessage());
            showToast(mActivity, defResponse);
        }
    }

    private void RemoveProductFromCartApi(String prodId) {
        showLoading();
        AppsStrings.setApiString("RemoveProductFromCart");
        String url = AppUrls.RemoveProductFromCart;
        Log.v(AppsStrings.apiUrl, url);
        JSONObject json = new JSONObject();
        JSONObject json_data = new JSONObject();

        try {
            json_data.put("user_id", prefs.get(AppConstants.userId));
            json_data.put("product_id", prodId);
            json.put(AppsStrings.BASEJSON, json_data);
            Log.v(AppsStrings.apiJson, json.toString());
        } catch (Exception e) {
            showToast(mActivity, defResponse);
            Log.v(apiErrorException, e.getMessage());
            e.printStackTrace();
        }


        AndroidNetworking.post(url)
                .addJSONObjectBody(json_data)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        parseRemoveProductFromCartApi(response);
                    }

                    @Override
                    public void onError(ANError error) {

                        hideLoading();
                        // handle error
                        if (error.getErrorCode() != 0) {
                            showToast(mActivity, defResponse);
                            Log.v(AppsStrings.apiErrorCode, "onError errorCode : " + error.getErrorCode());
                            Log.v(AppsStrings.apiErrorBody, "onError errorBody : " + error.getErrorBody());
                            Log.v(AppsStrings.apiErrorDetail, "onError errorDetail : " + error.getErrorDetail());
                        } else {
                            showToast(mActivity, defResponse);
                            Log.v(apiErrorException, error.getMessage());

                        }
                    }
                });
    }

    private void parseRemoveProductFromCartApi(JSONObject response) {

        Log.d(AppsStrings.apiResponse, response.toString());
        try {
            JSONObject jsonObject = response.getJSONObject("ecommerce");
            hideLoading();
            if (jsonObject.getString("res_code").equalsIgnoreCase("1")) {
                btn_addToCart.setVisibility(View.VISIBLE);
                rl_cartFooter.setVisibility(View.GONE);
                cartQuantity = "1";
                prefs.set(AppConstants.cartQuantity, updateQuantity(prefs.get(AppConstants.cartQuantity), -1)).commit();
                tvCart.setText(prefs.get(AppConstants.cartQuantity));
                tv_cartQuantity.setText(cartQuantity);
            }
            showToast(mActivity, jsonObject.getString("res_msg"));
        } catch (Exception e) {
            Log.v(apiErrorException, e.getMessage());
            showToast(mActivity, defResponse);
        }
    }

    private void AddWishListApi(String wishId) {
        showLoading();
        AppsStrings.setApiString("AddWishList");
        String url = AppUrls.AddWishList;
        Log.v(AppsStrings.apiUrl, url);
        JSONObject json = new JSONObject();
        JSONObject json_data = new JSONObject();

        try {
            json_data.put("user_id", prefs.get(AppConstants.userId));
            json_data.put("product_id", wishId);
            json.put(AppsStrings.BASEJSON, json_data);
            Log.v(AppsStrings.apiJson, json.toString());
        } catch (Exception e) {
            showToast(mActivity, defResponse);
            Log.v(apiErrorException, e.getMessage());
            e.printStackTrace();
        }


        AndroidNetworking.post(url)
                .addJSONObjectBody(json_data)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onResponse(JSONObject response) {
                        parseAddWishListApi(response);
                    }

                    @Override
                    public void onError(ANError error) {

                        hideLoading();
                        // handle error
                        if (error.getErrorCode() != 0) {
                            showToast(mActivity, defResponse);
                            Log.v(AppsStrings.apiErrorCode, "onError errorCode : " + error.getErrorCode());
                            Log.v(AppsStrings.apiErrorBody, "onError errorBody : " + error.getErrorBody());
                            Log.v(AppsStrings.apiErrorDetail, "onError errorDetail : " + error.getErrorDetail());
                        } else {
                            showToast(mActivity, defResponse);
                            Log.v(apiErrorException, error.getMessage());

                        }
                    }
                });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void parseAddWishListApi(JSONObject response) {

        Log.d(AppsStrings.apiResponse, response.toString());
        try {
            JSONObject jsonObject = response.getJSONObject("ecommerce");
            hideLoading();
            if (jsonObject.getString("res_code").equalsIgnoreCase("1")) {
                productDetails.replace("WishlistStatus", "false", "true");
                tv_wishlist.setBackgroundResource(R.drawable.ic_wishlist_fill);
            }
            showToast(mActivity, jsonObject.getString("res_msg"));
        } catch (Exception e) {
            Log.v(apiErrorException, e.getMessage());
            showToast(mActivity, defResponse);
        }
    }

    private void removeWishListApi(String wishId) {

        showLoading();
        AppsStrings.setApiString("RemoveWishList");
        String url = AppUrls.RemoveWishList;
        Log.v(AppsStrings.apiUrl, url);
        JSONObject json = new JSONObject();
        JSONObject json_data = new JSONObject();

        try {
            json_data.put("wishlist_id", wishId);
            json.put(AppsStrings.BASEJSON, json_data);
            Log.v(AppsStrings.apiJson, json.toString());
        } catch (Exception e) {
            showToast(mActivity, defResponse);
            Log.v(apiErrorException, e.getMessage());
            e.printStackTrace();
        }


        AndroidNetworking.post(url)
                .addJSONObjectBody(json_data)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onResponse(JSONObject response) {
                        parseRemoveWishListApi(response);
                    }

                    @Override
                    public void onError(ANError error) {

                        hideLoading();
                        // handle error
                        if (error.getErrorCode() != 0) {
                            showToast(mActivity, defResponse);
                            Log.v(AppsStrings.apiErrorCode, "onError errorCode : " + error.getErrorCode());
                            Log.v(AppsStrings.apiErrorBody, "onError errorBody : " + error.getErrorBody());
                            Log.v(AppsStrings.apiErrorDetail, "onError errorDetail : " + error.getErrorDetail());
                        } else {
                            showToast(mActivity, defResponse);
                            Log.v(apiErrorException, error.getMessage());

                        }
                    }
                });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void parseRemoveWishListApi(JSONObject response) {

        Log.d(AppsStrings.apiResponse, response.toString());
        hideLoading();
        try {
            JSONObject jsonObject = response.getJSONObject("ecommerce");
            if (jsonObject.getString("res_code").equalsIgnoreCase("1")) {
                productDetails.replace("WishlistStatus", "true", "false");
                tv_wishlist.setBackgroundResource(R.drawable.ic_whishlist_blank);
            }

            showToast(mActivity, jsonObject.getString("res_msg"));

        } catch (Exception e) {
            hideLoading();
            Log.v(apiErrorException, e.getMessage());
            showToast(mActivity, defResponse);
        }
    }

    @Override
    public void onBackPressed() {
        //startActivity(new Intent(mActivity, Dashboard.class));
        finish();
    }

    public class ImageSliderAdapter extends PagerAdapter {

        LayoutInflater inflater;
        ArrayList<String> imageList;

        public ImageSliderAdapter(ArrayList<String> imageList) {
            try {

                this.imageList = imageList;
                this.inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            } catch (Exception e) {
                e.printStackTrace();
                showToast(mActivity, defResponse);

            }

        }

        @Override
        public int getCount() {
            return imageList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            View itemView = inflater.inflate(R.layout.multiple_product, container, false);

            ImageView imageView = itemView.findViewById(R.id.img_view);
            if (imageList.get(position).length() > 0) {
                setImgPicasso(imageList.get(position), mActivity, imageView);
            }


            container.addView(itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(mActivity, FullScreenImage.class);
                    intent.putStringArrayListExtra(AppConstants.arrayImages, imageList);
                    intent.putExtra(AppConstants.imageSelected, position);
                    startActivity(intent);

                }
            });

            return itemView;
        }

        public View getTabView(int position) {

            View view = LayoutInflater.from(mActivity).inflate(R.layout.tab_layout, null);
            ImageView icon = view.findViewById(R.id.icon);
            if (imageList.get(position).length() > 0) {
                setImgPicasso(imageList.get(position), mActivity, icon);
            }


            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

    private class ColorsAdapter extends RecyclerView.Adapter<ColorsAdapter.MyViewHolder> {

        ArrayList<HashMap<String, String>> arrayList1;

        public ColorsAdapter(ArrayList<HashMap<String, String>> arrayListEvent) {
            arrayList1 = arrayListEvent;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_color, viewGroup, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, @SuppressLint("RecyclerView") int i) {
            String imgUrl = arrayList1.get(i).get("main_image");
//            Log.v("MyCategImages", "a " + imgUrl);
            setImgPicasso(imgUrl, mActivity, myViewHolder.img_color);
            if (arrayList1.get(i).get("isActive").equalsIgnoreCase("true"))
                myViewHolder.cl_main.setBackground(mActivity.getResources().getDrawable(R.drawable.selected_box));
            else
                myViewHolder.cl_main.setBackground(mActivity.getResources().getDrawable(R.drawable.et_bg));

            myViewHolder.cl_main.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!arrayList1.get(i).get("isActive").equalsIgnoreCase("true"))
                        productDetailApi(arrayList1.get(i).get("product_id"));

                }
            });
        }

        @Override
        public int getItemCount() {
            return arrayList1.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            ImageView img_color;
            ConstraintLayout cl_main;


            public MyViewHolder(@NonNull View itemView) {
                super(itemView);

                img_color = itemView.findViewById(R.id.img_color);
                cl_main = itemView.findViewById(R.id.cl_main);


            }
        }
    }

    private class SizeAdapter extends RecyclerView.Adapter<SizeAdapter.MyViewHolder> {

        ArrayList<HashMap<String, String>> arrayList1;

        public SizeAdapter(ArrayList<HashMap<String, String>> arrayListEvent) {
            arrayList1 = arrayListEvent;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_size, viewGroup, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, @SuppressLint("RecyclerView") int i) {

            // myViewHolder.tv_sizename.setText(arrayList1.get(i).get("size"));
            if (arrayList1.get(i).get("isActive").equalsIgnoreCase("true"))
                myViewHolder.rl_main.setBackground(mActivity.getResources().getDrawable(R.drawable.selected_box));
            else
                myViewHolder.rl_main.setBackground(mActivity.getResources().getDrawable(R.drawable.et_bg));

            myViewHolder.rl_main.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!arrayList1.get(i).get("isActive").equalsIgnoreCase("true"))
                        productDetailApi(arrayList1.get(i).get("product_id"));

                }
            });
        }

        @Override
        public int getItemCount() {
            return arrayList1.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            TextView tv_sizename;
            RelativeLayout rl_main;


            public MyViewHolder(@NonNull View itemView) {
                super(itemView);

                tv_sizename = itemView.findViewById(R.id.tv_sizename);
                rl_main = itemView.findViewById(R.id.rl_main);


            }
        }
    }

    private class KeyAdapter extends RecyclerView.Adapter<KeyAdapter.MyViewHolder> {

        ArrayList<HashMap<String, String>> arrayList1;

        public KeyAdapter(ArrayList<HashMap<String, String>> arrayListEvent) {
            arrayList1 = arrayListEvent;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_keyfeatures, viewGroup, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {

            myViewHolder.tv_keyValue.setText(arrayList1.get(i).get("Value"));
            myViewHolder.tv_keyTitle.setText(arrayList1.get(i).get("Title"));


        }

        @Override
        public int getItemCount() {
            return arrayList1.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            TextView tv_keyValue, tv_keyTitle;


            public MyViewHolder(@NonNull View itemView) {
                super(itemView);

                tv_keyValue = itemView.findViewById(R.id.tv_keyValue);
                tv_keyTitle = itemView.findViewById(R.id.tv_keyTitle);


            }
        }
    }
}