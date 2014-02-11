package controlCenter;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class BacklogUpdateSelectionDialog extends JDialog {
	private ResourceManager resourceManager;

	private String databaseName;

	private JList list;

	private JScrollPane textPane;

	private JScrollPane listPane;

	private final JButton okButton;

	private final JButton cancelButton;

	private final Box buttonGroupBox;

	public BacklogUpdateSelectionDialog(ResourceManager resourceManager,
			String databaseName) {
		super(resourceManager.controlCenter, true);
		this.resourceManager = resourceManager;
		this.databaseName = databaseName;

		this.setTitle(ResourceManager
				.getResource("bus.backlog.update.selection"));

		// this.addWindowListener(new MyWindowAdapter(this));
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		final ActionAdapter actionAdapter = new ActionAdapter(this);

		// final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		// setLocation(screen.width / 5, screen.height / 5);
		// final Color backgroundColor = (new JPanel()).getBackground();

		final JTextArea textArea = new JTextArea(3, 1);
		textArea.setEditable(false);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setFont(ResourceManager.NORMAL_FONT);
		textArea.setBackground(ResourceManager.BACKGROUND_COLOR);
		textPane = new JScrollPane(textArea);
		textPane
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		textPane.setPreferredSize(new Dimension(
				ResourceManager.SCROLL_PANEL_DIMENSION_X, 50));
		textArea.setText(ResourceManager.getResource("bus.stmt"));
		textPane.setBorder(null);

		list = new JList();
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.addListSelectionListener(actionAdapter);
		listPane = new JScrollPane(list);
		listPane.setPreferredSize(new Dimension(
				ResourceManager.SCROLL_PANEL_DIMENSION_X,
				ResourceManager.SCROLL_PANEL_DIMENSION_Y / 2));
		final TitledBorder border = new TitledBorder(ResourceManager.BORDER,
				ResourceManager.getResource("label.tables"));
		border.setTitleFont(ResourceManager.BOLD_FONT);
		listPane.setBorder(border);

		okButton = new JButton();
		okButton.setText(ResourceManager.getResource("label.ok"));
		okButton.addActionListener(actionAdapter);
		cancelButton = new JButton();
		cancelButton.setText(ResourceManager.getResource("label.cancel"));
		cancelButton.addActionListener(actionAdapter);

		final ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(okButton);
		buttonGroup.add(cancelButton);
		buttonGroupBox = Box.createHorizontalBox();
		buttonGroupBox.add(okButton);
		buttonGroupBox.add(cancelButton);

		setModal(true);
		setLocationRelativeTo(null);
		showPanel();
	}

	public void showPanel() {
		final Vector tables = resourceManager
				.getBacklogTablesWithSchemaChange(databaseName);
		list.setListData(tables);

		this.getContentPane().removeAll();

		//
		// Root panel
		//
		GridBagLayout gridBagLayout = new GridBagLayout();
		this.getContentPane().setLayout(gridBagLayout);
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(ResourceManager.INSETS_PADDING,
				ResourceManager.INSETS_PADDING, ResourceManager.INSETS_PADDING,
				ResourceManager.INSETS_PADDING);

		c.weightx = 1.0;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		gridBagLayout.setConstraints(textPane, c);
		this.getContentPane().add(textPane);

		c.weightx = 1.0;
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		gridBagLayout.setConstraints(listPane, c);
		this.getContentPane().add(listPane);

		c.anchor = GridBagConstraints.CENTER;
		c.weightx = 1.0;
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 1;
		gridBagLayout.setConstraints(buttonGroupBox, c);
		this.getContentPane().add(buttonGroupBox);

		checkInput();

		pack();
		setResizable(false);
		// setLocation(getParent().getLocation());
	}

	// public void thisWindowClosed(WindowEvent e) {
	// };
	//
	// class MyWindowAdapter extends WindowAdapter {
	// private BacklogUpdateSelectionDialog adaptee;
	//
	// MyWindowAdapter(BacklogUpdateSelectionDialog adaptee) {
	// this.adaptee = adaptee;
	// }
	//
	// public void windowClosed(WindowEvent e) {
	// adaptee.thisWindowClosed(e);
	// }
	// }

	class ActionAdapter implements ActionListener, ListSelectionListener {
		final BacklogUpdateSelectionDialog dialog;

		public ActionAdapter(BacklogUpdateSelectionDialog dialog) {
			this.dialog = dialog;
		}

		public void valueChanged(ListSelectionEvent e) {
			checkInput();
		}

		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == okButton) {
			    boolean res = true;
				final TableDescriptor table = (TableDescriptor) list
						.getSelectedValue();
				TableDescriptor backlog=null;
				Vector backlogs = resourceManager
				    .getBacklogsOfTable(table);

				int siz = backlogs.size();
				if (siz > 0) {
				    backlog = (TableDescriptor) backlogs.get(0);
				    
				    // check for any dropped columns
				    //
				    Vector tcols = resourceManager.getDatabaseColumns(table, false);
				    Vector bcols = resourceManager.getDatabaseColumns(backlog, false);
				    int i1=0;
				    int i2=0;
				    int tsize = tcols.size();
				    int bsize = bcols.size();
				    while (i2 < bsize && res) {
					ColumnDescriptor tcol = null;
					if (i1 < tsize)
					    tcol = (ColumnDescriptor) tcols.get(i1);
					ColumnDescriptor bcol = (ColumnDescriptor) bcols.get(i2);
					if (bcol.columnName.equals("OPR")) i2++;
					else if (bcol.columnName.equals("TIM")) i2++;
					else if (bcol.columnName.equals("USR")) i2++;
					else if (bcol.columnName.equals("PKEY")) i2++;
					else {
					    int cmp = -1;
					    if (tcol != null)
						cmp = bcol.columnName.compareTo(tcol.columnName);
					    if (cmp == 0) {
						i1++;
						i2++;
					    }
					    else if (cmp < 0) {
						
						// dropped column
						//
						res = resourceManager.addDroppedColumn(table, bcol);
						
						i2++;
					    }
					    else {
						i1++;
					    }
					}
				    }
				    
				    //
				    // Start Clio for schema reconciliation.
				    //
				    // ATTENTION: Use uppercase names for table and schema names!
				    //
				    // final DBCon connection =
				    // resourceManager.getConnection(databaseName);
				    //
				    if (res==false) {
					MessageBox.show(resourceManager.controlCenter, resourceManager
							.getResource("label.error"), resourceManager
							.getResource("label.see.err"),
							MessageBox.ICON_ERR);

				    }
				    else {
					final String relativeOutputPath = ResourceManager
					    .getResource("clio.data.directory");
					
					final String sourceSchemaName = backlog.schemaName;
					final Vector sourceTableNames = new Vector(4, 2);
					for (int i=0;i<siz;i++) {
					    backlog = (TableDescriptor) backlogs.get(i);
					    sourceTableNames.add(backlog.tableName);
					}
					
					//  				final String targetSchemaName = table.schemaName.toUpperCase();
					//  				final Vector targetTableNames = new Vector(4, 2);
					//  				targetTableNames.add(table.tableName.toUpperCase());
					
		TableDescriptor newBacklog = resourceManager.createBacklogTableFirst(table, false);
		if (newBacklog != null) {
		    sourceTableNames.add(newBacklog.tableName);
		    final BacklogDefinitionDialog backlogDefinitionDialog = new BacklogDefinitionDialog(
													resourceManager, table, newBacklog, relativeOutputPath,
													sourceSchemaName, sourceTableNames);
		    //  						, targetSchemaName,
		    //  						targetTableNames);
		    
		    backlogDefinitionDialog.setLocationRelativeTo(null);
		    backlogDefinitionDialog.show();
		}
				    }
				}
				hide();
			} else if (e.getSource() == cancelButton) {
				setVisible(false);
			}
			checkInput();
		}
	}

	private void checkInput() {
		boolean selected = !list.isSelectionEmpty();
		okButton.setEnabled(selected);
	}
}
