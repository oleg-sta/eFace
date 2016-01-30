package ru.flightlabs.eface.data;

import android.graphics.Bitmap;

/**
 * Информация о местонахождении лица. Размеры в процентах. Фотография должна
 * быть "натурально" ориентирована, т.е. фотографию сначала необходимо повернуть
 * и уже потом применять кординаты.
 * 
 * @author sov
 * 
 */
public class Face {
    public double centerX, centerY, height, width;
    public int photoId;
    public String guid;
    public float probability;
}
