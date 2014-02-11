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
import javax.swing.table.AbstractTableModel;

public class BacklogDisplayPanel extends JPanel implements ActionListener,
		ListSelectionListener, ResourceListener {

	private JTable backlogTable;

	private final ControlCenter controlCenter;

	private BacklogTableModel backlogTableModel;

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

	public BacklogDisplayPanel(ControlCenter controlCenter) {
		this.controlCenter = controlCenter;
		controlCenter.resourceManager.addResourceListener(this);

		popupMenus = new PopupMenus(this);

		backlogTableModel = new BacklogTableModel();
		backlogTable = new AppJTable(backlogTableModel);
		final MyListener listener = new MyListener();
		backlogTable.addMouseListener(listener);
		backlogTable.getSelectionModel().addListSelectionListener(this);
		backlogTable.setRowHeight(20);
		backlogTable.setShowVerticalLines(false);
		backlogTable.setGridColor(Color.WHITE);
		backlogTable.setBackground(ResourceManager.TABLE_BACKGROUND_COLOR);
		backlogTable.setAutoscrolls(true);
		backlogTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		backlogTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		JScrollPane scrollPane = new JScrollPane(backlogTable);
		setLayout(new BorderLayout());
		add(scrollPane, BorderLayout.CENTER);
		backlogTable.getInputMap().put(
				KeyStroke.getKeyStroke("released DELETE"), "DELETE");
		backlogTable.getActionMap().put("DELETE", new AbstractAction("DELETE") {
			public void actionPerformed(ActionEvent e) {
				performDelete();
			}
		});
	}

	private void performDelete() {
		if (MessageBox.result(controlCenter, ResourceManager
				.getResource("label.confirm"), ResourceManager
				.getResource("label.backlogDisplayPanel.1")
				+ " " + backlogTableModel.getBacklog(selectedRow),
				MessageBox.ICON_CON) == MessageBox.BUTTON_YES)
			if (!controlCenter.resourceManager
					.dropBacklogTable(backlogTableModel.getBacklog(selectedRow)))
				MessageBox.show(controlCenter, ResourceManager
						.getResource("label.error"), ResourceManager
						.getResource("label.backlogDisplayPanel.0"),
						MessageBox.ICON_ERR);
	}

	public void showPanel(Vector versionInfo) {
		final Vector backlogs = controlCenter.resourceManager
				.getTablesWithBacklog((String) versionInfo.elementAt(0));
		backlogTableModel.setbacklogs(backlogs);
		backlogTableModel.setDatabaseName((String) versionInfo.elementAt(0));
		backlogTable.clearSelection();
	}

	public void valueChanged(ListSelectionEvent e) {
		if (e.getSource() == backlogTable.getSelectionModel()) {
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
				int i = backlogTable.rowAtPoint(e.getPoint());

				if (i >= backlogTable.getColumnCount() || i < 0)
					i = backlogTable.getSelectedColumn();

				// /controlCenter.auditor.infoPanel.setApplication(backlogTableModel.getApplication(i));
				// /controlCenter.auditor.setDivider();
			} catch (Exception exp) {
				exp.printStackTrace();
			}
		}

		public void mousePressed(MouseEvent e) {

			if (e.getButton() != MouseEvent.BUTTON1) {
				final int i = backlogTable.rowAtPoint(e.getPoint());
				backlogTable.clearSelection();
				backlogTable.addRowSelectionInterval(i, i);
				popupMenus.popupMenu.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == popupMenus.deleteItem) {
			performDelete();
		}
	}

	/**
	 * Note that the following columns are NOT show althought they are stored in
	 * the database bacause they can be seen in the tree view that was used to
	 * arrive at this table display.
	 */
	class BacklogTableModel extends AbstractTableModel {

		private Vector backlogs;

		private String databaseName;

		public String getDatabaseName() {
			return databaseName;
		}

		public void setDatabaseName(final String databaseName) {
			this.databaseName = databaseName;
		}

		public BacklogTableModel() {
			super();
			backlogs = new Vector(4, 2);
		}

		public void setbacklogs(Vector backlogs) {
			this.backlogs = backlogs;
		}

		public int getRowCount() {
			return backlogs.size();
		}

		public int getColumnCount() {
			// Display only the first columns. They others are "invisible".
			return 1;
		}

		static final int TABLE = 0;

		public TableDescriptor getBacklog(int row) {

			final TableDescriptor backlog = (TableDescriptor) backlogs
					.elementAt(row);
			return backlog;
		}

		public Object getValueAt(int row, int column) {

			if (row < backlogs.size()) {

				final TableDescriptor backlog = getBacklog(row);

				if (column == TABLE)
					return backlog.toVerboseString();
			}
			return "";
		}

		public String getColumnName(int column) {
			if (column == TABLE)
				return ResourceManager.getResource("table.backlogged.tables");
			else
				return "";
		}
	}

	public void auditChanged(Version version) {
		// TODO Auto-generated method stub

	}

	public void backlogChanged(String databaseName) {
		final Vector backlogs = controlCenter.resourceManager
				.getTablesWithBacklog(databaseName);
		backlogTableModel.setbacklogs(backlogs);
		backlogTableModel.setDatabaseName(databaseName);
		backlogTableModel.fireTableDataChanged();

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

	public void columnsInScopeChanged(String databaseName, String policyName) {
		// TODO Auto-generated method stub

	}

	public void entitiesChanged(String databaseName) {
		// TODO Auto-generated method stub

	}
}
