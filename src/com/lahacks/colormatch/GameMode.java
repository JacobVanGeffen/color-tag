package com.lahacks.colormatch;

import android.annotation.SuppressLint;

public enum GameMode {

	CLASSIC, MATCH_MAKER, MATCH_MAKER_V2, RUSH, TENS;

	@SuppressLint("DefaultLocale")
	public String formatScore(double score) {
		switch (this) {
		case RUSH:
			long time = (long) score;
			return String.format("%2d:%02d", time / 60000, time % 60000 / 1000);
		case CLASSIC:
		case TENS:
			return String.format("%.3f", score);
		case MATCH_MAKER:
		case MATCH_MAKER_V2:
			return String.format("%d", (int) score);
		default:
			return score + "";
		}
	}

	public int compare(double score1, double score2) {
		switch (this) {
		case RUSH:
			if(score2==0)
				return 1;
			return (score1 < score2 ? 1 : (score1 > score2 ? -1 : 0));
		default:
			return (score1 > score2 ? 1 : (score1 < score2 ? -1 : 0));
		}
	}

}
