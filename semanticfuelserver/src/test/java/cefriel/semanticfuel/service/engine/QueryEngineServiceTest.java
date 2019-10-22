package cefriel.semanticfuel.service.engine;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import cefriel.semanticfuel.model.GasStation;
import cefriel.semanticfuel.model.Point;
import cefriel.semanticfuel.service.mapdao.OpenStreetDirectionDao;
import cefriel.semanticfuel.utils.AbstractTest;

public class QueryEngineServiceTest extends AbstractTest {
	@Autowired
	private QueryEngineService engine;

	@Autowired
	private OpenStreetDirectionDao directionService;

	@Test
	public void getGasStationsFromMultiplePolygonTest() {
		// somewhere near Pertengo (Vercelli)
		Point start = new Point(45.238720, 8.432447);
		// somewhere near Brescia
		Point end = new Point(45.495881, 10.206008);

		// something about 1100 points
		List<Point> testPath = directionService.getDirection(start, end);
		List<GasStation> result = engine.getGasStations(testPath, "Benzina");

		assertTrue(result.size() > 10);
	}
}
