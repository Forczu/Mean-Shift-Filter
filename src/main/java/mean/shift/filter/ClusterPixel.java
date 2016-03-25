package mean.shift.filter;

import java.util.HashSet;

import mean.shift.pixel.Pixel;

public class ClusterPixel {

	public Pixel pixel;
	public HashSet<Pixel> cluster;
	
	public ClusterPixel(Pixel pixel){
		this.pixel = pixel;
		cluster = null;
	}
}
