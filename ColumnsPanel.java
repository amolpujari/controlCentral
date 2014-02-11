package controlCenter;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EtchedBorder;

import org.apache.log4j.Logger;

class ColumnsPanel extends NewWizardPanel implements SelectionListener,
		MouseListener {
	// public final static int TYPE_NEW_POLICY_COLUMNS_IN_SCOPE = 0;
	public final static int TYPE_NEW_POLICY_RULE_COLUMNS = 1;

	public final static int TYPE_AUDIT = -2;

	public final static int TYPE_NONE = 2;

	private final SelectionPanel columnsSP;

	private Hashtable tableInfo;

	private final int[] checks;

	private final JList tableList;

	private final JComboBox schemasBox;

	private final Logger logger = Logger.getLogger(getClass().getName());

	private final int type;

	private Vector coloredData = new Vector();

	private JLabel defineBacklogButton;

	private final ColumnsPanel me;

	public ColumnsPanel(final NewWizard wiz, final int type,
			final int[] checks, final Vector initialSelectedColumns,
			final Vector schemas, final Hashtable tablesInfo,
			final ResourceManager resourceManager, final String databaseName) {
		super();
		me = this;
		logger.debug("started initiating columns panel");
		this.wiz = wiz;
		this.type = type;
		this.checks = checks;
		this.tableInfo = tablesInfo;
		final JPanel tablePanel = new JPanel();
		schemasBox = new JComboBox();
		schemasBox.setBounds(5, 18, 149, 20);
		tableList = new JList();
		tableList.setName("table_list");
		tableList.setBorder(BorderFactory.createLoweredBevelBorder());
		tablePanel.setLayout(null);
		final JScrollPane tableScrollPane = new JScrollPane(tableList,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		tableScrollPane.setBounds(4, 40, 150, 178);
		tableList.setBounds(0, 0, 150, 178);
		tablePanel.add(schemasBox);
		tablePanel.add(tableScrollPane);
		tablePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(EtchedBorder.LOWERED), " "+ResourceManager.getResource("cp.schema.tables")+" "));

		columnsSP = new SelectionPanel(checks, this, " "+ResourceManager.getResource("cp.unselected.column")+" ",
				" "+ResourceManager.getResource("cp.selected.combinations")+" ", new Vector(4, 2),
				initialSelectedColumns, true, false);
		tablePanel.setBounds(5, 5, 159, 223);
		columnsSP.setBounds(161, 0, 400, 227);
		columnsSP.setBorder(null);
		add(tablePanel);
		add(columnsSP);

		for (int i = 0; i < schemas.size(); i++)
			schemasBox.addItem(schemas.get(i));

		if (schemasBox.getItemCount() > 0)
			schemasBox.setSelectedIndex(0);

		schemasBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateTableList();
			}
		});

		updateTableList();

		tableList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tableList.addMouseListener(this);

		if (type == TYPE_AUDIT) {
			defineBacklogButton = new JLabel(ResourceManager.getResource("cp.define.backlogs"));
			defineBacklogButton.setForeground(Color.BLUE);
			defineBacklogButton.setBounds(430, 240, 110, 20);
			defineBacklogButton.setCursor(Cursor
					.getPredefinedCursor(Cursor.HAND_CURSOR));
			defineBacklogButton.addMouseListener(new MouseListener() {
				public void mouseClicked(MouseEvent arg0) {
					resourceManager.setColumnsPanel(me);
					new NewEntryWizard(ChoosePanel.BACKLOG,
							ResourceManager.getResource("cp.select.tables.for.backlog"), databaseName,
							resourceManager, null);
				}

				public void mouseEntered(MouseEvent arg0) {
				}

				public void mouseExited(MouseEvent arg0) {
				}

				public void mousePressed(MouseEvent arg0) {
				}

				public void mouseReleased(MouseEvent arg0) {
				}
			});

			add(defineBacklogButton);
		}

		refresh();

	}

	public void backlogChanged(Hashtable info) {
		tableInfo = info;
		columnsSP.setAvailableData(new Vector());
		columnsSP.clearSelection();
		updateTableList();
	}

	protected void updateTableList() {
		Enumeration e = this.tableInfo.keys();
		final String schemaSelected = (String) schemasBox.getSelectedItem();
		String fullTableName;
		final Vector tables = new Vector(4, 2);

		while (e.hasMoreElements()) {
			fullTableName = (String) e.nextElement();

			if (fullTableName.toString().startsWith(schemaSelected + "."))
				tables.add(fullTableName);
		}

		tableList.removeAll();
		tableList.setListData(tables);

	}

	public Vector getSelectedColumns() {
		return columnsSP.getSelectedData();
	}

	public int[] getChecks() {
		return columnsSP.getChecks();
	}

	public void selectionChanged(Vector selection) {
		if (selection.isEmpty()) {
			finished = false;
			wiz.setFinishEnabled(false);
			wiz.setMessage(ResourceManager.getResource("cp.select.columns"), false);
		} else {
			finished = true;
			wiz.panelRefreshed(-1);
			wiz.setMessage((checks == null) ? ""
					: ResourceManager.getResource("cp.pseudonym.only"), true);
		}
	}

	/**
	 * Get called when this panel get activated
	 */
	public void refresh() {
		try {
			if (type == TYPE_NEW_POLICY_RULE_COLUMNS) {
				tableInfo = ((PolicyRuleWizard) wiz).getUpdatedList();
				updateTableList();
				validateSelectionPanel();
			}
		} catch (NullPointerException e) {
			/*
			 * this is the case when initially not all the panels are
			 * constructed but those which being construct they would call this
			 * function and it would throw null pointer exception in case other
			 * panels ane not constrcuted yet
			 */

		}

		selectionChanged(columnsSP.getSelectedData());
	}

	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void mouseReleased(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			if (tableList.getSelectedValue() == null)
				return;

			final Vector columns = new Vector(4, 2);
			final Vector orgnColumns = (Vector) tableInfo.get(tableList
					.getSelectedValue());

			for (int i = 0; i < orgnColumns.size(); i++) {
				try {
					columns.add(((ColumnDescriptor) orgnColumns.get(i))
							.clone(false));
				} catch (CloneNotSupportedException exp) {
					exp.printStackTrace();
				} catch (Exception exp) {
					exp.printStackTrace();
				}
			}

			columnsSP.setAvailableData(columns);
		}
	}

	private void validateSelectionPanel() {
		columnsSP.setSelection(((PolicyRuleWizard) wiz)
				.filterColumnsInScope(columnsSP.getSelectedData()));

		columnsSP.setAvailableData(((PolicyRuleWizard) wiz)
				.filterColumnsInScope(columnsSP.getUnselectedData()));
	}

	public void setColoredData(Vector coloredData) {
		this.coloredData = coloredData;
	}

}
