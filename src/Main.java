import javax.swing.*;

public class Main {
	public static void main(String[] args) {
		FilePicker dialog = new FilePicker();
		dialog.setTitle("VIN List Merger");
		dialog.pack();
		dialog.setVisible(true);
		System.exit(0);
	}

	static void showError(String s) {
		JDialog d = new Error(s);
		d.pack();
		d.setVisible(true);
		System.exit(0);
	}
}
