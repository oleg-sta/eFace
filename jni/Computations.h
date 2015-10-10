#include "Detector.cpp"
#include "Stage.cpp"
#include "Tree.cpp"

#ifndef _COMPUTATIONS_H_
#define _COMPUTATIONS_H_

class Computations {
public:
	Computations();
	Point* getPoint(JNIEnv* env, jobject rectObj);
	Feature* getFeature(JNIEnv* env, jobject featureJObject);
	Rect* getRect(JNIEnv* env, jobject rectObj);
	int getObjectField(JNIEnv* env, jobject obj, jclass clsFeature, const char* name);
	float getObjectFieldF(JNIEnv* env, jobject obj, jclass clsFeature, const char* name);
//    void getFaces(JNIEnv* env, jobject thiz, jobjectArray image, jfloat baseScale, jfloat increment,
//    		jint min_neighbors, jfloat scale_inc, jboolean doCannyPruning, jobject detectorJObj);
};

#endif
