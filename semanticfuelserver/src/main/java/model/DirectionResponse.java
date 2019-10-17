package model;

import java.util.List;

public class DirectionResponse {

	private List<Point> pathCoordinates;
	private List<GasStation> gasStation;

	public List<Point> getPathCoordinates() {
		return pathCoordinates;
	}

	public void setPathCoordinates(List<Point> pathCoordinates) {
		this.pathCoordinates = pathCoordinates;
	}

	public List<GasStation> getGasStation() {
		return gasStation;
	}

	public void setGasStation(List<GasStation> gasStation) {
		this.gasStation = gasStation;
	}

}
