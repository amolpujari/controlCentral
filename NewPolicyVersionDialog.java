package controlCenter;

import javax.swing.*;
import java.awt.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Vector;

public class NewPolicyVersionDialog extends JDialog {
    private ResourceManager resourceManager;

    JTextField ruleNameTextField = new JTextField(20);
    JTextField descriptionTextField = new JTextField();

    ButtonGroup buttonGroup = new ButtonGroup();
    JButton okButton = new JButton();
    JButton cancelButton = new JButton();
    JButton backButton = new JButton();
    JButton nextButton = new JButton();
    
	private Vector existingVersions = null;
	
    public NewPolicyVersionDialog(ResourceManager resourceManager) {
    	super(resourceManager.controlCenter,true);
        this.resourceManager = resourceManager;

        setTitle("New Version");
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
        setModal(true);
        setLocation(screen.width / 5, screen.height / 5);
        
        /*
         * added by amol pujari 06/10/2006
         * to prevent user okaying existing version name
         */
        ruleNameTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                checkInput(e);
            }

//            public void keyTyped(KeyEvent e) {
//                checkInput(e);
//            }

           public void keyReleased(KeyEvent e) {
                checkInput(e);
            }
        });
 
    }

    private String policyName;
    private String dbName;

    public void showDialog(String dbName, String policyName) {
        this.policyName = policyName;
        this.dbName = dbName;

        ruleNameTextField.setText("");
        /*
         * added by amol pujari 06/10/2006
         * to prevent user okaying existing version name
         */
        existingVersions = resourceManager.getVersions(dbName, policyName);

        setVisible(true);

    }

    class ActionAdapter implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == cancelButton)
                setVisible(false);
            else if (e.getSource() == okButton) {
                final Version version = new Version(dbName, policyName, ruleNameTextField.getText());
                if (resourceManager.addPolicy(version))
                    setVisible(false);
                else
                    System.err.println("Error in creating policy!");

            }
        }
    }
    
    /*
     * added by amol pujari 06/10/2006
     * to prevent user okaying existing version name
     */
   
    private void checkInput(KeyEvent e) {
    	
    	final String text = ruleNameTextField.getText().trim();
    	
    	okButton.setEnabled(true);
    	if(text.length()==0)
    	{
    		okButton.setEnabled(false);
    		return;
    	}

       	String item = null;

       	for(int i=0; i<existingVersions.size(); i++)
        {
        	try
        	{
        		item = (String)existingVersions.get(i);
        	}
        	catch(Exception exp){}// invalid cast or else
        	
        	if(item!=null)
       		if( item.equalsIgnoreCase(text))
       		{
       			okButton.setEnabled(false);
       			return;
       		}
        }
    }


}
