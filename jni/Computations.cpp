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
    flip(matImage, matImage, 1); //transpose+flip(1)=CW
  } else if (rotflag == 3) { // 270 %
    transpose(matImage, matImage);
    flip(matImage, matImage,0); //transpose+flip(0)=CCW
  } else if (rotflag ==2) { // 180%
    flip(matImage, matImage, -1);    //flip(-1)=180
  } else if (rotflag != 0){ //if not 0,1,2,3:
    // cout  << "Unknown rotation flag(" << rotflag << ")" << endl;
  }
}

JNIEXPORT jobjectArray JNICALL Java_ru_flightlabs_eface_jni_Computations_findFaces2(JNIEnv* env, jobject thiz, jstring detectorXml1, jstring detectorXml2, jstring photoPath, jdouble koef, jint rotflat) {
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
	face_cascade.detectMultiScale( gray_image, faces, 1.25, 2, 0|CV_HAAR_SCALE_IMAGE, cv::Size(32, 32) );
	int sd = faces.size();
	__android_log_print(ANDROID_LOG_INFO, "Computations", "faces %d", sd);
	// применяем второй алгоритм
	for (int i = 0; i < sd; i++) {
		float faceMore = 0;
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

		cv::resize(croppedImage, croppedImage, cv::Size(36, 36));

		std::vector<cv::Rect> faces2;
		//face_cascade2.detectMultiScale( croppedImage, faces2, 1.1, 2, 0|CV_HAAR_SCALE_IMAGE, cv::Size());
		face_cascade2.detectMultiScale( croppedImage, faces2, 1.1, 1, 0|CV_HAAR_SCALE_IMAGE, cv::Size(32, 32));
		if (faces2.size() > 0) {
			int sd2 = faces2.size();
			__android_log_print(ANDROID_LOG_INFO, "Computations", "face true %d", sd2);
			cv::Rect r1 = faces2[0];
			__android_log_print(ANDROID_LOG_INFO, "Computations", "face1 %d %d %d %d", r1.x, r1.y, r1.width, r1.height);
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

}
