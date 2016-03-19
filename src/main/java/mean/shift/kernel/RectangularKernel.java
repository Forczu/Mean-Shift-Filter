package mean.shift.kernel;
import java.lang.Math;

@Deprecated
/**
 * Protokatny kernel nie spelnia warunku
 * symetrii promienistej, ktory musza
 * spelniac rozpatrywane funkcje jadra.
 * @author Forczu
 *
 */
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
		return calculate(distance, range);
	}

	@Override
	public float profile(float x) {
		return 0;
	}
}
