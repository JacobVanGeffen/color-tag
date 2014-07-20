package com.lahacks.colormatch;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.lahacks.colormatch.NetworkUtil.FinishLoadingService;
import com.lahacks.colormatch.NetworkUtil.Game;

public class MainActivity extends Activity {

	private int background;
	/*ideas: 
	 * ***ALLOW USERS TO SET FORE/BACKGROUND COLORS
	 * *have an "I'm done" button
	 * 		**have a pause button
	 * *static colored buttons?: (light) green, blue, purple, pink/red
	 * 		**auto-set first generated color scheme (static) to look good
	 * *always use Roboto-Black, color=7fffffff/7f000000 font (esp. on CameraActivity text)
	 */
	public static int width, height;
	
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main2);

		added = new TreeSet<String>();
		findViewById(R.id.rlOuter).setOnClickListener(getOnClickListener());
	}
	
	@SuppressWarnings("unused")
	private void setupGUI() {
		background = ColorUtil.getDarkColor();
		findViewById(R.id.rlOuter).setBackgroundColor(background);
		
		RelativeLayout inner = (RelativeLayout) findViewById(R.id.rlInner);
		int gbIds[] = {R.id.gbMatchMaker, R.id.gbClassic, R.id.gbHost, R.id.gbRush};
		String texts[] = {"Match\nMaker", "Classic", "Multiplayer", "Rush"};
		GameButton buttons[] = new GameButton[gbIds.length];
		for(int a=0; a<buttons.length; a++)
			buttons[a] = new GameButton(this, texts[a], "", gbIds[a]);
		RelativeLayout.LayoutParams params[] = new RelativeLayout.LayoutParams[buttons.length];
		
		params[0] = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, height/10);
		params[0].setMargins(20, 20, 20, 20);
		for(int a=1; a<params.length; a++){
			params[a] = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, height/10);
			params[a].addRule(RelativeLayout.BELOW, gbIds[a-1]);
			params[a].setMargins(20, 20, 20, 20);
		}
		
		for (int a=0; a<buttons.length; a++){
			buttons[a].setOnClickListener(getOnClickListener());
			inner.addView(buttons[a], params[a]);
		}
		
		NumberPicker picker = (NumberPicker) findViewById(R.id.npTime);
		picker.setMaxValue(5);
		picker.setMinValue(1);
		for(int a=0; a<picker.getChildCount(); a++)
			if(picker.getChildAt(a) instanceof TextView)
				((TextView)picker.getChildAt(a)).setTextColor(getResources().getColor(android.R.color.white));
		
		GameButton start = new GameButton(this, "Start Game", "-1", R.id.gbStart);

		start.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, height/10));
		((RelativeLayout.LayoutParams)start.getLayoutParams()).setMargins(20, 20, 20, 20);
		((RelativeLayout.LayoutParams)start.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
		((ViewGroup)findViewById(R.id.rlHostInfo)).addView(start);
		start.setOnClickListener(getOnClickListener());
	}
	
	private void setupGUI2(){
		background = ColorUtil.getDarkColor();
		findViewById(R.id.rlOuter).setBackgroundColor(background);
		
		RelativeLayout inner = (RelativeLayout) findViewById(R.id.rlInner);
		int gbIds[] = {R.id.gbClassic, R.id.gbRush, R.id.gbMatchMaker, R.id.gbHost};
		String texts[] = {"Classic", "Rush", "Match\nMaker", "Multiplayer"};
		GameButton buttons[] = new GameButton[gbIds.length];
		for(int a=0; a<buttons.length; a++)
			buttons[a] = new GameButton(this, texts[a], "", gbIds[a]);
		RelativeLayout.LayoutParams params[] = new RelativeLayout.LayoutParams[buttons.length];
		int[] rules = {RelativeLayout.LEFT_OF, RelativeLayout.ABOVE, RelativeLayout.RIGHT_OF, RelativeLayout.BELOW};
		
		for(int a=0; a<buttons.length; a++){
			params[a] = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			params[a].addRule(rules[(a<<1&2)+1], R.id.center);
			params[a].addRule(rules[a&2], R.id.center);
		}
		
		for (int a=0; a<buttons.length; a++){
			buttons[a].setOnClickListener(getOnClickListener());
			inner.addView(buttons[a], params[a]);
		}
	}

	private boolean hasSetupGUI = false;
	
	@Override
	public void onResume(){
		super.onResume();
		if(!hasSetupGUI){
			hasSetupGUI = true;
			new Thread(new Runnable(){
				public void run(){
					while(height == 0){
						try {
							Thread.sleep(100);
							Point p = new Point();
							MainActivity.this.getWindowManager().getDefaultDisplay().getSize(p);
							height = p.y;
							width = p.x;
						} catch (InterruptedException e) {}
					}
					runOnUiThread(new Runnable(){
						public void run(){
							setupGUI2();
						}
					});
					shouldStop = false;
					startScraper();
				}
			}).start();
		}else{
			findViewById(R.id.gbClassic).startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.reset));
			findViewById(R.id.gbMatchMaker).startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.reset));
			findViewById(R.id.gbRush).startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.reset));
			findViewById(R.id.gbHost).startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.reset));
			findViewById(R.id.rlOuter).startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.reset));
		}
	}
	
	@Override
	public void onBackPressed(){
		if(findViewById(R.id.rlHostInfo).getVisibility()==View.VISIBLE)
			findViewById(R.id.rlHostInfo).setVisibility(View.INVISIBLE);
		else super.onBackPressed();
	}
	
	@Override
	public void onPause(){
		super.onPause();
		shouldStop = true;
	}
	
	private boolean shouldStop = false;
	private Set<String> added;
	private void startScraper(){
		try{
			new Thread(new Runnable() {
				public void run() {
					while(true){
						if(shouldStop)
							break;
						games = NetworkUtil.getListData(new FinishLoadingService() {
							public void onEnd() {}
						});
						for(final String id : games.keySet()){
							runOnUiThread(new Runnable(){
								public void run(){
									if(!added.add(id))
										return;
									GameButton b = new GameButton(MainActivity.this, games.get(id), id);
									b.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, height/10));
									((LinearLayout.LayoutParams)b.getLayoutParams()).setMargins(20, 20, 20, 20);
									((LinearLayout)findViewById(R.id.llScrollGames)).addView(b);
								}
							});
						}
						try {Thread.sleep(2000);} catch (InterruptedException e) {}
					}
				}
			}).start();
		}catch(Exception e){}
	}
	
	private static Map<String, String> games;
	private OnClickListener getOnClickListener(){
		return new OnClickListener() {
			public void onClick(final View v) {
				new Thread(new Runnable(){
					public void run(){
						runOnUiThread(new Runnable(){
							public void run(){
								findViewById(R.id.gbClassic).startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_left));
								findViewById(R.id.gbMatchMaker).startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_right));
								findViewById(R.id.rlOuter).startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_alpha));
							}
						});
						
						try {
							Thread.sleep(250);
						} catch (InterruptedException e) {}
						

						final Animation animHandler = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_left);
						animHandler.setAnimationListener(new AnimationListener(){

							@Override
							public void onAnimationEnd(Animation arg0) {
								startGameSetup(v);
							}

							@Override
							public void onAnimationRepeat(Animation arg0) {}

							@Override
							public void onAnimationStart(Animation arg0) {}
							
						});
						runOnUiThread(new Runnable(){
							public void run(){
								findViewById(R.id.gbRush).startAnimation(animHandler);
								findViewById(R.id.gbHost).startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_right));
							}
						});
					}
				}).start();
			}
			
			private void startGameSetup(View v){
				switch(v.getId()){
				case R.id.gbClassic:
					CameraActivity.mode = GameMode.CLASSIC;
					startGame();
					break;
				case R.id.gbRush:
					CameraActivity.mode = GameMode.RUSH;
					startGame();
					break;
				case R.id.gbMatchMaker:
					CameraActivity.mode = GameMode.MATCH_MAKER_V2;
					startGame();
					break;
				case R.id.gbHost:
					findViewById(R.id.rlHostInfo).setVisibility(View.VISIBLE);
					break;
				case R.id.gbStart:
					findViewById(R.id.rlHostInfo).setVisibility(View.INVISIBLE);
					/*CameraActivity.gameID = */NetworkUtil.postData(new Game(((TextView)findViewById(R.id.etName)).getText().toString(), "stuff",
									CameraActivity.color = ColorUtil
											.getRandomColor(),
									CameraActivity.length = 60000*((NumberPicker)findViewById(R.id.npTime)).getValue()));
					startActivity(new Intent("com.lahacks.colormatch.RULESACTIVITY"));
					break;
				}
			}
			
			private void startGame(){
				CameraActivity.color = ColorUtil.getRandomColor();
				CameraActivity.length = 62000;
				CameraActivity.gameID="-1";
				startActivity(new Intent("com.lahacks.colormatch.CAMERAACTIVITY"));
				overridePendingTransition(R.anim.fill_alpha, R.anim.fade_alpha);
			}
		};
	}
	
}
