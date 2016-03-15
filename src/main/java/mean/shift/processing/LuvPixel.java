package mean.shift.processing;

public class LuvPixel {

	private Position pos = null;

	private Color color = null;

	public LuvPixel(Position pos, Color color) {
		this.pos = pos;
		this.color = color;
	}

	public LuvPixel(int x, int y, float l, float u, float v) {
		pos = Position.getInstance(x, y);
		color = Color.getInstance(l, u, v);
	}

	public float getL() {
		return color.l();
	}

	public float getU() {
		return color.u();
	}

	public float getV() {
		return color.v();
	}

	public float x() {
		return pos.x();
	}

	public float y() {
		return pos.y();
	}

	public void setPos(Position pos) {
		this.pos = pos;
	}

	public Position getPos() {
		return pos;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public Color getColor() {
		return color;
	}

	@Override
    public int hashCode() {
        int x = (int)x();
        int y = x + (int)y();
        return 31 * y / 25;
    }

    @Override
    public boolean equals(Object obj) {
       if (!(obj instanceof LuvPixel))
            return false;
        if (obj == this)
            return true;
        LuvPixel rhs = (LuvPixel) obj;
        double thisX = x();
        double thisY = y();
        double otherX = rhs.x();
        double otherY = rhs.y();
        return otherX > thisX - 0.5 && otherX < thisX + 0.5
        		&& otherY > thisY - 0.5 && otherY < thisY + 0.5;
    }
}
