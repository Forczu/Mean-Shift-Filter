package mean.shift.filter;

import javafx.scene.image.Image;
import mean.shift.kernel.Kernel;
import mean.shift.processing.Metrics;

/**
 * Glowna klasa algorytmu mean shift
 * (singleton)
 *
 * @author Forczu
 */
public class MeanShift {

	private static MeanShift instance = null;

	protected MeanShift() {
	}

	public static MeanShift getInstance() {
		if (instance == null) {
			instance = new MeanShift();
		}
		return instance;
	}

	public MeanShiftTask createFilterWorker(Image image, Kernel kernel, int spatialPar, int rangePar, int maxIters,
			int minShift, Metrics metrics, int width) {
		return new MeanShiftFilterTask(image, kernel, spatialPar, rangePar, maxIters, minShift, metrics, width);
	}

	public MeanShiftTask createSegmentationWorker(Image image, Kernel kernel, int spatialPar, int rangePar, int maxIters,
			int minShift, Metrics metrics, int width) {
		return new MeanShiftSegmentationTask(image, kernel, spatialPar, rangePar, maxIters, minShift, metrics, width);
	}

}

