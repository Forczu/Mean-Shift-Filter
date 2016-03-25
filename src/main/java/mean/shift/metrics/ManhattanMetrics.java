package mean.shift.metrics;

public class ManhattanMetrics implements Metrics {

	public static Metrics getInstance() {
		return new ManhattanMetrics();
	}

	public static String getName() {
		return "Manhattan";
	}

	@Override
	public float getDistance(float... points) {
		if (points.length == 1)
			return 0;
		float sum = 0;
		for (int i = 0; i < points.length; i++) {
			sum += Math.abs(points[i]);
		}
		return sum;
	}

	@Override
	public boolean isWithinDistance(float distance, float... points) {
		float sum = 0;
		for (float point : points) {
			sum += point;
		}
		return sum <= distance;
	}

}
