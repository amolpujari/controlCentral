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

public class ApplicationDisplayPanel extends JPanel implements ActionListener,
		ListSelectionListener, ResourceListener {
	private JTable appTable;

	private final ControlCenter controlCenter;

	private ApplicationTableModel appTableModel;

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

	public ApplicationDisplayPanel(ControlCenter controlCenter) {
		this.controlCenter = controlCenter;
		controlCenter.resourceManager.addResourceListener(this);
		
		popupMenus = new PopupMenus(this);

		appTableModel = new ApplicationTableModel();
		appTable = new AppJTable(appTableModel);
		final MyListener listener = new MyListener();
		appTable.addMouseListener(listener);
		appTable.getSelectionModel().addListSelectionListener(this);
		appTable.setRowHeight(20);
		appTable.setShowVerticalLines(false);
		appTable.setGridColor(Color.WHITE);
		appTable.setBackground(ResourceManager.TABLE_BACKGROUND_COLOR);
		appTable.setAutoscrolls(true);
		appTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		JScrollPane scrollPane = new JScrollPane(appTable);
		setLayout(new BorderLayout());
		add(scrollPane, BorderLayout.CENTER);
	}

	public void showPanel(Vector versionInfo) {
		final Vector applications = controlCenter.resourceManager
				.getApplicationUsage((String) versionInfo.elementAt(0));
		appTableModel.setApplications(applications);
		appTable.clearSelection();
		appTableModel.setDatabaseName((String) versionInfo.elementAt(0));
	}

	public void valueChanged(ListSelectionEvent e) {
		if (e.getSource() == appTable.getSelectionModel()) {
			ListSelectionModel listSelectionModel = (ListSelectionModel) e
					.getSource();

			boolean hasSelection = !listSelectionModel.isSelectionEmpty();
			if (hasSelection) {
				// /selectedRow = listSelectionModel.getMinSelectionIndex();
				// /controlCenter.auditor.infoPanel.setApplication(appTableModel.getApplication(selectedRow));
				// /controlCenter.auditor.setDivider();
			}
		}
	}

	public void applicationChanged(final String databaseName) {
		final Vector applications = controlCenter.resourceManager
				.getApplicationUsage(databaseName);
		appTableModel.setApplications(applications);
		appTable.clearSelection();
		appTableModel.fireTableDataChanged();
		appTableModel.setDatabaseName(databaseName);
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
				final int i = appTable.rowAtPoint(e.getPoint());

				if (!e.isControlDown())
					appTable.clearSelection();

				appTable.addRowSelectionInterval(i, i);
				popupMenus.popupMenu.show(e.getComponent(), e.getX(), e.getY());
			}
		}

		public void mousePressed(MouseEvent e) {
		}
	}

	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == popupMenus.deleteItem) {
			final int[] selection = appTable.getSelectedRows();
			final String[] entries = new String[selection.length];

			for (int i = 0; i < selection.length; i++)
				entries[i] = appTableModel.getApplication(selection[i]).name;

			controlCenter.resourceManager.deleteApplicationUsage(appTableModel
					.getDatabaseName(), entries);
		}

	}

	/**
	 * Note that the following columns are NOT show althought they are stored in
	 * the database bacause they can be seen in the tree view that was used to
	 * arrive at this table display.
	 */
	class ApplicationTableModel extends AbstractTableModel {

		private Vector applications;

		private String databaseName;

		public String getDatabaseName() {
			return databaseName;
		}

		public void setDatabaseName(final String databaseName) {
			this.databaseName = databaseName;
		}

		public ApplicationTableModel() {
			super();
			applications = new Vector(4, 2);
		}

		public void setApplications(Vector applications) {
			this.applications = applications;
		}

		public int getRowCount() {
			return applications.size();
		}

		public int getColumnCount() {
			// Display only the first columns. They others are "invisible".
			return 4;
		}

		static final int APPID = 0;

		static final int PURPOSES = 1;

		static final int ACCESSORS = 2;

		static final int RECIPIENTS = 3;

		public ApplicationUsage getApplication(int row) {

			final ApplicationUsage application = (ApplicationUsage) applications
					.elementAt(row);
			return application;
		}

		public Object getValueAt(int row, int column) {

			if (row < applications.size()) {

				final ApplicationUsage application = getApplication(row);

				if (column == APPID)
					return application.name;
				else if (column == PURPOSES)
					return application.purpose;
				else if (column == RECIPIENTS)
					return application.recipient;
				else if (column == ACCESSORS)
					return application.accessor;
			}
			return "";
		}

		public String getColumnName(int column) {
			if (column == APPID)
				return ResourceManager.getResource("table.application");
			else if (column == PURPOSES)
				return ResourceManager.getResource("table.purpose");
			else if (column == ACCESSORS)
				return ResourceManager.getResource("table.accessor");
			else if (column == RECIPIENTS)
				return ResourceManager.getResource("table.recipient");
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
		// TODO Auto-generated method stub

	}

	public void backlogChanged(String databaseName) {
		// TODO Auto-generated method stub

	}

	public void columnsInScopeChanged(String databaseName, String policyName) {
		// TODO Auto-generated method stub

	}
}
