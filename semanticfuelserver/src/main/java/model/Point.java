package model;

import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Point {

	private Double latitude;
	private Double longitude;

	public Point(Double latitude, Double longitude) {
		super();
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	@Override
	public String toString() {
		return "Point [latitude=" + latitude + ", longitude=" + longitude + "]";
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(latitude).append(longitude).toHashCode();
	}
}
