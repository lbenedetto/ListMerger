import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;

public class Vehicle implements Comparable<Vehicle> {
	private static HashSet<Vehicle> vehicles = new HashSet<>();
	private String VIN, entryDate, owner;
	private static final ArrayList<String> errors = new ArrayList<>();

	private Vehicle(String VIN, String entryDate, String owner) {
		this.VIN = VIN.toUpperCase();
		this.entryDate = entryDate;
		this.owner = owner.toUpperCase();
	}

	private static void addVehicle(String data) {
		data = simplify(data);
		verifyData(data);
		String[] datum = data.split(",");
		if (datum[0].equals("VIN NUMBER")) return;
		Vehicle v;
		if (datum.length == 3) {
			v = new Vehicle(datum[0], datum[1], datum[2]);
		} else
			//The formatting of the file is broken, but we can't risk losing any data
			v = new Vehicle(data, "", "");
		vehicles.add(v);
	}

	static Vehicle[] merge(File... files) {
		try {
			for (File file : files)
				new BufferedReader(new FileReader(file)).lines().forEach(Vehicle::addVehicle);
		} catch (FileNotFoundException e) {
			Main.showError(e.getMessage());
		}
		Vehicle[] v = Vehicle.vehicles.toArray(new Vehicle[vehicles.size()]);
		Arrays.sort(v);
		return v;
	}

	@Override
	public String toString() {
		return String.format("%-17s,\t%-10s,\t%s\r\n", VIN, entryDate, owner);
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
		return s
				.replace("\"", "")
				.toUpperCase()
				.replaceAll("BILL BUNCH CHEVROLET, INC", "BILL BUNCH CHEVROLET")
				.replaceAll("\t", "")
				.trim();
	}

	private static void addError(String s, String[] data) {
		try {
			errors.add(s + "==>" + Arrays.toString(data) + "\n\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
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

	@Override
	public int compareTo(Vehicle o) {
		if (entryDate.equals(o.entryDate)) {
			if (owner.equals(o.owner)) {
				return VIN.compareTo(o.VIN);
			}
			return owner.compareTo(o.owner);
		}
		return compareDateString(entryDate, o.entryDate);
	}

	private int compareDateString(String s1, String s2) {
		int[] is1 = Arrays.stream(s1.split("/")).mapToInt(Integer::parseInt).toArray();
		int[] is2 = Arrays.stream(s2.split("/")).mapToInt(Integer::parseInt).toArray();
		if (is1.length == 3 && is2.length == 3) {
			int year = is1[2] - is2[2];
			if (year == 0) {
				int month = is1[0] - is2[0];
				if (month == 0)
					return is1[1] - is2[1];
				return month;
			}
			return year;
		}
		return 0;
	}
}
