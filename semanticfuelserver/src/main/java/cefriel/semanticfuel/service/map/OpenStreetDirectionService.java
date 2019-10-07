package cefriel.semanticfuel.service.map;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cefriel.semanticfuel.service.mapdao.OpenStreetDirectionDao;
import model.Point;

@Service
public class OpenStreetDirectionService {
	
	@Autowired
	private OpenStreetDirectionDao openStreetDirectionDao;
	
	public List<Point> getDirection(Point start, Point end) {
		return openStreetDirectionDao.server(start, end);
	}

}
