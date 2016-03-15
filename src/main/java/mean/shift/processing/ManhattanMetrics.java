package mean.shift.processing;

public class ManhattanMetrics implements Metrics {

	public static Metrics getInstance() {
		return new ManhattanMetrics();
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

}
