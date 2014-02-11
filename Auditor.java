package controlCenter;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

public class Auditor extends ControlCenterComponent {

	private JMenuBar menubar;

	private AuditTree auditTree;

	private JPanel blankPanel;

	private LoginCredentialsPanel loginCredentialsPanel;

	private TaskDisplayPanel taskDisplayPanel;

	private PurposeDisplayPanel purposesDisplayPanel;

	private RecipientDisplayPanel recipientsDisplayPanel;

	private AccessorDisplayPanel accessorsDisplayPanel;

	private BacklogDisplayPanel backlogDisplayPanel;

	static final int BLANK_PANEL = 1;

	static final int DATABASE_CONNECTION_PANEL = 2;

	static final int TASK_DISPLAY_PANEL = 3;

	static final int PURPOSES_DISPLAY_PANEL = 6;

	static final int RECIPIENTS_DISPLAY_PANEL = 7;

	static final int ACCESSORS_DISPLAY_PANEL = 8;

	static final int BACKLOGS_DISPLAY_PANEL = 9;

	private JComponent currentComponent;

	private JSplitPane leftRightSplitPane;

	private JSplitPane rightSplitPane;

	private JPanel rightTopBackPane;

	public InfoPanel taskInfoPanel = new InfoPanel(InfoPanel.TYPE_AUDIT_TASK);

	private ControlCenter controlCenter;

	public void setDivider() {
		leftRightSplitPane.setDividerLocation(leftRightSplitPane
				.getDividerLocation() - 1);
		leftRightSplitPane.setDividerLocation(leftRightSplitPane
				.getDividerLocation() + 1);
	}

	public Auditor(ControlCenter controlCenter) {
		super(controlCenter);
		this.controlCenter = controlCenter;
		this.menubar = null;

		auditTree = new AuditTree(controlCenter, this);
		blankPanel = new JPanel();
		AuditorMenuListener listener = new AuditorMenuListener();
		loginCredentialsPanel = new LoginCredentialsPanel(listener);
		taskDisplayPanel = new TaskDisplayPanel(controlCenter);
		purposesDisplayPanel = new PurposeDisplayPanel(controlCenter);
		recipientsDisplayPanel = new RecipientDisplayPanel(controlCenter);
		accessorsDisplayPanel = new AccessorDisplayPanel(controlCenter);
		backlogDisplayPanel = new BacklogDisplayPanel(controlCenter);

		leftRightSplitPane = new JSplitPane();
		leftRightSplitPane.setContinuousLayout(true);
		leftRightSplitPane.setDividerSize(4);
		leftRightSplitPane.setDividerLocation(200);
		leftRightSplitPane.setDividerLocation(200);

		rightSplitPane = new JSplitPane();
		rightSplitPane.setContinuousLayout(true);
		rightSplitPane.setDividerSize(4);
		rightSplitPane.setDividerLocation(200);
		rightSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);

		rightTopBackPane = new JPanel();
		rightTopBackPane.setLayout(new BorderLayout());
		rightSplitPane.setTopComponent(rightTopBackPane);

		rightSplitPane.setBorder(null);

		rightSplitPane.setBottomComponent(taskInfoPanel);

		leftRightSplitPane.setRightComponent(rightSplitPane);

		leftRightSplitPane.setLeftComponent(auditTree);
		setLayout(new BorderLayout());
		add(leftRightSplitPane, BorderLayout.CENTER);
		changeRightComponent(BLANK_PANEL);
	}

	public void changeRightComponent(int nextComponent, Version version) {
		final int dl = leftRightSplitPane.getDividerLocation();
		final Vector userObject = new Vector(1, 1);
		if (version != null)
			userObject.add(version.databaseName);

		taskInfoPanel.reset();

		if (currentComponent != null) 
			rightTopBackPane.remove(currentComponent);

		if (nextComponent == BLANK_PANEL) {
			currentComponent = blankPanel;
		} else if (nextComponent == DATABASE_CONNECTION_PANEL) {
			loginCredentialsPanel.reset();
			currentComponent = loginCredentialsPanel;
			rightSplitPane.setDividerLocation(300);
		} else if (nextComponent == TASK_DISPLAY_PANEL) {
			taskDisplayPanel.showPanel(version);
			currentComponent = taskDisplayPanel;
			final int location = rightSplitPane.getDividerLocation();
			rightSplitPane.setBottomComponent(taskInfoPanel);
			rightSplitPane.setDividerLocation(location);

		} else if (nextComponent == PURPOSES_DISPLAY_PANEL) {
			purposesDisplayPanel.showPanel((Vector) userObject);
			currentComponent = purposesDisplayPanel;
		} else if (nextComponent == RECIPIENTS_DISPLAY_PANEL) {
			recipientsDisplayPanel.showPanel((Vector) userObject);
			currentComponent = recipientsDisplayPanel;
		} else if (nextComponent == ACCESSORS_DISPLAY_PANEL) {
			accessorsDisplayPanel.showPanel((Vector) userObject);
			currentComponent = accessorsDisplayPanel;
		} else if (nextComponent == BACKLOGS_DISPLAY_PANEL) {
			backlogDisplayPanel.showPanel((Vector) userObject);
			currentComponent = backlogDisplayPanel;
		}

		rightTopBackPane.add(currentComponent);
		setDivider();

		leftRightSplitPane.setDividerLocation(dl);
	}

	public void checkBacklogSchemaChange() {
		final Enumeration databaseNames = controlCenter.resourceManager
				.getConnectedDatabaseNames();

		final Vector staleBacklogDatabases = new Vector(4, 2);

		while (databaseNames.hasMoreElements()) {
			final String databaseName = (String) databaseNames.nextElement();
			if (controlCenter.resourceManager
					.hasBacklogTableSchemaChange(databaseName)) {
				staleBacklogDatabases.add(databaseName);
			}
		}
	}

	public JMenuBar getMenuBar() {
		if (menubar == null) {
			menubar = new JMenuBar();

			JMenu fileMenu = getFileMenu();
			JMenu toolsMenu = getToolsMenu();
			JMenu helpMenu = getHelpMenu();

			menubar.add(fileMenu);
			menubar.add(toolsMenu);
			menubar.add(helpMenu);

			auditorMenuItem.setEnabled(false);
			policyEditorMenuItem.setEnabled(true);
		}

		return menubar;
	}

	public AuditTree getAuditTree() {
		return auditTree;
	}

	// intended to be called for panels that do not require showPanel to be
	// called
	public void changeRightComponent(int nextComponent) {
		changeRightComponent(nextComponent, null);
	}

	class AuditorMenuListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == loginCredentialsPanel.loginButton) {
				// validateLogin

				new Thread(new Runnable() {
					public void run() {
						final String msg = controlCenter.resourceManager
								.openConnection(
										loginCredentialsPanel.databaseField
												.getText().toUpperCase(),
										loginCredentialsPanel.usernameField
												.getText(),
										new String(
												loginCredentialsPanel.passwordField
														.getPassword()));

						MessageBox.stopBusy();
						if (msg.length() > 0)
							loginCredentialsPanel.loginStatusLabel
									.setText("<html>" + msg + "</html>");
						else
							changeRightComponent(BLANK_PANEL);
					}
				}).start();
				MessageBox.showBusy(controlCenter, ResourceManager.getResource("label.connecting.to")+" \""
						+ loginCredentialsPanel.databaseField.getText()
								.toUpperCase() + "\"");

			}
		}
	}

}
