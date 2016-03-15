package mean.shift.kernel;
import java.lang.Math;
public class GaussianKernel implements Kernel{

	@Override
	public double calculate(double distance, int range) {
		// TODO Auto-generated method stub
		return Math.exp(-0.5*Math.pow(distance/range, 2));
	}

	@Override
	public double gFunction(double distance, int range) {
		// TODO Auto-generated method stub
		return calculate(distance, range);
	}

}
