package mean.shift.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import mean.shift.kernel.Kernel;
import mean.shift.metrics.Metrics;
import mean.shift.processing.Color;
import mean.shift.processing.ColorProcesser;
import mean.shift.processing.LuvPixel;
import mean.shift.processing.MeanShiftParameter;
import mean.shift.utils.StopWatch;

public abstract class MeanShiftTask extends Task<Image> {

	protected static Logger LOGGER = LoggerFactory.getLogger(MeanShiftTask.class);

	protected StopWatch stopWatch = new StopWatch();

	protected Image image;
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
		LuvPixel[] filtered = filter();
		updateTitle("Zakonczono przetwarzanie");
		return convertPixelToImage(filtered);
	}

	/**
	 * Konwersja tablicy pikseli luv na rysunek.
	 * @param luv
	 * @return
	 */
	protected Image convertPixelToImage(LuvPixel[] luv) {
		int width = (int)image.getWidth();
		int height = luv.length / width;
		int[][] rgb = colorProcesser.getRgbArray(luv, width);
		WritableImage filteredImage = new WritableImage(width, height);
		PixelWriter pixelWriter = filteredImage.getPixelWriter();
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				pixelWriter.setArgb(i, j, rgb[i][j]);
			}
		}
		return filteredImage;
	}

	/**
	 * Metoda szablonowa dla przetwarzania pikseli.
	 * @return
	 */
	protected abstract LuvPixel[] process(int[][] pixels, LuvPixel[] luvInputImage);

	/**
	 * Wydzielenie pikseli luv i przefiltrowanie ich
	 * wg odpowiedniej metody.
	 * @return
	 */
	public LuvPixel[] filter() {
        stopWatch.start();
		algorithmProgress = 0;
		updateMessage(stopWatch.getFormattedTime());
		updateTitle("Trwa filtrowanie...");

		LOGGER.info("START MEAN SHIFT PROCESSING");
		int[][] pixels = colorProcesser.getPixelArray(image);
		LuvPixel[] luv = colorProcesser.getLuvArray(pixels);
		LuvPixel[] processedPixels = process(pixels, luv);

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
	public void meanShiftFiltration(int[][] pixels, LuvPixel[] luv, LuvPixel[] outImageLuv, int start, int end) {

		LOGGER.info("START MEAN SHIFT ALGORITHM");

		//LuvPixel[] outImage = new LuvPixel[luv.length];
		int width = pixels.length;
		int height = pixels[0].length;

		float shift = 0;
		int iters = 0;
		int hrad = spatialPar;
		int hcolor = rangePar;
		int pixelNumber = luv.length;
		updateProgress(algorithmProgress++, pixelNumber);

		// dla kazdego piksela
		for (int i = start; i < end; i++) {

			// pobierz aktualna pozycje piksela
			int xWindowCenterPosition = (int) luv[i].getPos().x();
			int yWindowCenterPosition = (int) luv[i].getPos().y();
			// miejsce na stare dane
			int xWindowCenterPositionOld, yWindowCenterPositionOld;
			float oldPointColorL, oldPointColorU, oldPointColorV;
			// aktualna poyzcja i kolor
			Color color = luv[i].getColor();
			float pointColorL = color.l();
			float pointColorU = color.u();
			float pointColorV = color.v();
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
				float windowShiftX = 0, windowShiftY = 0, pointColorShiftL = 0, pointColorShiftU = 0, pointColorShiftV = 0;
				float pointNum = 0.0f, colorNum = 0.0f;
				// MEAN SHIFT (17)
				for (int ry = -hrad; ry <= hrad; ry++) {
					int y2 = yWindowCenterPosition + ry;
					if (y2 >= 0 && y2 < height) {
						for (int rx = -hrad; rx <= hrad; rx++) {
							int x2 = xWindowCenterPosition + rx;
							if (x2 >= 0 && x2 < width) {
								float pointDistance = metrics.getDistance(ry, rx);
								if (pointDistance <= hrad) {
									color = luv[y2 * width + x2].getColor();

									float L2 = color.l();
									float U2 = color.u();
									float V2 = color.v();

									float dL = pointColorL - L2;
									float dU = pointColorU - U2;
									float dV = pointColorV - V2;

									float colorDistance = metrics.getDistance(dL, dU, dV);
									if (colorDistance <= hcolor) {
										float pointKernelWeight = kernel.gFunction(pointDistance, hrad);
										windowShiftX += x2 * pointKernelWeight;
										windowShiftY += y2 * pointKernelWeight;
										pointNum += pointKernelWeight;
										float colorKernelWeight = kernel.gFunction(colorDistance, hcolor);
										pointColorShiftL += L2 * colorKernelWeight;
										pointColorShiftU += U2 * colorKernelWeight;
										pointColorShiftV += V2 * colorKernelWeight;
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
				pointColorL = (float) (pointColorShiftL * (1.0 / colorNum));
				pointColorU = (float) (pointColorShiftU * (1.0 / colorNum));
				pointColorV = (float) (pointColorShiftV * (1.0 / colorNum));
				// mean-shift
				int dx = xWindowCenterPosition - xWindowCenterPositionOld;
				int dy = yWindowCenterPosition - yWindowCenterPositionOld;
				float dL = pointColorL - oldPointColorL;
				float dU = pointColorU - oldPointColorU;
				float dV = pointColorV - oldPointColorV;

				shift = metrics.getDistance(dx, dy, dL, dU, dV);
				iters++;
			} while (shift > minShift && iters < maxIters);

			outImageLuv[i] = new LuvPixel(luv[i].getPos(), Color.getInstance(pointColorL, pointColorU, pointColorV));
			updateProgress(algorithmProgress++, pixelNumber);
			updateMessage(stopWatch.getFormattedTime());

		}
		LOGGER.info("MEAN SHIFT ALGORITHM FINISHED AND START SEGMENTATION");
	}

}
