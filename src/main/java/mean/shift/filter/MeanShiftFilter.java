package mean.shift.filter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javafx.concurrent.Task;
import javafx.geometry.Point3D;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import mean.shift.kernel.Kernel;
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
		LuvPixel[] outImage = new LuvPixel[luv.length];

		int width = pixels.length;
		int height = pixels[0].length;

		double shift = 0;
		int iters = 0;
		int hrad = spatialPar;
		int hcolor = rangePar;
		int pixelNumber = luv.length;
		updateProgress(0, pixelNumber);

		// dla kazdego piksela
		for (int i = 0; i < pixelNumber; i++) {

			// pobierz aktualna pozycje piksela
			int xWindowCenterPosition = (int) luv[i].getPosition().getX();
			int yWindowCenterPosition = (int) luv[i].getPosition().getY();
			// miejsce na stare dane
			int xWindowCenterPositionOld, yWindowCenterPositionOld;
			float oldPointColorL, oldPointColorU, oldPointColorV;
			// aktualna poyzcja i kolor
			Point3D color = luv[i].getColor();
			float pointColorL = (float) color.getX();
			float pointColorU = (float) color.getY();
			float pointColorV = (float) color.getZ();
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
				double pointNum = 0.0, colorNum = 0.0;
				// MEAN SHIFT (17)
				for (int ry = -hrad; ry <= hrad; ry++) {
					int y2 = yWindowCenterPosition + ry;
					if (y2 >= 0 && y2 < height) {
						for (int rx = -hrad; rx <= hrad; rx++) {
							int x2 = xWindowCenterPosition + rx;
							if (x2 >= 0 && x2 < width) {
								double pointDistance = metrics.getDistance(ry, rx);
								if (pointDistance <= hrad) {
									color = luv[y2 * width + x2].getColor();

									float L2 = (float) color.getX();
									float U2 = (float) color.getY();
									float V2 = (float) color.getZ();

									double dL = pointColorL - L2;
									double dU = pointColorU - U2;
									double dV = pointColorV - V2;

									double colorDistance = metrics.getDistance(dL, dU, dV);
									if (colorDistance <= hcolor) {
										double pointKernelWeight = kernel.gFunction(Math.pow(pointDistance / hrad, 2));
										windowShiftX += x2 * pointKernelWeight;
										windowShiftY += y2 * pointKernelWeight;
										pointNum += pointKernelWeight;
										double colorKernelWeight = kernel.gFunction(Math.pow(colorDistance / hcolor, 2));
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

			outImage[i] = new LuvPixel(luv[i].getPosition(), new Point3D(pointColorL, pointColorU, pointColorV));
			updateProgress(i, pixelNumber);

		}

		// Segmentacja
		List<HashSet<LuvPixel>> clusters = new ArrayList<>();

		for (int i = 0; i < pixelNumber; i++) {
			int xPixel = (int) outImage[i].getPosition().getX();
			int yPixel = (int) outImage[i].getPosition().getY();
			HashSet<LuvPixel> actualCluster;
			actualCluster = null;
			Point3D actualPixelColor = outImage[i].getColor();

			for (HashSet<LuvPixel> cluster : clusters) {
				if (cluster.contains(outImage[i])) {
					actualCluster = cluster;
					break;
				}
			}
			if (actualCluster == null) {
				HashSet<LuvPixel> cluster = new HashSet<LuvPixel>();
				cluster.add(outImage[i]);
				clusters.add(cluster);
				actualCluster = cluster;
			}

			for (int yDistance = -hrad; yDistance <= hrad; yDistance++) {
				int pixelPositionYInWindow = countCheckedPixelPosition(yPixel, yDistance);
				if (pixelInImage(pixelPositionYInWindow, height)) {
					for (int xDistance = -hrad; xDistance <= hrad; xDistance++) {
						int pixelPositionXInWindow = countCheckedPixelPosition(xPixel, xDistance);
						if (pixelInImage(pixelPositionXInWindow, width)) {
							if (pixelInSpatialDistance(xDistance, yDistance, hrad)) {
								int pixelIndex = pixelPositionYInWindow * width + pixelPositionXInWindow;
								Point3D color = outImage[pixelIndex].getColor();
								if (pixelInColorDistance(actualPixelColor, color, hcolor))
									actualCluster.add(outImage[pixelIndex]);
							}
						}

					}
				}
			}
		}

		// Sprawdzanie iloœci elementów w klastrach
		deletePixelsFromSmallClusters(clusters);
		// Koniec segmentacji
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

	int countCheckedPixelPosition(int actualPixelOneDimensionPosition, int distanceFromActualPixel) {
		int pixelPosition = actualPixelOneDimensionPosition + distanceFromActualPixel;
		return pixelPosition;
	}

	boolean pixelInImage(int pixelOneDimensionPosition, int range) {
		return (pixelOneDimensionPosition >= 0 && pixelOneDimensionPosition < range);
	}

	boolean pixelInSpatialDistance(int xPixelPostion, int yPixelPosition, int distance) {
		double pointDistance = metrics.getDistance(xPixelPostion, yPixelPosition);
		return (pointDistance <= distance);
	}

	boolean pixelInColorDistance(Point3D actualColor, Point3D secondColor, int range) {
		double L2 = secondColor.getX();
		double U2 = secondColor.getY();
		double V2 = secondColor.getZ();

		double lPixel = actualColor.getX();
		double uPixel = actualColor.getY();
		double vPixel = actualColor.getZ();

		double dL = lPixel - L2;
		double dU = uPixel - U2;
		double dV = vPixel - V2;

		double colorDistance = metrics.getDistance(dL, dU, dV);
		return (colorDistance <= range);
	}

	void deletePixelsFromSmallClusters(List<HashSet<LuvPixel>> clusters) {
		for (HashSet<LuvPixel> cluster : clusters) {
			if (cluster.size() < 20) {
				for (LuvPixel outPixel : cluster) {
					outPixel.setColor(new Point3D(100, 0.0008906695967064726, -0.01710795288980549));
				}
			}
		}
	}

}
