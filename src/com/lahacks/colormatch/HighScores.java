package com.lahacks.colormatch;

import android.content.Context;
import android.content.SharedPreferences;

public class HighScores {

	public static double[] readScores(Context c, GameMode mode){
		double ret[] = new double[10];
		SharedPreferences prefs = c.getSharedPreferences("com.lahacks.colormatch_"+mode, Context.MODE_PRIVATE);
		for(int a=0; a<10; a++)
			ret[a] = prefs.getFloat(a+"", 0);
		return ret;
	}
	
	public static void writeScore(double score, Context c, GameMode mode){
		double scores[] = readScores(c, mode);
		int rep = -1;
		for(int a=0; a<10; a++)
			if(mode.compare(score, scores[a])>0){
				rep = a;
				break;
			}
		if(rep==-1)
			return;
		for(int a=8; a>=rep; a--)
			scores[a+1] = scores[a];
		scores[rep] = score;
		SharedPreferences prefs = c.getSharedPreferences("com.lahacks.colormatch_"+mode, Context.MODE_PRIVATE);
		prefs.edit().clear().commit();
		for(int a=0; a<10; a++)
			prefs.edit().putFloat(a+"", (float) scores[a]).commit();
	}
	
}
