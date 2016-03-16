package mean.shift.processing;

import java.util.List;

public class ConfigurationProfile {

	private String path;
	private List<String> images;
	private String kernel;
	private String metrics;
	private int spatialPar;
	private int rangePar;
	private int maxIters;
	private int minShift;

	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public List<String> getImages() {
		return images;
	}
	public void setImages(List<String> images) {
		this.images = images;
	}
	public String getKernel() {
		return kernel;
	}
	public void setKernel(String kernel) {
		this.kernel = kernel;
	}
	public String getMetrics() {
		return metrics;
	}
	public void setMetrics(String metrics) {
		this.metrics = metrics;
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



}
