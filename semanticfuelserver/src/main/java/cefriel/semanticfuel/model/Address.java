package cefriel.semanticfuel.model;

public class Address {
	private String address;
	private String city;
	private String prov;

	public Address(String address, String city, String prov) {
		this.address = address;
		this.city = city;
		this.prov = prov;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getProv() {
		return prov;
	}

	public void setProv(String prov) {
		this.prov = prov;
	}
}
