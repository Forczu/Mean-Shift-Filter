package mean.shift.kernel;
import java.lang.Math;
public class GaussianKernel implements Kernel{

	@Override
	public float calculate(float distance, int range) {
		// TODO Auto-generated method stub
		return (float)Math.exp(-0.5*Math.pow(distance/range, 2));
	}

	@Override
	public float gFunction(float distance, int range) {
		// TODO Auto-generated method stub
		return calculate(distance, range);
	}

}
