package com.example.gallerydemo;

import com.demo.render.DisPlayList;
import com.example.utils.Constants;

import android.app.Activity;
import android.os.Bundle;

public class FlyInActivity extends Activity {
	private RenderView mRenderView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Bundle bl = this.getIntent().getExtras();
		String activityTitle = bl.getString(Constants.ACTIVITY_TITLE);
		this.setTitle(activityTitle);
		mRenderView = new RenderView(this);
		setContentView(mRenderView);
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		DisPlayList.getInstance().clear();
	}
	
	
}
