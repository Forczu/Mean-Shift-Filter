package mean.shift.pixel;

public class Position {

	private float x;
	private float y;

	private Position(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public static Position getInstance(float x, float y) {
		return new Position(x, y);
	}

	public float x() {
		return x;
	}

	public float y() {
		return y;
	}


}
