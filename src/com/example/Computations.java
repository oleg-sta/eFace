package com.example;

import java.util.List;

import detection.Detector;
import detection.Rectangle;
import detection.Stage;

public class Computations {
	static {
		System.loadLibrary("Computations");
	}

	public native int stringFromJNI();

	public native int intFromJni(int[] f);
	
	public native int intFromJni2(int[] f);

	public native Rectangle[] findFaces(int[][] grayImage, float baseScale, float increment, int min_neighbors, float scale_inc,
			boolean doCannyPruning, Detector detector, int threadsNum);
	
}
