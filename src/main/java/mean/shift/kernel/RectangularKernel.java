package mean.shift.kernel;

public class RectangularKernel implements Kernel {

	@Override
	public double calculate(double x) {
		return (x < 0.5 && x > -0.5) ? 1 : 0;
	}

	@Override
	public double gFunction(double x) {
		return calculate(x);
	}
}
