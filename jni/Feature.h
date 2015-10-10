#include "Rect.h"
#include "Point.h"
#ifndef _FEATURE_H_
#define _FEATURE_H_

class Feature {
public:
	Rect** rects;
	int nb_rects;
	float threshold;
	float left_val;
	float right_val;
	Point* size;
	int left_node;
	int right_node;
	bool has_left_val;
	bool has_right_val;
	int getLeftOrRight(int** grayImage, int** squares, int i, int j, float scale);
};

#endif
