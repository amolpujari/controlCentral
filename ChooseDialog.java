package controlCenter;

import Utility;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.util.Collections;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;
import javax.swing.*;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

public class ChooseDialog extends JDialog {
    public static final int COLUMN_CHOOSE_TYPE = 1;
    public static final int ENTITY_CHOOSE_TYPE = 2;
    public static final int PURPOSE_CHOOSE_TYPE = 3;
    public static final int ACCESSOR_CHOOSE_TYPE = 4;
    public static final int RECIPIENT_CHOOSE_TYPE = 5;

    public static final int BACKLOG_TABLE_CHOOSE_TYPE = 6;
    public static final int ENTITY_DEFINITION_CHOOSE_TYPE = 7;

    public static final int SINGLE_ROW_SCROLL_PANEL_DIMENSION_X = 60;
    public static final int SCROLL_PANEL_DIMENSION_X = 170;
    public static final int SCROLL_PANEL_DIMENSION_Y = 200;
    public static final int INSETS_PADDING = 2;
    public static final int TEXT_FIELD_SIZE = 20;

    public final static Color BLACK_COLOR = Color.black;
    public final static Color GRAY_COLOR = Color.gray;
    public final static Color RED_COLOR = Color.red;
    public final static Color BACKGROUND_COLOR = (new JPanel()).getBackground();

    public static final String RIGHT_ARROW = "\u25ba";
    public static final String LEFT_ARROW = "\u25c4";

    public static final Border BORDER = BorderFactory.createEtchedBorder(Color.white, GRAY_COLOR);

    public final static Font NORMAL_FONT = (new JLabel()).getFont();
    public final static Font BOLD_FONT = new Font(NORMAL_FONT.getFamily(), Font.BOLD, NORMAL_FONT.getSize());
    public final static Font COURIER_FONT = new Font("Courier", NORMAL_FONT.getStyle(), NORMAL_FONT.getSize());

    private ControlCenter controlCenter;
    private ResourceManager resourceManager;
    private ResourceManager.Rule currentRule;
    private Wizard wizard;
    private int chooseType;

    private final String chooseObjectName;
    private final String chooseObjectNameUppercase;

    private JList schemaList;
    private JList itemsNotInScopeList;
    private JList itemsInScopeList;
    private JList itemsInScopeOriginalList; // The list shown when showDialog()
    // is executed.
    private JList selectedCombinationList;

    private JButton l2rAllButton; // Left to right all items
    private JButton l2rOneButton; // Left to right single item
    private JButton r2lAllButton; // Right to left all items
    private JButton r2lOneButton; // Right to left single item
    private JButton nextButton;
    private JButton backButton;
    private JButton cancelButton;
    private JButton createNewButton;
    private JButton addButton;
    private JButton deleteButton;

    private JTextField createNewTextField;

    private String databaseName;
    private final Vector empty = new Vector();
    private Vector currentItemsInScope = new Vector();
    private Vector currentAllColumns = new Vector();

    private int wizardType;

    public ChooseDialog(ControlCenter controlCenter, int chooseType) {
        this(controlCenter.resourceManager, null, chooseType);
        this.controlCenter = controlCenter;
    }

    public ChooseDialog(ResourceManager resourceManager, Wizard wizard, int chooseType) {
    	super(resourceManager.controlCenter,true);
        this.resourceManager = resourceManager;
        this.wizard = wizard;
        this.chooseType = chooseType;
        this.wizardType = (wizard == null) ? 0 : wizard.getType();

        final Container contentPane = this.getContentPane();
        final ActionAdapter actionAdapter = new ActionAdapter();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        if (chooseType == COLUMN_CHOOSE_TYPE) {
            chooseObjectName = "columns";
            chooseObjectNameUppercase = "Columns";
        }
        else if (chooseType == ENTITY_CHOOSE_TYPE) {
            chooseObjectName = "entities";
            chooseObjectNameUppercase = "Entities";
        }
        else if (chooseType == PURPOSE_CHOOSE_TYPE) {
            chooseObjectName = "purposes";
            chooseObjectNameUppercase = "Purposes";
        }
        else if (chooseType == ACCESSOR_CHOOSE_TYPE) {
            chooseObjectName = "accessors";
            chooseObjectNameUppercase = "Accessors";
        }
        else if (chooseType == RECIPIENT_CHOOSE_TYPE) {
            chooseObjectName = "recipients";
            chooseObjectNameUppercase = "Recipients";
        }
        else if (chooseType == BACKLOG_TABLE_CHOOSE_TYPE) {
            chooseObjectName = "backlog tables";
            chooseObjectNameUppercase = "Backlog tables";
        }
        else if (chooseType == ENTITY_DEFINITION_CHOOSE_TYPE) {
            chooseObjectName = "entities";
            chooseObjectNameUppercase = "Entities";
        }
        else {
            (new Utility()).applicationError(this, "constructor", "Unknown choose type.");
            chooseObjectName = "";
            chooseObjectNameUppercase = "";
        }

        if (wizard != null) {
            // This dialog belongs to a wizard (which has "back" and "next"
            // buttons.
            setTitle(wizard.getObjectNameUppercase() + " wizard: " + chooseObjectNameUppercase);
        }
        else {
            // This dialog is a stand-alone dialog (like backlog table
            // selection).
            setTitle(chooseObjectNameUppercase + " selection");
        }

        final String topLabelText;
        if (chooseType == COLUMN_CHOOSE_TYPE)
            topLabelText = "Select the table columns that might have been disclosed together.";
        else
            topLabelText = "Select the " + chooseObjectName + ".";
        JLabel topLabel = new JLabel(topLabelText);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new FlowLayout());

        JPanel schemaPanel = new JPanel();
        final TitledBorder schemaTitledBorder = new TitledBorder(ChooseDialog.BORDER, "Table names");
        schemaTitledBorder.setTitleFont(ChooseDialog.BOLD_FONT);
        schemaPanel.setBorder(schemaTitledBorder);
        schemaList = new JList();
        schemaList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        schemaList.addListSelectionListener(actionAdapter);
        JScrollPane schemaPane = new JScrollPane(schemaList);
        schemaPane.setPreferredSize(new Dimension(SCROLL_PANEL_DIMENSION_X, SCROLL_PANEL_DIMENSION_Y));
        schemaPanel.add(schemaPane);
        if (chooseType == COLUMN_CHOOSE_TYPE)
            centerPanel.add(schemaPanel);

        JPanel columnNotInScopePanel = new JPanel();
        final String columnNotInScopePanelText;

        if (chooseType == COLUMN_CHOOSE_TYPE)
            columnNotInScopePanelText = "Unselected " + chooseObjectName;
        else if (chooseType == BACKLOG_TABLE_CHOOSE_TYPE)
            columnNotInScopePanelText = "Tables without backlog";
        else
            columnNotInScopePanelText = "Previously defined " + chooseObjectName;

        final TitledBorder columnNotInScopeTitledBorder = new TitledBorder(ChooseDialog.BORDER,
                columnNotInScopePanelText);
        columnNotInScopeTitledBorder.setTitleFont(ChooseDialog.BOLD_FONT);
        columnNotInScopePanel.setBorder(columnNotInScopeTitledBorder);
        itemsNotInScopeList = new JList();
        itemsNotInScopeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        itemsNotInScopeList.addListSelectionListener(actionAdapter);
        JScrollPane itemsNotInScopePane = new JScrollPane(itemsNotInScopeList);
        itemsNotInScopePane.setPreferredSize(new Dimension(SCROLL_PANEL_DIMENSION_X, SCROLL_PANEL_DIMENSION_Y));
        columnNotInScopePanel.add(itemsNotInScopePane);
        centerPanel.add(columnNotInScopePanel);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        l2rAllButton = new JButton(RIGHT_ARROW + RIGHT_ARROW);
        l2rOneButton = new JButton(RIGHT_ARROW);
        r2lOneButton = new JButton(LEFT_ARROW);
        r2lAllButton = new JButton(LEFT_ARROW + LEFT_ARROW);
        l2rAllButton.addActionListener(actionAdapter);
        l2rOneButton.addActionListener(actionAdapter);
        r2lOneButton.addActionListener(actionAdapter);
        r2lAllButton.addActionListener(actionAdapter);
        l2rAllButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        l2rOneButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        r2lOneButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        r2lAllButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel.add(l2rAllButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        buttonPanel.add(l2rOneButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        buttonPanel.add(r2lOneButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        buttonPanel.add(r2lAllButton);
        centerPanel.add(buttonPanel);

        JPanel selectionButtonPanel = new JPanel();
        selectionButtonPanel.setLayout(new FlowLayout());
        //selectionButtonPanel.setBorder(BorderFactory.createEtchedBorder());
        addButton = new JButton("Add");
        addButton.addActionListener(actionAdapter);
        deleteButton = new JButton("Delete");
        deleteButton.addActionListener(actionAdapter);
        selectionButtonPanel.add(addButton);
        selectionButtonPanel.add(deleteButton);

        JPanel columnInScopePanel = new JPanel();
        final String columnInScopePanelText;
        if (chooseType == BACKLOG_TABLE_CHOOSE_TYPE)
            columnInScopePanelText = "Tables with backlog";
        else
            columnInScopePanelText = "Selected " + chooseObjectName;

        final TitledBorder columnInScopeTitledBorder = new TitledBorder(ChooseDialog.BORDER, columnInScopePanelText);
        columnInScopeTitledBorder.setTitleFont(ChooseDialog.BOLD_FONT);
        columnInScopePanel.setBorder(columnInScopeTitledBorder);
        itemsInScopeOriginalList = new JList();
        itemsInScopeList = new JList();
        itemsInScopeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        itemsInScopeList.addListSelectionListener(actionAdapter);
        JScrollPane itemsInScopePane = new JScrollPane(itemsInScopeList);
        itemsInScopePane.setPreferredSize(new Dimension(SCROLL_PANEL_DIMENSION_X, SCROLL_PANEL_DIMENSION_Y));
        columnInScopePanel.add(itemsInScopePane);
        centerPanel.add(columnInScopePanel);

        JPanel createNewPanel = new JPanel();
        createNewPanel.setBorder(new TitledBorder(BORDER, "Create new"));

        final TitledBorder createNewTitledBorder = new TitledBorder(ChooseDialog.BORDER, "Create new");
        createNewTitledBorder.setTitleFont(ChooseDialog.BOLD_FONT);
        createNewPanel.setBorder(createNewTitledBorder);

        createNewPanel.setLayout(new FlowLayout());
        //JLabel createNewLabel = new JLabel("Create new:");
        createNewButton = new JButton("Add");
        createNewButton.addActionListener(actionAdapter);
        createNewTextField = new JTextField(TEXT_FIELD_SIZE);
        createNewTextField.addKeyListener(new java.awt.event.KeyAdapter() {
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
        //createNewPanel.add(createNewLabel);
        createNewPanel.add(createNewTextField);
        createNewPanel.add(createNewButton);

        JPanel combinationPanel = new JPanel();
        String combinationTitledBorderText = (wizardType == Wizard.RULE_WIZARD_TYPE) ?
        	"Selected combinations (check if allow pseudonym access only)" : "Selected combinations";
        final TitledBorder combinationTitledBorder = new TitledBorder(ChooseDialog.BORDER, combinationTitledBorderText);
        combinationTitledBorder.setTitleFont(ChooseDialog.BOLD_FONT);
        combinationPanel.setBorder(combinationTitledBorder);

        selectedCombinationList = (wizardType == Wizard.RULE_WIZARD_TYPE) ? new JCheckBoxList() : new JList();

        selectedCombinationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        selectedCombinationList.addListSelectionListener(actionAdapter);
        JScrollPane combinationPane = new JScrollPane(selectedCombinationList);
        combinationPane.setPreferredSize(new Dimension(2 * ChooseDialog.SCROLL_PANEL_DIMENSION_X,
                ChooseDialog.SCROLL_PANEL_DIMENSION_Y / 2));
        combinationPanel.add(combinationPane);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout());
        backButton = new JButton(LEFT_ARROW + " Back");
        backButton.addActionListener(actionAdapter);

        if ((chooseType == BACKLOG_TABLE_CHOOSE_TYPE) || (chooseType == ENTITY_DEFINITION_CHOOSE_TYPE)) {
            nextButton = new JButton("OK");
        }
        else {
            nextButton = new JButton("Next " + RIGHT_ARROW);
        }
        nextButton.addActionListener(actionAdapter);
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(actionAdapter);
        if ((chooseType == BACKLOG_TABLE_CHOOSE_TYPE) || (chooseType == ENTITY_DEFINITION_CHOOSE_TYPE)) {
            bottomPanel.add(nextButton); // Was renamed to "OK".
            bottomPanel.add(cancelButton);
        }
        else {
            bottomPanel.add(backButton);
            bottomPanel.add(cancelButton);
            bottomPanel.add(nextButton);
        }

        topLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        selectionButtonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        bottomPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        contentPane.add(topLabel);
        contentPane.add(centerPanel);
        if ((chooseType == PURPOSE_CHOOSE_TYPE) || (chooseType == ACCESSOR_CHOOSE_TYPE)
                || (chooseType == RECIPIENT_CHOOSE_TYPE))
            contentPane.add(createNewPanel);

        //if ((wizardType == Wizard.TASK_WIZARD_TYPE) && (chooseType == COLUMN_CHOOSE_TYPE)) {
        if (chooseType == COLUMN_CHOOSE_TYPE) {
            contentPane.add(selectionButtonPanel);
            contentPane.add(combinationPanel);
        }

        contentPane.add(bottomPanel);

        pack();
        updateButtons();
        setResizable(false);
        setModal(true);
        setLocation(Utility.getTopLeftPoint(this));
    }

    public int getChooseType() {
        return chooseType;
    }

    public String getChooseObjectName() {
        return chooseObjectName;
    }

    public String getChooseObjectNameUppercase() {
        return chooseObjectNameUppercase;
    }

    public Vector getItemsInScopeOriginal() {
        Vector vector = new Vector();
        for (int i = 0; i < itemsInScopeOriginalList.getModel().getSize(); i++)
            vector.add(itemsInScopeOriginalList.getModel().getElementAt(i));
        return vector;
    }

    public Vector getItemsInScope() {
        Vector vector = new Vector();
        for (int i = 0; i < itemsInScopeList.getModel().getSize(); i++)
            vector.add(itemsInScopeList.getModel().getElementAt(i));
        return vector;
    }

    public Vector getItemsNotInScope() {
        Vector vector = new Vector();
        for (int i = 0; i < itemsNotInScopeList.getModel().getSize(); i++)
            vector.add(itemsNotInScopeList.getModel().getElementAt(i));
        return vector;
    }

    public void showDialog(String databaseName, ResourceManager.Rule currentRule) {
        this.databaseName = databaseName;
        this.currentRule = currentRule;

        //
        // ATTENTION!
        // Do not use currentAllColumns.clear() instead!
        // It would lead to the situation that we loose the content of the
        // selected items GUI list
        // when the user clicks on BACK.
        //
        currentAllColumns = new Vector();

        Vector items = null;
        if (chooseType == COLUMN_CHOOSE_TYPE)
            items = currentRule.columns;
        else if (chooseType == ENTITY_CHOOSE_TYPE)
            items = currentRule.entities;
        else if (chooseType == PURPOSE_CHOOSE_TYPE)
            items = currentRule.purposes;
        else if (chooseType == ACCESSOR_CHOOSE_TYPE)
            items = currentRule.accessors;
        else if (chooseType == RECIPIENT_CHOOSE_TYPE)
            items = currentRule.recipients;
        else if (chooseType == BACKLOG_TABLE_CHOOSE_TYPE)
            items = resourceManager.getTablesWithBacklog(databaseName);
        else if (chooseType == ENTITY_DEFINITION_CHOOSE_TYPE) {
            items = resourceManager.getNonEntities(databaseName);
        }

        //Utility.out(this, "showDialog():", chooseType + ": " + items);

        Collections.sort(items);
        itemsInScopeList.setListData(items);
        itemsInScopeOriginalList.setListData(items);

        Vector availableItems = null;

        //        if (chooseType == COLUMN_CHOOSE_TYPE) {
        //            //availableItems = resourceManager.getEntries(databaseName,
        //            // ResourceManager.COLUMNS_TABLE);
        //            availableItems =
        // resourceManager.getConnectedDatabaseTables(databaseName);
        //
        //            Collections.sort(availableItems);
        //            schemaList.setListData(availableItems);
        //
        //            itemsNotInScopeList.setListData(empty);
        //            itemsInScopeList.setListData(items);
        //
        //            // Change HERE
        //               System.out.println("+++ "+currentRule);
        //            if (items.isEmpty())
        //                selectedCombinationList.setListData(empty);
        //            else {
        //                selectedCombinationList.setListData(items);
        //                for (int i=0;i<items.size();i++)
        //                    System.out.println("----------------->
        // "+((ColumnDescriptor)items.get(i)).toVerboseString());
        //            }
        //            currentItemsInScope = items;
        //       }
        if (chooseType == COLUMN_CHOOSE_TYPE) {
            //availableItems = resourceManager.getEntries(databaseName,
            // ResourceManager.COLUMNS_TABLE);
            availableItems = resourceManager.getConnectedDatabaseTables(databaseName);

            Collections.sort(availableItems);
            schemaList.setListData(availableItems);

            itemsNotInScopeList.setListData(empty);
            itemsInScopeList.setListData(empty);

            if (items.isEmpty())
                selectedCombinationList.setListData(empty);
            else {
				if (wizardType == Wizard.RULE_WIZARD_TYPE)
				{
                	((JCheckBoxList)selectedCombinationList).setModelForPseudonym(items);
                	selectedCombinationList.setListData(addCheckboxesToList(items));
				}
				else selectedCombinationList.setListData(items);
            }

            currentItemsInScope = new Vector();

            for (int i = 0; i < items.size(); i++) {
                final ColumnDescriptor column = (ColumnDescriptor) items.get(i);
                final boolean verbose = true;
                try {
                    final ColumnDescriptor clonedColumn = column.clone(verbose);
                    currentAllColumns.add(clonedColumn);
                }
                catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
            }
            Collections.sort(currentAllColumns);
			if (wizardType == Wizard.RULE_WIZARD_TYPE)
			{
                ((JCheckBoxList)selectedCombinationList).setModelForPseudonym(currentAllColumns);
                selectedCombinationList.setListData(addCheckboxesToList(currentAllColumns));
			}
			else selectedCombinationList.setListData(currentAllColumns);

        }
        else if (chooseType == BACKLOG_TABLE_CHOOSE_TYPE) {
            availableItems = resourceManager.getTablesWithoutBacklog(databaseName);
            Collections.sort(availableItems);

            items = resourceManager.getTablesWithBacklog(databaseName);
            Collections.sort(items);

            itemsNotInScopeList.setListData(availableItems);
            itemsInScopeList.setListData(items);

            currentItemsInScope = items;
        }
        else if (chooseType == ENTITY_DEFINITION_CHOOSE_TYPE) {
            availableItems = resourceManager.getNonEntities(databaseName);
            Collections.sort(availableItems);

            items = resourceManager.getEntities(databaseName);
            Collections.sort(items);

            itemsNotInScopeList.setListData(availableItems);
            itemsInScopeList.setListData(items);

            currentItemsInScope = items;
            printDebug("showDialog()");
        }
        else {
            if (chooseType == ENTITY_CHOOSE_TYPE)
                availableItems = resourceManager.getEntries(databaseName, ResourceManager.TABLE_ENTITIES);
            else if (chooseType == PURPOSE_CHOOSE_TYPE)
                availableItems = resourceManager.getEntries(databaseName, ResourceManager.TABLE_PURPOSES);
            else if (chooseType == ACCESSOR_CHOOSE_TYPE)
                availableItems = resourceManager.getEntries(databaseName, ResourceManager.TABLE_ACCESSORS);
            else if (chooseType == RECIPIENT_CHOOSE_TYPE)
                availableItems = resourceManager.getEntries(databaseName, ResourceManager.TABLE_RECIPIENTS);

            final int numItems = items.size();
            for (int i = 0; i < numItems; i++)
                availableItems.remove(items.elementAt(i));

            Collections.sort(availableItems);
            Collections.sort(items);

            itemsNotInScopeList.setListData(availableItems);
            currentItemsInScope = items;
        }

        createNewTextField.setText("");
        updateButtons();
        setVisible(true);
    }

    class ActionAdapter implements ActionListener, ListSelectionListener {

        /**
         * This event is only needed for the column choose dialog, where the
         * columns for a selected table name are displayed.
         */
        public void valueChanged(ListSelectionEvent e) {
            if (chooseType == COLUMN_CHOOSE_TYPE) {
                if (e.getSource() == schemaList) {
                    itemsNotInScopeList.setListData(empty);
                    itemsInScopeList.setListData(empty);

                    final TableDescriptor selectedTable = (TableDescriptor) schemaList.getSelectedValue();
                    final Vector inScope = new Vector();
                    final Vector notInScope = new Vector();

                    if (selectedTable != null) {
                        final Vector columns = resourceManager.getDatabaseColumns(selectedTable);
                        for (int i = 0; i < columns.size(); i++) {
                            final ColumnDescriptor columnDescriptor = (ColumnDescriptor) columns.elementAt(i);
                            //if (resourceManager.isInScope(policyName,
                            // columnDescriptor) ||
                            // currentColumnsInScope.indexOf(columnDescriptor)
                            // != -1)

                            if (currentItemsInScope.indexOf(columnDescriptor) != -1)
                                inScope.add(columnDescriptor);
                            else
                                notInScope.add(columnDescriptor);
                        }
                    }
                    Collections.sort(inScope);
                    itemsInScopeList.setListData(inScope);
                    Collections.sort(notInScope);
                    itemsNotInScopeList.setListData(notInScope);
                }
            }
            /*******************************************************************
             * if (e.getSource() == itemsNotInScopeList) { // Make sure that
             * only this item is selcted while any selection // in the other
             * list is cleared.
             *
             * //if (!itemsNotInScopeList.isSelectedIndex(e.getFirstIndex())) //
             * itemsNotInScopeList.setSelectedIndex(e.getFirstIndex()); if
             * (!itemsInScopeList.isSelectionEmpty()) {
             * itemsInScopeList.clearSelection(); } } else if (e.getSource() ==
             * itemsInScopeList) { // Dito.
             * //itemsInScopeList.setSelectedIndex(e.getFirstIndex());
             *
             * //if (!itemsInScopeList.isSelectedIndex(e.getFirstIndex())) //
             * itemsInScopeList.setSelectedIndex(e.getFirstIndex()); if
             * (!itemsNotInScopeList.isSelectionEmpty())
             * itemsNotInScopeList.clearSelection(); }
             ******************************************************************/
            updateButtons();
        }

        public void actionPerformed(ActionEvent e) {

            /*******************************************************************
             * // >> if (e.getSource() == l2rAllButton) { // add to
             * itemsInScopeList ListModel originalItemsInScope =
             * itemsInScopeList.getModel(); DefaultListModel newItemsInScope =
             * new DefaultListModel(); int numOriginalItemsInScope =
             * originalItemsInScope.getSize();
             *
             * for (int i = 0; i < numOriginalItemsInScope; i++)
             * newItemsInScope.add(originalItemsInScope.getElementAt(i)); // add
             * from itemsNotInScopeList ListModel originalItemsNotInScopeList =
             * itemsNotInScopeList.getModel();
             *
             * int numOriginalItemsNotInScopeList =
             * originalItemsNotInScopeList.getSize(); for (int i = 0; i <
             * numOriginalItemsNotInScopeList; i++) {
             * newItemsInScope.add(originalItemsNotInScopeList.getElementAt(i));
             * currentItemsInScope.add(originalItemsNotInScopeList.getElementAt(i)); }
             *
             * itemsNotInScopeList.setListData(empty);
             * itemsInScopeList.setModel(newItemsInScope);
             * //nextButton.setEnabled((newItemsInScope.size() != 0)); }
             ******************************************************************/

            // >>
            if (e.getSource() == l2rAllButton) {
                printDebug("BEFORE >>");

                // Add the original elements to the temporary vector.
                final ListModel originalItemsInScope = itemsInScopeList.getModel();
                final DefaultListModel newItemsInScope = new DefaultListModel();

                for (int i = 0; i < originalItemsInScope.getSize(); i++)
                    newItemsInScope.addElement(originalItemsInScope.getElementAt(i));

                // add from itemsNotInScopeList
                final ListModel originalItemsNotInScopeList = itemsNotInScopeList.getModel();
                for (int i = 0; i < originalItemsNotInScopeList.getSize(); i++) {
                    newItemsInScope.addElement(originalItemsNotInScopeList.getElementAt(i));
                    currentItemsInScope.add(originalItemsNotInScopeList.getElementAt(i));
                }

                // Sort the temporary vector and copy it to the list.
                itemsNotInScopeList.setListData(empty);
                itemsInScopeList.setModel(sort(newItemsInScope));
                printDebug("AFTER >>");
            }

            // <<
            else if (e.getSource() == r2lAllButton) {
                printDebug("BEFORE <<");
                // Select each element, one after another.
                // Move the elements from one list to the other.
                final Vector newItemsNotInScopeVector = new Vector();
                for (int i = 0; i < itemsInScopeList.getModel().getSize(); i++) {
                    itemsInScopeList.setSelectedIndex(i);
                    newItemsNotInScopeVector.add(itemsInScopeList.getSelectedValue());
                }
                currentItemsInScope.removeAll(newItemsNotInScopeVector);

                // Sort the temporary vector and copy it to the list.
                Collections.sort(newItemsNotInScopeVector);

                final DefaultListModel newItemsNotInScope = new DefaultListModel();
                for (int i = 0; i < newItemsNotInScopeVector.size(); i++)
                    newItemsNotInScope.addElement(newItemsNotInScopeVector.get(i));

                itemsNotInScopeList.setModel(newItemsNotInScope);
                itemsInScopeList.setListData(empty);

                printDebug("AFTER <<");

                /***************************************************************
                 * DefaultListModel newItemsNotInScope = new DefaultListModel();
                 *
                 *
                 * for (int i = 0; i < numOriginalItemsNotInScope; i++)
                 * newItemsNotInScope.add(originalItemsNotInScope.getElementAt(i)); //
                 * add from itemsInScopeList ListModel originalItemsInScope =
                 * itemsInScopeList.getModel();
                 *
                 *
                 * for (int i = 0; i < numOriginalItemsInScope; i++) {
                 * newItemsNotInScope.add(originalItemsInScope.getElementAt(i));
                 * currentItemsInScope.remove(originalItemsInScope.getElementAt(i)); }
                 *
                 * itemsNotInScopeList.setModel(newItemsNotInScope);
                 * itemsInScopeList.setListData(empty);
                 **************************************************************/
            }
            /*
             * else if (e.getSource() == r2lAllButton) { final Vector
             * newItemsNotInScopeVector = new Vector();
             *
             * printDebug("BEFORE < <");
             *
             * final ListModel originalItemsNotInScope =
             * itemsNotInScopeList.getModel(); for (int i = 0; i <
             * originalItemsNotInScope.getSize(); i++)
             * newItemsNotInScopeVector.add(originalItemsNotInScope.getElementAt(i));
             *
             * printDebug("HERE 1"); // Add from itemsInScopeList final
             * ListModel originalItemsInScope = itemsInScopeList.getModel(); for
             * (int i = 0; i < originalItemsInScope.getSize(); i++) {
             * newItemsNotInScopeVector.add(originalItemsInScope.getElementAt(i));
             * currentItemsInScope.remove(originalItemsInScope.getElementAt(i)); }
             *
             * printDebug("HERE 2"); // Sort the temporary vector and copy it to
             * the list. Collections.sort(newItemsNotInScopeVector); final
             * DefaultListModel newItemsNotInScope = new DefaultListModel(); for
             * (int i = 0; i < newItemsNotInScopeVector.size(); i++)
             * newItemsNotInScope.addElement(newItemsNotInScopeVector.get(i));
             *
             * printDebug("HERE 3");
             *
             * itemsNotInScopeList.setModel(newItemsNotInScope);
             * itemsInScopeList.setListData(empty);
             *
             * printDebug("AFTER < <"); }
             */
            // itemsNotInScopeList > itemsInScopeList
            else if (e.getSource() == l2rOneButton) {
                final Object selected = itemsNotInScopeList.getSelectedValue();
                //String selected = (String)
                // itemsNotInScopeList.getSelectedValue();
                if (selected != null) {
                    // add to itemsInScopeList
                    ListModel originalItemsInScope = itemsInScopeList.getModel();
                    DefaultListModel newItemsInScope = new DefaultListModel();
                    int numOriginalItemsInScope = originalItemsInScope.getSize();
                    newItemsInScope.addElement(selected);
                    currentItemsInScope.add(selected);

                    for (int i = 0; i < numOriginalItemsInScope; i++)
                        newItemsInScope.addElement(originalItemsInScope.getElementAt(i));

                    // remove from itemsNotInScopeList
                    ListModel originalItemsNotInScope = itemsNotInScopeList.getModel();
                    DefaultListModel newItemsNotInScope = new DefaultListModel();
                    int numOriginalItemsNotInScopeList = originalItemsNotInScope.getSize();
                    for (int i = 0; i < numOriginalItemsNotInScopeList; i++) {
                        final Object item = originalItemsNotInScope.getElementAt(i);
                        if (!selected.equals(item))
                            newItemsNotInScope.addElement(item);
                    }

                    itemsNotInScopeList.setModel(sort(newItemsNotInScope));
                    itemsInScopeList.setModel(sort(newItemsInScope));
                    //nextButton.setEnabled((newItemsInScope.size() != 0));
                }
            }

            // itemsNotInScopeList < itemsInScopeList
            else if (e.getSource() == r2lOneButton) {

                final Object selected = itemsInScopeList.getSelectedValue();
                if (selected != null) {
                    // add to itemsNotInScopeList
                    ListModel originalItemsNotInScope = itemsNotInScopeList.getModel();
                    DefaultListModel newItemsNotInScope = new DefaultListModel();
                    int numOriginalItemsNotInScope = originalItemsNotInScope.getSize();
                    newItemsNotInScope.addElement(selected);
                    currentItemsInScope.remove(selected);
                    for (int i = 0; i < numOriginalItemsNotInScope; i++)
                        newItemsNotInScope.addElement(originalItemsNotInScope.getElementAt(i));

                    // remove from itemsInScopeList
                    ListModel originalItemsInScope = itemsInScopeList.getModel();
                    DefaultListModel newItemsInScope = new DefaultListModel();
                    int numOriginalItemsInScope = originalItemsInScope.getSize();
                    for (int i = 0; i < numOriginalItemsInScope; i++) {
                        final Object item = originalItemsInScope.getElementAt(i);
                        if (!selected.equals(item))
                            newItemsInScope.addElement(item);
                    }

                    itemsNotInScopeList.setModel(sort(newItemsNotInScope));
                    itemsInScopeList.setModel(sort(newItemsInScope));

                    //nextButton.setEnabled((newItemsInScope.size() != 0));
                }
            }

            else if (e.getSource() == backButton) {
                setVisible(false);

                if (chooseType == ENTITY_CHOOSE_TYPE)
                    wizard.changeDialog(Wizard.ENTER_NAME, currentRule);
                else if (chooseType == COLUMN_CHOOSE_TYPE) {
                    if (wizard.getType() == Wizard.RULE_WIZARD_TYPE)
                        wizard.changeDialog(Wizard.ENTER_ENTITIES, currentRule);
                    else
                        wizard.changeDialog(Wizard.ENTER_NAME, currentRule);
                }
                else if (chooseType == PURPOSE_CHOOSE_TYPE) {
                    if (wizard.getType() == Wizard.TASK_WIZARD_TYPE) {
                        System.out.println();
                        System.out.println("1### ### ### purpose ### ### ### "
                                + ((ResourceManager.Task) currentRule).toString());
                        for (int i = 0; i < ((ResourceManager.Task) currentRule).columns.size(); i++)
                            System.out.println("1### ### ### purpose ### ### ### "
                                    + ((ColumnDescriptor) ((ResourceManager.Task) currentRule).columns.get(i))
                                            .toVerboseString());
                        System.out.println();
                    }
						wizard.changeDialog(Wizard.ENTER_COLUMNS, currentRule);
                }
                else if (chooseType == RECIPIENT_CHOOSE_TYPE)
                    wizard.changeDialog(Wizard.ENTER_PURPOSES, currentRule);
                else if (chooseType == ACCESSOR_CHOOSE_TYPE)
                    wizard.changeDialog(Wizard.ENTER_RECIPIENTS, currentRule);
            }
            else if (e.getSource() == nextButton) {
                setVisible(false);

                if (chooseType == ENTITY_CHOOSE_TYPE) {
                    currentRule.entities = currentItemsInScope;
                    wizard.changeDialog(Wizard.ENTER_COLUMNS, currentRule);
                }
                else if (chooseType == COLUMN_CHOOSE_TYPE) {
                    if (wizardType == Wizard.TASK_WIZARD_TYPE)
                        currentRule.columns = currentAllColumns;
                    else {

                        currentRule.columns = currentAllColumns;
					}

					//if (wizardType == Wizard.RULE_WIZARD_TYPE)
					//	wizard.changeDialog(Wizard.ENTER_PSEUDONYMS, currentRule);
					//else
                    	wizard.changeDialog(Wizard.ENTER_PURPOSES, currentRule);
                }
                else if (chooseType == PURPOSE_CHOOSE_TYPE) {
                    System.out.println("===> " + currentItemsInScope);
                    currentRule.purposes = currentItemsInScope;
                    wizard.changeDialog(Wizard.ENTER_RECIPIENTS, currentRule);
                }
                else if (chooseType == RECIPIENT_CHOOSE_TYPE) {
                    currentRule.recipients = currentItemsInScope;
                    wizard.changeDialog(Wizard.ENTER_ACCESSORS, currentRule);
                }
                else if (chooseType == ACCESSOR_CHOOSE_TYPE) {
                    currentRule.accessors = currentItemsInScope;
                    
                    /*
                     * amol pujari 09/10/2006
                     * in edit mode "currentRule.condition" should not be blanked so commenting out this stmt below
                     */
                    //currentRule.condition = "";
                    wizard.changeDialog(Wizard.ENTER_CONDITIONS_ADVANCED, currentRule);
                }
                else if (chooseType == BACKLOG_TABLE_CHOOSE_TYPE) {
                    YesNoDialog confirmDialog = new YesNoDialog(YesNoDialog.CREATE_AND_DROP_BACKLOG_TABLES_DIALOG_TYPE,
                            controlCenter, null);
                    confirmDialog.show();
                    /***********************************************************
                     * for (int i = 0; i <
                     * itemsInScopeList.getModel().getSize(); i++) { final
                     * TableDescriptor table = (TableDescriptor)
                     * itemsInScopeList.getModel().getElementAt(i);
                     * resourceManager.createBacklogTable(table); } for (int i =
                     * 0; i < itemsNotInScopeList.getModel().getSize(); i++) {
                     * final TableDescriptor table = (TableDescriptor)
                     * itemsNotInScopeList.getModel().getElementAt(i);
                     * resourceManager.dropBacklogTable(table); }
                     **********************************************************/
                }
                else if (chooseType == ENTITY_DEFINITION_CHOOSE_TYPE) {
                    YesNoDialog confirmDialog = new YesNoDialog(YesNoDialog.ENTITY_DEFINITION_DIALOG_TYPE,
                            controlCenter, databaseName, null);
                    confirmDialog.show();
                }

                setVisible(false); // close this window
            }
            else if (e.getSource() == createNewButton) {
                final String newEntry = createNewTextField.getText();

                Vector items = null;

                if (chooseType == PURPOSE_CHOOSE_TYPE) {
                    if (resourceManager.addEntry(databaseName, ResourceManager.TABLE_PURPOSES, newEntry)) {
                        items = resourceManager.getEntries(databaseName, ResourceManager.TABLE_PURPOSES);
                    }
                }
                else if (chooseType == ACCESSOR_CHOOSE_TYPE) {
                    if (resourceManager.addEntry(databaseName, ResourceManager.TABLE_ACCESSORS, newEntry)) {
                        items = resourceManager.getEntries(databaseName, ResourceManager.TABLE_ACCESSORS);
                    }
                }
                else if (chooseType == RECIPIENT_CHOOSE_TYPE) {
                    if (resourceManager.addEntry(databaseName, ResourceManager.TABLE_RECIPIENTS, newEntry)) {
                        items = resourceManager.getEntries(databaseName, ResourceManager.TABLE_RECIPIENTS);
                    }
                }
                else if (chooseType == ENTITY_CHOOSE_TYPE) {
                    if (resourceManager.addEntry(databaseName, ResourceManager.TABLE_ENTITIES, newEntry)) {
                        items = resourceManager.getEntries(databaseName, ResourceManager.TABLE_ENTITIES);
                    }
                }
                else {
                    (new Utility()).applicationError(this, "actionPerformed()", "Cannot create new " + chooseObjectName
                            + ".");
                }

                final Vector itemsInScope = new Vector();
                for (int i = 0; i < itemsInScopeList.getModel().getSize(); i++)
                    itemsInScope.add(itemsInScopeList.getModel().getElementAt(i));

                
                items.removeAll(itemsInScope);
                
                itemsNotInScopeList.setListData(items);
                createNewTextField.setText("");
            }
            else if (e.getSource() == addButton) {
                //
                // Retrieve the old elements from the list.
                //
                final ListModel listModel = itemsInScopeList.getModel();
                for (int i = 0; i < listModel.getSize(); i++) {
                    final ColumnDescriptor column = (ColumnDescriptor) listModel.getElementAt(i);
                    final boolean verbose = true;
                    try {
                        final ColumnDescriptor clonedColumn = column.clone(verbose);

                        for (int j = 0; j < currentAllColumns.size(); j++)
                            System.out.println("%%% " + ((ColumnDescriptor) currentAllColumns.get(j)).toVerboseString()
                                    + " " + clonedColumn.toVerboseString() + " "
                                    + currentAllColumns.get(j).equals(clonedColumn));
                        if (!currentAllColumns.contains(clonedColumn)) {
                            currentAllColumns.add(clonedColumn);
                        }
                    }
                    catch (CloneNotSupportedException e2) {
                        e2.printStackTrace();
                    }
                }
                Collections.sort(currentAllColumns);

				if (wizardType == Wizard.RULE_WIZARD_TYPE)
				{
               	 	((JCheckBoxList)selectedCombinationList).setModelForPseudonym(currentAllColumns);
               	 	selectedCombinationList.setListData(addCheckboxesToList(currentAllColumns));
				}
				else selectedCombinationList.setListData(currentAllColumns);
            }
            else if (e.getSource() == deleteButton) {

				if (wizardType == Wizard.RULE_WIZARD_TYPE)
				{
				 	ColumnDescriptor selectedColumn = (ColumnDescriptor)((JCheckBoxList)selectedCombinationList).getSelectedColumnDescriptor();
                	currentAllColumns.remove(selectedColumn);
               	 	((JCheckBoxList)selectedCombinationList).setModelForPseudonym(currentAllColumns);
               	 	selectedCombinationList.setListData(addCheckboxesToList(currentAllColumns));
				}
				else
				{
                	ColumnDescriptor selectedColumn = (ColumnDescriptor) selectedCombinationList.getSelectedValue();
                	currentAllColumns.remove(selectedColumn);
					selectedCombinationList.setListData(currentAllColumns);
				}
            }
            else if (e.getSource() == cancelButton) {
                setVisible(false);
            }
            updateButtons();
        }
    }

    private void printDebug(String text) {
        System.out.println("\n---------------- " + text + ":");
        System.out.println("\nitems not in scope:");
        for (int i = 0; i < itemsNotInScopeList.getModel().getSize(); i++)
            System.out.println(itemsNotInScopeList.getModel().getElementAt(i));
        System.out.println("\nitems in scope:");
        for (int i = 0; i < itemsInScopeList.getModel().getSize(); i++)
            System.out.println(itemsInScopeList.getModel().getElementAt(i));
        System.out.println("\ncurrent items in scope:");
        for (int i = 0; i < currentItemsInScope.size(); i++)
            System.out.println(currentItemsInScope.get(i));
    }

    /**
     * Should be called after each interaction and when a dialog is displayed
     * for the first time. Checks which buttons should be enabled and disabled.
     */
    public void updateButtons() {
        checkInput();

        l2rOneButton.setEnabled(itemsNotInScopeList.getSelectedValue() != null);
        r2lOneButton.setEnabled(itemsInScopeList.getSelectedValue() != null);
        l2rAllButton.setEnabled(itemsNotInScopeList.getModel().getSize() != 0);
        r2lAllButton.setEnabled(itemsInScopeList.getModel().getSize() != 0);

        //
        // Next button
        //
        if ((chooseType == BACKLOG_TABLE_CHOOSE_TYPE) || (chooseType == BACKLOG_TABLE_CHOOSE_TYPE)) {
            final Vector itemsInScopeVector = getItemsInScope();
            final Vector itemsInScopeOriginalVector = getItemsInScopeOriginal();
            nextButton.setEnabled(!itemsInScopeVector.equals(itemsInScopeOriginalVector));
        }
        else if (chooseType == COLUMN_CHOOSE_TYPE) {
            nextButton.setEnabled(selectedCombinationList.getModel().getSize() != 0);
        }
        else {
            nextButton.setEnabled(itemsInScopeList.getModel().getSize() != 0);
        }

        if ((wizardType == Wizard.TASK_WIZARD_TYPE) && (chooseType == COLUMN_CHOOSE_TYPE)) {
            //
            // Add button
            //
            final boolean allSelected = (itemsInScopeList.getModel().getSize() != 0);
            if (allSelected) {
                //
                // Retrieve the already existing columns.
                //
                ListModel listModel = selectedCombinationList.getModel();
                TreeSet existingCombinations = new TreeSet();
                for (int i = 0; i < listModel.getSize(); i++) {
                    final ColumnDescriptor combination = (ColumnDescriptor) listModel.getElementAt(i);
                    existingCombinations.add(combination);
                }

                boolean foundDuplicate = false;

                //
                // Retrieve the newly selected existing columns and check for
                // duplicates.
                //
                listModel = itemsInScopeList.getModel();
                for (int i = 0; !foundDuplicate && (i < listModel.getSize()); i++) {
                    final ColumnDescriptor newCombination = (ColumnDescriptor) listModel.getElementAt(i);
                    foundDuplicate = existingCombinations.contains(newCombination);
                }

                addButton.setEnabled(!foundDuplicate);
            }
            else
                addButton.setEnabled(false);

            //
            // Delete button
            //
            deleteButton.setEnabled((selectedCombinationList.getModel().getSize() != 0)
                    && !selectedCombinationList.isSelectionEmpty());
        }
    }

	private Vector addCheckboxesToList(Vector vec)
	{
        Vector columnsWithPseudonymStatus = new Vector();
        for (int i = 0; i < vec.size(); i++)
        {
			ColumnDescriptor col = (ColumnDescriptor)vec.elementAt(i);
			JCheckBox status = new JCheckBox(col.toString(), col.pseudonym);
         	// according to Jerry we can encrypt all data types (by converting them into char)
			//status.setEnabled(col.typeName.equalsIgnoreCase("VARCHAR"));
	        columnsWithPseudonymStatus.add(status);
		}

		return columnsWithPseudonymStatus;
	}

    /**
     * Sort the list items.
     */
    public DefaultListModel sort(DefaultListModel listModel) {
        final Vector vector = new Vector();
        for (int i = 0; i < listModel.getSize(); i++)
            vector.add(listModel.getElementAt(i));

        Collections.sort(vector);

        final Vector duplicateFreeVector = new Vector();
        if (vector.size() > 0) {
            Object previous = vector.get(0);
            duplicateFreeVector.add(previous);

            for (int i = 1; i < vector.size(); i++) {
                final Object next = vector.get(i);
                if (!next.equals(previous))
                    duplicateFreeVector.add(next);
                previous = next;
            }
        }

        final DefaultListModel duplicateFreeListModel = new DefaultListModel();
        for (int i = 0; i < duplicateFreeVector.size(); i++)
            duplicateFreeListModel.addElement(duplicateFreeVector.get(i));

        return duplicateFreeListModel;
    }

    private void checkInput() {
        final String value = createNewTextField.getText().trim();
        final boolean isEmpty = value.equals("");

        final Vector itemsInScope = getItemsInScope();
        final Vector itemsNotInScope = getItemsNotInScope();
        final boolean alreadyExists = itemsInScope.contains(value) || itemsNotInScope.contains(value);

        createNewButton.setEnabled(!isEmpty && !alreadyExists);
    }

    private void checkInput(KeyEvent e) {
        checkInput();
    }
}
