#include "Tree.h"

#ifndef _STAGE_H_
#define _STAGE_H_

class Stage {
public:
	//Stage(Tree** trees, int lengthTrees, float threshold);
	~Stage();
	int lengthTrees;
	Tree** trees;
	float threshold;

	bool pass2(int** grayImage, int** squares, int i, int j, float scale);
};

#endif
