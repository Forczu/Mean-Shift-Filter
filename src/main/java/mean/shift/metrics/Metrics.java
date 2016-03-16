package mean.shift.metrics;

public interface Metrics {

	/**
	 * Zwraca dystans miedzy punktem zrodlowym, a punktem
	 * w n-wymiarowej przestrzeni
	 *
	 * @param points punkty
	 * @return dystans
	 */
	float getDistance(float... points);

}