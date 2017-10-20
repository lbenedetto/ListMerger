import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashSet;

public class Vehicle {
	private static HashSet<Vehicle> vehicles = new HashSet<>();
	private String VIN, stockNumber, entryDate, owner;

	private Vehicle(String VIN, String stockNumber, String entryDate, String owner) {
		this.VIN = VIN.toUpperCase();
		this.stockNumber = stockNumber.toUpperCase();
		this.entryDate = entryDate;
		this.owner = owner.toUpperCase();
	}

	private static void addVehicle(String data) {
		String[] datum = data.split(",");
		if (datum[0].equals("\"VIN NUMBER\"")) return;
		Vehicle v;
		if (datum.length == 4)
			v = new Vehicle(datum[0], datum[1], datum[2], datum[3]);
		else
			//The formatting of the file is broken, but we can't risk losing any data
			v = new Vehicle(data, "", "", "");
		vehicles.add(v);
	}

	static HashSet<Vehicle> merge(File... files) {
		try {
			for (File file : files)
				new BufferedReader(new FileReader(file)).lines().forEach(Vehicle::addVehicle);
		} catch (FileNotFoundException e) {
			Main.showError(e.getMessage());
		}
		return Vehicle.vehicles;
	}

	@Override
	public String toString() {
		return String.format("%s,%s,%s,%s\r\n", VIN, stockNumber, entryDate, owner);
	}

	@Override
	public int hashCode() {
		return this.VIN.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Vehicle) {
			Vehicle v = (Vehicle) obj;
			return this.VIN.equals(v.VIN) &&
					this.stockNumber.equals(v.stockNumber) &&
					this.entryDate.equals(v.entryDate) &&
					this.owner.equals(v.owner);
		}
		return false;

	}
}
