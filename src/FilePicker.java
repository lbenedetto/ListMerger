import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FilePicker extends JFrame {
	private JPanel contentPane;
	private JButton buttonMerge;
	private JButton buttonCancel;
	private JButton buttonOpenFile;
	private JTextArea textAreaFile;
	private File[] files;

	FilePicker() {
		setContentPane(contentPane);
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
		files = pickFile("Choose files to merge", true);
		if (files.length < 2) Main.showError("Must select at least 2 files");
		StringBuilder s = new StringBuilder();
		for (File f : files) {
			s.append(f.getPath()).append("\n");
		}
		textAreaFile.setText(s.toString());
	}

	private void onMerge() {
		if (files.length < 2) Main.showError("Must select at least 2 files");
		Vehicle[] vehicles = Vehicle.merge(files);
		File outfile = pickFile("Choose output file", false)[0];
		//Write the vehicles to the output file
		try (FileWriter fw = new FileWriter(outfile)) {
			StringBuilder s = new StringBuilder();
			for (Vehicle v : vehicles)
				s.append(v.toString());
			fw.write(s.toString());
		} catch (IOException e) {
			Main.showError(e.getMessage());
		}
		Vehicle.exportErrors();
		dispose();
	}

	private File[] pickFile(String title, boolean multi) {
		//Get start dir
		File dir = new File("\\\\vip-fs1\\Users");
		FileSystemView fsv = FileSystemView.getFileSystemView();
		dir = fsv.getParentDirectory(dir);
		dir = fsv.getChild(dir, "Users");
		dir = fsv.getChild(dir, "vipemp");
		dir = fsv.getChild(dir, "Filing");

		JFileChooser picker = new JFileChooser(dir);
		picker.setDialogTitle(title);
		picker.setMultiSelectionEnabled(multi);
		picker.setFileFilter(new FileNameExtensionFilter("text files (*.txt)", "txt"));
		picker.showOpenDialog(this);
		if (multi)
			return picker.getSelectedFiles();
		else
			return new File[]{picker.getSelectedFile()};
	}

	private void onCancel() {
		dispose();
	}
}
