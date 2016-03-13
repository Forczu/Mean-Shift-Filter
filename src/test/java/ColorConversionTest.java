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
	
	@Test
	public void luvToRgbConversionTest()
	{
		float[] testLuv = { 100.0f, 50.0f, -100.0f };
		int pixel = cp.luvToRgb(testLuv);		
		int[] rgb = { 0xFF & (pixel >> 16), 0xFF & (pixel >> 8), 0xFF & (pixel >> 0) } ;
		
		assertEquals(255, rgb[0], 3);
		assertEquals(211, rgb[1], 3);
		assertEquals(255, rgb[2], 3);
		
		float[] testLuv2 = {20.0f, -120.0f, 30.0f};
		pixel = cp.luvToRgb(testLuv2);
		int[] rgb2 = { 0xFF & (pixel >> 16), 0xFF & (pixel >> 8), 0xFF & (pixel >> 0) } ;
		
		assertEquals(0, rgb2[0], 3);
		assertEquals(82.812f, rgb2[1], 3);
		assertEquals(20.649f, rgb2[2], 3);	
	}
}
