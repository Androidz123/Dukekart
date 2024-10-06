package com.dukeKart.android.fragment;

import static com.dukeKart.android.common.AppUtils.setImgPicasso;
import static com.dukeKart.android.common.AppUtils.setPrice;
import static com.dukeKart.android.common.AppUtils.showToast;
import static com.dukeKart.android.common.AppUtils.updateQuantity;
import static com.dukeKart.android.common.AppsStrings.apiErrorException;
import static com.dukeKart.android.common.AppsStrings.defResponse;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.bumptech.glide.Glide;
import com.dukeKart.android.R;
import com.dukeKart.android.activity.AddToCart;
import com.dukeKart.android.activity.Login;
import com.dukeKart.android.activity.ProductDetails;
import com.dukeKart.android.common.AppUrls;
import com.dukeKart.android.common.AppsStrings;
import com.dukeKart.android.views.BaseFragment;
import com.dukeKart.android.database.AppConstants;
import com.dukeKart.android.database.Preferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;



public class Wishlist extends BaseFragment implements View.OnClickListener {

    ArrayList<HashMap<String, String>> wishList = new ArrayList<>();
    Preferences pref;
    ImageView ivBack;
    View view;
    RecyclerView rv_Wishlist;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.wishlist_frag, container, false);
       startPage();
        return view;

    }

    public void startPage() {
        setIDs();
        setListeners();
        getGetWishListApi();
    }


    public void setIDs() {
        pref = new Preferences(mActivity);
        rv_Wishlist = view.findViewById(R.id.rv_Wishlist);
        rv_Wishlist.setHasFixedSize(true);
        rv_Wishlist.setLayoutManager(new LinearLayoutManager(mActivity));
//        ivBack = view.findViewById(R.id.ivBack);
    }

    public void setListeners() {
//        btnAddNew.setOnClickListener(this);

    }


    private void getGetWishListApi() {
        showLoading();
        AppsStrings.setApiString("GetWishList");
        String url = AppUrls.GetWishList;
        Log.v(AppsStrings.apiUrl, url);
        JSONObject json = new JSONObject();
        JSONObject json_data = new JSONObject();

        try {
            json_data.put("user_id", pref.get(AppConstants.userId));
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

                        parseGetWishListApi(response);
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

    private void parseGetWishListApi(JSONObject response) {
        hideLoading();
        Log.d(AppsStrings.apiResponse, response.toString());
        try {
            JSONObject jsonObject = response.getJSONObject("ecommerce");
            wishList.clear();

            if (jsonObject.getString("res_code").equalsIgnoreCase("1")) {
                JSONArray getWishList = jsonObject.getJSONArray("WishList");
                for (int i = 0; i < getWishList.length(); i++) {
                    HashMap<String, String> hashProduct = new HashMap<>();
                    hashProduct.put("CartStatus", getWishList.getJSONObject(i).getString("CartStatus"));
                    hashProduct.put("id", getWishList.getJSONObject(i).getString("id"));
                    hashProduct.put("product_id", getWishList.getJSONObject(i).getString("product_id"));
                    hashProduct.put("SKU", getWishList.getJSONObject(i).getString("SKU"));
                    hashProduct.put("Color", getWishList.getJSONObject(i).getString("Color"));
                    hashProduct.put("Size", getWishList.getJSONObject(i).getString("Size"));
                    hashProduct.put("ProductName", getWishList.getJSONObject(i).getString("ProductName"));
                    hashProduct.put("RegularPrice", getWishList.getJSONObject(i).getString("RegularPrice"));
                    hashProduct.put("MRP", getWishList.getJSONObject(i).getString("MRP"));
                    hashProduct.put("Highlights", getWishList.getJSONObject(i).getString("Highlights"));
                    hashProduct.put("Brand", getWishList.getJSONObject(i).getString("Brand"));
                    hashProduct.put("Description", getWishList.getJSONObject(i).getString("Description"));
                    hashProduct.put("main_image", getWishList.getJSONObject(i).getString("main_image"));
                    hashProduct.put("image2", getWishList.getJSONObject(i).getString("image2"));
                    hashProduct.put("discount_percentage", getWishList.getJSONObject(i).getString("discount_percentage"));
                    wishList.add(hashProduct);
                }

            } else {

                showToast(mActivity, jsonObject.getString("res_msg"));

            }
            setAdapters();


        } catch (Exception e) {
            Log.v(apiErrorException, e.getMessage());
            showToast(mActivity, defResponse);
        }

    }

    private void setAdapters() {
        setRecyclerAdapter(rv_Wishlist);
        WishlistAdapter wishlistAdapter = new WishlistAdapter(wishList);
        rv_Wishlist.setAdapter(wishlistAdapter);
    }

    public void setRecyclerAdapter(RecyclerView recyclerView) {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        recyclerView.setNestedScrollingEnabled(false);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(mActivity, 2);
        recyclerView.setLayoutManager(mLayoutManager);
//        recyclerView.addItemDecoration(new GridSpacingItemDecoration(1, dpToPx(1, mActivity), true));
//        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
/*
            case R.id.btnAddNew:
                startActivity(new Intent(mActivity, AddAddress.class));
*/

        }
    }

    @Override
    public void onResume() {
       // startPage();
        super.onResume();
    }

    private void removeWishListApi(String wish_id) {

        showLoading();
        AppsStrings.setApiString("RemoveWishList");
        String url = AppUrls.RemoveWishList;
        Log.v(AppsStrings.apiUrl, url);
        JSONObject json = new JSONObject();
        JSONObject json_data = new JSONObject();

        try {
//            json_data.put("user_id", prefs.get(AppConstants.userId));
            json_data.put("wishlist_id", wish_id);
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

    private void parseRemoveWishListApi(JSONObject response) {

        Log.d(AppsStrings.apiResponse, response.toString());
        try {
            JSONObject jsonObject = response.getJSONObject("ecommerce");
            hideLoading();
            if (jsonObject.getString("res_code").equalsIgnoreCase("1"))
                getGetWishListApi();
            else {
                hideLoading();
                showToast(mActivity, jsonObject.getString("res_msg"));
            }
        }catch (Exception e) {
            hideLoading();
            Log.v(apiErrorException, e.getMessage());
            showToast(mActivity, defResponse);
        }
    }



    private class WishlistAdapter extends RecyclerView.Adapter<WishlistAdapter.MyViewHolder> {

        ArrayList<HashMap<String, String>> arrayList1;

        public WishlistAdapter(ArrayList<HashMap<String, String>> arrayListEvent) {
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
            String imgUrl = arrayList1.get(myViewHolder.getAdapterPosition()).get("main_image");
            Log.v("MyProductImages", "a " + imgUrl);

            Log.v("MyProductImages", "b " + imgUrl);
            setImgPicasso("https://dukekart.in//assets/product_images/"+imgUrl, mActivity, myViewHolder.img_Product);

//            setImgPicasso(imgUrl, mActivity, myViewHolder.img_Product);
            /*Glide.with(myViewHolder.img_Product.getContext())
                            .load(imgUrl)
                                    .placeholder(R.mipmap.placeholder)
                                            .into(myViewHolder.img_Product);
*/
            myViewHolder.tv_prodweight.setText(arrayList1.get(myViewHolder.getAdapterPosition()).get("Color")+" "+arrayList1.get(myViewHolder.getAdapterPosition()).get("Size"));
            myViewHolder.tv_proddiscount.setText(arrayList1.get(myViewHolder.getAdapterPosition()).get("discount_percentage")+"% Off");
            myViewHolder.tv_prodTitle.setText(arrayList1.get(myViewHolder.getAdapterPosition()).get("ProductName"));
            myViewHolder.tv_prodPrice.setText(setPrice(arrayList1.get(myViewHolder.getAdapterPosition()).get("RegularPrice")));
            myViewHolder.tv_prodOldPrice.setText(setPrice(arrayList1.get(myViewHolder.getAdapterPosition()).get("MRP")));
            myViewHolder.tv_prodOldPrice.setPaintFlags(myViewHolder.tv_prodOldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            myViewHolder.tv_wishlist.setBackgroundResource(R.drawable.ic_wishlist_fill);
            myViewHolder.rl_product.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mActivity, ProductDetails.class);
                    intent.putExtra(AppConstants.productId,arrayList1.get(myViewHolder.getAdapterPosition()).get("product_id"));
                    intent.putExtra(AppConstants.pagePath, "1");
                    startActivity(intent);
                }
            });
            myViewHolder.rl_wishlist.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.v("Clicked", "WishItem");
                    removeWishListApi(arrayList1.get(myViewHolder.getAdapterPosition()).get("id"));
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
                    if (pref.get(AppConstants.userId).equalsIgnoreCase("")) {
                        startActivity(new Intent(mActivity, Login.class));
                    }  else {
                        myViewHolder.addcart_btn.setVisibility(View.GONE);
                        myViewHolder.buy_btn.setVisibility(View.VISIBLE);
                        AddToCardApi(arrayList1.get(myViewHolder.getAdapterPosition()).get("id"));   }
                }
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
 Button buy_btn;
 Button addcart_btn;
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
                buy_btn = itemView.findViewById(R.id.buy_btn);
                addcart_btn = itemView.findViewById(R.id.addcart_btn);


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
            json_data.put("user_id", pref.get(AppConstants.userId));
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
                pref.set(AppConstants.cartQuantity, updateQuantity(pref.get(AppConstants.cartQuantity), 1)).commit();
               /* tv_cartQuantity.setText(cartQuantity);
                tvCart.setText(prefs.get(AppConstants.cartQuantity));*/
            }
            showToast(mActivity, jsonObject.getString("res_msg"));
        } catch (Exception e) {
            Log.v("res_msg","res_msg");
            Log.v(apiErrorException, e.getMessage());
            showToast(mActivity, defResponse);
        }
    }
}
