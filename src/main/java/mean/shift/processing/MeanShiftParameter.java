package mean.shift.processing;

import javafx.scene.image.Image;
import mean.shift.kernel.Kernel;
import mean.shift.metrics.Metrics;

public class MeanShiftParameter {

	private Image image;
	private Kernel kernel;
	private int spatialPar;
	private int rangePar;
	private int maxIters;
	private int minShift;
	private Metrics metrics;

	public MeanShiftParameter() {
	}


	public MeanShiftParameter(Image image, Kernel kernel, int spatialPar, int rangePar, int maxIters, int minShift,
			Metrics metrics) {
		this.image = image;
		this.kernel = kernel;
		this.spatialPar = spatialPar;
		this.rangePar = rangePar;
		this.maxIters = maxIters;
		this.minShift = minShift;
		this.metrics = metrics;
	}

	public Image getImage() {
		return image;
	}
	public void setImage(Image image) {
		this.image = image;
	}
	public Kernel getKernel() {
		return kernel;
	}
	public void setKernel(Kernel kernel) {
		this.kernel = kernel;
	}
	public int getSpatialPar() {
		return spatialPar;
	}
	public void setSpatialPar(int spatialPar) {
		this.spatialPar = spatialPar;
	}
	public int getRangePar() {
		return rangePar;
	}
	public void setRangePar(int rangePar) {
		this.rangePar = rangePar;
	}
	public int getMaxIters() {
		return maxIters;
	}
	public void setMaxIters(int maxIters) {
		this.maxIters = maxIters;
	}
	public int getMinShift() {
		return minShift;
	}
	public void setMinShift(int minShift) {
		this.minShift = minShift;
	}
	public Metrics getMetrics() {
		return metrics;
	}
	public void setMetrics(Metrics metrics) {
		this.metrics = metrics;
	}



}
