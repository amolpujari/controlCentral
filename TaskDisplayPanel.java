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

public class TaskDisplayPanel extends JPanel implements ActionListener,
		ListSelectionListener, ResourceListener {
	private JTable taskTable;

	private final ControlCenter controlCenter;

	private TaskTableModel taskTableModel;

	private PopupMenus popupMenus;

	private Vector resultQueryIdentifiers; // Result of an audit using a single

	private Vector queryResults;

	private Vector queryVerdicts;

	private static final int NONE = -1;

	public TaskDisplayPanel(ControlCenter controlCenter) {
		this.controlCenter = controlCenter;
		popupMenus = new PopupMenus(this);
		controlCenter.resourceManager.addResourceListener(this);

		taskTableModel = new TaskTableModel();
		taskTable = new AppJTable(taskTableModel);
		final MyListener listener = new MyListener(
				controlCenter.resourceManager);
		taskTable.addMouseListener(listener);
		taskTable.getSelectionModel().addListSelectionListener(this);

		taskTable.setRowHeight(20);
		taskTable.setShowVerticalLines(false);
		taskTable.setGridColor(Color.WHITE);
		taskTable.setBackground(ResourceManager.TABLE_BACKGROUND_COLOR);
		taskTable.setAutoscrolls(true);

		taskTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		taskTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		JScrollPane scrollPane = new JScrollPane(taskTable);
		setLayout(new BorderLayout());
		add(scrollPane, BorderLayout.CENTER);

		resultQueryIdentifiers = new Vector(4, 2);
		queryResults = new Vector(4, 2);
		queryVerdicts = new Vector(4, 2);
		taskTable.getInputMap().put(KeyStroke.getKeyStroke("released DELETE"),
				"DELETE");
		taskTable.getActionMap().put("DELETE", new AbstractAction("DELETE") {
			public void actionPerformed(ActionEvent e) {
				performDelete();
			}
		});
		taskTable.getInputMap().put(KeyStroke.getKeyStroke("alt E"), "EDIT");
		taskTable.getActionMap().put("EDIT", new AbstractAction("EDIT") {
			public void actionPerformed(ActionEvent e) {
				performEdit();
			}
		});
		taskTable.getInputMap().put(KeyStroke.getKeyStroke("alt R"), "RUN");
		taskTable.getActionMap().put("RUN", new AbstractAction("RUN") {
			public void actionPerformed(ActionEvent e) {
				performRun();
			}
		});
	}

	private void performRun() {
		final Task task = taskTableModel.getTask(selectedRow);
		new RunTask(controlCenter, task);
	}

	private void performEdit() {
		final Task task = taskTableModel.getTask(selectedRow);
		MessageBox.showBusy(controlCenter, "Starting audit task wizard");
		new Thread(new Runnable() {
			public void run() {
				try {
					new AuditQueryWizard(false, task, (Task) task.clone(),
							controlCenter.resourceManager, version.databaseName);
				} catch (CloneNotSupportedException ex) {
					ex.printStackTrace();
				}
			}
		}).start();
	}

	private void performDelete() {
		final Task task = taskTableModel.getTask(selectedRow);
		if (MessageBox.result(controlCenter, ResourceManager
				.getResource("label.confirm"), ResourceManager
				.getResource("tdp.sure.del.task"), MessageBox.ICON_CON) == MessageBox.BUTTON_YES)
			if (!controlCenter.resourceManager.deleteTask(version, task))
				MessageBox.show(controlCenter, ResourceManager
						.getResource("label.error"), ResourceManager
						.getResource("tdp.could.not.del.task"),
						MessageBox.ICON_ERR);
	}

	public Vector getResultQueryIdentifiers() {
		return resultQueryIdentifiers;
	}

	private Version version;

	// this is a hack, should really define a better task object rather than
	// vector
	public void showPanel(Version version) {
		this.version = version;
		final Vector tasks = controlCenter.resourceManager
				.getTasksWithNestedColumns(version);
		taskTableModel.setTasks(tasks);
		taskTable.clearSelection();

		controlCenter.auditor.taskInfoPanel.setTask(taskTableModel
				.getTask(selectedRow));
		controlCenter.auditor.setDivider();

	}

	private int selectedRow = NONE;

	public void valueChanged(ListSelectionEvent e) {
		if (e.getSource() == taskTable.getSelectionModel()) {
			ListSelectionModel listSelectionModel = (ListSelectionModel) e
					.getSource();

			boolean hasSelection = !listSelectionModel.isSelectionEmpty();
			popupMenus.runMenuItem.setEnabled(hasSelection);
			popupMenus.editMenuItem.setEnabled(hasSelection);
			popupMenus.deleteMenuItem.setEnabled(hasSelection);

			if (hasSelection) {
				selectedRow = listSelectionModel.getMinSelectionIndex();

				controlCenter.auditor.taskInfoPanel.setTask(taskTableModel
						.getTask(selectedRow));
				controlCenter.auditor.setDivider();

			} else
				selectedRow = NONE;
		}
	}

	public void auditChanged(Version version) {
		final Vector tasks = controlCenter.resourceManager
				.getTasksWithNestedColumns(version);
		taskTableModel.setTasks(tasks);
		taskTableModel.fireTableDataChanged();
	}

	public void policyChanged(Version version) {
		// Do nothing. Not needed.
	}

	public void databaseConnected(String dbname) {
	} // ignored

	public void databaseDisconnected(String dbname) {
	} // ignored

	class MyListener extends MouseAdapter implements ActionListener,
			TableModelListener {
		private PopupMenus popupMenus;

		private ResourceManager resourceManager;

		public MyListener(ResourceManager resourceManager) {
			popupMenus = new PopupMenus(this);
			this.resourceManager = resourceManager;
		}

		public void tableChanged(TableModelEvent e) {
		}

		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == popupMenus.runMenuItem) {
				performRun();
			} else if (e.getSource() == popupMenus.editMenuItem) {
				performEdit();
			} else if (e.getSource() == popupMenus.deleteMenuItem) {
				performDelete();
			}
		}

		public void mouseReleased(MouseEvent e) {
		}

		public void mousePressed(MouseEvent e) {
			if (e.getButton() != MouseEvent.BUTTON1) {
				final int i = taskTable.rowAtPoint(e.getPoint());
				taskTable.clearSelection();
				taskTable.addRowSelectionInterval(i, i);
				popupMenus.popupMenu.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}

	public void actionPerformed(ActionEvent e) {
	}

	/**
	 * Note that the following columns are NOT show althought they are stored in
	 * the database bacause they can be seen in the tree view that was used to
	 * arrive at this table display.
	 */
	class TaskTableModel extends AbstractTableModel {
		private Vector tasks;

		private final Logger logger = Logger.getLogger(getClass().getName());

		public TaskTableModel() {
			super();
			tasks = new Vector(4, 2);
		}

		public void setTasks(Vector tasks) {
			this.tasks = tasks;
		}

		public int getRowCount() {
			return tasks.size();
		}

		public int getColumnCount() {
			// Display only the first columns. They others are "invisible".
			return 8;
		}

		static final int TASKID = 0;

		static final int PURPOSES = 1;

		static final int ACCESSORS = 2;

		static final int RECIPIENTS = 3;

		static final int COLUMNS = 4;

		static final int CONDITION = 5;

		static final int BEGIN = 6;

		static final int END = 7;

		static final int POLICY_NAME = 10;

		static final int VERSION = 11;

		static final int ENTITY = 12;

		public Task getTask(int row) {
			Task task;

			try {
				task = (Task) tasks.elementAt(row);
			} catch (ArrayIndexOutOfBoundsException e) {
				return null;
			}

			try {
				task = (Task) task.clone();
			} catch (CloneNotSupportedException e) {
				logger.error("Could not clone a task", e);
			}

			return task;
		}

		public Object getValueAt(int row, int column) {

			if (row < tasks.size()) {
				final Task task = getTask(row);

				if (column == POLICY_NAME)
					return task.policyName;
				else if (column == VERSION)
					return task.version;
				else if (column == TASKID)
					return task.name;
				else if (column == PURPOSES) {
					if (task.purposes.size() == 1
							&& (((String) task.purposes.get(0)).indexOf("%")) >= 0)
						return task.getDisplayableEntry((String) task.purposes
								.get(0));
					else
						return new VectorWrapper(task.purposes);
				} else if (column == RECIPIENTS) {
					if (task.recipients.size() == 1
							&& (((String) task.recipients.get(0)).indexOf("%")) >= 0)
						return task
								.getDisplayableEntry((String) task.recipients
										.get(0));
					else
						return new VectorWrapper(task.recipients);
				} else if (column == ACCESSORS) {
					if (task.accessors.size() == 1
							&& (((String) task.accessors.get(0)).indexOf("%")) >= 0)
						return task.getDisplayableEntry((String) task.accessors
								.get(0));
					else
						return new VectorWrapper(task.accessors);
				} else if (column == COLUMNS) {
					final ColumnDescriptorVectorWrapper columnDescriptors = new ColumnDescriptorVectorWrapper(
							task.columns);
					return columnDescriptors;
				} else if (column == CONDITION) {
					/*
					 * amol pujari 09/10/2006 added to show the only relevant
					 * part of condition to the user
					 */
					if (task.condition.length() > 0) {
						String str = "";

						try {
							str = task.condition.substring(task.condition
									.indexOf("(") + 1);
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

					return task.condition;
				} else if (column == BEGIN)
					return task.begin;
				else if (column == END)
					return task.end;
				// else if (column == ENTITY) return task.entities.elementAt(0);
			}
			return "";
		}

		public String getColumnName(int column) {
			if (column == POLICY_NAME)
				return ResourceManager.getResource("table.audit");
			else if (column == VERSION)
				return ResourceManager.getResource("table.version");
			else if (column == TASKID)
				return ResourceManager.getResource("table.task");
			else if (column == PURPOSES)
				return ResourceManager.getResource("table.purposes");
			else if (column == ACCESSORS)
				return ResourceManager.getResource("table.accessors");
			else if (column == RECIPIENTS)
				return ResourceManager.getResource("table.recipients");
			else if (column == COLUMNS)
				return ResourceManager.getResource("table.columns");
			else if (column == CONDITION)
				return ResourceManager.getResource("table.cond");
			else if (column == BEGIN)
				return ResourceManager.getResource("table.start.time");
			else if (column == END)
				return ResourceManager.getResource("table.end.time");
			else if (column == ENTITY)
				return ResourceManager.getResource("table.entity");
			else
				return "";
		}
	}

	public class PopupMenus {
		public JPopupMenu popupMenu;

		public JMenuItem runMenuItem;

		public JMenuItem editMenuItem;

		public JMenuItem deleteMenuItem;

		public PopupMenus(ActionListener listener) {
			popupMenu = new JPopupMenu();

			runMenuItem = new JMenuItem("Run");
			runMenuItem.addActionListener(listener);
			runMenuItem.setAccelerator(KeyStroke.getKeyStroke("alt R"));
			editMenuItem = new JMenuItem("Edit");
			editMenuItem.addActionListener(listener);
			editMenuItem.setAccelerator(KeyStroke.getKeyStroke("alt E"));
			deleteMenuItem = new JMenuItem("Delete");
			deleteMenuItem.setAccelerator(KeyStroke.getKeyStroke("DELETE"));
			deleteMenuItem.addActionListener(listener);

			popupMenu.add(runMenuItem);
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

	public Vector getQueryResults() {
		return queryResults;
	}

	public Vector getQueryVerdicts() {
		return queryVerdicts;
	}
}
