package mean.shift.filter;

import javafx.scene.image.Image;
import mean.shift.kernel.Kernel;
import mean.shift.processing.MeanShiftParameter;

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

	public MeanShiftTask createFilterWorker(MeanShiftParameter parameter) {
		return new MeanShiftFilterTask(parameter);
	}

	public MeanShiftTask createSegmentationWorker(MeanShiftParameter parameter) {
		return new MeanShiftSegmentationTask(parameter);
	}

}

