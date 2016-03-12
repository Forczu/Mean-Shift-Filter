import static org.junit.Assert.*;

import org.junit.Test;

import mean.shift.processing.ColorProcesser;

public class ColorConversionTest {

	@Test
	public void rgbToLuvConversionTest() {
		int pixel = (1 /*aplha*/ << 24)  | (255 /*red*/ << 16 ) | (255 /*green*/ <<8) | 0 /*blue*/;
		ColorProcesser cp = new ColorProcesser();
		float[] luv = cp.rgbToLuv(pixel);
		assertEquals(097.138, luv[0], 4);
		assertEquals(007.702, luv[1], 4);
		assertEquals(106.789, luv[2], 4);
	}

}
