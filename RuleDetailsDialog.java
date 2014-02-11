package controlCenter;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

import controlCenter.ResourceManager;

public class RuleDetailsDialog extends JDialog {
	// private Rule rule;

	private final JScrollPane policyPane;

	private final JScrollPane versionPane;

	private final JScrollPane rulePane;

	private final JScrollPane purposePane;

	private final JScrollPane accessorPane;

	private final JScrollPane recipientPane;

	private final JScrollPane schemaPane;

	private final JScrollPane tablePane;

	private final JScrollPane columnPane;

	private final JScrollPane conditionPane;

	private final JButton okButton;

	private final Box buttonGroupBox;

	public RuleDetailsDialog(Rule rule, Frame parent) {
		super(parent, true);
		// this.rule = rule;

		this.setTitle("Rule details");

		// this.addWindowListener(new MyWindowAdapter(this));
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		final ActionAdapter actionAdapter = new ActionAdapter(this);

		// final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		// setLocation(screen.width / 5, screen.height / 5);

		final Color backgroundColor = (new JPanel()).getBackground();

		final JTextArea policyTextArea = new JTextArea();
		policyTextArea.setFont(ResourceManager.NORMAL_FONT);
		policyTextArea.setBackground(backgroundColor);
		policyTextArea.setEditable(false);
		policyTextArea.setText(rule.policyName);
		policyPane = new JScrollPane(policyTextArea);
		policyPane.setPreferredSize(new Dimension(
				ResourceManager.SCROLL_PANEL_DIMENSION_X,
				ResourceManager.SINGLE_ROW_SCROLL_PANEL_DIMENSION_X));
		final TitledBorder policyTitledBorder = new TitledBorder(
				ResourceManager.BORDER, "Policy");
		policyTitledBorder.setTitleFont(ResourceManager.BOLD_FONT);
		policyPane.setBorder(policyTitledBorder);

		final JTextArea versionTextArea = new JTextArea();
		versionTextArea.setFont(ResourceManager.NORMAL_FONT);
		versionTextArea.setBackground(backgroundColor);
		versionTextArea.setEditable(false);
		versionTextArea.setText(rule.version);
		versionPane = new JScrollPane(versionTextArea);
		versionPane.setPreferredSize(new Dimension(
				ResourceManager.SCROLL_PANEL_DIMENSION_X,
				ResourceManager.SINGLE_ROW_SCROLL_PANEL_DIMENSION_X));
		final TitledBorder versionTitledBorder = new TitledBorder(
				ResourceManager.BORDER, "Version");
		versionTitledBorder.setTitleFont(ResourceManager.BOLD_FONT);
		versionPane.setBorder(versionTitledBorder);

		final JTextArea ruleTextArea = new JTextArea();
		ruleTextArea.setFont(ResourceManager.NORMAL_FONT);
		ruleTextArea.setBackground(backgroundColor);
		ruleTextArea.setEditable(false);
		ruleTextArea.setText(rule.name);
		rulePane = new JScrollPane(ruleTextArea);
		rulePane.setPreferredSize(new Dimension(
				ResourceManager.SCROLL_PANEL_DIMENSION_X,
				ResourceManager.SINGLE_ROW_SCROLL_PANEL_DIMENSION_X));
		final TitledBorder ruleTitledBorder = new TitledBorder(
				ResourceManager.BORDER, "Rule");
		ruleTitledBorder.setTitleFont(ResourceManager.BOLD_FONT);
		rulePane.setBorder(ruleTitledBorder);

		final JTextArea purposeTextArea = new JTextArea();
		purposeTextArea.setFont(ResourceManager.NORMAL_FONT);
		purposeTextArea.setBackground(backgroundColor);
		purposeTextArea.setText((String) rule.purposes.get(0));
		purposeTextArea.setEditable(false);
		purposePane = new JScrollPane(purposeTextArea);
		purposePane.setPreferredSize(new Dimension(
				ResourceManager.SCROLL_PANEL_DIMENSION_X,
				ResourceManager.SINGLE_ROW_SCROLL_PANEL_DIMENSION_X));
		final TitledBorder purposeTitledBorder = new TitledBorder(
				ResourceManager.BORDER, "Purpose");
		purposeTitledBorder.setTitleFont(ResourceManager.BOLD_FONT);
		purposePane.setBorder(purposeTitledBorder);

		final JTextArea accessorTextArea = new JTextArea();
		accessorTextArea.setFont(ResourceManager.NORMAL_FONT);
		accessorTextArea.setBackground(backgroundColor);
		accessorTextArea.setText((String) rule.accessors.get(0));
		accessorTextArea.setEditable(false);
		accessorPane = new JScrollPane(accessorTextArea);
		accessorPane.setPreferredSize(new Dimension(
				ResourceManager.SCROLL_PANEL_DIMENSION_X,
				ResourceManager.SINGLE_ROW_SCROLL_PANEL_DIMENSION_X));
		final TitledBorder accessorTitledBorder = new TitledBorder(
				ResourceManager.BORDER, "Accessor");
		accessorTitledBorder.setTitleFont(ResourceManager.BOLD_FONT);
		accessorPane.setBorder(accessorTitledBorder);

		final JTextArea recipientTextArea = new JTextArea();
		recipientTextArea.setFont(ResourceManager.NORMAL_FONT);
		recipientTextArea.setBackground(backgroundColor);
		recipientTextArea.setText((String) rule.recipients.get(0));
		recipientTextArea.setEditable(false);
		recipientPane = new JScrollPane(recipientTextArea);
		recipientPane.setPreferredSize(new Dimension(
				ResourceManager.SCROLL_PANEL_DIMENSION_X,
				ResourceManager.SINGLE_ROW_SCROLL_PANEL_DIMENSION_X));
		final TitledBorder recipientTitledBorder = new TitledBorder(
				ResourceManager.BORDER, "Recipient");
		recipientTitledBorder.setTitleFont(ResourceManager.BOLD_FONT);
		recipientPane.setBorder(recipientTitledBorder);

		final ColumnDescriptor column = (ColumnDescriptor) rule.columns.get(0);

		final JTextArea schemaTextArea = new JTextArea();
		schemaTextArea.setFont(ResourceManager.NORMAL_FONT);
		schemaTextArea.setEditable(false);
		schemaTextArea.setBackground(backgroundColor);
		schemaTextArea.setText(column.table.schemaName);
		schemaPane = new JScrollPane(schemaTextArea);
		schemaPane.setPreferredSize(new Dimension(
				ResourceManager.SCROLL_PANEL_DIMENSION_X,
				ResourceManager.SINGLE_ROW_SCROLL_PANEL_DIMENSION_X));
		final TitledBorder schemaTitledBorder = new TitledBorder(
				ResourceManager.BORDER, "Schema");
		schemaTitledBorder.setTitleFont(ResourceManager.BOLD_FONT);
		schemaPane.setBorder(schemaTitledBorder);

		final JTextArea tableTextArea = new JTextArea();
		tableTextArea.setFont(ResourceManager.NORMAL_FONT);
		tableTextArea.setEditable(false);
		tableTextArea.setBackground(backgroundColor);
		tableTextArea.setText(column.table.tableName);
		tablePane = new JScrollPane(tableTextArea);
		tablePane.setPreferredSize(new Dimension(
				ResourceManager.SCROLL_PANEL_DIMENSION_X,
				ResourceManager.SINGLE_ROW_SCROLL_PANEL_DIMENSION_X));
		final TitledBorder tableTitledBorder = new TitledBorder(
				ResourceManager.BORDER, "Table");
		tableTitledBorder.setTitleFont(ResourceManager.BOLD_FONT);
		tablePane.setBorder(tableTitledBorder);

		final JTextArea columnTextArea = new JTextArea();
		columnTextArea.setFont(ResourceManager.NORMAL_FONT);
		columnTextArea.setEditable(false);
		columnTextArea.setBackground(backgroundColor);
		String pseudonymAccessOnly = (column.pseudonym) ? " / pseudonym access only"
				: " / plaintext access";
		columnTextArea.setText(column.columnName + pseudonymAccessOnly);
		columnPane = new JScrollPane(columnTextArea);
		columnPane.setPreferredSize(new Dimension(
				ResourceManager.SCROLL_PANEL_DIMENSION_X,
				ResourceManager.SINGLE_ROW_SCROLL_PANEL_DIMENSION_X));
		final TitledBorder columnTitledBorder = new TitledBorder(
				ResourceManager.BORDER, "Column");
		columnTitledBorder.setTitleFont(ResourceManager.BOLD_FONT);
		columnPane.setBorder(columnTitledBorder);

		final JTextArea conditionTextArea = new JTextArea();
		conditionTextArea.setFont(ResourceManager.NORMAL_FONT);
		conditionTextArea.setEditable(false);
		conditionTextArea.setBackground(backgroundColor);
		conditionTextArea.setText(rule.condition);
		conditionPane = new JScrollPane(conditionTextArea);
		conditionPane.setPreferredSize(new Dimension(3
				* ResourceManager.SCROLL_PANEL_DIMENSION_X + 4
				* ResourceManager.INSETS_PADDING,
				ResourceManager.SCROLL_PANEL_DIMENSION_Y));
		final TitledBorder conditionTitledBorder = new TitledBorder(
				ResourceManager.BORDER, "Condition");
		conditionTitledBorder.setTitleFont(ResourceManager.BOLD_FONT);
		conditionPane.setBorder(conditionTitledBorder);

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
		c.insets = new Insets(ResourceManager.INSETS_PADDING,
				ResourceManager.INSETS_PADDING, ResourceManager.INSETS_PADDING,
				ResourceManager.INSETS_PADDING);
		// c.fill = GridBagConstraints.VERTICAL;

		// c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.weightx = 1.0;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		gridBagLayout.setConstraints(policyPane, c);
		this.getContentPane().add(policyPane);

		c.weightx = 1.0;
		c.gridx = 1;
		c.gridy = 0;
		c.gridwidth = 1;
		gridBagLayout.setConstraints(versionPane, c);
		this.getContentPane().add(versionPane);

		c.weightx = 1.0;
		c.gridx = 2;
		c.gridy = 0;
		c.gridwidth = 1;
		gridBagLayout.setConstraints(rulePane, c);
		this.getContentPane().add(rulePane);

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
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 1;
		gridBagLayout.setConstraints(schemaPane, c);
		this.getContentPane().add(schemaPane);

		c.weightx = 1.0;
		c.gridx = 1;
		c.gridy = 2;
		c.gridwidth = 1;
		gridBagLayout.setConstraints(tablePane, c);
		this.getContentPane().add(tablePane);

		c.weightx = 1.0;
		c.gridx = 2;
		c.gridy = 2;
		c.gridwidth = 1;
		gridBagLayout.setConstraints(columnPane, c);
		this.getContentPane().add(columnPane);

		c.weightx = 1.0;
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 3;
		gridBagLayout.setConstraints(conditionPane, c);
		this.getContentPane().add(conditionPane);

		c.anchor = GridBagConstraints.CENTER;
		c.weightx = 1.0;
		c.gridx = 0;
		c.gridy = 4;
		c.gridwidth = 3;
		gridBagLayout.setConstraints(buttonGroupBox, c);
		this.getContentPane().add(buttonGroupBox);

		pack();
		setResizable(false);
		// setLocation(getParent().getLocation());
		setLocationRelativeTo(null);
	}

	// public void thisWindowClosed(WindowEvent e) {
	// };
	//
	// class MyWindowAdapter extends WindowAdapter {
	// private RuleDetailsDialog adaptee;
	//
	// MyWindowAdapter(RuleDetailsDialog adaptee) {
	// this.adaptee = adaptee;
	// }
	//
	// public void windowClosed(WindowEvent e) {
	// adaptee.thisWindowClosed(e);
	// }
	// }

	class ActionAdapter implements ActionListener {
		final RuleDetailsDialog dialog;

		public ActionAdapter(RuleDetailsDialog dialog) {
			this.dialog = dialog;
		}

		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == okButton) {
				hide();
			}
		}
	}
}
