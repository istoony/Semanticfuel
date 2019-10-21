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
	private QueryManager queryManager;

	@Autowired
	private PathProcessor preprocesser;

	public List<GasStation> getGasStations(List<Point> path, String fuel) {
		// get the area where to search for stations
		Geometry searchingArea = preprocesser.getPathArea(path);

		return getGasStaions(searchingArea, fuel);
	}

	public List<GasStation> getGasStaions(Geometry area, String fuel) {
		// build the query
		Query query = queryManager.buildQuery(fuel, area);

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
		LOG.debug("Result: {}", gasStations);
		return new ArrayList<>(gasStations.values());

	}

	private GasStation parseGasStation(QuerySolution queryRaw, String fuel) {
		String stationName = queryRaw.get(QueryManager.QUERY_TARGET_STATION_NAME).asLiteral().getString();
		String stationOwner = queryRaw.get(QueryManager.QUERY_TARGET_STATION_OWNER).asLiteral().getString();
		String stationType = queryRaw.get(QueryManager.QUERY_TARGET_STATION_TYPE).asLiteral().getString();
		String stationFlag = queryRaw.get(QueryManager.QUERY_TARGET_STATION_FLAG).asLiteral().getString();
		String stationAddress = queryRaw.get(QueryManager.QUERY_TARGET_STATION_ADDRESS).asLiteral().getString();
		String stationCity = queryRaw.get(QueryManager.QUERY_TARGET_STATION_CITY).asLiteral().getString();
		String stationProvince = queryRaw.get(QueryManager.QUERY_TARGET_STATION_PROVINCE).asLiteral().getString();
		double stationPumpFuelPrice = queryRaw.get(QueryManager.QUERY_TARGET_FUEL_PRICE).asLiteral().getDouble();
		boolean stationPumpService = queryRaw.get(QueryManager.QUERY_TARGET_PUMP_TOS).asLiteral().getBoolean();
		double stationLat = queryRaw.get(QueryManager.QUERY_TARGET_STATION_LAT).asLiteral().getDouble();
		double stationLong = queryRaw.get(QueryManager.QUERY_TARGET_STATION_LONG).asLiteral().getDouble();

		StationBuilder builder = new StationBuilder();
		builder.setName(stationName).setFlag(stationFlag).setOwner(stationOwner).setType(stationType)
				.setCoordinate(new Point(stationLat, stationLong))
				.setAddress(new Address(stationAddress, stationCity, stationProvince))
				.addPump(new FuelPump(fuel, stationPumpFuelPrice, stationPumpService));
		return builder.build();
	}
}
