package com.example.gallerydemo;

import com.example.utils.Constants;

import android.app.Activity;
import android.os.Bundle;

public class RotateActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Bundle bl = this.getIntent().getExtras();
		String activityTitle = bl.getString(Constants.ACTIVITY_TITLE);
		this.setTitle(activityTitle);
		setContentView(R.layout.layout_3d);
	}

}
