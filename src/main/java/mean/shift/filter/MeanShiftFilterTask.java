package mean.shift.filter;

import mean.shift.pixel.Pixel;
import mean.shift.pixel.Pixel;
import mean.shift.processing.MeanShiftParameter;
import mean.shift.thread.BaseThread;
import mean.shift.thread.MeanShiftThread;

public class MeanShiftFilterTask extends MeanShiftTask {

	public MeanShiftFilterTask(MeanShiftParameter parameter) {
		super(parameter);
	}

	@Override
	protected Pixel[] process(int[][] pixels, Pixel[] luvInputImage) {
		Pixel[] outImage = new Pixel[luvInputImage.length];

		int threadsCount = Runtime.getRuntime().availableProcessors();
		int segmentSize = luvInputImage.length / threadsCount;
		BaseThread[] threads = new BaseThread[threadsCount];

		for (int i = 0; i < threadsCount; i++) {
			int start = i * segmentSize;
			int end = start + segmentSize;

			// Jesli ilosc pikseli LUV nie jest podzielna przez ilosc rdzeni, ostatni bedzie murzynem
			if (i == threadsCount - 1)
				end += luvInputImage.length % threadsCount;

			threads[i] = new MeanShiftThread(pixels, luvInputImage, outImage, this, start, end);
		}

		try {
			for (Thread thread : threads) {
				thread.start();
			}
			for (Thread thread : threads) {
				thread.join();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return outImage;
	}

}
