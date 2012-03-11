package cz.pavel.uudownloader;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;


/**
 * This dialog is shown during download.
 * It displays log of download activities.
 */
public class LogDialog extends JDialog implements ActionListener {
	

	private static final long serialVersionUID = -1174919709384624844L;

	private JTextArea logTA;
	private JButton okButton;

	
	public LogDialog(Frame owner) {
		super(owner, true);
		setTitle("A jedem ...");
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		setLayout(new BorderLayout());
		logTA = new JTextArea(10, 50);
		logTA.setFont((new JTextField()).getFont()); // use the same font as for textfield
		logTA.setEditable(false);
		add(new JScrollPane(logTA), BorderLayout.CENTER);
		
		okButton = new JButton("Zavřít");
		okButton.setEnabled(false);
		okButton.addActionListener(this);
		add(okButton, BorderLayout.SOUTH);
		
		pack();
		setLocationRelativeTo(owner);
	}

	public void addLog(String data) {
		logTA.setText(logTA.getText() + data);
	}

	
	public void done() {
		okButton.setEnabled(true);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// the only possible action is the click on the OK button
		dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING)); 
	}
}
