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
		List<Point> samples = getSamplesFromPath(path);
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

			shapeFactory.setCentre(new Coordinate((p1.getLatitude() + p2.getLatitude()) / 2, (p1.getLongitude() + p2.getLongitude())/2));
			// length in meters of 1° of longitude = 40075 km * cos( latitude ) / 360
			shapeFactory.setHeight(diameter);
			shapeFactory.setWidth(diameter);
			

			result.add(shapeFactory.createCircle());
		}
		MultiPolygon multiPoligon = new MultiPolygon(result.toArray(new Polygon[result.size()]), factory);
		LOG.debug("Multipoligon = {}", multiPoligon);
		return multiPoligon;
	}

	private List<Point> getSamplesFromPath(List<Point> pointList) {
		List<Point> result = IntStream.range(0, pointList.size()).filter(i -> i % 40 == 0)
				.mapToObj(e -> pointList.get(e)).collect(Collectors.toList());
		result.add(pointList.get(pointList.size() - 1));
		return result;
	}

	public Double computeDistance(Point p1, Point p2) {

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
