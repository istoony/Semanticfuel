package cefriel.semanticfuel.service.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cefriel.semanticfuel.model.Address;
import cefriel.semanticfuel.model.FuelPump;
import cefriel.semanticfuel.model.GasStation;
import cefriel.semanticfuel.model.GasStation.StationBuilder;
import cefriel.semanticfuel.model.Point;
import cefriel.semanticfuel.service.AbstractService;
import cefriel.semanticfuel.service.fetcher.ModelKeeperService;

@Service
public class QueryEngineService extends AbstractService {
	@Autowired
	private ModelKeeperService modelManager;

	@Autowired
	private PathProcessor preprocesser;

	@Autowired
	private GeometryBuilder geometryBuilder;

	public List<GasStation> getGasStations(List<Point> path, String fuel) {
		// get the area where to search for stations
		List<Polygon> searchingArea = preprocesser.getPathArea(path);

		return getGasStaions(searchingArea, fuel);
	}

	public List<GasStation> getGasStaions(List<Polygon> area, String fuel) {
		Set<GasStation> result = new HashSet<>();

		List<MultiPolygon> geometries = geometryBuilder.createMultyPoligons(area, 3);

		for (Geometry geom : geometries) {
			// build the query
			Query query = new QueryBuilder().addAllTarget().buildQuery(fuel, geom);

			LOG.debug("Running query: \n" + query.toString());

			Map<Integer, GasStation> gasStations = new HashMap<>();
			try (QueryExecution qe = QueryExecutionFactory.create(query, modelManager.getCurrentModel())) {
				long queryStart = System.currentTimeMillis();

				// run the query
				ResultSet rs = qe.execSelect();

				LOG.info("Query executed in " + ((System.currentTimeMillis() - queryStart) / 1000) + " seconds");

				// parse the result, one line per pump found (possibly multiple lines for each
				// station)
				while (rs.hasNext()) {
					GasStation gs = parseGasStation(rs.next(), fuel);

					if (gasStations.containsKey(gs.hashCode()))
						// if the stations map already contained this station entry, just update the
						// pump list
						gasStations.get(gs.hashCode()).addPumps(gs.getPumps());
					else
						gasStations.put(gs.hashCode(), gs);
				}
			}

			result.addAll(gasStations.values());
		}

		return new ArrayList<>(result);

	}

	private GasStation parseGasStation(QuerySolution queryRaw, String fuel) {
		RDFNode stationName = queryRaw.get(QueryBuilder.QUERY_TARGET_STATION_NAME);
		RDFNode stationOwner = queryRaw.get(QueryBuilder.QUERY_TARGET_STATION_OWNER);
		RDFNode stationType = queryRaw.get(QueryBuilder.QUERY_TARGET_STATION_TYPE);
		RDFNode stationFlag = queryRaw.get(QueryBuilder.QUERY_TARGET_STATION_FLAG);
		RDFNode stationAddress = queryRaw.get(QueryBuilder.QUERY_TARGET_STATION_ADDRESS);
		RDFNode stationCity = queryRaw.get(QueryBuilder.QUERY_TARGET_STATION_CITY);
		RDFNode stationProvince = queryRaw.get(QueryBuilder.QUERY_TARGET_STATION_PROVINCE);
		RDFNode stationPumpFuelPrice = queryRaw.get(QueryBuilder.QUERY_TARGET_FUEL_PRICE);
		RDFNode stationPumpService = queryRaw.get(QueryBuilder.QUERY_TARGET_PUMP_TOS);
		RDFNode stationLat = queryRaw.get(QueryBuilder.QUERY_TARGET_STATION_LAT);
		RDFNode stationLong = queryRaw.get(QueryBuilder.QUERY_TARGET_STATION_LONG);

		StationBuilder builder = new StationBuilder();
		if (stationName != null)
			builder.setName(stationName.asLiteral().getString());
		if (stationFlag != null)
			builder.setFlag(stationFlag.asLiteral().getString());
		if (stationOwner != null)
			builder.setOwner(stationOwner.asLiteral().getString());
		if (stationType != null)
			builder.setType(stationType.asLiteral().getString());
		if (stationLat != null && stationLong != null)
			builder.setCoordinate(new Point(stationLat.asLiteral().getDouble(), stationLong.asLiteral().getDouble()));
		String address = stationAddress != null ? stationAddress.asLiteral().getString() : null;
		String province = stationProvince != null ? stationProvince.asLiteral().getString() : null;
		String city = stationCity != null ? stationCity.asLiteral().getString() : null;
		builder.setAddress(new Address(address, city, province));
		boolean service = stationPumpService != null ? stationPumpService.asLiteral().getBoolean() : false;
		double price = stationPumpFuelPrice != null ? stationPumpFuelPrice.asLiteral().getDouble() : 0;
		builder.addPump(new FuelPump(fuel, price, service));

		return builder.build();
	}
}
