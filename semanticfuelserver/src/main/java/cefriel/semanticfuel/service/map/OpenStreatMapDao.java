package cefriel.semanticfuel.service.map;

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
public class OpenStreatMapDao extends AbstractService {
	private static final String DIRECTION_GET_URL = "https://api.openrouteservice.org/v2/directions/driving-car?api_key={key}&start={start}&end={end}";
	private static final String KEY = "5b3ce3597851110001cf624863f9b5105fef45a2887307080631b7b5";

	public List<Point> server() {
		String result = performRestRequest();

		List<Point> points = parseJson(result);

		return points;
	}

	private List<Point> parseJson(String result) {
		JsonParser parser = JsonParserFactory.getJsonParser();
		Map<String, Object> map1 = parser.parseMap(result);

		Map<String, Object> geometry = (Map<String, Object>) ((Map<String, Object>) ((List<Object>) map1
				.get("features")).get(0)).get("geometry");

		List<Point> points = ((List<List<Double>>) geometry.get("coordinates")).stream()
				.map(x -> new Point(x.get(0), x.get(1))).collect(Collectors.toList());
		return points;
	}

	private String performRestRequest() {
		RestTemplate restTemplate = new RestTemplate();
		String result = restTemplate.getForObject(DIRECTION_GET_URL, String.class, KEY, "8.681495,49.41461",
				"8.687872,48.420318");

		return result;
	}
}
