#include "VectorRects.h"


VectorRects::VectorRects() {
	limit = 50;
	rects = new Rectangle*[limit];
	currIndex = 0;
}

void VectorRects::addRect(Rectangle* rect) {
	if (currIndex >= limit) {
		int newSize = (double)limit * 1.5;
		Rectangle** newRects = new Rectangle*[newSize];
		for (int i = 0; i < limit; i++) {
			newRects[i] = rects[i];
		}
		delete[] rects;
		rects = newRects;
		limit = newSize;
	}
	rects[currIndex] = rect;
	currIndex++;
}
