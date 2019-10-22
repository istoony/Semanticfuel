package cefriel.semanticfuel.service.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.jena.query.Query;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;

import cefriel.semanticfuel.model.Point;
import cefriel.semanticfuel.utils.AbstractTest;

public class QueryBuilderTest extends AbstractTest {
	@Autowired
	private GeometryBuilder geometryBuilder;

	@Test
	public void minimalQueryTest() {
		Query query = new QueryBuilder().buildQuery();

		String targetQuery = " PREFIX gso: <http://gas_station.example.com/data#> " + "SELECT ?owner ?flag ?name ?type "
				+ "WHERE { ?station gso:name ?name ; gso:owner ?owner ; gso:flag ?flag ; gso:type ?type } ";

		String comparableQuery = query.toString().replaceAll("\\s+", "");
		String comparableTargetQuery = targetQuery.replaceAll("\\s+", "");

		assertEquals(comparableQuery, comparableTargetQuery);
	}

	@Test
	public void fuelGeomQueryTest() {
		Geometry geom = geometryBuilder.createPolygon(8, new Point(45.0, 10.0), 1000);

		Query query = new QueryBuilder().buildQuery("Benzina", geom);

		String targetQuery = "PREFIX geo: <http://www.opengis.net/ont/geosparql#> PREFIX gso: <http://gas_station.example.com/data#> PREFIX geof: <http://www.opengis.net/def/function/geosparql/> "
				+ "SELECT ?owner ?flag ?name ?type"
				+ " WHERE { ?station gso:has_pump ?pump . ?pump gso:fuel \"Benzina\" . ?station geo:hasGeometry ?geom . ?geom geo:asWKT ?wkt . ?station gso:name ?name ; gso:owner ?owner ; gso:flag ?flag ; gso:type ?type"
				+ " FILTER geof:sfWithin(?wkt, \"<http://www.opengis.net/def/crs/OGC/1.3/CRS84>POLYGON ((545 10, 398.5533905932738 363.5533905932737, 45.00000000000003 510, -308.5533905932737 363.5533905932738, -455 10.00000000000006, -308.55339059327383 -343.5533905932737, 44.99999999999991 -490, 398.55339059327366 -343.55339059327383, 545 10))\"^^geo:wktLiteral) } ";

		String comparableQuery = query.toString().replaceAll("\\s+", "");
		String comparableTargetQuery = targetQuery.replaceAll("\\s+", "");

		assertEquals(comparableQuery, comparableTargetQuery);
	}

	@Test
	public void fuelQueryTest() {
		Query query = new QueryBuilder().buildQuery("Benzina");

		String targetQuery = "PREFIX gso: <http://gas_station.example.com/data#> " + "SELECT ?owner ?flag ?name ?type"
				+ " WHERE { ?station gso:has_pump ?pump . ?pump gso:fuel \"Benzina\" . ?station gso:name ?name ; gso:owner ?owner ; gso:flag ?flag ; gso:type ?type } ";

		String comparableQuery = query.toString().replaceAll("\\s+", "");
		String comparableTargetQuery = targetQuery.replaceAll("\\s+", "");

		assertEquals(comparableQuery, comparableTargetQuery);
	}

	@Test
	public void areaQueryTest() {
		Geometry geom = geometryBuilder.createPolygon(8, new Point(45.0, 10.0), 1000);

		Query query = new QueryBuilder().buildQuery(geom);

		String targetQuery = "PREFIX geo: <http://www.opengis.net/ont/geosparql#> PREFIX gso: <http://gas_station.example.com/data#> PREFIX geof: <http://www.opengis.net/def/function/geosparql/> "
				+ "SELECT ?owner ?flag ?name ?type "
				+ "WHERE { ?station geo:hasGeometry ?geom . ?geom geo:asWKT ?wkt . ?station gso:name ?name ; gso:owner ?owner ; gso:flag ?flag ; gso:type ?type"
				+ " FILTER geof:sfWithin(?wkt, \"<http://www.opengis.net/def/crs/OGC/1.3/CRS84>POLYGON ((545 10, 398.5533905932738 363.5533905932737, 45.00000000000003 510, -308.5533905932737 363.5533905932738, -455 10.00000000000006, -308.55339059327383 -343.5533905932737, 44.99999999999991 -490, 398.55339059327366 -343.55339059327383, 545 10))\"^^geo:wktLiteral) }  ";

		String comparableQuery = query.toString().replaceAll("\\s+", "");
		String comparableTargetQuery = targetQuery.replaceAll("\\s+", "");

		assertEquals(comparableQuery, comparableTargetQuery);
	}

	@Test
	public void locationQueryTest() {
		Query query = new QueryBuilder().addTargetGroup(QueryBuilder.QUERY_TARGET_GROUP_LOCATION).buildQuery();

		String targetQuery = " PREFIX wgs84_pos: <http://www.w3.org/2003/01/geo/wgs84_pos#>" + " SELECT ?lat ?long"
				+ " WHERE { ?station wgs84_pos:location ?location . ?location wgs84_pos:lat ?lat . ?station wgs84_pos:location ?location . ?location wgs84_pos:long ?long } ";

		String comparableQuery = query.toString().replaceAll("\\s+", "");
		String comparableTargetQuery = targetQuery.replaceAll("\\s+", "");

		assertEquals(comparableQuery, comparableTargetQuery);
	}

	@Test
	public void addressQueryTest() {
		Query query = new QueryBuilder().addTargetGroup(QueryBuilder.QUERY_TARGET_GROUP_ADDRESS).buildQuery();

		String targetQuery = "PREFIX gso: <http://gas_station.example.com/data#>" + " SELECT ?address ?province ?city"
				+ " WHERE { ?station gso:has_address ?fullAddress . ?fullAddress gso:address ?address . ?station gso:has_address ?fullAddress . ?fullAddress gso:city ?city . ?station gso:has_address ?fullAddress . ?fullAddress gso:prov ?province } ";

		String comparableQuery = query.toString().replaceAll("\\s+", "");
		String comparableTargetQuery = targetQuery.replaceAll("\\s+", "");

		assertEquals(comparableQuery, comparableTargetQuery);
	}
};