package com.dukeKart.android.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.dukeKart.android.activity.AddToCart;
import com.dukeKart.android.activity.SearchProducts;
import com.dukeKart.android.activity.SubSubProductCategory;
import com.google.android.material.tabs.TabLayout;
import com.dukeKart.android.R;
import com.dukeKart.android.activity.AllProducts;
import com.dukeKart.android.activity.Login;
import com.dukeKart.android.activity.ProductDetails;
import com.dukeKart.android.activity.SubProductCategory;
import com.dukeKart.android.common.AppUrls;
import com.dukeKart.android.common.AppsStrings;
import com.dukeKart.android.common.GridSpacingItemDecoration;
import com.dukeKart.android.database.AppConstants;
import com.dukeKart.android.database.Preferences;
import com.dukeKart.android.views.BaseFragment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import static com.dukeKart.android.common.AppUtils.dpToPx;
import static com.dukeKart.android.common.AppUtils.setImgPicasso;
import static com.dukeKart.android.common.AppUtils.setPrice;
import static com.dukeKart.android.common.AppUtils.showToast;
import static com.dukeKart.android.common.AppUtils.updateQuantity;
import static com.dukeKart.android.common.AppsStrings.apiErrorException;
import static com.dukeKart.android.common.AppsStrings.defResponse;


public class Home extends BaseFragment implements View.OnClickListener {

    private final int mInterval = 5000;
    Preferences prefs;
    ImageView ivBack, imgBannerA, imgBannerB, imgBannerC;
    View view;
    CardView card_search;
    TextView tv_timme;
    ViewPager viewPager,viewPager2;
    TabLayout tabLayout,tabLayout2;
    ArrayList<HashMap<String, String>> arraySlider = new ArrayList<>();
    ArrayList<HashMap<String, String>> arrayOfferSlider = new ArrayList<>();
    ArrayList<HashMap<String, String>> arrayCategory = new ArrayList<>();

SearchView search_view;
    ArrayList<HashMap<String, String>> arrayProductdata = new ArrayList<>();
    ArrayList<HashMap<String, String>> arrayKeydata = new ArrayList<>();

    RecyclerView recyclercategory, recyclerfeature;
    int count = 0;
    int id = 0;
    AppCompatEditText search_edit_text;
    private Handler mHandler;
    TextView tv_FeaturedAll,tv_text,tvCart;
    RelativeLayout  rlCart;
    SwipeRefreshLayout swipeRefresh;
    String key;



    Timer timer;
    final long DELAY_MS = 4000;//delay in milliseconds before task is to be executed
    final long PERIOD_MS = 4000;
    private static int currentPage = 0;

    // ViewPager no offpage
    private static int NUM_PAGES = 0;
    public  static  int VB_PAGES ;
    public static int VB_LOOPS = 1000;
    public static int VB_FIRST_PAGE;
    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                updateStatus(); //this function can change value of mInterval.
            } finally {
                // 100% guarantee that this always happens, even if
                // your update method throws an exception
                mHandler.postDelayed(mStatusChecker, mInterval);
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.home_frag, container, false);
        setIDs();
        setListeners();
        getDashboardApi();
        //Log.v("selectedTitle","array "+arrayKeydataSelected);
        return view;

    }

    private void setAdapters() {
        //set Topselling Cataegory
        recyclercategory.setHasFixedSize(true);
        recyclercategory.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        recyclercategory.setNestedScrollingEnabled(false);
        CategoryAdapter categoryAdapter = new CategoryAdapter(arrayCategory);
        recyclercategory.setAdapter(categoryAdapter);

    }

    public void setRecyclerAdapter(RecyclerView recyclerView) {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        recyclerView.setNestedScrollingEnabled(false);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(mActivity);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(1, dpToPx(1, mActivity), true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    public void setMainRecyclerAdapter(RecyclerView recyclerView) {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        recyclerView.setNestedScrollingEnabled(false);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(mActivity, 2);
        recyclerView.setLayoutManager(mLayoutManager);
//        recyclerView.addItemDecoration(new GridSpacingItemDecoration(1, dpToPx(1, mActivity), true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    public void setIDs() {
        prefs = new Preferences(mActivity);
        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);


        search_edit_text = view.findViewById(R.id.search_edit_text);
        tvCart = view.findViewById(R.id.tvCart);

        rlCart = view.findViewById(R.id.rlCart);
        rlCart.setVisibility(View.VISIBLE);
        viewPager = view.findViewById(R.id.viewPager);
        viewPager2 = view.findViewById(R.id.viewPager2);
        card_search = view.findViewById(R.id.card_search);
        tv_text = view.findViewById(R.id.tv_text);
        search_view = view.findViewById(R.id.search_view);
        tabLayout = view.findViewById(R.id.tabLayout);
        imgBannerA = view.findViewById(R.id.imgBannerA);
        tv_timme = view.findViewById(R.id.tv_timme);
        imgBannerB = view.findViewById(R.id.imgBannerB);
        imgBannerC = view.findViewById(R.id.imgBannerC);
        recyclerfeature = view.findViewById(R.id.recyclerfeature);
        tv_text.setText(prefs.get(AppConstants.userName));

        recyclercategory = view.findViewById(R.id.recyclercategory);
        tv_FeaturedAll = view.findViewById(R.id.tv_FeaturedAll);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        mHandler = new Handler();
        setRecyclerAdapter(recyclerfeature);
        if(timeOfDay >= 0 && timeOfDay < 12){
            tv_timme.setText("Good Morning");
        }else if(timeOfDay >= 12 && timeOfDay < 16){
            tv_timme.setText("Good Afternoon");
        }else if(timeOfDay >= 16 && timeOfDay < 21){
            tv_timme.setText("Good Evening");
        }else if(timeOfDay < 24){
            tv_timme.setText("Good Night");
        }
    }

    public void setListeners() {
        tv_FeaturedAll.setOnClickListener(this);
        card_search.setOnClickListener(this);
        search_view.setOnClickListener(this);
        search_edit_text.setOnClickListener(this);
        rlCart.setOnClickListener(this);
        swipeRefresh.setColorScheme(R.color.colorAccent, R.color.colorAccent, R.color.green, R.color.orange);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getDashboardApi();
                swipeRefresh.setRefreshing(false);
            }
        });

    }

    private void getDashboardApi() {
        showLoading();
        AppsStrings.setApiString("Dashbord");
        String url = AppUrls.Dashbord1;
        Log.v(AppsStrings.apiUrl, url);
        JSONObject json = new JSONObject();
        JSONObject json_data = new JSONObject();

        try {
            if (prefs.get(AppConstants.userId).equalsIgnoreCase(""))
                json_data.put("user_id", "1");
            else
                json_data.put("user_id", prefs.get(AppConstants.userId));

            json.put("dukeKart", json_data);
            Log.v(AppsStrings.apiJson, json.toString());
        } catch (Exception e) {
           // showToast(mActivity, defResponse);
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
                        parseDashboardApi(response);
                    }

                    @Override
                    public void onError(ANError error) {

                        hideLoading();
                        // handle error
                        if (error.getErrorCode() != 0) {
                            //showToast(mActivity, defResponse);
                            Log.v(AppsStrings.apiErrorCode, "onError errorCode : " + error.getErrorCode());
                            Log.v(AppsStrings.apiErrorBody, "onError errorBody : " + error.getErrorBody());
                            Log.v(AppsStrings.apiErrorDetail, "onError errorDetail : " + error.getErrorDetail());
                        } else {
                            //showToast(mActivity, defResponse);
                            Log.v(apiErrorException, error.getMessage());

                        }
                    }
                });
    }

    private void parseDashboardApi(JSONObject response) {
        Log.d("testApi", response.toString());
        try {
            tvCart.setText(prefs.get(AppConstants.cartQuantity));

            JSONObject jsonObject = response.getJSONObject("ecommerce");
            hideLoading();
            if (jsonObject.getString("res_code").equalsIgnoreCase("1")) {
                JSONObject jsonData = jsonObject.getJSONObject("Data");
                if (jsonData.has("Banner1"))
                    Glide.with(imgBannerA.getContext()).load(jsonData.get("Banner1")).into(imgBannerA);
//                    setImgPicasso(jsonData.getString("Banner1"), mActivity, imgBannerA);
                if (jsonData.has("Banner2"))
                    Glide.with(imgBannerB.getContext()).load(jsonData.get("Banner1")).into(imgBannerB);
//                    setImgPicasso(jsonData.getString("Banner2"), mActivity, imgBannerB);
                if (jsonData.has("Banner3"))
                    Glide.with(imgBannerC.getContext()).load(jsonData.get("Banner1")).into(imgBannerC);
//                    setImgPicasso(jsonData.getString("Banner3"), mActivity, imgBannerC);
               /* if (jsonData.has("Banner4"))

                    setImgPicasso(jsonData.getString("Banner4"), mActivity, imgBannerA);*/
                arraySlider.clear();
                JSONArray sliderData = jsonData.getJSONArray("Slider");
                for (int i = 0; i < sliderData.length(); i++) {
                    HashMap<String, String> sliderValue = new HashMap<>();
                    sliderValue.put("Id", sliderData.getJSONObject(i).getString("Id"));
                    sliderValue.put("image", sliderData.getJSONObject(i).getString("image"));
                    arraySlider.add(sliderValue);
                }

               /* PagerAdapter adapter = new CustomAdapter(Home.this,imageId,imagesName);
                viewPager.setAdapter(adapter);*/

                /*After setting the adapter use the timer */



                final Handler handler = new Handler();
                currentPage++;
                NUM_PAGES = arraySlider.size();

                VB_PAGES=arraySlider.size();
                VB_FIRST_PAGE= VB_PAGES / 2;
                final Runnable Update = new Runnable() {
                    public void run() {
                        if (currentPage == NUM_PAGES-1) {
                            currentPage = 0;
                        }
                        viewPager.setCurrentItem(currentPage++, true);
                    }
                };

                timer = new Timer(); // This will create a new Thread
                timer.schedule(new TimerTask() { // task to be scheduled
                    @Override
                    public void run() {
                        handler.post(Update);
                    }
                }, DELAY_MS, PERIOD_MS);
              ImageSliderPagerAdapter imageSliderPagerAdapter = new ImageSliderPagerAdapter(arraySlider);

                viewPager.setAdapter(imageSliderPagerAdapter);
               /* tabLayout.setupWithViewPager(viewPager);*/

                 /* viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                    }

                    @Override
                    public void onPageSelected(int position) {

                        count = position;

                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {

                    }
                });
*/
                JSONArray OfferSlider = jsonData.getJSONArray("OfferSlider");
                for (int i = 0; i < OfferSlider.length(); i++) {
                    HashMap<String, String> sliderValue2 = new HashMap<>();
                    sliderValue2.put("Id", OfferSlider.getJSONObject(i).getString("Id"));
                    sliderValue2.put("image", OfferSlider.getJSONObject(i).getString("image"));
                    arrayOfferSlider.add(sliderValue2);
                }

                ImageSliderPagerAdapter imageSliderPagerAdapter2 = new ImageSliderPagerAdapter(arrayOfferSlider);

                viewPager2.setAdapter(imageSliderPagerAdapter2);
               /* tabLayout2.setupWithViewPager(viewPager2);*/

                viewPager2.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                    }

                    @Override
                    public void onPageSelected(int position) {

                        count = position;

                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {

                    }
                });


                arrayCategory.clear();
                JSONArray categoryData = jsonData.getJSONArray("Main Categories");
                for (int i = 0; i < categoryData.length(); i++) {
                    HashMap<String, String> categoryValue = new HashMap<>();
                    categoryValue.put("Id", categoryData.getJSONObject(i).getString("Id"));
                    categoryValue.put("Name", categoryData.getJSONObject(i).getString("Name"));
                    categoryValue.put("Thumb", categoryData.getJSONObject(i).getString("Thumb"));
                    arrayCategory.add(categoryValue);
                }
                arrayProductdata.clear();
                arrayKeydata.clear();
                JSONArray productData = jsonData.getJSONArray("productData");

                for (int j = 0; j < productData.length(); j++) {
                    JSONObject actor = productData.getJSONObject(j);
                    Iterator<?> keys = actor.keys();
                    while (keys.hasNext()) {
                        key = (String) keys.next();
                        if (actor.get(key) instanceof JSONArray) {
                            JSONArray jsonArray = actor.getJSONArray(key);
                            for (int l = 0; l < jsonArray.length(); l++) {
                                HashMap<String, String> hashMap = new HashMap<>();
                                hashMap.put("courseId", Integer.toString(count));
                                //hashMap.put("Id",jsonArray.getJSONObject(l).get("Id"));

                                hashMap.put("CartStatus", jsonArray.getJSONObject(l).getString("CartStatus"));
                                hashMap.put("Id", jsonArray.getJSONObject(l).getString("Id"));
                                hashMap.put("Category", jsonArray.getJSONObject(l).getString("Category"));
                                hashMap.put("SKU", jsonArray.getJSONObject(l).getString("SKU"));
                                hashMap.put("Color", jsonArray.getJSONObject(l).getString("Color"));
                                hashMap.put("Size", jsonArray.getJSONObject(l).getString("Size"));
                                hashMap.put("ProductName", jsonArray.getJSONObject(l).getString("ProductName"));
                                hashMap.put("RegularPrice", jsonArray.getJSONObject(l).getString("RegularPrice"));
                                hashMap.put("MRP", jsonArray.getJSONObject(l).getString("MRP"));
                              /*  hashMap.put("ImagePrimaryURL", jsonArray.getJSONObject(l).getString("ImagePrimaryURL"));
                                hashMap.put("ProductRating", jsonArray.getJSONObject(l).getString("ProductRating"));
                              */  hashMap.put("ImagePrimary", jsonArray.getJSONObject(l).getString("ImagePrimary"));
                                hashMap.put("Tags", jsonArray.getJSONObject(l).getString("Tags"));
                                hashMap.put("WishlistStatus", jsonArray.getJSONObject(l).getString("WishlistStatus"));
                                hashMap.put("WishlistId", jsonArray.getJSONObject(l).getString("WishlistId"));
                                hashMap.put("discount_percentage", jsonArray.getJSONObject(l).getString("discount_percentage"));

                                arrayProductdata.add(hashMap);
                            }
                            HashMap<String, String> hashMap2 = new HashMap<>();
                            hashMap2.put("Array", key);
                            hashMap2.put("Id", Integer.toString(count));
                            arrayKeydata.add(hashMap2);
                            count++;

                        }
                    }

                }


                for (int j = 0; j < productData.length(); j++) {
                    searchArray(id++);
                }
                Log.d("TestTag", "reached 325");

//                arrayFeatured = gettingProductsList(jsonData.getJSONArray("Featured Products"));
//                arrayTopSelling.clear();
//                arrayTopSelling = gettingProductsList(jsonData.getJSONArray("Top Best Selling"));
//                arrayRecommendedForYou.clear();
//                arrayRecommendedForYou = gettingProductsList(jsonData.getJSONArray("Recommended for You"));
//                arrayMenTshirts.clear();
//                arrayMenTshirts = gettingProductsList(jsonData.getJSONArray("Men T-shirts"));
//                arraySarees.clear();
//                arraySarees = gettingProductsList(jsonData.getJSONArray("Sarees"));
            } else {
                showToast(mActivity, jsonObject.getString("res_msg"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.d("TestTag", e.getMessage());
           // showToast(mActivity, defResponse);
        }
        setAdapters();
    }

    private void searchArray(int id) {
        Log.v("selectedTitle", "Id " + id);

        String title = "No Data";
        String selecteId = "";
        ArrayList<HashMap<String, String>> arrayKeydataSelected = new ArrayList<>();
        for (int i = 0; i < arrayKeydata.size(); i++) {
            if (Integer.toString(id).equalsIgnoreCase(arrayKeydata.get(i).get("Id"))) {
                selecteId = arrayKeydata.get(i).get("Id");
                title = arrayKeydata.get(i).get("Array");
            }
        }
        for (int j = 0; j < arrayProductdata.size(); j++) {
            if (selecteId.equalsIgnoreCase(arrayProductdata.get(j).get("courseId"))) {
                arrayKeydataSelected.add(arrayProductdata.get(j));
            }
        }
        Log.v("selectedTitle", "title " + title);
        Log.v("selectedTitle", "array " + arrayKeydataSelected);
        Log.v("selectedTitle", "array " + arrayKeydataSelected.size());

//        setRecyclerAdapter(recyclerfeature);
        MainAdapter featuredAdapter = new MainAdapter(arrayKeydata);
        recyclerfeature.setAdapter(featuredAdapter);
    }

    public ArrayList<HashMap<String, String>> gettingProductsList(JSONArray jsonArray) {
        ArrayList<HashMap<String, String>> hashMapArrayList = new ArrayList<>();
        try {
            int limit = jsonArray.length();
//            if(limit>6){
//                limit=6;
//            }
            for (int i = 0; i < limit; i++) {
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("Id", jsonArray.getJSONObject(i).getString("Id"));
                hashMap.put("Category", jsonArray.getJSONObject(i).getString("Category"));
                hashMap.put("SKU", jsonArray.getJSONObject(i).getString("SKU"));
                hashMap.put("Color", jsonArray.getJSONObject(i).getString("Color"));
                hashMap.put("Size", jsonArray.getJSONObject(i).getString("Size"));
                hashMap.put("ProductName", jsonArray.getJSONObject(i).getString("ProductName"));
                hashMap.put("RegularPrice", jsonArray.getJSONObject(i).getString("RegularPrice"));
                hashMap.put("MRP", jsonArray.getJSONObject(i).getString("MRP"));
                hashMap.put("ImagePrimary", jsonArray.getJSONObject(i).getString("ImagePrimary"));
                hashMap.put("Tags", jsonArray.getJSONObject(i).getString("Tags"));
                hashMap.put("WishlistStatus", jsonArray.getJSONObject(i).getString("WishlistStatus"));
                hashMap.put("WishlistId", jsonArray.getJSONObject(i).getString("WishlistId"));
                hashMapArrayList.add(hashMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hashMapArrayList;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }

    @Override
    public void onStop() {
        super.onStop();

        stopRepeatingTask();
    }

    @Override
    public void onPause() {
        super.onPause();

        stopRepeatingTask();
    }

    void updateStatus() {
      /*  if (count >= arraySlider.size()) {

            count = 0;
        }*/
     if (count >= arrayOfferSlider.size()) {

            count = 0;
        }

       /* viewPager.setCurrentItem(count);*/
        viewPager2.setCurrentItem(count);
        count++;
        Log.v("nos", String.valueOf(count));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_FeaturedAll:
                Intent intentFeature = new Intent(mActivity, AllProducts.class);
                intentFeature.putExtra(AppConstants.proTags, "1");
                startActivity(intentFeature);
                break;
                case R.id.search_view:
                    startActivity(new Intent(mActivity, SearchProducts.class));
                break;
                case R.id.card_search:
                    startActivity(new Intent(mActivity, SearchProducts.class));
                break;
                case R.id.search_edit_text:
                    startActivity(new Intent(mActivity, SearchProducts.class));
                break;
            case R.id.rlCart:

                if (prefs.get(AppConstants.loginCheck).equalsIgnoreCase("true")) {
                    startActivity(new Intent(mActivity, AddToCart.class));
                } else {
                    startActivity(new Intent(mActivity, Login.class));
                }
                break;
        }
    }

    private void AddWishListApi(ArrayList<HashMap<String, String>> arrayList, TextView textView, final int i) {
        showLoading();
        AppsStrings.setApiString("AddWishList");
        String url = AppUrls.AddWishList;
        Log.v(AppsStrings.apiUrl, url);
        JSONObject json = new JSONObject();
        JSONObject json_data = new JSONObject();

        try {
            json_data.put("user_id", prefs.get(AppConstants.userId));
            json_data.put("product_id", arrayList.get(i).get("Id"));
            json.put(AppsStrings.BASEJSON, json_data);
            Log.v(AppsStrings.apiJson, json.toString());
        } catch (Exception e) {
          //  showToast(mActivity, defResponse);
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
                        parseAddWishListApi(response, textView, i, arrayList);
                    }

                    @Override
                    public void onError(ANError error) {

                        hideLoading();
                        // handle error
                        if (error.getErrorCode() != 0) {
                           // showToast(mActivity, defResponse);
                            Log.v(AppsStrings.apiErrorCode, "onError errorCode : " + error.getErrorCode());
                            Log.v(AppsStrings.apiErrorBody, "onError errorBody : " + error.getErrorBody());
                            Log.v(AppsStrings.apiErrorDetail, "onError errorDetail : " + error.getErrorDetail());
                        } else {
                           // showToast(mActivity, defResponse);
                            Log.v(apiErrorException, error.getMessage());

                        }
                    }
                });
    }

    private void parseAddWishListApi(JSONObject response, TextView textView, int i, ArrayList<HashMap<String, String>> arrayList) {

        Log.d(AppsStrings.apiResponse, response.toString());
        try {
            JSONObject jsonObject = response.getJSONObject("ecommerce");
            hideLoading();
            if (jsonObject.getString("res_code").equalsIgnoreCase("1")) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    arrayList.get(i).replace("WishlistStatus", "false", "true");
                }
                textView.setBackgroundResource(R.drawable.ic_wishlist_fill);
            }
            showToast(mActivity, jsonObject.getString("res_msg"));
        } catch (Exception e) {
            Log.v(apiErrorException, e.getMessage());
           // showToast(mActivity, defResponse);
        }
    }

    private void removeWishListApi(ArrayList<HashMap<String, String>> arrayList, TextView textView, final int i) {

        showLoading();
        AppsStrings.setApiString("RemoveWishList");
        String url = AppUrls.RemoveWishList;
        Log.v(AppsStrings.apiUrl, url);
        JSONObject json = new JSONObject();
        JSONObject json_data = new JSONObject();

        try {
//            json_data.put("user_id", prefs.get(AppConstants.userId));
            json_data.put("wishlist_id", arrayList.get(i).get("WishlistId"));
            json.put(AppsStrings.BASEJSON, json_data);
            Log.v(AppsStrings.apiJson, json.toString());
        } catch (Exception e) {
           // showToast(mActivity, defResponse);
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
                        parseRemoveWishListApi(response, textView, i, arrayList);
                    }

                    @Override
                    public void onError(ANError error) {

                        hideLoading();
                        // handle error
                        if (error.getErrorCode() != 0) {
                           // showToast(mActivity, defResponse);
                            Log.v(AppsStrings.apiErrorCode, "onError errorCode : " + error.getErrorCode());
                            Log.v(AppsStrings.apiErrorBody, "onError errorBody : " + error.getErrorBody());
                            Log.v(AppsStrings.apiErrorDetail, "onError errorDetail : " + error.getErrorDetail());
                        } else {
                          //  showToast(mActivity, defResponse);
                            Log.v(apiErrorException, error.getMessage());

                        }
                    }
                });
    }

    private void parseRemoveWishListApi(JSONObject response, TextView textView, int i, ArrayList<HashMap<String, String>> arrayList) {

        Log.d(AppsStrings.apiResponse, response.toString());
        hideLoading();
        try {
            JSONObject jsonObject = response.getJSONObject("ecommerce");
            if (jsonObject.getString("res_code").equalsIgnoreCase("1")) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    arrayList.get(i).replace("WishlistStatus", "true", "false");
                }
                textView.setBackgroundResource(R.drawable.ic_whishlist_blank);
            }
            showToast(mActivity, jsonObject.getString("res_msg"));

        } catch (Exception e) {
            hideLoading();
            Log.v(apiErrorException, e.getMessage());
          //  showToast(mActivity, defResponse);
        }
    }


 /*   public class CustomAdapter extends PagerAdapter{

        private Activity activity;
        private Integer[] imagesArray;
        private String[] namesArray;

        public CustomAdapter(Activity activity,Integer[] imagesArray,String[] namesArray){

            this.activity = activity;
            this.imagesArray = imagesArray;
            this.namesArray = namesArray;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            LayoutInflater inflater = ((Activity)activity).getLayoutInflater();

            View viewItem = inflater.inflate(R.layout.image_item, container, false);
            ImageView imageView = (ImageView) viewItem.findViewById(R.id.imageView);
            imageView.setImageResource(imagesArray[position]);
            TextView textView1 = (TextView) viewItem.findViewById(R.id.textview);
            textView1.setText(namesArray[position]);
            ((ViewPager)container).addView(viewItem);

            return viewItem;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return imagesArray.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            // TODO Auto-generated method stub
            return view == ((View)object);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            // TODO Auto-generated method stub
            ((ViewPager) container).removeView((View) object);
        }
    }*/

    public class ImageSliderPagerAdapter extends PagerAdapter {

        LayoutInflater inflater;
        ArrayList<HashMap<String, String>> imageList;

        public ImageSliderPagerAdapter(ArrayList<HashMap<String, String>> imageList) {
            try {

                this.imageList = imageList;
                this.inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        public int getCount() {
            Log.v("siz", String.valueOf(imageList.size()));
            return imageList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View itemView = inflater.inflate(R.layout.adapter_bannerlist, container, false);

            Log.v("MyImage", "url123" + imageList);

            ImageView imageView = itemView.findViewById(R.id.img_view);


            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            // if (imageList.get(position).getImage().length() > 0){
            Log.v("MyImage", "url" + imageList.get(position));
//            setImgPicasso(imageList.get(position).get("image"), mActivity, imageView);
            Glide.with(container)
                    .load(imageList.get(position).get("image"))
                    .into(imageView);

/*
            Glide.with(context).load(imageList.get(position).getImage())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imageView);
*/

            //}

            container.addView(itemView);


            return itemView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

    private class FeaturedAdapter extends RecyclerView.Adapter<FeaturedAdapter.MyViewHolder> {

        ArrayList<HashMap<String, String>> arrayList1;

        public FeaturedAdapter(ArrayList<HashMap<String, String>> arrayListEvent) {
            arrayList1 = arrayListEvent;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_product, viewGroup, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
            String imgUrl = arrayList1.get(myViewHolder.getAdapterPosition()).get("ImagePrimary");
            Log.v("MyProductImages", "a " + imgUrl);

            Log.v("MyProductImages", "b " + imgUrl);


           setImgPicasso("https://dukekart.in//assets/product_images/"+imgUrl, mActivity, myViewHolder.img_Product);
           /* Glide.with(myViewHolder.img_Product.getContext())
                    .load(imgUrl).into(myViewHolder.img_Product);*/
            myViewHolder.tv_prodTitle.setText(arrayList1.get(myViewHolder.getAdapterPosition()).get("ProductName"));
            myViewHolder.tv_prodPrice.setText(setPrice(arrayList1.get(myViewHolder.getAdapterPosition()).get("RegularPrice")));
            myViewHolder.tv_prodweight.setText(arrayList1.get(myViewHolder.getAdapterPosition()).get("Color")+" "+arrayList1.get(myViewHolder.getAdapterPosition()).get("Size"));
            myViewHolder.tv_prodOldPrice.setText(setPrice(arrayList1.get(myViewHolder.getAdapterPosition()).get("MRP")));

            myViewHolder.tv_proddiscount.setText(arrayList1.get(myViewHolder.getAdapterPosition()).get("discount_percentage")+"% Off");

            myViewHolder.tv_prodOldPrice.setPaintFlags(myViewHolder.tv_prodOldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            if (arrayList1.get(myViewHolder.getAdapterPosition()).get("WishlistStatus").equalsIgnoreCase("true")) {
                myViewHolder.tv_wishlist.setBackgroundResource(R.drawable.ic_wishlist_fill);

            } else {
                myViewHolder.tv_wishlist.setBackgroundResource(R.drawable.ic_whishlist_blank);
            }
            myViewHolder.rl_product.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mActivity, ProductDetails.class);
                    intent.putExtra(AppConstants.productId, arrayList1.get(myViewHolder.getAdapterPosition()).get("Id"));
                    startActivity(intent);
                }
            });
            myViewHolder.rl_wishlist.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.v("Clicked", "WishItem");
                   /* if (prefs.get(AppConstants.userId).equalsIgnoreCase("")) {
                        startActivity(new Intent(mActivity, Login.class));
                    } else*/ if (arrayList1.get(myViewHolder.getAdapterPosition()).get("WishlistStatus").equalsIgnoreCase("false")) {
                        AddWishListApi(arrayList1, myViewHolder.tv_wishlist, myViewHolder.getAdapterPosition());
                    } else {
                        removeWishListApi(arrayList1, myViewHolder.tv_wishlist, myViewHolder.getAdapterPosition());
                    }
                }
            });


          if(arrayList1.get(myViewHolder.getAdapterPosition()).get("CartStatus").equals("false")){
                myViewHolder.addcart_btn.setVisibility(View.VISIBLE);
                myViewHolder.buy_btn.setVisibility(View.GONE);
            }else{
                myViewHolder.addcart_btn.setVisibility(View.GONE);
                myViewHolder.buy_btn.setVisibility(View.VISIBLE);
            }



            myViewHolder.addcart_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.v("Clicked", "WishItem");
                   /* if (prefs.get(AppConstants.userId).equalsIgnoreCase("")) {
                        startActivity(new Intent(mActivity, Login.class));
                    }  else {*/
                        myViewHolder.addcart_btn.setVisibility(View.VISIBLE);
                       // myViewHolder.buy_btn.setVisibility(View.VISIBLE);
                        AddToCardApi(arrayList1.get(myViewHolder.getAdapterPosition()).get("Id"));   }
               /* }*/
            });myViewHolder.buy_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.v("Clicked", "WishItem");

                    startActivity(new Intent(mActivity, AddToCart.class));
                }
            });
        }

        @Override
        public int getItemCount() {
            return arrayList1.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            ImageView img_Product;
            RelativeLayout rl_wishlist, rl_product;
            TextView tv_prodTitle, tv_prodPrice,tv_proddiscount,tv_prodweight, tv_prodOldPrice, tv_wishlist;
              Button addcart_btn,buy_btn;
            public MyViewHolder(@NonNull View itemView) {
                super(itemView);

                img_Product = itemView.findViewById(R.id.img_Product);
                rl_wishlist = itemView.findViewById(R.id.rl_wishlist);
                rl_product = itemView.findViewById(R.id.rl_product);
                tv_prodTitle = itemView.findViewById(R.id.tv_prodTitle);
                tv_prodPrice = itemView.findViewById(R.id.tv_prodPrice);
                tv_prodweight = itemView.findViewById(R.id.tv_prodweight);
                tv_proddiscount = itemView.findViewById(R.id.tv_proddiscount);
                tv_prodOldPrice = itemView.findViewById(R.id.tv_prodOldPrice);
                tv_wishlist = itemView.findViewById(R.id.tv_wishlist);
                addcart_btn = itemView.findViewById(R.id.addcart_btn);
                buy_btn = itemView.findViewById(R.id.buy_btn);


            }
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

               // cartQuantity = "1";
                prefs.set(AppConstants.cartQuantity, updateQuantity(prefs.get(AppConstants.cartQuantity), 1)).commit();
               /* tv_cartQuantity.setText(cartQuantity);
                tvCart.setText(prefs.get(AppConstants.cartQuantity));*/
                tvCart.setText(prefs.get(AppConstants.cartQuantity));
            }
            showToast(mActivity, jsonObject.getString("res_msg"));
        } catch (Exception e) {
            Log.v("res_msg","res_msg");
            Log.v(apiErrorException, e.getMessage());
            showToast(mActivity, defResponse);
        }
    }
    private class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.MyViewHolder> {

        ArrayList<HashMap<String, String>> arrayList1;

        public CategoryAdapter(ArrayList<HashMap<String, String>> arrayListEvent) {
            arrayList1 = arrayListEvent;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_category, viewGroup, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
            String imgUrl = arrayList1.get(myViewHolder.getAdapterPosition()).get("Thumb");
            Log.v("MyCategImages", "a " + imgUrl);

            RequestOptions RequestOptions = null;
            Glide.with(myViewHolder.circularImageView.getContext())
                    .load(imgUrl)
                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(20)))
                    .into(myViewHolder.circularImageView);

            myViewHolder.tv_proTitle.setText(arrayList1.get(myViewHolder.getAdapterPosition()).get("Name"));



            myViewHolder.rl_product.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(arrayList1.get(myViewHolder.getAdapterPosition()).get("Id").equals("2")){

                        Intent intent = new Intent(mActivity, SubSubProductCategory.class);

                        startActivity(intent);
                    }else{
                    Intent intent = new Intent(mActivity, SubProductCategory.class);
                    intent.putExtra(AppConstants.subcategory_id, arrayList1.get(myViewHolder.getAdapterPosition()).get("Id"));
                    intent.putExtra(AppConstants.categoryName, arrayList1.get(myViewHolder.getAdapterPosition()).get("Name"));
                    intent.putExtra(AppConstants.categoryBanner, imgUrl);
                    startActivity(intent);
                }
                }
            });
        }

        @Override
        public int getItemCount() {
            return arrayList1.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            de.hdodenhof.circleimageview.CircleImageView circularImageView;
            RelativeLayout rl_product;
            TextView tv_proTitle;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);

                circularImageView = itemView.findViewById(R.id.circularImageView);
                rl_product = itemView.findViewById(R.id.rl_product);
                tv_proTitle = itemView.findViewById(R.id.tv_proTitle);


            }
        }
    }


    private class MainAdapter extends RecyclerView.Adapter<MainAdapter.MyViewHolder> {

        ArrayList<HashMap<String, String>> arrayList1;

        //int num2 =1 ;
        public MainAdapter(ArrayList<HashMap<String, String>> arrayListEvent) {
            arrayList1 = arrayListEvent;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_main, viewGroup, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
            myViewHolder.tv_heading.setText(arrayList1.get(myViewHolder.getAdapterPosition()).get("Array"));
            int Id = Integer.parseInt(arrayList1.get(myViewHolder.getAdapterPosition()).get("Id"));
            int clickId = Id + 1;
            ArrayList<HashMap<String, String>> arrayKeydataSelected = new ArrayList<>();
            for (int j = 0; j < arrayProductdata.size(); j++) {
                if (arrayList1.get(i).get("Id").equalsIgnoreCase(arrayProductdata.get(j).get("courseId"))) {
                    arrayKeydataSelected.add(arrayProductdata.get(j));
                }
            }
            setMainRecyclerAdapter(myViewHolder.recyclerMain);
           /* if(arrayKeydataSelected.size()>0){
                myViewHolder.relative_feature.setVisibility(View.GONE);
            }else{
                myViewHolder.relative_feature.setVisibility(View.VISIBLE);
            }*/
            FeaturedAdapter featuredAdapter = new FeaturedAdapter(arrayKeydataSelected);
            myViewHolder.recyclerMain.setAdapter(featuredAdapter);

            myViewHolder.tv_categoriesAll.setOnClickListener(v -> {
                Intent intentFeature = new Intent(mActivity, AllProducts.class);
                intentFeature.putExtra(AppConstants.proTags, Integer.toString(clickId));
                intentFeature.putExtra(AppConstants.proHead, arrayList1.get(myViewHolder.getAdapterPosition()).get("Array"));
                Log.d("TestApi", arrayList1.get(myViewHolder.getAdapterPosition()).get("Array").toString());
                startActivity(intentFeature);
            });
        }

        @Override
        public int getItemCount() {
            return arrayList1.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            TextView tv_heading, tv_categoriesAll;
            RecyclerView recyclerMain;
          RelativeLayout relative_feature;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);

                tv_heading = itemView.findViewById(R.id.tv_heading);
                relative_feature = itemView.findViewById(R.id.relative_feature);
                tv_categoriesAll = itemView.findViewById(R.id.tv_categoriesAll);
                recyclerMain = itemView.findViewById(R.id.recyclerMain);


            }
        }
    }

}
