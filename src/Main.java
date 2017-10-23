import javax.swing.*;

public class Main {
	public static void main(String[] args) {
		FilePicker window = new FilePicker();
		window.setSize(500,200);
		window.setTitle("VIN List Merger");
		window.setVisible(true);
	}

	static void showError(String s) {
		System.out.println(s);
		JDialog d = new Error(s);
		d.pack();
		d.setVisible(true);
		System.exit(0);
	}
}
