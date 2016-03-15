package mean.shift.utils;

/**
 * Class for logging elapsed time.
 *
 */
public class StopWatch {

	private long start;

    public StopWatch() {
        start = 0;
    }
    
    public void start() {
    	
    	start = System.currentTimeMillis();
    }
    
    public void reset() {
    	
    	start = 0;
    }

    public double elapsedTime() {
        long now = System.currentTimeMillis();
        return (now - start) / 1000.0;
    }
}