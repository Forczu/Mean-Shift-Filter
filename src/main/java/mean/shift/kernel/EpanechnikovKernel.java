package mean.shift.kernel;
import java.lang.Math;

public class EpanechnikovKernel implements Kernel {

	public static Kernel getInstance() {
		return new EpanechnikovKernel();
	}

	public static String getName() {
		return "Epanechnikova";
	}

	@Override
	public float calculate(float distance, int range) {
		float x = distance / range;
		return (x >= -1 || x <= 1 ? 0.75f * profile(x*x) : 0);
	}

	@Override
	public float profile(float x) {
		float abs = x > 0.0f ? x : -x;
		if (abs >= 0 && abs <= 1)
			return 1.0f - x;
		else
			return 0;
	}

	@Override
	public float gFunction(float distance, int range) {
		// negatywna pochodna profilu
		return 1f;
	}

}
