package cefriel.semanticfuel.utils.geo;

import cefriel.semanticfuel.model.Point;

public class GeoUtils {
	public static Double computeDistance(Point p1, Point p2) {
		Double R = 6371e3; // metres
		Double fi1 = Math.toRadians(p1.getLatitude());
		Double fi2 = Math.toRadians(p2.getLatitude());
		Double deltafi = Math.toRadians(p2.getLatitude() - p1.getLatitude());
		Double deltaLambda = Math.toRadians(p2.getLongitude() - p1.getLongitude());

		Double a = Math.sin(deltafi / 2) * Math.sin(deltafi / 2)
				+ Math.cos(fi1) * Math.cos(fi2) * Math.sin(deltaLambda / 2) * Math.sin(deltaLambda / 2);
		Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

		Double d = R * c;

		return d;
	}
}
