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

public class PurposeDisplayPanel extends JPanel implements ActionListener,
		ResourceListener {

	private JTable purposeTable;

	private final ControlCenter controlCenter;

	private PurposeTableModel purposeTableModel;

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

	public PurposeDisplayPanel(ControlCenter controlCenter) {
		this.controlCenter = controlCenter;
		controlCenter.resourceManager.addResourceListener(this);

		popupMenus = new PopupMenus(this);

		purposeTableModel = new PurposeTableModel();
		purposeTable = new AppJTable(purposeTableModel);
		final MyListener listener = new MyListener();
		purposeTable.addMouseListener(listener);
		purposeTable.setRowHeight(20);
		purposeTable.setShowVerticalLines(false);
		purposeTable.setGridColor(Color.WHITE);
		purposeTable.setBackground(ResourceManager.TABLE_BACKGROUND_COLOR);
		purposeTable.setAutoscrolls(true);
		purposeTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		JScrollPane scrollPane = new JScrollPane(purposeTable);
		setLayout(new BorderLayout());
		add(scrollPane, BorderLayout.CENTER);
		purposeTable.getInputMap().put(
				KeyStroke.getKeyStroke("released DELETE"), "DELETE");
		purposeTable.getActionMap().put("DELETE", new AbstractAction("DELETE") {
			public void actionPerformed(ActionEvent e) {
				performDelete();
			}
		});
	}

	private void performDelete() {
		final int[] selection = purposeTable.getSelectedRows();
		final String[] entries = new String[selection.length];

		for (int i = 0; i < selection.length; i++)
			entries[i] = purposeTableModel.getPurpose(selection[i]);

		controlCenter.resourceManager.batchDeleteEntries(purposeTableModel
				.getDatabaseName(), ResourceManager.DELETE_ALL_PURPOSES,
				entries);
	}

	public void showPanel(Vector versionInfo) {
		final Vector purposes = controlCenter.resourceManager.getEntries(
				(String) versionInfo.elementAt(0), ResourceManager
						.getResource("metadata.table.purposes"));
		purposeTableModel.setPurposes(purposes);
		purposeTable.clearSelection();
		purposeTableModel.setDatabaseName((String) versionInfo.elementAt(0));
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
				final int i = purposeTable.rowAtPoint(e.getPoint());
				if (!e.isControlDown())
					purposeTable.clearSelection();
				purposeTable.addRowSelectionInterval(i, i);
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

	/**
	 * Note that the following columns are NOT show althought they are stored in
	 * the database bacause they can be seen in the tree view that was used to
	 * arrive at this table display.
	 */
	class PurposeTableModel extends AbstractTableModel {

		private Vector purposes;

		private String databaseName;

		public void setDatabaseName(final String databaseName) {
			this.databaseName = databaseName;
		}

		public String getDatabaseName() {
			return databaseName;
		}

		public PurposeTableModel() {
			super();
			purposes = new Vector(4, 2);
		}

		public void setPurposes(Vector purposes) {
			this.purposes = purposes;
		}

		public int getRowCount() {
			return purposes.size();
		}

		public int getColumnCount() {
			// Display only the first columns. They others are "invisible".
			return 1;
		}

		static final int PURPOSES = 0;

		public String getPurpose(int row) {

			final String purpose = (String) purposes.elementAt(row);
			return purpose;
		}

		public Object getValueAt(int row, int column) {

			if (row < purposes.size()) {

				final String purpose = getPurpose(row);

				if (column == PURPOSES)
					return purpose;
			}
			return "";
		}

		public String getColumnName(int column) {
			if (column == PURPOSES)
				return ResourceManager.getResource("table.purposes");
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
		final Vector purposes = controlCenter.resourceManager.getEntries(
				databaseName, ResourceManager
						.getResource("metadata.table.purposes"));
		purposeTableModel.setPurposes(purposes);
		purposeTableModel.fireTableDataChanged();
		purposeTableModel.setDatabaseName(databaseName);
	}

	public void recipientsChanged(String databaseName) {
		// TODO Auto-generated method stub

	}

	public void accessorsChanged(String databaseName) {
	}

	public void backlogChanged(String databaseName) {
		// TODO Auto-generated method stub

	}

	public void columnsInScopeChanged(String databaseName, String policyName) {
		// TODO Auto-generated method stub

	}
}
