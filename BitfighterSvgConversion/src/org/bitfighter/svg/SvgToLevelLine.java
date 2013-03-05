package org.bitfighter.svg;

import java.awt.Shape;
import java.awt.geom.PathIterator;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import com.kitfox.svg.Path;
import com.kitfox.svg.SVGCache;
import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGElement;

/**
 * @author dbuck
 *
 */
public class SvgToLevelLine {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		URI fileUri = new File("/home/dbuck/code/TrefoilKnot_01.svg").toURI();
//		URI fileUri = new File("/home/dbuck/code/19418-svgkde_celt12/kdesvgc2.svg").toURI();
//		URI fileUri = new File("/usr/share/kde4/apps/kdm/themes/SUSE/logo.svg").toURI();

		int barrierWidth = 50;
		int maxPoints = 31;  // Do not make greater than 31!
		
		double flatness = 0.1d;  // 0.01 - 0.5d is decent flatness
		double scaleFactor = 0.1f;
		
		System.out.print(
				run(fileUri, barrierWidth, maxPoints, scaleFactor, flatness)
				);
	}
	
	
	/**
	 * Run the conversion from SVG path to BarrierMaker level line
	 * 
	 * @param fileUri
	 * @param barrierWidth
	 * @param maxPoints
	 * @param scaleFactor
	 * @param flatness
	 */
	public static String run(URI fileUri, int barrierWidth, int maxPoints, 
			double scaleFactor, double flatness) {
		StringBuilder stringBuilder = new StringBuilder();
		
		List<Shape> shapeList = loadShapesFromSVG(fileUri);
		
		// This list will be list of all the shapes after path traversal has 
		// converted them into a collection of ordered points
		List<List<Point>> allShapePointLists = new ArrayList<List<Point>>();
		
		for(Shape shape: shapeList) {
			allShapePointLists.add(loadPointsFromShape(shape, flatness));
		}

		// De-dupe
		List<List<Point>> deDupedShapePointLists = deDupShapeList(allShapePointLists);

		for(List<Point> pointList: deDupedShapePointLists) {
			List<List<Point>> splitList = splitPointList(pointList, maxPoints);
			
			stringBuilder.append(
					buildLevelLineString(splitList, barrierWidth, maxPoints, scaleFactor)
					);
		}
		
		return stringBuilder.toString();
	}


	/**
	 * Inefficent method to remove duplicate shapes that are sometimes created...
	 * 
	 * @param allShapePointLists
	 */
	private static List<List<Point>> deDupShapeList(List<List<Point>> allShapePointLists) {
		List<List<Point>> returnListList = new ArrayList<List<Point>>();
		
		outerLoop: for(List<Point> origList: allShapePointLists) {
			
			for(List<Point> list: returnListList)
				if(Util.areListsEqual(origList, list))
					continue outerLoop;
			
			returnListList.add(origList);
		}
		
		return returnListList;
	}


	/**
	 * Splits a list of points if it is greater than the maximum allowed 
	 * 
	 * All because level files have a max line length of 4094...
	 * 
	 * This is hacky...
	 * 
	 * @param originalList
	 * @param maxPoints
	 * @return
	 */
	private static List<List<Point>> splitPointList(List<Point> originalList, int maxPoints) {
		List<List<Point>> splitList = new ArrayList<List<Point>>();

		int startIndex = 0;  // inclusive
		int endIndex = 0;    // exclusive  <-- this is why we have the '+ 1'
		int listSize = originalList.size();

		while (startIndex < listSize - 1) {
			if(endIndex + maxPoints + 1 >= listSize)
				endIndex = listSize - 1;
			else
				endIndex = startIndex + maxPoints + 1;

			List<Point> subList = new ArrayList<Point>();
			subList.addAll(originalList.subList(startIndex, endIndex));
			splitList.add(subList);

			startIndex += maxPoints;
		}

		return splitList;
	}
	

	/**
	 * Loads points from an SVG file
	 * 
	 * @param fileUri
	 * @return
	 */
	private static List<Shape> loadShapesFromSVG(URI fileUri) {
		SVGDiagram diagram = SVGCache.getSVGUniverse().getDiagram(fileUri);
		diagram.setIgnoringClipHeuristic(true);
		
		List<Shape> shapeList = new ArrayList<Shape>();

		recursePath(shapeList, diagram.getRoot());
		
		return shapeList;
	}
	
	
	private static void recursePath(List<Shape> fillList, SVGElement element) {
		List<?> children = element.getChildren(null);
		
		// Get all the path nodes
		for (Object o : children) {
			if (o instanceof Path)
				fillList.add(((Path) o).getShape());
			else
				recursePath(fillList, (SVGElement) o);
		}
	}
	
	
	/**
	 * Transforms a shape into a set of points
	 * 
	 * @param shape
	 * @return
	 */
	private static List<Point> loadPointsFromShape(Shape shape, double flatness) {
		List<Point> pointList = new ArrayList<Point>();

		PathIterator pathIterator = shape.getPathIterator(null, flatness);
		double[] coords = new double[2];

		while (!pathIterator.isDone()) {
			pathIterator.currentSegment(coords);
			pointList.add(new Point(coords[0], coords[1]));
			pathIterator.next();
		}

		return pointList;
	}

	
	/**
	 * Creates a level line String for the points in the specified list
	 * 
	 * @param pointList
	 * @param maxPoints
	 * @param scaleFactor
	 */
	private static String buildLevelLineString(List<List<Point>> splitList, int barrierWidth,
			int maxPoints, double scaleFactor) {
		
		StringBuilder stringBuilder = new StringBuilder();

		for(List<Point> thisList: splitList) {
			stringBuilder.append("BarrierMaker ");
			stringBuilder.append(barrierWidth);
			for (Point c : thisList) {
				stringBuilder.append("  ");
				stringBuilder.append(Util.doubleFormat(c.x * scaleFactor));
				stringBuilder.append(" ");
				stringBuilder.append(Util.doubleFormat(c.y * scaleFactor));
			}
			stringBuilder.append("\n");
		}
		
		return stringBuilder.toString();
	}

	
	// Helper methods


}
