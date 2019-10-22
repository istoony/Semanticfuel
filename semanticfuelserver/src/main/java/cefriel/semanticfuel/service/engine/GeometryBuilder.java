package cefriel.semanticfuel.service.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.util.GeometricShapeFactory;
import org.springframework.stereotype.Service;

import cefriel.semanticfuel.model.Point;
import cefriel.semanticfuel.service.AbstractService;

@Service
public class GeometryBuilder extends AbstractService {
	private GeometryFactory gf;

	@PostConstruct
	public void init() {
		gf = new GeometryFactory();
	}

	public Polygon createCircle(int npoints, Point center, double diameter) {
		GeometricShapeFactory shapeFactory = new GeometricShapeFactory();
		shapeFactory.setNumPoints(npoints);
		shapeFactory.setCentre(new Coordinate(center.getLatitude(), center.getLongitude()));
		shapeFactory.setHeight(diameter);
		shapeFactory.setWidth(diameter);

		return shapeFactory.createCircle();
	}

	public List<MultiPolygon> createMultyPoligons(List<Polygon> polygons, int maxsize) {
		int n = polygons.size() / maxsize;

		Map<Integer, List<Polygon>> multyPolygonMap = new HashMap<>();

		for (int i = 0; i < polygons.size(); i++) {
			int listIndex = i % n;

			List<Polygon> list = multyPolygonMap.get(listIndex);
			if (list == null)
				multyPolygonMap.put(listIndex, list = new ArrayList<>());
			list.add(polygons.get(i));
		}

		List<MultiPolygon> result = new ArrayList<>();

		for (List<Polygon> list : multyPolygonMap.values())
			result.add(new MultiPolygon(list.toArray(new Polygon[0]), gf));

		return result;
	}
}
