package mean.shift.processing;

import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;

public class ColorProcesser {

	protected static final double Epsilon = 0.008856;
	protected static final double Kappa = 903.3;
	protected static final double c = -1.0 / 3.0;

	// D65 white point
	protected static final double xn = 95.047;
	protected static final double yn = 100.000;
	protected static final double zn = 108.883;

	/**
	 * Zwraca reprezentacje obrazu w postaci tablicy pikseli.
	 * Kazdy piksel reprezentowany jest przez 32-bitego inta.
	 *
	 * @param image obraz
	 * @return tablica pikseli
	 */
	public int[][] getPixelArray(Image image) {
    	// wczytanie pierwotnych pikseli; x_i, i = 1, 2, ... , n
    	PixelReader pr = image.getPixelReader();
    	int width = (int)image.getWidth();
    	int height = (int)image.getHeight();
    	int[][] pixelArray = new int[width][height];
    	for(int i = 0; i < width; ++i) {
    		for(int j = 0; j < height; ++j) {
    			pixelArray[i][j] = pr.getArgb(i, j);
    		}
    	}
    	return pixelArray;
	}

	private float normalize(int color) {
		return color / 255.0f;
	}

	private float[] toRgb(int argb) {
		float[] color = new float[3];
		color[0] = normalize(0xFF & (argb >> 16));
		color[1] = normalize(0xFF & (argb >> 8 ));
		color[2] = normalize(0xFF & (argb >> 0 ));
		return color;
	}

	public float[] rgbToLuv(int argb) {

		// normalizacja RGB
		float[] rgb = toRgb(argb);
		double R = rgb[0];
		double G = rgb[1];
		double B = rgb[2];

		double r = toPivotRgb(R);
		double g = toPivotRgb(G);
		double b = toPivotRgb(B);

        // Observer. = 2‹, Illuminant = D65
		// RGB -> XYZ
		double X = r * 0.4124 + g * 0.3576 + b * 0.1805;
		double Y = r * 0.2126 + g * 0.7152 + b * 0.0722;
		double Z = r * 0.0193 + g * 0.1192 + b * 0.9505;
		// XYZ -> RGB
		double y = Y / yn;
		double L = y > Epsilon ? 116.0 * Math.pow(y, 1.0 / 3.0) - 16.0 : Kappa * y;

		double targetDenominator = (1.0 * X + 15.0 * Y + 3.0 * Z);
		double referenceDenominator = (1.0 * xn + 15.0 * yn + 3.0 * zn);

		double xTarget = targetDenominator == 0 ? 0 : ((4.0 * X / targetDenominator) - (4.0 * xn / referenceDenominator));
		double yTarget = targetDenominator == 0 ? 0 : ((9.0 * Y / targetDenominator) - (9.0 * yn / referenceDenominator));

		double U = 13.0 * L * xTarget;
		double V = 13.0 * L * yTarget;

		return new float[] {(float)L, (float)U, (float)V};
	}

	public int luvToRgb(float[] luv) {
		// LUV
		double L = luv[0];
		double U = luv[1];
		double V = luv[2];
		// LUV -> XYZ
		double uPrime = 4.0 * xn / (1.0 * xn + 15.0 * yn + 3.0 * zn);
		double vPrime = 9.0 * yn / (1.0 * xn + 15.0 * yn + 3.0 * zn);
		double a = (1.0 / 3.0) * ((52.0 * L) / (U + 13 * L * uPrime) - 1.0);
		double imteL_16_116 = (L + 16.0) / 116.0;
		double y = L > Kappa * Epsilon
				? imteL_16_116 * imteL_16_116 * imteL_16_116
				: L / Kappa;
		double b = -5.0 * y;
		double d = y * ((39.0 * L) / (V + 13.0 * L * vPrime) - 5.0);
		double x = (d - b) / (a - c);
		double z = x * a + b;
		// XYZ -> RGB
		double r = x * 3.2406 + y * -1.5372 + z * -0.4986;
		double g = x * -0.9689 + y * 1.8758 + z * 0.0415;
		b = x * 0.0557 + y * -0.2040 + z * 1.0570;

		r = r > 0.0031308 ? 1.055 * Math.pow(r, 1.0 / 2.4) - 0.055 : 12.92 * r;
        g = g > 0.0031308 ? 1.055 * Math.pow(g, 1.0 / 2.4) - 0.055 : 12.92 * g;
        b = b > 0.0031308 ? 1.055 * Math.pow(b, 1.0 / 2.4) - 0.055 : 12.92 * b;
        // standaryzacja RGB
        int R = (int)toRgb(r);
        int G = (int)toRgb(g);
        int B = (int)toRgb(b);

		return 0xFF000000 | (R << 16 ) | (G << 8) | B;
	}

	private double toRgb(double n) {
        double result = 255.0 * n;
        if (result < 0) return 0;
        if (result > 255) return 255;
        return result;
	}

	private double toPivotRgb(double n) {
		return (n > 0.04045 ? Math.pow((n + 0.055) / 1.055, 2.4) : n / 12.92) * 100.0;
	}

	public LuvPixel[] getLuvArray(int[][] rgbArray) {
    	// wczytanie zfiltrowanych pikseli; z_i, i = 1, 2, ... , n
		int width = rgbArray.length;
		int height = rgbArray[0].length;
		LuvPixel[] luvArray = new LuvPixel[width * height];
    	for(int i = 0; i < width; ++i) {
    		for(int j = 0; j < height; ++j) {
    			float[] color = rgbToLuv(rgbArray[i][j]);
    			luvArray[j * width + i] = new LuvPixel(i, j, color[0], color[1], color[2]);
    		}
    	}
    	return luvArray;
	}

	public int[][] getRgbArray(LuvPixel[] luvArray, int width) {
		int[][] rgbArray = new int[width][luvArray.length / width];
		for (int i = 0; i < luvArray.length; i++) {
			Position pos = luvArray[i].getPos();
			Color color = luvArray[i].getColor();
			int argb = luvToRgb(new float[]{color.l(), color.u(), color.v()});
			rgbArray[(int)pos.x()][(int)pos.y()] = argb;
		}
		return rgbArray;
	}
}
