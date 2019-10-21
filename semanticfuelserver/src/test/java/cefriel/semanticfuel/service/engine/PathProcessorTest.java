package cefriel.semanticfuel.service.engine;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;

import cefriel.semanticfuel.model.GasStation;
import cefriel.semanticfuel.model.Point;
import cefriel.semanticfuel.service.map.OpenStreetDirectionService;
import cefriel.semanticfuel.utils.AbstractTest;


public class PathProcessorTest extends AbstractTest{
	
	@Autowired
	private PathProcessor pathProcessor;

	@Autowired
	private OpenStreetDirectionService directionService;
	
	@Autowired
	private QueryEngineService queryEngine;
	
	@Test
	public void pathProcessorTest() {
		// somewhere near Pertengo (Vercelli)
		Point start = new Point(45.238720, 8.432447);
		// somewhere near Brescia
		Point end = new Point(45.495881, 10.206008);

		// something about 1100 points
		List<Point> testPath = directionService.getDirection(start, end);
		Geometry geometry = pathProcessor.getPathArea(testPath);
		LOG.debug("Geometry = {}", geometry);
		
		List<GasStation> result = queryEngine.getGasStaions(geometry, "Benzina");
		LOG.debug("Result = {}", result);
		
		
	}

}
