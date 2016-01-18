#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <android/log.h>
#include "Computations.h"
#include <vector>
#include "opencv2/objdetect/objdetect.hpp"
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/imgproc/imgproc.hpp"
#include <math.h>

int const maxHeight = 320;
int const maxWidth = 480;


Computations::Computations() {
}

extern "C" {

void rot90(cv::Mat &matImage, int rotflag);

void rot90(cv::Mat &matImage, int rotflag){
  //1=CW, 2=CCW, 3=180
  if (rotflag == 1){ // 90%
    transpose(matImage, matImage);
    flip(matImage, matImage,1); //transpose+flip(1)=CW
  } else if (rotflag == 3) { // 270 %
    transpose(matImage, matImage);
    flip(matImage, matImage,0); //transpose+flip(0)=CCW
  } else if (rotflag ==2){ // 180%
    flip(matImage, matImage,-1);    //flip(-1)=180
  } else if (rotflag != 0){ //if not 0,1,2,3:
    // cout  << "Unknown rotation flag(" << rotflag << ")" << endl;
  }
}

JNIEXPORT jobjectArray JNICALL Java_ru_trolleg_faces_jni_Computations_findFaces2(JNIEnv* env, jobject thiz, jstring detectorXml1, jstring detectorXml2, jstring photoPath, jdouble koef, jint rotflat) {
	try {
	cv::CascadeClassifier face_cascade;
	const char *s = env->GetStringUTFChars(detectorXml1, NULL);
	const char *s2 = env->GetStringUTFChars(photoPath, NULL);
	__android_log_print(ANDROID_LOG_INFO, "Computations", "processing photo %s", s2);
	face_cascade.load(s);

	cv::CascadeClassifier face_cascade2;
	const char *s3 = env->GetStringUTFChars(detectorXml2, NULL);
	face_cascade2.load(s3);

	__android_log_print(ANDROID_LOG_INFO, "Computations", "imread start");
	cv::Mat img = cv::imread(s2, 1);
	__android_log_print(ANDROID_LOG_INFO, "Computations", "imread end %d", img.total() * img.elemSize());
	cv::resize(img, img, cv::Size(), koef, koef);
	__android_log_print(ANDROID_LOG_INFO, "Computations", "resize");
	rot90(img, rotflat);
	cv::Mat gray_image;
	cv::cvtColor( img, gray_image, CV_BGR2GRAY );
	__android_log_print(ANDROID_LOG_INFO, "Computations", "size %d %d", gray_image.rows, gray_image.cols);
	std::vector<cv::Rect> faces;
	std::vector<float> floats;
	face_cascade.detectMultiScale( gray_image, faces, 1.1, 2, 0|CV_HAAR_SCALE_IMAGE, cv::Size(32, 32) );
	int sd = faces.size();
	__android_log_print(ANDROID_LOG_INFO, "Computations", "faces %d", sd);
	// применяем второй алгоритм
	for (int i = 0; i < sd; i++) {
		float faceMore = 0.2;
		int x1 = faces[i].x - faces[i].width * faceMore / 2;
		int y1 = faces[i].y - faces[i].height * faceMore / 2;
		int x2 = faces[i].x + faces[i].width * (1 + faceMore / 2);
		int y2 = faces[i].y + faces[i].height * (1 + faceMore / 2);
		x1 = std::max(x1, 0);
		y1 = std::max(y1, 0);
		x2 = std::min(x2, gray_image.cols);
		y2 = std::min(y2, gray_image.rows);
		__android_log_print(ANDROID_LOG_INFO, "Computations", "faces %d %d %d %d", x1, y1, x2, y2);
		__android_log_print(ANDROID_LOG_INFO, "Computations", "faces %d %d", gray_image.rows, gray_image.cols);

		cv::Rect myROI(x1, y1, x2 - x1, y2  - y1);
		cv::Mat croppedImage = gray_image(myROI);

		//cv::resize(croppedImage, croppedImage, cv::Size(32, 32));

		std::vector<cv::Rect> faces2;
		//face_cascade2.detectMultiScale( croppedImage, faces2, 1.1, 2, 0|CV_HAAR_SCALE_IMAGE, cv::Size());
		face_cascade2.detectMultiScale( croppedImage, faces2, 1.1, 2, 0|CV_HAAR_SCALE_IMAGE, cv::Size(faces[i].width / (1+faceMore), faces[i].height / (1 + faceMore)));
		if (faces2.size() > 0) {
			int sd2 = faces2.size();
			__android_log_print(ANDROID_LOG_INFO, "Computations", "face true %d", sd2);
			floats.insert(floats.begin()+i, 1.0f);
		} else {
			__android_log_print(ANDROID_LOG_INFO, "Computations", "face false");
			floats.insert(floats.begin()+i, 0.5f);
		}

	}

	jclass cls = env->FindClass("detection/Rectangle");
	jmethodID constructor = env->GetMethodID(cls, "<init>", "(IIIIF)V");
	jobjectArray jobAr =env->NewObjectArray(faces.size(), cls, NULL);
	for( size_t i = 0; i < faces.size(); i++ )
	{
		jobject object = env->NewObject(cls, constructor, faces[i].x, faces[i].y, faces[i].width, faces[i].height, floats[i]);
		env->SetObjectArrayElement(jobAr, i, object);
	}

	__android_log_print(ANDROID_LOG_INFO, "Computations", "photo processed");
	return jobAr;
	}
	    catch (...) {
	    	__android_log_print(ANDROID_LOG_INFO, "Computations", "some error occured");
	    	jclass cls = env->FindClass("detection/Rectangle");
	    		jmethodID constructor = env->GetMethodID(cls, "<init>", "(IIIIF)V");
	    		jobjectArray jobAr =env->NewObjectArray(0, cls, NULL);

	        return jobAr;
	    }

}

JNIEXPORT jobjectArray JNICALL Java_ru_trolleg_faces_jni_Computations_findFaces(JNIEnv* env, jobject thiz, jobjectArray image, jfloat baseScale, jfloat increment,
		jint min_neighbors, jfloat scale_inc, jboolean doCannyPruning, jobject detectorJObj, jint threadsNum) {




	//d.
	// ����������� ����������� � � ������
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
	d->threadsNum = threadsNum;
	Computations* comp = new Computations();

	jclass clsDetector = env->GetObjectClass(detectorJObj);

	jfieldID sizeFieldDetector = env->GetFieldID(clsDetector, "size",
			"Ldetection/Point;");
	jobject jobjSize = env->GetObjectField(detectorJObj, sizeFieldDetector);
	d->size = comp->getPoint(env, jobjSize);
	env->DeleteLocalRef(jobjSize);

	jfieldID stagesFieldDetector = env->GetFieldID(clsDetector, "stages",
				"[Ldetection/Stage;");
	jobject stagesList = env->GetObjectField(detectorJObj, stagesFieldDetector);
	int listStagesCount = env->GetArrayLength((jobjectArray) stagesList);

	Stage** stages = new Stage*[listStagesCount];
	d->stages = stages;
	d->sizeStages = listStagesCount;
	for( int i=0; i < listStagesCount; ++i )
	{
		stages[i] = new Stage();
		jobject stage = env->GetObjectArrayElement((jobjectArray) stagesList,
						i);
		jclass cls = env->GetObjectClass(stage);
		stages[i]->threshold = comp->getObjectFieldF(env, stage, cls, "threshold");
		jfieldID listTree = env->GetFieldID(cls, "trees", "[Ldetection/Tree;");
		jobject listTreeJOjbect = env->GetObjectField(stage, listTree);
		int listTreesCount = (int)env->GetArrayLength( (jobjectArray)listTreeJOjbect);
		Tree** trees = new Tree*[listTreesCount];
		for (int j = 0; j < listTreesCount; j++) {
			trees[j] = new Tree();
			jobject treeJObject = env->GetObjectArrayElement((jobjectArray)listTreeJOjbect, j);
			jclass clsTree = env->GetObjectClass(treeJObject);
			jfieldID listFeaturesField = env->GetFieldID(clsTree, "features", "[Ldetection/Feature;");
			jobject listFeaturesTreeJOjbect = env->GetObjectField(treeJObject, listFeaturesField);
			int listFeatures = (int)env->GetArrayLength( (jobjectArray)listFeaturesTreeJOjbect );
			Feature** features = new Feature*[listFeatures];
			for (int k = 0; k < listFeatures; k++) {
				jobject featureJObject = env->GetObjectArrayElement((jobjectArray)listFeaturesTreeJOjbect, k);
				features[k] = comp->getFeature(env, featureJObject);
				env->DeleteLocalRef(featureJObject);
			}
			trees[j]->features = features;
			trees[j]->lengthFeatures = listFeatures;
			env->DeleteLocalRef(listFeaturesTreeJOjbect);
			env->DeleteLocalRef(clsTree);
			env->DeleteLocalRef(treeJObject);
		}
		stages[i]->trees = trees;
		stages[i]->lengthTrees = listTreesCount;
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
		delete[] imageLocal[i];
	}
	delete[] imageLocal;

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

}
