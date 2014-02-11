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

public class RecipientDisplayPanel extends JPanel implements ActionListener,
		ResourceListener {

	private JTable recipientTable;

	private final ControlCenter controlCenter;

	private RecipientTableModel recipientTableModel;

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

	public RecipientDisplayPanel(ControlCenter controlCenter) {
		this.controlCenter = controlCenter;
		controlCenter.resourceManager.addResourceListener(this);

		popupMenus = new PopupMenus(this);

		recipientTableModel = new RecipientTableModel();
		recipientTable = new AppJTable(recipientTableModel);
		final MyListener listener = new MyListener();
		recipientTable.addMouseListener(listener);
		recipientTable.setRowHeight(20);
		recipientTable.setShowVerticalLines(false);
		recipientTable.setGridColor(Color.WHITE);
		recipientTable.setBackground(ResourceManager.TABLE_BACKGROUND_COLOR);
		recipientTable.setAutoscrolls(true);
		recipientTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		JScrollPane scrollPane = new JScrollPane(recipientTable);
		setLayout(new BorderLayout());
		add(scrollPane, BorderLayout.CENTER);
		recipientTable.getInputMap().put(
				KeyStroke.getKeyStroke("released DELETE"), "DELETE");
		recipientTable.getActionMap().put("DELETE",
				new AbstractAction("DELETE") {
					public void actionPerformed(ActionEvent e) {
						performDelete();
					}
				});
	}

	private void performDelete() {
		final int[] selection = recipientTable.getSelectedRows();
		final String[] entries = new String[selection.length];

		for (int i = 0; i < selection.length; i++)
			entries[i] = recipientTableModel.getRecipient(selection[i]);

		controlCenter.resourceManager.batchDeleteEntries(recipientTableModel
				.getDatabaseName(), ResourceManager.DELETE_ALL_RECIPIENTS,
				entries);
	}

	public void showPanel(Vector versionInfo) {
		final Vector recipients = controlCenter.resourceManager.getEntries(
				(String) versionInfo.elementAt(0), ResourceManager
						.getResource("metadata.table.recipients"));
		recipientTableModel.setRecipients(recipients);
		recipientTableModel.setDatabaseName((String) versionInfo.elementAt(0));
		recipientTable.clearSelection();
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
				final int i = recipientTable.rowAtPoint(e.getPoint());
				if (!e.isControlDown())
					recipientTable.clearSelection();
				recipientTable.addRowSelectionInterval(i, i);
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
	class RecipientTableModel extends AbstractTableModel {

		private Vector recipients;

		private String databaseName;

		public RecipientTableModel() {
			super();
			recipients = new Vector(4, 2);
		}

		public String getDatabaseName() {
			return databaseName;
		}

		public void setDatabaseName(final String databaseName) {
			this.databaseName = databaseName;
		}

		public void setRecipients(Vector recipients) {
			this.recipients = recipients;
		}

		public int getRowCount() {
			return recipients.size();
		}

		public int getColumnCount() {
			// Display only the first columns. They others are "invisible".
			return 1;
		}

		static final int RECIPIENTS = 0;

		public String getRecipient(int row) {

			final String recipient = (String) recipients.elementAt(row);
			return recipient;
		}

		public Object getValueAt(int row, int column) {

			if (row < recipients.size()) {

				final String recipient = getRecipient(row);

				if (column == RECIPIENTS)
					return recipient;
			}
			return "";
		}

		public String getColumnName(int column) {
			if (column == RECIPIENTS)
				return ResourceManager.getResource("table.recipients");
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
	}

	public void recipientsChanged(String databaseName) {
		final Vector recipients = controlCenter.resourceManager.getEntries(
				databaseName, ResourceManager
						.getResource("metadata.table.recipients"));
		recipientTableModel.setRecipients(recipients);
		recipientTableModel.setDatabaseName(databaseName);
		recipientTableModel.fireTableDataChanged();
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
