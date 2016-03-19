package mean.shift.kernel;
import java.lang.Math;

public class GaussianKernel implements Kernel{

	public static Kernel getInstance() {
		return new GaussianKernel();
	}

	public static String getName() {
		return "Gaussa";
	}

	@Override
	public float calculate(float distance, int range) {
		// sigma = 1
		float x = distance/range;
		return (float) (1.0f / Math.sqrt(2 * Math.PI) * profile(x*x));
	}

	@Override
	public float profile(float x) {
		return (float)Math.exp(-0.5f * x);
	}

	@Override
	public float gFunction(float distance, int range) {
		float x = distance/range;
		// negatywna pochodna profilu
		return 0.5f * (float)Math.exp(-0.5 * x*x);
	}

}
