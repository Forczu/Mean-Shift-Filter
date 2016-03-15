package mean.shift.filter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javafx.scene.image.Image;
import mean.shift.kernel.Kernel;
import mean.shift.processing.Color;
import mean.shift.processing.LuvPixel;
import mean.shift.processing.Metrics;

public class MeanShiftSegmentationTask extends MeanShiftTask {

	public MeanShiftSegmentationTask(Image image, Kernel kernel, int spatialPar, int rangePar, int maxIters,
			int minShift, Metrics metrics, int width) {
		super(image, kernel, spatialPar, rangePar, maxIters, minShift, metrics, width);
	}

	@Override
	protected LuvPixel[] process(int[][] pixels, LuvPixel[] luvInputImage) {
		LuvPixel[] outImage = meanShiftFiltration(pixels, luvInputImage);
		updateTitle("Trwa segmentacja...");
		segmentationAlgorithm(pixels, luvInputImage, outImage);
		LOGGER.info("SEGMENTATION FINISHED");
		return outImage;
	}


	/**
	 * Segmentacja obrazu wg algorytmu MS.
	 * @param pixels wejsciowe piksele RGB
	 * @param luvInputImage wejsciowe piksele LUV
	 * @param luvOutputImage wyjsciowe piksele LUV
	 */
	void segmentationAlgorithm(int[][] pixels, LuvPixel[] luvInputImage, LuvPixel[] luvOutputImage) {
		int width = pixels.length;
		int height = pixels[0].length;
		int pixelRange = spatialPar;
		int colorRange = rangePar;
		int pixelNumber = luvInputImage.length;
		List<HashSet<LuvPixel>> clusters = new ArrayList<>();
		updateProgress(0, pixelNumber);

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
			updateProgress(i, pixelNumber);
			updateMessage(String.valueOf(stopWatch.elapsedTime()));
		}
		coloringPixelsInClusters(clusters);
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

}
