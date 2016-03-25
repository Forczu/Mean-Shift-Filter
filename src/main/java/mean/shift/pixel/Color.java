package mean.shift.pixel;

@Deprecated
/**
 * Kolor jest reprezentowany jako wektor floatow.
 * @author Forczu
 */
public class Color {

	private final float l;
	private final float u;
	private final float v;

	private Color(float l, float u, float v) {
		this.l = l;
		this.u = u;
		this.v = v;
	}

	public static Color getInstance(float l, float u, float v) {
		return new Color(l, u, v);
	}

	public float l() {
		return l;
	}

	public float u() {
		return u;
	}

	public float v() {
		return v;
	}

}
