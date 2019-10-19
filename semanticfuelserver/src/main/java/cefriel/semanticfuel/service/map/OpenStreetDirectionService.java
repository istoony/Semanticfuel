package cefriel.semanticfuel.service.map;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.jena.geosparql.implementation.jts.CustomCoordinateSequenceFactory;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateSequenceFactory;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cefriel.semanticfuel.model.DirectionRequest;
import cefriel.semanticfuel.model.DirectionResponse;
import cefriel.semanticfuel.model.Point;
import cefriel.semanticfuel.service.AbstractService;
import cefriel.semanticfuel.service.mapdao.OpenStreetDirectionDao;

@Service
public class OpenStreetDirectionService extends AbstractService {

	private static final double L = 0.0001;
	@Autowired
	private OpenStreetDirectionDao openStreetDirectionDao;

	public DirectionResponse computeResponse(DirectionRequest request) {

		DirectionResponse result = new DirectionResponse();

		List<Point> pointList = openStreetDirectionDao.server(request.getStart().getCoordinates(),
				request.getEnd().getCoordinates());

		List<Point> reducedList = reduceTheList(pointList);
		MultiPolygon rectangleList = computePoligons(reducedList);

		LOG.debug("Polygon = {}", rectangleList);
		result.setPathCoordinates(pointList);

		return result;
	}

	public MultiPolygon computePoligons(List<Point> points) {
		List<Polygon> result = new ArrayList<Polygon>();
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

	public List<Point> computeRectanglesPath(List<Point> reducedList, Double L) {
		List<Point> result = new ArrayList<Point>();
		List<Point> back = new ArrayList<Point>();
		for (int i = 1; i < reducedList.size(); i++) {
			double y1 = reducedList.get(i - 1).getLatitude();
			double x1 = reducedList.get(i - 1).getLongitude();

			double y2 = reducedList.get(i).getLatitude();
			double x2 = reducedList.get(1).getLongitude();

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

	private List<Point> reduceTheList(List<Point> pointList) {
		List<Point> result = IntStream.range(0, pointList.size()).filter(i -> i % 40 == 0)
				.mapToObj(e -> pointList.get(e)).collect(Collectors.toList());
		result.add(pointList.get(pointList.size() - 1));
		return result;
	}

	public List<Point> getDirection(Point start, Point end) {
		return openStreetDirectionDao.server(start, end);
	}

}
