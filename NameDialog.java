package controlCenter;

import Utility;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.util.Vector;

public class NameDialog extends JDialog {
    private ResourceManager resourceManager;
    private ResourceManager.Rule currentRule;
    private Wizard wizard;
    private Vector existingNames;

    private JTextField nameTextField;
    private JButton okButton;
    private JButton cancelButton;
    private JButton backButton;
    private JButton nextButton;

    public NameDialog(ResourceManager resourceManager, Wizard wizard) {
    	super(resourceManager.controlCenter,true);
        this.resourceManager = resourceManager;
        this.wizard = wizard;

        setTitle(wizard.getObjectNameUppercase());
        ActionAdapter actionAdapter = new ActionAdapter();

        try {
            JLabel introductionLabel = new JLabel();
            introductionLabel.setText("Enter the name of the " + wizard.getObjectName() + ".");

            JLabel nameLabel = new JLabel("Name:");
            nameTextField = new JTextField(30);
            nameTextField.addActionListener(actionAdapter);

            nameTextField.addKeyListener(new java.awt.event.KeyAdapter() {
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

            backButton = new JButton(ChooseDialog.LEFT_ARROW + " Back");
            okButton = new JButton("OK");
            cancelButton = new JButton("Cancel");
            nextButton = new JButton("Next " + ChooseDialog.RIGHT_ARROW);

            okButton.addActionListener(actionAdapter);
            cancelButton.addActionListener(actionAdapter);
            nextButton.addActionListener(actionAdapter);

            final JPanel navigationButtonPanel = new JPanel();
            navigationButtonPanel.setLayout(new FlowLayout());
            navigationButtonPanel.add(cancelButton);
            navigationButtonPanel.add(nextButton);

            JPanel textFieldPanel = new JPanel();
            GridBagLayout gridBagLayout = new GridBagLayout();
            textFieldPanel.setLayout(gridBagLayout);
            GridBagConstraints c = new GridBagConstraints();

            //c.fill = GridBagConstraints.HORIZONTAL;
            c.insets = new Insets(2, 2, 2, 2);

            c.anchor = GridBagConstraints.LINE_START;

            c.weightx = 0.1;
            c.gridx = 0;
            c.gridy = 0;
            c.gridwidth = 1;
            gridBagLayout.setConstraints(nameLabel, c);
            textFieldPanel.add(nameLabel);

            c.weightx = 1.0;
            c.gridx = 1;
            c.gridy = 0;
            c.gridwidth = 1;
            gridBagLayout.setConstraints(nameTextField, c);
            textFieldPanel.add(nameTextField);

            //
            // Root panel
            //
            gridBagLayout = new GridBagLayout();
            this.getContentPane().setLayout(gridBagLayout);
            c = new GridBagConstraints();
            c.insets = new Insets(2, 2, 2, 2);
            //c.fill = GridBagConstraints.VERTICAL;

            c.weightx = 1.0;
            c.gridx = 0;
            c.gridy = 0;
            c.gridwidth = 1;
            gridBagLayout.setConstraints(introductionLabel, c);
            this.getContentPane().add(introductionLabel);

            c.weightx = 1.0;
            c.gridx = 0;
            c.gridy = 1;
            c.gridwidth = 1;
            gridBagLayout.setConstraints(textFieldPanel, c);
            this.getContentPane().add(textFieldPanel);

            c.weightx = 1.0;
            c.gridx = 0;
            c.gridy = 2;
            c.gridwidth = 1;
            gridBagLayout.setConstraints(navigationButtonPanel, c);
            this.getContentPane().add(navigationButtonPanel);

            pack();
            setResizable(false);
            setModal(true);
            setLocation(Utility.getTopLeftPoint(this));
            //setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showDialog(ResourceManager.Rule currentRule, Vector existingNames) {
        nameTextField.setText(currentRule.name);
        this.currentRule = currentRule;
        this.existingNames = existingNames;

        nextButton.setEnabled((!currentRule.name.equals("")));
        setVisible(true);
    }

    private void checkInput(KeyEvent e) {
        System.out.println("ex names: " + existingNames);
        final boolean enabled = !nameTextField.getText().trim().equals("")
                && !existingNames.contains(nameTextField.getText().trim());
        
        
        /*
         *	Added by amol pujari 06/10/2006 
         */
        if(enabled)
       	// now checking for case insensitiveness
        {
        	String item = null;
        	for(int i=0; i<existingNames.size(); i++)
        	{
        		try
        		{
        			item = (String)existingNames.get(i);
        		}
        		catch(Exception exp){}// invalid cast or else
        		
        		if(item!=null)
       			if( item.equalsIgnoreCase(nameTextField.getText().trim()))
       			{
       				nextButton.setEnabled(false);
       				return;
       			}
        	}
        }
        
        
        nextButton.setEnabled(enabled);

        //int keyCode = (int) e.getKeyCode();
        //System.out.println(keyCode);
    }

    class ActionAdapter implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == cancelButton)
                setVisible(false);

            else if (e.getSource() == nameTextField)
                nextButton.setEnabled((!nameTextField.getText().equals("")));

            else if (e.getSource() == nextButton) {
                final String name = nameTextField.getText().trim();
                /***************************************************************
                 * if (existingNames.contains(name)) { final String text = "Name
                 * \"" + name + "\" already exists."; final NotificationDialog
                 * notificationDialog = new NotificationDialog("Error", text);
                 * notificationDialog.show(); System.err.println(text); } else
                 **************************************************************/

                if (!name.equals("")) {
                    setVisible(false);
                    currentRule.name = name;
                    if (wizard.getType() == Wizard.RULE_WIZARD_TYPE)
                        wizard.changeDialog(Wizard.ENTER_ENTITIES, currentRule);
                    else
                        wizard.changeDialog(Wizard.ENTER_COLUMNS, currentRule);
                }
                else {
                    (new Utility()).applicationError(this, "actionPerformed()", "Cannot create new name.");
                }
            }
        }
    }
}
