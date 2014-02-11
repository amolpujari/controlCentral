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

public class EntityDisplayPanel extends JPanel implements ActionListener,
		ListSelectionListener, ResourceListener {
	private JTable entityTable;

	private final ControlCenter controlCenter;

	private EntityTableModel entityTableModel;

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

	public EntityDisplayPanel(ControlCenter controlCenter) {
		this.controlCenter = controlCenter;
		controlCenter.resourceManager.addResourceListener(this);

		popupMenus = new PopupMenus(this);

		entityTableModel = new EntityTableModel();
		entityTable = new AppJTable(entityTableModel);
		final MyListener listener = new MyListener();
		entityTable.addMouseListener(listener);
		entityTable.getSelectionModel().addListSelectionListener(this);
		entityTable.setRowHeight(20);
		entityTable.setShowVerticalLines(false);
		entityTable.setGridColor(Color.WHITE);
		entityTable.setBackground(ResourceManager.TABLE_BACKGROUND_COLOR);
		entityTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		entityTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		JScrollPane scrollPane = new JScrollPane(entityTable);
		setLayout(new BorderLayout());
		add(scrollPane, BorderLayout.CENTER);
		entityTable.getInputMap().put(
				KeyStroke.getKeyStroke("released DELETE"), "DELETE");
		entityTable.getActionMap().put("DELETE", new AbstractAction("DELETE") {
			public void actionPerformed(ActionEvent e) {
				performDelete();
			}
		});
	}

	private void performDelete() {
		final String msg = controlCenter.resourceManager.dropEntities(
				entityTableModel.getDatabaseName(), entityTableModel
						.getEntities(selectedRow));

		MessageBox.show(controlCenter, ResourceManager
				.getResource("label.message"), msg, MessageBox.ICON_INFO);
	}

	public void showPanel(Vector versionInfo) {
		final Vector entities = controlCenter.resourceManager.getEntries(
				(String) versionInfo.elementAt(0), ResourceManager
						.getResource("metadata.table.entities"));
		entityTableModel.setEntities(entities);
		entityTableModel.setDatabaseName((String) versionInfo.elementAt(0));
		entityTable.clearSelection();
	}

	public void valueChanged(ListSelectionEvent e) {
		if (e.getSource() == entityTable.getSelectionModel()) {
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
				int i = entityTable.rowAtPoint(e.getPoint());

				if (i >= entityTable.getColumnCount() || i < 0)
					i = entityTable.getSelectedColumn();

				// /controlCenter.auditor.infoPanel.setApplication(entityTableModel.getApplication(i));
				// /controlCenter.auditor.setDivider();
			} catch (Exception exp) {
				exp.printStackTrace();
			}
		}

		public void mousePressed(MouseEvent e) {

			if (e.getButton() != MouseEvent.BUTTON1) {
				final int i = entityTable.rowAtPoint(e.getPoint());
				entityTable.clearSelection();
				entityTable.addRowSelectionInterval(i, i);
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
	class EntityTableModel extends AbstractTableModel {

		private Vector entities;

		private String databaseName;

		public String getDatabaseName() {
			return databaseName;
		}

		public void setDatabaseName(final String databaseName) {
			this.databaseName = databaseName;
		}

		public EntityTableModel() {
			super();
			entities = new Vector(4, 2);
		}

		public void setEntities(Vector entities) {
			this.entities = entities;
		}

		public Vector getEntities(int row) {

			final Vector entity = new Vector(1, 1);
			entity.add(entities.elementAt(row));
			return entity;
		}

		public int getRowCount() {
			return entities.size();
		}

		public int getColumnCount() {
			// Display only the first columns. They others are "invisible".
			return 1;
		}

		static final int TABLE = 0;

		public TableDescriptor getEntity(int row) {

			final TableDescriptor entity = (TableDescriptor) entities
					.elementAt(row);
			return entity;
		}

		public Object getValueAt(int row, int column) {

			if (row < entities.size()) {

				final TableDescriptor entity = getEntity(row);

				if (column == TABLE)
					return entity.toVerboseString();
			}
			return "";
		}

		public String getColumnName(int column) {
			if (column == TABLE)
				return ResourceManager.getResource("table.entity.tables");
			else
				return "";
		}
	}

	public void auditChanged(Version version) {
		// TODO Auto-generated method stub

	}

	public void entitiesChanged(String databaseName) {
		final Vector entities = controlCenter.resourceManager.getEntries(
				databaseName, ResourceManager
						.getResource("metadata.table.entities"));
		entityTableModel.setEntities(entities);
		entityTableModel.setDatabaseName(databaseName);
		entityTableModel.fireTableDataChanged();

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
