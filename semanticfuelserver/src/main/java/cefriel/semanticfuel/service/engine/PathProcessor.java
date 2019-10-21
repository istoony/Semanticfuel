package cefriel.semanticfuel.service.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.locationtech.jts.geom.Polygon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cefriel.semanticfuel.model.Point;
import cefriel.semanticfuel.service.AbstractService;
import cefriel.semanticfuel.utils.geo.GeoUtils;

@Service
public class PathProcessor extends AbstractService {
	@Autowired
	private GeometryBuilder geometryFactory;

	public List<Polygon> getPathArea(List<Point> path) {
		List<Point> samples = filterByIndex(path, 40);
		return computePoligons(samples.subList(0, 1));
	}

	private List<Polygon> computePoligons(List<Point> points) {
		List<Polygon> result = new ArrayList<>();

		for (int i = 1; i < points.size(); i++) {

			Point p1 = points.get(i - 1);
			Point p2 = points.get(i);

			double diameter = GeoUtils.computeDistance(p1, p2);
			Point center = new Point((p1.getLatitude() + p2.getLatitude()) / 2,
					(p1.getLongitude() + p2.getLongitude()) / 2);

			result.add(geometryFactory.createCircle(6, center, diameter));
		}

		return result;
	}

	private List<Point> filterBySlope(List<Point> pointList, double slopeTollerance) {
		List<Point> result = new ArrayList<>();

		double currentSlope = GeoUtils.computeSlope(pointList.get(0), pointList.get(1));
		Point currentPoint = pointList.get(0);

		for (int i = 1; i < pointList.size(); i++) {
			Point p = pointList.get(i);

			double slope = GeoUtils.computeSlope(currentPoint, p);

			if (Math.abs(slope - currentSlope) > slopeTollerance) {
				result.add(p);

				currentPoint = p;
				currentSlope = GeoUtils.computeSlope(pointList.get(i - 1), p);
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

			double p12 = GeoUtils.computeDistance(p1, p2);

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

}
