package controlCenter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

public class ColumnInScopeDisplayPanel extends JPanel implements
		ActionListener, ListSelectionListener, ResourceListener {
	private JTable table;

	private final ControlCenter controlCenter;

	private tableModel tableModel;

	private int selectedRow = -1;

	private PopupMenus popupMenus;

	public class PopupMenus {
		public JPopupMenu popupMenu;

		public JMenuItem deleteItem;

		public PopupMenus(ActionListener listener) {
			popupMenu = new JPopupMenu();
			deleteItem = new JMenuItem(ResourceManager
					.getResource("label.delete"));
			deleteItem.addActionListener(listener);
			popupMenu.add(deleteItem);
		}
	}

	public ColumnInScopeDisplayPanel(ControlCenter controlCenter) {
		this.controlCenter = controlCenter;
		controlCenter.resourceManager.addResourceListener(this);

		popupMenus = new PopupMenus(this);

		tableModel = new tableModel();
		table = new AppJTable(tableModel);
		final MyListener listener = new MyListener();
		table.addMouseListener(listener);
		table.getSelectionModel().addListSelectionListener(this);
		table.setRowHeight(20);
		table.setShowVerticalLines(false);
		table.setGridColor(Color.WHITE);
		table.setBackground(ResourceManager.TABLE_BACKGROUND_COLOR);
		table.setAutoscrolls(true);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		JScrollPane scrollPane = new JScrollPane(table);
		setLayout(new BorderLayout());
		add(scrollPane, BorderLayout.CENTER);
	}

	public void showPanel(Vector versionInfo) {

		final Vector columnsInScope = controlCenter.resourceManager
				.getColumnsInScope((String) versionInfo.elementAt(0),
						(String) versionInfo.elementAt(1));
		tableModel.setColumnsInScope(columnsInScope);
		tableModel.setDatabaseName((String) versionInfo.elementAt(0));
		tableModel.setPolicyName((String) versionInfo.elementAt(1));
		tableModel.fireTableDataChanged();
	}

	public void valueChanged(ListSelectionEvent e) {
		if (e.getSource() == table.getSelectionModel()) {
			ListSelectionModel listSelectionModel = (ListSelectionModel) e
					.getSource();

			boolean hasSelection = !listSelectionModel.isSelectionEmpty();
			if (hasSelection) {
				selectedRow = listSelectionModel.getMinSelectionIndex();
				// /controlCenter.auditor.infoPanel.setAccessor(accessorTableModel.getAccessor(selectedRow));
				// /controlCenter.auditor.setDivider();
			} else
				selectedRow = -1;
		}
	}

	public void applicationChanged(final String databaseName) {
	}

	public void policyChanged(Version version) {
	}

	public void databaseConnected(String dbname) {
	} // ignored

	public void databaseDisconnected(String dbname) {
	} // ignored

	class MyListener extends MouseAdapter {
		public void mouseReleased(MouseEvent e) {
			try {
				int i = table.rowAtPoint(e.getPoint());

				if (i >= table.getColumnCount() || i < 0)
					i = table.getSelectedColumn();

				// /controlCenter.auditor.infoPanel.setApplication(tableModel.getApplication(i));
				// /controlCenter.auditor.setDivider();
			} catch (Exception exp) {
				exp.printStackTrace();
			}
		}

		public void mousePressed(MouseEvent e) {

			if (e.getButton() != MouseEvent.BUTTON1) {
				final int i = table.rowAtPoint(e.getPoint());
				table.clearSelection();
				table.addRowSelectionInterval(i, i);
				popupMenus.popupMenu.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == popupMenus.deleteItem) {

			if (MessageBox.result(controlCenter, ResourceManager
					.getResource("label.confirm"), ResourceManager
					.getResource("label.cis.1"), MessageBox.ICON_WARN) != MessageBox.BUTTON_YES)
				return;

			final String msg = controlCenter.resourceManager.deleteScope(
					new Version(tableModel.getDatabaseName(), tableModel
							.getPolicyName(), ""), tableModel
							.getColumnsInScope(selectedRow));

			MessageBox.show(controlCenter, ResourceManager
					.getResource("label.error"), msg, MessageBox.ICON_ERR);

		}
	}

	/**
	 * Note that the following columns are NOT show althought they are stored in
	 * the database bacause they can be seen in the tree view that was used to
	 * arrive at this table display.
	 */
	class tableModel extends AbstractTableModel {

		private Vector columnsInScope;

		private String policyName;

		private String databaseName;

		public String getDatabaseName() {
			return databaseName;
		}

		public void setDatabaseName(final String databaseName) {
			this.databaseName = databaseName;
		}

		public tableModel() {
			super();
			columnsInScope = new Vector(4, 2);
		}

		public void setColumnsInScope(Vector columnsInScope) {
			this.columnsInScope = columnsInScope;
		}

		public Vector getColumnsInScope(int row) {

			final Vector columns = new Vector(1, 1);
			columns.add(columnsInScope.elementAt(row));
			return columns;
		}

		public int getRowCount() {
			return columnsInScope.size();
		}

		public int getColumnCount() {
			// Display only the first columns. They others are "invisible".
			return 1;
		}

		static final int TABLE = 0;

		public ColumnDescriptor getColumnInScope(int row) {

			final ColumnDescriptor entity = (ColumnDescriptor) columnsInScope
					.elementAt(row);
			return entity;
		}

		public Object getValueAt(int row, int column) {
			if (row < columnsInScope.size()) {
				final ColumnDescriptor columnsInScope = getColumnInScope(row);
				if (column == TABLE)
					return columnsInScope.toString();
			}
			return "";
		}

		public String getColumnName(int column) {
			if (column == TABLE)
				return ResourceManager.getResource("table.columnsinscope");
			else
				return "";
		}

		public String getPolicyName() {
			return policyName;
		}

		public void setPolicyName(String policyName) {
			this.policyName = policyName;
		}
	}

	public void auditChanged(Version version) {
		// TODO Auto-generated method stub

	}

	public void columnsInScopeChanged(final String databaseName,
			final String policyName) {

		final Vector columnsInScope = controlCenter.resourceManager
				.getColumnsInScope(databaseName, policyName);
		tableModel.setColumnsInScope(columnsInScope);
		tableModel.setDatabaseName(databaseName);
		tableModel.setPolicyName(policyName);
		tableModel.fireTableDataChanged();

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

	public void entitiesChanged(String databaseName) {
		// TODO Auto-generated method stub

	}

}
