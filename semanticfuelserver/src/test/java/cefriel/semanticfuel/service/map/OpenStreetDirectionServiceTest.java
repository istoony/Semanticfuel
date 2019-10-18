package cefriel.semanticfuel.service.map;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.MultiPolygon;
import org.springframework.beans.factory.annotation.Autowired;

import cefriel.semanticfuel.utils.AbstractTest;
import model.Point;

public class OpenStreetDirectionServiceTest extends AbstractTest {

	@Autowired
	private OpenStreetDirectionService openStreetDirectionService;

	@Test
	public void makeARectangleTest() {
		Point a = new Point(1.0, 1.0);
		Point b = new Point(3.0, 1.0);
		Point c= new Point(3.0, 5.0);
		List<Point> request = new ArrayList<Point>();
		request.add(a);
		request.add(b);
		request.add(c);

		MultiPolygon result = openStreetDirectionService.computePoligons(request);

		LOG.debug("result = {}", result);

	}

}
