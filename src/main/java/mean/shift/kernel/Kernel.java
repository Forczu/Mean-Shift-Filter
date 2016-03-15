package mean.shift.kernel;

public interface Kernel {

	public float calculate(float distance, int range);

	public float gFunction(float distance, int range);

}
