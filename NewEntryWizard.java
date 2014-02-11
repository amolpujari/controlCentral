package controlCenter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EtchedBorder;

public class NewEntryWizard extends NewWizard {

	public final static int TYPE_COLUMNS_IN_SCOPE = 100;

	public final static int TYPE_POLICY_RENAME = 200;

	public final static int TYPE_VERSION_RENAME = 300;

	public final static int TYPE_AUDIT_RENAME = 400;

	public final static int TYPE_NEW_VERSION = 500;

	private final String databaseName;

	private final ResourceManager resourceManager;

	private Version version;

	private NewWizardPanel panel;

	private final int type;

	private String table = "";

	private ImportRuleWizard importRuleWizard;

	private Vector importedRules = new Vector(4, 2);

	public NewEntryWizard(final int type, final String title,
			final String databaseName, final ResourceManager resourceManager,
			final Version version) {
		super(title, 1, resourceManager.controlCenter, true,
				(type != TYPE_COLUMNS_IN_SCOPE));
		this.databaseName = databaseName;
		this.type = type;
		this.resourceManager = resourceManager;
		this.version = version;

		final NewWizardPanel[] panels = new NewWizardPanel[1];
		String[] panelInfo = new String[1];

		if (type == TYPE_POLICY_RENAME) {
			panel = new NamePanel(this, NamePanel.NORMAL, ResourceManager
					.getResource("new.policy"), resourceManager
					.getExistingPolicies(databaseName), version.collectionName,
					"", Version.TYPE_COMPLEX);

			panelInfo[0] = " "
					+ ResourceManager.getResource("new.enter.new.name") + " ";// message

			((NamePanel) panel).setNameTextWidth(300);

		} else if (type == TYPE_AUDIT_RENAME) {
			panel = new NamePanel(this, NamePanel.NORMAL, ResourceManager
					.getResource("new.audit"), resourceManager
					.getExistingAudits(databaseName), version.collectionName,
					"", Version.TYPE_COMPLEX);

			panelInfo[0] = " "
					+ ResourceManager.getResource("new.enter.new.name") + " ";// message

			((NamePanel) panel).setNameTextWidth(300);
		} else if (type == TYPE_VERSION_RENAME) {
			panel = new NamePanel(this, NamePanel.NORMAL, ResourceManager
					.getResource("new.ver"), resourceManager.getVersions(
					databaseName, version.collectionName), version.versionName,
					"", Version.TYPE_COMPLEX);

			panelInfo[0] = " "
					+ ResourceManager.getResource("new.enter.new.name") + " ";// message

			((NamePanel) panel).setNameTextWidth(300);

		} else if (type == TYPE_NEW_VERSION) {

			panel = new NamePanel(this, NamePanel.NORMAL, ResourceManager
					.getResource("new.ver"), resourceManager.getVersions(
					databaseName, version.collectionName), "", "",
					Version.TYPE_COMPLEX);

			panelInfo[0] = " " + ResourceManager.getResource("new.enter.name")
					+ " ";// message

			final JPanel pan = new JPanel();
			pan.setBorder(BorderFactory
					.createEtchedBorder(EtchedBorder.LOWERED));
			pan.setBounds(2, 40, 394, 222);
			final JLabel label = new JLabel(ResourceManager
					.getResource("new.import.from.oldver"));
			label.setBounds(10, 8, 200, 20);
			final JButton importRulesButton = new JButton(ResourceManager
					.getResource("new.import"));
			importRulesButton.setBounds(316, 8, 70, 23);
			pan.setLayout(null);
			pan.add(label);
			pan.add(importRulesButton);
			panel.add(pan);
			final Vector otherVersions = resourceManager
					.getPolicyVersions(version);
			final Vector allRules = resourceManager.getPolicyRules(version);
			final JList list = new JList();
			final JScrollPane rightScrollPane = new JScrollPane(list,
					ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			list.setBorder(BorderFactory.createLoweredBevelBorder());
			rightScrollPane.setBounds(4, 36, 386, 180);
			pan.add(rightScrollPane);
			rightScrollPane.setVisible(false);

			importRulesButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {

					importRuleWizard = new ImportRuleWizard(ResourceManager
							.getResource("new.import.rules"),
							resourceManager.controlCenter, version,
							importedRules, otherVersions, allRules);

					importedRules = importRuleWizard.getSelectedRules();
					list.removeAll();
					list.setListData(importedRules);
					rightScrollPane
							.setVisible((importedRules.size() > 0) ? true
									: false);
				}
			});

			((NamePanel) panel).setNameTextWidth(300);

		} else if (type == TYPE_COLUMNS_IN_SCOPE) {
			panel = new ColumnsPanel(this, ColumnsPanel.TYPE_NONE, null,
					resourceManager.getColumnsInScope(databaseName,
							version.collectionName), resourceManager
							.listRelevantSchemas(databaseName), resourceManager
							.getDatabaseTablesInfo(databaseName, false),
					resourceManager, databaseName);

			panelInfo[0] = " "
					+ ResourceManager.getResource("new.select.column.scope")
					+ " ";// message
		} else if (type == ChoosePanel.BACKLOG
				|| type == ChoosePanel.ADD_ENTITIES) {

			panel = new ChoosePanel(this, ChoosePanel.AUDIT,// no mater which is
					// used here
					type, (type == ChoosePanel.BACKLOG) ? resourceManager
							.getTablesWithBacklog(databaseName) : new Vector(),
					databaseName, resourceManager);

			final Vector columnsInScope = resourceManager.getColumnsInScope(
					databaseName, "");
			final Hashtable tablesWithColumnsInScope = resourceManager
					.getDatabaseTablesFiltered(databaseName, columnsInScope);
			final Vector candidateBacklog = new Vector(4, 2);

			final Enumeration e = tablesWithColumnsInScope.keys();
			while (e.hasMoreElements())
				candidateBacklog.add(e.nextElement());

			((ChoosePanel) panel).setColoredData(candidateBacklog);

			panelInfo[0] = " "
					+ ResourceManager.getResource("new.select.tables") + " ";// message
		} else {
			String item = "";

			if (type == NamePanel.PURPOSE) {
				item = ResourceManager.getResource("new.pur");
				panelInfo[0] = " "
						+ ResourceManager.getResource("new.select.pur") + " ";// message
				table = ResourceManager.getResource("metadata.table.purposes");
			} else if (type == NamePanel.RECIPIENT) {
				item = ResourceManager.getResource("new.rec");
				panelInfo[0] = " "
						+ ResourceManager.getResource("new.select.rec") + " ";// message
				table = ResourceManager
						.getResource("metadata.table.recipients");
			} else if (type == NamePanel.ACCESSOR) {
				item = ResourceManager.getResource("new.acc");
				panelInfo[0] = " "
						+ ResourceManager.getResource("new.select.acc") + " ";// message
				table = ResourceManager.getResource("metadata.table.accessors");
			}

			panel = new NamePanel(this, type, item, resourceManager.getEntries(
					databaseName, table), "", "", Version.TYPE_COMPLEX);

			((NamePanel) panel).setNameTextWidth(300);

		}

		panels[0] = panel;
		set(panelInfo, panels);
	}

	public void finished() {
		String msg = "";

		if (type == TYPE_POLICY_RENAME) {
			msg = resourceManager.renamePolicy(version, ((NamePanel) panel)
					.getName());
		} else if (type == TYPE_AUDIT_RENAME) {
			msg = resourceManager.renameAudit(version, ((NamePanel) panel)
					.getName());
		} else if (type == TYPE_VERSION_RENAME) {
			msg = resourceManager.renamePolicyVersion(version,
					((NamePanel) panel).getName());
		} else if (type == TYPE_NEW_VERSION) {
			version.versionName = null;
			version = resourceManager.getPolicyVersionType(version);
			version.versionName = ((NamePanel) panel).getName();
			version.enabled = (version.type == Version.TYPE_SIMPLE) ? 0 : 1;
			msg = resourceManager.addPolicy(version);

			if (importedRules.size() > 0) {
				new Thread(new Runnable() {
					public void run() {
						final String msg = resourceManager.importPolicyRules(
								version, importedRules);
						MessageBox.stopBusy();
						MessageBox.show(resourceManager.controlCenter,
								ResourceManager.getResource("label.message"),
								msg, MessageBox.ICON_INFO);

					}
				}).start();
				MessageBox.showBusy(resourceManager.controlCenter,
						ResourceManager.getResource("new.importing.rules"));
			}

		} else if (type == TYPE_COLUMNS_IN_SCOPE) {
			msg = resourceManager.updateScope(version, ((ColumnsPanel) panel)
					.getSelectedColumns());

			final Vector columnsInScope = resourceManager.getColumnsInScope(
					databaseName, "");
			final Hashtable tablesWithColumnsInScope = resourceManager
					.getDatabaseTablesFiltered(databaseName, columnsInScope);

			final Enumeration e = tablesWithColumnsInScope.keys();

			// jk 01/02/2007
			// don't automatically create backlogs with scope since
			// customers may not be using the auditing feature
			//
//  			while (e.hasMoreElements()) {
//  				final String table = (String) e.nextElement();
//  				final String schemaName = table
//  						.substring(0, table.indexOf("."));
//  				final String tableName = table.substring(
//  						table.indexOf(".") + 1, table.length());
//  				if (resourceManager.getBacklogTableCount(new TableDescriptor(
//  						databaseName, schemaName, tableName)) == 0) {
//  					MessageBox.show(resourceManager.controlCenter,
//  							ResourceManager.getResource("label.message"),
//  							ResourceManager.getResource("new.msg.1"),
//  							MessageBox.ICON_INFO);

//  					new NewEntryWizard(ChoosePanel.BACKLOG, ResourceManager
//  							.getResource("new.define.backlog"),
//  							version.databaseName, resourceManager, null);
//  					break;
//  				}
//  			}

		} else if (type == ChoosePanel.ADD_ENTITIES)
			msg = resourceManager.createAssociationAndChoiceTables(
					databaseName, ((ChoosePanel) panel).getSelection());
		
		else if (type == ChoosePanel.BACKLOG) {
			new Thread(new Runnable() {
				public void run() {
					final String msg = resourceManager.createBacklogs(
							databaseName, ((ChoosePanel) panel).getSelection(),
							((ChoosePanel) panel).getNonSelection());
					MessageBox.stopBusy();
					MessageBox.show(resourceManager.controlCenter,
							ResourceManager.getResource("label.message"), msg,
							MessageBox.ICON_INFO);
				}
			}).start();
			MessageBox.showBusy(resourceManager.controlCenter, ResourceManager
					.getResource("new.creating.backlog"));

		} else {
			if (!resourceManager.addEntry(databaseName, table,
					((NamePanel) panel).getName()))
				msg = ResourceManager.getResource("new.msg.2") + " "
						+ ((NamePanel) panel).getName() + " "
						+ ResourceManager.getResource("new.msg.3");
		}

		MessageBox.show(resourceManager.controlCenter, ResourceManager
				.getResource("label.message"), msg, MessageBox.ICON_INFO);
	}
}
