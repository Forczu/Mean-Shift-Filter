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

	/**
	 * Sprawdza czy odleglosc miedzy punktem zrodlowym, a docelowym
	 * jest mniejsza lub rowna zadanej.
	 * Moze byc stosowane w celu zmniejszenia liczby obliczen.
	 * @param distance
	 * @param points
	 * @return
	 */
	boolean isWithinDistance(float distance, float ... points);

}