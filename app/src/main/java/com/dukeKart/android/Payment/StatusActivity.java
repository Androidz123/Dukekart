package com.dukeKart.android.Payment;



import android.content.Intent;
import android.os.Bundle;

import android.widget.TextView;
import android.widget.Toast;

import com.dukeKart.android.R;
import com.dukeKart.android.views.BaseActivity;

public class StatusActivity extends BaseActivity {

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.activity_status);

		Intent mainIntent = getIntent();
		TextView tv4 = (TextView) findViewById(R.id.textView1);
		tv4.setText(mainIntent.getStringExtra("transStatus"));
	}
	
	public void showToast(String msg) {
		Toast.makeText(this, "Toast: " + msg, Toast.LENGTH_LONG).show();
	}

} 