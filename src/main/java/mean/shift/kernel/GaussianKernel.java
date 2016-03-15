package mean.shift.kernel;
import java.lang.Math;

public class GaussianKernel implements Kernel{
	
	public static Kernel getInstance() {
		return new GaussianKernel();
	}

	@Override
	public float calculate(float distance, int range) {
		return (float)Math.exp(-0.5*Math.pow(distance/range, 2));
	}

	@Override
	public float gFunction(float distance, int range) {
		return calculate(distance, range);
	}

}
