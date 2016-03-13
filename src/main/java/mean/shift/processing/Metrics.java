package mean.shift.processing;

public interface Metrics {

	/**
	 * Zwraca dystans miedzy punktem zrodlowym, a punktem
	 * w n-wymiarowej przestrzeni
	 *
	 * @param points punkty
	 * @return dystans
	 */
	double getDistance(double... points);

}