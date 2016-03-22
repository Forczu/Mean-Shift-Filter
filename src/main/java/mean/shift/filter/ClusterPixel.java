package mean.shift.filter;

import java.util.HashSet;

import mean.shift.processing.LuvPixel;

public class ClusterPixel {

	public LuvPixel pixel;
	public HashSet<LuvPixel> cluster;
	
	public ClusterPixel(LuvPixel pixel){
		this.pixel = pixel;
		cluster = null;
	}
}
