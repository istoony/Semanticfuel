package cefriel.semanticfuel.service.engine;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cefriel.semanticfuel.model.Point;
import cefriel.semanticfuel.service.AbstractService;
import cefriel.semanticfuel.utils.geo.GeoUtils;

@Service
public class PathProcessor extends AbstractService {
	@Autowired
	private GeometryBuilder geometryFactory;

	public List<Geometry> getPathArea(List<Point> path) {
		List<Point> samples = filterByDistance(path, 1000);
		return computePoligons(samples);

	}

	private List<Geometry> computePoligons(List<Point> points) {
		List<Geometry> result = new ArrayList<>();

		for (int i = 1; i < points.size(); i++) {
			Point p1 = points.get(i - 1);
			Point p2 = points.get(i);

			double y1 = p1.getLatitude();
			double x1 = p1.getLongitude();

			double y2 = p2.getLatitude();
			double x2 = p2.getLongitude();

			double diameter = Math.sqrt(2 * Math.pow((x2 - x1), 2) + Math.pow((y2 - y1), 2));

			result.add(geometryFactory.createPolygon(8, new Point((y1 + y2) / 2, (x1 + x2) / 2), diameter));
		}

		return result;
	}

	private List<Point> filterByDistance(List<Point> pointList, double distance) {
		double currentDistance = 0;
		LOG.info("Iniztial Size: {}", pointList.size());
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
		LOG.info("Final Size: {}", result.size());
		return result;

	}

}
