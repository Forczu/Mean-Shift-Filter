package mean.shift.kernel;
import java.lang.Math;

public class RectangularKernel implements Kernel {

	public static Kernel getInstance() {
		return new RectangularKernel();
	}
	
	public static String getName() {
		return "Prostokatny";
	}

	@Override
	public float calculate(float distance, int range) {
		float x = (float)Math.pow(distance/range, 2);
		return (x < 0.5 && x > -0.5) ? 1 : 0;
	}

	@Override
	public float gFunction(float distance, int range) {
		return calculate(distance,range);
	}
}
