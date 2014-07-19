package com.lahacks.colormatch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;

public class RulesActivity extends Activity{

	public void onCreate(Bundle bundle){
		super.onCreate(bundle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.rules);
		findViewById(R.id.rlOuter).setBackgroundColor(CameraActivity.color);
		OnClickListener listener = new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent("com.lahacks.colormatch.CAMERAACTIVITY"));
			}
		};
		findViewById(R.id.rlOuter).setOnClickListener(listener);
	}
	
}
