package mean.shift.processing;

import javafx.geometry.Point2D;
import javafx.geometry.Point3D;

public class LuvPixel {

	private Point2D position = null;

	private Point3D color = null;

	public LuvPixel(Point2D position, Point3D color) {
		this.position = position;
		this.color = color;
	}

	public LuvPixel(int x, int y, float l, float u, float v) {
		position = new Point2D(x, y);
		color = new Point3D(l, u, v);
	}

	public float getL() {
		return (float)color.getX();
	}

	public float getU() {
		return (float)color.getY();
	}

	public float getV() {
		return (float)color.getZ();
	}

	public Point2D getPosition() {
		return position;
	}

	public Point3D getColor() {
		return color;
	}

	public float getSpatialDistance() {
		return (float) position.distance(0, 0);
	}

	public float getRangeDistance() {
		return (float) color.distance(0, 0, 0);
	}

	public void setColor(Point3D color) {
		this.color = color;
	}
}
