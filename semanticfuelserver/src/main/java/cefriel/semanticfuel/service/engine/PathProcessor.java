package cefriel.semanticfuel.service.engine;

import java.util.ArrayList;
import java.util.List;

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
		List<Point> samples = filterByDistance(path, 1300);
		return computePoligons(samples);

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
