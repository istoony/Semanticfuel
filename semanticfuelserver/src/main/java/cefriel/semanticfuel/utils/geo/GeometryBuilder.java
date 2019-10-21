package cefriel.semanticfuel.utils.geo;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.springframework.stereotype.Service;

import cefriel.semanticfuel.service.AbstractService;

@Service
public class GeometryBuilder extends AbstractService {
	private GeometryFactory gf;

	@PostConstruct
	public void init() {
		gf = new GeometryFactory();
	}

	public List<MultiPolygon> createMultyPoligons(List<Polygon> polygons, int maxsize) {
		int n = polygons.size() / maxsize;

		List<List<Polygon>> polygonLists = new ArrayList<>();
		for (int i = 0; i < n; i++) {
			polygonLists.add(new ArrayList<>());
		}

		for (int j = 0; j < maxsize; j++) {
			for (int i = 0; i < n; i++) {
				polygonLists.get(i).add(polygons.get(i * maxsize + j));
			}
		}
		if (polygons.size() % maxsize != 0) {
			for (int i = 0; i < polygons.size() % maxsize; i++) {
				polygonLists.add(new ArrayList<>());
				polygonLists.get(polygonLists.size() - 1).add(polygons.get(n * maxsize + i));
			}
		}

		List<MultiPolygon> result = new ArrayList<>();

		for (List<Polygon> list : polygonLists)
			result.add(new MultiPolygon(list.toArray(new Polygon[0]), gf));

		return result;
	}
}
