package cefriel.semanticfuel.service.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.jline.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cefriel.semanticfuel.service.AbstractService;
import cefriel.semanticfuel.service.fetcher.ModelKeeperService;
import model.Address;
import model.FuelPump;
import model.GasStation;
import model.GasStation.StationBuilder;
import model.Point;

@Service
public class QueryEngineService extends AbstractService {
	@Autowired
	private ModelKeeperService modelManager;

	public List<GasStation> getGasStations(List<Point> path, String fuel) {
		ParameterizedSparqlString paramString = new ParameterizedSparqlString();
		paramString.setCommandText("PREFIX gso:  <http://gas_station.example.com/data#> "
				+ " PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ " PREFIX geo: <http://www.opengis.net/ont/geosparql#> "
				+ " PREFIX geof: <http://www.opengis.net/def/function/geosparql/> "
				+ " SELECT ?name ?owner ?flag ?type ?address ?province ?price ?fuel ?isSelf ?city ?lot ?lang"
				+ " WHERE{?station geo:hasGeometry ?geom . " + " ?geom geo:asWKT ?wkt . "
				+ " ?station gso:has_pump ?pump . ?pump gso:fuel ?fuelParam . ?station gso:name ?name . "
				+ " ?station gso:has_address ?address . ?address gso:city ?city ."
				+ " FILTER(geof:sfWithin(?wkt, ?poliParam" + "^^geo:wktLiteral))}");
		final String fuelParam = "fuelParam";
		final String poliParam = "poliParam";

		paramString.setLiteral(fuelParam, fuel);
		paramString.setLiteral(poliParam,
				"<http://www.opengis.net/def/crs/OGC/1.3/CRS84>Polygon ((45.381781 8.941661, 45.590982 8.990870, 45.549157 9.355141, 45.367502 9.382289, 45.381781 8.941661))");

		Log.debug("Running query: " + paramString.toString());

		Map<Integer, GasStation> gasStations = new HashMap<>();

		try (QueryExecution qe = QueryExecutionFactory.create(paramString.asQuery(), modelManager.getCurrentModel())) {
			ResultSet rs = qe.execSelect();

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
