package controlCenter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import org.apache.log4j.Logger;

public class QueryDisplayPanel extends JPanel implements
// ActionListener,
		ListSelectionListener, ResourceListener {
	private ControlCenter controlCenter;

	private JTable queryTable;

	private QueryTableModel tableModel;

	// private PopupMenus popupMenus;
	private static final int NONE = -1;

	private Vector queryResults;

	private Vector queryVerdicts;

	private final Color COLOR_SUSPICIOUS = new Color(222, 203, 217);

	private final Color COLOR_CANDIDATE = new Color(204, 206, 221);

	private InfoPanel queryInfoPanel;

	private int sortedColumn = -1;

	private boolean asc = true;

	class CustomTable extends AppJTable {
		public CustomTable(TableModel tableModel) {
			super(tableModel);
		}

		public Component prepareRenderer(TableCellRenderer renderer,
				int rowIndex, int vColIndex) {
			Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);

			if (((String) queryVerdicts.get(rowIndex))
					.equalsIgnoreCase(ResourceManager
							.getResource("table.value.suspicious"))) {
				c.setBackground(COLOR_SUSPICIOUS);
			} else
				c.setBackground(COLOR_CANDIDATE);
			return c;
		}
	}

	TableCellRenderer iconHeaderRenderer = new DefaultTableCellRenderer() {
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {

			if (table != null) {
				JTableHeader header = table.getTableHeader();
				if (header != null) {
					setForeground(header.getForeground());
					setBackground(header.getBackground());
					setFont(header.getFont());
				}
			}

			setText(table.getModel().getColumnName(column));
			if (column == sortedColumn)
				setIcon(((asc) ? ResourceManager.ICON_ascending
						: ResourceManager.ICON_descending));
			else
				setIcon(null);
			setBorder(UIManager.getBorder("TableHeader.cellBorder"));
			// setHorizontalAlignment(JLabel.LEFT);
			return this;
		}
	};

	public QueryDisplayPanel(ControlCenter controlCenter,
			final InfoPanel queryInfoPanel) {
		this.controlCenter = controlCenter;
		this.queryInfoPanel = queryInfoPanel;
		controlCenter.resourceManager.addResourceListener(this);

		tableModel = new QueryTableModel();
		queryTable = new CustomTable(tableModel);
		queryTable.setRowHeight(20);
		queryTable.setShowVerticalLines(false);
		queryTable.setGridColor(Color.WHITE);
		queryTable.setBackground(ResourceManager.TABLE_BACKGROUND_COLOR);
		queryTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		queryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		queryTable.getSelectionModel().addListSelectionListener(this);

		for (int i = 0; i < tableModel.getColumnCount(); i++)
			queryTable.getTableHeader().getColumnModel().getColumn(i)
					.setHeaderRenderer(iconHeaderRenderer);

		queryTable.getTableHeader().addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent arg0) {
			}

			public void mouseEntered(MouseEvent arg0) {
			}

			public void mouseExited(MouseEvent arg0) {
			}

			public void mousePressed(MouseEvent arg0) {
			}

			public void mouseReleased(MouseEvent arg0) {
				asc = (sortedColumn == queryTable
						.columnAtPoint(arg0.getPoint())) ? !asc : true;
				sortedColumn = queryTable.columnAtPoint(arg0.getPoint());
				tableModel.prepareDataSortedBy(sortedColumn, asc);
			}
		});

		JScrollPane scrollPane = new JScrollPane(queryTable);
		setLayout(new BorderLayout());
		add(scrollPane, BorderLayout.CENTER);
		queryTable.setSelectionForeground(Color.BLUE);
	}

	public void showPanel(final String databaseName,
			Vector resultQueryIdentifiers, Vector queryResults,
			Vector queryVerdicts) {

		this.queryResults = queryResults;
		this.queryVerdicts = queryVerdicts;
		final Vector queries = controlCenter.resourceManager
				.getAuditResultQueries(databaseName, resultQueryIdentifiers);
		tableModel.setQueries(queries);
		queryInfoPanel.setQuery(tableModel.getQuery(selectedRow));
		tableModel.fireTableDataChanged();
	}

	public void reset() {
		tableModel.setQueries(new Vector());
		tableModel.fireTableDataChanged();
		queryInfoPanel.reset();
	}

	private int selectedRow = NONE;

	public void valueChanged(ListSelectionEvent e) {
		if (e.getSource() == queryTable.getSelectionModel()) {
			ListSelectionModel listSelectionModel = (ListSelectionModel) e
					.getSource();

			boolean hasSelection = !listSelectionModel.isSelectionEmpty();

			if (hasSelection) {
				selectedRow = listSelectionModel.getMinSelectionIndex();
				queryInfoPanel.setQuery(tableModel.getQuery(selectedRow));
			} else
				selectedRow = NONE;
		}
	}

	public void auditChanged(Version version) {
	}

	public void policyChanged(Version version) {
		// Do nothing. Not needed.
	}

	public void databaseConnected(String dbname) {
	} // ignored

	public void databaseDisconnected(String dbname) {
	} // ignored

	public void actionPerformed(ActionEvent e) {
	}

	public void mousePressed(MouseEvent e) {
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

	public void columnsInScopeChanged(String databaseName, String policyName) {
		// TODO Auto-generated method stub

	}

	public void backlogChanged(String databaseName) {
		// TODO Auto-generated method stub

	}

	/**
	 * Note that the following columns are NOT show althought they are stored in
	 * the database bacause they can be seen in the tree view that was used to
	 * arrive at this table display.
	 */
	class QueryTableModel extends AbstractTableModel {
		private Vector queries;

		private final Logger logger = Logger.getLogger(getClass().getName());

		public QueryTableModel() {
			super();
			queries = new Vector(4, 2);
		}

		public void setQueries(Vector queries) {
			this.queries = queries;
		}

		public int getRowCount() {
			return queries.size();
		}

		public int getColumnCount() {
			// Display only the first columns. They others are "invisible".
			return 8;
		}

		static final int QUERYID = 0;

		static final int PURPOSE = 1;

		static final int ACCESSOR = 2;

		static final int RECIPIENT = 3;

		static final int ISOLATION_LEVEL = 4;

		static final int BEGIN = 5;

		static final int END = 6;

		static final int VERDICT = 7;

		public Query getQuery(int row) {
			Query query;

			try {
				query = (Query) queries.elementAt(row);
				query = (Query) query.clone();
				query.result = (String) queryResults.get(row);
				query.verdict = (String) queryVerdicts.get(row);
			} catch (ArrayIndexOutOfBoundsException e) {
				return null;
			} catch (CloneNotSupportedException e) {
				return null;
			}

			return query;
		}

		public Object getValueAt(int row, int column) {
			if (row < queries.size()) {
				final Query query = getQuery(row);

				if (column == QUERYID)
					return query.name;
				else if (column == PURPOSE)
					return query.purpose;
				else if (column == ACCESSOR)
					return query.accessor;
				else if (column == RECIPIENT)
					return query.recipient;
				else if (column == BEGIN)
					return query.begin;
				else if (column == END)
					return query.end;
				else if (column == ISOLATION_LEVEL)
					return query.isolationText;
				else if (column == VERDICT)
					return query.verdict;
			}
			return "";
		}

		public String getColumnName(int column) {
			if (column == QUERYID)
				return ResourceManager.getResource("table.queryId");
			else if (column == PURPOSE)
				return ResourceManager.getResource("table.purpose");
			else if (column == ACCESSOR)
				return ResourceManager.getResource("table.accessor");
			else if (column == RECIPIENT)
				return ResourceManager.getResource("table.recipient");
			else if (column == BEGIN)
				return ResourceManager.getResource("table.start.time");
			else if (column == END)
				return ResourceManager.getResource("table.end.time");
			else if (column == ISOLATION_LEVEL)
				return ResourceManager.getResource("table.iso");
			else if (column == VERDICT)
				return ResourceManager.getResource("table.verdict");
			else
				return "";
		}

		public void prepareDataSortedBy(final int column,
				final boolean ascending) {

			if (queries.size() < 1)
				return;

			final Vector sorted = new Vector(4, 2);
			Object element;
			Object param;
			// Object param2;
			boolean added = false;

			for (int i = 0; i < queries.size(); i++) {
				element = queries.get(i);
				param = getValueAt(i, column);

				for (int j = 0; j < sorted.size(); j++) {

					if (ascending) {
						if (column == 0)// int column
						{
							if (Integer.parseInt(param.toString()) < Integer
									.parseInt(getValueAt(
											queries.indexOf(sorted.get(j)),
											column).toString())) {
								sorted.insertElementAt(element, j);
								added = true;
								break;
							}
						} else if (param.toString().compareToIgnoreCase(
								getValueAt(queries.indexOf(sorted.get(j)),
										column).toString()) < 0) {
							sorted.insertElementAt(element, j);
							added = true;
							break;
						}
					} else {
						if (column == 0)// int column
						{
							if (Integer.parseInt(param.toString()) > Integer
									.parseInt(getValueAt(
											queries.indexOf(sorted.get(j)),
											column).toString())) {
								sorted.insertElementAt(element, j);
								added = true;
								break;
							}
						} else if (param.toString().compareToIgnoreCase(
								getValueAt(queries.indexOf(sorted.get(j)),
										column).toString()) > 0) {
							sorted.insertElementAt(element, j);
							added = true;
							break;
						}
					}
				}

				if (!added)
					sorted.add(element);

				added = false;
			}
			queries = sorted;
			fireTableDataChanged();
		}
	}
}