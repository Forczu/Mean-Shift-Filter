package mean.shift.kernel;

public class RectangularKernel {

	public double calculate(double x) {
		return (x < 0.5 && x > -0.5) ? 1 : 0;
	}

	public double gFunction(double x) {
		return calculate(x);
	}
}
