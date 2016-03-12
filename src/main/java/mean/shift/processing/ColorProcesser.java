package mean.shift.processing;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;

public class ColorProcesser {

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
		// standaryzacja RGB
		float[] rgb = toRgb(argb);
		double var_R = rgb[0];
		double var_G = rgb[1];
		double var_B = rgb[2];
		// RGB -> XYZ (http://www.easyrgb.com/index.php?X=MATH&H=02#text2)
		if ( var_R > 0.04045 ) var_R = Math.pow((var_R + 0.055) / 1.055, 2.4);
		else                   var_R = var_R / 12.92f;
		if ( var_G > 0.04045 ) var_G = Math.pow((var_G + 0.055) / 1.055, 2.4);
		else                   var_G = var_G / 12.92f;
		if ( var_B > 0.04045 ) var_B = Math.pow((var_B + 0.055) / 1.055, 2.4);
		else                   var_B = var_B / 12.92f;

		var_R *= 100;
		var_G *= 100;
		var_B *= 100;

		//Observer. = 2‹, Illuminant = D65
		double X = var_R * 0.4124 + var_G * 0.3576 + var_B * 0.1805;
		double Y = var_R * 0.2126 + var_G * 0.7152 + var_B * 0.0722;
		double Z = var_R * 0.0193 + var_G * 0.1192 + var_B * 0.9505;

		//XYZ -> LUV (http://www.easyrgb.com/index.php?X=MATH&H=16#text16)
		double var_U = ( 4 * X ) / ( X + ( 15 * Y ) + ( 3 * Z ) );
		double var_V = ( 9 * Y ) / ( X + ( 15 * Y ) + ( 3 * Z ) );

		double var_Y = Y / 100;
		if (var_Y > 0.008856 ) var_Y = Math.pow( var_Y , ( 1/3 ));
		else    			   var_Y = ( 7.787 * var_Y ) + ( 16 / 116 );

		double ref_X =  95.047;        //Observer= 2°, Illuminant= D65
		double ref_Y = 100.000;
		double ref_Z = 108.883;

		double ref_U = ( 4 * ref_X ) / ( ref_X + ( 15 * ref_Y ) + ( 3 * ref_Z ) );
		double ref_V = ( 9 * ref_Y ) / ( ref_X + ( 15 * ref_Y ) + ( 3 * ref_Z ) );

		double L =  116 * var_Y - 16;
		double u =  13 * L * (var_U - ref_U);
		double v =  13 * L * (var_V - ref_V);

		return new float[] {(float)L, (float)u, (float)v};
	}

	public float[][][] getLuvArray(int[][] rgbArray) {
    	// wczytanie zfiltrowanych pikseli; z_i, i = 1, 2, ... , n
		int width = rgbArray.length;
		int height = rgbArray[0].length;
		float[][][] luvArray = new float[width][height][3];
    	for(int i = 0; i < width; ++i) {
    		for(int j = 0; j < height; ++j) {
    			luvArray[i][j] = rgbToLuv(rgbArray[i][j]);
    		}
    	}
    	return luvArray;
	}


}
