package org.bitfighter.svg;

public class Point {
	public double x;
	public double y;
	
	// 1 / this number = how many decimal places should the equals() method consider?
	private final int precision = 1000;
	
	public Point(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public Point(double[] coords) {
		x = coords[0];
		y = coords[1];
	}
	
	public String toString() {
		return "["+x+","+y+"]";
	}

	
	/**
	 * test if two Points are close enough to be considered equal
	 * 
	 * @param otherPoint
	 * @return
	 */
	public boolean equals(Point otherPoint) {
		if(Math.round(this.x * precision) == Math.round(otherPoint.x * precision) &&
				Math.round(this.y * precision) == Math.round(otherPoint.y * precision))
			return true;
		
		return false;
	}
}