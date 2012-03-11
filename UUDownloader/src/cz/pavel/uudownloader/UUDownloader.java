package cz.pavel.uudownloader;

import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.ProxySelector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.log4j.Logger;

import cz.pavel.uudownloader.utils.Configuration;
import cz.pavel.uudownloader.utils.CustomProxySelector;
import cz.pavel.uudownloader.utils.LogUtils;

public class UUDownloader extends JFrame implements ActionListener, Runnable {
	
	private static final Logger log = LogUtils.getLogger();
	
	private static final long serialVersionUID = 1092198808287297052L;
	
	private GridBagLayout layout = new GridBagLayout();
	
	
	private JTextField destinationFolderTF;
	private JTextField uuAccessCode1TF;
	private JTextField uuAccessCode2TF;
	private JCheckBox directoryForArtifactCB;
	private JTextArea artifactsTA;
	
	private JButton okButton;
	
	private LogDialog logDialog;
	
	
	public UUDownloader() {
		this.setLayout(layout);
		this.setTitle("UUDownloader");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		

		this.add(getLabel("UU access code 1"), getConstraints(0, 0));
		this.add(uuAccessCode1TF = getPasswordField(12), getConstraints(1, 0, true, 2));
		
		this.add(getLabel("UU access code 2"), getConstraints(0, 1));
		this.add(uuAccessCode2TF = getPasswordField(12), getConstraints(1, 1, true, 2));

		this.add(getLabel("Cílový adresář"), getConstraints(0, 2));
		this.add(destinationFolderTF = getTextField(50), getConstraints(1, 2, true, 2));

		this.add(getLabel("Adresář pro artefakt"), getConstraints(0, 3));
		this.add(directoryForArtifactCB = getCheckBox(), getConstraints(1, 3, true, 2));
		
		this.add(getLabel("Artefakty"), getConstraints(0, 4));
		GridBagConstraints artifactsConstraints = getConstraints(1, 4, true, 2);
		artifactsConstraints.fill = GridBagConstraints.BOTH;
		artifactsConstraints.weighty = 1;
		this.add(getScrollPane(artifactsTA = getTextArea("", 10, 10)), artifactsConstraints);
		
		this.add(okButton = getButton("Stáhnout"), getConstraints(0, 10, true, 3));
		
		okButton.addActionListener(this);
		
		this.getRootPane().setDefaultButton(okButton);
	}
	
	private static JScrollPane getScrollPane(JTextArea textArea) {
		return new JScrollPane(textArea);
	}
	
	private static JTextArea getTextArea(String text, int columns, int rows) {
		JTextArea textArea = new JTextArea(rows, columns);
		textArea.setFont((new JTextField()).getFont()); // use the same font as for textfield
		return textArea;
	}
	
	private static JButton getButton(String text) {
		return new JButton(text);
	}
	
	private static JLabel getLabel(String text) {
		return new JLabel(text);
	}
	
	private static JTextField getTextField(int characters) {
		JTextField textField = new JTextField(characters);
		return textField;
	}
	
	private static JCheckBox getCheckBox() {
		return new JCheckBox();
	}
	
	private static JTextField getPasswordField(int characters) {
		return new JPasswordField(characters);
	}
	
	private static GridBagConstraints getConstraints(int x, int y) {
		return getConstraints(x, y, 1);
	}

	private static GridBagConstraints getConstraints(int x, int y, int width) {
		return getConstraints(x, y, false);
	}

	private static GridBagConstraints getConstraints(int x, int y, boolean fill) {
		return getConstraints(x, y, fill, 1);
	}

	private static GridBagConstraints getConstraints(int x, int y, boolean fill, int width) {
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = x;
		constraints.gridy = y;
		constraints.insets = new Insets(2, 5, 2, 5);
		if (fill) {
			constraints.fill = GridBagConstraints.HORIZONTAL;
			constraints.weightx = 1;
		}
		constraints.gridwidth = width;
		constraints.anchor = GridBagConstraints.WEST;
		return constraints;
	}
	
	public void setData() throws IOException {
		Configuration.readProperties();
		uuAccessCode1TF.setText(Configuration.getEncryptedString(Configuration.Parameters.UU_ACCESS_CODE1));
		uuAccessCode2TF.setText(Configuration.getEncryptedString(Configuration.Parameters.UU_ACCESS_CODE2));
		destinationFolderTF.setText(Configuration.getString(Configuration.Parameters.DESTINATION_FOLDER));
		directoryForArtifactCB.setSelected(Configuration.getBoolean(Configuration.Parameters.DIRECTORY_FOR_ARTIFACT));
		String [] artifacts = Configuration.getString(Configuration.Parameters.ARTIFACTS).split(",");
		StringBuilder resArtifacts = new StringBuilder();
		for (String artifact : artifacts) {
			if (resArtifacts.length() > 0) {
				resArtifacts.append("\n");
			}
			resArtifacts.append(artifact.trim());
		}
		artifactsTA.setText(resArtifacts.toString());
	}

	private void storeConfiguration() throws IOException {
		Configuration.setEncryptedString(Configuration.Parameters.UU_ACCESS_CODE1, uuAccessCode1TF.getText());
		Configuration.setEncryptedString(Configuration.Parameters.UU_ACCESS_CODE2, uuAccessCode2TF.getText());
		Configuration.setString(Configuration.Parameters.DESTINATION_FOLDER, destinationFolderTF.getText());
		Configuration.setBoolean(Configuration.Parameters.DIRECTORY_FOR_ARTIFACT, directoryForArtifactCB.isSelected());
		String [] artifacts = artifactsTA.getText().split("\n");
		StringBuilder resArtifacts = new StringBuilder();
		for (String artifact : artifacts) {
			if (resArtifacts.length() > 0) {
				resArtifacts.append(",");
			}
			resArtifacts.append(artifact.trim());
		}
		Configuration.setString(Configuration.Parameters.ARTIFACTS, resArtifacts.toString());
		Configuration.storeProperties();
	}
	
	private void addLog(String data) {
		logDialog.addLog(data);
	}
	
	private void addLogOK() {
		addLog("OK\n");
	}
	
	private static void deleteDir(File file, boolean deleteDir) {
		if (file.isDirectory()) {
			for (String child : file.list()) {
	            deleteDir(new File(file, child), true);
	        }
		} 
		if (deleteDir) {
			file.delete();
		}
	}

	@Override
	public void run() {
		try {
			try {
				storeConfiguration();
			} catch (IOException ioe) {
				log.error("Error writing properties to file", ioe);
				JOptionPane.showMessageDialog(this, "Chyba při aktualizaci konfigurace", "Chyba", JOptionPane.ERROR_MESSAGE);
				return;
			}
			addLog("Mažu adresář " + destinationFolderTF.getText() + "...");
			deleteDir(new File(destinationFolderTF.getText()), false);
			new File(destinationFolderTF.getText()).mkdir();
			addLogOK();
			
			addLog("Přihlašuji se...");
			UUManager manager = new UUManager();
			manager.initHttpClient();
			String data = manager.logIn();
			addLogOK();
			for(String artifact : artifactsTA.getText().split("\n")) {
				if (artifact.trim().length() > 0) {
					addLog(artifact + "...");
					data = manager.goToArtifact(data, artifact);
					data = manager.goToAttachments(data);
					
					File targetDir = new File(destinationFolderTF.getText());
					if (directoryForArtifactCB.isSelected()) {
						String dirName = artifact;
						if (dirName.indexOf(':') >= 0) {
							dirName = dirName.substring(dirName.indexOf(':') + 1);
						}
						dirName = dirName.replace('/', '_');
						targetDir = new File(targetDir, dirName);
						targetDir.mkdir();
					}
					manager.downloadAttachments(data, targetDir);
					addLogOK();
				}
			}
			addLog("Hotovo");
	        log.info("Total KBs read: " + (manager.getTotalBytes() / 1024));
		} catch (Exception e) {
			log.error("Error when downloading data", e);
			addLog(e.toString());
		} finally {
			this.setCursor(Cursor.getDefaultCursor());
			logDialog.done();
		}
	}


	
	@Override
	public void actionPerformed(ActionEvent event) {
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		logDialog = new LogDialog(this);
		
		// do the job in a new thread, so that the dispatcher is not blocked
		Thread thread = new Thread(this);
		thread.start();

		// and show dialog
		logDialog.setVisible(true);

	}

	
	
	public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, IOException {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			// ignore any exception when setting look & feel
		}
		// set proxy
		ProxySelector.setDefault(new CustomProxySelector(ProxySelector.getDefault()));
		
		// open window
		UUDownloader downloader = new UUDownloader();
		downloader.setData();
		downloader.setVisible(true);
		downloader.pack();
		downloader.setResizable(true);
		downloader.setLocationRelativeTo(null);
	}

}
