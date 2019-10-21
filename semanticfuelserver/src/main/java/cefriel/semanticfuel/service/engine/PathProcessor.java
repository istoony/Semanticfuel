package cefriel.semanticfuel.service.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.util.GeometricShapeFactory;
import org.springframework.stereotype.Service;

import cefriel.semanticfuel.model.Point;
import cefriel.semanticfuel.service.AbstractService;

@Service
public class PathProcessor extends AbstractService {

	public Geometry getPathArea(List<Point> path) {
		List<Point> samples = filterByIndex(path, 40);
		return computePoligons(samples.subList(0, 1));
	}

	private MultiPolygon computePoligons(List<Point> points) {
		List<Polygon> result = new ArrayList<>();
		GeometryFactory factory = new GeometryFactory();

		GeometricShapeFactory shapeFactory = new GeometricShapeFactory();
		shapeFactory.setNumPoints(6);

		for (int i = 1; i < points.size(); i++) {

			Point p1 = points.get(i - 1);
			Point p2 = points.get(i);

			double diameter = computeDistance(p1, p2);

			shapeFactory.setCentre(new Coordinate((p1.getLatitude() + p2.getLatitude()) / 2,
					(p1.getLongitude() + p2.getLongitude()) / 2));
			// length in meters of 1° of longitude = 40075 km * cos( latitude ) / 360
			shapeFactory.setHeight(diameter);
			shapeFactory.setWidth(diameter);

			result.add(shapeFactory.createCircle());
		}
		MultiPolygon multiPoligon = new MultiPolygon(result.toArray(new Polygon[result.size()]), factory);
		LOG.debug("Multipoligon = {}", multiPoligon);
		return multiPoligon;
	}

	private List<Point> computeRectanglesPath(List<Point> reducedList, Double L) {
		List<Point> result = new ArrayList<Point>();
		List<Point> back = new ArrayList<Point>();
		for (int i = 1; i < reducedList.size(); i++) {
			double y1 = reducedList.get(i - 1).getLatitude();
			double x1 = reducedList.get(i - 1).getLongitude();

			double y2 = reducedList.get(i).getLatitude();
			double x2 = reducedList.get(i).getLongitude();

			double m = (y2 - y1) / (x2 - x1);
			double m2 = -1 / m;

			double dx = Math.sqrt(Math.pow(L, 2) / (1 + Math.pow(m2, 2))) / 2;
			double dy = m2 * dx;

			LOG.debug("dx = {}", dx);
			LOG.debug("dy = {}", dy);
			LOG.debug("m = {}", m);
			LOG.debug("m2 = {}", m2);

			Point p1 = new Point(y1 + dy, x1 + dx);
			Point p2 = new Point(y1 - dy, x1 - dx);
			Point p3 = new Point(y2 + dy, x2 + dx);
			Point p4 = new Point(y2 - dy, x2 - dx);

			result.add(p1);
			result.add(p3);
			back.add(p2);
			back.add(p4);
		}

		for (int i = back.size() - 1; i >= 0; i--) {
			result.add(back.get(i));
		}
		result.add(result.get(0));

		return result;
	}

	private List<Point> filterBySlope(List<Point> pointList, double slopeTollerance) {
		List<Point> result = new ArrayList<>();

		double currentSlope = computeSlope(pointList.get(0), pointList.get(1));
		Point currentPoint = pointList.get(0);

		for (int i = 1; i < pointList.size(); i++) {
			Point p = pointList.get(i);

			double slope = computeSlope(currentPoint, p);

			if (Math.abs(slope - currentSlope) > slopeTollerance) {
				result.add(p);

				currentPoint = p;
				currentSlope = computeSlope(pointList.get(i - 1), p);
			} else
				currentSlope = slope;
		}
		if (!result.get(result.size() - 1).equals(pointList.get(pointList.size() - 1)))
			result.add(pointList.get(pointList.size() - 1));

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

	private List<Point> filterByIndex(List<Point> pointList, int rate) {
		List<Point> result = IntStream.range(0, pointList.size()).filter(i -> i % rate == 0)
				.mapToObj(e -> pointList.get(e)).collect(Collectors.toList());
		if (pointList.size() % rate != 0)
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

	private double computeSlope(Point p1, Point p2) {
		double y1 = p1.getLatitude();
		double x1 = p1.getLongitude();

		double y2 = p2.getLatitude();
		double x2 = p2.getLongitude();

		return (y2 - y1) / (x2 - x1);
	}

}
