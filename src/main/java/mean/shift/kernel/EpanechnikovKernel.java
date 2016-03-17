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
		float x = (float)(distance/range);
		return (Math.abs(x) >= 1) ? 0 : 0.75f*(1 - (float)Math.pow(x, 2));
	}

	@Override
	public float gFunction(float distance, int range) {
		return calculate(distance, range);
	}

}
