import java.io.*;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Vehicle implements Comparable<Vehicle> {
	private static HashSet<Vehicle> vehicles = new HashSet<>();
	private String VIN, entryDate, owner, notes;
	private static final ArrayList<String> errors = new ArrayList<>();
	private static final Pattern fourComma = Pattern.compile("(^[^,]*,[^,]*,[^,]*,[^,]*)");
	private static final Pattern dataPattern = Pattern.compile("(^[^,]+)?,([^,]*)?,([^,]*)?,([^,]*)?$");
	private static final int FIELDS = 4;

	private Vehicle(String[] datum) {
		this.VIN = clean(datum[0]);
		this.entryDate = clean(datum[1]);
		this.owner = clean(datum[2]);
		this.notes = clean(datum[3]);
	}

	private static void addVehicle(String data) {
		data = simplify(data);
		String[] datum = data.split(",");
		if (datum[0].equals("VIN NUMBER")) return;
		Vehicle v;
		if (datum.length != FIELDS) {
			//The formatting of the file is broken, but we can't risk losing any data
			Matcher m = dataPattern.matcher(data);
			datum = new String[FIELDS];
			if (m.find()) {
				for (int i = 0; i < m.groupCount(); i++) {
					datum[i] = m.group(i + 1);
				}
			}
		}
		v = new Vehicle(datum);
		if (vehicles.add(v)) {
			v.verifyData();
		}
	}

	private static String clean(String s) {
		return fixEmpty(s).toUpperCase().trim();
	}

	private static String fixEmpty(String s) {
		return s == null || s.isEmpty() ? "???" : s;
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
		return String.format("%-17s,\t%-10s,\t%-45s,\t%s\r\n", VIN, entryDate, owner, notes);
	}

	@Override
	public int hashCode() {
		return this.VIN.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Vehicle) {
			Vehicle v = (Vehicle) obj;
			if (VIN.equals(v.VIN) &&
					equalsIgnoreUnknown(entryDate, v.entryDate) &&
					equalsIgnoreUnknown(owner, v.owner)) {
				//Merge wildcard data
				if (!entryDate.equals("???") ^ !v.entryDate.equals("???")) {
					entryDate = entryDate.equals("???") ? v.entryDate : entryDate;
					v.entryDate = v.entryDate.equals("???") ? entryDate : v.entryDate;
				}
				if (!owner.equals("???") ^ !v.owner.equals("???")) {
					owner = owner.equals("???") ? v.owner : owner;
					v.owner = v.owner.equals("???") ? owner : v.owner;
				}
				if (!notes.equals("???") ^ !v.notes.equals("???")) {
					notes = notes.equals("???") ? v.notes : notes;
					v.notes = v.notes.equals("???") ? notes : v.notes;
				}
				return true;
			}
		}
		return false;
	}

	private static boolean equalsIgnoreUnknown(String s1, String s2) {
		//They are the same if either one is a wildcard
		return s1.equals("???") || s2.equals("???") || s1.equals(s2);
	}

	private void verifyData() {
		String[] datum = new String[]{VIN, entryDate, owner};
		try {
			DateFormat df = new SimpleDateFormat("mm/dd/yyyy");
			if (VIN.length() != 17)
				addError("Incorrect VIN length (Expected 17, found " + VIN.length() + ")", datum);
			if (owner.equals(""))
				addError("Owner not specified", datum);
			try {
				if (entryDate.equals("???")) {
					Date d = df.parse(entryDate);
					if (d.before(df.parse("1/1/2015")))
						addError("Date is likely incorrect as it is before 2015", datum);
				}
			} catch (ParseException e) {
				addError("Date is incorrectly formatted", datum);
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
			System.out.printf("%s,%s,%s", VIN, entryDate, owner);
		}

	}

	private static String simplify(String s) {
		s = s.toUpperCase()
				.replace("\"", "")
				.replaceAll("\t", "")
				.trim();
		Matcher m = fourComma.matcher(s);
		if (m.find()) {
			s = m.group(1);
		}
		return s;
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
			out.write("===========Begin Log===========\r\n");
			String date = new SimpleDateFormat("MM.dd.yyyy").format(new Timestamp(System.currentTimeMillis()));
			out.write("===========" + date + "===========\r\n");

			for (String s : errors)
				out.write(s);
			out.write("===========END LOG===========\r\n");
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
		try {
			int[] is1 = Arrays.stream(s1.split("/")).mapToInt(x -> Integer.parseInt(x.trim())).toArray();
			int[] is2 = Arrays.stream(s2.split("/")).mapToInt(x -> Integer.parseInt(x.trim())).toArray();
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
		} catch (NumberFormatException e) {
			System.out.println(s1 + ":::" + s2);
		}
		return s1.compareTo(s2);
	}
}
