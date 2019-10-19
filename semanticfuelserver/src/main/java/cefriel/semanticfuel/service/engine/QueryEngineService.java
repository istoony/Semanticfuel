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
import org.jline.utils.Log;
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

		// build the query
		Query query = queryManager.buildQuery(fuel, searchingArea);

		Log.debug("Running query: " + query.toString());

		Map<Integer, GasStation> gasStations = new HashMap<>();
		try (QueryExecution qe = QueryExecutionFactory.create(query, modelManager.getCurrentModel())) {
			// run the query
			ResultSet rs = qe.execSelect();

			// parse the result, one line per pump found (possibly multiple lines for each
			// station)
			while (rs.hasNext()) {
				GasStation gs = parseGasStation(rs.next());

				if (gasStations.containsKey(gs.hashCode()))
					// if the stations map already contained this station entry, just update the
					// pump list
					gasStations.get(gs.hashCode()).addPumps(gs.getPumps());
				else
					gasStations.put(gs.hashCode(), gs);
			}
		}

		return new ArrayList<>(gasStations.values());
	}

	private GasStation parseGasStation(QuerySolution queryRaw) {
		String stationName = queryRaw.get("name").asLiteral().toString();
		String stationOwner = queryRaw.get("owner").asLiteral().toString();
		String stationType = queryRaw.get("type").asLiteral().toString();
		String stationFlag = queryRaw.get("flag").asLiteral().toString();
		String stationAddress = queryRaw.get("address").asLiteral().toString();
		String stationCity = queryRaw.get("city").asLiteral().toString();
		String stationProvince = queryRaw.get("province").asLiteral().toString();
		String stationPumpFuel = queryRaw.get("fuel").asLiteral().toString();
		String stationPumpFuelPrice = queryRaw.get("price").asLiteral().toString();
		String stationPumpService = queryRaw.get("isSelf").asLiteral().toString();
		String stationLat = queryRaw.get("long").asLiteral().toString();
		String stationLong = queryRaw.get("lat").asLiteral().toString();

		StationBuilder builder = new StationBuilder();
		builder.setName(stationName).setFlag(stationFlag).setOwner(stationOwner).setType(stationType)
				.setCoordinate(new Point(Double.parseDouble(stationLat), Double.parseDouble(stationLong)))
				.setAddress(new Address(stationAddress, stationCity, stationProvince))
				.addPump(new FuelPump(stationPumpFuel, Double.parseDouble(stationPumpFuelPrice),
						Boolean.parseBoolean(stationPumpService)));
		return builder.build();
	}
}
