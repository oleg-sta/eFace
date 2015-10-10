#import "Rectangle.h"

#ifndef _VECTORRECTS_H_
#define _VECTORRECTS_H_

class VectorRects {
public:
	VectorRects();
	void addRect(Rectangle* rect);
	int currIndex;
	Rectangle** rects;
private:
	int limit;
};

#endif
