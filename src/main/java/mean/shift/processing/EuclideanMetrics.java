package mean.shift.processing;

public class EuclideanMetrics {

	public double getDistance(double ... points) {
		if (points.length == 1)
			return 0;
		double sum = 0;
		for (int i = 0; i < points.length; i++) {
			sum += points[i] * points[i];
		}
		return Math.sqrt(sum);
	}

}
