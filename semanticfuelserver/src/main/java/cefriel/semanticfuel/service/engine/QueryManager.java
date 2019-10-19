package cefriel.semanticfuel.service.engine;

import javax.annotation.PostConstruct;

import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.Query;
import org.apache.jena.shared.PrefixMapping;
import org.eclipse.rdf4j.model.vocabulary.GEO;
import org.eclipse.rdf4j.model.vocabulary.GEOF;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.opengis.geometry.Geometry;
import org.springframework.stereotype.Service;

import cefriel.semanticfuel.service.AbstractService;

@Service
public class QueryManager extends AbstractService {
	private static final String PREFIX_GEO = "geo";
	private static final String PREFIX_RDF = "rdf";
	private static final String PREFIX_GSO = "gso";
	private static final String PREFIX_GEOF = "geof";
	private static final String NS_GEO = GEO.NAMESPACE;
	private static final String NS_RDF = RDF.NAMESPACE;
	private static final String NS_GSO = "http://gas_station.example.com/data#";
	private static final String NS_GEOF = GEOF.NAMESPACE;

	private static final String QUERY_TARGET_STATION_NAME = "name";
	private static final String QUERY_TARGET_STATION_OWNER = "owner";
	private static final String QUERY_TARGET_STATION_FLAG = "flag";
	private static final String QUERY_TARGET_STATION_TYPE = "type";
	private static final String QUERY_TARGET_STATION_ADDRESS = "address";
	private static final String QUERY_TARGET_STATION_CITY = "city";
	private static final String QUERY_TARGET_STATION_PROVINCE = "province";
	private static final String QUERY_TARGET_STATION_PRICE = "price";
	private static final String QUERY_TARGET_STATION_SERVICE = "isself";
	private static final String QUERY_TARGET_STATION_LAT = "lat";
	private static final String QUERY_TARGET_STATION_LONG = "long";

	private static final String QUERY_PARAM_FUEL = "fuelParam";
	private static final String QUERY_PARAM_AREA = "areaParam";

	private static final String QUERY_VAR_STATION = "?station";
	private static final String QUERY_VAR_GEOM = "?geom";
	private static final String QUERY_VAR_WKT = "?wkt";
	private static final String QUERY_VAR_ADDRESS = "?fullAddress";
	private static final String QUERY_VAR_PUMP = "?pump";
	private static final String QUERY_VAR_LOCATION = "?location";

	private ParameterizedSparqlString paramString;

	@PostConstruct
	public void init() {
		PrefixMapping pm = PrefixMapping.Factory.create();
		pm.setNsPrefix(PREFIX_GEO, NS_GEO);
		pm.setNsPrefix(PREFIX_GEOF, NS_GEOF);
		pm.setNsPrefix(PREFIX_GSO, NS_GSO);
		pm.setNsPrefix(PREFIX_RDF, NS_RDF);

		// target params
		String query = "SELECT ?" + QUERY_TARGET_STATION_NAME + " ?" + QUERY_TARGET_STATION_ADDRESS + " ?"
				+ QUERY_TARGET_STATION_CITY + " ?" + QUERY_TARGET_STATION_FLAG + " ?" + QUERY_TARGET_STATION_LAT + " ?"
				+ QUERY_TARGET_STATION_LONG + " ?" + QUERY_TARGET_STATION_OWNER + " ?" + QUERY_TARGET_STATION_OWNER
				+ " ?" + QUERY_TARGET_STATION_PRICE + " ?" + QUERY_TARGET_STATION_PROVINCE + " ?"
				+ QUERY_TARGET_STATION_SERVICE + " ?" + QUERY_TARGET_STATION_TYPE;
		// clauses
		query += " WHERE {";
		// station attributes
		query += QUERY_VAR_STATION + " gso:name ?" + QUERY_TARGET_STATION_NAME + " . " + QUERY_VAR_STATION
				+ " gso:owner ?" + QUERY_TARGET_STATION_OWNER + " . " + QUERY_VAR_STATION + " gso:type ?"
				+ QUERY_TARGET_STATION_TYPE + " . " + QUERY_VAR_STATION + " gso:flag ?" + QUERY_TARGET_STATION_FLAG;
		// station address
		query += " . " + QUERY_VAR_STATION + " gso:has_address " + QUERY_VAR_ADDRESS + " . " + QUERY_VAR_ADDRESS
				+ " gso:address ?" + QUERY_TARGET_STATION_ADDRESS + " . " + QUERY_VAR_ADDRESS + " gso:city ?"
				+ QUERY_TARGET_STATION_CITY + " . " + QUERY_VAR_ADDRESS + " gso:prov ?" + QUERY_TARGET_STATION_PROVINCE;
		// station pumps
		query += " . " + QUERY_VAR_STATION + " gso:has_pump " + QUERY_VAR_PUMP + " . " + QUERY_VAR_PUMP + " gso:fuel ?"
				+ QUERY_PARAM_FUEL + " . " + QUERY_VAR_PUMP + " gso:is_self ?" + QUERY_TARGET_STATION_SERVICE + " . "
				+ QUERY_VAR_PUMP + " gso:price ?" + QUERY_TARGET_STATION_PRICE;
		// station location
		query += " . " + QUERY_VAR_STATION + " wgs84_pos:location " + QUERY_VAR_LOCATION + " . " + QUERY_VAR_LOCATION
				+ " wgs84_pos:lat ?" + QUERY_TARGET_STATION_LAT + " . " + QUERY_VAR_LOCATION + " wgs84_pos:long ?"
				+ QUERY_TARGET_STATION_LONG;
		// station geometry
		query += " . " + QUERY_VAR_STATION + " geo:hasGeometry " + QUERY_VAR_GEOM + " . " + QUERY_VAR_GEOM
				+ " geo:asWKT " + QUERY_VAR_WKT;
		query += " . FILTER(geof:sfWithin(" + QUERY_VAR_WKT + ", ?" + QUERY_PARAM_AREA + "^^geo:wktLiteral))}";

		paramString.setNsPrefixes(pm);
		paramString.setCommandText(query);

		paramString.setCommandText("PREFIX gso:  <http://gas_station.example.com/data#> "
				+ " PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ " PREFIX geo: <http://www.opengis.net/ont/geosparql#> "
				+ " PREFIX geof: <http://www.opengis.net/def/function/geosparql/> "
				+ " SELECT ?name ?owner ?flag ?type ?address ?province ?price ?fuel ?isSelf ?city ?lot ?lang"
				+ " WHERE{?station geo:hasGeometry ?geom . " + " ?geom geo:asWKT ?wkt . "
				+ " ?station gso:has_pump ?pump . ?pump gso:fuel ?fuelParam . ?station gso:name ?name . "
				+ " ?station gso:has_address ?fullAddress . ?address gso:city ?city ."
				+ " FILTER(geof:sfWithin(?wkt, ?poliParam" + "^^geo:wktLiteral))}");
	}

	public Query buildQuery(String fuel, Geometry target) {
		paramString.setLiteral(QUERY_PARAM_FUEL, fuel);
		paramString.setLiteral(QUERY_PARAM_AREA, "<http://www.opengis.net/def/crs/OGC/1.3/CRS84>" + target.toString());

		return paramString.asQuery();
	}
}
