#include "Detector.h"

Detector::Detector() {
}

Detector::~Detector() {
	delete[] stages;
}

VectorRects* Detector::getFaces(float baseScale, float scale_inc, float increment,
		int min_neighbors, bool doCannyPruning, int **imageLocal, int width,
		int height) {

	__android_log_print(ANDROID_LOG_INFO, "Detector", "getFaces baseScale=%f scale_inc=%f increment=%f min_neighbors=%d width=%d height=%d", baseScale, scale_inc, increment, min_neighbors, width, height);

	VectorRects* ret = new VectorRects();
	float maxScale = fmin((float) width / size->x, (float) height / size->y);
	//Stage* s = new Stage();
	/* Compute the grayscale image, the integral image and the squared integral image.*/
	int** grayImage = new int*[width]; // ������������� ��������
	int** img = new int*[width]; // ������������ ��������� ��������
	int** squares = new int*[width];
	for (int i = 0; i < width; i++) {
		grayImage[i] = new int[height];
		img[i] = new int[height];
		squares[i] = new int[height];
		int col = 0;
		int col2 = 0;
		for (int j = 0; j < height; j++) {
			int c = imageLocal[i][j]; //getRGB(i,j);
			int red = (c & 0x00ff0000) >> 16;
			int green = (c & 0x0000ff00) >> 8;
			int blue = c & 0x000000ff;
			int value = (30 * red + 59 * green + 11 * blue) / 100;
			img[i][j] = value;
			grayImage[i][j] = (i > 0 ? grayImage[i - 1][j] : 0) + col + value;
			squares[i][j] = (i > 0 ? squares[i - 1][j] : 0) + col2
					+ value * value;
			col += value;
			col2 += value * value;
		}
	}
	int** canny;
	if (doCannyPruning) {
		canny = getIntegralCanny(img, width, height);
	}

	__android_log_print(ANDROID_LOG_INFO, "Detector", "gettFaces3 baseScale=%f maxScale=%f scale_inc=%f", baseScale, maxScale, scale_inc);
	for (float scale = baseScale; scale < maxScale; scale *= scale_inc) {
		int step = (int) (scale * size->x * increment);
		int sizeI = (int) (scale * size->x * 1);
		__android_log_print(ANDROID_LOG_INFO, "Detector", "gettFaces2 %f %d %d", scale, step, sizeI);
		/*For each position of the window on the image, check whether the object is detected there.*/
		for (int i = 0; i < width - sizeI; i += step) {
			//__android_log_print(ANDROID_LOG_INFO, "Detector", "gettFaces2 i=%d", i);
			for (int j = 0; j < height - sizeI; j += step) {
				/* If Canny pruning is on, compute the edge density of the zone.
				 * If it is too low, the object should not be there so skip the region.*/
				if (doCannyPruning) {
					int edges_density = canny[i + sizeI][j + sizeI] + canny[i][j]
							- canny[i][j + sizeI] - canny[i + sizeI][j];
					int d = edges_density / sizeI / sizeI;
					if (d < 20 || d > 100)
						continue;
				}
				bool pass = true;
				/* Perform each stage of the detector on the window. If one stage fails, the zone is rejected.*/

				for (int iStage = 0; iStage < sizeStages; iStage++) {
					//__android_log_print(ANDROID_LOG_INFO, "Detector", "gettFaces2 stageI %d", iStage);
					Stage* s = stages[iStage];
					if (!s->pass2(grayImage, squares, i, j, scale)) {
						pass = false;
						break;
					}
				}

				/* If the window passed all stages, add it to the results. */
				if (pass) {
					__android_log_print(ANDROID_LOG_INFO, "Detector", "we pass!");
					Rectangle* r = new Rectangle(i, j, sizeI, sizeI);
					ret->addRect(r);
				}
			}
		}

	}
	return merge(ret, min_neighbors);
}

int** Detector::getIntegralCanny(int** grayImage, int width, int height) {
	int** canny = new int*[width];
	int** grad = new int*[width];
	for (int i = 0; i < width; i++) {
		canny[i] = new int[height];
		grad[i] = new int[height];
	}
	for (int i = 2; i < width - 2; i++)
		for (int j = 2; j < height - 2; j++) {
			int sum = 0;
			sum += 2 * grayImage[i - 2][j - 2];
			sum += 4 * grayImage[i - 2][j - 1];
			sum += 5 * grayImage[i - 2][j + 0];
			sum += 4 * grayImage[i - 2][j + 1];
			sum += 2 * grayImage[i - 2][j + 2];
			sum += 4 * grayImage[i - 1][j - 2];
			sum += 9 * grayImage[i - 1][j - 1];
			sum += 12 * grayImage[i - 1][j + 0];
			sum += 9 * grayImage[i - 1][j + 1];
			sum += 4 * grayImage[i - 1][j + 2];
			sum += 5 * grayImage[i + 0][j - 2];
			sum += 12 * grayImage[i + 0][j - 1];
			sum += 15 * grayImage[i + 0][j + 0];
			sum += 12 * grayImage[i + 0][j + 1];
			sum += 5 * grayImage[i + 0][j + 2];
			sum += 4 * grayImage[i + 1][j - 2];
			sum += 9 * grayImage[i + 1][j - 1];
			sum += 12 * grayImage[i + 1][j + 0];
			sum += 9 * grayImage[i + 1][j + 1];
			sum += 4 * grayImage[i + 1][j + 2];
			sum += 2 * grayImage[i + 2][j - 2];
			sum += 4 * grayImage[i + 2][j - 1];
			sum += 5 * grayImage[i + 2][j + 0];
			sum += 4 * grayImage[i + 2][j + 1];
			sum += 2 * grayImage[i + 2][j + 2];

			canny[i][j] = sum / 159;
		}

	/*Computation of the discrete gradient of the image.*/
	for (int i = 1; i < width - 1; i++)
		for (int j = 1; j < height - 1; j++) {
			int grad_x = -canny[i - 1][j - 1] + canny[i + 1][j - 1]
					- 2 * canny[i - 1][j] + 2 * canny[i + 1][j]
					- canny[i - 1][j + 1] + canny[i + 1][j + 1];
			int grad_y = canny[i - 1][j - 1] + 2 * canny[i][j - 1]
					+ canny[i + 1][j - 1] - canny[i - 1][j + 1]
					- 2 * canny[i][j + 1] - canny[i + 1][j + 1];
			grad[i][j] = abs(grad_x) + abs(grad_y);
		}

	/* Suppression of non-maxima of the gradient and computation of the integral Canny image. */
	for (int i = 0; i < width; i++) {
		int col = 0;
		for (int j = 0; j < height; j++) {
			int value = grad[i][j];
			canny[i][j] = (i > 0 ? canny[i - 1][j] : 0) + col + value;
			col += value;
		}
	}
	return canny;
}

int abs(int x) {
	if (x < 0) {
		x = -x;
	}
	return x;
}

VectorRects* Detector::merge(VectorRects* rects, int min_neighbors) {
	VectorRects* retour = new VectorRects();
	int* ret = new int[rects->currIndex];
	int nb_classes = 0;
	for (int i = 0; i < rects->currIndex; i++) {
		bool found = false;
		for (int j = 0; j < i; j++) {
			if (equals(rects->rects[i], rects->rects[j])) {
				found = true;
				ret[i] = ret[j];
			}
		}
		if (!found) {
			ret[i] = nb_classes;
			nb_classes++;
		}
	}
	int* neighbors = new int[nb_classes];
	Rectangle** rect = new Rectangle*[nb_classes];
	for (int i = 0; i < nb_classes; i++) {
		neighbors[i] = 0;
		rect[i] = new Rectangle(0, 0, 0, 0);
	}
	for (int i = 0; i < rects->currIndex; i++) {
		neighbors[ret[i]]++;
		rect[ret[i]]->x += rects->rects[i]->x;
		rect[ret[i]]->y += rects->rects[i]->y;
		rect[ret[i]]->height += rects->rects[i]->height;
		rect[ret[i]]->width += rects->rects[i]->width;
	}
	for (int i = 0; i < nb_classes; i++) {
		int n = neighbors[i];
		if (n >= min_neighbors) {
			Rectangle* r = new Rectangle(0, 0, 0, 0);
			r->x = (rect[i]->x * 2 + n) / (2 * n);
			r->y = (rect[i]->y * 2 + n) / (2 * n);
			r->width = (rect[i]->width * 2 + n) / (2 * n);
			r->height = (rect[i]->height * 2 + n) / (2 * n);
			retour->addRect(r);
		}
	}
	return retour;
}

bool Detector::equals(Rectangle* r1, Rectangle* r2) {
	int distance = (int) (r1->width * 0.2);

	if (r2->x <= r1->x + distance && r2->x >= r1->x - distance
			&& r2->y <= r1->y + distance && r2->y >= r1->y - distance
			&& r2->width <= (int) (r1->width * 1.2)
			&& (int) (r2->width * 1.2) >= r1->width)
		return true;
	if (r1->x >= r2->x && r1->x + r1->width <= r2->x + r2->width && r1->y >= r2->y
			&& r1->y + r1->height <= r2->y + r2->height)
		return true;
	return false;
}