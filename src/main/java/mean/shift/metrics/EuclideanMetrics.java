package mean.shift.metrics;

public class EuclideanMetrics implements Metrics {

	public static Metrics getInstance() {
		return new EuclideanMetrics();
	}

	public static String getName() {
		return "Euklidesowa";
	}

	@Override
	public float getDistance(float ... points) {
		if (points.length == 1)
			return Math.abs(points[0]);
		float sum = 0;
		for (int i = 0; i < points.length; i++) {
			sum += points[i] * points[i];
		}
		return Float.intBitsToFloat(((Float.floatToIntBits(sum) - (1 << 52)) >> 1) + (1 << 61));
	}

	@Override
	public boolean isWithinDistance(float distance, float... points) {
		float sum = 0;
		for (float point : points) {
			sum += point * point;
		}
		return sum <= distance * distance;
	}

}
