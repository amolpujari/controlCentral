package controlCenter;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.border.EtchedBorder;

public class MessageBox extends JDialog {
	public final static int TYPE_CONFIRM = 10;

	public final static int TYPE_YESNO = 20;

	public final static int BUTTON_YES = 0;

	public final static int BUTTON_NO = 1;

	public final static int BUTTON_CANCEL = 2;

	public final static int ICON_NONE = 110;

	public final static int ICON_CON = 111;

	public final static int ICON_ERR = 112;

	public final static int ICON_WARN = 113;

	public final static int ICON_FAIL = 114;

	public final static int ICON_SUC = 115;

	public final static int ICON_INFO = 117;

	private int result = BUTTON_CANCEL;

	private final JButton yesButton = new JButton("  "
			+ ResourceManager.getResource("button.yes") + "  ");

	private final JButton noButton = new JButton("  "
			+ ResourceManager.getResource("button.no") + "  ");

	private final JButton cancelButton = new JButton(" "
			+ ResourceManager.getResource("button.cancel") + " ");

	private final JButton okButton = new JButton("  "
			+ ResourceManager.getResource("button.ok") + "  ");

	private final Canvas c1 = new Canvas();

	private final Canvas c2 = new Canvas();

	private final JPanel pan1 = new JPanel();

	private final JPanel pan2 = new JPanel();

	private final JPanel pan3 = new JPanel();

	private final JLabel text = new JLabel();

	private final JLabel icon = new JLabel();

	private static MessageBox instance = null;

	private JPanel pan10;

	private JPanel pan11;

	protected MessageBox() throws Exception {
		// Exists only to defeat instantiation.
	}

	public static MessageBox getInstance(final Frame parent,
			final String title, final String message, final int type,
			final int icon) {
		if (instance == null)
			instance = new MessageBox(parent, title, message, type);
		else {
			instance.setTitle(title);
			instance.text.setText("<html><br>" + message + "<br></html>");
		}

		instance.icon.setIcon(null);

		switch (icon) {
		case ICON_ERR:
			instance.icon.setIcon(ResourceManager.ICON_errorBig);
			break;
		case ICON_WARN:
			instance.icon.setIcon(ResourceManager.ICON_warningBig);
			break;
		case ICON_CON:
			instance.icon.setIcon(ResourceManager.ICON_confirmBig);
			break;
		case ICON_INFO:
			instance.icon.setIcon(ResourceManager.ICON_confirmBig);
			break;
		case ICON_NONE:
			instance.icon.setIcon(null);
			break;
		case ICON_SUC:
			instance.icon.setIcon(ResourceManager.ICON_successBig);
			break;
		case ICON_FAIL:
			instance.icon.setIcon(ResourceManager.ICON_failureBig);
			break;
		}

		if (type == TYPE_YESNO) {
			instance.okButton.setVisible(false);
			instance.yesButton.setVisible(true);
			instance.noButton.setVisible(true);
			instance.cancelButton.setVisible(true);
		} else if (type == TYPE_CONFIRM) {
			instance.yesButton.setVisible(false);
			instance.noButton.setVisible(false);
			instance.cancelButton.setVisible(false);
			instance.okButton.setVisible(true);
		}

		instance.setVisible(false);
		instance.pack();
		instance.setLocationRelativeTo(null);

		return instance;
	}

	private MessageBox(final Frame parent, final String title,
			final String message, final int type) {
		super(parent, true);
		setTitle(title);
		text.setText("<html><br>" + message + "<br></html>");
		getContentPane().setLayout(
				new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		c1.setSize(100, 20);
		c2.setSize(100, 20);
		getContentPane().add(pan1);
		getContentPane().add(pan2);
		getContentPane().add(pan3);
		text.setMinimumSize(new Dimension(400, 150));
		pan10 = new JPanel();
		pan10.setMinimumSize(new Dimension(100, 150));
		pan10.setPreferredSize(new Dimension(100, text.getHeight()));
		pan1.add(pan10);
		pan1.add(icon);
		pan1.add(text);
		pan11 = new JPanel();
		pan11.setMinimumSize(new Dimension(100, 150));
		pan11.setPreferredSize(new Dimension(100, text.getHeight()));
		pan1.add(pan11);
		pan2.add(c1);
		pan3.setSize(50, 20);
		yesButton.setMinimumSize(new Dimension(70, 26));
		noButton.setMinimumSize(new Dimension(70, 26));
		cancelButton.setMinimumSize(new Dimension(70, 26));
		yesButton.setMnemonic(yesButton.getText().charAt(0));
		noButton.setMnemonic(noButton.getText().charAt(0));
		cancelButton.setMnemonic(cancelButton.getText().charAt(0));
		okButton.setMnemonic(okButton.getText().charAt(0));
		pan2.add(yesButton);
		pan2.add(noButton);
		pan2.add(cancelButton);
		yesButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				result = BUTTON_YES;
				dispose();
			}
		});

		noButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				result = BUTTON_NO;
				dispose();
			}
		});

		cancelButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				result = BUTTON_CANCEL;
				dispose();
			}
		});

		okButton.setMinimumSize(new Dimension(70, 26));
		pan2.add(okButton);
		okButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				dispose();
			}
		});

		pan2.add(c2);
	}

	public static int result(final Frame parent, final String title,
			final String message, final int icon) {
		if (message == null)
			return BUTTON_CANCEL;

		if (message.length() == 0)
			return BUTTON_CANCEL;

		MessageBox box = MessageBox.getInstance(parent, title, message,
				TYPE_YESNO, icon);
		box.show();
		return box.result;
	}

	public static void show(final Frame parent, final String title,
			final String message, final int icon) {
		if (message == null)
			return;

		if (message.length() == 0)
			return;

		MessageBox box = MessageBox.getInstance(parent, title, message,
				TYPE_CONFIRM, icon);
		box.show();
	}

	public static void showBusy(final Frame parent, String message) {
		if (busyScreen == null)
			busyScreen = new BusyScreen(parent, message);

		busyScreen.setMessage(message);
		busyScreen.setVisible(true);
	}

	public static void stopBusy() {
		if (busyScreen != null)
			busyScreen.setVisible(false);
	}

	public static void setMessage(final String message) {
		instance.text.setText("<html><br>" + message + "<br></html>");
		instance.text.updateUI();
	}

	public static void setBusyMessage(final String message) {
		busyScreen.setMessage("<html><br>" + message + "<br></html>");
	}

	public static void setIcon(final ImageIcon icon) {
		instance.icon.setIcon(icon);
		instance.icon.updateUI();
	}

	private static BusyScreen busyScreen;

}

class BusyScreen extends JWindow {

	final JLabel message = new JLabel();

	public BusyScreen(Frame parent, String text) {
		super(parent);
		if (text == null || text.length() < 4)
			message.setText(ResourceManager.getResource("label.processing"));
		else
			message.setText(text + " ...");
		final JLabel icon = new JLabel(ResourceManager.ICON_Busy);
		final JPanel pan = new JPanel();
		pan.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		pan.setMinimumSize(new Dimension(300, 80));
		message.setMinimumSize(new Dimension(200, 60));
		message.setPreferredSize(new Dimension(300, 60));
		pan.add(icon);
		pan.add(message);
		getContentPane().add(pan, BorderLayout.CENTER);
		pack();
		setLocationRelativeTo(null);
	}

	public void setMessage(String text) {
		if (text == null || text.length() < 4)
			message.setText(ResourceManager.getResource("label.processing"));
		else
			message.setText(text + " ...");
		message.updateUI();
	}
}
