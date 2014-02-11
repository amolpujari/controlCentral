package controlCenter;

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

import controlCenter.ChooseDialog.ActionAdapter;

public class QueryDetailsDialog extends JDialog {
    private ResourceManager.Query query;

    private final JScrollPane namePane;
    private final JScrollPane purposePane;
    private final JScrollPane accessorPane;
    private final JScrollPane recipientPane;
    private final JScrollPane isolationPane;
    private final JScrollPane startPane;
    private final JScrollPane endPane;
    private final JScrollPane sqlPane;
    private final JCheckBox showSQLCheckBox;
    private final JButton okButton;
    private final Box buttonGroupBox;

    public QueryDetailsDialog(ResourceManager.Query query, Frame parent) {
    	super(parent,true);
        this.query = query;

        this.setTitle("Query details");

        this.addWindowListener(new MyWindowAdapter(this));
        final ActionAdapter actionAdapter = new ActionAdapter(this);

        final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(screen.width / 5, screen.height / 5);
        final Color backgroundColor = (new JPanel()).getBackground();

        final JTextArea nameTextArea = new JTextArea();
        nameTextArea.setFont(ChooseDialog.NORMAL_FONT);
        nameTextArea.setBackground(backgroundColor);
        nameTextArea.setEditable(false);
        nameTextArea.setText(query.name);
        namePane = new JScrollPane(nameTextArea);
        namePane.setPreferredSize(new Dimension(2 * ChooseDialog.SCROLL_PANEL_DIMENSION_X + 2
                * ChooseDialog.INSETS_PADDING, ChooseDialog.SINGLE_ROW_SCROLL_PANEL_DIMENSION_X));
        final TitledBorder nameTitledBorder = new TitledBorder(ChooseDialog.BORDER, "Query ID");
        nameTitledBorder.setTitleFont(ChooseDialog.BOLD_FONT);
        namePane.setBorder(nameTitledBorder);

        final JTextArea startTextArea = new JTextArea();
        startTextArea.setFont(ChooseDialog.NORMAL_FONT);
        startTextArea.setEditable(false);
        startTextArea.setBackground(backgroundColor);
        startTextArea.setText(query.begin.toString());
        startPane = new JScrollPane(startTextArea);
        startPane.setPreferredSize(new Dimension(ChooseDialog.SCROLL_PANEL_DIMENSION_X, ChooseDialog.SINGLE_ROW_SCROLL_PANEL_DIMENSION_X));
        final TitledBorder startTitledBorder = new TitledBorder(ChooseDialog.BORDER, "Start time");
        startTitledBorder.setTitleFont(ChooseDialog.BOLD_FONT);
        startPane.setBorder(startTitledBorder);

        final JTextArea endTextArea = new JTextArea();
        endTextArea.setFont(ChooseDialog.NORMAL_FONT);
        endTextArea.setEditable(false);
        endTextArea.setBackground(backgroundColor);
        endTextArea.setText(query.end.toString());
        endPane = new JScrollPane(endTextArea);
        endPane.setPreferredSize(new Dimension(ChooseDialog.SCROLL_PANEL_DIMENSION_X, ChooseDialog.SINGLE_ROW_SCROLL_PANEL_DIMENSION_X));
        final TitledBorder endTitledBorder = new TitledBorder(ChooseDialog.BORDER, "End time");
        endTitledBorder.setTitleFont(ChooseDialog.BOLD_FONT);
        endPane.setBorder(endTitledBorder);

        final JTextArea purposeTextArea = new JTextArea();
        purposeTextArea.setFont(ChooseDialog.NORMAL_FONT);
        purposeTextArea.setBackground(backgroundColor);
        purposeTextArea.setText(query.purpose);
        purposeTextArea.setEditable(false);
               purposePane = new JScrollPane(purposeTextArea);
        purposePane.setPreferredSize(new Dimension(ChooseDialog.SCROLL_PANEL_DIMENSION_X,
                ChooseDialog.SINGLE_ROW_SCROLL_PANEL_DIMENSION_X));
        final TitledBorder purposeTitledBorder = new TitledBorder(ChooseDialog.BORDER, "Purpose");
        purposeTitledBorder.setTitleFont(ChooseDialog.BOLD_FONT);
        purposePane.setBorder(purposeTitledBorder);

        final JTextArea accessorTextArea = new JTextArea();
        accessorTextArea.setFont(ChooseDialog.NORMAL_FONT);
        accessorTextArea.setBackground(backgroundColor);
        accessorTextArea.setText(query.accessor);
        accessorTextArea.setEditable(false);
        accessorPane = new JScrollPane(accessorTextArea);
        accessorPane.setPreferredSize(new Dimension(ChooseDialog.SCROLL_PANEL_DIMENSION_X,
                ChooseDialog.SINGLE_ROW_SCROLL_PANEL_DIMENSION_X));
        final TitledBorder accessorTitledBorder = new TitledBorder(ChooseDialog.BORDER, "Accessor");
        accessorTitledBorder.setTitleFont(ChooseDialog.BOLD_FONT);
        accessorPane.setBorder(accessorTitledBorder);

        final JTextArea recipientTextArea = new JTextArea();
        recipientTextArea.setFont(ChooseDialog.NORMAL_FONT);
        recipientTextArea.setBackground(backgroundColor);
        recipientTextArea.setText(query.recipient);
        recipientTextArea.setEditable(false);
        recipientPane = new JScrollPane(recipientTextArea);
        recipientPane.setPreferredSize(new Dimension(ChooseDialog.SCROLL_PANEL_DIMENSION_X,
                ChooseDialog.SINGLE_ROW_SCROLL_PANEL_DIMENSION_X));
        final TitledBorder recipientTitledBorder = new TitledBorder(ChooseDialog.BORDER, "Recipient");
        recipientTitledBorder.setTitleFont(ChooseDialog.BOLD_FONT);
        recipientPane.setBorder(recipientTitledBorder);

        final JTextArea isolationLevelTextArea = new JTextArea();
        isolationLevelTextArea.setFont(ChooseDialog.NORMAL_FONT);
        isolationLevelTextArea.setEditable(false);
        isolationLevelTextArea.setBackground(backgroundColor);
        isolationLevelTextArea.setText(query.isolation.toString());
        isolationPane = new JScrollPane(isolationLevelTextArea);
        isolationPane.setPreferredSize(new Dimension(ChooseDialog.SCROLL_PANEL_DIMENSION_X, ChooseDialog.SINGLE_ROW_SCROLL_PANEL_DIMENSION_X));
        final TitledBorder isolationTitledBorder = new TitledBorder(ChooseDialog.BORDER, "Isolation level");
        isolationTitledBorder.setTitleFont(ChooseDialog.BOLD_FONT);
        isolationPane.setBorder(isolationTitledBorder);

        //
        // SQL text of audit query.
        //
        final JTextArea sqlTextArea = new JTextArea();
        sqlTextArea.setFont(ChooseDialog.COURIER_FONT);
        sqlTextArea.setBackground(backgroundColor);
        sqlTextArea.setEditable(false);
        sqlTextArea.setText(query.text);
        ResourceManager.printSQL("USER QUERY:\n"+query.text);
        sqlPane = new JScrollPane(sqlTextArea);
        sqlPane.setPreferredSize(new Dimension(4 * ChooseDialog.SCROLL_PANEL_DIMENSION_X + 6
                * ChooseDialog.INSETS_PADDING, ChooseDialog.SCROLL_PANEL_DIMENSION_Y));
        final TitledBorder sqlTitledBorder = new TitledBorder(ChooseDialog.BORDER, "SQL query");
        sqlTitledBorder.setTitleFont(ChooseDialog.BOLD_FONT);
        sqlPane.setBorder(sqlTitledBorder);

        showSQLCheckBox = new JCheckBox("Show SQL");
        showSQLCheckBox.setMnemonic(KeyEvent.VK_C);
        showSQLCheckBox.setSelected(false);
        showSQLCheckBox.addActionListener(actionAdapter);

        okButton = new JButton();
        okButton.setText("OK");
        okButton.addActionListener(actionAdapter);

        final ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(okButton);
        buttonGroupBox = Box.createHorizontalBox();
        buttonGroupBox.add(okButton);
		setModal(true);
        showPanel();
    }

    public void showPanel() {
        this.getContentPane().removeAll();

        //
        // Root panel
        //
        GridBagLayout gridBagLayout = new GridBagLayout();
        this.getContentPane().setLayout(gridBagLayout);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(ChooseDialog.INSETS_PADDING, ChooseDialog.INSETS_PADDING, ChooseDialog.INSETS_PADDING,
                ChooseDialog.INSETS_PADDING);
        //c.fill = GridBagConstraints.VERTICAL;

        //c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.weightx = 1.0;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        gridBagLayout.setConstraints(namePane, c);
        this.getContentPane().add(namePane);

        c.weightx = 1.0;
        c.gridx = 2;
        c.gridy = 0;
        c.gridwidth = 1;
        gridBagLayout.setConstraints(startPane, c);
        this.getContentPane().add(startPane);

        c.weightx = 1.0;
        c.gridx = 3;
        c.gridy = 0;
        c.gridwidth = 1;
        gridBagLayout.setConstraints(endPane, c);
        this.getContentPane().add(endPane);

        c.weightx = 1.0;
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        gridBagLayout.setConstraints(purposePane, c);
        this.getContentPane().add(purposePane);

        c.weightx = 1.0;
        c.gridx = 1;
        c.gridy = 1;
        c.gridwidth = 1;
        gridBagLayout.setConstraints(accessorPane, c);
        this.getContentPane().add(accessorPane);

        c.weightx = 1.0;
        c.gridx = 2;
        c.gridy = 1;
        c.gridwidth = 1;
        gridBagLayout.setConstraints(recipientPane, c);
        this.getContentPane().add(recipientPane);

        c.weightx = 1.0;
        c.gridx = 3;
        c.gridy = 1;
        c.gridwidth = 1;
        gridBagLayout.setConstraints(isolationPane, c);
        this.getContentPane().add(isolationPane);

        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.weightx = 1.0;
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 4;
        gridBagLayout.setConstraints(showSQLCheckBox, c);
        this.getContentPane().add(showSQLCheckBox);

        if (showSQLCheckBox.isSelected()) {
            c.weightx = 1.0;
            c.gridx = 0;
            c.gridy = 3;
            c.gridwidth = 4;
            gridBagLayout.setConstraints(sqlPane, c);
            this.getContentPane().add(sqlPane);
        }

        c.anchor = GridBagConstraints.CENTER;
        c.weightx = 1.0;
        c.gridx = 0;
        if (showSQLCheckBox.isSelected())
            c.gridy = 4;
        else
            c.gridy = 2;
        c.gridwidth = 4;
        gridBagLayout.setConstraints(buttonGroupBox, c);
        this.getContentPane().add(buttonGroupBox);

        pack();
        setResizable(false);
        //setLocation(getParent().getLocation());
    }

    public void thisWindowClosed(WindowEvent e) {
    };

    class MyWindowAdapter extends WindowAdapter {
        private QueryDetailsDialog adaptee;

        MyWindowAdapter(QueryDetailsDialog adaptee) {
            this.adaptee = adaptee;
        }

        public void windowClosed(WindowEvent e) {
            adaptee.thisWindowClosed(e);
        }
    }

    class ActionAdapter implements ActionListener {
        final QueryDetailsDialog dialog;

        public ActionAdapter(QueryDetailsDialog dialog) {
            this.dialog = dialog;
        }

        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == okButton) {
                hide();
            }
            else if (e.getSource() == showSQLCheckBox) {
                dialog.showPanel();
            }
        }
    }
}
