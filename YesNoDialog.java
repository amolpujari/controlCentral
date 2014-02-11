package controlCenter;

import Utility;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;
import java.io.*;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class YesNoDialog extends JDialog {
    private final int dialogType;
    private final ControlCenter controlCenter;
    private final DefaultMutableTreeNode selectedNode;
    private String databaseName;
    private ResourceManager.Task task;
    private ResourceManager.Rule rule;

    private JButton yesButton;
    private JButton noButton;
    private JButton cancelButton;

    private static final int SCROLL_PANEL_DIMENSION_X = 300;
    private static final int SCROLL_PANEL_DIMENSION_Y = 200;

    public static int EXIT_DIALOG_TYPE = 1;
    public static int COMMIT_THIS_DATABASE_DIALOG_TYPE = 2;
    public static int COMMIT_ALL_DATABASES_DIALOG_TYPE = 3;
    public static int COMMIT_ALL_DATABASES_AND_EXIT_DIALOG_TYPE = 4; // First
    // pops up
    // the
    // commit
    // dialog
    // and then
    // the exit
    // dialog.
    public static int ROLLBACK_THIS_DATABASE_DIALOG_TYPE = 5;
    public static int CREATE_APPLICATION_USAGE_DIALOG_TYPE = 6;
    public static int CREATE_AND_DROP_BACKLOG_TABLES_DIALOG_TYPE = 7;
    public static int DELETE_ALL_AUDITS_DIALOG_TYPE = 8;
    public static int DELETE_AUDIT_DIALOG_TYPE = 9;
    public static int DELETE_VERSION_DIALOG_TYPE = 10;
    public static int DELETE_TASK_DIALOG_TYPE = 11;
    public static int DELETE_RULE_DIALOG_TYPE = 12;
    public static int RECONCILE_BACKLOG_AFTER_SCHEMA_CHANGE_DIALOG_TYPE = 13;
    public static int ENTITY_DEFINITION_DIALOG_TYPE = 14;
    public static int DISCONNECT_DIALOG_TYPE = 15;
    public static int DROP_AUDIT_METADATA_DIALOG_TYPE = 16;

    /*
     * amol pujari 27/09/2006
     * adding this one to pop up dialog box for all policies delete confirmation
     */
    public static int DELETE_ALL_POLICIES_DIALOG_TYPE = 17;

    public YesNoDialog(int dialogType, ControlCenter controlCenter, String databaseName, ResourceManager.Rule rule) {
        this(dialogType, controlCenter, databaseName);
        this.rule = rule;
    }

    public YesNoDialog(int dialogType, ControlCenter controlCenter, String databaseName, ResourceManager.Task task) {
        this(dialogType, controlCenter, databaseName);
        this.task = task;
   }

    public YesNoDialog(int dialogType, ControlCenter controlCenter, String databaseName) {
        this(dialogType, controlCenter);
        this.databaseName = databaseName;
    }

    public YesNoDialog(int dialogType, ControlCenter controlCenter) {
        this(dialogType, null, controlCenter);
    }

    public YesNoDialog(int dialogType, DefaultMutableTreeNode selectedNode, ControlCenter controlCenter) {
    	super(controlCenter,true);
    	this.dialogType = dialogType;
        this.controlCenter = controlCenter;
        this.selectedNode = selectedNode;
        this.setModal(true);

        String title = "";
        String question = "";
        String comment = "";

        try {
            this.addWindowListener(new MyWindowAdapter(this));

            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            //setSize(new Dimension((int)(screen.width * 3.0 / 4),
            // (int)(screen.height * 3.0 / 4)));
            //setLocationRelativeTo(parent);
            //setSize(new Dimension(300, 350));
            setLocation(screen.width / 5, screen.height / 5);
            //setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            yesButton = new JButton();
            yesButton.setText("Yes");
            yesButton.addActionListener(new ActionAdapter(this));
            noButton = new JButton();
            noButton.setText("No");
            noButton.addActionListener(new ActionAdapter(this));
            cancelButton = new JButton();
            cancelButton.setText("Cancel");
            cancelButton.addActionListener(new ActionAdapter(this));

            final ButtonGroup buttonGroup = new ButtonGroup();
            buttonGroup.add(yesButton);
            buttonGroup.add(noButton);

            final Box buttonGroupBox = Box.createHorizontalBox();
            buttonGroupBox.add(yesButton);
            buttonGroupBox.add(noButton);

            JScrollPane dropListPane = null;
            JScrollPane createListPane = null;

            title = "Question";

            if (dialogType == COMMIT_THIS_DATABASE_DIALOG_TYPE) {
                question = "Save all changes to the database?";
                buttonGroup.add(cancelButton);
                buttonGroupBox.add(cancelButton);
            }
            else if (dialogType == ROLLBACK_THIS_DATABASE_DIALOG_TYPE) {
                question = "Undo all changes to the database?";
                buttonGroup.add(cancelButton);
                buttonGroupBox.add(cancelButton);
            }
            else if (dialogType == COMMIT_ALL_DATABASES_DIALOG_TYPE) {
                question = "Save all changes to all databases?";
                buttonGroup.add(cancelButton);
                buttonGroupBox.add(cancelButton);
            }
            else if (dialogType == COMMIT_ALL_DATABASES_AND_EXIT_DIALOG_TYPE) {
                // Text of the first dialog in the sequence.
                question = "Save all changes to all databases?";
                buttonGroup.add(cancelButton);
                buttonGroupBox.add(cancelButton);
            }
            else if (dialogType == EXIT_DIALOG_TYPE) {
                question = "Really exit?";
            }
            else if (dialogType == DROP_AUDIT_METADATA_DIALOG_TYPE) {
                question = "Really delete audit metadata?";
                comment = "You will not be able to perform an audit on data accesses\n"
                        + "prior to now if you will save changes to the database!";
            }
            else if (dialogType == DELETE_ALL_AUDITS_DIALOG_TYPE) {
                question = "Really delete all audits?";
            }
            else if (dialogType == DELETE_AUDIT_DIALOG_TYPE) {
                question = "Really delete audit?";
            }
            else if (dialogType == DELETE_VERSION_DIALOG_TYPE) {
                question = "Really delete audit version?";
            }
            else if (dialogType == DELETE_RULE_DIALOG_TYPE) {
                question = "Really delete policy rule?";
            }
            else if (dialogType == DELETE_TASK_DIALOG_TYPE) {
                question = "Really delete audit task?";
            }
            else if (dialogType == CREATE_AND_DROP_BACKLOG_TABLES_DIALOG_TYPE) {
                question = "Backlog will be created for the following tables.\nReally change the backlog?";
                final Vector createTables = controlCenter.getAuditor().getAuditTree().getBacklogTablesSelectionDialog()
                        .getItemsInScope();
                final Vector dropTables = controlCenter.getAuditor().getAuditTree().getBacklogTablesSelectionDialog()
                        .getItemsNotInScope();

                comment = "";

                //                for (int i = 0, createCount = 0; i < createTables.size();
                // i++) {
                //                    final TableDescriptor table = (TableDescriptor)
                // createTables.get(i);
                //                    if (!controlCenter.resourceManager.hasBacklogTable(table)) {
                //                        if (createCount == 0)
                //                            comment += "";
                //                        createCount++;
                //                        comment += " " + createCount + ". " + table + "\n";
                //                    }
                //                }
                //                for (int i = 0, dropCount = 0; i < dropTables.size(); i++) {
                //                    final TableDescriptor table = (TableDescriptor)
                // dropTables.get(i);
                //                    if (controlCenter.resourceManager.hasBacklogTable(table)) {
                //                        dropCount++;
                //                        comment += " " + dropCount + ". " + table + "\n";
                //                    }
                //                }

                final JList dropList = new JList();
                dropList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                dropList.setListData(dropTables);
                dropList.setBackground(ChooseDialog.BACKGROUND_COLOR);
                dropListPane = new JScrollPane(dropList);
                dropListPane.setPreferredSize(new Dimension(ChooseDialog.SCROLL_PANEL_DIMENSION_X,
                        ChooseDialog.SCROLL_PANEL_DIMENSION_Y / 2));
                final TitledBorder dropBorder = new TitledBorder(ChooseDialog.BORDER, "Drop backlogs");
                dropBorder.setTitleFont(ChooseDialog.BOLD_FONT);
                dropListPane.setBorder(dropBorder);

                final JList createList = new JList();
                createList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                createList.setListData(createTables);
                createList.setBackground(ChooseDialog.BACKGROUND_COLOR);
                createListPane = new JScrollPane(createList);
                createListPane.setPreferredSize(new Dimension(ChooseDialog.SCROLL_PANEL_DIMENSION_X,
                        ChooseDialog.SCROLL_PANEL_DIMENSION_Y / 2));
                final TitledBorder createBorder = new TitledBorder(ChooseDialog.BORDER, "Create backlogs");
                createBorder.setTitleFont(ChooseDialog.BOLD_FONT);
                createListPane.setBorder(createBorder);
            }
            else if (dialogType == RECONCILE_BACKLOG_AFTER_SCHEMA_CHANGE_DIALOG_TYPE) {
                question = "The database schema has changed for the tables below.\nReconcile the backlog now?";

                final String databaseName = (String) selectedNode.getUserObject();
                final Vector tables = controlCenter.resourceManager.getConnectedDatabaseTables(databaseName);
                final Vector changedTables = new Vector();
                for (int i = 0; i < tables.size(); i++) {
                    final TableDescriptor table = (TableDescriptor) tables.get(i);
                    if (!controlCenter.resourceManager.isBacklogTable(table)) {
                        final boolean hasChangedSchema = controlCenter.resourceManager
                                .hasBacklogTableSchemaChange(table);
                        if (hasChangedSchema)
                            changedTables.add(table);
                    }
                }
                comment = "";
                for (int i = 0; i < changedTables.size(); i++) {
                    if (i > 0)
                        comment += "\n\n";

                    final TableDescriptor tableDescriptor = (TableDescriptor) changedTables.get(i);
                    if (!controlCenter.resourceManager.hasBacklogTable(tableDescriptor)) {
                        comment += "  " + (i + 1) + ".  " + tableDescriptor.schemaName.toLowerCase() + "."
                                + tableDescriptor.tableName.toLowerCase() + " does not have any backlog yet";
                    }
                    else {
                        comment += "  " + (i + 1) + ".  " + tableDescriptor.schemaName.toLowerCase() + "."
                                + tableDescriptor.tableName.toLowerCase() + "\n";

                        final String backlogTableNamePrefix = controlCenter.resourceManager
                                .getResource("backlog.table.prefix");
                        final TableDescriptor backlogTableDescriptor = new TableDescriptor(
                                tableDescriptor.databaseName, tableDescriptor.schemaName, backlogTableNamePrefix
                                        + tableDescriptor.tableName);
                        final Vector columns = controlCenter.resourceManager.getDatabaseColumns(tableDescriptor);
                        final String[] ignoredBacklogTableMetaDateColumnNames = Utility
                                .parseArray(controlCenter.resourceManager.getResource("backlog.table.metadata.columns"));
                        final Vector backlogColumns = controlCenter.resourceManager.getDatabaseColumns(
                                backlogTableDescriptor, ignoredBacklogTableMetaDateColumnNames);

                        comment += "\tOld schema:\n";
                        for (int j = 0; j < backlogColumns.size(); j++) {
                            if (j > 0)
                                comment += "\n";
                            final ColumnDescriptor column = (ColumnDescriptor) backlogColumns.get(j);
                            comment += "\t\t" + column.columnName.toLowerCase() + ": " + column.typeName;
                        }
                        comment += "\n";
                        comment += "\tNew schema:\n";
                        for (int j = 0; j < columns.size(); j++) {
                            if (j > 0)
                                comment += "\n";
                            final ColumnDescriptor column = (ColumnDescriptor) columns.get(j);
                            comment += "\t\t" + column.columnName.toLowerCase() + ": " + column.typeName;
                        }
                    }
                }
            }
            else if (dialogType == ENTITY_DEFINITION_DIALOG_TYPE) {
                question = "Really create entities?";
            }
            else if (dialogType == DISCONNECT_DIALOG_TYPE) {
                final String databaseName = (String) selectedNode.getUserObject();
                question = "Really disconnect from database \"" + databaseName + "\"?";
            }
            else if (dialogType == CREATE_APPLICATION_USAGE_DIALOG_TYPE) {
                question = "Really create the following application usage?";

                final Vector applicationUsageVector = controlCenter.getPolicyEditor().getPolicyTree()
                        .getApplicationUsageDialog().getApplicationUsageVector();

                comment = "";
                for (int i = 0; i < applicationUsageVector.size(); i++) {
                    final ResourceManager.ApplicationUsage applicationUsage = (ResourceManager.ApplicationUsage) applicationUsageVector
                            .get(i);
                    comment += applicationUsage.toStringDetails() + "\n\n";
                }
            }
            /*
             * amol pujari 27/09/2006
             * adding this one to pop up dialog box for all policies delete confirmation
             */
            else if (dialogType == DELETE_ALL_POLICIES_DIALOG_TYPE ) {
                question = "Really delete all Policies?";
            }
            

            this.setTitle(title);

            final Color panelBackgroundColor = (new JPanel()).getBackground();

            final JTextArea questionTextArea = new JTextArea(question);
            //questionTextArea.setBorder(new EmptyBorder(5, 10, 5, 10));
            questionTextArea.setFont(ChooseDialog.BOLD_FONT);
            questionTextArea.setEditable(false);
            questionTextArea.setBackground(panelBackgroundColor);
            questionTextArea.setText(question);

            JTextArea commentTextArea = new JTextArea(comment);
            //commentTextArea.setBorder(new EmptyBorder(5, 10, 5, 10));
            commentTextArea.setFont(ChooseDialog.NORMAL_FONT);
            commentTextArea.setEditable(false);
            commentTextArea.setBackground(panelBackgroundColor);

            JScrollPane commentPane = new JScrollPane(commentTextArea);
            commentPane.setPreferredSize(new Dimension(SCROLL_PANEL_DIMENSION_X, SCROLL_PANEL_DIMENSION_Y));

            //
            // Root panel
            //
            GridBagLayout gridBagLayout = new GridBagLayout();
            this.getContentPane().setLayout(gridBagLayout);
            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(2, 2, 2, 2);
            //c.fill = GridBagConstraints.VERTICAL;

            c.weightx = 1.0;
            c.gridx = 0;
            c.gridy = 0;
            c.gridwidth = 2;
            gridBagLayout.setConstraints(questionTextArea, c);
            this.getContentPane().add(questionTextArea);

            // Add comment only if needed.
            if (!commentTextArea.getText().trim().equals("")) {
                c.weightx = 1.0;
                c.gridx = 0;
                c.gridy++;
                c.gridwidth = 2;
                //gridBagLayout.setConstraints(commentTextArea, c);
                //this.getContentPane().add(commentTextArea);
                gridBagLayout.setConstraints(commentPane, c);
                this.getContentPane().add(commentPane);
            }

            if (dialogType == CREATE_AND_DROP_BACKLOG_TABLES_DIALOG_TYPE) {
                questionTextArea.setFont(ChooseDialog.NORMAL_FONT);

                c.weightx = 1.0;
                c.gridx = 0;
                c.gridy++;
                c.gridwidth = 1;
                gridBagLayout.setConstraints(dropListPane, c);
                this.getContentPane().add(dropListPane);

                c.weightx = 1.0;
                c.gridx = 1;
                //c.gridy++;
                c.gridwidth = 1;
                gridBagLayout.setConstraints(createListPane, c);
                this.getContentPane().add(createListPane);
            }

            c.weightx = 1.0;
            c.gridx = 0;
            c.gridy++;
            c.gridwidth = 2;
            gridBagLayout.setConstraints(buttonGroupBox, c);
            this.getContentPane().add(buttonGroupBox);

            pack();
            setResizable(false);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void thisWindowClosed(WindowEvent e) {
    };

    class MyWindowAdapter extends WindowAdapter {
        private YesNoDialog adaptee;

        MyWindowAdapter(YesNoDialog adaptee) {
            this.adaptee = adaptee;
        }

        public void windowClosed(WindowEvent e) {
            adaptee.thisWindowClosed(e);
        }
    }

    class ActionAdapter implements ActionListener {
        private YesNoDialog adaptee;

        ActionAdapter(YesNoDialog adaptee) {
            this.adaptee = adaptee;
        }

        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == yesButton) {
                if (dialogType == COMMIT_THIS_DATABASE_DIALOG_TYPE) {
                    final NotificationDialog notificationDialog;
                    final String databaseName = (String) selectedNode.getUserObject();
                    if (controlCenter.resourceManager.commitChanges(databaseName)) {
                        hide();
                        notificationDialog = new NotificationDialog("Success", "Changes to database " + databaseName
                                + " successfully saved.",controlCenter);
                        notificationDialog.show();
                    }
                    else {
                        hide();
                        notificationDialog = new NotificationDialog("Error", "Cannot save changes to database "
                                + databaseName + ".",controlCenter);
                        notificationDialog.show();
                    }
                }
                else if (dialogType == ROLLBACK_THIS_DATABASE_DIALOG_TYPE) {
                    final NotificationDialog notificationDialog;
                    final String databaseName = (String) selectedNode.getUserObject();
                    if (controlCenter.resourceManager.rollbackChanges(databaseName)) {
                        hide();
                        notificationDialog = new NotificationDialog("Success", "Changes to database " + databaseName
                                + " successfully undone.",controlCenter);
                        notificationDialog.show();
                    }
                    else {
                        hide();
                        notificationDialog = new NotificationDialog("Error", "Cannot undo changes to database "
                                + databaseName + ".",controlCenter);
                        notificationDialog.show();
                    }
                    //
                    // Refresh the audit and policy tree.
                    //
                    controlCenter.getAuditor().getAuditTree().refresh();
                    controlCenter.getPolicyEditor().getPolicyTree().refresh();
                }
                else if (dialogType == COMMIT_ALL_DATABASES_DIALOG_TYPE) {
                    System.out.println("Committing changes.");
                    NotificationDialog notificationDialog;
                    if (controlCenter.resourceManager.commitChanges()) {
                        //setVisible(false);
                        hide();
                        notificationDialog = new NotificationDialog("Success",
                                "All changes to databases successfully saved.",controlCenter);
                        notificationDialog.show();
                    }
                    else {
                        hide();
                        notificationDialog = new NotificationDialog("Error", "Cannot commit changes to all databases.",controlCenter);
                        notificationDialog.show();
                    }
                    System.out.println("Closing jdbc connections.");
                    if (controlCenter.resourceManager.closeAllConnections()) {
                        notificationDialog = new NotificationDialog("Success",
                                "All database connections successfully closed.",controlCenter);
                        notificationDialog.show();
                    }
                    else {
                        notificationDialog = new NotificationDialog("Error", "Cannot close all database connections.",controlCenter);
                        notificationDialog.show();
                    }
                }
                else if (dialogType == COMMIT_ALL_DATABASES_AND_EXIT_DIALOG_TYPE) {
                    if (controlCenter.resourceManager.commitChanges()) {
                        setVisible(false);

                        // Switch to the exit dialog.
                        hide();
                        controlCenter.exitDialog.show();
                    }
                    else
                        System.err.println("Cannot save changes to database!");
                }
                else if (dialogType == EXIT_DIALOG_TYPE) {
                    System.exit(0);
                }
                else if (dialogType == DROP_AUDIT_METADATA_DIALOG_TYPE) {
                    final NotificationDialog notificationDialog;
                    if (!controlCenter.resourceManager.dropBacklog((String) selectedNode.getUserObject())) {
                        notificationDialog = new NotificationDialog("Error", "Cannot delete audit metadata.",controlCenter);
                        notificationDialog.show();
                    }
                    else {
                        final DefaultTreeModel auditTreeModel = controlCenter.getAuditor().getAuditTree()
                                .getAuditTreeModel();
                        DefaultMutableTreeNode taskNameNode = (DefaultMutableTreeNode) auditTreeModel.getChild(
                                selectedNode, 0);
                        auditTreeModel.removeNodeFromParent(taskNameNode);
                        hide();
                        notificationDialog = new NotificationDialog("Success", "Audit metadata deleted successfully.",controlCenter);
                        notificationDialog.show();
                    }
                }
                else if (dialogType == DELETE_ALL_AUDITS_DIALOG_TYPE) {
                    final DefaultMutableTreeNode databaseNameNode = (DefaultMutableTreeNode) selectedNode.getParent();
                    final String databaseName = (String) databaseNameNode.getUserObject();
                    final Version version = new Version(databaseName, null, null);

                    if (controlCenter.resourceManager.deleteAudit(version)) {
                        hide();
                    }
                    else {
                        hide();
                        final NotificationDialog notificationDialog = new NotificationDialog("Error",
                                "Cannot delete all audits in database \"" + databaseName + "\".",controlCenter);
                        notificationDialog.show();
                    }
                }
                else if (dialogType == DELETE_AUDIT_DIALOG_TYPE) {
                    final DefaultMutableTreeNode databaseNameNode = (DefaultMutableTreeNode) selectedNode.getParent()
                            .getParent();
                    final String databaseName = (String) databaseNameNode.getUserObject();
                    final String auditName = (String) selectedNode.getUserObject();
                    final Version version = new Version(databaseName, auditName, null);
                    if (controlCenter.resourceManager.deleteAudit(version)) {
                        hide();
                    }
                    else {
                        hide();
                        final NotificationDialog notificationDialog = new NotificationDialog("Error",
                                "Cannot delete audit \"" + auditName + "\" in database \"" + databaseName + "\".",controlCenter);
                        notificationDialog.show();
                    }
                }
                else if (dialogType == DELETE_VERSION_DIALOG_TYPE) {
                    final DefaultMutableTreeNode databaseNode = (DefaultMutableTreeNode) selectedNode.getParent()
                            .getParent().getParent().getParent();
                    final DefaultMutableTreeNode auditNameNode = (DefaultMutableTreeNode) selectedNode.getParent()
                            .getParent();
                    final String databaseName = (String) databaseNode.getUserObject();
                    final String auditName = (String) auditNameNode.getUserObject();
                    final String versionName = (String) selectedNode.getUserObject();
                    final Version version = new Version(databaseName, auditName, versionName);
                    if (controlCenter.resourceManager.deleteAudit(version)) {
                        hide();
                        /*******************************************************
                         * final DefaultTreeModel auditTreeModel =
                         * controlCenter.getAuditor().getAuditTree().getAuditTreeModel();
                         * final DefaultMutableTreeNode taskNameNode =
                         * (DefaultMutableTreeNode)
                         * auditTreeModel.getChild(selectedNode, 0);
                         * auditTreeModel.removeNodeFromParent(taskNameNode);
                         ******************************************************/
                    }
                    else {
                        hide();
                        final NotificationDialog notificationDialog = new NotificationDialog("Error",
                                "Cannot delete version \"" + versionName + "\" of audit \"" + auditName
                                        + "\" in database \"" + databaseName + "\".",controlCenter);
                        notificationDialog.show();
                    }
                }
                else if (dialogType == DELETE_RULE_DIALOG_TYPE) {
                    final NotificationDialog notificationDialog;
                    final String policyName = rule.policyName;
                    final String versionName = rule.version;
                    final Version version = new Version(databaseName, policyName, versionName);
                    if (!controlCenter.resourceManager.deleteRule(version, rule)) {
                        final String text = "Cannot delete rule!";
                        notificationDialog = new NotificationDialog("Error", text,controlCenter);
                        hide();
                        notificationDialog.show();
                        Utility.err(this, "actionPerformed()", text);
                    }
                    else {
                        hide();
                    }
                }
                else if (dialogType == DELETE_TASK_DIALOG_TYPE) {
                    final NotificationDialog notificationDialog;
                    final Version version = new Version(databaseName, null, null);
                    if (!controlCenter.resourceManager.deleteTask(version, task)) {
                        final String text = "Cannot delete task!";
                        notificationDialog = new NotificationDialog("Error", text,controlCenter);
                        hide();
                        notificationDialog.show();
                        Utility.err(this, "actionPerformed()", text);
                    }
                    else {
                        hide();
                    }
                }
                else if (dialogType == CREATE_AND_DROP_BACKLOG_TABLES_DIALOG_TYPE) {
                    boolean allIsSuccessfull = true;
                    final Vector createTables = controlCenter.getAuditor().getAuditTree()
                            .getBacklogTablesSelectionDialog().getItemsInScope();
                    final Vector dropTables = controlCenter.getAuditor().getAuditTree()
                            .getBacklogTablesSelectionDialog().getItemsNotInScope();
                    for (int i = 0; i < createTables.size(); i++) {
                        final TableDescriptor tableDescriptor = (TableDescriptor) createTables.get(i);
                        if (controlCenter.resourceManager.createBacklogTableFirst(tableDescriptor) == null) {
                            final String text = "Cannot create backlog for table " + tableDescriptor + "!";
                            final NotificationDialog errorNotificationDialog = new NotificationDialog("Error", text,controlCenter);
                            hide();
                            errorNotificationDialog.show();
                            allIsSuccessfull = false;
                        }
                    }
                    for (int i = 0; i < dropTables.size(); i++) {
                        final TableDescriptor tableDescriptor = (TableDescriptor) dropTables.get(i);
                        if (!controlCenter.resourceManager.dropBacklogTable(tableDescriptor)) {
                            final String text = "Cannot remove backlog for table " + tableDescriptor + "!";
                            final NotificationDialog errorNotificationDialog = new NotificationDialog("Error", text,controlCenter);
                            hide();
                            errorNotificationDialog.show();
                            allIsSuccessfull = false;
                        }
                    }
                    if (allIsSuccessfull) {
                        final NotificationDialog notificationDialog;
                        final String text = "Backlog changed successfully!";
                        notificationDialog = new NotificationDialog("Success", text,controlCenter);
                        hide();
                        notificationDialog.show();
                    }
                    else {
                        hide();
                    }
                }
                else if (dialogType == RECONCILE_BACKLOG_AFTER_SCHEMA_CHANGE_DIALOG_TYPE) {
                    final DefaultMutableTreeNode databaseNode = (DefaultMutableTreeNode) selectedNode;
                    final String databaseName = (String) databaseNode.getUserObject();

                    hide();
                    final NotificationDialog notificationDialog = new NotificationDialog("Schema reconciliation",
                            "Now, CLIO should start up to reconcile the schema of database \"" + databaseName + "\".",controlCenter);
                    notificationDialog.show();
                    //String oldSQL = "";
                    //oldSQL += "SELECT a, b\n";
                    //oldSQL += "FROM T";
                    //final BacklogDefinitionDialog backlogDefinitionDialog =
                    // new BacklogDefinitionDialog(oldSQL);
                    //backlogDefinitionDialog.show();
                }
                else if (dialogType == ENTITY_DEFINITION_DIALOG_TYPE) {
                    boolean allIsSuccessfull = true;
                    final Vector entities = controlCenter.getPolicyEditor().getPolicyTree().getEntityDefinitionDialog()
                            .getItemsInScope();
                    if (controlCenter.resourceManager.createAssociationAndChoiceTables(databaseName, entities)) {
                        final NotificationDialog notificationDialog;
                        final String text = "Entity metadata created successfully!";
                        notificationDialog = new NotificationDialog("Success", text,controlCenter);
                        hide();
                        notificationDialog.show();
                    }
                    else {
                        String text = "Cannot create metadata in database \"" + databaseName
                                + "\" for the following entities:";
                        for (int i = 0; i < entities.size(); i++)
                            text += (TableDescriptor) entities.get(i);
                        final NotificationDialog errorNotificationDialog = new NotificationDialog("Error", text,controlCenter);
                        hide();
                        errorNotificationDialog.show();
                    }
                }
                else if (dialogType == DISCONNECT_DIALOG_TYPE) {
                    final DefaultMutableTreeNode databaseNode = (DefaultMutableTreeNode) selectedNode;
                    final String databaseName = (String) databaseNode.getUserObject();

                    final NotificationDialog notificationDialog;

                    if (controlCenter.resourceManager.hasUncommittedChanges(databaseName)) {
                        if (controlCenter.resourceManager.commitChanges()) {
                            //setVisible(false);
                            hide();
                            final YesNoDialog confirmDialog = new YesNoDialog(
                                    YesNoDialog.COMMIT_THIS_DATABASE_DIALOG_TYPE, selectedNode, controlCenter);
                            confirmDialog.show();
                        }
                        else {
                            hide();
                            notificationDialog = new NotificationDialog("Error", "Cannot save changes to database "
                                    + databaseName + ".",controlCenter);
                            notificationDialog.show();
                        }
                    }
                    else {
                        if (controlCenter.resourceManager.closeConnection(databaseName)) {
                            hide();
                        }
                        else {
                            final String text = "Cannot disconnect from database \"" + databaseName + "\".";
                            final NotificationDialog errorNotificationDialog = new NotificationDialog("Error", text,controlCenter);
                            hide();
                            errorNotificationDialog.show();
                        }
                    }
                }
                else if (dialogType == CREATE_APPLICATION_USAGE_DIALOG_TYPE) {
                    final Vector applicationUsageVector = controlCenter.getPolicyEditor().getPolicyTree()
                            .getApplicationUsageDialog().getApplicationUsageVector();

                    System.out.println("DATABASE: " + databaseName);

                    if (controlCenter.resourceManager.updateApplicationUsage(databaseName, applicationUsageVector)) {
                        String text = "Application usage successfully created.";
                        final NotificationDialog notificationDialog = new NotificationDialog("Success", text,controlCenter);
                        hide();
                        notificationDialog.show();
                    }
                    else {
                        String text = "Cannot create application usage.";
                        final NotificationDialog notificationDialog = new NotificationDialog("Error", text,controlCenter);
                        hide();
                        notificationDialog.show();
                    }
                }
                /*
                 * amol pujari 27/09/2006
                 * adding this one to pop up dialog box for all policies delete confirmation
                 */
                else if (dialogType == DELETE_ALL_POLICIES_DIALOG_TYPE) {
                	
                	final Version version = new Version(databaseName, null, null);
                	
                	try
                	{
                		controlCenter.resourceManager.deletePolicy(version);
                	}
                	catch(Exception exp)
                	{
                		exp.printStackTrace();
                	}
                	
                	hide();
                }

            }
            else if (e.getSource() == noButton) {
                if ((dialogType == COMMIT_THIS_DATABASE_DIALOG_TYPE)
                        || (dialogType == ROLLBACK_THIS_DATABASE_DIALOG_TYPE)
                        || (dialogType == COMMIT_ALL_DATABASES_DIALOG_TYPE) || (dialogType == EXIT_DIALOG_TYPE)
                        || (dialogType == DROP_AUDIT_METADATA_DIALOG_TYPE)
                        || (dialogType == DELETE_ALL_AUDITS_DIALOG_TYPE) || (dialogType == DELETE_AUDIT_DIALOG_TYPE)
                        || (dialogType == DELETE_VERSION_DIALOG_TYPE) || (dialogType == DELETE_TASK_DIALOG_TYPE)
                        || (dialogType == CREATE_AND_DROP_BACKLOG_TABLES_DIALOG_TYPE)
                        || (dialogType == RECONCILE_BACKLOG_AFTER_SCHEMA_CHANGE_DIALOG_TYPE)
                        || (dialogType == ENTITY_DEFINITION_DIALOG_TYPE) || (dialogType == DISCONNECT_DIALOG_TYPE)
                        || (dialogType == CREATE_APPLICATION_USAGE_DIALOG_TYPE)
                        || (dialogType == DELETE_RULE_DIALOG_TYPE)) {
                    hide();
                    // Do nothing.
                }
                else if (dialogType == COMMIT_ALL_DATABASES_AND_EXIT_DIALOG_TYPE) {
                    // Switch to the exit dialog.
                    hide();
                    controlCenter.exitDialog.show();
                }
            }
            else if (e.getSource() == cancelButton) {
                hide();
                // Do nothing.
            }
        }
    }
}
