package controlCenter;

import java.awt.Color;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

public class LoginCredentialsPanel extends JPanel implements KeyChangedInformer {
	final TitledBorder titledBorder;

	public JButton loginButton;

	public JTextField usernameField;

	public JPasswordField passwordField;

	public JTextField databaseField;

	public JLabel loginStatusLabel;

	public Vector panelComponents;

	public LoginCredentialsPanel(ActionListener listener) {
		super();

		titledBorder = new TitledBorder(ResourceManager.BORDER, ResourceManager
				.getResource("label.login.info"));
		titledBorder.setTitleFont(ResourceManager.BOLD_FONT);

		this.setBorder(titledBorder);

		panelComponents = new Vector(4, 2);

		setLayout(null);

		JLabel usernameLabel = new JLabel(ResourceManager
				.getResource("label.username")
				+ ": ");
		usernameField = new JTextField(30);
		usernameField.addKeyListener(new CommonKeyListener(this));
		usernameLabel.setLabelFor(usernameField);
		panelComponents.add(usernameLabel);
		panelComponents.add(usernameField);

		JLabel passwordLabel = new JLabel(ResourceManager
				.getResource("label.password")
				+ ": ");
		passwordField = new JPasswordField(30);
		passwordField.addKeyListener(new CommonKeyListener(this));
		passwordLabel.setLabelFor(passwordField);
		panelComponents.add(passwordLabel);
		panelComponents.add(passwordField);

		JLabel databaseLabel = new JLabel(ResourceManager
				.getResource("label.database")
				+ ": ");
		databaseField = new JTextField(30);
		databaseField.addKeyListener(new CommonKeyListener(this));
		databaseLabel.setLabelFor(databaseField);
		panelComponents.add(databaseLabel);
		panelComponents.add(databaseField);

		JLabel systemLabel = new JLabel(ResourceManager
				.getResource("label.system")
				+ ": ");
		String[] systems = { ResourceManager.getResource("label.system.db2") }; // ,
		// "Oracle",
		// "SQL
		// Server"
		// };
		JComboBox systemComboBox = new JComboBox(systems);
		systemLabel.setLabelFor(systemComboBox);
		systemComboBox.setSelectedIndex(0);
		systemComboBox.addKeyListener(new CommonKeyListener(this));
		panelComponents.add(systemLabel);
		panelComponents.add(systemComboBox);

		loginButton = new JButton(ResourceManager.getResource("label.login"));
		loginButton.addActionListener(listener);
		panelComponents.add(loginButton);

		loginStatusLabel = new JLabel(" ");
		loginStatusLabel.setForeground(Color.red);
		loginStatusLabel.setFont(ResourceManager.BOLD_FONT);

		usernameLabel.setBounds(12, 30, 100, 20);
		passwordLabel.setBounds(12, 55, 100, 20);
		databaseLabel.setBounds(12, 80, 100, 20);
		systemLabel.setBounds(12, 105, 100, 20);

		usernameField.setBounds(110, 30, 370, 20);
		passwordField.setBounds(110, 55, 370, 20);
		databaseField.setBounds(110, 80, 370, 20);
		systemComboBox.setBounds(110, 105, 370, 20);

		loginButton.setBounds(220, 135, 70, 23);

		loginStatusLabel.setBounds(50, 160, 400, 50);
		loginStatusLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		loginStatusLabel.setAlignmentY(JLabel.CENTER_ALIGNMENT);

		add(usernameLabel);
		add(usernameField);
		add(passwordLabel);
		add(passwordField);
		add(databaseLabel);
		add(databaseField);
		add(systemLabel);
		add(systemComboBox);
		add(loginButton);
		add(loginStatusLabel);
		
		loginButton.setMnemonic(loginButton.getText().charAt(0));

		reset();
	}

	public void setEnabled(boolean enabled) {
		if (enabled)
			titledBorder.setTitleColor(Color.black);
		else
			titledBorder.setTitleColor(Color.gray);
		repaint();
	}

	public void checkInput() {
		loginStatusLabel.setText(" ");
		repaint();
	}

	public void reset() {
		usernameField
				.setText(ResourceManager.getResource("login.default.user"));
		passwordField.setText(ResourceManager
				.getResource("login.default.password"));
		databaseField.setText(ResourceManager
				.getResource("login.default.database"));
		loginStatusLabel.setText("");
	}

}
