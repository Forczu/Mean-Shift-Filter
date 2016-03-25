package mean.shift.pixel;

public class Pixel {

	protected Position pos = null;

	protected float[] colorVector = null;

	public float[] getColorVector() {
		return colorVector;
	}

	public void setColorVector(float[] colorVector) {
		this.colorVector = colorVector;
	}

	public Pixel(Position pos, float ... colors) {
		this.pos = pos;
		this.colorVector = colors;
	}

	public Pixel(int x, int y, float ... colors) {
		pos = Position.getInstance(x, y);
		this.colorVector = colors;
	}

	public void setPos(Position pos) {
		this.pos = pos;
	}

	public Position getPos() {
		return pos;
	}

	public float x() {
		return pos.x();
	}

	public float y() {
		return pos.y();
	}

	@Override
    public int hashCode() {
        int x = (int)x();
        int y = x + (int)y();
        return 31 * y / 25;
    }

    @Override
    public boolean equals(Object obj) {
       if (!(obj instanceof Pixel))
            return false;
        if (obj == this)
            return true;
        Pixel rhs = (Pixel) obj;
        double thisX = x();
        double thisY = y();
        double otherX = rhs.x();
        double otherY = rhs.y();
        return otherX > thisX - 0.5 && otherX < thisX + 0.5
        		&& otherY > thisY - 0.5 && otherY < thisY + 0.5;
    }
}
