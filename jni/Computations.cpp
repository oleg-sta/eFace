#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <android/log.h>
#include "Computations.h"


Computations::Computations() {
}

//void Computations::getFaces(JNIEnv* env, jobject thiz, jobjectArray image, jfloat baseScale, jfloat increment,
//		jint min_neighbors, jfloat scale_inc, jboolean doCannyPruning, jobject detectorJObj) {
//	Detector* de = new Detector();
//	de->getFaces(baseScale, scale_inc, increment,
//			min_neighbors, doCannyPruning, imageLocal, len1, len2);
//}

extern "C" {

int getVal();

JNIEXPORT jint JNICALL Java_com_example_Computations_stringFromJNI(JNIEnv* env, jobject thiz) {
	return getVal();
}

JNIEXPORT jint JNICALL Java_com_example_Computations_intFromJni(JNIEnv* env, jobject thiz, jintArray arr) {
	jsize d = env->GetArrayLength(arr);
	jboolean j;
	int * p = env->GetIntArrayElements(arr, &j);
	jint e = p[0];
	return e;
}

JNIEXPORT jint JNICALL Java_com_example_Computations_intFromJni2(JNIEnv* env, jobject thiz, jintArray arr) {
	jboolean j;
	int * p = env->GetIntArrayElements(arr, &j);
	p[0] = 22;
	return 0;
}

JNIEXPORT jobjectArray JNICALL Java_com_example_Computations_findFaces(JNIEnv* env, jobject thiz, jobjectArray image, jfloat baseScale, jfloat increment,
		jint min_neighbors, jfloat scale_inc, jboolean doCannyPruning, jobject detectorJObj) {

	// копирование изображения в С массив
	//int res = 0;
	int len1 = env -> GetArrayLength(image);
	jboolean j2;
	jintArray dim=  (jintArray)env->GetObjectArrayElement(image, 0);
	int len2 = env -> GetArrayLength(dim);
	// copy to local
	int **imageLocal;
	imageLocal = new int*[len1];
	__android_log_print(ANDROID_LOG_INFO, "Computations", "copy image %d %d", len1, len2);
	for (int i = 0; i < len1; i++) {
		jintArray oneDim= (jintArray)env->GetObjectArrayElement(image, i);
		int *element = env->GetIntArrayElements(oneDim, &j2);
		imageLocal[i] = new int[len2];
		for(int j=0; j<len2; ++j) {
			imageLocal[i][j]= element[j];
			//res += element[j];
		}
		env->ReleaseIntArrayElements(oneDim, element, JNI_ABORT);
		env->DeleteLocalRef(oneDim);
	}
	Detector* d = new Detector();
	Computations* comp = new Computations();

	jclass clsDetector = env->GetObjectClass(detectorJObj);

	jfieldID sizeFieldId3 = env->GetFieldID(clsDetector, "size",
			"Ldetection/Point;");
	jobject jobjarray = env->GetObjectField(detectorJObj, sizeFieldId3);
	d->size = comp->getPoint(env, jobjarray);
	env->DeleteLocalRef(jobjarray);

	__android_log_write(ANDROID_LOG_INFO, "Computations", "1");
	jfieldID stagesFieldId2 = env->GetFieldID(clsDetector, "stages", "Ljava/util/List;");
	__android_log_write(ANDROID_LOG_INFO, "Computations", "4");
	jobject stagesList = env->GetObjectField(detectorJObj, stagesFieldId2);
	__android_log_write(ANDROID_LOG_INFO, "Computations", "5");

	jclass listClass = env->FindClass( "java/util/List" );
	jmethodID getMethodIDList = env->GetMethodID( listClass, "get", "(I)Ljava/lang/Object;" );
	jmethodID sizeMethodIDList = env->GetMethodID( listClass, "size", "()I" );
	int listStagesCount = (int)env->CallIntMethod( stagesList, sizeMethodIDList );

	__android_log_write(ANDROID_LOG_INFO, "Computations", "6");

	Stage** stages = new Stage*[listStagesCount];
	d->stages = stages;
	d->sizeStages = listStagesCount;
	for( int i=0; i < listStagesCount; ++i )
	{
		stages[i] = new Stage();
		//stagesList[i] = new Stage();
		// Call "java.util.List.get" method and get IdentParams object by index.
		jobject stage = env->CallObjectMethod( stagesList, getMethodIDList, i);
		jclass cls = env->GetObjectClass(stage);
		stages[i]->threshold = comp->getObjectFieldF(env, stage, cls, "threshold");
		jfieldID listTree = env->GetFieldID(cls, "trees", "Ljava/util/List;");
		jobject listTreeJOjbect = env->GetObjectField(stage, listTree);
		int listTreesCount = (int)env->CallIntMethod( listTreeJOjbect, sizeMethodIDList );
		Tree** trees = new Tree*[listTreesCount];
		for (int j = 0; j < listTreesCount; j++) {
			trees[j] = new Tree();
			jobject treeJObject = env->CallObjectMethod(listTreeJOjbect, getMethodIDList, j);
			jclass clsTree = env->GetObjectClass(treeJObject);
			jfieldID listFeaturesField = env->GetFieldID(clsTree, "features", "Ljava/util/List;");
			jobject listFeaturesTreeJOjbect = env->GetObjectField(treeJObject, listFeaturesField);
			int listFeatures = (int)env->CallIntMethod( listFeaturesTreeJOjbect, sizeMethodIDList );
			Feature** features = new Feature*[listFeatures];
			for (int k = 0; k < listFeatures; k++) {
				jobject featureJObject = env->CallObjectMethod(listFeaturesTreeJOjbect, getMethodIDList, k);
				features[k] = comp->getFeature(env, featureJObject);
				env->DeleteLocalRef(featureJObject);
			}
			trees[j]->features = features;
			trees[j]->lengthFeatures = listFeatures;
			env->DeleteLocalRef(listFeaturesTreeJOjbect);
			env->DeleteLocalRef(clsTree);
			env->DeleteLocalRef(treeJObject);
		}
		//stages[i] = new Stage();
		stages[i]->trees = trees;
		stages[i]->lengthTrees = listTreesCount;
		//stages[i]->threshold = threshold;
		env->DeleteLocalRef(listTreeJOjbect);
		env->DeleteLocalRef(cls);
		env->DeleteLocalRef(stage);
	}
	env->DeleteLocalRef(stagesList);
	__android_log_write(ANDROID_LOG_INFO, "Computations", "Wow!");

	__android_log_print(ANDROID_LOG_INFO, "Computations", "Stages %d", d->sizeStages);
	VectorRects* faces = d->getFaces(baseScale, scale_inc, increment,
			min_neighbors, doCannyPruning, imageLocal, len1, len2);

	__android_log_print(ANDROID_LOG_INFO, "Computations", "Stages2 %d", faces->currIndex);

	jclass cls = env->FindClass("detection/Rectangle");
	jobjectArray jobAr =env->NewObjectArray(faces->currIndex, cls, NULL);

	jmethodID constructor = env->GetMethodID(cls, "<init>", "(IIII)V");
	for (int i = 0; i < faces->currIndex; i++) {
		Rectangle* re = faces->rects[i];
		jobject object = env->NewObject(cls, constructor, re->x, re->y, re->width, re->height);
		env->SetObjectArrayElement(jobAr, i, object);
	}

	// release memory
	for (int i = 0; i < len1; i++) {
		free(imageLocal[i]);
	}
	free(imageLocal);

	return jobAr;
}

Feature* Computations::getFeature(JNIEnv* env, jobject featureJObject) {
	Feature* feature = new Feature();
	jclass clsFeature = env->GetObjectClass(featureJObject);

	jfieldID rectsFieldId2 = env->GetFieldID(clsFeature, "rects",
			"[Ldetection/Rect;");
	jobject jobjarray2 = env->GetObjectField(featureJObject, rectsFieldId2);
	int rectslength = env->GetArrayLength((jobjectArray) jobjarray2);
	rectslength = getObjectField(env, featureJObject, clsFeature, "nb_rects");
	Rect** rects = new Rect*[rectslength];
	for (int l = 0; l < rectslength; l++) {
		jobject rectObj = env->GetObjectArrayElement((jobjectArray) jobjarray2,
				l);
		rects[l] = getRect(env, rectObj);
		env->DeleteLocalRef(rectObj);
	}
	feature->rects = rects;
	feature->nb_rects = getObjectField(env, featureJObject, clsFeature, "nb_rects");
	feature->threshold = getObjectFieldF(env, featureJObject, clsFeature, "threshold");
	feature->left_val = getObjectFieldF(env, featureJObject, clsFeature, "left_val");
	feature->right_val = getObjectFieldF(env, featureJObject, clsFeature, "right_val");

	jfieldID sizeFieldId2 = env->GetFieldID(clsFeature, "size",
			"Ldetection/Point;");
	jobject jobjarray = env->GetObjectField(featureJObject, sizeFieldId2);
	feature->size = getPoint(env, jobjarray);
	env->DeleteLocalRef(jobjarray);

	feature->left_node = getObjectField(env, featureJObject, clsFeature, "left_node");
	feature->right_node = getObjectField(env, featureJObject, clsFeature, "right_node");
	jfieldID has_left_valFieldId2 = env->GetFieldID(clsFeature, "has_left_val",
			"Z");
	feature->has_left_val = env->GetBooleanField(featureJObject,
			has_left_valFieldId2);
	jfieldID has_right_valFieldId2 = env->GetFieldID(clsFeature,
			"has_right_val", "Z");
	feature->has_right_val = env->GetBooleanField(featureJObject,
			has_right_valFieldId2);

	env->DeleteLocalRef(clsFeature);
	env->DeleteLocalRef(jobjarray2);
	return feature;
}

Rect* Computations::getRect(JNIEnv* env, jobject rectObj) {
	Rect* rect = new Rect();
	jclass clsFeature = env->GetObjectClass(rectObj);
	rect->x1 = getObjectField(env, rectObj, clsFeature, "x1");
	rect->x2 = getObjectField(env, rectObj, clsFeature, "x2");
	rect->y1 = getObjectField(env, rectObj, clsFeature, "y1");
	rect->y2 = getObjectField(env, rectObj, clsFeature, "y2");
	rect->weight = getObjectFieldF(env, rectObj, clsFeature, "weight");
	__android_log_print(ANDROID_LOG_INFO, "Computations", "getRect18 %d %d %d %d %f", rect->x1, rect->x2, rect->y1, rect->y2, rect->weight);
	env->DeleteLocalRef(clsFeature);
	return rect;
}

Point* Computations::getPoint(JNIEnv* env, jobject rectObj) {
	Point* p = new Point();
	jclass clsPoint = env->GetObjectClass(rectObj);
	p->x = getObjectField(env, rectObj, clsPoint, "x");
	p->y = getObjectField(env, rectObj, clsPoint, "y");
	env->DeleteLocalRef(clsPoint);
	return p;
}

int Computations::getObjectField(JNIEnv* env, jobject obj, jclass clsFeature, const char* name) {
	jfieldID x1FieldId2 = env->GetFieldID(clsFeature, name, "I");
	return env->GetIntField(obj, x1FieldId2);
}
float Computations::getObjectFieldF(JNIEnv* env, jobject obj, jclass clsFeature, const char* name) {
	jfieldID x1FieldId2 = env->GetFieldID(clsFeature, name, "F");
	return env->GetFloatField(obj, x1FieldId2);
}

int getVal() {
	return 155;
}


}
