package mean.shift.kernel;

public interface Kernel {

	public double calculate(double distance, int range);

	public double gFunction(double distance, int range);

}
