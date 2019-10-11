package cefriel.semanticfuel.service.mapdao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import cefriel.semanticfuel.service.AbstractService;
import model.Place;
import model.Point;

@Service
public class OpenStreetPlacesDao extends AbstractService {

	private static final String PLACE_GET_URL = "https://api.openrouteservice.org/geocode/search?api_key={key}&text= {place}";

	public List<Place> getPlace(String request) {
		String json = performRestRequest(request);

		List<Place> result = parseJson(json);

		return result;

	}

	private List<Place> parseJson(String json) {
		JsonParser parser = JsonParserFactory.getJsonParser();
		Map<String, Object> pl1 = parser.parseMap(json);

		List<Place> result = new ArrayList<Place>();
		for (Map<String, Object> feature : (List<Map<String, Object>>) pl1.get("features")) {
			Place p = new Place();
			p.setCountry(((Map<String, String>) feature.get("properties")).get("country"));
			p.setRegion(((Map<String, String>) feature.get("properties")).get("region"));
			p.setName(((Map<String, String>) feature.get("properties")).get("name"));
			List<Double> coordinates = ((Map<String, List<Double>>) feature.get("geometry")).get("coordinates");
			Point coor = new Point(coordinates.get(0), coordinates.get(1));
			p.setCoordinates(coor);
			result.add(p);
		}
		return result;
	}

	private String performRestRequest(String place) {
		RestTemplate restTemplate = new RestTemplate();
		String result = restTemplate.getForObject(PLACE_GET_URL, String.class, KEY, place);

		return result;
	}

}
