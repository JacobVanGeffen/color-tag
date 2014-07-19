package com.lahacks.colormatch;

import android.app.Activity;
import android.os.CountDownTimer;

public class GameSession {

	public static interface CountDownTimerService{
		public void onTick(long timeLeft);
		public void onFinish();
	}
	
	private int millis;
	private CountDownTimerService service;
	private boolean isStarted = false, isFinished = false;
	
	public GameSession(int time, CountDownTimerService service){
		millis = time;
		this.service = service;
	}
	
	public boolean isStarted(){
		return isStarted;
	}
	
	public boolean isFinished(){
		return isFinished;
	}
	
	private CountDownTimer timer;
	public long start(GameMode mode){
		isStarted = true;
		(timer = new CountDownTimer(millis, 10) {

		     public void onTick(long millisUntilFinished) {
		         service.onTick(millisUntilFinished);
		     }

		     public void onFinish() {
		         isFinished = true;
		    	 service.onFinish();
		     }
		  }).start();
		return System.currentTimeMillis();
	}
	
	public void interrupt(Activity c){
		timer.cancel();
		c.onBackPressed();
	}
	
}
