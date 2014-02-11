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
import javax.swing.table.AbstractTableModel;

public class AccessorDisplayPanel extends JPanel implements ActionListener,
		ResourceListener {

	private JTable accessorTable;

	private final ControlCenter controlCenter;

	private AccessorTableModel accessorTableModel;

	private PopupMenus popupMenus;

	public class PopupMenus {
		public JPopupMenu popupMenu;

		public JMenuItem deleteItem;

		public PopupMenus(ActionListener listener) {
			popupMenu = new JPopupMenu();
			deleteItem = new JMenuItem(ResourceManager.getResource("label.delete"));
			deleteItem.addActionListener(listener);
			popupMenu.add(deleteItem);
		}
	}

	public AccessorDisplayPanel(ControlCenter controlCenter) {
		this.controlCenter = controlCenter;
		controlCenter.resourceManager.addResourceListener(this);

		popupMenus = new PopupMenus(this);

		accessorTableModel = new AccessorTableModel();
		accessorTable = new AppJTable(accessorTableModel);
		final MyListener listener = new MyListener();
		accessorTable.addMouseListener(listener);
		accessorTable.setRowHeight(20);
		accessorTable.setShowVerticalLines(false);
		accessorTable.setGridColor(Color.WHITE);
		accessorTable.setBackground(ResourceManager.TABLE_BACKGROUND_COLOR);
		accessorTable.setAutoscrolls(true);
		accessorTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		JScrollPane scrollPane = new JScrollPane(accessorTable);
		setLayout(new BorderLayout());
		add(scrollPane, BorderLayout.CENTER);
		accessorTable.getInputMap().put(
				KeyStroke.getKeyStroke("released DELETE"), "DELETE");
		accessorTable.getActionMap().put("DELETE",
				new AbstractAction("DELETE") {
					public void actionPerformed(ActionEvent e) {
						performDelete();
					}
				});
	}

	public void showPanel(Vector versionInfo) {
		final Vector accessors = controlCenter.resourceManager.getEntries(
				(String) versionInfo.elementAt(0),ResourceManager
						.getResource("metadata.table.accessors"));
		accessorTableModel.setAccessors(accessors);
		accessorTable.clearSelection();
		accessorTableModel.setDatabaseName((String) versionInfo.elementAt(0));
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
			if (e.getButton() != MouseEvent.BUTTON1) {
				final int i = accessorTable.rowAtPoint(e.getPoint());

				if (!e.isControlDown())
					accessorTable.clearSelection();

				accessorTable.addRowSelectionInterval(i, i);
				popupMenus.popupMenu.show(e.getComponent(), e.getX(), e.getY());
			}
		}

		public void mousePressed(MouseEvent e) {
		}
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == popupMenus.deleteItem) {
			performDelete();
		}
	}

	private void performDelete() {
		final int[] selection = accessorTable.getSelectedRows();
		final String[] entries = new String[selection.length];

		for (int i = 0; i < selection.length; i++)
			entries[i] = accessorTableModel.getAccessor(selection[i]);

		controlCenter.resourceManager.batchDeleteEntries(accessorTableModel
				.getDatabaseName(), ResourceManager.DELETE_ALL_ACCESSORS,
				entries);
	}

	/**
	 * Note that the following columns are NOT show althought they are stored in
	 * the database bacause they can be seen in the tree view that was used to
	 * arrive at this table display.
	 */
	class AccessorTableModel extends AbstractTableModel {

		private Vector accessors;

		private String databaseName;

		public String getDatabaseName() {
			return databaseName;
		}

		public void setDatabaseName(final String databaseName) {
			this.databaseName = databaseName;
		}

		public AccessorTableModel() {
			super();
			accessors = new Vector(4, 2);
		}

		public void setAccessors(Vector accessors) {
			this.accessors = accessors;
		}

		public int getRowCount() {
			return accessors.size();
		}

		public int getColumnCount() {
			// Display only the first columns. They others are "invisible".
			return 1;
		}

		static final int ACCESSORS = 0;

		public String getAccessor(int row) {

			final String accessor = (String) accessors.elementAt(row);
			return accessor;
		}

		public Object getValueAt(int row, int column) {

			if (row < accessors.size()) {

				final String accessor = getAccessor(row);

				if (column == ACCESSORS)
					return accessor;
			}
			return "";
		}

		public String getColumnName(int column) {
			if (column == ACCESSORS)
				return ResourceManager.getResource("table.accessors");
			else
				return "";
		}
	}

	public void auditChanged(Version version) {
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
		final Vector accessors = controlCenter.resourceManager.getEntries(
				databaseName,ResourceManager
						.getResource("metadata.table.accessors"));
		accessorTableModel.setAccessors(accessors);
		accessorTableModel.fireTableDataChanged();
		accessorTableModel.setDatabaseName(databaseName);
	}

	public void backlogChanged(String databaseName) {
		// TODO Auto-generated method stub

	}

	public void columnsInScopeChanged(String databaseName, String policyName) {
		// TODO Auto-generated method stub

	}

}
