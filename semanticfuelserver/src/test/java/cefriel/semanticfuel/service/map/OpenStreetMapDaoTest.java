package cefriel.semanticfuel.service.map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import cefriel.semanticfuel.service.mapdao.OpenStreetDirectionDao;
import cefriel.semanticfuel.service.mapdao.OpenStreetPlacesDao;
import cefriel.semanticfuel.utils.AbstractTest;
import model.Place;
import model.Point;

public class OpenStreetMapDaoTest extends AbstractTest {

	@Autowired
	private OpenStreetDirectionDao openStreatDirectionDao;
	
	@Autowired
	private OpenStreetPlacesDao openStreetPlacesDao;

	//@Test
	public void getRequestTest() {
		Point start = new Point(8.681495,49.41461);
		Point end = new Point(9.687872,39.420318);
		List<Point> result = openStreatDirectionDao.server(start, end);
		for (Point point : result) {
			LOG.debug("Point found {}", point);
		}
		assertTrue(result.size() > 100);
	}
	
	@Test
	public void getPlacesTest() {
		List<Place> result = openStreetPlacesDao.getPlace("Venice");
		LOG.debug("Places Found = {}", result);
		assertNotNull(result.size());
	}
	
	@Test
	public void DoubleTest() {
		String s = "8.681496";
		Double d = Double.valueOf(s);
		Double expected = 8.681496;
		assertEquals(expected, d);
	}

}
