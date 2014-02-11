package controlCenter;

import javax.swing.*;
import java.awt.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.Border;
import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class RenameDialog extends JDialog {
    private int type;
    private ControlCenter controlCenter;
    private Wizard wizard;
    private DefaultMutableTreeNode selectedNode;
    private TreePath selectionPath;
    private Vector existingNames;

    private JTextField newNameTextField;
    private ButtonGroup buttonGroup;
    private JButton okButton;
    private JButton cancelButton;

    public static int RENAME_AUDIT_DIALOG_TYPE = 1;
    public static int RENAME_POLICY_DIALOG_TYPE = 2;
    public static int RENAME_VERSION_DIALOG_TYPE = 3;

    private void checkInput(KeyEvent e) {
        final boolean enabled = !newNameTextField.getText().trim().equals("")
                && !existingNames.contains(newNameTextField.getText().trim());
        okButton.setEnabled(enabled);
    }

    public RenameDialog(int type, ControlCenter controlCenter, Wizard wizard, DefaultMutableTreeNode selectedNode,
            TreePath selectionPath) {
    	super(controlCenter,true);
        this.type = type;
        this.controlCenter = controlCenter;
        this.wizard = wizard;
        this.selectedNode = selectedNode;
        this.selectionPath = selectionPath;

        setTitle("Rename");
        ActionAdapter actionAdapter = new ActionAdapter();

        try {
            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            setLocation(screen.width / 5, screen.height / 5);
            //setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            final JLabel introductionLabel = new JLabel();
            String introductionText = "";
            String labelText = "";
            if ((type == RENAME_AUDIT_DIALOG_TYPE) || (type == RENAME_POLICY_DIALOG_TYPE)) {
                final String objectName;
                if (type == RENAME_AUDIT_DIALOG_TYPE)
                    objectName = "audit";
                else if (type == RENAME_POLICY_DIALOG_TYPE)
                    objectName = "policy";
                else {
                    // Error!
                    objectName = "";
                }
                introductionText = "Enter the new " + objectName + " name.";
                labelText = "New " + objectName + " name:";
            }
            else if (type == RENAME_VERSION_DIALOG_TYPE) {
                introductionText = "Enter the new version.";
                labelText = "New version name:";
            }

            introductionLabel.setText(introductionText);
            JLabel label = new JLabel(labelText);

            newNameTextField = new JTextField(30);
            newNameTextField.setText("");
            newNameTextField.addActionListener(actionAdapter);
            newNameTextField.addKeyListener(new java.awt.event.KeyAdapter() {
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
            okButton.setEnabled(false);
            okButton.addActionListener(actionAdapter);

            cancelButton = new JButton("Cancel");
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
            c.gridx = 0;
            c.gridy = 0;
            c.gridwidth = 1;
            gridBagLayout.setConstraints(label, c);
            textFieldPanel.add(label);

            c.weightx = 1.0;
            c.gridx = 1;
            c.gridy = 0;
            c.gridwidth = 1;
            gridBagLayout.setConstraints(newNameTextField, c);
            textFieldPanel.add(newNameTextField);

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
            gridBagLayout.setConstraints(buttonGroupBox, c);
            this.getContentPane().add(buttonGroupBox);
			setModal(true);
            pack();
            setResizable(false);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showDialog(Vector existingNames) {
        this.existingNames = existingNames;
        //System.out.println("\nEXISTING: " + existingNames + "\n");
        setVisible(true);
    }

    class ActionAdapter implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == okButton) {
                final String newCollectionName = newNameTextField.getText().trim();

                if (type == RENAME_AUDIT_DIALOG_TYPE) {
                    //final DefaultMutableTreeNode auditsNode =
                    // (DefaultMutableTreeNode) selectedNode.getParent();
                    final DefaultMutableTreeNode databaseNameNode = (DefaultMutableTreeNode) selectedNode.getParent()
                            .getParent();
                    final String databaseName = (String) databaseNameNode.getUserObject();
                    final String oldAuditName = (String) selectedNode.getUserObject();
                    final Version version = new Version(databaseName, oldAuditName, null);
                    if (controlCenter.resourceManager.renameAudit(version, newCollectionName)) {
                        // Does not work because a new tree is created. The old
                        // nodes no longer exist.
                        //controlCenter.getAuditor().getAuditTree().refreshAuditsNode(auditsNode);
                        //controlCenter.getAuditor().getAuditTree().makeVisible(selectionPath.getParentPath());
                        hide();
                    }
                    else {
                        hide();
                        final NotificationDialog notificationDialog = new NotificationDialog("Error",
                                "Could not rename audit!",controlCenter);
                        notificationDialog.show();
                    }
                }
                else if (type == RENAME_POLICY_DIALOG_TYPE) {
                    final DefaultMutableTreeNode databaseNameNode = (DefaultMutableTreeNode) selectedNode.getParent()
                            .getParent();
                    final String databaseName = (String) databaseNameNode.getUserObject();
                    final String oldPolicyName = (String) selectedNode.getUserObject();
                    final Version version = new Version(databaseName, oldPolicyName, null);
                    if (controlCenter.resourceManager.renamePolicy(version, newCollectionName)) {
                        hide();
                    }
                    else {
                        hide();
                        final NotificationDialog notificationDialog = new NotificationDialog("Error",
                                "Could not rename policy!",controlCenter);
                        notificationDialog.show();
                    }
                }
                else if (type == RENAME_VERSION_DIALOG_TYPE) {
                    final DefaultMutableTreeNode databaseNameNode = (DefaultMutableTreeNode) selectedNode.getParent()
                            .getParent().getParent().getParent();
                    final DefaultMutableTreeNode collectionNameNode = (DefaultMutableTreeNode) selectedNode.getParent()
                            .getParent();
                    final String databaseName = (String) databaseNameNode.getUserObject();
                    final String collectionName = (String) collectionNameNode.getUserObject();
                    final String oldVersionName = (String) selectedNode.getUserObject();
                    final Version version = new Version(databaseName, collectionName, oldVersionName);

                    boolean success = false;
                    if (wizard.getType() == Wizard.TASK_WIZARD_TYPE)
                        success = controlCenter.resourceManager.renameAuditVersion(version, newCollectionName);
                    else if (wizard.getType() == Wizard.RULE_WIZARD_TYPE)
                        success = controlCenter.resourceManager.renamePolicyVersion(version, newCollectionName);
                    if (success) {
                        if (wizard.getType() == Wizard.TASK_WIZARD_TYPE) {
                            controlCenter.getAuditor().getAuditTree().refreshDatabaseNameNode(databaseNameNode);
                            controlCenter.getAuditor().getAuditTree().makeVisible(selectionPath.getParentPath());
                        }
                        else if (wizard.getType() == Wizard.RULE_WIZARD_TYPE) {
                            controlCenter.getPolicyEditor().getPolicyTree().refreshDatabaseNameNode(databaseNameNode);
                            controlCenter.getPolicyEditor().getPolicyTree().makeVisible(selectionPath.getParentPath());
                        }
                        hide();
                    }
                    else {
                        hide();
                        final NotificationDialog notificationDialog = new NotificationDialog("Error",
                                "Could not rename the version!",controlCenter);
                        notificationDialog.show();
                    }
                }
            }
            else if (e.getSource() == cancelButton) {
                hide();
                //setVisible(false);
            }
        }
    }
}

/*******************************************************************************
 * package controlCenter;
 *
 * import javax.swing.*; import java.awt.*; import
 * javax.swing.border.EtchedBorder; import javax.swing.border.Border; import
 * javax.swing.border.TitledBorder; import java.awt.event.ActionEvent; import
 * java.awt.event.ActionListener;
 *
 *
 * public class NewAuditVersionDialog extends JDialog { private ResourceManager
 * resourceManager;
 *
 *
 * JTextField ruleNameTextField = new JTextField(20); JTextField
 * descriptionTextField = new JTextField();
 *
 * ButtonGroup buttonGroup = new ButtonGroup(); JButton okButton = new
 * JButton(); JButton cancelButton = new JButton(); JButton backButton = new
 * JButton(); JButton nextButton = new JButton();
 *
 * public NewAuditVersionDialog(ResourceManager resourceManager) {
 * this.resourceManager = resourceManager;
 *
 * setTitle("New audit version"); ActionAdapter actionAdapter = new
 * ActionAdapter();
 *
 *
 * JPanel rulePanel = new JPanel(); rulePanel.setLayout(new FlowLayout());
 *
 * rulePanel.add(new JLabel("Version name:")); rulePanel.add(ruleNameTextField);
 *
 *
 * JPanel navigationPanel = new JPanel(); navigationPanel.setLayout(new
 * FlowLayout()); navigationPanel.setBorder(BorderFactory.createEtchedBorder());
 *
 * okButton.setText("OK"); cancelButton.setText("Cancel"); backButton.setText(" <
 * Back"); nextButton.setText("Next >"); buttonGroup.add(okButton);
 * buttonGroup.add(cancelButton); //buttonGroup.add(backButton);
 * //buttonGroup.add(nextButton); navigationPanel.add(okButton);
 * navigationPanel.add(cancelButton); //navigationPanel.add(backButton);
 * //navigationPanel.add(nextButton);
 *
 * okButton.addActionListener(actionAdapter);
 * cancelButton.addActionListener(actionAdapter);
 *
 *
 * Container container = getContentPane(); container.setLayout(new
 * BoxLayout(container, BoxLayout.Y_AXIS)); container.add(rulePanel);
 * container.add(navigationPanel);
 *
 * Dimension screen = Toolkit.getDefaultToolkit().getScreenSize(); setSize(new
 * Dimension(400, 150)); setLocation(screen.width / 5, screen.height / 5); }
 *
 * private String collectionName; private String databaseName;
 *
 * public void showDialog(String databaseName, String collectionName) {
 * this.collectionName = collectionName; this.databaseName = databaseName;
 * ruleNameTextField.setText(""); setVisible(true); }
 *
 * class ActionAdapter implements ActionListener { public void
 * actionPerformed(ActionEvent e) { if (e.getSource() == cancelButton)
 * setVisible(false); else if (e.getSource() == okButton) { if
 * (resourceManager.createAudit(databaseName, collectionName,
 * ruleNameTextField.getText())) setVisible(false); else
 * System.err.println("Error in creating audit!"); } } } }
 ******************************************************************************/
