package com.example.gallerydemo;

import com.example.utils.Constants;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class GalleryDemoActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gallery_demo);
		initView();
	}

	private void initView() {
		Button btn_01 = (Button) this.findViewById(R.id.btn_effect01);
		Button btn_02 = (Button) this.findViewById(R.id.btn_effect02);
		Button btn_03 = (Button) this.findViewById(R.id.btn_effect03);
		Button btn_04 = (Button) this.findViewById(R.id.btn_effect04);
		Button btn_05 = (Button) this.findViewById(R.id.btn_effect05);
		Button btn_06 = (Button) this.findViewById(R.id.btn_effect06);
		btn_01.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(GalleryDemoActivity.this, FlyInActivity.class);
				Bundle bl = new Bundle();
				bl.putString(Constants.ACTIVITY_TITLE, GalleryDemoActivity.this.getString(R.string.fly_in_locale));
				intent.putExtras(bl);
				startActivity(intent);
			}
		});
		
		btn_02.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(GalleryDemoActivity.this, FlyInActivity.class);
				Bundle bl = new Bundle();
				bl.putString(Constants.ACTIVITY_TITLE, GalleryDemoActivity.this.getString(R.string.fly_in_kuaipan));
				intent.putExtras(bl);
				startActivity(intent);
			}
		});
		
		btn_03.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(GalleryDemoActivity.this, FadeInActivity.class);
				Bundle bl = new Bundle();
				bl.putString(Constants.ACTIVITY_TITLE, GalleryDemoActivity.this.getString(R.string.fade_in_locale));
				intent.putExtras(bl);
				startActivity(intent);
			}
		});
		
		btn_04.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(GalleryDemoActivity.this, FadeInActivity.class);
				Bundle bl = new Bundle();
				bl.putString(Constants.ACTIVITY_TITLE, GalleryDemoActivity.this.getString(R.string.fade_in_kuaipan));
				intent.putExtras(bl);
				startActivity(intent);
			}
		});
		
		btn_05.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(GalleryDemoActivity.this, RotateActivity.class);
				Bundle bl = new Bundle();
				bl.putString(Constants.ACTIVITY_TITLE, GalleryDemoActivity.this.getString(R.string.rotate_locale));
				intent.putExtras(bl);
				startActivity(intent);
			}
		});
		
		btn_06.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(GalleryDemoActivity.this, RotateActivity.class);
				Bundle bl = new Bundle();
				bl.putString(Constants.ACTIVITY_TITLE, GalleryDemoActivity.this.getString(R.string.rotate_kuaipan));
				intent.putExtras(bl);
				startActivity(intent);
			}
		});
	}


}
