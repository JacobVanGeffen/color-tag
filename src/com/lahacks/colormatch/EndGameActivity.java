package com.lahacks.colormatch;

import java.io.IOException;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lahacks.colormatch.NetworkUtil.FinishLoadingService;

public class EndGameActivity extends Activity {
	
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.endgame);
		findViewById(R.id.llOuter).setBackgroundColor(CameraActivity.color);
		TextView tv;
		(tv = (TextView) findViewById(R.id.tvFinalScore))
				.setTextColor(getTextColor());
		tv.setText("Score: "+CameraActivity.mode.formatScore(CameraActivity.score));
		tv.setTypeface(Typeface.createFromAsset(getAssets(), "Roboto-Black.ttf"));
		HighScores.writeScore(CameraActivity.score, this, CameraActivity.mode);
		Log.wtf(CameraActivity.gameID, "yep");
		if(CameraActivity.gameID.equals("-1"))
			showHighScores();
		else showWinLose();
		showPics();
	}
	
	private int getTextColor() {
		return ColorUtil.isDark(CameraActivity.color) ? getResources()
				.getColor(R.color.text_white) : getResources().getColor(
				R.color.text_black);
	}
	
	private void showPics(){
		((ImageView)findViewById(R.id.ivPic)).setImageBitmap(CameraActivity.bestBit);
	}
	
	private void showHighScores(){
		TextView tvHigh = (TextView) findViewById(R.id.tvHigh);
		tvHigh.setVisibility(View.VISIBLE);
		tvHigh.setTextColor(getTextColor());
		tvHigh.setTypeface(Typeface.createFromAsset(getAssets(), "Roboto-Black.ttf"));
		
		double scores[] = HighScores.readScores(this, CameraActivity.mode);
		LinearLayout layouts[] = {new LinearLayout(this), new LinearLayout(this)};
		RelativeLayout.LayoutParams lParams[] = {
				new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
						LayoutParams.MATCH_PARENT),
				new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
						LayoutParams.MATCH_PARENT),
						new RelativeLayout.LayoutParams(0, 0) };
		
		RelativeLayout sep = new RelativeLayout(this);
		
		View split = new View(this);
		split.setId(R.id.split);
		
		lParams[0].addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
		lParams[0].addRule(RelativeLayout.LEFT_OF, R.id.split);
		lParams[1].addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
		lParams[1].addRule(RelativeLayout.RIGHT_OF, R.id.split);
		lParams[2].addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
		
		sep.addView(split, lParams[2]);
		sep.addView(layouts[0], lParams[0]);
		sep.addView(layouts[1], lParams[1]);
		layouts[0].setOrientation(LinearLayout.VERTICAL);
		layouts[1].setOrientation(LinearLayout.VERTICAL);
		((LinearLayout) findViewById(R.id.llOuter)).addView(sep,
				new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
						LayoutParams.WRAP_CONTENT));
		
		for(int a=0; a<10; a++){
			TextView tv = new TextView(this);
			tv.setTextColor(getTextColor());
			tv.setTypeface(Typeface.createFromAsset(getAssets(), "Roboto-Black.ttf"));
			tv.setTextSize(30);
			tv.setText((a+1)+". "+CameraActivity.mode.formatScore(scores[a]));
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params.gravity = Gravity.CENTER_HORIZONTAL;
			if(a<5)
				layouts[0].addView(tv, params);
			else layouts[1].addView(tv, params);
		}
	}
	
	private void showWinLose(){
		if(System.currentTimeMillis()/1000>CameraActivity.expTime+CameraActivity.length*2){
			NetworkUtil.postData(CameraActivity.gameID, CameraActivity.score);
			try {
				NetworkUtil.getGame(CameraActivity.gameID, new FinishLoadingService() {
					public void onEnd() {
						showPic();
					}
				});
			} catch (IOException e) {} catch (InterruptedException e) {}
		}else{
			findViewById(R.id.tvWaiting).setVisibility(View.VISIBLE);
			new Thread(new Runnable(){
				public void run(){
						try {
							Thread.sleep(CameraActivity.expTime-System.currentTimeMillis()/100);
						} catch (Exception e) {}
					runOnUiThread(new Runnable(){public void run(){findViewById(R.id.tvWaiting).setVisibility(View.GONE);}});
					NetworkUtil.postData(CameraActivity.gameID, CameraActivity.score);
					try {
						NetworkUtil.getGame(CameraActivity.gameID, new FinishLoadingService() {
							public void onEnd() {
								showPic();
							}
						});
					} catch (IOException e) {} catch (InterruptedException e) {}
				}
			}).start();
		}
	}
	
	private void showPic(){
		runOnUiThread(
				new Runnable(){
					public void run(){
						((ImageView)findViewById(R.id.ivResult)).setVisibility(View.VISIBLE);
						if(CameraActivity.score>=CameraActivity.globalHighScore)
							((ImageView)findViewById(R.id.ivResult)).setImageResource(R.drawable.won);
						else ((ImageView)findViewById(R.id.ivResult)).setImageResource(R.drawable.lost);
					}
				});
	}
	
}
