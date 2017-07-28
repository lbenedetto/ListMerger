import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashSet;

public class Vehicle {
	private static HashSet<Vehicle> vehicles = new HashSet<>();
	private String VIN, StockNumber, EntryDate, Owner;

	private Vehicle(String VIN, String stockNumber, String entryDate, String owner) {
		this.VIN = VIN;
		StockNumber = stockNumber;
		EntryDate = entryDate;
		Owner = owner;
	}

	private static void addVehicle(String data) {
		String[] datum = data.split(",");
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
		return String.format("%s,%s,%s,%s\r\n", VIN, StockNumber, EntryDate, Owner);
	}

	@Override
	public int hashCode() {
		return this.VIN.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Vehicle && this.VIN.equals((((Vehicle) obj).VIN));
	}
}
