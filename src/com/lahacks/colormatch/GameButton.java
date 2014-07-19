package com.lahacks.colormatch;

import java.io.IOException;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff.Mode;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lahacks.colormatch.NetworkUtil.FinishLoadingService;

public class GameButton extends RelativeLayout {

	private Context context;
	private String name, uid;
	private ImageView clickHandler;

	public GameButton(Context context){
		this(context, "name", "1");
	}
	
	public GameButton(Context context, String name, String uid) {
		super(context);
		this.context = context;
		this.name = name;
		this.uid = uid;
		setClickable(true);
		setupGUI();
	}
	
	public GameButton(Context context, int color, String name, String uid) {
		this(context, name, uid);
		setBackgroundColor(color);
	}
	
	public GameButton(Context context, String name, String uid, int id){
		this(context, name, uid);
		setId(id);
	}

	/*@Override
	public void setOnClickListener(OnClickListener l) {
		clickHandler.setOnClickListener(l);
	}
	
	@Override
	public void setId(int id) {
		clickHandler.setId(id);
	}*/
	
	@SuppressWarnings("deprecation")
	private void setupGUI() {
		TextView tv = new TextView(context);
		ImageView images[] = {new ImageView(context), new ImageView(context)};
		
		Drawable back = context.getResources().getDrawable(R.drawable.solid_round_color), 
				front = context.getResources().getDrawable(R.drawable.gradient_round_color);
		back.setColorFilter(ColorUtil.getLightColor(), Mode.MULTIPLY);
		images[0].setBackgroundDrawable(back);
		images[1].setBackgroundDrawable(front);
		
		tv.setTextColor(context.getResources().getColor(android.R.color.black));
		tv.setText(name);
		tv.setTextSize(30);
		tv.setTypeface(Typeface.createFromAsset(context.getAssets(), "Roboto-Black.ttf"));
		tv.setGravity(Gravity.CENTER);
		tv.setTextColor(0x7f000000);;
		
		LayoutParams params[] = {
				new LayoutParams(LayoutParams.WRAP_CONTENT,
						LayoutParams.WRAP_CONTENT),
				new LayoutParams(LayoutParams.MATCH_PARENT,
						LayoutParams.MATCH_PARENT),
				new LayoutParams(LayoutParams.MATCH_PARENT,
						LayoutParams.MATCH_PARENT) };
		
		for(int a=0; a<params.length; a++)
			params[a].addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

		tv.setLayoutParams(params[0]);
		images[0].setLayoutParams(params[1]);
		images[1].setLayoutParams(params[2]);
		
		for(ImageView image : images)
			addView(image);
		addView(tv);
		setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				try {
					NetworkUtil.getGame(uid, new FinishLoadingService() {
						public void onEnd() {
							CameraActivity.gameID = uid;
							context.startActivity(new Intent("com.lahacks.colormatch.RULESACTIVITY"));
						}
					});
				} catch (IOException e) {} catch (InterruptedException e) {}
			}
		});
		setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				switch(event.getAction()){
				case MotionEvent.ACTION_DOWN:
					getLayoutParams().height = MainActivity.height/10-10;
					break;
				case MotionEvent.ACTION_UP:
					getLayoutParams().height = MainActivity.height/10;
					break;
				}
				return false;
			}
		});
	}
	
}
