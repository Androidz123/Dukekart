package com.dukeKart.android.fragment;

import android.app.AlertDialog;
import android.content.Intent;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.dukeKart.android.R;
import com.dukeKart.android.activity.AddAddress;
import com.dukeKart.android.common.AppUrls;
import com.dukeKart.android.common.AppsStrings;
import com.dukeKart.android.database.AppConstants;
import com.dukeKart.android.database.Preferences;
import com.dukeKart.android.views.BaseFragment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import static com.dukeKart.android.common.AppUtils.hideSoftKeyboard;
import static com.dukeKart.android.common.AppUtils.showToast;
import static com.dukeKart.android.common.AppsStrings.apiErrorException;
import static com.dukeKart.android.common.AppsStrings.defResponse;

public class ShippingAddress extends BaseFragment implements View.OnClickListener {

    ArrayList<HashMap<String, String>> addressList = new ArrayList<>();
    Preferences pref;
    ImageView ivBack;
    View view;
    RecyclerView rv_ShippingAddress;
    Button btnAddNew;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.shipping_frag, container, false);
       // hideLoading();
        startPage();
        return view;

    }

    public void startPage() {
        setIDs();
        setListeners();
        getAddressListApi();
        hideSoftKeyboard(mActivity);
    }


    public void setIDs() {
        pref = new Preferences(mActivity);
        rv_ShippingAddress = view.findViewById(R.id.rv_ShippingAddress);
        btnAddNew = view.findViewById(R.id.btnAddNew);
        rv_ShippingAddress.setHasFixedSize(true);
        rv_ShippingAddress.setLayoutManager(new LinearLayoutManager(mActivity));
//        ivBack = view.findViewById(R.id.ivBack);
    }

    public void setListeners() {
        btnAddNew.setOnClickListener(this);

    }


    private void getAddressListApi() {
        showLoading();
        AppsStrings.setApiString("GetAddressList");
        String url = AppUrls.GetAddressList;
        Log.v(AppsStrings.apiUrl, url);
        JSONObject json = new JSONObject();
        JSONObject json_data = new JSONObject();

        try {
            json_data.put("user_id", pref.get(AppConstants.userId));
//            json_data.put("user_id", "2");
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
                        parseGetAddressListApi(response);
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

    private void parseGetAddressListApi(JSONObject response) {
        hideLoading();
        Log.d(AppsStrings.apiResponse, response.toString());
        try {
            JSONObject jsonObject = response.getJSONObject("ecommerce");
            addressList.clear();
            if (jsonObject.getString("res_code").equalsIgnoreCase("1")) {
                JSONArray getAddressList = jsonObject.getJSONArray("AddressList");
                for (int i = 0; i < getAddressList.length(); i++) {
                    HashMap<String, String> hashProduct = new HashMap<>();
                    hashProduct.put("address_id", getAddressList.getJSONObject(i).getString("address_id"));
                    hashProduct.put("user_master_id", getAddressList.getJSONObject(i).getString("user_master_id"));
                    hashProduct.put("title", getAddressList.getJSONObject(i).getString("title"));
                    hashProduct.put("contact_person", getAddressList.getJSONObject(i).getString("contact_person"));
                    hashProduct.put("mobile_number", getAddressList.getJSONObject(i).getString("mobile_number"));
                    hashProduct.put("alternate_number", getAddressList.getJSONObject(i).getString("alternate_number"));
                    hashProduct.put("address", getAddressList.getJSONObject(i).getString("address"));
                    hashProduct.put("localty", getAddressList.getJSONObject(i).getString("localty"));
                    hashProduct.put("landmark", getAddressList.getJSONObject(i).getString("landmark"));
                    hashProduct.put("pincode", getAddressList.getJSONObject(i).getString("pincode"));
                    hashProduct.put("state", getAddressList.getJSONObject(i).getString("state"));
                    hashProduct.put("city", getAddressList.getJSONObject(i).getString("city"));
                    addressList.add(hashProduct);
                }

            } else {

                showToast(mActivity, jsonObject.getString("res_msg"));

            }
            ShippingAdapter shippingAdapter = new ShippingAdapter(addressList);
            rv_ShippingAddress.setAdapter(shippingAdapter);


        } catch (Exception e) {
            Log.v(apiErrorException, e.getMessage());
            showToast(mActivity, defResponse);
        }

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnAddNew:
                startActivity(new Intent(mActivity, AddAddress.class).putExtra(AppConstants.pagePath,"0"));

        }
    }

    @Override
    public void onResume() {
        //startPage();
        super.onResume();
    }
    public void confirmDeleteDialog(String addressId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        LayoutInflater inflater = LayoutInflater.from(mActivity);
        View dialogView = inflater.inflate(R.layout.confirm_popup, null);
        builder.setView(dialogView);

        TextView tv_Header = dialogView.findViewById(R.id.tv_Header);
        tv_Header.setText("Confirm Delete");
        TextView tv_Message = dialogView.findViewById(R.id.tv_Message);
        tv_Message.setText("Are you sure you want to delete?");

        RelativeLayout rrSubmit = dialogView.findViewById(R.id.rr_submit);
        RelativeLayout rr_lg_cancel = dialogView.findViewById(R.id.rr_lg_cancel);

        final AlertDialog alert = builder.create();
        rrSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeAddressApi(addressId);
                alert.dismiss();
            }
        });


        RelativeLayout rrCancel = dialogView.findViewById(R.id.rr_cancel);
        rr_lg_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.dismiss();
            }
        });
        rrCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.dismiss();
            }
        });
        alert.show();

     }


    private void removeAddressApi(String address_id) {

        showLoading();
        AppsStrings.setApiString("DeleteAddress");
        String url = AppUrls.DeleteAddress;
        Log.v(AppsStrings.apiUrl, url);
        JSONObject json = new JSONObject();
        JSONObject json_data = new JSONObject();

        try {
//            json_data.put("user_id", prefs.get(AppConstants.userId));
            json_data.put("address_id", address_id);
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
                        parseRemoveAddressApi(response);
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

    private void parseRemoveAddressApi(JSONObject response) {
        hideLoading();

        Log.d(AppsStrings.apiResponse, response.toString());
        try {
            JSONObject jsonObject = response.getJSONObject("ecommerce");
            if (jsonObject.getString("res_code").equalsIgnoreCase("1"))
                getAddressListApi();
            else {
                hideLoading();
                showToast(mActivity, jsonObject.getString("res_msg"));
            }
        } catch (Exception e) {
            hideLoading();
            Log.v(apiErrorException, e.getMessage());
            showToast(mActivity, defResponse);
        }
    }

    private class ShippingAdapter extends RecyclerView.Adapter<ShippingAdapter.MyViewHolder> {

        ArrayList<HashMap<String, String>> arrayList1;

        public ShippingAdapter(ArrayList<HashMap<String, String>> arrayListEvent) {
            arrayList1 = arrayListEvent;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_shipping, viewGroup, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
            try {
                myViewHolder.tv_shippingtitle.setText(arrayList1.get(i).get("title"));
                myViewHolder.tv_contactPerson.setText(arrayList1.get(i).get("contact_person"));
                myViewHolder.tv_contactNumber.setText(arrayList1.get(i).get("mobile_number"));
                myViewHolder.tv_altNumber.setText(arrayList1.get(i).get("alternate_number"));
                myViewHolder.tv_shippingAddress.setText(arrayList1.get(i).get("address") + "," + arrayList1.get(i).get("localty") + "," + arrayList1.get(i).get("city") + "," + arrayList1.get(i).get("state") + "-" + arrayList1.get(i).get("pincode"));
                myViewHolder.btn_update.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(mActivity, AddAddress.class).putExtra(AppConstants.pagePath,"0");
                        intent.putExtra(AppConstants.addressArray, arrayList1.get(i));
                        startActivity(intent);
                    }
                });
                myViewHolder.btn_delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        confirmDeleteDialog(arrayList1.get(i).get("address_id"));
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                showToast(mActivity, defResponse);
            }
        }

        @Override
        public int getItemCount() {
            return arrayList1.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            Button btn_update, btn_delete;
            TextView tv_shippingtitle, tv_contactPerson, tv_contactNumber, tv_altNumber, tv_shippingAddress;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);

                tv_shippingtitle = itemView.findViewById(R.id.tv_shippingtitle);
                tv_contactPerson = itemView.findViewById(R.id.tv_contactPerson);
                tv_contactNumber = itemView.findViewById(R.id.tv_contactNumber);
                tv_altNumber = itemView.findViewById(R.id.tv_altNumber);
                tv_shippingAddress = itemView.findViewById(R.id.tv_shippingAddress);
                btn_update = itemView.findViewById(R.id.btn_update);
                btn_delete = itemView.findViewById(R.id.btn_delete);

            }
        }
    }

}
