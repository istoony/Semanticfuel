package cefriel.semanticfuel.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cefriel.semanticfuel.service.map.OpenStreetDirectionService;
import cefriel.semanticfuel.service.map.OpenStreetPlacesService;
import model.DirectionRequest;
import model.DirectionResponse;
import model.Place;

@RestController
@RequestMapping("/api")
public class MapController extends AbstractController {

	@Autowired
	private OpenStreetPlacesService openStreetPlacesService;

	@Autowired
	private OpenStreetDirectionService openStreetDirectionService;

	@GetMapping("findplace/{place}")
	public List<Place> findPlace(@PathVariable("place") String place) {
		return openStreetPlacesService.getListOfPlaces(place);
	}

	@PostMapping("direction")
	public DirectionResponse direction(@RequestBody DirectionRequest request) {
		return openStreetDirectionService.computeResponse(request);
	}

}
