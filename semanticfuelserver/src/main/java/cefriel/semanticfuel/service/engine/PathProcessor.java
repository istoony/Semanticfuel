package cefriel.semanticfuel.service.engine;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.util.GeometricShapeFactory;
import org.springframework.stereotype.Service;

import cefriel.semanticfuel.model.Point;
import cefriel.semanticfuel.service.AbstractService;

@Service
public class PathProcessor extends AbstractService {

	public List<Polygon> getPathArea(List<Point> path) {
		List<Point> samples = filterByDistance(path, 1300);
		return computePoligons(samples);

	}

	private List<Polygon> computePoligons(List<Point> points) {
		List<Polygon> result = new ArrayList<>();

		for (int i = 1; i < points.size(); i++) {
			GeometricShapeFactory shapeFactory = new GeometricShapeFactory();
			shapeFactory.setNumPoints(8);

			Point p1 = points.get(i - 1);
			Point p2 = points.get(i);
			double y1 = p1.getLatitude();
			double x1 = p1.getLongitude();

			double y2 = p2.getLatitude();
			double x2 = p2.getLongitude();

			double diameter = Math.sqrt(Math.pow((x2 - x1), 2) + Math.pow((y2 - y1), 2));

			shapeFactory.setCentre(new Coordinate((p1.getLatitude() + p2.getLatitude()) / 2,
					(p1.getLongitude() + p2.getLongitude()) / 2));

			shapeFactory.setHeight(diameter);
			shapeFactory.setWidth(diameter);

			result.add(shapeFactory.createEllipse());
		}

		return result;
	}

	private List<Point> filterByDistance(List<Point> pointList, double distance) {
		double currentDistance = 0;

		List<Point> result = new ArrayList<>();
		result.add(pointList.get(0));
		for (int i = 0; i < pointList.size() - 1; i++) {
			Point p1 = pointList.get(i);
			Point p2 = pointList.get(i + 1);

			double p12 = computeDistance(p1, p2);

			currentDistance += p12;
			if (currentDistance > distance) {
				result.add(p2);

				currentDistance = 0;
			}
		}
		if (!result.get(result.size() - 1).equals(pointList.get(pointList.size() - 1)))
			result.add(pointList.get(pointList.size() - 1));

		return result;

	}

	private Double computeDistance(Point p1, Point p2) {

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
