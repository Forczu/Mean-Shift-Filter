package mean.shift;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javafx.concurrent.Task;
import javafx.geometry.Point3D;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import mean.shift.kernel.Kernel;
import mean.shift.processing.ColorProcesser;
import mean.shift.processing.LuvPixel;
import mean.shift.processing.Metrics;

/**
 * Glowna klasa algorytmu mean shift
 *
 * @author Forczu
 */
public class MeanShiftFilter extends Task<Image> {

	protected Image image;
	protected Kernel kernel;
	protected int spatialPar;
	protected int rangePar;
	protected int maxIters;
	protected int minShift;
	protected Metrics metrics;

	public MeanShiftFilter(Image image, Kernel kernel, int spatialPar, int rangePar, int maxIters, int minShift,
			Metrics metrics) {
		this.image = image;
		this.kernel = kernel;
		this.spatialPar = spatialPar;
		this.rangePar = rangePar;
		this.maxIters = maxIters;
		this.minShift = minShift;
		this.metrics = metrics;
	}

	@Override
	protected Image call() {

		ColorProcesser cp = new ColorProcesser();
		int[][] pixels = cp.getPixelArray(image);
		LuvPixel[] luv = cp.getLuvArray(pixels);
		LuvPixel[] out = new LuvPixel[luv.length];

		int width = pixels.length;
		int height = pixels[0].length;

		double shift = 0;
		int iters = 0;
		int hrad = spatialPar;
		int hcolor = rangePar;
		int pixelNumber = luv.length;
		updateProgress(0, pixelNumber);

		// dla kazdego piksela
		for (int i = 0; i < pixelNumber; i++) {

			// pobierz aktualna pozycje piksela
			int xc = (int) luv[i].getPosition().getX();
			int yc = (int) luv[i].getPosition().getY();
			// miejsce na stare dane
			int xcOld, ycOld;
			float LcOld, UcOld, VcOld;
			// aktualna poyzcja i kolor
			Point3D color = luv[i].getColor();
			float Lc = (float) color.getX();
			float Uc = (float) color.getY();
			float Vc = (float) color.getZ();
			// licznik iteracji
			iters = 0;
			// mean-shiftowanie
			do {
				// zachowanie starych danych
				xcOld = xc;
				ycOld = yc;
				LcOld = Lc;
				UcOld = Uc;
				VcOld = Vc;
				// wartosci przesuniecia
				float mx = 0, my = 0, mL = 0, mU = 0, mV = 0;
				double pointNum = 0.0, colorNum = 0.0;
				// MEAN SHIFT (17)
				for (int ry = -hrad; ry <= hrad; ry++) {
					int y2 = yc + ry;
					if (y2 >= 0 && y2 < height) {
						for (int rx = -hrad; rx <= hrad; rx++) {
							int x2 = xc + rx;
							if (x2 >= 0 && x2 < width) {
								double pointDistance = metrics.getDistance(ry, rx);
								if (pointDistance <= hrad) {
									color = luv[y2 * width + x2].getColor();

									float L2 = (float) color.getX();
									float U2 = (float) color.getY();
									float V2 = (float) color.getZ();

									double dL = Lc - L2;
									double dU = Uc - U2;
									double dV = Vc - V2;

									double colorDistance = metrics.getDistance(dL, dU, dV);
									if (colorDistance <= hcolor) {
										double pointKernel = kernel.gFunction(Math.pow(pointDistance / hrad, 2));
										mx += x2 * pointKernel;
										my += y2 * pointKernel;
										pointNum += pointKernel;
										double colorKernel = kernel.gFunction(Math.pow(colorDistance / hcolor, 2));
										mL += L2 * colorKernel;
										mU += U2 * colorKernel;
										mV += V2 * colorKernel;
										colorNum += colorKernel;
									}
								}
							}
						}
					}
				}
				// nowe przesuniecie okna
				xc = (int) (mx * (1.0 / pointNum) + 0.5);
				yc = (int) (my * (1.0 / pointNum) + 0.5);
				Lc = (float) (mL * (1.0 / colorNum));
				Uc = (float) (mU * (1.0 / colorNum));
				Vc = (float) (mV * (1.0 / colorNum));
				// mean-shift
				int dx = xc - xcOld;
				int dy = yc - ycOld;
				float dL = Lc - LcOld;
				float dU = Uc - UcOld;
				float dV = Vc - VcOld;

				shift = metrics.getDistance(dx, dy, dL, dU, dV);
				iters++;
			} while (shift > minShift && iters < maxIters);

			out[i] = new LuvPixel(luv[i].getPosition(), new Point3D(Lc, Uc, Vc));
			updateProgress(i, pixelNumber);

		}

		
		//Segmentacja
		List<HashSet<LuvPixel>> clusters = new ArrayList<>();

		for (int i = 0; i < pixelNumber; i++) {
			int xPixel = (int) out[i].getPosition().getX();
			int yPixel = (int) out[i].getPosition().getY();
			HashSet<LuvPixel> actualCluster;
			actualCluster = null;
			Point3D color = out[i].getColor();
			float lPixel = (float) color.getX();
			float uPixel = (float) color.getY();
			float vPixel = (float) color.getZ();

			if (clusters.isEmpty()) {
				HashSet<LuvPixel> cluster = new HashSet<>();
				cluster.add(out[i]);
				clusters.add(cluster);
				actualCluster = cluster;
			} else {
				for (HashSet<LuvPixel> cluster : clusters) {
					if (cluster.contains(out[i])) {
						actualCluster = cluster;
						break;
					}
				}
				if(actualCluster == null){
					HashSet<LuvPixel> cluster = new HashSet<LuvPixel>();
					cluster.add(out[i]);
					clusters.add(cluster);
					actualCluster = cluster;
				}
			}

			for (int ry = -hrad; ry <= hrad; ry++) {
				int y2 = yPixel + ry;
				if (y2 >= 0 && y2 < height) {
					for (int rx = -hrad; rx <= hrad; rx++) {
						int x2 = xPixel + rx;
						if (x2 >= 0 && x2 < width) {
							double pointDistance = metrics.getDistance(ry, rx);
							if (pointDistance <= hrad) {
								int pixelIndex = y2 * width + x2;
								color = out[pixelIndex].getColor();

								float L2 = (float) color.getX();
								float U2 = (float) color.getY();
								float V2 = (float) color.getZ();

								double dL = lPixel - L2;
								double dU = uPixel - U2;
								double dV = vPixel - V2;

								double colorDistance = metrics.getDistance(dL, dU, dV);
								if (colorDistance <= hcolor) {
									//Dodawanie elementu do klastra
									actualCluster.add(out[pixelIndex]);
								}
							}
						}
					}
				}
			}
		}
		
		//Sprawdzanie iloœci elementów w klastrach
		for(HashSet<LuvPixel> cluster: clusters){
			if(cluster.size() < 20){
				for(LuvPixel outPixel:cluster){
					outPixel.setColor(new Point3D(100, 0.0008906695967064726, -0.01710795288980549));
				}
			}
		}
		//Koniec segmentacji
		int[][] rgb = cp.getRgbArray(out, width);
		WritableImage filteredImage = new WritableImage(width, height);
		PixelWriter pw = filteredImage.getPixelWriter();
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				pw.setArgb(i, j, rgb[i][j]);
			}
		}
		return filteredImage;
	}
}
