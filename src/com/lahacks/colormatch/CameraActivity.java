package com.lahacks.colormatch;

import java.io.ByteArrayInputStream;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.graphics.Typeface;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.ShutterCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CameraActivity extends Activity {

	private static final int RUSH_MATCHES = 5;
	
	//make all of this in the bundle
	public static GameMode mode = GameMode.RUSH;
	public static int color, length;
	public static double score, globalHighScore;
	public static long expTime;
	public static Bitmap bestBit;
	public static String gameID;
	
	private double bestScore = 0;
	private long startTime = 0;
	private Camera camera;
	private TextureView tvCamera;
	private RelativeLayout colorBox, bestPic;
	private SurfaceTexture texture;
	private GameSession session;

	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.camera2);
		tvCamera = (TextureView) findViewById(R.id.tvCamera);
		colorBox = (RelativeLayout) findViewById(R.id.rlColorBox);
		bestPic = (RelativeLayout) findViewById(R.id.rlBestPic);
		setIvColorBackground();
		((TextView)findViewById(R.id.tvScore)).setTypeface(Typeface.createFromAsset(getAssets(), "Roboto-Black.ttf"));
		((TextView)findViewById(R.id.tvTime)).setTypeface(Typeface.createFromAsset(getAssets(), "Roboto-Black.ttf"));
		tvCamera.setSurfaceTextureListener(surfaceListener);
		tvCamera.setOnClickListener(onClickListener);
		colorBox.setOnClickListener(onClickListener);
		session = new GameSession(length, new GameSession.CountDownTimerService() {
			public void onTick(long timeLeft) {
				if(mode==GameMode.RUSH)
					timeLeft = System.currentTimeMillis() - startTime;
				((TextView)findViewById(R.id.tvTime)).setText(String.format("%2d:%02d", timeLeft/60000, timeLeft%60000/1000));
			}
			public void onFinish() {
				if(mode==GameMode.RUSH){
					long timeLeft = System.currentTimeMillis() - startTime;
					((TextView)findViewById(R.id.tvTime)).setText(String.format("%2d:%02d", timeLeft/60000, timeLeft%60000/1000));
					session.start(mode);
					return;
				}
				score = bestScore;
				startActivity(new Intent("com.lahacks.colormatch.ENDGAMEACTIVITY"));
			}
		});
		startTime = session.start(mode);
	}
	
	@Override
	public void onPause(){
		super.onPause();
		if(!session.isFinished() && session.isStarted())
			session.interrupt(this);
	}
	
	@Override
	public void onResume(){
		super.onResume();
	}
	
	private Animation getMinimizeAnimation(){
		AnimationSet ret = new AnimationSet(true);
		ret.addAnimation(new TranslateAnimation(0, 50, 0, 50));
		ret.addAnimation(new ScaleAnimation(1, (float)(100/540.0), 1, (float)(100/960.0)));
		return ret;
	}
	
	private void setIvColorBackground(){
		Drawable ivColorBackground = getResources().getDrawable(R.drawable.solid_round_color);
		ivColorBackground.setColorFilter(color, Mode.MULTIPLY);
		findViewById(R.id.ivColor).setBackgroundDrawable(ivColorBackground);
	}
	
	private void handleScore(double score, Bitmap bitmap){
		switch(mode){
		case MATCH_MAKER:
			color = ColorUtil.getRandomColor();
		case MATCH_MAKER_V2: 
			if(ColorUtil.isMatch(score)){
				color = ColorUtil.getRandomColor();
				setIvColorBackground();
				bestScore++;
				((ImageView) findViewById(R.id.ivPic))
						.setImageBitmap(bestBit = Bitmap.createBitmap(bitmap,
								bitmap.getWidth() / 2 - 50,
								bitmap.getHeight() / 2 - 50, 100, 100));
				findViewById(R.id.ivBestPicText).setVisibility(View.INVISIBLE);
			}
			((TextView)findViewById(R.id.tvScore)).setText((ColorUtil.isMatch(score)?"Nice!":"Nope :/") + "\nScore: "+(int)bestScore);
			break;
		case CLASSIC:
			if(score>bestScore){
				((ImageView) findViewById(R.id.ivPic))
						.setImageBitmap(bestBit = Bitmap.createBitmap(bitmap,
								bitmap.getWidth() / 2 - 50,
								bitmap.getHeight() / 2 - 50, 100, 100));
				findViewById(R.id.ivBestPicText).setVisibility(View.INVISIBLE);
				bestScore = score;
				((TextView)findViewById(R.id.tvScore)).setText("Best Score: "+String.format("%.1f\n", bestScore));
			}
			break;
		case RUSH:
			if(ColorUtil.isMatch(score)){
				bestScore++;
				((ImageView) findViewById(R.id.ivPic))
						.setImageBitmap(bestBit = Bitmap.createBitmap(bitmap,
								bitmap.getWidth() / 2 - 50,
								bitmap.getHeight() / 2 - 50, 100, 100));
				findViewById(R.id.ivBestPicText).setVisibility(View.INVISIBLE);
				if(bestScore<RUSH_MATCHES)
					color = ColorUtil.getRandomColor();
				setIvColorBackground();
			}
			((TextView)findViewById(R.id.tvScore)).setText((ColorUtil.isMatch(score)?"Nice!":"Nope :/") 
					+ "\n" + (int)(10-bestScore)+" left");
			if(bestScore>=RUSH_MATCHES){
				CameraActivity.score = (System.currentTimeMillis() - startTime);
				startActivity(new Intent("com.lahacks.colormatch.ENDGAMEACTIVITY"));
			}
			break;
		case TENS:
		
			break;	
		}
	}
	
	private void handlePicture(byte[] data) {
		double score[] = new double[2];
		Bitmap bitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(
				data));
		int width = bitmap.getWidth(), height = bitmap.getHeight();
		Log.wtf("Bitmap", width+" "+height);
		for (int a = width / 2 - 50; a < width / 2 + 50; a++)
			for (int s = height / 2 - 50; s < height / 2 + 50; s++){
				score[0] += ColorUtil.e76(bitmap.getPixel(a, s), color);
				score[1] += ColorUtil.e94(bitmap.getPixel(a, s), color);
			}
		score[0] /= 10000;
		score[1] /= 10000;
		
		score[0] = 100 - score[0];
		score[1] = 100 - score[1];
		if(score[0]<0)
			score[0]= 0;
		if(score[1]<0)
			score[1]= 0;
		
		handleScore(score[1], bitmap);
	}

	private static boolean isLoading = false;

	private OnClickListener onClickListener = 
		new OnClickListener() {
			public void onClick(View v) {
				if (isLoading || session.isFinished())
					return;
				isLoading = true;
				camera.takePicture(new ShutterCallback() {
					public void onShutter() {
						//MediaPlayer mp = MediaPlayer.create(CameraActivity.this, R.raw.camera);
						//mp.start();
					}                                  
				}, new Camera.PictureCallback() {
					public void onPictureTaken(byte[] data, Camera camera) {
						Log.wtf(camera.getParameters().getPictureSize().width+"", ""+camera.getParameters().getPictureSize().width);
						//if (data != null)
							//handlePicture(data);
					}
				}, new Camera.PictureCallback() {
					public void onPictureTaken(byte[] data, Camera camera) {
						Log.wtf(camera.getParameters().getPictureSize().width+"", ""+camera.getParameters().getPictureSize().width);
						if (data != null)
							handlePicture(data);
						isLoading = false;

						try {
							camera.setPreviewTexture(texture);
							camera.startPreview();
						} catch (Exception ex) {
							Log.wtf("ERROR", ex.toString());
						}
					}
				});
			}
		};

	private TextureView.SurfaceTextureListener surfaceListener =
		 new SurfaceTextureListener() {

			@Override
			public void onSurfaceTextureUpdated(SurfaceTexture surface) {

			}

			@Override
			public void onSurfaceTextureSizeChanged(SurfaceTexture surface,
					int width, int height) {

			}

			@Override
			public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
				camera.stopPreview();
				camera.release();
				return true;
			}

			@Override
			public void onSurfaceTextureAvailable(SurfaceTexture surface,
					int width, int height) {
				camera = Camera.open();
				texture = surface;

				findViewById(R.id.ivShotBox).getLayoutParams().width = (int) (100.0 * width / camera
						.getParameters().getPictureSize().height);
				findViewById(R.id.ivShotBox).getLayoutParams().height = (int) (100.0 * height / camera
						.getParameters().getPictureSize().width);

				colorBox.setLayoutParams(new RelativeLayout.LayoutParams(height / 4, height / 4));
				bestPic.setLayoutParams(new RelativeLayout.LayoutParams(height / 4, height / 4));
				((RelativeLayout.LayoutParams)bestPic.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
				
				try {
					camera.setPreviewTexture(surface);
					camera.setDisplayOrientation(90);
					Parameters p = camera.getParameters();
					p.setRotation(90);
					camera.setParameters(p);
					camera.startPreview();
				} catch (Exception ex) {
					Log.wtf("ERROR", ex+"");
					//Toast.makeText(CameraActivity.this, ex.toString(), Toast.LENGTH_LONG).show();
				}
			}
		};

}
