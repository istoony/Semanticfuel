package cefriel.semanticfuel.service.engine;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import cefriel.semanticfuel.service.map.OpenStreetDirectionService;
import cefriel.semanticfuel.service.map.OpenStreetPlacesService;
import cefriel.semanticfuel.utils.AbstractTest;
import model.DirectionRequest;
import model.DirectionResponse;
import model.Place;

public class QueryEngineServiceTest extends AbstractTest {
	@Autowired
	private QueryEngineService engine;

	@Autowired
	private OpenStreetPlacesService openStreetPlacesService;

	@Autowired
	private OpenStreetDirectionService openStreetDirectionService;

	//@Test
	public void updateOntology() {
		List<Place> startList = openStreetPlacesService.getListOfPlaces("Via Manzoni, Lecco");
		LOG.debug("Start List = {}", startList);
		Place start = startList.get(0);

		List<Place> endListList = openStreetPlacesService.getListOfPlaces("Duomo di Milano, Milano");
		LOG.debug("End List = {}", endListList);
		Place end = endListList.get(0);

		DirectionRequest request = new DirectionRequest();
		request.setStart(start);
		request.setEnd(end);
		request.setFuel("Benzina");

		DirectionResponse response = openStreetDirectionService.computeResponse(request);

		assertNotNull(response.getGasStation());
		assertTrue(response.getPathCoordinates().size() > 100);

		assertNotNull(response.getGasStation());
		assertTrue(response.getGasStation().size() > 5);

	}
};