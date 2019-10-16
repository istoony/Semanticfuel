package cefriel.semanticfuel.service.map;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cefriel.semanticfuel.service.mapdao.OpenStreetPlacesDao;
import model.Place;

@Service
public class OpenStreetPlacesService {

	@Autowired
	private OpenStreetPlacesDao openStreetPlacesDao;

	public List<Place> getListOfPlaces(String place) {
		List<Place> result = openStreetPlacesDao.getPlace(place);
		return result.stream().filter(e -> e.getCountry().toLowerCase().contains("italy")).collect(Collectors.toList());
	}

}
