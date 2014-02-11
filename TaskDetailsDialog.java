package controlCenter;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import java.util.Vector;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;

import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

public class TaskDetailsDialog extends JDialog {
	// private Task task;

	private final JScrollPane auditPane;

	private final JScrollPane taskPane;

	private final JScrollPane purposesPane;

	private final JScrollPane accessorsPane;

	private final JScrollPane recipientsPane;

	private final JScrollPane columnsPane;

	private final JScrollPane conditionPane;

	private final JScrollPane startPane;

	private final JScrollPane endPane;

	private final JScrollPane sqlPane;

	private final JCheckBox showSQLCheckBox;

	private final JButton okButton;

	private final Box buttonGroupBox;

	public TaskDetailsDialog(Task task, Frame parent) {
		super(parent, true);
		// this.task = task;

		this.setTitle("Audit query details");

		// this.addWindowListener(new MyWindowAdapter(this));
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		final ActionAdapter actionAdapter = new ActionAdapter(this);

		// final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		// setLocation(screen.width / 5, screen.height / 5);

		final JTextArea auditTextArea = new JTextArea();
		auditTextArea.setFont(ResourceManager.NORMAL_FONT);
		auditTextArea.setBackground(ResourceManager.BACKGROUND_COLOR);
		auditTextArea.setEditable(false);
		auditTextArea.setText(task.policyName);
		auditPane = new JScrollPane(auditTextArea);
		auditPane.setPreferredSize(new Dimension(
				ResourceManager.SCROLL_PANEL_DIMENSION_X,
				ResourceManager.SINGLE_ROW_SCROLL_PANEL_DIMENSION_X));
		final TitledBorder auditTitledBorder = new TitledBorder(
				ResourceManager.BORDER, "Audit");
		auditTitledBorder.setTitleFont(ResourceManager.BOLD_FONT);
		auditPane.setBorder(auditTitledBorder);

		final JTextArea taskTextArea = new JTextArea();
		taskTextArea.setFont(ResourceManager.NORMAL_FONT);
		taskTextArea.setBackground(ResourceManager.BACKGROUND_COLOR);
		taskTextArea.setEditable(false);
		taskTextArea.setText(task.name);
		taskPane = new JScrollPane(taskTextArea);
		taskPane.setPreferredSize(new Dimension(
				ResourceManager.SCROLL_PANEL_DIMENSION_X,
				ResourceManager.SINGLE_ROW_SCROLL_PANEL_DIMENSION_X));
		final TitledBorder taskTitledBorder = new TitledBorder(
				ResourceManager.BORDER, "Query");
		taskTitledBorder.setTitleFont(ResourceManager.BOLD_FONT);
		taskPane.setBorder(taskTitledBorder);

		final JTextArea startTextArea = new JTextArea();
		startTextArea.setFont(ResourceManager.NORMAL_FONT);
		startTextArea.setEditable(false);
		startTextArea.setBackground(ResourceManager.BACKGROUND_COLOR);
		startTextArea.setText(task.begin.toString());
		startPane = new JScrollPane(startTextArea);
		startPane.setPreferredSize(new Dimension(
				ResourceManager.SCROLL_PANEL_DIMENSION_X,
				ResourceManager.SINGLE_ROW_SCROLL_PANEL_DIMENSION_X));
		final TitledBorder startTitledBorder = new TitledBorder(
				ResourceManager.BORDER, "Start time");
		startTitledBorder.setTitleFont(ResourceManager.BOLD_FONT);
		startPane.setBorder(startTitledBorder);

		final JTextArea endTextArea = new JTextArea();
		endTextArea.setFont(ResourceManager.NORMAL_FONT);
		endTextArea.setEditable(false);
		endTextArea.setBackground(ResourceManager.BACKGROUND_COLOR);
		endTextArea.setText(task.end.toString());
		endPane = new JScrollPane(endTextArea);
		endPane.setPreferredSize(new Dimension(
				ResourceManager.SCROLL_PANEL_DIMENSION_X,
				ResourceManager.SINGLE_ROW_SCROLL_PANEL_DIMENSION_X));
		final TitledBorder endTitledBorder = new TitledBorder(
				ResourceManager.BORDER, "End time");
		endTitledBorder.setTitleFont(ResourceManager.BOLD_FONT);
		endPane.setBorder(endTitledBorder);

		final JList purposesList = new JList();
		purposesList.setFont(ResourceManager.NORMAL_FONT);
		purposesList.setBackground(ResourceManager.BACKGROUND_COLOR);
		purposesList.setListData(task.purposes);
		purposesPane = new JScrollPane(purposesList);
		purposesPane.setPreferredSize(new Dimension(
				ResourceManager.SCROLL_PANEL_DIMENSION_X,
				ResourceManager.SCROLL_PANEL_DIMENSION_Y));
		final TitledBorder purposesTitledBorder = new TitledBorder(
				ResourceManager.BORDER, "Purposes");
		purposesTitledBorder.setTitleFont(ResourceManager.BOLD_FONT);
		purposesPane.setBorder(purposesTitledBorder);

		final JList accessorsList = new JList();
		accessorsList.setFont(ResourceManager.NORMAL_FONT);
		accessorsList.setBackground(ResourceManager.BACKGROUND_COLOR);
		accessorsList.setListData(task.accessors);
		accessorsPane = new JScrollPane(accessorsList);
		accessorsPane.setPreferredSize(new Dimension(
				ResourceManager.SCROLL_PANEL_DIMENSION_X,
				ResourceManager.SCROLL_PANEL_DIMENSION_Y));
		final TitledBorder accessorsTitledBorder = new TitledBorder(
				ResourceManager.BORDER, "Accessors");
		accessorsTitledBorder.setTitleFont(ResourceManager.BOLD_FONT);
		accessorsPane.setBorder(accessorsTitledBorder);

		// final JLabel recipientsLabel = new JLabel("Recipients:");
		final JList recipientsList = new JList();
		recipientsList.setFont(ResourceManager.NORMAL_FONT);
		recipientsList.setBackground(ResourceManager.BACKGROUND_COLOR);
		recipientsList.setListData(task.recipients);
		recipientsPane = new JScrollPane(recipientsList);
		recipientsPane.setPreferredSize(new Dimension(
				ResourceManager.SCROLL_PANEL_DIMENSION_X,
				ResourceManager.SCROLL_PANEL_DIMENSION_Y));
		final TitledBorder recipientsTitledBorder = new TitledBorder(
				ResourceManager.BORDER, "Recipients");
		recipientsTitledBorder.setTitleFont(ResourceManager.BOLD_FONT);
		recipientsPane.setBorder(recipientsTitledBorder);

		final JList columnsList = new JList();
		columnsList.setFont(ResourceManager.NORMAL_FONT);
		columnsList.setBackground(ResourceManager.BACKGROUND_COLOR);
		final Vector columns = task.columns;
		final Vector verboseColumns = new Vector(4, 2);

		for (int i = 0; i < columns.size(); i++) {
			final ColumnDescriptor column = (ColumnDescriptor) columns.get(i);
			try {
				final ColumnDescriptor verboseColumn = (ColumnDescriptor) column
						.clone(true);
				verboseColumns.add(verboseColumn);
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
		}

		columnsList.setListData(verboseColumns);
		columnsPane = new JScrollPane(columnsList);
		columnsPane.setPreferredSize(new Dimension(
				ResourceManager.SCROLL_PANEL_DIMENSION_X,
				ResourceManager.SCROLL_PANEL_DIMENSION_Y));
		final TitledBorder columnsTitledBorder = new TitledBorder(
				ResourceManager.BORDER, "Columns");
		columnsTitledBorder.setTitleFont(ResourceManager.BOLD_FONT);
		columnsPane.setBorder(columnsTitledBorder);

		final JTextArea conditionTextArea = new JTextArea();
		conditionTextArea.setFont(ResourceManager.NORMAL_FONT);
		conditionTextArea.setEditable(false);
		conditionTextArea.setBackground(ResourceManager.BACKGROUND_COLOR);
		conditionTextArea.setText(task.condition);
		conditionPane = new JScrollPane(conditionTextArea);
		conditionPane.setPreferredSize(new Dimension(4
				* ResourceManager.SCROLL_PANEL_DIMENSION_X + 6
				* ResourceManager.INSETS_PADDING,
				ResourceManager.SCROLL_PANEL_DIMENSION_Y / 2));
		final TitledBorder conditionTitledBorder = new TitledBorder(
				ResourceManager.BORDER, "Condition");
		conditionTitledBorder.setTitleFont(ResourceManager.BOLD_FONT);
		conditionPane.setBorder(conditionTitledBorder);

		//
		// SQL text of audit query.
		//
		final JTextArea sqlTextArea = new JTextArea();
		sqlTextArea.setFont(ResourceManager.COURIER_FONT);
		sqlTextArea.setBackground(ResourceManager.BACKGROUND_COLOR);
		sqlTextArea.setEditable(false);
		sqlTextArea.setText(task.getSQLAuditQuery());
		// ResourceManager.printSQL("AUDIT QUERY:\n" + task.getSQLAuditQuery());
		sqlPane = new JScrollPane(sqlTextArea);
		sqlPane.setPreferredSize(new Dimension(4
				* ResourceManager.SCROLL_PANEL_DIMENSION_X + 6
				* ResourceManager.INSETS_PADDING,
				ResourceManager.SCROLL_PANEL_DIMENSION_Y));
		final TitledBorder sqlTitledBorder = new TitledBorder(
				ResourceManager.BORDER, "SQL query");
		sqlTitledBorder.setTitleFont(ResourceManager.BOLD_FONT);
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
		setLocationRelativeTo(null);

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
		gridBagLayout.setConstraints(auditPane, c);
		this.getContentPane().add(auditPane);

		c.weightx = 1.0;
		c.gridx = 1;
		c.gridy = 0;
		c.gridwidth = 1;
		gridBagLayout.setConstraints(taskPane, c);
		this.getContentPane().add(taskPane);

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
		gridBagLayout.setConstraints(purposesPane, c);
		this.getContentPane().add(purposesPane);

		c.weightx = 1.0;
		c.gridx = 1;
		c.gridy = 1;
		c.gridwidth = 1;
		gridBagLayout.setConstraints(accessorsPane, c);
		this.getContentPane().add(accessorsPane);

		c.weightx = 1.0;
		c.gridx = 2;
		c.gridy = 1;
		c.gridwidth = 1;
		gridBagLayout.setConstraints(recipientsPane, c);
		this.getContentPane().add(recipientsPane);

		c.weightx = 1.0;
		c.gridx = 3;
		c.gridy = 1;
		c.gridwidth = 1;
		gridBagLayout.setConstraints(columnsPane, c);
		this.getContentPane().add(columnsPane);

		c.weightx = 1.0;
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 4;
		gridBagLayout.setConstraints(conditionPane, c);
		this.getContentPane().add(conditionPane);

		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.weightx = 1.0;
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 1;
		gridBagLayout.setConstraints(showSQLCheckBox, c);
		this.getContentPane().add(showSQLCheckBox);

		if (showSQLCheckBox.isSelected()) {
			c.weightx = 1.0;
			c.gridx = 0;
			c.gridy = 4;
			c.gridwidth = 4;
			gridBagLayout.setConstraints(sqlPane, c);
			this.getContentPane().add(sqlPane);
		}

		c.anchor = GridBagConstraints.CENTER;
		c.weightx = 1.0;
		c.gridx = 0;
		if (showSQLCheckBox.isSelected())
			c.gridy = 6;
		else
			c.gridy = 4;
		c.gridwidth = 4;
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
	// private TaskDetailsDialog adaptee;
	//
	// MyWindowAdapter(TaskDetailsDialog adaptee) {
	// this.adaptee = adaptee;
	// }
	//
	// public void windowClosed(WindowEvent e) {
	// adaptee.thisWindowClosed(e);
	// }
	// }

	class ActionAdapter implements ActionListener {
		final TaskDetailsDialog dialog;

		public ActionAdapter(TaskDetailsDialog dialog) {
			this.dialog = dialog;
		}

		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == okButton) {
				hide();
			} else if (e.getSource() == showSQLCheckBox) {
				dialog.showPanel();
			}
		}
	}
}
