package controlCenter;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;

public class LoginPanel extends ControlCenterComponent {

	private JMenuBar menubar;

	private JButton policyEditorButton;

	private JButton auditorButton;

	private Vector postLoginComponents;

	private LoginCredentialsPanel loginCredentialsPanel;

	public LoginPanel(ControlCenter controlCenter) {
		super(controlCenter);
		this.menubar = null;
		postLoginComponents = new Vector(4, 2);

		LoginPanelListener panelListener = new LoginPanelListener();
		loginCredentialsPanel = new LoginCredentialsPanel(panelListener);

		JLabel initTaskInstruction = new JLabel("");
		postLoginComponents.add(initTaskInstruction);

		policyEditorButton = new JButton(ResourceManager
				.getResource("label.switch.pe"), new ImageIcon(ResourceManager
				.getResource("image.file.editor")));
		policyEditorButton.setVerticalTextPosition(AbstractButton.BOTTOM);
		policyEditorButton.setHorizontalTextPosition(AbstractButton.CENTER);
		policyEditorButton.addActionListener(panelListener);

		auditorButton = new JButton(ResourceManager
				.getResource("label.switch.au"), new ImageIcon(ResourceManager
				.getResource("image.file.auditor")));
		auditorButton.setVerticalTextPosition(AbstractButton.BOTTOM);
		auditorButton.setHorizontalTextPosition(AbstractButton.CENTER);
		auditorButton.addActionListener(panelListener);

		postLoginComponents.add(policyEditorButton);
		postLoginComponents.add(auditorButton);

		initTaskInstruction.setMinimumSize(new Dimension(700, 50));
		initTaskInstruction.setPreferredSize(new Dimension(700, 50));
		initTaskInstruction.setMaximumSize(new Dimension(700, 50));

		loginCredentialsPanel.setMinimumSize(new Dimension(500, 260));
		loginCredentialsPanel.setPreferredSize(new Dimension(500, 260));
		loginCredentialsPanel.setMaximumSize(new Dimension(500, 260));

		final JPanel pan0 = new JPanel();
		final JPanel pan1 = new JPanel();
		final JPanel pan2 = new JPanel();

		final JPanel pan3 = new JPanel();
		final JPanel pan4 = new JPanel();
		pan3.add(policyEditorButton);
		pan4.add(auditorButton);

		pan2.setLayout(new GridLayout());
		pan2.add(pan3);
		pan2.add(pan4);

		pan1.setLayout(new BoxLayout(pan1, BoxLayout.X_AXIS));

		final JPanel panT1 = new JPanel();
		final JPanel panT2 = new JPanel();

		panT1.setMinimumSize(new Dimension(100, 260));
		panT1.setPreferredSize(new Dimension(100, 260));
		panT1.setMaximumSize(new Dimension(9500, 260));

		panT2.setMinimumSize(new Dimension(100, 260));
		panT2.setPreferredSize(new Dimension(100, 260));
		panT2.setMaximumSize(new Dimension(9500, 260));

		pan1.add(panT1);
		pan1.add(loginCredentialsPanel);
		pan1.add(panT2);

		pan0.setMinimumSize(new Dimension(100, 60));
		pan0.setPreferredSize(new Dimension(100, 60));
		pan0.setMaximumSize(new Dimension(9500, 100));

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(pan0);
		add(pan1);
		add(initTaskInstruction);
		add(pan2);
		// Leave empty vertical space where a possible message on login failure
		// will be printed.
		loginCredentialsPanel.loginStatusLabel.setText("");

		setEnableStatus(postLoginComponents, false);

		auditorButton.setMnemonic('A');
		policyEditorButton.setMnemonic('P');
	}

	public JMenuBar getMenuBar() {
		if (menubar == null) {
			menubar = new JMenuBar();
			JMenu helpMenu = getHelpMenu();
			// Add glue menus so that the help menu appears on the right border.
			// menubar.add(Box.createHorizontalGlue());
			menubar.add(helpMenu);
		}
		return menubar;
	}

	class LoginPanelListener implements ActionListener {

		String msg = "";

		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == loginCredentialsPanel.loginButton) {
				// validateLogin

				new Thread(new Runnable() {
					public void run() {
						msg = controlCenter.resourceManager.openConnection(
								loginCredentialsPanel.databaseField.getText()
										.toUpperCase(),
								loginCredentialsPanel.usernameField.getText(),
								new String(loginCredentialsPanel.passwordField
										.getPassword()));

						if (msg.length() > 0) {
							loginCredentialsPanel.loginStatusLabel
									.setText("<html>" + msg + "</html>");
						} else {
							loginCredentialsPanel.loginStatusLabel.setText(" "); // Success
							loginCredentialsPanel.loginStatusLabel.setText(" "); // Success
							setEnableStatus(
									loginCredentialsPanel.panelComponents,
									false);
							setEnableStatus(postLoginComponents, true);
							loginCredentialsPanel.setEnabled(false);
						}
						MessageBox.stopBusy();
					}
				}).start();

				MessageBox.showBusy(controlCenter, ResourceManager
						.getResource("label.connecting.to")
						+ " \""
						+ loginCredentialsPanel.databaseField.getText()
								.toUpperCase() + "\"");

			} else if (e.getSource() == policyEditorButton)
				controlCenter.changePanel(ControlCenter.POLICY_EDITOR);
			else if (e.getSource() == auditorButton)
				controlCenter.changePanel(ControlCenter.AUDITOR);
		}
	}

	private void setEnableStatus(Vector components, boolean status) {
		int numElements = components.size();
		for (int i = 0; i < numElements; i++)
			((JComponent) components.elementAt(i)).setEnabled(status);
	}

}