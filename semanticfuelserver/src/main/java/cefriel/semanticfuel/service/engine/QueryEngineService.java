package cefriel.semanticfuel.service.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;
import org.locationtech.jts.geom.Geometry;
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

	public List<GasStation> getGasStations(List<Point> path, String fuel) {
		// get the area where to search for stations
		List<Geometry> searchingArea = preprocesser.getPathArea(path);

		return getGasStaions(searchingArea, fuel);
	}

	private List<GasStation> getGasStaions(List<Geometry> area, String fuel) {
		Map<Integer, GasStation> result = new HashMap<>();

		// build the query
		Query query = new QueryBuilder().addAllTarget().buildQuery(fuel, area);

		LOG.debug("Running query: \n" + query.toString());

		try (QueryExecution qe = QueryExecutionFactory.create(query, modelManager.getCurrentModel())) {
			long queryStart = System.currentTimeMillis();

			// run the query
			ResultSet rs = qe.execSelect();

			LOG.info("Query executed in " + ((System.currentTimeMillis() - queryStart) / 1000) + " seconds");

			// parse the result, one line per pump found (possibly multiple lines for each
			// station)
			while (rs.hasNext()) {
				GasStation gs = parseGasStation(rs.next(), fuel);

				if (result.containsKey(gs.hashCode())) {
					// if the stations map already contained this station entry, just update the
					// pump list
					result.get(gs.hashCode()).addPumps(gs.getPumps());
				} else
					result.put(gs.hashCode(), gs);
			}
		}

		return new ArrayList<>(result.values());

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
		// if one of the station var is not null, then same stands for the others
		if (stationName != null) {
			builder.setName(stationName.asLiteral().getString());
			builder.setFlag(stationFlag.asLiteral().getString());
			builder.setOwner(stationOwner.asLiteral().getString());
			builder.setType(stationType.asLiteral().getString());
		}
		if (stationLat != null && stationLong != null)
			builder.setCoordinate(new Point(stationLat.asLiteral().getDouble(), stationLong.asLiteral().getDouble()));
		// if one of the address var is not null, then same stands for the others
		if (stationAddress != null)
			builder.setAddress(new Address(stationAddress.asLiteral().getString(), stationCity.asLiteral().getString(),
					stationProvince.asLiteral().getString()));
		// if one of the pump var is not null, then same stands for the others
		if (stationPumpService != null)
			builder.addPump(new FuelPump(fuel, stationPumpFuelPrice.asLiteral().getDouble(),
					stationPumpService.asLiteral().getBoolean()));

		return builder.build();
	}
}
