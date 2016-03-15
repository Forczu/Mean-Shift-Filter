package mean.shift.kernel;
import java.lang.Math;
public class RectangularKernel implements Kernel {

	@Override
	public double calculate(double distance, int range) {
		double x = Math.pow(distance/range, 2);
		return (x < 0.5 && x > -0.5) ? 1 : 0;
	}

	@Override
	public double gFunction(double distance, int range) {
		return calculate(distance,range);
	}
}
