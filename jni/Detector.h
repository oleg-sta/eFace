#include "Stage.h"
#include "VectorRects.cpp"
#include "Rect.cpp"
#include "Rectangle.cpp"

class Detector
{
public:
	Detector();
	~Detector();
	Point* size;
	int sizeStages;
	Stage** stages;
    int** getIntegralCanny(int** grayImage, int width, int height);
    VectorRects* getFaces(float baseScale, float scale_inc, float increment, int min_neighbors, bool doCannyPruning, int **imageLocal, int len1, int len2);
    VectorRects* merge(VectorRects* rects, int min_neighbors);
    bool equals(Rectangle* r1, Rectangle* r2);
};
