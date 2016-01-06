package ru.trolleg.faces.jni;

import detection.Detector;
import detection.Rectangle;

public class Computations {
    static {
        System.loadLibrary("Computations");
    }

    public native Rectangle[] findFaces(int[][] grayImage, float baseScale, float increment, int min_neighbors,
            float scale_inc, boolean doCannyPruning, Detector detector, int threadsNum);
    
    /**
     * 
     * @param photo полный путь до фото
     * @param modelName полный путь до файла модели
     * @param koef коэфициент сжатия( меньше или равен 1)
     * @param rotate поворт (0 - 0%,1 - 90%,2 - 180%,3 - 270%)
     * @return
     */
    public native Rectangle[] findFaces2(String modelName, String modelName2, String photo, double koef, int rotate);

}
