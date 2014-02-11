package controlCenter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

public class AboutBox extends JDialog {

	/**
	 * @param owner
	 * @param imageLabel
	 * @param title
	 * @param bigMessage
	 * @param smallMessage
	 */
	public AboutBox(Frame owner, JLabel imageLabel, String title,
			String bigMessage, String smallMessage) {
		super(owner, title, true);

		JPanel panel = new JPanel();
		final Color panelBackgroundColor = panel.getBackground();

		final Border border1 = BorderFactory
				.createEtchedBorder(EtchedBorder.RAISED);
		final Border border2 = new EmptyBorder(0, 0, 0, 0);
		imageLabel.setBorder(new CompoundBorder(border1, border2));
		panel.add(imageLabel);
		getContentPane().add(panel, BorderLayout.WEST);

		JTextArea text = new JTextArea(bigMessage);
		text.setBorder(new EmptyBorder(5, 10, 5, 10));
		text.setEditable(false);
		text.setBackground(panelBackgroundColor);
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(text);

		text = new JTextArea(smallMessage);
		text.setBorder(new EmptyBorder(5, 10, 5, 10));
		text.setEditable(false);
		text.setBackground(panelBackgroundColor);
		panel.add(text);

		getContentPane().add(panel, BorderLayout.CENTER);

		final JButton buttonOK = new JButton("OK");
		final ActionListener listener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		};
		buttonOK.addActionListener(listener);
		panel = new JPanel();
		panel.add(buttonOK);
		getContentPane().add(panel, BorderLayout.SOUTH);

		pack();
		setResizable(false);
		setLocationRelativeTo(null);
	}
}