package mean.shift.thread;

import mean.shift.filter.MeanShiftTask;
import mean.shift.processing.LuvPixel;

public abstract class BaseThread extends Thread {
	int[][] pixels;
	LuvPixel[] inputImage;
	LuvPixel[] outputImage;
	MeanShiftTask meanShiftTaskObject;
	int start, end;
	
   protected BaseThread(int[][] pixels, LuvPixel[] luvInputImage, LuvPixel[] luvOutputImage, MeanShiftTask meanShiftTaskObject, int start, int end) {
	   this.pixels = pixels;
	   this.inputImage = luvInputImage;
	   this.outputImage = luvOutputImage;
	   this.meanShiftTaskObject = meanShiftTaskObject;
	   this.start = start;
	   this.end = end;
   }

   public abstract void run();
}
