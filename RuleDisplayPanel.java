package controlCenter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;

public class RuleDisplayPanel extends JPanel implements ListSelectionListener,
		ResourceListener {
	private JTable ruleTable;

	private final ControlCenter controlCenter;

	private RuleTableModel ruleTableModel;

	private PopupMenus popupMenus;

	public RuleDisplayPanel(ControlCenter controlCenter) {
		this.controlCenter = controlCenter;
		popupMenus = new PopupMenus(new MyListener());
		controlCenter.resourceManager.addResourceListener(this);

		ruleTableModel = new RuleTableModel();
		ruleTable = new AppJTable(ruleTableModel);
		ruleTable.setRowHeight(20);
		ruleTable.setShowVerticalLines(false);
		ruleTable.setGridColor(Color.WHITE);
		ruleTable.setBackground(ResourceManager.TABLE_BACKGROUND_COLOR);

		final MyListener listener = new MyListener();
		ruleTable.addMouseListener(listener);
		ruleTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		ruleTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		ruleTable.getSelectionModel().addListSelectionListener(this);

		JScrollPane scrollPane = new JScrollPane(ruleTable);

		setLayout(new BorderLayout());
		add(scrollPane, BorderLayout.CENTER);
		ruleTable.getInputMap().put(KeyStroke.getKeyStroke("released DELETE"),
				"DELETE");
		ruleTable.getActionMap().put("DELETE", new AbstractAction("DELETE") {
			public void actionPerformed(ActionEvent e) {
				performDelete();
			}
		});
		ruleTable.getInputMap().put(KeyStroke.getKeyStroke("alt E"), "EDIT");
		ruleTable.getActionMap().put("EDIT", new AbstractAction("EDIT") {
			public void actionPerformed(ActionEvent e) {
				performEdit();
			}
		});
	}

	private void performEdit() {
		MessageBox.showBusy(controlCenter, "Starting policy rule wizard");
		new Thread(new Runnable() {
			public void run() {
				try {
					Rule rule = ruleTableModel.getRule(selectedRow);
					rule = controlCenter.resourceManager.getPolicyVersionType(
							version.databaseName, rule);
					new PolicyRuleWizard(false, rule, (Rule) rule.clone(),
							controlCenter.resourceManager, version.databaseName);
				} catch (CloneNotSupportedException ex) {
					ex.printStackTrace();
				}
			}
		}).start();
	}

	private void performDelete() {

		Rule rule = ruleTableModel.getRule(selectedRow);

		if (MessageBox.result(controlCenter, ResourceManager
				.getResource("label.confirm"), ResourceManager
				.getResource("rdp.sure.del.rule"), MessageBox.ICON_CON) == MessageBox.BUTTON_YES)
			if (!controlCenter.resourceManager.deleteRule(version, rule))
				MessageBox.show(controlCenter, ResourceManager
						.getResource("label.error"), ResourceManager
						.getResource("rdp.could.not.del.rule"),
						MessageBox.ICON_ERR);

	}

	private Version version;

	// this is a hack, should really define a better policy object rather than
	// vector
	public void showPanel(Vector versionInfo) {
		final String versionName = (String) versionInfo.elementAt(0);
		final String policyName = (String) versionInfo.elementAt(1);
		final String databaseName = (String) versionInfo.elementAt(2);

		version = new Version(databaseName, policyName, versionName);
		Vector rules = controlCenter.resourceManager.getPolicyRules(version);
		ruleTableModel.setRules(rules);

		controlCenter.policyEditor.infoPanel.setRule(ruleTableModel
				.getRule(selectedRow));
		controlCenter.policyEditor.setDivider();
	}

	static final int NONE = -1;

	private int selectedRow = NONE;

	public void valueChanged(ListSelectionEvent e) {
		if (e.getSource() == ruleTable.getSelectionModel()) {
			ListSelectionModel listSelectionModel = (ListSelectionModel) e
					.getSource();

			boolean hasSelection = !listSelectionModel.isSelectionEmpty();
			popupMenus.editMenuItem.setEnabled(hasSelection);
			popupMenus.deleteMenuItem.setEnabled(hasSelection);

			if (hasSelection) {
				selectedRow = listSelectionModel.getMinSelectionIndex();
				controlCenter.policyEditor.infoPanel.setRule(ruleTableModel
						.getRule(selectedRow));
				controlCenter.policyEditor.setDivider();
			} else
				selectedRow = NONE;
		}
	}

	class MyListener extends MouseAdapter implements ActionListener,
			TableModelListener {
		private PopupMenus popupMenus;

		public MyListener() {
			popupMenus = new PopupMenus(this);
		}

		public void tableChanged(TableModelEvent e) {
		}

		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == popupMenus.editMenuItem) {
				performEdit();
			} else if (e.getSource() == popupMenus.deleteMenuItem) {
				performDelete();
			}
		}

		public void mouseReleased(MouseEvent e) {
		}

		public void mousePressed(MouseEvent e) {
			if (e.getButton() != MouseEvent.BUTTON1) {
				ruleTable.clearSelection();
				final int i = ruleTable.rowAtPoint(e.getPoint());
				ruleTable.addRowSelectionInterval(i, i);
				popupMenus.popupMenu.show(e.getComponent(), e.getX(), e.getY());
			}

		}
	}

	public void auditChanged(Version version) {
		// Do nothing. Not needed.
	}

	public void policyChanged(Version version) {
		if (version.collectionName != null) {
			Vector rules = controlCenter.resourceManager
					.getPolicyRules(version);
			ruleTableModel.setRules(rules);
			ruleTable.setModel(ruleTableModel);
			ruleTableModel.fireTableDataChanged();
		}
	}

	public void databaseConnected(String dbname) {
	} // ignored

	public void databaseDisconnected(String dbname) {
	} // ignored

	class RuleTableModel extends AbstractTableModel {
		private Vector rules;

		private final Logger logger = Logger.getLogger(getClass().getName());

		public RuleTableModel() {
			super();
			rules = new Vector(4, 2);
		}

		public void setRules(Vector rules) {
			this.rules = rules;
		}

		public int getRowCount() {
			return rules.size();
		}

		public int getColumnCount() {
			return 9;
		}

		static final int RULEID = 0;

		static final int PURPOSE = 1;

		static final int ACCESSOR = 2;

		static final int RECIPIENT = 3;

		static final int SCHEMA = 4;

		static final int TABLE = 5;

		static final int COLUMN = 6;

		static final int PSEUDONYM = 7;

		static final int CONDITION = 8;

		public Rule getRule(int row) {
			Rule rule;

			try {
				rule = (Rule) rules.elementAt(row);
			} catch (ArrayIndexOutOfBoundsException e) {
				return null;
			}

			try {
				rule = (Rule) rule.clone();
			} catch (CloneNotSupportedException e) {
				logger.error("Could not clone a rule", e);
			}

			return rule;
		}

		public Object getValueAt(int row, int column) {
			if (row < rules.size()) {
				final Rule rule = (Rule) rules.elementAt(row);
				final ColumnDescriptor columnDescriptor = (ColumnDescriptor) rule.columns
						.elementAt(0);

				if (column == RULEID)
					return rule.name;
				else if (column == PURPOSE)
					return rule.purposes.elementAt(0);
				else if (column == RECIPIENT)
					return rule.recipients.elementAt(0);
				else if (column == ACCESSOR)
					return rule.accessors.elementAt(0);
				else if (column == SCHEMA)
					return columnDescriptor.table.schemaName;
				else if (column == TABLE)
					return columnDescriptor.table.tableName;
				else if (column == COLUMN)
					return columnDescriptor.columnName;
				else if (column == CONDITION) {
					/*
					 * amol pujari 09/10/2006 added to show the only relevant
					 * part of condition to the user
					 */

					if (rule.condition.length() > 0) {
						String str = rule.condition.substring(rule.condition
								.indexOf("(") + 1);
						try {
							str = str.substring(str.indexOf("(\n"));
						} catch (Exception e) {
						}// array index out of bound in case of audit known
						// ...so ignoring...
						try {
							str = str.substring(1, str.indexOf("\n)"));
						} catch (Exception e) {
							str = "";
						}// array index out of bound in case of audit known
						// ...so ignoring...

						return str;
					}

					return rule.condition;
				} else if (column == PSEUDONYM)
					return new Boolean(columnDescriptor.pseudonym);
			}
			return "";
		}

		public String getColumnName(int column) {
			if (column == RULEID)
				return ResourceManager.getResource("table.rule");
			else if (column == PURPOSE)
				return ResourceManager.getResource("table.purpose");
			else if (column == ACCESSOR)
				return ResourceManager.getResource("table.accessor");
			else if (column == RECIPIENT)
				return ResourceManager.getResource("table.recipient");
			else if (column == SCHEMA)
				return ResourceManager.getResource("table.schema");
			else if (column == TABLE)
				return ResourceManager.getResource("table.table");
			else if (column == COLUMN)
				return ResourceManager.getResource("table.column");
			else if (column == CONDITION)
				return ResourceManager.getResource("table.cond");
			else if (column == PSEUDONYM)
				return ResourceManager.getResource("table.pseud");
			else
				return "";
		}

		public Vector getRules() {
			return rules;
		}
	}

	public class PopupMenus {
		public JPopupMenu popupMenu;

		public JMenuItem editMenuItem;

		public JMenuItem deleteMenuItem;

		public PopupMenus(ActionListener listener) {
			popupMenu = new JPopupMenu();

			editMenuItem = new JMenuItem("Edit");
			editMenuItem.addActionListener(listener);
			editMenuItem.setAccelerator(KeyStroke.getKeyStroke("alt E"));
			deleteMenuItem = new JMenuItem("Delete");
			deleteMenuItem.setAccelerator(KeyStroke.getKeyStroke("DELETE"));
			deleteMenuItem.addActionListener(listener);
			popupMenu.add(editMenuItem);
			popupMenu.add(deleteMenuItem);
		}
	}

	public void applicationChanged(String databaseName) {
		// TODO Auto-generated method stub

	}

	public void entitiesChanged(String databaseName) {
		// TODO Auto-generated method stub

	}

	public void purposesChanged(String databaseName) {
		// TODO Auto-generated method stub

	}

	public void recipientsChanged(String databaseName) {
		// TODO Auto-generated method stub

	}

	public void accessorsChanged(String databaseName) {
		// TODO Auto-generated method stub

	}

	public void backlogChanged(String databaseName) {
		// TODO Auto-generated method stub

	}

	public void columnsInScopeChanged(String databaseName, String policyName) {
		// TODO Auto-generated method stub

	}

}