package cefriel.semanticfuel.service.map;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import cefriel.semanticfuel.utils.AbstractTest;
import model.Point;

public class OpenStreetMapDaoTest extends AbstractTest {

	@Autowired
	private OpenStreatMapDao openStreatMapDao;

	@Test
	public void getRequestTest() {
		List<Point> result = openStreatMapDao.server();
		for (Point point : result) {
			LOG.debug("Point found {}", point);
		}
	}
	
	@Test
	public void DoubleTest() {
		String s = "8.681496";
		Double d = Double.valueOf(s);
		Double expected = 8.681496;
		Assertions.assertEquals(expected, d);
	}

}
