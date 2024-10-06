package com.dukeKart.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.dukeKart.android.fragment.Settings;
import com.dukeKart.android.views.BaseActivity;
import com.google.android.material.tabs.TabLayout;
import com.dukeKart.android.R;
import com.dukeKart.android.database.AppConstants;

import com.dukeKart.android.fragment.Profile;
import com.dukeKart.android.fragment.ShippingAddress;


public class Account extends BaseActivity implements TabLayout.OnTabSelectedListener, View.OnClickListener {

    ViewPager vp_Account;
    TabLayout tabLayout;
    ImageView iv_back;
    TextView tv_title;
    RelativeLayout rlCart;
    ImageView iv_search;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        setIds();
        //Adding the tabs using addTab() method
        tabLayout.addTab(tabLayout.newTab().setText("My Account"));
        tabLayout.addTab(tabLayout.newTab().setText("Shipping Address"));
        tabLayout.addTab(tabLayout.newTab().setText("Settings"));
        /*tabLayout.addTab(tabLayout.newTab().setText("Change Password"));*/

        //Initializing viewPager
        vp_Account = findViewById(R.id.vp_Account);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        vp_Account.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        //Adding adapter to pager
        vp_Account.setAdapter(adapter);

        //Adding onTabSelectedListener to swipe views
        tabLayout.setOnTabSelectedListener(this);

        setListeners();
        setPage();
    }

    private void setPage() {
        try{
            setHeaders();
            if(getIntent().getStringExtra(AppConstants.pagePath).equalsIgnoreCase("1")){
                vp_Account.setCurrentItem(1);
            }else {
                vp_Account.setCurrentItem(0);
            }
        }catch (Exception e){
            vp_Account.setCurrentItem(0);
        }
    }
    private void setListeners() {
        iv_back.setOnClickListener(this);
    }
    private void setHeaders() {
        tv_title.setText("Accounts");
        iv_search.setVisibility(View.INVISIBLE);
        rlCart.setVisibility(View.INVISIBLE);
    }
    private void setIds() {
        iv_back = findViewById(R.id.iv_back);
        tabLayout = findViewById(R.id.tabLayout);
        tv_title = findViewById(R.id.tv_title);
        iv_search = findViewById(R.id.iv_search);
        rlCart = findViewById(R.id.rlCart);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        vp_Account.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                onBackPressed();
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(mActivity, Dashboard.class));
        finish();
    }

    public class ViewPagerAdapter extends FragmentStatePagerAdapter {

        //integer to count number of tabs
        int tabCount;

        //Constructor to the class
        public ViewPagerAdapter(FragmentManager fm, int tabCount) {
            super(fm);
            //Initializing tab count
            this.tabCount = tabCount;
        }

        //Overriding method getItem
        @Override
        public Fragment getItem(int position) {
            //Returning the current tabs
            switch (position) {
                case 0:
                    return new Profile();

                case 1:
                    return new ShippingAddress();

                case 2:

                    return new Settings();
                default:
                    return null;
            }
        }

        //Overriden method getCount to get the number of tabs
        @Override
        public int getCount() {
            return tabCount;
        }
    }

}