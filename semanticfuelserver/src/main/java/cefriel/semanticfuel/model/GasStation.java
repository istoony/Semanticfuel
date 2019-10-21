package cefriel.semanticfuel.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.HashCodeBuilder;

public class GasStation {
	private List<FuelPump> pumps;
	private Point coordinate;
	private Address address;
	private String name;
	private String owner;
	private String flag;
	private String type;

	private GasStation(StationBuilder builder) {
		this.pumps = builder.pumps;
		this.address = builder.address;
		this.coordinate = builder.coordinate;
		this.name = builder.name;
		this.owner = builder.owner;
		this.flag = builder.flag;
		this.type = builder.type;
	}

	public List<FuelPump> getPumps() {
		return pumps;
	}

	public void addPumps(List<FuelPump> pumps) {
		this.pumps.addAll(pumps);
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

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
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

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(name).append(coordinate).toHashCode();
	}

	@Override
	public String toString() {
		return "GasStation [pumps=" + pumps + ", coordinate=" + coordinate + ", address=" + address + ", name=" + name
				+ ", owner=" + owner + ", flag=" + flag + ", type=" + type + "]";
	}

	public static class StationBuilder {
		private List<FuelPump> pumps;
		private Point coordinate;
		private Address address;
		private String name;
		private String owner;
		private String flag;
		private String type;

		public StationBuilder() {
			pumps = new ArrayList<>();
		}

		public GasStation build() {
			return new GasStation(this);
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

		public StationBuilder setType(String type) {
			this.type = type;
			return this;
		}
	}
}
