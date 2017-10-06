import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;

public class FilePicker extends JDialog {
	private JPanel contentPane;
	private JButton buttonMerge;
	private JButton buttonCancel;
	private JButton buttonOpenFile;
	private JTextArea textAreaFile;
	private File[] files;

	FilePicker() {
		setContentPane(contentPane);
		setModal(true);
		getRootPane().setDefaultButton(buttonMerge);
		buttonMerge.addActionListener(e -> onMerge());
		buttonCancel.addActionListener(e -> onCancel());
		// call onCancel() when cross is clicked
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				onCancel();
			}
		});
		// call onCancel() on ESCAPE
		contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

		buttonOpenFile.addActionListener(e -> pickFiles());
	}

	private void pickFiles() {
		JFileChooser picker = new JFileChooser("\\\\vip-fs1\\Users\\Filing");
		picker.setDialogTitle("Choose files to merge");
		picker.setMultiSelectionEnabled(true);
		picker.setFileFilter(new FileNameExtensionFilter("csv files (*.csv)", "csv"));
		picker.showOpenDialog(this);
		files = picker.getSelectedFiles();
		if (files.length < 2) Main.showError("Must select at least 2 files");
		StringBuilder s = new StringBuilder();
		for (File f : files) {
			s.append(f.getPath()).append("\n");
		}
		textAreaFile.setText(s.toString());
	}

	private void onMerge() {
		if (files.length < 2) Main.showError("Must select at least 2 files");
		HashSet<Vehicle> vehicles = Vehicle.merge(files);
		JFileChooser picker = new JFileChooser("\\\\vip-fs1\\Users\\Filing");
		picker.setDialogTitle("Choose output file");
		picker.setMultiSelectionEnabled(false);
		picker.setFileFilter(new FileNameExtensionFilter("csv files (*.csv)", "csv"));
		picker.showOpenDialog(this);
		try (FileWriter fw = new FileWriter(picker.getSelectedFile() + ".csv")) {
			StringBuilder s = new StringBuilder();
			for (Vehicle v : vehicles)
				s.append(v.toString());
			fw.write(s.toString());
		} catch (IOException e) {
			Main.showError(e.getMessage());
		}
		dispose();
	}

	private void onCancel() {
		dispose();
	}
}
