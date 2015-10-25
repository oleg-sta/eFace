package ru.trolleg.faces.jni;

import detection.Detector;
import detection.Rectangle;

public class Computations {
    static {
        System.loadLibrary("Computations");
    }

    public native Rectangle[] findFaces(int[][] grayImage, float baseScale, float increment, int min_neighbors,
            float scale_inc, boolean doCannyPruning, Detector detector, int threadsNum);

}
