package controlCenter;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
import java.awt.*;
import javax.swing.JPanel;
import javax.swing.BorderFactory;
import javax.swing.border.Border;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

public class ScopeSelectionDialog extends JDialog {
    private ResourceManager resourceManager;
    private JList schemaList;
    private JList columnsNotInScopeList;
    private JList columnsInScopeList;

    private JButton leftToRightAllButton;
    private JButton leftToRightOneButton;
    private JButton rightToLeftOneButton;
    private JButton rightToLeftAllButton;
    private JButton doneButton;
    private Version version;
    private Vector empty = new Vector();
    private Vector currentColumnsInScope = new Vector();

    public ScopeSelectionDialog(ResourceManager resourceManager) {
    	super(resourceManager.controlCenter,true);
        this.resourceManager = resourceManager;

        Container contentPane = this.getContentPane();
        ActionAdapter actionAdapter = new ActionAdapter();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        setTitle("Scope");

        JLabel topLabel = new JLabel("Select scope for this policy");

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new FlowLayout());

        JPanel schemaPanel = new JPanel();
        schemaPanel.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(Color.white, new Color(165, 163, 151)),
                "Table names"));
        schemaList = new JList();
        schemaList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        schemaList.addListSelectionListener(actionAdapter);
        JScrollPane schemaPane = new JScrollPane(schemaList);
        schemaPane.setPreferredSize(new Dimension(150, 200));
        schemaPanel.add(schemaPane);
        centerPanel.add(schemaPanel);

        JPanel columnNotInScopePanel = new JPanel();
        columnNotInScopePanel.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(Color.white, new Color(165,
                163, 151)), "Columns not in scope"));
        columnsNotInScopeList = new JList();
        columnsNotInScopeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane columnsNotInScopePane = new JScrollPane(columnsNotInScopeList);
        columnsNotInScopePane.setPreferredSize(new Dimension(150, 200));
        columnNotInScopePanel.add(columnsNotInScopePane);
        centerPanel.add(columnNotInScopePanel);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        leftToRightAllButton = new JButton(">>");
        leftToRightAllButton.addActionListener(actionAdapter);
        leftToRightOneButton = new JButton("> ");
        leftToRightOneButton.addActionListener(actionAdapter);
        rightToLeftOneButton = new JButton("< ");
        rightToLeftOneButton.addActionListener(actionAdapter);
        rightToLeftAllButton = new JButton("<<");
        rightToLeftAllButton.addActionListener(actionAdapter);
        buttonPanel.add(leftToRightAllButton);
        buttonPanel.add(leftToRightOneButton);
        buttonPanel.add(rightToLeftOneButton);
        buttonPanel.add(rightToLeftAllButton);
        centerPanel.add(buttonPanel);

        JPanel columnInScopePanel = new JPanel();
        columnInScopePanel.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(Color.white, new Color(165, 163,
                151)), "Columns in scope"));
        columnsInScopeList = new JList();
        columnsInScopeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane columnsInScopePane = new JScrollPane(columnsInScopeList);
        columnsInScopePane.setPreferredSize(new Dimension(150, 200));
        columnInScopePanel.add(columnsInScopePane);
        centerPanel.add(columnInScopePanel);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout());
        bottomPanel.setBorder(BorderFactory.createEtchedBorder());
        doneButton = new JButton("Done");
        doneButton.addActionListener(actionAdapter);
        bottomPanel.add(doneButton);

        contentPane.add(topLabel);
        contentPane.add(centerPanel);
        contentPane.add(bottomPanel);

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(new Dimension(600, 350));
        setModal(true);
        setLocation(screen.width / 5, screen.height / 5);

    }

    public void showDialog(Version version) {
        this.version = version;
        Vector databaseTables = resourceManager.getConnectedDatabaseTables(version.databaseName);

        currentColumnsInScope = resourceManager.getColumnsInScope(version.databaseName, version.collectionName);
        schemaList.setListData(databaseTables);
        columnsInScopeList.setListData(empty);
        columnsNotInScopeList.setListData(empty);
        setVisible(true);
    }

    class ActionAdapter implements ActionListener, ListSelectionListener {
        public void valueChanged(ListSelectionEvent e) {
            TableDescriptor selectedTable = (TableDescriptor) schemaList.getSelectedValue();
            Vector inScope = new Vector();
            Vector notInScope = new Vector();

            if (selectedTable != null) {
                Vector columns = resourceManager.getDatabaseColumns(selectedTable);
                int numColumns = columns.size();

                for (int i = 0; i < numColumns; i++) {
                    ColumnDescriptor columnDescriptor = (ColumnDescriptor) columns.elementAt(i);
                    if (resourceManager.isInScope(version.collectionName, columnDescriptor)
                            || currentColumnsInScope.indexOf(columnDescriptor) != -1)
                        inScope.add(columnDescriptor);
                    else
                        notInScope.add(columnDescriptor);
                }
            }
            columnsInScopeList.setListData(inScope);
            columnsNotInScopeList.setListData(notInScope);
        }

        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == leftToRightAllButton) // >>
            {
                // add to columnsInScopeList
                ListModel origColumnsInScope = columnsInScopeList.getModel();
                DefaultListModel newColumnsInScope = new DefaultListModel();
                int numOrigColumnsInScope = origColumnsInScope.getSize();

                for (int i = 0; i < numOrigColumnsInScope; i++)
                    newColumnsInScope.addElement(origColumnsInScope.getElementAt(i));

                // add from columnsNotInScopeList
                ListModel origColumnsNotInScopeList = columnsNotInScopeList.getModel();

                int numOrigColumnsNotInScopeList = origColumnsNotInScopeList.getSize();
                for (int i = 0; i < numOrigColumnsNotInScopeList; i++) {
                    newColumnsInScope.addElement(origColumnsNotInScopeList.getElementAt(i));
                    currentColumnsInScope.addElement(origColumnsNotInScopeList.getElementAt(i));
                }

                columnsNotInScopeList.setListData(empty);
                columnsInScopeList.setModel(newColumnsInScope);

            }
            else if (e.getSource() == leftToRightOneButton) // columnsNotInScopeList
            // >
            // columnsInScopeList
            {
                ColumnDescriptor selected = (ColumnDescriptor) columnsNotInScopeList.getSelectedValue();
                if (selected != null) {
                    // add to columnsInScopeList
                    ListModel origColumnsInScope = columnsInScopeList.getModel();
                    DefaultListModel newColumnsInScope = new DefaultListModel();
                    int numOrigColumnsInScope = origColumnsInScope.getSize();
                    newColumnsInScope.addElement(selected);
                    currentColumnsInScope.addElement(selected);
                    for (int i = 0; i < numOrigColumnsInScope; i++)
                        newColumnsInScope.addElement(origColumnsInScope.getElementAt(i));

                    // remove from columnsNotInScopeList
                    ListModel origColumnsNotInScope = columnsNotInScopeList.getModel();
                    DefaultListModel newColumnsNotInScope = new DefaultListModel();
                    int numOrigColumnsNotInScopeList = origColumnsNotInScope.getSize();
                    for (int i = 0; i < numOrigColumnsNotInScopeList; i++) {
                        ColumnDescriptor table = (ColumnDescriptor) origColumnsNotInScope.getElementAt(i);
                        if (!selected.equals(table))
                            newColumnsNotInScope.addElement(table);
                    }

                    columnsNotInScopeList.setModel(newColumnsNotInScope);
                    columnsInScopeList.setModel(newColumnsInScope);
                }
            }
            else if (e.getSource() == rightToLeftOneButton) // columnsNotInScopeList
            // <
            // columnsInScopeList
            {

                ColumnDescriptor selected = (ColumnDescriptor) columnsInScopeList.getSelectedValue();
                if (selected != null) {
                    // add to columnsNotInScopeList
                    ListModel origColumnsNotInScope = columnsNotInScopeList.getModel();
                    DefaultListModel newColumnsNotInScope = new DefaultListModel();
                    int numOrigColumnsNotInScope = origColumnsNotInScope.getSize();
                    newColumnsNotInScope.addElement(selected);
                    currentColumnsInScope.remove(selected);
                    for (int i = 0; i < numOrigColumnsNotInScope; i++)
                        newColumnsNotInScope.addElement(origColumnsNotInScope.getElementAt(i));

                    // remove from columnsInScopeList
                    ListModel origColumnsInScope = columnsInScopeList.getModel();
                    DefaultListModel newColumnsInScope = new DefaultListModel();
                    int numOrigColumnsInScope = origColumnsInScope.getSize();
                    for (int i = 0; i < numOrigColumnsInScope; i++) {
                        ColumnDescriptor table = (ColumnDescriptor) origColumnsInScope.getElementAt(i);
                        if (!selected.equals(table))
                            newColumnsInScope.addElement(table);
                    }

                    columnsNotInScopeList.setModel(newColumnsNotInScope);
                    columnsInScopeList.setModel(newColumnsInScope);
                }
            }
            else if (e.getSource() == rightToLeftAllButton) // <<
            {
                // add to columnsNotInScopeList
                ListModel origColumnsNotInScope = columnsNotInScopeList.getModel();
                DefaultListModel newColumnsNotInScope = new DefaultListModel();
                int numOrigColumnsNotInScope = origColumnsNotInScope.getSize();

                for (int i = 0; i < numOrigColumnsNotInScope; i++)
                    newColumnsNotInScope.addElement(origColumnsNotInScope.getElementAt(i));


                // add from columnsInScopeList
                ListModel origColumnsInScope = columnsInScopeList.getModel();

                int numOrigColumnsInScope = origColumnsInScope.getSize();
                for (int i = 0; i < numOrigColumnsInScope; i++) {
                    newColumnsNotInScope.addElement(origColumnsInScope.getElementAt(i));
                    currentColumnsInScope.remove(origColumnsInScope.getElementAt(i));
                }

				columnsNotInScopeList.setModel(newColumnsNotInScope);
                columnsInScopeList.setListData(empty);

            }
            else if (e.getSource() == doneButton) {
                if (resourceManager.updateScope(version.databaseName, version.collectionName, currentColumnsInScope))
                    setVisible(false); // close this window
                else
                    System.err.println("Error encountered while creating scope entries"); // error
            }
        }
    }
}
