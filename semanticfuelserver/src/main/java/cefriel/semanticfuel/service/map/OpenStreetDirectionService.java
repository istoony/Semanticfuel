package cefriel.semanticfuel.service.map;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cefriel.semanticfuel.service.mapdao.OpenStreetDirectionDao;
import model.DirectionRequest;
import model.DirectionResponse;
import model.Point;

@Service
public class OpenStreetDirectionService {

	@Autowired
	private OpenStreetDirectionDao openStreetDirectionDao;

	public DirectionResponse computeResponse(DirectionRequest request) {
		
		DirectionResponse result = new DirectionResponse();
		
		List<Point> pointList = openStreetDirectionDao.server(request.getStart().getCoordinates(),
				request.getEnd().getCoordinates());
		
		List<Point> reducedList = IntStream.range(0, pointList.size()).filter(i -> i % 40 == 0)
				.mapToObj(e -> pointList.get(e)).collect(Collectors.toList());
		result.setPathCoordinates(reducedList);
		
		return result;

	}

	public List<Point> getDirection(Point start, Point end) {
		return openStreetDirectionDao.server(start, end);
	}

}
