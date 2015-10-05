package com.example;

import java.util.List;

import detection.Stage;

public class Computations {
	static {
		System.loadLibrary("Computations");
	}

	public native int stringFromJNI();

	public native int intFromJni(int[] f);
	
	public native int intFromJni2(int[] f);

	public native int findFaces(int[][] grayImage, float baseScale, float scale_inc, boolean doCannyPruning);
	
}
