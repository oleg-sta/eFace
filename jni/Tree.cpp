#include "Tree.h"

Tree::~Tree() {
	for (int i = 0; i < lengthFeatures; i++) {
			delete features[i];
		}
	delete[] features;
}

float Tree::getVal(int** grayImage, int** squares, int i, int j, float scale) {
	Feature* cur_node = features[0];
	while (true) {
		/* Compute the feature to see if we should go to the left or right child on the node.*/
		int where = cur_node->getLeftOrRight(grayImage, squares, i, j, scale);
		if (where == LEFT) {
			/* If the left child has a value, return it.*/
			if (cur_node->has_left_val) {
				return cur_node->left_val;
			} else {
				/* Else move to the left child node. */
				cur_node = features[cur_node->left_node];
			}
		} else {
			/* If the right child has a value, return it.*/
			if (cur_node->has_right_val) {
				return cur_node->right_val;
			} else {
				/* Else move to the right child node. */
				cur_node = features[cur_node->right_node];
			}
		}
	}
}
