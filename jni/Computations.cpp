#include <jni.h>
#include <stdio.h>
#include <stdlib.h>

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

JNIEXPORT jint JNICALL Java_com_example_Computations_findFaces(JNIEnv* env, jobject thiz, jobjectArray image, jfloat baseScale, jfloat scale_inc, jboolean doCannyPruning) {

	int res = 0;
	int len1 = env -> GetArrayLength(image);
	jboolean j2;
	jintArray dim=  (jintArray)env->GetObjectArrayElement(image, 0);
	int len2 = env -> GetArrayLength(dim);
	// copy to local
	int **imageLocal;
	imageLocal = new int*[len1];
	for (int i = 0; i < len1; i++) {
		jintArray oneDim= (jintArray)env->GetObjectArrayElement(image, i);
		int *element = env->GetIntArrayElements(oneDim, &j2);
		imageLocal[i] = new int[len2];
		for(int j=0; j<len2; ++j) {
			imageLocal[i][j]= element[j];
			res += element[j];
		}
	}

	for(float scale=baseScale;scale<0;scale*=scale_inc)
	{
		int step = (int) (scale * 24 * 1);
		int size = (int) (scale * 24);
		/*For each position of the window on the image, check whether the object is detected there.*/
		for (int i = 0; i < len1 - size; i += step) {
			for (int j = 0; j < len2 - size; j += step) {
				/* If Canny pruning is on, compute the edge density of the zone.
				 * If it is too low, the object should not be there so skip the region.*/
				if (doCannyPruning) {
//					int edges_density = canny[i + size][j + size] + canny[i][j]
//							- canny[i][j + size] - canny[i + size][j];
//					int d = edges_density / size / size;
//					if (d < 20 || d > 100)
//						continue;
				}
				bool pass = true;
				/* Perform each stage of the detector on the window. If one stage fails, the zone is rejected.*/
				/*
				for (Stage s : stages) {

					if (!s.pass(grayImage, squares, i, j, scale)) {
						pass = false;
						break;
					}
				}
				*/
				/* If the window passed all stages, add it to the results. */
				if (pass) {}
					//ret.add(new Rectangle(i, j, size, size));
			}
		}

	}
	// release memory
	for (int i = 0; i < len1; i++) {
		free(imageLocal[i]);
	}
	free(imageLocal);

	return res;
}

int getVal() {
	return 155;
}


}
