package cefriel.semanticfuel.service.map;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cefriel.semanticfuel.model.DirectionRequest;
import cefriel.semanticfuel.model.DirectionResponse;
import cefriel.semanticfuel.model.GasStation;
import cefriel.semanticfuel.model.Point;
import cefriel.semanticfuel.service.AbstractService;
import cefriel.semanticfuel.service.engine.QueryEngineService;
import cefriel.semanticfuel.service.mapdao.OpenStreetDirectionDao;

@Service
public class OpenStreetDirectionService extends AbstractService {

	private static final double L = 0.0001;
	@Autowired
	private OpenStreetDirectionDao openStreetDirectionDao;

	@Autowired
	private QueryEngineService queryEngine;

	public DirectionResponse computeResponse(DirectionRequest request) {

		DirectionResponse result = new DirectionResponse();

		// compute the path from the start/end points contained in the request, through
		// the OpenStreet API
		List<Point> pointList = getDirection(request.getStart().getCoordinates(), request.getEnd().getCoordinates());

		// call the query engine to search for the closest gas stations near the path
		List<GasStation> stationList = queryEngine.getGasStations(pointList, request.getFuel());

		// compose the result
		result.setPathCoordinates(pointList);
		result.setGasStations(stationList);

		return result;
	}

	public List<Point> getDirection(Point start, Point end) {
		return openStreetDirectionDao.getDirection(start, end);
	}

}
