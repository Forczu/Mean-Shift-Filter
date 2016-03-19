package mean.shift.kernel;

public interface Kernel {

	@Deprecated
	public float calculate(float distance, int range);

	public float profile(float x);

	public float gFunction(float distance, int range);

}
