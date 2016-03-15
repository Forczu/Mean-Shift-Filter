package mean.shift.filter;

import javafx.scene.image.Image;
import mean.shift.kernel.Kernel;
import mean.shift.processing.LuvPixel;
import mean.shift.processing.Metrics;

public class MeanShiftFilterTask extends MeanShiftTask {

	public MeanShiftFilterTask(Image image, Kernel kernel, int spatialPar, int rangePar, int maxIters, int minShift,
			Metrics metrics, int width) {
		super(image, kernel, spatialPar, rangePar, maxIters, minShift, metrics, width);
	}

	@Override
	protected LuvPixel[] process(int[][] pixels, LuvPixel[] luvInputImage) {
		return meanShiftFiltration(pixels, luvInputImage);
	}

}
