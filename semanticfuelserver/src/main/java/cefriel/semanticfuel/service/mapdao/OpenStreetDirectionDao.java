package cefriel.semanticfuel.service.mapdao;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import cefriel.semanticfuel.service.AbstractService;
import model.Point;

@Service
public class OpenStreetDirectionDao extends AbstractService {
	private static final String DIRECTION_GET_URL = "https://api.openrouteservice.org/v2/directions/driving-car?api_key={key}&start={start}&end={end}";

	public List<Point> server(Point start, Point end) {
		String result = performRestRequest(start, end);
		List<Point> points = parseJson(result);

		return points;
	}

	private List<Point> parseJson(String result) {
		JsonParser parser = JsonParserFactory.getJsonParser();
		Map<String, Object> map1 = parser.parseMap(result);

		Map<String, Object> geometry = (Map<String, Object>) ((Map<String, Object>) ((List<Object>) map1
				.get("features")).get(0)).get("geometry");

		List<Point> points = ((List<List<Double>>) geometry.get("coordinates")).stream()
				.map(x -> new Point(x.get(1), x.get(0))).collect(Collectors.toList());
		return points;
	}

	private String performRestRequest(Point start, Point end) {
		RestTemplate restTemplate = new RestTemplate();
		String result = restTemplate.getForObject(DIRECTION_GET_URL, String.class, KEY, printPoint(start),
				printPoint(end));

		return result;
	}

	private String printPoint(Point start) {
		return start.getLongitude() + "," + start.getLatitude();
	}
}
