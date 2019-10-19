package cefriel.semanticfuel.model;

public class DirectionRequest {
	private Place start;
	private Place end;
	private String fuel;

	public String getFuel() {
		return fuel;
	}

	public void setFuel(String fuel) {
		this.fuel = fuel;
	}

	public Place getStart() {
		return start;
	}

	public void setStart(Place start) {
		this.start = start;
	}

	public Place getEnd() {
		return end;
	}

	public void setEnd(Place end) {
		this.end = end;
	}
}
