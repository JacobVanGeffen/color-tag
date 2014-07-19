package com.lahacks.colormatch;

import android.graphics.Color;

public class ColorUtil {

	public static double e94(int color, int base) {
		@SuppressWarnings("unused")
		double[] lab1 = convertToLab(color), lab2 = convertToLab(base), delta = new double[3], 
				k = {1, 1, 1}, s = {1, 1+.045*Math.hypot(lab1[1], lab1[2]), 1+.015*Math.hypot(lab1[1], lab1[2])};
		delta[0] = lab1[0]-lab2[0];
		delta[1] = Math.hypot(lab1[1], lab1[2])-Math.hypot(lab2[1], lab2[2]);
		delta[2] = Math.sqrt(Math.pow(lab1[1]-lab2[1], 2)+Math.pow(lab1[2]-lab2[2], 2)-Math.pow(delta[1], 2));
		return Math.sqrt(Math.pow(delta[0]/s[0], 2)+Math.pow(delta[1]/s[1], 2)+Math.pow(delta[2]/s[2], 2));
	}

	public static double e76(int color, int base) {
		double[] lab1 = convertToLab(color), lab2 = convertToLab(base);
		double sum = 0;
		for (int a = 0; a < 3; a++)
			sum += Math.pow(lab1[a] - lab2[a], 2);
		return Math.sqrt(sum);
	}

	public static int score(int color, int base) {
		int score = 0;
		for (int a = 0; a < 3; a++)
			score += Math.abs((color >>> a * 8) & (0xff) - (base >>> a * 8)
					& (0xff));
		return score;
	}

	public static int scoreDistNoAbs(int color, int base) {
		int score = 0;
		for (int a = 0; a < 3; a++)
			score += (color >>> a * 8) & (0xff) - (base >>> a * 8) & (0xff);
		return score;
	}

	public static double scoreRat(int color, int base) {
		double rats[][] = {
				{ 1.0 * Color.red(color) / Color.blue(color),
						1.0 * Color.blue(color) / Color.green(color),
						1.0 * Color.green(color) / Color.red(color) },
				{ 1.0 * Color.red(base) / Color.blue(base),
						1.0 * Color.blue(base) / Color.green(base),
						1.0 * Color.green(base) / Color.red(base) } };
		return Math.sqrt(Math.pow(rats[0][0] - rats[1][0], 2)
				+ Math.pow(rats[0][1] - rats[1][1], 2)
				+ Math.pow(rats[0][2] - rats[1][2], 2));
	}

	public static int getRandomColor() {
		int ret = 0xff;
		for (int a = 0; a < 3; a++) {
			ret <<= 8;
			ret += rand(0x100, 0);
		}
		return ret;
	}

	public static int getLightColor() {
		int ret = 0xff;
		for (int a = 0; a < 3; a++) {
			ret <<= 8;
			ret += rand(128, 128);
		}
		return ret;
	}

	public static int getDarkColor() {
		int ret = 0xff;
		for (int a = 0; a < 3; a++) {
			ret <<= 8;
			ret += rand(128, 0);
		}
		return ret;
	}

	public static boolean isDark(int c) {
		return (((c >> 16) & 0xff) + ((c >> 8) & 0xff) + ((c) & 0xff)) < 0xff * 3.0 / 2;
	}

	private static int rand(int range, int min) {
		return (int) (Math.random() * range + min);
	}

	// var_R = ( R / 255 ) //R from 0 to 255
	// var_G = ( G / 255 ) //G from 0 to 255
	// var_B = ( B / 255 ) //B from 0 to 255
	//
	// if ( var_R > 0.04045 ) var_R = ( ( var_R + 0.055 ) / 1.055 ) ^ 2.4
	// else var_R = var_R / 12.92
	// if ( var_G > 0.04045 ) var_G = ( ( var_G + 0.055 ) / 1.055 ) ^ 2.4
	// else var_G = var_G / 12.92
	// if ( var_B > 0.04045 ) var_B = ( ( var_B + 0.055 ) / 1.055 ) ^ 2.4
	// else var_B = var_B / 12.92
	//
	// var_R = var_R * 100
	// var_G = var_G * 100
	// var_B = var_B * 100
	//
	// //Observer. = 2°, Illuminant = D65
	// X = var_R * 0.4124 + var_G * 0.3576 + var_B * 0.1805
	// Y = var_R * 0.2126 + var_G * 0.7152 + var_B * 0.0722
	// Z = var_R * 0.0193 + var_G * 0.1192 + var_B * 0.9505

	private static double[] toXYZ(int rgb) {
		double[] vals = new double[3], rets = new double[3];
		for (int a = 0; a < 3; a++)
			vals[a] = ((rgb >> a * 8) & 0xff) / 255.0;

		if (vals[0] > 0.04045)
			vals[0] = Math.pow((vals[0] + 0.055) / 1.055, 2.4);
		else
			vals[0] = vals[0] / 12.92;
		if (vals[1] > 0.04045)
			vals[1] = Math.pow((vals[1] + 0.055) / 1.055, 2.4);
		else
			vals[1] = vals[1] / 12.92;
		if (vals[2] > 0.04045)
			vals[2] = Math.pow((vals[2] + 0.055) / 1.055, 2.4);
		else
			vals[2] = vals[2] / 12.92;

		for (int a = 0; a < 3; a++)
			vals[a] *= 100;

		rets[0] = vals[0] * 0.4124 + vals[1] * 0.3576 + vals[2] * 0.1805;
		rets[1] = vals[0] * 0.2126 + vals[1] * 0.7152 + vals[2] * 0.0722;
		rets[2] = vals[0] * 0.0193 + vals[1] * 0.1192 + vals[2] * 0.9505;

		return rets;
	}

	// var_X = X / 95.047 //ref_X = 95.047 Observer= 2°, Illuminant= D65
	// var_Y = Y / 100.000 //ref_Y = 100.000
	// var_Z = Z / 108.883 //ref_Z = 108.883
	//
	// if ( xyz[0] > 0.008856 ) xyz[0] = xyz[0] ^ ( 1/3 )
	// else xyz[0] = ( 7.787 * xyz[0] ) + ( 16 / 116 )
	// if ( xyz[1] > 0.008856 ) xyz[1] = xyz[1] ^ ( 1/3 )
	// else xyz[1] = ( 7.787 * xyz[1] ) + ( 16 / 116 )
	// if ( xyz[2] > 0.008856 ) xyz[2] = xyz[2] ^ ( 1/3 )
	// else xyz[2] = ( 7.787 * xyz[2] ) + ( 16 / 116 )
	//
	// CIE-L* = ( 116 * xyz[1] ) - 16
	// CIE-a* = 500 * ( xyz[0] - xyz[1] )
	// CIE-b* = 200 * ( xyz[1] - xyz[2] )

	private static double[] toLab(double[] xyz) {
		double ret[] = new double[3];

		xyz[0] /= 95.047;
		xyz[1] /= 100.000;
		xyz[2] /= 108.883;

		if (xyz[0] > 0.008856)
			xyz[0] = Math.pow(xyz[0], (1.0 / 3));
		else
			xyz[0] = (7.787 * xyz[0]) + (16.0 / 116);
		if (xyz[1] > 0.008856)
			xyz[1] = Math.pow(xyz[1], (1.0 / 3));
		else
			xyz[1] = (7.787 * xyz[1]) + (16.0 / 116);
		if (xyz[2] > 0.008856)
			xyz[2] = Math.pow(xyz[2], (1.0 / 3));
		else
			xyz[2] = (7.787 * xyz[2]) + (16.0 / 116);

		ret[0] = (116 * xyz[1]) - 16;
		ret[1] = 500 * (xyz[0] - xyz[1]);
		ret[2] = 200 * (xyz[1] - xyz[2]);

		return ret;
	}

	public static double[] convertToLab(int rgb) {
		return toLab(toXYZ(rgb));
	}

	public static boolean isMatch(double score) {
		return score > 60;
	}

}
