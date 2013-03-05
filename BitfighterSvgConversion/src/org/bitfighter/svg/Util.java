package org.bitfighter.svg;

import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;

public class Util {

	private static DecimalFormat df = new DecimalFormat("#####0.000");
	
	/**
	 * Format our point coordinate
	 * 
	 * @param d
	 * @return
	 */
	public static String doubleFormat(double d) {
		return df.format(d);
	}
	
	
	/**
	 * Compare two lists of Points
	 * 
	 * Point and their order must match for the lists to be considered identical
	 * 
	 * @param list1
	 * @param list2
	 * @return 
	 */
	public static boolean areListsEqual(List<Point> list1, List<Point> list2) {

		// quick size check first
		if(list1.size() != list2.size())
			return false;
		
		Iterator<Point> iterator1 = list1.iterator();
		
		for(Point entry: list2)
			if(!entry.equals(iterator1.next()))
				return false;
		
		return true;
	}
}
