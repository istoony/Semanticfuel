package cefriel.semanticfuel.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import cefriel.semanticfuel.service.map.OpenStreetDirectionService;
import cefriel.semanticfuel.service.map.OpenStreetPlacesService;
import model.Place;

@RestController
public class MapController {

	@Autowired
	private OpenStreetPlacesService openStreetPlacesService;

	@Autowired
	private OpenStreetDirectionService openStreetDirectionService;

	@GetMapping("findplace/{place}")
	public List<Place> findPlace(@PathVariable("place") String place) {
		return openStreetPlacesService.getListOfPlaces(place);
	}

}
