package cefriel.semanticfuel.model;

public class Place {

	private String name;
	private String region;
	private String country;
	private Point coordinates;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public Point getCoordinates() {
		return coordinates;
	}

	public void setCoordinates(Point coordinates) {
		this.coordinates = coordinates;
	}

	@Override
	public String toString() {
		return "Place [name=" + name + ", region=" + region + ", country=" + country + ", coordinates=" + coordinates
				+ "]";
	}
}
