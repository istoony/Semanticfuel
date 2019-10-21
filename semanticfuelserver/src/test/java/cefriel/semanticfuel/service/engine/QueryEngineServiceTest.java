package cefriel.semanticfuel.service.engine;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.geosparql.implementation.jts.CustomCoordinateSequenceFactory;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateSequenceFactory;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.util.GeometricShapeFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cefriel.semanticfuel.model.GasStation;
import cefriel.semanticfuel.model.Point;
import cefriel.semanticfuel.service.mapdao.OpenStreetDirectionDao;
import cefriel.semanticfuel.utils.AbstractTest;

public class QueryEngineServiceTest extends AbstractTest {
	@Autowired
	private QueryEngineService engine;

	@Autowired
	private OpenStreetDirectionDao directionService;

	
	@Test
	public void getGasStationsFromPolygon() {
		GeometryFactory gf = new GeometryFactory();
		CoordinateSequenceFactory pointFactory = new CustomCoordinateSequenceFactory();

		Coordinate c1 = new Coordinate(45.436265, 8.350394);
		Coordinate c2 = new Coordinate(45.781680, 9.123766);
		Coordinate c3 = new Coordinate(45.626680, 10.374884);
		Coordinate c4 = new Coordinate(45.140756, 9.521774);
		Coordinate c5 = new Coordinate(45.133265, 8.302243);

		Coordinate[] coordinates = { c1, c2, c3, c4, c5, c1 };
		CoordinateSequence cSequence = pointFactory.create(coordinates);
		LinearRing shell = new LinearRing(cSequence, gf);
		Polygon p = new Polygon(shell, null, gf);

		long testStart = System.currentTimeMillis();

		List<GasStation> stations = engine.getGasStaions(p, "Benzina");

		LOG.info("Test finished in " + ((System.currentTimeMillis() - testStart) / 1000) + " seconds, with "
				+ stations.size() + " results");

		assertTrue(!stations.isEmpty());
	}

	
	@Test
	public void getGasStationsFromMultipolygonTest() {
		// somewhere near Pertengo (Vercelli)
		Point start = new Point(45.238720, 8.432447);
		// somewhere near Brescia
		Point end = new Point(45.495881, 10.206008);

		// something about 1100 points
		List<Point> testPath = directionService.getDirection(start, end);

		List<Geometry> geomList = new ArrayList<>();

		GeometryFactory mpFactory = new GeometryFactory();
		GeometricShapeFactory shapeFactory = new GeometricShapeFactory();
		shapeFactory.setNumPoints(10);
		double diameter = 2000.0;
		// length in meters of 1° of latitude = always 111.32 km
		shapeFactory.setWidth(diameter / 111320d);

		for (Point p : testPath) {
			shapeFactory.setCentre(new Coordinate(p.getLatitude(), p.getLongitude()));
			// length in meters of 1° of longitude = 40075 km * cos( latitude ) / 360
			shapeFactory.setHeight(diameter / (40075000 * Math.cos(Math.toRadians(p.getLatitude())) / 360));

			geomList.add(shapeFactory.createEllipse());
		}

		// number of polygons per multipolygon (all of the same size, sampled from the
		// list created above)
		int[] geometrySize = new int[] { 100, 200, 300, 400, 500, 600, 700, 800, 900, 1000 };
		geometrySize = new int[] { 10 };

		for (int size : geometrySize) {
			List<Geometry> subPolygons = new ArrayList<>();

			int samplingRate = geomList.size() / size;

			// create the current test multipolygon
			for (int i = 0; i < geomList.size(); i++) {
				if (i % samplingRate == 0)
					subPolygons.add(geomList.get(i));
			}

			LOG.debug("Starting test for MP of size " + subPolygons.size() + "...");
			long testStart = System.currentTimeMillis();

			List<GasStation> stations = engine
					.getGasStaions(new MultiPolygon(subPolygons.toArray(new Polygon[0]), mpFactory), "Benzina");

			LOG.debug("Test with " + subPolygons.size() + " polygons finished in "
					+ ((System.currentTimeMillis() - testStart) / 1000) + " seconds, with " + stations.size()
					+ " results");

			assertTrue(!stations.isEmpty());
		}
	}

	@Test
	public void getGasStationsFromMultiplePolygonTest() {
		// somewhere near Pertengo (Vercelli)
		Point start = new Point(45.238720, 8.432447);
		// somewhere near Brescia
		Point end = new Point(45.495881, 10.206008);

		// something about 1100 points
		List<Point> testPath = directionService.getDirection(start, end);

		List<Geometry> geomList = new ArrayList<>();

		GeometricShapeFactory shapeFactory = new GeometricShapeFactory();
		shapeFactory.setNumPoints(10);
		double diameter = 2000.0;
		// length in meters of 1° of latitude = always 111.32 km
		shapeFactory.setWidth(diameter / 111320d);

		for (Point p : testPath) {
			shapeFactory.setCentre(new Coordinate(p.getLatitude(), p.getLongitude()));
			// length in meters of 1° of longitude = 40075 km * cos( latitude ) / 360
			shapeFactory.setHeight(diameter / (40075000 * Math.cos(Math.toRadians(p.getLatitude())) / 360));

			geomList.add(shapeFactory.createEllipse());
		}

		// number of polygons per multipolygon (all of the same size, sampled from the
		// list created above)
		int[] geometrySize = new int[] { 100, 200, 300, 400, 500, 600, 700, 800, 900, 1000 };
	//	geometrySize = new int[] { 10 };

		for (int size : geometrySize) {
			List<Geometry> subPolygons = new ArrayList<>();

			int samplingRate = geomList.size() / size;

			// create the current test multipolygon
			for (int i = 0; i < geomList.size(); i++) {
				if (i % samplingRate == 0)
					subPolygons.add(geomList.get(i));
			}

			LOG.debug("Starting test for MP of size " + subPolygons.size() + "...");
			long testStart = System.currentTimeMillis();

			Set<GasStation> stations = new HashSet<>();

			long avgRunTime = 0;
			for (Geometry g : subPolygons) {
				long subTestStart = System.currentTimeMillis();

				List<GasStation> subTestStations = engine.getGasStaions(g, "Benzina");
				stations.addAll(subTestStations);

				long subTestTime = System.currentTimeMillis() - subTestStart;
				LOG.debug("Subtest executed in " + (subTestTime / 1000) + " seconds");

				avgRunTime += subTestTime;
			}
			avgRunTime = avgRunTime / subPolygons.size();

			LOG.debug("Test with " + subPolygons.size() + " polygons finished in "
					+ ((System.currentTimeMillis() - testStart) / 1000) + " seconds (" + (avgRunTime / 1000)
					+ " seconds on average), with " + stations.size() + " results");

			assertTrue(!stations.isEmpty());
		}
	}
}
