package mean.shift.kernel;

public class BiweightKernel implements Kernel {

	public static Kernel getInstance() {
		return new BiweightKernel();
	}

	public static String getName() {
		return "Biweight";
	}

	@Override
	public float profile(float x) {
		return Math.abs(x) <= 1 ? (float)Math.pow(1.0f - x, 2) : 0;
	}

	@Override
	public float gFunction(float distance, int range) {
		float x = distance / range;
		return Math.abs(x) <= 1 ? 2.0f * (1.0f - x) : 0;
	}

	@Override
	public float calculate(float distance, int range) {
		// TODO Auto-generated method stub
		return 0;
	}

}
