package mean.shift.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import mean.shift.kernel.Kernel;
import mean.shift.metrics.Metrics;
import mean.shift.processing.ColorProcesser;
import mean.shift.pixel.Pixel;
import mean.shift.processing.MeanShiftParameter;
import mean.shift.utils.StopWatch;

public abstract class MeanShiftTask extends Task<Image> {

	protected static Logger LOGGER = LoggerFactory.getLogger(MeanShiftTask.class);

	protected StopWatch stopWatch = new StopWatch();

	protected Image image;
	protected int channels;
	protected Kernel kernel;
	protected int spatialPar;
	protected int rangePar;
	protected int maxIters;
	protected int minShift;
	protected Metrics metrics;

	protected static long algorithmProgress = 0;

	protected ColorProcesser colorProcesser = null;

	public MeanShiftTask(MeanShiftParameter parameter) {
		this.image = parameter.getImage();
		this.channels = parameter.getChannels();
		this.kernel = parameter.getKernel();
		this.spatialPar = parameter.getSpatialPar();
		this.rangePar = parameter.getRangePar();
		this.maxIters = parameter.getMaxIters();
		this.minShift = parameter.getMinShift();
		this.metrics = parameter.getMetrics();
		colorProcesser = new ColorProcesser();
	}

	@Override
	protected Image call() throws Exception {
		Pixel[] filtered = filter();
		updateTitle("Zakonczono przetwarzanie");
		return convertPixelToImage(filtered);
	}

	/**
	 * Konwersja tablicy pikseli luv na rysunek.
	 * @param pixels
	 * @return
	 */
	protected Image convertPixelToImage(Pixel[] pixels) {
		int width = (int)image.getWidth();
		int height = pixels.length / width;
		int[][] rawPixels;
		if (channels == 1) {
			rawPixels = colorProcesser.getGrayByteArray(pixels, width);
		} else {
			rawPixels = colorProcesser.getRgbArray(pixels, width);
		}
		WritableImage filteredImage = new WritableImage(width, height);
		PixelWriter pixelWriter = filteredImage.getPixelWriter();
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				pixelWriter.setArgb(i, j, rawPixels[i][j]);
			}
		}
		return filteredImage;
	}

	/**
	 * Metoda szablonowa dla przetwarzania pikseli.
	 * @return
	 */
	protected abstract Pixel[] process(int[][] pixels, Pixel[] luvInputImage);

	/**
	 * Wydzielenie pikseli luv i przefiltrowanie ich
	 * wg odpowiedniej metody.
	 * @return
	 */
	public Pixel[] filter() {
        stopWatch.start();
		algorithmProgress = 0;
		updateMessage(stopWatch.getFormattedTime());
		updateTitle("Trwa filtrowanie...");

		LOGGER.info("START MEAN SHIFT PROCESSING");
		int[][] rawPixels;
		Pixel[] pixels;
		if (channels == 1) {
			rawPixels = colorProcesser.getGrayByteArray(image);
			pixels = colorProcesser.getGrayscaleArray(rawPixels);
		} else {
			rawPixels = colorProcesser.getPixelArray(image);
			pixels = colorProcesser.getLuvArray(rawPixels);
		}
		Pixel[] processedPixels = process(rawPixels, pixels);

		return processedPixels;
	}



	protected void colorFiltration(Pixel[] luv, Pixel[] outImageLuv, int start, int end,
			int width, int height, int hrad, int hcolor, int pixelNumber) {

		int iters = 0;
		int xWindowCenterPosition, yWindowCenterPosition;
		int xWindowCenterPositionOld, yWindowCenterPositionOld;
		// stary color
		float oldPointColorL, oldPointColorU, oldPointColorV;
		// nowy kolor
		float pointColorL, pointColorU, pointColorV, xWindowCenterPositionShift, yWindowCenterPositionShift;

		float windowShiftX = 0, windowShiftY = 0;
		float pointNum = 0, colorNum = 0;
		float pointColorShiftL = 0, pointColorShiftU = 0, pointColorShiftV = 0;

		// dla kazdego piksela
		for (int i = start; i < end; i++) {
			// pobierz aktualna pozycje piksela
			xWindowCenterPosition = (int) luv[i].getPos().x();
			yWindowCenterPosition = (int) luv[i].getPos().y();
			// aktualna poyzcja i kolor
			float[] pointColor = luv[i].getColorVector();
			pointColorL = pointColor[0];
			pointColorU = pointColor[1];
			pointColorV = pointColor[2];
			// licznik iteracji
			iters = 0;
			// mean-shiftowanie
			do {
				// zachowanie starych danych
				xWindowCenterPositionOld = xWindowCenterPosition;
				yWindowCenterPositionOld = yWindowCenterPosition;
				oldPointColorL = pointColorL;
				oldPointColorU = pointColorU;
				oldPointColorV = pointColorV;
				// wartosci przesuniecia
				windowShiftX = 0; windowShiftY = 0;
				pointColorShiftL = 0; pointColorShiftU = 0; pointColorShiftV = 0;
				pointNum = 0; colorNum = 0;
				// MEAN SHIFT (17)
				for (int ry = -hrad; ry <= hrad; ry++) {
					int y2 = yWindowCenterPosition + ry;
					if (y2 >= 0 && y2 < height) {
						for (int rx = -hrad; rx <= hrad; rx++) {
							int x2 = xWindowCenterPosition + rx;
							if (x2 >= 0 && x2 < width) {
								float pointDistance = metrics.getDistance(rx, ry);
								if (pointDistance  <= hrad) {
									float[] pointColor2 = luv[y2 * width + x2].getColorVector();
									float dColorL = pointColorL - pointColor2[0];
									float dColorU = pointColorU - pointColor2[1];
									float dColorV = pointColorV - pointColor2[2];

									float colorDistance = metrics.getDistance(dColorL, dColorU, dColorV);
									if (colorDistance <= hcolor) {
										float pointKernelWeight = kernel.gFunction(pointDistance, hrad);
										windowShiftX += x2 * pointKernelWeight;
										windowShiftY += y2 * pointKernelWeight;
										pointNum += pointKernelWeight;
										float colorKernelWeight = kernel.gFunction(colorDistance, hcolor);
										pointColorShiftL += pointColor2[0] * colorKernelWeight;
										pointColorShiftU += pointColor2[1] * colorKernelWeight;
										pointColorShiftV += pointColor2[2] * colorKernelWeight;
										colorNum += colorKernelWeight;
									}
								}
							}
						}
					}
				}
				// nowe przesuniecie okna
				xWindowCenterPosition = (int) (windowShiftX * (1.0 / pointNum) + 0.5);
				yWindowCenterPosition = (int) (windowShiftY * (1.0 / pointNum) + 0.5);
				pointColorL = (float)(pointColorShiftL * (1.0f / colorNum));
				pointColorU = (float)(pointColorShiftU * (1.0f / colorNum));
				pointColorV = (float)(pointColorShiftV * (1.0f / colorNum));
				// mean-shift
				xWindowCenterPositionShift = xWindowCenterPosition - xWindowCenterPositionOld;
				yWindowCenterPositionShift = yWindowCenterPosition - yWindowCenterPositionOld;
				pointColorShiftL = pointColorL - oldPointColorL;
				pointColorShiftU = pointColorU - oldPointColorU;
				pointColorShiftV = pointColorV - oldPointColorV;
				iters++;
			} while (metrics.isWithinDistance(minShift, xWindowCenterPositionShift, yWindowCenterPositionShift,
					pointColorShiftL, pointColorShiftU, pointColorShiftV) && iters < maxIters);

			outImageLuv[i] = new Pixel(luv[i].getPos(), pointColorL, pointColorU, pointColorV);
			updateProgress(algorithmProgress++, pixelNumber);
			updateMessage(stopWatch.getFormattedTime());
		}
	}

	protected void grayscaleFiltration(Pixel[] luv, Pixel[] outImageLuv, int start, int end,
			int width, int height, int hrad, int hcolor, int pixelNumber) {

		int iters = 0;
		int xWindowCenterPosition, yWindowCenterPosition;
		int xWindowCenterPositionOld, yWindowCenterPositionOld;
		// stary color
		float oldPointColor;
		// nowy kolor
		float pointColor, xWindowCenterPositionShift, yWindowCenterPositionShift;

		float windowShiftX = 0, windowShiftY = 0;
		float pointNum = 0, colorNum = 0;
		float pointColorShift = 0;

		// dla kazdego piksela
		for (int i = start; i < end; i++) {
			// pobierz aktualna pozycje piksela
			xWindowCenterPosition = (int) luv[i].getPos().x();
			yWindowCenterPosition = (int) luv[i].getPos().y();
			// aktualna poyzcja i kolor
			pointColor = luv[i].getColorVector()[0];
			// licznik iteracji
			iters = 0;
			// mean-shiftowanie
			do {
				// zachowanie starych danych
				xWindowCenterPositionOld = xWindowCenterPosition;
				yWindowCenterPositionOld = yWindowCenterPosition;
				oldPointColor = pointColor;
				// wartosci przesuniecia
				windowShiftX = 0; windowShiftY = 0; pointColorShift = 0;
				pointNum = 0; colorNum = 0;
				// MEAN SHIFT (17)
				for (int ry = -hrad; ry <= hrad; ry++) {
					int y2 = yWindowCenterPosition + ry;
					if (y2 >= 0 && y2 < height) {
						for (int rx = -hrad; rx <= hrad; rx++) {
							int x2 = xWindowCenterPosition + rx;
							if (x2 >= 0 && x2 < width) {
								float pointDistance = metrics.getDistance(rx, ry);
								if (pointDistance  <= hrad) {
									float pointColor2 = luv[y2 * width + x2].getColorVector()[0];
									float dColor = pointColor - pointColor2;

									float colorDistance = metrics.getDistance(dColor);
									if (colorDistance <= hcolor) {
										float pointKernelWeight = kernel.gFunction(pointDistance, hrad);
										windowShiftX += x2 * pointKernelWeight;
										windowShiftY += y2 * pointKernelWeight;
										pointNum += pointKernelWeight;
										float colorKernelWeight = kernel.gFunction(colorDistance, hcolor);
										pointColorShift += pointColor2 * colorKernelWeight;
										colorNum += colorKernelWeight;
									}
								}
							}
						}
					}
				}
				// nowe przesuniecie okna
				xWindowCenterPosition = (int) (windowShiftX * (1.0 / pointNum) + 0.5);
				yWindowCenterPosition = (int) (windowShiftY * (1.0 / pointNum) + 0.5);
				pointColor = (float)(pointColorShift * (1.0f / colorNum));
				// mean-shift
				xWindowCenterPositionShift = xWindowCenterPosition - xWindowCenterPositionOld;
				yWindowCenterPositionShift = yWindowCenterPosition - yWindowCenterPositionOld;
				pointColorShift = pointColor - oldPointColor;
				iters++;
			} while (metrics.isWithinDistance(minShift, xWindowCenterPositionShift, yWindowCenterPositionShift,
					pointColorShift) && iters < maxIters);

			outImageLuv[i] = new Pixel(luv[i].getPos(), pointColor);
			updateProgress(algorithmProgress++, pixelNumber);
			updateMessage(stopWatch.getFormattedTime());
		}
	}
	/**
	 * Wygadzenie obrazu wg algorytmu MS.
	 * @param pixels wejsciowe piksele RGB
	 * @param luv wejsciowe piksele LUV
	 * @param outImageLuv wyjsciowe piksele LUV
	 * @param start Indeks poczatku tablicy
	 * @param end Indeks konca tablicy
	 */
	public void meanShiftFiltration(int[][] pixels, Pixel[] luv, Pixel[] outImageLuv, int start, int end) {

		LOGGER.info("START MEAN SHIFT ALGORITHM");

		int width = pixels.length;
		int height = pixels[0].length;
		int hrad = spatialPar;
		int hcolor = rangePar;
		int pixelNumber = luv.length;
		updateProgress(algorithmProgress++, pixelNumber);

		// tu powinien byc wzorzec strategii, ale juz mi sie nie chce
		if (this.channels > 1) {
			colorFiltration(luv, outImageLuv, start, end, width, height, hrad, hcolor, pixelNumber);
		} else {
			grayscaleFiltration(luv, outImageLuv, start, end, width, height, hrad, hcolor, pixelNumber);
		}

		LOGGER.info("MEAN SHIFT ALGORITHM FINISHED AND START SEGMENTATION");
	}

}
