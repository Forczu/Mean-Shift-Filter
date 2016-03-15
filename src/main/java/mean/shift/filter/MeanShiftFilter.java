package mean.shift.filter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import mean.shift.kernel.Kernel;
import mean.shift.processing.Color;
import mean.shift.processing.ColorProcesser;
import mean.shift.processing.LuvPixel;
import mean.shift.processing.Metrics;

/**
 * Glowna klasa algorytmu mean shift
 *
 * @author Forczu
 */
public class MeanShiftFilter extends Task<Image> {

	protected Image image;
	protected Kernel kernel;
	protected int spatialPar;
	protected int rangePar;
	protected int maxIters;
	protected int minShift;
	protected Metrics metrics;

	public MeanShiftFilter(Image image, Kernel kernel, int spatialPar, int rangePar, int maxIters, int minShift,
			Metrics metrics) {
		this.image = image;
		this.kernel = kernel;
		this.spatialPar = spatialPar;
		this.rangePar = rangePar;
		this.maxIters = maxIters;
		this.minShift = minShift;
		this.metrics = metrics;
	}

	@Override
	protected Image call() {


		ColorProcesser colorProcesser = new ColorProcesser();
		int[][] pixels = colorProcesser.getPixelArray(image);
		LuvPixel[] luv = colorProcesser.getLuvArray(pixels);
		LuvPixel[] outImage = meanShiftAlgorithm(pixels, luv);

		segmentationAlgorithm(pixels,luv,outImage);
		Image filteredImage = convertPixelToImage(pixels,colorProcesser,outImage);	
		return filteredImage;
	}


	private Image convertPixelToImage(int[][] pixels,ColorProcesser colorProcesser,LuvPixel[] outImage) {
		int width = pixels.length;
		int height = pixels[0].length;
		int[][] rgb = colorProcesser.getRgbArray(outImage, width);
		WritableImage filteredImage = new WritableImage(width, height);
		PixelWriter pixelWriter = filteredImage.getPixelWriter();
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				pixelWriter.setArgb(i, j, rgb[i][j]);
			}
		}
		return filteredImage;
	}

	LuvPixel[] meanShiftAlgorithm(int[][] pixels,LuvPixel[] luv){
		LuvPixel[] outImage = new LuvPixel[luv.length];
		int width = pixels.length;
		int height = pixels[0].length;

		float shift = 0;
		int iters = 0;
		int hrad = spatialPar;
		int hcolor = rangePar;
		int pixelNumber = luv.length;
		updateProgress(0, pixelNumber);

		int threadsCount = Runtime.getRuntime().availableProcessors();
		// TODO: przekazac tablicy pikseli do kazdego watku

		// dla kazdego piksela
		for (int i = 0; i < pixelNumber; i++) {

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

			outImage[i] = new LuvPixel(luv[i].getPos(), Color.getInstance(pointColorL, pointColorU, pointColorV));
			updateProgress(i, pixelNumber);

		}
		return outImage;
	}

	void segmentationAlgorithm(int[][] pixels, LuvPixel[] luvInputImage, LuvPixel[] luvOutputImage) {
		int width = pixels.length;
		int height = pixels[0].length;
		int pixelRange = 3;
		int colorRange = 3;
		int pixelNumber = luvInputImage.length;
		List<HashSet<LuvPixel>> clusters = new ArrayList<>();

		for (int i = 0; i < pixelNumber; i++) {
			int xPixel = (int) luvOutputImage[i].getPos().x();
			int yPixel = (int) luvOutputImage[i].getPos().y();
			HashSet<LuvPixel> actualCluster;
			actualCluster = null;
			Color actualPixelColor = luvOutputImage[i].getColor();

			for (HashSet<LuvPixel> cluster : clusters) {
				if (cluster.contains(luvOutputImage[i])) {
					actualCluster = cluster;
					break;
				}
			}
			if (actualCluster == null) {
				HashSet<LuvPixel> cluster = new HashSet<LuvPixel>();
				cluster.add(luvOutputImage[i]);
				clusters.add(cluster);
				actualCluster = cluster;
			}

			for (int yDistance = -pixelRange; yDistance <= pixelRange; yDistance++) {
				int pixelPositionYInWindow = countCheckedPixelPosition(yPixel, yDistance);
				if (pixelInImage(pixelPositionYInWindow, height)) {
					for (int xDistance = -pixelRange; xDistance <= pixelRange; xDistance++) {
						int pixelPositionXInWindow = countCheckedPixelPosition(xPixel, xDistance);
						if (pixelInImage(pixelPositionXInWindow, width)) {
							if (pixelInSpatialDistance(xDistance, yDistance, pixelRange)) {
								int pixelIndex = pixelPositionYInWindow * width + pixelPositionXInWindow;
								Color color = luvOutputImage[pixelIndex].getColor();
								if (pixelInColorDistance(actualPixelColor, color, colorRange))
									actualCluster.add(luvOutputImage[pixelIndex]);
							}
						}

					}
				}
			}
		}
		coloringPixelsInClusters(clusters);
		//deletePixelsFromSmallClusters(clusters);
	}

	private void coloringPixelsInClusters(List<HashSet<LuvPixel>> clusters) {
		Color color;
		for(HashSet<LuvPixel> cluster:clusters){
			color = null;
			for(LuvPixel luvPixel: cluster){
				if(color == null){
					color = luvPixel.getColor();
				}
				luvPixel.setColor(color);
			}
		}

	}

	private int countCheckedPixelPosition(int actualPixelOneDimensionPosition, int distanceFromActualPixel) {
		int pixelPosition = actualPixelOneDimensionPosition + distanceFromActualPixel;
		return pixelPosition;
	}

	private boolean pixelInImage(int pixelOneDimensionPosition, int range) {
		return (pixelOneDimensionPosition >= 0 && pixelOneDimensionPosition < range);
	}

	private boolean pixelInSpatialDistance(int xPixelPostion, int yPixelPosition, int distance) {
		float pointDistance = metrics.getDistance(xPixelPostion, yPixelPosition);
		return (pointDistance <= distance);
	}

	private boolean pixelInColorDistance(Color actualColor, Color secondColor, int range) {
		float L2 = secondColor.l();
		float U2 = secondColor.u();
		float V2 = secondColor.v();

		float lPixel = actualColor.l();
		float uPixel = actualColor.u();
		float vPixel = actualColor.v();

		float dL = lPixel - L2;
		float dU = uPixel - U2;
		float dV = vPixel - V2;

		float colorDistance = metrics.getDistance(dL, dU, dV);
		return (colorDistance <= range);
	}

	private void deletePixelsFromSmallClusters(List<HashSet<LuvPixel>> clusters) {
		for (HashSet<LuvPixel> cluster : clusters) {
			if (cluster.size() < 20) {
				for (LuvPixel outPixel : cluster) {
					outPixel.setColor(Color.getInstance(100, 0.0008906695967064726f, -0.01710795288980549f));
				}
			}
		}
	}

}
