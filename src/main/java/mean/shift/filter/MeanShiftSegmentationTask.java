package mean.shift.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mean.shift.pixel.Pixel;
import mean.shift.processing.MeanShiftParameter;
import mean.shift.thread.BaseThread;
import mean.shift.thread.MeanShiftThread;

public class MeanShiftSegmentationTask extends MeanShiftTask {

	public MeanShiftSegmentationTask(MeanShiftParameter parameter) {
		super(parameter);
	}

	@Override
	public Pixel[] process(int[][] pixels, Pixel[] luvInputImage) {
		Pixel[] luvOutputImage = new Pixel[luvInputImage.length];

		int threadsCount = Runtime.getRuntime().availableProcessors();
		int segmentSize = luvInputImage.length / threadsCount;
		BaseThread[] threads = new BaseThread[threadsCount];

		for (int i = 0; i < threadsCount; i++) {
			int start = i * segmentSize;
			int end = start + segmentSize;

			// Jesli ilosc pikseli LUV nie jest podzielna przez ilosc rdzeni,
			// ostatni bedzie murzynem
			if (i == threadsCount - 1)
				end += luvInputImage.length % threadsCount;

			threads[i] = new MeanShiftThread(pixels, luvInputImage, luvOutputImage, this, start, end);
		}

		try {
			for (Thread thread : threads) {
				thread.start();
			}
			for (Thread thread : threads) {
				thread.join();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		updateTitle("Trwa segmentacja...");
		algorithmProgress = 0;

		segmentationAlgorithm(pixels, (Pixel[])luvInputImage, (Pixel[])luvOutputImage);

		LOGGER.info("SEGMENTATION FINISHED");

		return luvOutputImage;
	}

	/**
	 * Segmentacja obrazu wg algorytmu MS.
	 *
	 * @param pixels
	 *            wejsciowe piksele RGB
	 * @param luvInputImage
	 *            wejsciowe piksele LUV
	 * @param luvOutputImage
	 *            wyjsciowe piksele LUV
	 */
	public void segmentationAlgorithm(int[][] pixels, Pixel[] luvInputImage, Pixel[] luvOutputImage) {

		int width = pixels.length;
		int height = pixels[0].length;
		int pixelRange = 3;
		int colorRange = 3;
		int pixelNumber = luvInputImage.length;
		int clusterCount = 0;
		List<HashSet<Pixel>> clusters = new ArrayList<>();
		updateProgress(algorithmProgress++, pixelNumber);
		int[] assigned = new int[pixelNumber];
		Arrays.fill(assigned, -1);
		LOGGER.debug("Start");
		for (int i = 0; i < pixelNumber; i++) {
			LOGGER.debug("Petla " + i);
			LOGGER.debug("ASSIGNED = " + assigned[i]);
			Set<Integer> pixelIndexsToDBScanFunction = new HashSet<>();
			int xPixel = (int) luvOutputImage[i].getPos().x();
			int yPixel = (int) luvOutputImage[i].getPos().y();
			HashSet<Pixel> actualCluster;
			actualCluster = null;
			float[] actualPixelColor = luvOutputImage[i].getColorVector();
			if (assigned[i] < 0) {
				HashSet<Pixel> cluster = new HashSet<Pixel>();
				cluster.add(luvOutputImage[i]);
				clusters.add(cluster);
				actualCluster = cluster;
				assigned[i] = clusterCount++;

				for (int yDistance = -pixelRange; yDistance <= pixelRange; ++yDistance) {
					int pixelPositionYInWindow = countCheckedPixelPosition(yPixel, yDistance);
					if (pixelInImage(pixelPositionYInWindow, height)) {
						for (int xDistance = -pixelRange; xDistance <= pixelRange; ++xDistance) {
							int pixelPositionXInWindow = countCheckedPixelPosition(xPixel, xDistance);
							if (pixelInImage(pixelPositionXInWindow, width)) {
								if (pixelInSpatialDistance(xDistance, yDistance, pixelRange)) {
									int pixelIndex = pixelPositionYInWindow * width + pixelPositionXInWindow;
									float[] color = luvOutputImage[pixelIndex].getColorVector();

									if (pixelInColorDistance(actualPixelColor, color, colorRange)) {
										if (assigned[pixelIndex] < 0) {
											actualCluster.add(luvOutputImage[pixelIndex]);
											assigned[pixelIndex] = assigned[i];
											pixelIndexsToDBScanFunction.add(pixelIndex);
										}
									}
								}
							}

						}
					}
				}
				updateProgress(algorithmProgress++, pixelNumber);
				updateMessage(stopWatch.getFormattedTime());
				try {
					HashSet<Integer> pixelIndexs = new HashSet<>();
					do {
						pixelIndexs.clear();
						for (Integer pixelIndex : pixelIndexsToDBScanFunction) {
							LOGGER.debug("Pixels: " + pixelIndex);
							pixelIndexs.addAll(dbscanClusteringFunction(pixelIndex, height, width, pixelRange,
									colorRange, luvOutputImage, actualCluster, assigned, pixelNumber));
						}
						pixelIndexsToDBScanFunction.clear();
						pixelIndexsToDBScanFunction.addAll(pixelIndexs);
					} while (!pixelIndexs.isEmpty());
				} catch (Exception e) {
					LOGGER.debug("EXCEPTION: " + e.toString());
				}
			}


		}
		coloringPixelsInClusters(clusters);
	}

	private HashSet<Integer> dbscanClusteringFunction(int actualPixelIndex, int height, int width, int pixelRange,
			int colorRange, Pixel[] luvOutputImage, HashSet<Pixel> actualCluster, int[] assigned, int pixelNumber) {
		HashSet<Integer> pixelIndexsToDBScanFunction = new HashSet<>();
		int xPixel = (int) luvOutputImage[actualPixelIndex].getPos().x();
		int yPixel = (int) luvOutputImage[actualPixelIndex].getPos().y();
		float[] actualPixelColor = luvOutputImage[actualPixelIndex].getColorVector();
		for (int yDistance = -pixelRange; yDistance <= pixelRange; ++yDistance) {
			int pixelPositionYInWindow = countCheckedPixelPosition(yPixel, yDistance);
			if (pixelInImage(pixelPositionYInWindow, height)) {
				for (int xDistance = -pixelRange; xDistance <= pixelRange; ++xDistance) {
					int pixelPositionXInWindow = countCheckedPixelPosition(xPixel, xDistance);
					if (pixelInImage(pixelPositionXInWindow, width)) {
						if (pixelInSpatialDistance(xDistance, yDistance, pixelRange)) {
							int pixelIndex = pixelPositionYInWindow * width + pixelPositionXInWindow;
							float[] color = luvOutputImage[pixelIndex].getColorVector();

							if (pixelInColorDistance(actualPixelColor, color, colorRange)) {
								if (assigned[pixelIndex] < 0) {
									actualCluster.add(luvOutputImage[pixelIndex]);
									assigned[pixelIndex] = assigned[actualPixelIndex];
									pixelIndexsToDBScanFunction.add(pixelIndex);
									LOGGER.debug("PIXELS: " + pixelIndex);
								}
							}

						}
					}

				}
			}
		}
		updateProgress(algorithmProgress++, pixelNumber);
		updateMessage(stopWatch.getFormattedTime());
		return pixelIndexsToDBScanFunction;
	}

	private void coloringPixelsInClusters(List<HashSet<Pixel>> clusters) {
		float[] color;
		for (HashSet<Pixel> cluster : clusters) {
			color = cluster.iterator().next().getColorVector();
			for (Pixel luvPixel : cluster) {
				luvPixel.setColorVector(color);
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

	private boolean pixelInColorDistance(float[] actualColor, float[] secondColor, int range) {

		float[] dColor = new float[channels];
		for (int i = 0; i < dColor.length; i++) {
			dColor[i] = actualColor[i] - secondColor[i];
		}

		return metrics.isWithinDistance(range, dColor);
	}

}
