#include "Stage.h"

//Stage::Stage(Tree* trees2, int lengthTrees2, float threshold2) {
//	trees = trees2;
//	lengthTrees = lengthTrees2;
//	threshold = threshold2;
//}

bool Stage::pass2(int** grayImage, int** squares, int i, int j, float scale) {
	float sum = 0;
	/* Compute the sum of values returned by each tree of the stage. */
	for (int iTree = 0; iTree < lengthTrees; iTree++) {
		Tree* t = trees[iTree];
		sum += t->getVal(grayImage, squares, i, j, scale);
	}
	/*
	 * The stage succeeds if the sum exceeds the stage threshold, and fails
	 * otherwise.
	 */
	//__android_log_print(ANDROID_LOG_INFO, "Stage", "sum=%f threshold=%f", sum, threshold);
	return sum > threshold;
}
