package mean.shift.processing;

public class EuclideanMetrics implements Metrics {

	public static Metrics getInstance() {
		return new EuclideanMetrics();
	}

	@Override
	public float getDistance(float ... points) {
		if (points.length == 1)
			return 0;
		double sum = 0;
		for (int i = 0; i < points.length; i++) {
			sum += points[i] * points[i];
		}
		return (float)Math.sqrt(sum);
	}

}
