package cefriel.semanticfuel.service.map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import cefriel.semanticfuel.model.DirectionRequest;
import cefriel.semanticfuel.model.DirectionResponse;
import cefriel.semanticfuel.model.Place;
import cefriel.semanticfuel.model.Point;
import cefriel.semanticfuel.utils.AbstractTest;

public class OpenStreetDirectionServiceTest extends AbstractTest {

	@Autowired
	private OpenStreetDirectionService openStreetDirectionService;

	@Autowired
	private OpenStreetPlacesService openStreetPlacesService;

	@Test
	public void makeARectangleTest() {
		Point a = new Point(1.0, 1.0);
		Point b = new Point(3.0, 1.0);
		Point c = new Point(3.0, 5.0);
		List<Point> request = new ArrayList<Point>();
		request.add(a);
		request.add(b);
		request.add(c);

		// MultiPolygon result = openStreetDirectionService.computePoligons(request);

		// LOG.debug("result = {}", result);

	}

	@Test
	public void computeResponseTest() {
		List<Place> startList = openStreetPlacesService.getListOfPlaces("Via Manzoni, Lecco");
		LOG.info("Start List = {}", startList);
		Place start = startList.get(0);

		List<Place> endListList = openStreetPlacesService.getListOfPlaces("Duomo di Milano, Milano");
		LOG.info("End List = {}", endListList);
		Place end = endListList.get(0);

		DirectionRequest request = new DirectionRequest();
		request.setStart(start);
		request.setEnd(end);
		request.setFuel("Benzina");

		DirectionResponse response = openStreetDirectionService.computeResponse(request);

		assertNotNull(response.getGasStations());
		assertTrue(response.getPathCoordinates().size() > 100);

		assertNotNull(response.getGasStations());
		assertTrue(response.getGasStations().size() > 5);

	}

}
