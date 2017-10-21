import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;

public class Vehicle {
	private static HashSet<Vehicle> vehicles = new HashSet<>();
	private String VIN, stockNumber, entryDate, owner;
	private static final ArrayList<String> errors = new ArrayList<>();

	private Vehicle(String VIN, String stockNumber, String entryDate, String owner) {
		this.VIN = VIN.toUpperCase();
		this.stockNumber = stockNumber;
		this.entryDate = entryDate;
		this.owner = owner.toUpperCase();
	}

	private static void addVehicle(String data) {
		data = simplify(data);
		String[] datum = data.split(",");
		if (datum[0].equals("VIN NUMBER")) return;
		verifyData(data);
		Vehicle v;
		if (datum.length == 4) {
			if (datum[0].length() <= 8)
				v = new Vehicle(datum[0], datum[0], datum[2], datum[3]);
			else
				v = new Vehicle(datum[0], last8(datum[0]), datum[2], datum[3]);
		} else
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
					this.entryDate.equals(v.entryDate) &&
					this.owner.equals(v.owner);
		}
		return false;
	}

	private static void verifyData(String data) {
		String[] datum = data.split(",");
		if (datum[0].equals("\"VIN NUMBER\"")) return;
		if (datum.length >= 4) {
			String VIN = datum[0];
			String date = datum[2];
			String owner = datum[3];
			if (date.contains("2015") || date.contains("2016")) return;
			try {
				DateFormat df = new SimpleDateFormat("mm/dd/yyyy");
				if (VIN.length() != 17)
					addError("Incorrect VIN length (Expected 17, found " + VIN.length() + ")", datum);
				if (owner.equals(""))
					addError("Owner not specified", datum);
				try {
					Date d = df.parse(date);
					if (d.before(df.parse("1/1/2015")))
						addError("Date is likely incorrect as it is before 2015", datum);
				} catch (ParseException e) {
					addError("Date is incorrectly formatted", datum);
				}
			} catch (ArrayIndexOutOfBoundsException e) {
				e.printStackTrace();
				System.out.printf("%s,%s,%s", VIN, date, owner);
			}
		} else {
			addError("Missing Data (most likely Owner field is empty)", datum);
		}
	}

	private static String simplify(String s) {
		return s.replace("\"", "").toUpperCase();
	}

	private static void addError(String s, String[] data) {
		try {
			errors.add(s + "==>" + Arrays.toString(data) + "\n\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String last8(String s) {
		return s.substring(s.length() - 8, s.length());
	}

	static void exportErrors() {
		try {
			FileWriter out = new FileWriter(new File("MergeLogErrors.txt"));
			errors.sort(String::compareTo);
			for (String s : errors)
				out.write(s);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
