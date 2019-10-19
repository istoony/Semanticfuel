package cefriel.semanticfuel.model;

public class FuelPump {
	private String fuel;
	private boolean isSelf;
	private double price;

	public FuelPump(String fuel, double price, boolean isSelf) {
		this.price = price;
		this.fuel = fuel;
		this.isSelf = isSelf;
	}

	public String getFuel() {
		return fuel;
	}

	public void setFuel(String fuel) {
		this.fuel = fuel;
	}

	public boolean isSelf() {
		return isSelf;
	}

	public void setSelf(boolean isSelf) {
		this.isSelf = isSelf;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}
}
