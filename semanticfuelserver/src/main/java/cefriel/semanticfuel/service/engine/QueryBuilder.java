package cefriel.semanticfuel.service.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.Query;
import org.apache.jena.shared.PrefixMapping;
import org.eclipse.rdf4j.model.vocabulary.GEO;
import org.eclipse.rdf4j.model.vocabulary.GEOF;
import org.locationtech.jts.geom.Geometry;

public class QueryBuilder {
	private static final String PREFIX_GEO = "geo";
	private static final String PREFIX_GSO = "gso";
	private static final String PREFIX_GEOF = "geof";
	private static final String PREFIX_WGS84 = "wgs84_pos";
	private static final String NS_GEO = GEO.NAMESPACE;
	private static final String NS_GSO = "http://gas_station.example.com/data#";
	private static final String NS_GEOF = GEOF.NAMESPACE;
	private static final String NS_WGS84 = "http://www.w3.org/2003/01/geo/wgs84_pos#";

	protected static final String QUERY_TARGET_STATION_NAME = "name";
	protected static final String QUERY_TARGET_STATION_OWNER = "owner";
	protected static final String QUERY_TARGET_STATION_FLAG = "flag";
	protected static final String QUERY_TARGET_STATION_TYPE = "type";
	protected static final String QUERY_TARGET_STATION_ADDRESS = "address";
	protected static final String QUERY_TARGET_STATION_CITY = "city";
	protected static final String QUERY_TARGET_STATION_PROVINCE = "province";
	protected static final String QUERY_TARGET_FUEL_PRICE = "price";
	protected static final String QUERY_TARGET_PUMP_TOS = "isself";
	protected static final String QUERY_TARGET_STATION_LAT = "lat";
	protected static final String QUERY_TARGET_STATION_LONG = "long";

	protected static final String QUERY_TARGET_GROUP_STATION = "station";
	protected static final String QUERY_TARGET_GROUP_LOCATION = "location";
	protected static final String QUERY_TARGET_GROUP_ADDRESS = "address";
	protected static final String QUERY_TARGET_GROUP_PUMP = "pump";

	private static final String QUERY_PARAM_AREA = "geomParam";
	private static final String QUERY_PARAM_FUEL = "fuelParam";

	private static final String QUERY_VAR_STATION = "?station";
	private static final String QUERY_VAR_GEOM = "?geom";
	private static final String QUERY_VAR_WKT = "?wkt";
	private static final String QUERY_VAR_ADDRESS = "?fullAddress";
	private static final String QUERY_VAR_PUMP = "?pump";
	private static final String QUERY_VAR_LOCATION = "?location";

	private ParameterizedSparqlString paramString;

	private Set<String> targets;
	private Map<WhereClause, Integer> clauses;
	private Map<WhereFilter, Integer> filters;
	private PrefixMapping prefixes;
	private Map<String, Geometry> geomParams;

	public QueryBuilder() {
		targets = new HashSet<>();
		clauses = new HashMap<>();
		filters = new HashMap<>();
		prefixes = PrefixMapping.Factory.create();
		geomParams = new HashMap<>();

		paramString = new ParameterizedSparqlString();
	}

	public Query buildQuery() {
		String fuel = null;
		return buildQuery(fuel);
	}

	public Query buildQuery(String fuel) {
		List<Geometry> list = null;
		return buildQuery(fuel, list);
	}

	public Query buildQuery(Geometry geom) {
		return buildQuery(null, geom);
	}

	public Query buildQuery(List<Geometry> geoms) {
		return buildQuery(null, geoms);
	}

	public Query buildQuery(String fuel, Geometry geom) {
		List<Geometry> geoms = new ArrayList<>();
		geoms.add(geom);
		return buildQuery(fuel, geoms);
	}

	public Query buildQuery(String fuel, List<Geometry> target) {
		if (fuel != null) {
			// add the part of the query dealing with fuel parameter
			addNamespace(PREFIX_GSO, NS_GSO);
			addWhereClause(QUERY_VAR_STATION, "gso:has_pump", QUERY_VAR_PUMP);
			addWhereClause(QUERY_VAR_PUMP, "gso:fuel", "?" + QUERY_PARAM_FUEL);
		}

		if (target != null) {
			// add the part of the query dealing with geometry parameter
			addNamespace(PREFIX_GEO, NS_GEO);
			addNamespace(PREFIX_GEOF, NS_GEOF);
			addWhereClause(QUERY_VAR_STATION, "geo:hasGeometry", QUERY_VAR_GEOM);
			addWhereClause(QUERY_VAR_GEOM, "geo:asWKT", QUERY_VAR_WKT);

			for (Geometry geom : target) {
				addWhereFilter("geof:sfWithin", QUERY_VAR_WKT, "?" + QUERY_PARAM_AREA + "^^geo:wktLiteral");
				geomParams.put(QUERY_PARAM_AREA + geomParams.size(), geom);
			}
		}

		// select at least the attributes of the stations, if no target is specifieds
		if (targets.isEmpty())
			addTargetGroup(QUERY_TARGET_GROUP_STATION);

		// build the parameterized command
		String command = buildCommand();

		paramString.setNsPrefixes(prefixes);
		paramString.setCommandText(command);
		if (fuel != null)
			paramString.setLiteral(QUERY_PARAM_FUEL, fuel);
		if (target != null)
			for (String geomParam : geomParams.keySet())
				paramString.setLiteral(geomParam,
						"<http://www.opengis.net/def/crs/OGC/1.3/CRS84>" + geomParams.get(geomParam).toString());

		return paramString.asQuery();
	}

	public QueryBuilder addAllTarget() {
		addTargetGroup(QUERY_TARGET_GROUP_STATION);
		addTargetGroup(QUERY_TARGET_GROUP_ADDRESS);
		addTargetGroup(QUERY_TARGET_GROUP_LOCATION);
		addTargetGroup(QUERY_TARGET_GROUP_PUMP);

		return this;
	}

	public QueryBuilder addTargetGroup(String group) {
		switch (group) {
		case QUERY_TARGET_GROUP_ADDRESS:
			addTarget(QUERY_TARGET_STATION_ADDRESS);
			addTarget(QUERY_TARGET_STATION_CITY);
			addTarget(QUERY_TARGET_STATION_PROVINCE);
			break;
		case QUERY_TARGET_GROUP_LOCATION:
			addTarget(QUERY_TARGET_STATION_LAT);
			addTarget(QUERY_TARGET_STATION_LONG);
			break;
		case QUERY_TARGET_GROUP_PUMP:
			addTarget(QUERY_TARGET_PUMP_TOS);
			addTarget(QUERY_TARGET_FUEL_PRICE);
			break;
		case QUERY_TARGET_GROUP_STATION:
			addTarget(QUERY_TARGET_STATION_NAME);
			addTarget(QUERY_TARGET_STATION_OWNER);
			addTarget(QUERY_TARGET_STATION_FLAG);
			addTarget(QUERY_TARGET_STATION_TYPE);
		}

		return this;
	}

	private QueryBuilder addTarget(String target) {
		switch (target) {
		case QUERY_TARGET_STATION_NAME:
			addNamespace(PREFIX_GSO, NS_GSO);
			addWhereClause(QUERY_VAR_STATION, "gso:name", "?" + QUERY_TARGET_STATION_NAME);
			break;
		case QUERY_TARGET_STATION_OWNER:
			addNamespace(PREFIX_GSO, NS_GSO);
			addWhereClause(QUERY_VAR_STATION, "gso:owner", "?" + QUERY_TARGET_STATION_OWNER);
			break;
		case QUERY_TARGET_STATION_TYPE:
			addNamespace(PREFIX_GSO, NS_GSO);
			addWhereClause(QUERY_VAR_STATION, "gso:type", "?" + QUERY_TARGET_STATION_TYPE);
			break;
		case QUERY_TARGET_STATION_FLAG:
			addNamespace(PREFIX_GSO, NS_GSO);
			addWhereClause(QUERY_VAR_STATION, "gso:flag", "?" + QUERY_TARGET_STATION_FLAG);
			break;
		case QUERY_TARGET_FUEL_PRICE:
			addNamespace(PREFIX_GSO, NS_GSO);
			addWhereClause(QUERY_VAR_STATION, "gso:has_pump", QUERY_VAR_PUMP);
			addWhereClause(QUERY_VAR_PUMP, "gso:price", "?" + QUERY_TARGET_FUEL_PRICE);
			break;
		case QUERY_TARGET_PUMP_TOS:
			addNamespace(PREFIX_GSO, NS_GSO);
			addWhereClause(QUERY_VAR_STATION, "gso:has_pump", QUERY_VAR_PUMP);
			addWhereClause(QUERY_VAR_PUMP, "gso:is_self", "?" + QUERY_TARGET_PUMP_TOS);
			break;
		case QUERY_TARGET_STATION_ADDRESS:
			addNamespace(PREFIX_GSO, NS_GSO);
			addWhereClause(QUERY_VAR_STATION, "gso:has_address", QUERY_VAR_ADDRESS);
			addWhereClause(QUERY_VAR_ADDRESS, "gso:address", "?" + QUERY_TARGET_STATION_ADDRESS);
			break;
		case QUERY_TARGET_STATION_CITY:
			addNamespace(PREFIX_GSO, NS_GSO);
			addWhereClause(QUERY_VAR_STATION, "gso:has_address", QUERY_VAR_ADDRESS);
			addWhereClause(QUERY_VAR_ADDRESS, "gso:city", "?" + QUERY_TARGET_STATION_CITY);
			break;
		case QUERY_TARGET_STATION_PROVINCE:
			addNamespace(PREFIX_GSO, NS_GSO);
			addWhereClause(QUERY_VAR_STATION, "gso:has_address", QUERY_VAR_ADDRESS);
			addWhereClause(QUERY_VAR_ADDRESS, "gso:prov", "?" + QUERY_TARGET_STATION_PROVINCE);
			break;
		case QUERY_TARGET_STATION_LAT:
			addNamespace(PREFIX_WGS84, NS_WGS84);
			addWhereClause(QUERY_VAR_STATION, "wgs84_pos:location", QUERY_VAR_LOCATION);
			addWhereClause(QUERY_VAR_LOCATION, "wgs84_pos:lat", "?" + QUERY_TARGET_STATION_LAT);
			break;
		case QUERY_TARGET_STATION_LONG:
			addNamespace(PREFIX_WGS84, NS_WGS84);
			addWhereClause(QUERY_VAR_STATION, "wgs84_pos:location", QUERY_VAR_LOCATION);
			addWhereClause(QUERY_VAR_LOCATION, "wgs84_pos:long", "?" + QUERY_TARGET_STATION_LONG);
			break;
		default:
			return this;
		}

		targets.add(target);

		return this;
	}

	private String buildCommand() {
		String query = "SELECT ";
		for (String target : targets)
			query += "?" + target + " ";

		List<WhereClause> clausesList = clauses.entrySet().stream().sorted(Map.Entry.comparingByValue())
				.map(Map.Entry::getKey).collect(Collectors.toList());

		query += " WHERE {";
		for (int i = 0; i < clausesList.size(); i++) {
			WhereClause wc = clausesList.get(i);
			query += wc.subject + " " + wc.property + " " + wc.object + (i == clausesList.size() - 1 ? "" : " . ");
		}

		if (!filters.isEmpty()) {
			query += " . FILTER (";

			List<WhereFilter> filtersList = filters.entrySet().stream().sorted(Map.Entry.comparingByValue())
					.map(Map.Entry::getKey).collect(Collectors.toList());

			for (int i = 0; i < filtersList.size(); i++) {
				WhereFilter wf = filtersList.get(i);
				query += wf.function + " (";
				for (int j = 0; j < wf.params.length; j++)
					query += wf.params[j] + (j == wf.params.length - 1 ? "" : ",");
				query += ")" + (i == filtersList.size() - 1 ? ")" : " || ");
			}
		}

		query += "}";

		return query;
	}

	private void addNamespace(String prefix, String ns) {
		prefixes.setNsPrefix(prefix, ns);
	}

	private void addWhereClause(String s, String p, String o) {
		WhereClause wc = new WhereClause(s, p, o);
		if (!clauses.containsKey(wc))
			clauses.put(wc, clauses.size());
	}

	private void addWhereFilter(String function, String... params) {
		WhereFilter wf = new WhereFilter(function, params);
		if (!filters.containsKey(wf))
			filters.put(wf, filters.size());
	}

	class WhereClause {
		private String subject;
		private String property;
		private String object;

		public WhereClause(String s, String p, String o) {
			this.subject = s;
			this.property = p;
			this.object = o;
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder().append(subject).append(property).append(object).toHashCode();
		}
	}

	class WhereFilter {
		private String function;
		private String[] params;

		public WhereFilter(String function, String... params) {
			this.function = function;
			this.params = params;
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder().append(function).append(params).toHashCode();
		}
	}
}
