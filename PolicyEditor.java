package controlCenter;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

public class PolicyEditor extends ControlCenterComponent {

	private ControlCenter controlCenter;

	private JMenuBar menubar;

	private PolicyTree policyTree;

	private JPanel blankPanel;

	private LoginCredentialsPanel loginCredentialsPanel;

	private RuleDisplayPanel ruleDisplayPanel;

	private ApplicationDisplayPanel applicationDisplayPanel;

	private EntityDisplayPanel entitiesDisplayPanel;

	private PurposeDisplayPanel purposesDisplayPanel;

	private RecipientDisplayPanel recipientsDisplayPanel;

	private AccessorDisplayPanel accessorsDisplayPanel;

	private ColumnInScopeDisplayPanel columnInScopeDisplayPanel;

	static final int BLANK_PANEL = 1;

	static final int DB_CONNECTION_PANEL = 2;

	static final int RULE_DISPLAY_PANEL = 3;

	static final int APPLICATION_DISPLAY_PANEL = 4;

	static final int ENTITIES_DISPLAY_PANEL = 5;

	static final int PURPOSES_DISPLAY_PANEL = 6;

	static final int RECIPIENTS_DISPLAY_PANEL = 7;

	static final int ACCESSORS_DISPLAY_PANEL = 8;

	static final int COLUMNS_IN_SCOPE_DISPLAY_PANEL = 9;

	private JComponent currentComponent;

	private JSplitPane leftRightSplitPane;

	private JSplitPane rightSplitPane;

	private JPanel rightTopBackPane;

	public InfoPanel infoPanel = new InfoPanel(InfoPanel.TYPE_POLICY_RULE);

	public void setDivider() {
		leftRightSplitPane.setDividerLocation(leftRightSplitPane
				.getDividerLocation() - 1);
		leftRightSplitPane.setDividerLocation(leftRightSplitPane
				.getDividerLocation() + 1);
	}

	public PolicyEditor(ControlCenter controlCenter) {
		super(controlCenter);
		this.controlCenter = controlCenter;
		this.menubar = null;

		policyTree = new PolicyTree(controlCenter, this);
		blankPanel = new JPanel();
		PolicyEditorMenuListener listener = new PolicyEditorMenuListener();
		loginCredentialsPanel = new LoginCredentialsPanel(listener);
		ruleDisplayPanel = new RuleDisplayPanel(controlCenter);
		applicationDisplayPanel = new ApplicationDisplayPanel(controlCenter);
		entitiesDisplayPanel = new EntityDisplayPanel(controlCenter);
		purposesDisplayPanel = new PurposeDisplayPanel(controlCenter);
		recipientsDisplayPanel = new RecipientDisplayPanel(controlCenter);
		accessorsDisplayPanel = new AccessorDisplayPanel(controlCenter);
		columnInScopeDisplayPanel = new ColumnInScopeDisplayPanel(controlCenter);

		leftRightSplitPane = new JSplitPane();
		leftRightSplitPane.setContinuousLayout(true);
		leftRightSplitPane.setDividerSize(4);
		leftRightSplitPane.setDividerLocation(200);

		rightSplitPane = new JSplitPane();
		rightSplitPane.setContinuousLayout(true);
		rightSplitPane.setDividerSize(4);
		rightSplitPane.setDividerLocation(200);
		rightSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		rightTopBackPane = new JPanel();
		rightTopBackPane.setLayout(new BorderLayout());
		rightSplitPane.setTopComponent(rightTopBackPane);
		rightSplitPane.setBottomComponent(infoPanel);
		leftRightSplitPane.setRightComponent(rightSplitPane);

		leftRightSplitPane.setLeftComponent(policyTree);
		setLayout(new BorderLayout());
		add(leftRightSplitPane, BorderLayout.CENTER);
		changeRightComponent(BLANK_PANEL);

	}

	public void changeRightComponent(int nextComponent, Object userObject) {

		final int dl = leftRightSplitPane.getDividerLocation();

		infoPanel.reset();

		if (currentComponent != null)
			rightTopBackPane.remove(currentComponent);

		if (nextComponent == BLANK_PANEL)
			currentComponent = blankPanel;
		else if (nextComponent == DB_CONNECTION_PANEL) {
			loginCredentialsPanel.reset();
			currentComponent = loginCredentialsPanel;
			rightSplitPane.setDividerLocation(300);
		} else if (nextComponent == RULE_DISPLAY_PANEL) {
			ruleDisplayPanel.showPanel((Vector) userObject);
			currentComponent = ruleDisplayPanel;
		} else if (nextComponent == APPLICATION_DISPLAY_PANEL) {
			applicationDisplayPanel.showPanel((Vector) userObject);
			currentComponent = applicationDisplayPanel;
		} else if (nextComponent == ENTITIES_DISPLAY_PANEL) {
			entitiesDisplayPanel.showPanel((Vector) userObject);
			currentComponent = entitiesDisplayPanel;
		} else if (nextComponent == PURPOSES_DISPLAY_PANEL) {
			purposesDisplayPanel.showPanel((Vector) userObject);
			currentComponent = purposesDisplayPanel;
		} else if (nextComponent == RECIPIENTS_DISPLAY_PANEL) {
			recipientsDisplayPanel.showPanel((Vector) userObject);
			currentComponent = recipientsDisplayPanel;
		} else if (nextComponent == ACCESSORS_DISPLAY_PANEL) {
			accessorsDisplayPanel.showPanel((Vector) userObject);
			currentComponent = accessorsDisplayPanel;
		} else if (nextComponent == COLUMNS_IN_SCOPE_DISPLAY_PANEL) {
			columnInScopeDisplayPanel.showPanel((Vector) userObject);
			currentComponent = columnInScopeDisplayPanel;
		}

		rightTopBackPane.add(currentComponent);
		// infoPanel.removeAll();
		setDivider();

		leftRightSplitPane.setDividerLocation(dl);
	}

	public PolicyTree getPolicyTree() {
		return policyTree;
	}

	public JMenuBar getMenuBar() {
		if (menubar == null) {
			menubar = new JMenuBar();

			JMenu fileMenu = getFileMenu();
			JMenu toolsMenu = getToolsMenu();
			JMenu helpMenu = getHelpMenu();

			menubar.add(fileMenu);
			menubar.add(toolsMenu);
			// menubar.add(Box.createHorizontalGlue());
			menubar.add(helpMenu);

			auditorMenuItem.setEnabled(true);
			policyEditorMenuItem.setEnabled(false);
		}

		return menubar;
	}

	// intended to be called for panels that do not require showPanel to be
	// called
	public void changeRightComponent(int nextComponent) {
		changeRightComponent(nextComponent, null);
	}

	class PolicyEditorMenuListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == loginCredentialsPanel.loginButton) {

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
				MessageBox.showBusy(controlCenter, ResourceManager
						.getResource("label.connecting.to")
						+ " \""
						+ loginCredentialsPanel.databaseField.getText()
								.toUpperCase() + "\"");

			}
		}
	}

}
