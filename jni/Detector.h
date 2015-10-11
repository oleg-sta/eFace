#ifndef _DETECTOR_H_
#define _DETECTOR_H_

#include "Stage.h"
#include "VectorRects.cpp"
#include "Rect.cpp"
#include "Rectangle.cpp"

class Detector
{
public:
	Detector();
	~Detector();

	int threadsNum;
	int curThread = 1;
	VectorRects** res2;

	float baseScale, scale_inc, increment, maxScale;
	int width, height;
	int borderX;
	bool doCannyPruning;
	int** grayImage; // интенсивность пикселей
	int** img; // интенсивноть квадратов пикселей
	int** squares;
	int** canny;
	Point* size;
	int sizeStages;
	Stage** stages;
    int** getIntegralCanny(int** grayImage, int width, int height);
    VectorRects* getFaces(float baseScale, float scale_inc, float increment, int min_neighbors, bool doCannyPruning, int **imageLocal, int len1, int len2);
    VectorRects* merge(VectorRects* rects, int min_neighbors);
    VectorRects* getResult(int thrN);
    bool equals(Rectangle* r1, Rectangle* r2);
    VectorRects* worker22( void);
};


#endif
