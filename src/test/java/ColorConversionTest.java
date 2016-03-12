import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import mean.shift.processing.ColorProcesser;

public class ColorConversionTest {

	ColorProcesser cp = null;

	@Before
	public void setUp() throws Exception {
		cp = new ColorProcesser();
	}

	@Test
	public void rgbToLuvConversionTest() {
		int pixel = 0xFF000000 | (255 /*red*/ << 16 ) | (255 /*green*/ <<8) | 0 /*blue*/;
		float[] luv = cp.rgbToLuv(pixel);
		assertEquals(097.138, luv[0], 4);
		assertEquals(007.702, luv[1], 4);
		assertEquals(106.789, luv[2], 4);
		pixel = 0xFF000000 | (255 /*red*/ << 16 ) | (0 /*green*/ <<8) | 0 /*blue*/;
		luv = cp.rgbToLuv(pixel);
		assertEquals(53.232, luv[0], 0.5);
		assertEquals(175.053, luv[1], 0.5);
		assertEquals(37.750, luv[2], 0.5);
	}

	@Test
	public void rgbToRgbThroughLuvConverstionTest() {
		int red = 255, green = 255, blue = 0;
		int pixel = 0xFF000000 | (red << 16 ) | (green << 8) | blue;
		int returnPixel = cp.luvToRgb(cp.rgbToLuv(pixel));
		int returnRed = 0xFF & (returnPixel >> 16);
		int returnGreen = 0xFF & (returnPixel >> 8);
		int returnBlue = 0xFF & (returnPixel >> 0);
		assertEquals(red, returnRed, 1.0);
		assertEquals(green, returnGreen, 1.0);
		assertEquals(blue, returnBlue, 1.0);
	}
}
