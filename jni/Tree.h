//include "Feature.h"
#include "Feature.cpp"

#ifndef _TREE_H_
#define _TREE_H_

class Tree {
public:
	int lengthFeatures;
	Feature** features;
	const static int LEFT = 0;
	const static int RIGHT = 1;
	float getVal(int** grayImage, int** squares, int i, int j, float scale);
};

#endif
