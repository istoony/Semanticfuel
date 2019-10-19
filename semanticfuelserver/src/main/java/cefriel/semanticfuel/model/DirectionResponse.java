package cefriel.semanticfuel.model;

import java.util.List;

public class DirectionResponse {
	private List<Point> pathCoordinates;
	private List<GasStation> gasStations;

	public List<Point> getPathCoordinates() {
		return pathCoordinates;
	}

	public void setPathCoordinates(List<Point> pathCoordinates) {
		this.pathCoordinates = pathCoordinates;
	}

	public List<GasStation> getGasStations() {
		return gasStations;
	}

	public void setGasStations(List<GasStation> gasStations) {
		this.gasStations = gasStations;
	}
}
