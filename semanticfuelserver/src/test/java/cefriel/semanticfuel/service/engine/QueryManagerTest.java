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
import org.springframework.beans.factory.annotation.Autowired;

import cefriel.semanticfuel.utils.AbstractTest;

public class QueryManagerTest extends AbstractTest {
	@Autowired
	private QueryManager queryManager;

	@Test
	public void buildQueryTest() {
		CoordinateSequenceFactory pointFactory = new CustomCoordinateSequenceFactory();
		Coordinate[] coordinates = { new Coordinate(1, 1) };
		CoordinateSequence cSequence = pointFactory.create(coordinates);
		Point p = new Point(cSequence, new GeometryFactory());

		Query query = queryManager.buildQuery("Benzina", p);

		String targetQuery = "PREFIX  geo:  <http://www.opengis.net/ont/geosparql#>\n"
				+ "PREFIX  gso:  <http://gas_station.example.com/data#>\n"
				+ "PREFIX  wgs84_pos: <http://www.w3.org/2003/01/geo/wgs84_pos#>\n"
				+ "PREFIX  geof: <http://www.opengis.net/def/function/geosparql/>\n" + "PREFIX  bif:  <bif:>\n"
				+ "SELECT  ?name ?address ?city ?flag ?lat ?long ?owner ?price ?province ?isself ?type ?fuel\n"
				+ "WHERE\n" + "{ ?station  gso:name         ?name ;\n" + "            gso:owner        ?owner ;\n"
				+ "           gso:type         ?type ;\n" + "          gso:flag         ?flag ;\n"
				+ "          gso:has_address  ?fullAddress .\n" + "?fullAddress  gso:address  ?address ;\n"
				+ "         gso:city         ?city ;\n" + "         gso:prov         ?province .\n"
				+ "?station  gso:has_pump     ?pump .\n" + "?pump     gso:fuel         ?fuel ;\n"
				+ "          gso:is_self      ?isself ;\n" + "          gso:price        ?price\n"
				+ "FILTER bif:contains(?fuel, \"Benzina\")\n" + "?station  wgs84_pos:location  ?location .\n"
				+ "?location  wgs84_pos:lat      ?lat ;\n" + "         wgs84_pos:long      ?long .\n"
				+ "?station  geo:hasGeometry     ?geom .\n" + "?geom     geo:asWKT           ?wkt\n"
				+ "FILTER geof:sfWithin(?wkt, \"<http://www.opengis.net/def/crs/OGC/1.3/CRS84>POINT (1 1)\"^^geo:wktLiteral)\n"
				+ "}";

		String comparableQuery = query.toString().replaceAll("\\s+", "");
		String comparableTargetQuery = targetQuery.replaceAll("\\s+", "");

		assertEquals(comparableQuery, comparableTargetQuery);
	}
};