package mean.shift.thread;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import mean.shift.filter.MeanShiftTask;
import mean.shift.pixel.Pixel;

public class SegmentationThread extends BaseThread {

   public SegmentationThread(int[][] pixels, Pixel[] luvInputImage, Pixel[] luvOutputImage, MeanShiftTask meanShiftTaskObject, int start, int end) {
	   super(pixels, luvInputImage, luvOutputImage, meanShiftTaskObject, start, end);
   }

   @Override
   public void run() {
	   Class[] paramTypes = new Class[] {int[][].class, Pixel[].class, Pixel[].class, int.class, int.class};

	   try {
			Method algorithm = meanShiftTaskObject.getClass().getDeclaredMethod("segmentationAlgorithm", paramTypes);
			algorithm.invoke(meanShiftTaskObject, this.pixels, this.inputImage, this.outputImage, this.start, this.end);
	   }
	   catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
			e.printStackTrace();
	   }
   }
}
