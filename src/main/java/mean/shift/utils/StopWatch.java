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

    public double getElapsedTime() {
        long now = System.currentTimeMillis();
        return (now - start) / 1000.0;
    }
    
    public String getFormattedTime() {
    	double elapsedTime = getElapsedTime();
    	int minutes = (int)((int)elapsedTime / 60.0f);
    	short seconds = (short)((int)elapsedTime % 60.0f);
    	
    	return String.format("%02d", minutes) + ":" + String.format("%02d", seconds);
    }
}