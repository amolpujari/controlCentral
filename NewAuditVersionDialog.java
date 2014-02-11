package controlCenter;

import Utility;
import javax.swing.*;
import java.awt.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;


public class NewAuditVersionDialog extends JDialog {
		private ResourceManager resourceManager;

		private Version version;
		//private String collectionName;
		//private String databaseName;

		private JTextField textField;
		private ButtonGroup buttonGroup;
		private JButton okButton;
		private JButton cancelButton;

		private void checkInput(KeyEvent e) {
				okButton.setEnabled(!textField.getText().trim().equals(""));
		}

    public NewAuditVersionDialog(ResourceManager resourceManager) {
    	super(resourceManager.controlCenter,true);
				this.resourceManager = resourceManager;

				setTitle("New audit");
				ActionAdapter actionAdapter = new ActionAdapter();

        try {
						Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
						setLocation(screen.width / 5, screen.height / 5);
						//setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

						JLabel introductionLabel = new JLabel();
						introductionLabel.setText("Enter the version of the audit.");

						JLabel label = new JLabel("Version:");
						textField = new JTextField(30);
						textField.addActionListener(actionAdapter);

						textField.addKeyListener(new java.awt.event.KeyAdapter() {
										public void keyPressed(KeyEvent e) {
												checkInput(e);
										}
										public void keyTyped(KeyEvent e) {
												checkInput(e);
										}
										public void keyReleased(KeyEvent e) {
												checkInput(e);
										}
								});

						okButton = new JButton("OK");
						cancelButton = new JButton("Cancel");

						okButton.addActionListener(actionAdapter);
						cancelButton.addActionListener(actionAdapter);

						final ButtonGroup buttonGroup = new ButtonGroup();
						buttonGroup.add(okButton);
						buttonGroup.add(cancelButton);

						final Box buttonGroupBox = Box.createHorizontalBox();
						buttonGroupBox.add(okButton);
						buttonGroupBox.add(cancelButton);

						JPanel textFieldPanel = new JPanel();
						GridBagLayout gridBagLayout = new GridBagLayout();
						textFieldPanel.setLayout(gridBagLayout);
						GridBagConstraints c = new GridBagConstraints();

						//c.fill = GridBagConstraints.HORIZONTAL;
						c.insets = new Insets(2, 2, 2, 2);

						c.anchor = GridBagConstraints.LINE_START;

						c.weightx = 0.1;
						c.gridx = 0; c.gridy = 0; c.gridwidth = 1;
						gridBagLayout.setConstraints(label, c);
						textFieldPanel.add(label);

						c.weightx = 1.0;
						c.gridx = 1; c.gridy = 0; c.gridwidth = 1;
						gridBagLayout.setConstraints(textField, c);
						textFieldPanel.add(textField);

						//
						// Root panel
						//
						gridBagLayout = new GridBagLayout();
						this.getContentPane().setLayout(gridBagLayout);
						c = new GridBagConstraints();
						c.insets = new Insets(2, 2, 2, 2);
						//c.fill = GridBagConstraints.VERTICAL;

						c.weightx = 1.0;
						c.gridx = 0; c.gridy = 0; c.gridwidth = 1;
						gridBagLayout.setConstraints(introductionLabel, c);
						this.getContentPane().add(introductionLabel);

						c.weightx = 1.0;
						c.gridx = 0; c.gridy = 1; c.gridwidth = 1;
						gridBagLayout.setConstraints(textFieldPanel, c);
						this.getContentPane().add(textFieldPanel);

						c.weightx = 1.0;
						c.gridx = 0; c.gridy = 2; c.gridwidth = 1;
						gridBagLayout.setConstraints(buttonGroupBox, c);
						this.getContentPane().add(buttonGroupBox);

						pack();
						setModal(true);
						setResizable(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
		}

    public void showDialog(Version version) {
				this.version = version;
				textField.setText("");
				okButton.setEnabled(false);
				setVisible(true);
		}

		class ActionAdapter implements ActionListener {
				public void actionPerformed(ActionEvent e) {
						if (e.getSource() == okButton) {
								final String versionName = textField.getText().trim();
								final Version newVersion = new Version(version.databaseName, version.collectionName, versionName);
								if (resourceManager.addAudit(newVersion)) {
										hide();
								}
								else {
										final String text = "Error: Could not add new audit version!";
										(new Utility()).err(this, "actionPerformed()",  text);
								}
						}
						else if (e.getSource() == cancelButton) {
								hide();
								//setVisible(false);
						}
				}
		}
}




/***
package controlCenter;

import javax.swing.*;
import java.awt.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class NewAuditVersionDialog extends JDialog
{
		private ResourceManager resourceManager;


		JTextField ruleNameTextField = new JTextField(20);
		JTextField descriptionTextField = new JTextField();

		ButtonGroup buttonGroup = new ButtonGroup();
		JButton okButton = new JButton();
		JButton cancelButton = new JButton();
		JButton backButton = new JButton();
		JButton nextButton = new JButton();

    public NewAuditVersionDialog(ResourceManager resourceManager)
    {
				this.resourceManager = resourceManager;

				setTitle("New audit version");
				ActionAdapter actionAdapter = new ActionAdapter();


				JPanel rulePanel = new JPanel();
				rulePanel.setLayout(new FlowLayout());

				rulePanel.add(new JLabel("Version name:"));
				rulePanel.add(ruleNameTextField);


				JPanel navigationPanel = new JPanel();
				navigationPanel.setLayout(new FlowLayout());
				navigationPanel.setBorder(BorderFactory.createEtchedBorder());

				okButton.setText("OK");
				cancelButton.setText("Cancel");
				backButton.setText("< Back");
				nextButton.setText("Next >");
				buttonGroup.add(okButton);
				buttonGroup.add(cancelButton);
				//buttonGroup.add(backButton);
				//buttonGroup.add(nextButton);
				navigationPanel.add(okButton);
				navigationPanel.add(cancelButton);
				//navigationPanel.add(backButton);
				//navigationPanel.add(nextButton);

				okButton.addActionListener(actionAdapter);
				cancelButton.addActionListener(actionAdapter);


				Container container = getContentPane();
				container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
				container.add(rulePanel);
				container.add(navigationPanel);

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(new Dimension(400, 150));
        setLocation(screen.width / 5, screen.height / 5);

		}

		private String collectionName;
		private String databaseName;

    public void showDialog(String databaseName, String collectionName)
    {
				this.collectionName = collectionName;
				this.databaseName = databaseName;
				ruleNameTextField.setText("");
				setVisible(true);
		}

		class ActionAdapter implements ActionListener
		{
				public void actionPerformed(ActionEvent e)
				{
						if (e.getSource() == cancelButton) setVisible(false);
						else if (e.getSource() == okButton)
								{
										if (resourceManager.createAudit(databaseName, collectionName, ruleNameTextField.getText()))
												setVisible(false);
										else System.err.println("Error in creating audit!");

								}
				}
		}
}
***/