package cefriel.semanticfuel.service.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.jena.geosparql.implementation.jts.CustomCoordinateSequenceFactory;
import org.apache.jena.query.Query;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateSequenceFactory;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import cefriel.semanticfuel.utils.AbstractTest;

public class QueryBuilderTest extends AbstractTest {

	@Test
	public void minimalQueryTest() {
		Query query = new QueryBuilder().buildQuery();

		String targetQuery = " PREFIX gso: <http://gas_station.example.com/data#> " + "SELECT ?name"
				+ " WHERE { ?station gso:name ?name } ";

		String comparableQuery = query.toString().replaceAll("\\s+", "");
		String comparableTargetQuery = targetQuery.replaceAll("\\s+", "");

		assertEquals(comparableQuery, comparableTargetQuery);
	}

	@Test
	public void fuelGeomQueryTest() {
		CoordinateSequenceFactory pointFactory = new CustomCoordinateSequenceFactory();
		Coordinate[] coordinates = { new Coordinate(1, 1) };
		CoordinateSequence cSequence = pointFactory.create(coordinates);
		Point p = new Point(cSequence, new GeometryFactory());

		Query query = new QueryBuilder().buildQuery("Benzina", p);

		String targetQuery = "PREFIX geo: <http://www.opengis.net/ont/geosparql#> PREFIX gso: <http://gas_station.example.com/data#> PREFIX geof: <http://www.opengis.net/def/function/geosparql/> "
				+ "SELECT ?name"
				+ " WHERE { ?station gso:has_pump ?pump . ?pump gso:fuel \"Benzina\" . ?station geo:hasGeometry ?geom . ?geom geo:asWKT ?wkt . ?station gso:name ?name"
				+ " FILTER geof:sfWithin(?wkt, \"<http://www.opengis.net/def/crs/OGC/1.3/CRS84>POINT (1 1)\"^^geo:wktLiteral) }";

		String comparableQuery = query.toString().replaceAll("\\s+", "");
		String comparableTargetQuery = targetQuery.replaceAll("\\s+", "");

		assertEquals(comparableQuery, comparableTargetQuery);
	}

	@Test
	public void fuelQueryTest() {
		Query query = new QueryBuilder().buildQuery("Benzina");

		String targetQuery = " PREFIX gso: <http://gas_station.example.com/data#> " + "SELECT ?name"
				+ " WHERE { ?station gso:has_pump ?pump . ?pump gso:fuel \"Benzina\" . ?station gso:name ?name } ";

		String comparableQuery = query.toString().replaceAll("\\s+", "");
		String comparableTargetQuery = targetQuery.replaceAll("\\s+", "");

		assertEquals(comparableQuery, comparableTargetQuery);
	}

	@Test
	public void areaQueryTest() {
		CoordinateSequenceFactory pointFactory = new CustomCoordinateSequenceFactory();
		Coordinate[] coordinates = { new Coordinate(1, 1) };
		CoordinateSequence cSequence = pointFactory.create(coordinates);
		Point p = new Point(cSequence, new GeometryFactory());

		Query query = new QueryBuilder().buildQuery(p);

		String targetQuery = "PREFIX geo: <http://www.opengis.net/ont/geosparql#> PREFIX gso: <http://gas_station.example.com/data#> PREFIX geof: <http://www.opengis.net/def/function/geosparql/> SELECT ?name"
				+ " WHERE { ?station geo:hasGeometry ?geom . ?geom geo:asWKT ?wkt . ?station gso:name ?name"
				+ " FILTER geof:sfWithin(?wkt, \"<http://www.opengis.net/def/crs/OGC/1.3/CRS84>POINT (1 1)\"^^geo:wktLiteral) } ";

		String comparableQuery = query.toString().replaceAll("\\s+", "");
		String comparableTargetQuery = targetQuery.replaceAll("\\s+", "");

		assertEquals(comparableQuery, comparableTargetQuery);
	}

	@Test
	public void locationQueryTest() {
		Query query = new QueryBuilder().addTarget(QueryBuilder.QUERY_TARGET_STATION_LAT)
				.addTarget(QueryBuilder.QUERY_TARGET_STATION_LONG).buildQuery();

		String targetQuery = " PREFIX wgs84_pos: <http://www.w3.org/2003/01/geo/wgs84_pos#>" + " SELECT ?lat ?long"
				+ " WHERE { ?station wgs84_pos:location ?location . ?location wgs84_pos:lat ?lat . ?station wgs84_pos:location ?location . ?location wgs84_pos:long ?long } ";

		String comparableQuery = query.toString().replaceAll("\\s+", "");
		String comparableTargetQuery = targetQuery.replaceAll("\\s+", "");

		assertEquals(comparableQuery, comparableTargetQuery);
	}

	@Test
	public void addressQueryTest() {
		Query query = new QueryBuilder().addTarget(QueryBuilder.QUERY_TARGET_STATION_CITY).buildQuery();

		String targetQuery = "PREFIX gso: <http://gas_station.example.com/data#> " + "SELECT ?city "
				+ "WHERE { ?station gso:has_address ?fullAddress . ?fullAddress gso:city ?city } ";

		String comparableQuery = query.toString().replaceAll("\\s+", "");
		String comparableTargetQuery = targetQuery.replaceAll("\\s+", "");

		assertEquals(comparableQuery, comparableTargetQuery);
	}
};