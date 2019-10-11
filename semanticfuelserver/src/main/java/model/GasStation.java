package model;

import java.util.ArrayList;
import java.util.List;

public class GasStation {
	private List<FuelPump> pumps;
	private Point coordinate;
	private Address address;
	private String name;
	private String owner;
	private String flag;

	private GasStation(StationBuilder builder) {
		this.pumps = builder.pumps;
		this.address = builder.address;
		this.coordinate = builder.coordinate;
		this.name = builder.name;
		this.owner = builder.owner;
		this.flag = builder.flag;
	}

	public List<FuelPump> getPumps() {
		return pumps;
	}

	public void setPumps(List<FuelPump> pumps) {
		this.pumps = pumps;
	}

	public Point getCoordinate() {
		return coordinate;
	}

	public void setCoordinate(Point coordinate) {
		this.coordinate = coordinate;
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}

	public static class StationBuilder {
		private List<FuelPump> pumps;
		private Point coordinate;
		private Address address;
		private String name;
		private String owner;
		private String flag;

		public StationBuilder() {
			pumps = new ArrayList<>();
		}

		public StationBuilder addPumps(List<FuelPump> pumps) {
			this.pumps.addAll(pumps);
			return this;
		}

		public StationBuilder addPump(FuelPump pump) {
			this.pumps.add(pump);
			return this;
		}

		public StationBuilder setCoordinate(Point coordinate) {
			this.coordinate = coordinate;
			return this;
		}

		public StationBuilder setAddress(Address address) {
			this.address = address;
			return this;
		}

		public StationBuilder setName(String name) {
			this.name = name;
			return this;
		}

		public StationBuilder setOwner(String owner) {
			this.owner = owner;
			return this;
		}

		public StationBuilder setFlag(String flag) {
			this.flag = flag;
			return this;
		}
	}
}
