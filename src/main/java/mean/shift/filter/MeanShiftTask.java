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

		int iters = 0;
		int hrad = spatialPar;
		int hcolor = rangePar;
		int pixelNumber = luv.length;
		updateProgress(algorithmProgress++, pixelNumber);

		float[] argVector = new float[channels + 2];

		// dla kazdego piksela
		for (int i = start; i < end; i++) {

			// pobierz aktualna pozycje piksela
			int xWindowCenterPosition = (int) luv[i].getPos().x();
			int yWindowCenterPosition = (int) luv[i].getPos().y();
			// miejsce na stare dane
			int xWindowCenterPositionOld, yWindowCenterPositionOld;
			float[] oldPointColor;
			// aktualna poyzcja i kolor
			float[] pointColor = luv[i].getColorVector();
			// licznik iteracji
			iters = 0;
			// mean-shiftowanie
			do {
				// zachowanie starych danych
				xWindowCenterPositionOld = xWindowCenterPosition;
				yWindowCenterPositionOld = yWindowCenterPosition;
				oldPointColor = pointColor;
				// wartosci przesuniecia
				float windowShiftX = 0, windowShiftY = 0;
				float[] pointColorShift = new float[channels];
				float pointNum = 0, colorNum = 0;
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

									float[] dColor = new float[channels];
									for (int j = 0; j < dColor.length; j++) {
										dColor[j] = pointColor[j] - pointColor2[j];
									}

									float colorDistance = metrics.getDistance(dColor);
									if (colorDistance <= hcolor) {
										float pointKernelWeight = kernel.gFunction(pointDistance, hrad);
										windowShiftX += x2 * pointKernelWeight;
										windowShiftY += y2 * pointKernelWeight;
										pointNum += pointKernelWeight;
										float colorKernelWeight = kernel.gFunction(colorDistance, hcolor);
										for (int j = 0; j < pointColorShift.length; j++) {
											pointColorShift[j] += pointColor2[j] * colorKernelWeight;
										}
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
				for (int j = 0; j < pointColor.length; j++) {
					pointColor[j] = (float)(pointColorShift[j] * (1.0f / colorNum));
				}
				// mean-shift
				argVector[0] = xWindowCenterPosition - xWindowCenterPositionOld;
				argVector[1] = yWindowCenterPosition - yWindowCenterPositionOld;
				for (int j = 0; j < pointColor.length; j++) {
					argVector[j + 2] = pointColor[j] - oldPointColor[j];
				}
				iters++;
			} while (metrics.isWithinDistance(minShift, argVector) && iters < maxIters);

			outImageLuv[i] = new Pixel(luv[i].getPos(), pointColor);
			updateProgress(algorithmProgress++, pixelNumber);
			updateMessage(stopWatch.getFormattedTime());

		}
		LOGGER.info("MEAN SHIFT ALGORITHM FINISHED AND START SEGMENTATION");
	}

}
