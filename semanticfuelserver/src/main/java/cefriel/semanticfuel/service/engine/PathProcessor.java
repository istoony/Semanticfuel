package cefriel.semanticfuel.service.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.jena.geosparql.implementation.jts.CustomCoordinateSequenceFactory;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateSequenceFactory;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.springframework.stereotype.Service;

import cefriel.semanticfuel.model.Point;
import cefriel.semanticfuel.service.AbstractService;

@Service
public class PathProcessor extends AbstractService {

	public Geometry getPathArea(List<Point> path) {
		List<Point> samples = getSamplesFromPath(path);

		return computePoligons(samples);
	}

	private MultiPolygon computePoligons(List<Point> points) {
		List<Polygon> result = new ArrayList<>();
		GeometryFactory factory = new GeometryFactory();

		CoordinateSequenceFactory pointFactory = new CustomCoordinateSequenceFactory();

		for (int i = 1; i < points.size(); i++) {
			Point pInit = points.get(i - 1);
			Point pEnd = points.get(i);

			double y1 = pInit.getLatitude();
			double x1 = pInit.getLongitude();

			double y2 = pEnd.getLatitude();
			double x2 = pEnd.getLongitude();

			double r = Math.sqrt(Math.pow((x2 - x1), 2) + Math.pow((y2 - y1), 2));
			Coordinate c1 = new Coordinate(x1 + r, y1 + r);
			Coordinate c2 = new Coordinate(x1 + r, y1 - r);
			Coordinate c3 = new Coordinate(x1 - r, y1 - r);
			Coordinate c4 = new Coordinate(x1 - r, y1 + r);

			Coordinate[] coordinates = { c1, c2, c3, c4, c1 };
			CoordinateSequence cSequence = pointFactory.create(coordinates);
			LinearRing shell = new LinearRing(cSequence, factory);
			Polygon p = new Polygon(shell, null, factory);
			result.add(p);
		}

		MultiPolygon multiPoligon = new MultiPolygon(result.toArray(new Polygon[result.size()]), factory);

		return multiPoligon;
	}

	private List<Point> getSamplesFromPath(List<Point> pointList) {
		List<Point> result = IntStream.range(0, pointList.size()).filter(i -> i % 40 == 0)
				.mapToObj(e -> pointList.get(e)).collect(Collectors.toList());
		result.add(pointList.get(pointList.size() - 1));
		return result;
	}
}
