package controlCenter;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.MenuElement;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import Utility;

public class PolicyTree extends Tree implements ResourceListener {
	private PolicyEditor policyEditor;

	private DefaultMutableTreeNode selectedNode;

	private TreePath selectionPath;

	private ApplicationUsageDialog applicationUsageDialog;

	public PolicyTree(ControlCenter controlCenter, PolicyEditor policyEditor) {
		this.controlCenter = controlCenter;
		this.policyEditor = policyEditor;
		controlCenter.resourceManager.addResourceListener(this);

		jTree = new JTree();
		// Create a tree that allows one selection at a time.
		jTree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		// Listen for when the selection changes.
		PolicyTreeListener listener = new PolicyTreeListener();
		jTree.addTreeSelectionListener(listener);
		jTree.addMouseListener(listener);

		treeModel = new DefaultTreeModel(construct());
		jTree.setModel(treeModel);
		setViewportView(jTree);

		jTree.setCellRenderer(controlCenter.resourceManager.getTreeRenderer());

		applicationUsageDialog = new ApplicationUsageDialog(controlCenter);

		final Dimension controlCenterSize = controlCenter.getSize();
		setPreferredSize(new Dimension(
				(int) (controlCenterSize.getWidth() / 3),
				(int) (controlCenterSize.getHeight())));

		jTree.setRowHeight(jTree.getRowHeight() + 8);
	}

	public ApplicationUsageDialog getApplicationUsageDialog() {
		return applicationUsageDialog;
	}

	public void auditChanged(Version version) {
		// Nothing to do.
	}

	public void policyChanged(Version version) {
		final DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) treeModel
				.getRoot();
		DefaultMutableTreeNode databaseNodeToUpdate = null;

		final int numDatabases = treeModel.getChildCount(rootNode);

		for (int i = 0; (i < numDatabases) && (databaseNodeToUpdate == null); i++) {
			final DefaultMutableTreeNode databaseNode = (DefaultMutableTreeNode) treeModel
					.getChild(rootNode, i);
			if (((String) databaseNode.getUserObject())
					.equalsIgnoreCase(version.databaseName))
				databaseNodeToUpdate = databaseNode;
		}
		saveExpansion();
		refreshDatabaseNameNode(databaseNodeToUpdate);
		applyExpansion();
	}

	public void refreshDatabaseNameNode(DefaultMutableTreeNode databaseNode) {

		// get rid of all children first
		databaseNode.removeAllChildren();
		treeModel.reload();

		final Hashtable policiesUnsorted = controlCenter.resourceManager
				.getPolicies((String) databaseNode.getUserObject());

		if (policiesUnsorted != null) {
			final TreeMap policies = new TreeMap(policiesUnsorted);
			final DefaultMutableTreeNode policyNameHolder = new DefaultMutableTreeNode(
					ResourceManager.getResource("tree.policies.label"));
			treeModel.insertNodeInto(policyNameHolder, databaseNode,
					databaseNode.getChildCount());

			final Set policyNames = policies.keySet();
			final Iterator policyIterator = policyNames.iterator();
			while (policyIterator.hasNext()) {
				final String policyName = (String) policyIterator.next();
				final DefaultMutableTreeNode policyNode = new DefaultMutableTreeNode(
						policyName);
				treeModel.insertNodeInto(policyNode, policyNameHolder,
						policyNameHolder.getChildCount());

				final DefaultMutableTreeNode columnsInScopeNode = new DefaultMutableTreeNode(
						ResourceManager.getResource("pt.scope"));
				treeModel.insertNodeInto(columnsInScopeNode, policyNode,
						policyNode.getChildCount());

				final DefaultMutableTreeNode versionNameHolder = new DefaultMutableTreeNode(
						ResourceManager.getResource("label.versions"));
				treeModel.insertNodeInto(versionNameHolder, policyNode,
						policyNode.getChildCount());

				final TreeSet versions = new TreeSet((Vector) policies
						.get(policyName));
				final Iterator versionIterator = versions.iterator();
				while (versionIterator.hasNext()) {
					final DefaultMutableTreeNode versionNode = new DefaultMutableTreeNode(
							(String) versionIterator.next());
					treeModel.insertNodeInto(versionNode, versionNameHolder,
							versionNameHolder.getChildCount());
				}
			}

		}

		if (controlCenter.resourceManager
				.hasMetadataTables((String) databaseNode.getUserObject())) {
			final DefaultMutableTreeNode applicationsNode = new DefaultMutableTreeNode(
					ResourceManager.getResource("pt.app"));
			treeModel.insertNodeInto(applicationsNode, databaseNode,
					databaseNode.getChildCount());

			final DefaultMutableTreeNode entitiesNode = new DefaultMutableTreeNode(
					ResourceManager.getResource("pt.ent"));
			treeModel.insertNodeInto(entitiesNode, databaseNode, databaseNode
					.getChildCount());

		}

		if (controlCenter.resourceManager
				.hasMetadataTables((String) databaseNode.getUserObject())
				|| controlCenter.resourceManager
						.hasBacklogMetaData((String) databaseNode
								.getUserObject())) {
			final DefaultMutableTreeNode purposesNode = new DefaultMutableTreeNode(
					ResourceManager.getResource("pt.pur"));
			treeModel.insertNodeInto(purposesNode, databaseNode, databaseNode
					.getChildCount());

			final DefaultMutableTreeNode recipientsNode = new DefaultMutableTreeNode(
					ResourceManager.getResource("pt.rec"));
			treeModel.insertNodeInto(recipientsNode, databaseNode, databaseNode
					.getChildCount());

			final DefaultMutableTreeNode accessorsNode = new DefaultMutableTreeNode(
					ResourceManager.getResource("pt.acc"));
			treeModel.insertNodeInto(accessorsNode, databaseNode, databaseNode
					.getChildCount());
		}

		jTree.scrollPathToVisible(new TreePath(databaseNode.getLastLeaf()
				.getPath()));
	}

	class PopupMenus {
		JPopupMenu databaseNodePopup;

		JPopupMenu databaseNamePopup;

		JPopupMenu policyPopup;

		JPopupMenu policyNamePopup;

		JPopupMenu versionsPopup;

		JPopupMenu versionPopup;

		JPopupMenu entityPopup;

		JPopupMenu defineScopePopup;

		JPopupMenu applicationPopup;

		JPopupMenu purposePopup;

		JPopupMenu recipientPopup;

		JPopupMenu accessorPopup;

		JMenuItem expandEntireTreeMenuItem;

		JMenuItem collapseEntireTreeMenuItem;

		JMenuItem createMetadataMenuItem;

		JMenuItem dropMetadataMenuItem;

		JMenuItem connectToDatabaseMenuItem;

		JMenuItem disconnectFromDatabaseMenuItem;

		JMenuItem defineEntitiesMenuItem;

		JMenuItem defineApplicationUsageMenuItem;

		JMenuItem addPolicyMenuItem;

		JMenuItem renamePolicyMenuItem;

		JMenuItem deleteAllPoliciesMenuItem;

		JMenuItem addVersionMenuItem;

		JMenuItem addVersionMenuItem2;

		JMenuItem deleteAllVersionsMenuItem;

		JMenuItem deletePolicyMenuItem;

		JMenuItem scopeMenuItem;

		JMenuItem deleteVersionMenuItem;

		JMenuItem addRuleMenuItem;

		JMenuItem deployVersionMenuItem;

		JMenuItem renameVersionMenuItem;

		JMenuItem activateVersionMenuItem;

		JMenuItem importRulesVersionMenuItem;

		JMenuItem scopeMenuItem2;

		JMenuItem deleteAllScope;

		JMenuItem defineEntitiesMenuItem2;

		JMenuItem deleteAllEntitiesMenuItem;

		JMenuItem defineApplicationUsageMenuItem2;

		JMenuItem deleteAllApplicationUsageMenuItem;

		JMenuItem definePurposeMenuItem;

		JMenuItem deleteAllPurposeMenuItem;

		JMenuItem defineRecipientMenuItem;

		JMenuItem deleteAllRecipientMenuItem;

		JMenuItem defineAccessorMenuItem;

		JMenuItem deleteAllAccessorMenuItem;

		public PopupMenus(ActionListener listener) {

			databaseNodePopup = new JPopupMenu();
			connectToDatabaseMenuItem = new JMenuItem(ResourceManager
					.getResource("at.connect.to.anothor.database"));
			connectToDatabaseMenuItem.addActionListener(listener);
			expandEntireTreeMenuItem = new JMenuItem(ResourceManager
					.getResource("at.expand.tree"));
			expandEntireTreeMenuItem.addActionListener(listener);
			collapseEntireTreeMenuItem = new JMenuItem(ResourceManager
					.getResource("at.collapse.tree"));
			collapseEntireTreeMenuItem.addActionListener(listener);
			databaseNodePopup.add(connectToDatabaseMenuItem);
			databaseNodePopup.addSeparator();
			databaseNodePopup.add(expandEntireTreeMenuItem);
			databaseNodePopup.add(collapseEntireTreeMenuItem);

			databaseNamePopup = new JPopupMenu();
			createMetadataMenuItem = new JMenuItem(ResourceManager
					.getResource("pt.install.metadata"));
			createMetadataMenuItem.addActionListener(listener);
			dropMetadataMenuItem = new JMenuItem(ResourceManager
					.getResource("pt.uninstall.metadata"));
			dropMetadataMenuItem.addActionListener(listener);
			defineEntitiesMenuItem = new JMenuItem(ResourceManager
					.getResource("pt.define.entities"));
			defineEntitiesMenuItem.addActionListener(listener);
			defineApplicationUsageMenuItem = new JMenuItem(ResourceManager
					.getResource("pt.define.application.usage"));
			defineApplicationUsageMenuItem.addActionListener(listener);
			disconnectFromDatabaseMenuItem = new JMenuItem(ResourceManager
					.getResource("at.disconnect"));
			disconnectFromDatabaseMenuItem.addActionListener(listener);
			databaseNamePopup.add(createMetadataMenuItem);
			databaseNamePopup.add(dropMetadataMenuItem);
			databaseNamePopup.addSeparator();
			databaseNamePopup.add(defineEntitiesMenuItem);
			databaseNamePopup.add(defineApplicationUsageMenuItem);
			databaseNamePopup.addSeparator();
			databaseNamePopup.add(disconnectFromDatabaseMenuItem);

			policyPopup = new JPopupMenu();
			addPolicyMenuItem = new JMenuItem(ResourceManager
					.getResource("pt.add.policy"));
			addPolicyMenuItem.addActionListener(listener);
			deleteAllPoliciesMenuItem = new JMenuItem(ResourceManager
					.getResource("pt.delete.all.policies"));
			deleteAllPoliciesMenuItem.addActionListener(listener);
			policyPopup.add(addPolicyMenuItem);
			policyPopup.add(deleteAllPoliciesMenuItem);

			policyNamePopup = new JPopupMenu();
			addVersionMenuItem = new JMenuItem(ResourceManager
					.getResource("pt.add.ver"));
			addVersionMenuItem.addActionListener(listener);
			deletePolicyMenuItem = new JMenuItem(ResourceManager
					.getResource("label.delete"));
			deletePolicyMenuItem.addActionListener(listener);
			renamePolicyMenuItem = new JMenuItem(ResourceManager
					.getResource("label.rename"));
			renamePolicyMenuItem.addActionListener(listener);
			scopeMenuItem = new JMenuItem(ResourceManager
					.getResource("pt.define.scope"));
			scopeMenuItem.addActionListener(listener);
			policyNamePopup.add(addVersionMenuItem);
			policyNamePopup.add(deletePolicyMenuItem);
			policyNamePopup.add(renamePolicyMenuItem);
			policyNamePopup.addSeparator();
			policyNamePopup.add(scopeMenuItem);

			versionsPopup = new JPopupMenu();
			addVersionMenuItem2 = new JMenuItem(ResourceManager
					.getResource("pt.add.ver"));
			addVersionMenuItem2.addActionListener(listener);
			versionsPopup.add(addVersionMenuItem2);

			versionPopup = new JPopupMenu();
			addRuleMenuItem = new JMenuItem(ResourceManager
					.getResource("pt.add.rule"));
			addRuleMenuItem.addActionListener(listener);
			deleteVersionMenuItem = new JMenuItem(ResourceManager
					.getResource("label.delete"));
			deleteVersionMenuItem.addActionListener(listener);
			renameVersionMenuItem = new JMenuItem(ResourceManager
					.getResource("label.rename"));
			renameVersionMenuItem.addActionListener(listener);
			activateVersionMenuItem = new JMenuItem(ResourceManager
					.getResource("pt.act"));
			activateVersionMenuItem.addActionListener(listener);
			importRulesVersionMenuItem = new JMenuItem(ResourceManager
					.getResource("pt.import"));
			importRulesVersionMenuItem.addActionListener(listener);

			versionPopup.add(addRuleMenuItem);
			versionPopup.add(deleteVersionMenuItem);
			versionPopup.add(renameVersionMenuItem);
			versionPopup.addSeparator();
			versionPopup.add(activateVersionMenuItem);
			versionPopup.addSeparator();
			versionPopup.add(importRulesVersionMenuItem);

			defineScopePopup = new JPopupMenu();
			scopeMenuItem2 = new JMenuItem(ResourceManager
					.getResource("pt.define.scope"));
			scopeMenuItem2.addActionListener(listener);
			deleteAllScope = new JMenuItem(ResourceManager
					.getResource("pt.del.scope"));
			deleteAllScope.addActionListener(listener);
			defineScopePopup.add(scopeMenuItem2);
			defineScopePopup.addSeparator();
			defineScopePopup.add(deleteAllScope);

			entityPopup = new JPopupMenu();
			defineEntitiesMenuItem2 = new JMenuItem(ResourceManager
					.getResource("pt.define.entities"));
			defineEntitiesMenuItem2.addActionListener(listener);
			deleteAllEntitiesMenuItem = new JMenuItem(ResourceManager
					.getResource("pt.del.all.ent"));
			deleteAllEntitiesMenuItem.addActionListener(listener);
			entityPopup.add(defineEntitiesMenuItem2);
			entityPopup.addSeparator();
			entityPopup.add(deleteAllEntitiesMenuItem);

			applicationPopup = new JPopupMenu();
			defineApplicationUsageMenuItem2 = new JMenuItem(ResourceManager
					.getResource("pt.define.application.usage"));
			defineApplicationUsageMenuItem2.addActionListener(listener);
			deleteAllApplicationUsageMenuItem = new JMenuItem(ResourceManager
					.getResource("pt.del.all.app"));
			deleteAllApplicationUsageMenuItem.addActionListener(listener);
			applicationPopup.add(defineApplicationUsageMenuItem2);
			applicationPopup.addSeparator();
			applicationPopup.add(deleteAllApplicationUsageMenuItem);

			purposePopup = new JPopupMenu();
			definePurposeMenuItem = new JMenuItem(ResourceManager
					.getResource("pt.def.pur"));
			definePurposeMenuItem.addActionListener(listener);
			deleteAllPurposeMenuItem = new JMenuItem(ResourceManager
					.getResource("pt.del.all.pur"));
			deleteAllPurposeMenuItem.addActionListener(listener);
			purposePopup.add(definePurposeMenuItem);
			purposePopup.addSeparator();
			purposePopup.add(deleteAllPurposeMenuItem);

			recipientPopup = new JPopupMenu();
			defineRecipientMenuItem = new JMenuItem(ResourceManager
					.getResource("pt.def.rec"));
			defineRecipientMenuItem.addActionListener(listener);
			deleteAllRecipientMenuItem = new JMenuItem(ResourceManager
					.getResource("pt.del.all.rec"));
			deleteAllRecipientMenuItem.addActionListener(listener);
			recipientPopup.add(defineRecipientMenuItem);
			recipientPopup.addSeparator();
			recipientPopup.add(deleteAllRecipientMenuItem);

			accessorPopup = new JPopupMenu();
			defineAccessorMenuItem = new JMenuItem(ResourceManager
					.getResource("pt.def.acc"));
			defineAccessorMenuItem.addActionListener(listener);
			deleteAllAccessorMenuItem = new JMenuItem(ResourceManager
					.getResource("pt.del.all.acc"));
			deleteAllAccessorMenuItem.addActionListener(listener);
			accessorPopup.add(defineAccessorMenuItem);
			accessorPopup.addSeparator();
			accessorPopup.add(deleteAllAccessorMenuItem);
		}
	}

	class PolicyTreeListener extends MouseAdapter implements ActionListener,
			TreeSelectionListener {
		private PopupMenus popupMenus;

		public PolicyTreeListener() {
			popupMenus = new PopupMenus(this);
		}

		private String getDatabaseName(DefaultMutableTreeNode seleectedNode) {
			TreeNode[] tn = seleectedNode.getPath();
			return tn[1].toString();
		}

		public void actionPerformed(ActionEvent e) {

			if (e.getSource() == popupMenus.connectToDatabaseMenuItem)
				policyEditor
						.changeRightComponent(PolicyEditor.DB_CONNECTION_PANEL);

			else if (e.getSource() == popupMenus.expandEntireTreeMenuItem)
				Utility.expandTree(jTree);

			else if (e.getSource() == popupMenus.collapseEntireTreeMenuItem)
				Utility.collapseTree(jTree);

			else if (e.getSource() == popupMenus.createMetadataMenuItem) {

				new Thread(new Runnable() {
					public void run() {
						final String databaseName = (String) selectedNode
								.getUserObject();
						if (controlCenter.resourceManager
								.createMetadata(databaseName)) {

							MessageBox.stopBusy();
							MessageBox.show(controlCenter, ResourceManager
									.getResource("label.success"),
									ResourceManager
											.getResource("pt.install.success"),
									MessageBox.ICON_SUC);

							new NewEntryWizard(ChoosePanel.ADD_ENTITIES,
									ResourceManager
											.getResource("pt.plz.def.ent"),
									databaseName,
									controlCenter.resourceManager, null);
						} else {
							MessageBox.stopBusy();
							MessageBox.show(controlCenter, ResourceManager
									.getResource("label.error"),
									ResourceManager
											.getResource("pt.install.fail"),
									MessageBox.ICON_ERR);
						}
						saveExpansion();
						refreshDatabaseNameNode(selectedNode);
						applyExpansion();

						controlCenter.auditor.getAuditTree().refresh();
						controlCenter.auditor
								.changeRightComponent(Auditor.BLANK_PANEL);
					}
				}).start();
				MessageBox.showBusy(controlCenter, ResourceManager
						.getResource("pt.creating.meta"));

			} else if (e.getSource() == popupMenus.dropMetadataMenuItem) {
				if (MessageBox
						.result(controlCenter, ResourceManager
								.getResource("label.confirm"), ResourceManager
								.getResource("pt.sure.drop.meta"),
								MessageBox.ICON_WARN) == MessageBox.BUTTON_YES) {
					new Thread(new Runnable() {
						public void run() {
							final String databaseName = (String) selectedNode
									.getUserObject();
							if (controlCenter.resourceManager
									.dropMetadata(databaseName)) {
								MessageBox.stopBusy();
							} else {
								MessageBox.stopBusy();
								MessageBox
										.show(
												controlCenter,
												ResourceManager
														.getResource("label.error"),
												ResourceManager
														.getResource("pt.cannot.drop.meta"),
												MessageBox.ICON_ERR);
							}
							// while (selectedNode.getChildCount() > 0) {
							DefaultMutableTreeNode policyNameNode = (DefaultMutableTreeNode) treeModel
									.getChild(selectedNode, 0);
							treeModel.removeNodeFromParent(policyNameNode);
							// }

							policyNameNode = (DefaultMutableTreeNode) treeModel
									.getChild(selectedNode, 0);
							treeModel.removeNodeFromParent(policyNameNode);

							policyNameNode = (DefaultMutableTreeNode) treeModel
									.getChild(selectedNode, 0);
							treeModel.removeNodeFromParent(policyNameNode);

							if (!controlCenter.resourceManager
									.hasBacklogMetaData(databaseName)) {
								policyNameNode = (DefaultMutableTreeNode) treeModel
										.getChild(selectedNode, 0);
								treeModel.removeNodeFromParent(policyNameNode);

								policyNameNode = (DefaultMutableTreeNode) treeModel
										.getChild(selectedNode, 0);
								treeModel.removeNodeFromParent(policyNameNode);

								policyNameNode = (DefaultMutableTreeNode) treeModel
										.getChild(selectedNode, 0);
								treeModel.removeNodeFromParent(policyNameNode);
							}

							controlCenter.auditor.getAuditTree().refresh();
							controlCenter.auditor
									.changeRightComponent(Auditor.BLANK_PANEL);
						}
					}).start();
					MessageBox.showBusy(controlCenter, "");

				}
			} else if (e.getSource() == popupMenus.defineEntitiesMenuItem
					|| e.getSource() == popupMenus.defineEntitiesMenuItem2) {
				String databaseName = "";

				if (e.getSource() == popupMenus.defineEntitiesMenuItem)
					databaseName = (String) selectedNode.getUserObject();
				else
					databaseName = (String) ((DefaultMutableTreeNode) selectedNode
							.getParent()).getUserObject();

				new NewEntryWizard(ChoosePanel.ADD_ENTITIES, ResourceManager
						.getResource("pt.select.ent"), databaseName,
						controlCenter.resourceManager, null);
			} else if (e.getSource() == popupMenus.deleteAllEntitiesMenuItem) {
				final String databaseName = (String) ((DefaultMutableTreeNode) selectedNode
						.getParent()).getUserObject();

				if (MessageBox.result(controlCenter, ResourceManager
						.getResource("label.confirm"), ResourceManager
						.getResource("pt.sure.del.all.ent"), MessageBox.ICON_WARN) == MessageBox.BUTTON_YES) {

					new Thread(new Runnable() {
						public void run() {
							final String msg = controlCenter.resourceManager
									.createAssociationAndChoiceTables(
											databaseName, new Vector());
							MessageBox.stopBusy();

							MessageBox.show(controlCenter, ResourceManager
									.getResource("label.message"), msg,
									MessageBox.ICON_INFO);
						}
					}).start();
					MessageBox.showBusy(controlCenter, ResourceManager
							.getResource("pt.deleting.ent"));

				}

			} else if (e.getSource() == popupMenus.defineApplicationUsageMenuItem
					|| e.getSource() == popupMenus.defineApplicationUsageMenuItem2) {
				if (e.getSource() == popupMenus.defineApplicationUsageMenuItem)
					applicationUsageDialog.showDialog((String) selectedNode
							.getUserObject());
				else
					applicationUsageDialog
							.showDialog((String) ((DefaultMutableTreeNode) selectedNode
									.getParent()).getUserObject());

			} else if (e.getSource() == popupMenus.deleteAllApplicationUsageMenuItem) {
				final String databaseName = (String) ((DefaultMutableTreeNode) selectedNode
						.getParent()).getUserObject();

				if (MessageBox.result(controlCenter, ResourceManager
						.getResource("label.confirm"), ResourceManager
						.getResource("pt.sure.del.all.app"),
						MessageBox.ICON_WARN) == MessageBox.BUTTON_YES)
					MessageBox.show(controlCenter, ResourceManager
							.getResource("label.message"),
							controlCenter.resourceManager
									.updateApplicationUsage(databaseName,
											new Vector()), MessageBox.ICON_ERR);

			} else if (e.getSource() == popupMenus.disconnectFromDatabaseMenuItem) {
				final String databaseName = (String) selectedNode
						.getUserObject();

				try {
					controlCenter.resourceManager.closeConnection(databaseName);
				} catch (SQLException exp) {
					MessageBox.show(controlCenter, ResourceManager
							.getResource("label.error"), ResourceManager
							.getResource("pt.couldnot.reset")
							+ " " + databaseName, MessageBox.ICON_ERR);
				}

			} else if (e.getSource() == popupMenus.addPolicyMenuItem) {
				final DefaultMutableTreeNode databaseNameNode = (DefaultMutableTreeNode) selectedNode
						.getParent();
				final String databaseName = (String) databaseNameNode
						.getUserObject();

				final Rule newRule = new Rule();

				MessageBox.showBusy(controlCenter, ResourceManager
						.getResource("pt.starting.prw"));
				new Thread(new Runnable() {
					public void run() {
						new PolicyRuleWizard(true, newRule, null,
								controlCenter.resourceManager, databaseName);
					}
				}).start();

			} else if (e.getSource() == popupMenus.deleteAllPoliciesMenuItem) {
				// get the parent (i.e. database name)
				final DefaultMutableTreeNode databaseNameNode = (DefaultMutableTreeNode) selectedNode
						.getParent();
				final String databaseName = (String) databaseNameNode
						.getUserObject();

				if (MessageBox.result(controlCenter, ResourceManager
						.getResource("label.confirm"), ResourceManager
						.getResource("pt.sure.del.all.pol"),
						MessageBox.ICON_WARN) == MessageBox.BUTTON_YES) {

					new Thread(new Runnable() {
						public void run() {
							final String msg = controlCenter.resourceManager
									.deletePolicy(new Version(databaseName,
											null, null));
							MessageBox.stopBusy();
							MessageBox.show(controlCenter, ResourceManager
									.getResource("label.message"), msg,
									MessageBox.ICON_INFO);
						}
					}).start();
					MessageBox.showBusy(controlCenter, ResourceManager
							.getResource("pt.deleting.all.pol"));

				}

			} else if (e.getSource() == popupMenus.addVersionMenuItem) {

				DefaultMutableTreeNode policyNode = (DefaultMutableTreeNode) jTree
						.getLastSelectedPathComponent();
				DefaultMutableTreeNode databaseNode = (DefaultMutableTreeNode) policyNode
						.getParent().getParent();

				final String databaseName = (String) databaseNode
						.getUserObject();
				final String policyName = (String) policyNode.getUserObject();

				new NewEntryWizard(NewEntryWizard.TYPE_NEW_VERSION,
						ResourceManager.getResource("pt.new.ver"),
						databaseName, controlCenter.resourceManager,
						new Version(databaseName, policyName, null));

				saveExpansion();
				refreshDatabaseNameNode(databaseNode);
				applyExpansion();
			} else if (e.getSource() == popupMenus.addVersionMenuItem2) {
				DefaultMutableTreeNode versionsNode = (DefaultMutableTreeNode) jTree
						.getLastSelectedPathComponent();
				DefaultMutableTreeNode policyNode = (DefaultMutableTreeNode) versionsNode
						.getParent();
				DefaultMutableTreeNode databaseNode = (DefaultMutableTreeNode) policyNode
						.getParent().getParent();

				final String databaseName = (String) databaseNode
						.getUserObject();
				final String policyName = (String) policyNode.getUserObject();

				new NewEntryWizard(NewEntryWizard.TYPE_NEW_VERSION,
						ResourceManager.getResource("pt.new.ver"),
						databaseName, controlCenter.resourceManager,
						new Version(databaseName, policyName, null));

				saveExpansion();
				refreshDatabaseNameNode(databaseNode);
				applyExpansion();
			} else if ((e.getSource() == popupMenus.deletePolicyMenuItem)
					|| (e.getSource() == popupMenus.scopeMenuItem)) {

				final DefaultMutableTreeNode databaseNameNode = (DefaultMutableTreeNode) selectedNode
						.getParent().getParent();
				final DefaultMutableTreeNode policyNameNode = (DefaultMutableTreeNode) selectedNode;
				final String databaseName = (String) databaseNameNode
						.getUserObject();
				final String policyName = (String) policyNameNode
						.getUserObject();
				Version version = new Version(databaseName, policyName, null);

				if (e.getSource() == popupMenus.deletePolicyMenuItem) {

					if (MessageBox.result(controlCenter, ResourceManager
							.getResource("label.confirm"), ResourceManager
							.getResource("pt.sure.del.pol"),
							MessageBox.ICON_WARN) == MessageBox.BUTTON_YES) {
						final String msg = controlCenter.resourceManager
								.deletePolicy(version);

						MessageBox.show(controlCenter, ResourceManager
								.getResource("label.error"), msg,
								MessageBox.ICON_ERR);
					}

				} else if (e.getSource() == popupMenus.scopeMenuItem) {
					version = controlCenter.resourceManager
							.getPolicyVersionType(version);

					new NewEntryWizard(NewEntryWizard.TYPE_COLUMNS_IN_SCOPE,
							ResourceManager.getResource("pt.def.scope"),
							databaseName, controlCenter.resourceManager,
							version);
				}
			} else if ((e.getSource() == popupMenus.scopeMenuItem2)) {

				final DefaultMutableTreeNode databaseNameNode = (DefaultMutableTreeNode) selectedNode
						.getParent().getParent().getParent();
				final DefaultMutableTreeNode policyNameNode = (DefaultMutableTreeNode) selectedNode
						.getParent();
				final String databaseName = (String) databaseNameNode
						.getUserObject();
				final String policyName = (String) policyNameNode
						.getUserObject();
				Version version = new Version(databaseName, policyName, null);

				version = controlCenter.resourceManager
						.getPolicyVersionType(version);

				new NewEntryWizard(NewEntryWizard.TYPE_COLUMNS_IN_SCOPE,
						ResourceManager.getResource("pt.def.scope"),
						databaseName, controlCenter.resourceManager, version);

			} else if ((e.getSource() == popupMenus.deleteAllScope)) {
				final DefaultMutableTreeNode databaseNameNode = (DefaultMutableTreeNode) selectedNode
						.getParent().getParent().getParent();
				final DefaultMutableTreeNode policyNameNode = (DefaultMutableTreeNode) selectedNode
						.getParent();
				final String databaseName = (String) databaseNameNode
						.getUserObject();
				final String policyName = (String) policyNameNode
						.getUserObject();
				Version version = new Version(databaseName, policyName, null);
				version = controlCenter.resourceManager
						.getPolicyVersionType(version);
				if (MessageBox
						.result(controlCenter, ResourceManager
								.getResource("label.confirm"), ResourceManager
								.getResource("pt.sure.del.scope"),
								MessageBox.ICON_WARN) == MessageBox.BUTTON_YES)
					MessageBox.show(controlCenter, ResourceManager
							.getResource("label.message"),
							controlCenter.resourceManager.updateScope(version,
									new Vector()), MessageBox.ICON_INFO);
			} else if (e.getSource() == popupMenus.renamePolicyMenuItem) {
				final DefaultMutableTreeNode databaseNameNode = (DefaultMutableTreeNode) selectedNode
						.getParent().getParent();

				final String databaseName = (String) databaseNameNode
						.getUserObject();
				final String policyName = (String) selectedNode.getUserObject();

				final Version version = new Version(databaseName, policyName,
						null);

				new NewEntryWizard(NewEntryWizard.TYPE_POLICY_RENAME,
						ResourceManager.getResource("pt.rename.pol"),
						databaseName, controlCenter.resourceManager, version);
			} else if (e.getSource() == popupMenus.deleteVersionMenuItem) {
				final DefaultMutableTreeNode policyNameNode = (DefaultMutableTreeNode) selectedNode
						.getParent().getParent();
				final DefaultMutableTreeNode databaseNameNode = (DefaultMutableTreeNode) policyNameNode
						.getParent().getParent();
				final DefaultMutableTreeNode versionNameNode = (DefaultMutableTreeNode) selectedNode;
				final String databaseName = (String) databaseNameNode
						.getUserObject();
				final String policyName = (String) policyNameNode
						.getUserObject();
				final String versionName = (String) versionNameNode
						.getUserObject();
				final Version version = new Version(databaseName, policyName,
						versionName);
				if (MessageBox.result(controlCenter, ResourceManager
						.getResource("label.confirm"), ResourceManager
						.getResource("pt.sure.del.ver")
						+ " \"" + versionName + "\"?", MessageBox.ICON_WARN) == MessageBox.BUTTON_YES) {
					final String msg = controlCenter.resourceManager
							.deletePolicy(version);
					MessageBox.show(controlCenter, ResourceManager
							.getResource("label.error"), msg,
							MessageBox.ICON_ERR);
				}
			} else if (e.getSource() == popupMenus.addRuleMenuItem) {
				final DefaultMutableTreeNode versionNameNode = (DefaultMutableTreeNode) selectedNode;
				final DefaultMutableTreeNode policyNameNode = (DefaultMutableTreeNode) selectedNode
						.getParent().getParent();
				final DefaultMutableTreeNode databaseNameNode = (DefaultMutableTreeNode) policyNameNode
						.getParent().getParent();
				final String databaseName = (String) databaseNameNode
						.getUserObject();
				final String policyName = (String) policyNameNode
						.getUserObject();
				final String versionName = (String) versionNameNode
						.getUserObject();
				final Version version = new Version(databaseName, policyName,
						versionName);

				MessageBox.showBusy(controlCenter, ResourceManager
						.getResource("pt.starting.prw"));

				new Thread(new Runnable() {
					public void run() {
						Rule newRule = new Rule();
						newRule.version = version.versionName;
						newRule.policyName = version.collectionName;
						newRule = controlCenter.resourceManager
								.getPolicyVersionType(databaseName, newRule);
						new PolicyRuleWizard(false, newRule, null,
								controlCenter.resourceManager, databaseName);
					}
				}).start();

			} else if (e.getSource() == popupMenus.deployVersionMenuItem) {
			} else if (e.getSource() == popupMenus.activateVersionMenuItem) {
				final DefaultMutableTreeNode policyNameNode = (DefaultMutableTreeNode) selectedNode
						.getParent().getParent();
				final DefaultMutableTreeNode databaseNameNode = (DefaultMutableTreeNode) policyNameNode
						.getParent().getParent();
				final String databaseName = (String) databaseNameNode
						.getUserObject();
				final String versionName = (String) selectedNode
						.getUserObject();
				final String policyName = (String) policyNameNode
						.getUserObject();
				final Version version = new Version(databaseName, policyName,
						versionName);

				final String msg = controlCenter.resourceManager
						.activateVersion(version);
				MessageBox.show(controlCenter, ResourceManager
						.getResource("label.error"), msg, MessageBox.ICON_ERR);
			} else if (e.getSource() == popupMenus.importRulesVersionMenuItem) {
				final DefaultMutableTreeNode policyNameNode = (DefaultMutableTreeNode) selectedNode
						.getParent().getParent();
				final DefaultMutableTreeNode databaseNameNode = (DefaultMutableTreeNode) policyNameNode
						.getParent().getParent();
				final String databaseName = (String) databaseNameNode
						.getUserObject();
				final String versionName = (String) selectedNode
						.getUserObject();
				final String policyName = (String) policyNameNode
						.getUserObject();
				final Version version = new Version(databaseName, policyName,
						versionName);

				new Thread(new Runnable() {
					public void run() {
						Vector importedRules = new Vector(4, 2);
						final Vector otherVersions = controlCenter.resourceManager
								.getPolicyVersions(version);
						otherVersions.remove(versionName);// excluding this
						// version

						if (otherVersions.size() == 0) {
							MessageBox.stopBusy();
							MessageBox.show(controlCenter, ResourceManager
									.getResource("label.message"),
									ResourceManager
											.getResource("pt.no.second.ver"),
									MessageBox.ICON_INFO);
						} else {
							final Vector allRules = controlCenter.resourceManager
									.getPolicyRules(new Version(databaseName,
											policyName, null));
							final Vector otherVersionRules = new Vector();

							for (int i = 0; i < allRules.size(); i++) {
								if (!((Rule) allRules.get(i)).version
										.equals(versionName))
									otherVersionRules.add(allRules.get(i));
							}

							ImportRuleWizard importRuleWizard = new ImportRuleWizard(
									ResourceManager
											.getResource("pt.import.rules"),
									controlCenter, version, importedRules,
									otherVersions, otherVersionRules);

							final String msg = controlCenter.resourceManager
									.importPolicyRules(version,
											importRuleWizard.getSelectedRules());
							MessageBox.stopBusy();
							MessageBox.show(controlCenter, ResourceManager
									.getResource("label.message"), msg,
									MessageBox.ICON_ERR);
						}
						MessageBox.stopBusy();
					}
				}).start();
				MessageBox.showBusy(controlCenter, ResourceManager
						.getResource("pt.importing.rules"));

			} else if (e.getSource() == popupMenus.renameVersionMenuItem) {

				final DefaultMutableTreeNode policyNameNode = (DefaultMutableTreeNode) selectedNode
						.getParent().getParent();
				final DefaultMutableTreeNode databaseNameNode = (DefaultMutableTreeNode) policyNameNode
						.getParent().getParent();
				final String databaseName = (String) databaseNameNode
						.getUserObject();
				final String versionName = (String) selectedNode
						.getUserObject();
				final String policyName = (String) policyNameNode
						.getUserObject();
				final Version version = controlCenter.resourceManager
						.getPolicyVersionType(new Version(databaseName,
								policyName, versionName));

				new NewEntryWizard(NewEntryWizard.TYPE_VERSION_RENAME,
						ResourceManager.getResource("pt.rename.ver"),
						databaseName, controlCenter.resourceManager, version);

			} else if (e.getSource() == popupMenus.deleteAllPurposeMenuItem) {

				if (MessageBox.result(controlCenter, ResourceManager
						.getResource("label.confirm"), ResourceManager
						.getResource("pt.sure.del.all.pur"),
						MessageBox.ICON_WARN) == MessageBox.BUTTON_YES)
					MessageBox.show(controlCenter, ResourceManager
							.getResource("label.message"),
							controlCenter.resourceManager.deleteAllEntries(
									getDatabaseName(selectedNode),
									ResourceManager.DELETE_ALL_PURPOSES),
							MessageBox.ICON_INFO);
			} else if (e.getSource() == popupMenus.deleteAllRecipientMenuItem) {

				if (MessageBox.result(controlCenter, ResourceManager
						.getResource("label.confirm"), ResourceManager
						.getResource("pt.sure.del.all.rec"),
						MessageBox.ICON_WARN) == MessageBox.BUTTON_YES)
					MessageBox.show(controlCenter, ResourceManager
							.getResource("label.message"),
							controlCenter.resourceManager.deleteAllEntries(
									getDatabaseName(selectedNode),
									ResourceManager.DELETE_ALL_RECIPIENTS),
							MessageBox.ICON_INFO);
			} else if (e.getSource() == popupMenus.deleteAllAccessorMenuItem) {

				if (MessageBox.result(controlCenter, ResourceManager
						.getResource("label.confirm"), ResourceManager
						.getResource("pt.sure.del.all.acc"),
						MessageBox.ICON_WARN) == MessageBox.BUTTON_YES)
					MessageBox.show(controlCenter, ResourceManager
							.getResource("label.message"),
							controlCenter.resourceManager.deleteAllEntries(
									getDatabaseName(selectedNode),
									ResourceManager.DELETE_ALL_ACCESSORS),
							MessageBox.ICON_INFO);
			} else if (e.getSource() == popupMenus.definePurposeMenuItem) {
				new NewEntryWizard(NamePanel.PURPOSE, ResourceManager
						.getResource("pt.add.new.pur"),
						(String) ((DefaultMutableTreeNode) selectedNode
								.getParent()).getUserObject(),
						controlCenter.resourceManager, null);
			} else if (e.getSource() == popupMenus.defineRecipientMenuItem) {
				new NewEntryWizard(NamePanel.RECIPIENT, ResourceManager
						.getResource("pt.add.new.rec"),
						(String) ((DefaultMutableTreeNode) selectedNode
								.getParent()).getUserObject(),
						controlCenter.resourceManager, null);
			} else if (e.getSource() == popupMenus.defineAccessorMenuItem) {
				new NewEntryWizard(NamePanel.ACCESSOR, ResourceManager
						.getResource("pt.add.new.acc"),
						(String) ((DefaultMutableTreeNode) selectedNode
								.getParent()).getUserObject(),
						controlCenter.resourceManager, null);
			}

		}

		public void mousePressed(MouseEvent e) {

			TreePath path = jTree.getPathForLocation(e.getX(), e.getY());
			if (path == null) {
				jTree.clearSelection();
				return;
			}
			jTree.setSelectionPath(path);
		}

		public void mouseReleased(MouseEvent e) {

			if (e.getButton() != MouseEvent.BUTTON1 && selectedNode != null) {
				TreePath selPath = jTree.getSelectionPath();
				if (selPath != null) {
					int path = selPath.getPathCount();
					if (path == 1) {
						popupMenus.databaseNodePopup.show(e.getComponent(), e
								.getX() + 10, e.getY() + 10);

						final MenuElement[] menuItems = popupMenus.databaseNodePopup
								.getSubElements();
						((JMenuItem) menuItems[0]).setEnabled(true);
						((JMenuItem) menuItems[1]).setEnabled(!Utility
								.isFullyExpanded(jTree));
						((JMenuItem) menuItems[2]).setEnabled(!Utility
								.isFullyCollapsed(jTree));
					} else if (path == 2) {
						popupMenus.databaseNamePopup.show(e.getComponent(), e
								.getX() + 10, e.getY() + 10);
						MenuElement[] menuItems = popupMenus.databaseNamePopup
								.getSubElements();
						boolean hasMetadataTables = controlCenter.resourceManager
								.hasMetadataTables((String) selectedNode
										.getUserObject());

						((JMenuItem) menuItems[0])
								.setEnabled(!hasMetadataTables);
						((JMenuItem) menuItems[1])
								.setEnabled(hasMetadataTables);
						((JMenuItem) menuItems[2])
								.setEnabled(hasMetadataTables);
						((JMenuItem) menuItems[3])
								.setEnabled(hasMetadataTables);
						// popupMenus.

					} else if (path == 3) {
						if (selectedNode.getUserObject().toString().equals(
								ResourceManager.getResource("pt.app"))) {
							popupMenus.applicationPopup.show(e.getComponent(),
									e.getX() + 10, e.getY() + 10);
						} else if (selectedNode.getUserObject().toString()
								.equals(ResourceManager.getResource("pt.ent"))) {
							popupMenus.entityPopup.show(e.getComponent(), e
									.getX() + 10, e.getY() + 10);
						} else if (selectedNode.getUserObject().toString()
								.equals(ResourceManager.getResource("pt.pur"))) {
							popupMenus.purposePopup.show(e.getComponent(), e
									.getX() + 10, e.getY() + 10);
						} else if (selectedNode.getUserObject().toString()
								.equals(ResourceManager.getResource("pt.rec"))) {
							popupMenus.recipientPopup.show(e.getComponent(), e
									.getX() + 10, e.getY() + 10);
						} else if (selectedNode.getUserObject().toString()
								.equals(ResourceManager.getResource("pt.acc"))) {
							popupMenus.accessorPopup.show(e.getComponent(), e
									.getX() + 10, e.getY() + 10);
						} else {
							popupMenus.policyPopup.show(e.getComponent(), e
									.getX() + 10, e.getY() + 10);
							MenuElement[] menuItems = popupMenus.policyPopup
									.getSubElements();
							boolean hasPolicies = selectedNode.isLeaf();
							((JMenuItem) menuItems[1]).setEnabled(!hasPolicies);
						}
					} else if (path == 4) {
						popupMenus.policyNamePopup.show(e.getComponent(), e
								.getX() + 10, e.getY() + 10);
					} else if (path == 5) {
						if (selectedNode.getUserObject().toString().equals(
								ResourceManager.getResource("pt.scope"))) {
							popupMenus.defineScopePopup.show(e.getComponent(),
									e.getX() + 10, e.getY() + 10);

						} else// Versions
						{
							popupMenus.versionsPopup.show(e.getComponent(), e
									.getX() + 10, e.getY() + 10);
						}
					} else if (path == 6) {
						final String versionName = (String) selectedNode
								.getUserObject();
						final String policyName = (String) ((DefaultMutableTreeNode) selectedNode
								.getParent().getParent()).getUserObject();
						final String databaseName = (String) ((DefaultMutableTreeNode) selectedNode
								.getParent().getParent().getParent()
								.getParent()).getUserObject();
						final Version version = controlCenter.resourceManager
								.getPolicyVersionType(new Version(databaseName,
										policyName, versionName));
						popupMenus.activateVersionMenuItem
								.setEnabled((version.type == Version.TYPE_SIMPLE && version.enabled == 0));
						popupMenus.versionPopup.show(e.getComponent(),
								e.getX() + 10, e.getY() + 10);
					}
				}
			}
		}

		public void valueChanged(TreeSelectionEvent e) {
			selectedNode = (DefaultMutableTreeNode) jTree
					.getLastSelectedPathComponent();

			if (selectedNode != null) {
				selectionPath = jTree.getSelectionPath();

				if (selectionPath != null) {
					final int path = selectionPath.getPathCount();

					if (path == 6) // i.e. looking at a policy now
					{
						DefaultMutableTreeNode policyNameNode = (DefaultMutableTreeNode) selectedNode
								.getParent().getParent();
						DefaultMutableTreeNode databaseNode = (DefaultMutableTreeNode) policyNameNode
								.getParent().getParent();

						Vector versionInfo = new Vector(4, 2);
						versionInfo.add(selectedNode.getUserObject());
						versionInfo.add(policyNameNode.getUserObject());
						versionInfo.add(databaseNode.getUserObject());

						policyEditor.changeRightComponent(
								PolicyEditor.RULE_DISPLAY_PANEL, versionInfo);
					} else if (selectedNode.getUserObject().toString().equals(
							ResourceManager.getResource("pt.scope"))) {

						Vector versionInfo = new Vector(4, 2);
						// adding database name
						versionInfo.add(((DefaultMutableTreeNode) selectedNode
								.getParent().getParent().getParent())
								.getUserObject());
						// adding policy name
						versionInfo.add(((DefaultMutableTreeNode) selectedNode
								.getParent()).getUserObject());

						policyEditor.changeRightComponent(
								PolicyEditor.COLUMNS_IN_SCOPE_DISPLAY_PANEL,
								versionInfo);
					} else if (selectedNode.getUserObject().toString().equals(
							ResourceManager.getResource("pt.app"))) {
						DefaultMutableTreeNode databaseNode = (DefaultMutableTreeNode) selectedNode
								.getParent();

						Vector versionInfo = new Vector(4, 2);
						versionInfo.add(databaseNode.getUserObject());
						policyEditor.changeRightComponent(
								PolicyEditor.APPLICATION_DISPLAY_PANEL,
								versionInfo);
					} else if (selectedNode.getUserObject().toString().equals(
							ResourceManager.getResource("pt.ent"))) {
						DefaultMutableTreeNode databaseNode = (DefaultMutableTreeNode) selectedNode
								.getParent();

						Vector versionInfo = new Vector(4, 2);
						versionInfo.add(databaseNode.getUserObject());
						policyEditor.changeRightComponent(
								PolicyEditor.ENTITIES_DISPLAY_PANEL,
								versionInfo);
					} else if (selectedNode.getUserObject().toString().equals(
							ResourceManager.getResource("pt.pur"))) {
						DefaultMutableTreeNode databaseNode = (DefaultMutableTreeNode) selectedNode
								.getParent();

						Vector versionInfo = new Vector(4, 2);
						versionInfo.add(databaseNode.getUserObject());
						policyEditor.changeRightComponent(
								PolicyEditor.PURPOSES_DISPLAY_PANEL,
								versionInfo);
					} else if (selectedNode.getUserObject().toString().equals(
							ResourceManager.getResource("pt.rec"))) {
						DefaultMutableTreeNode databaseNode = (DefaultMutableTreeNode) selectedNode
								.getParent();

						Vector versionInfo = new Vector(4, 2);
						versionInfo.add(databaseNode.getUserObject());
						policyEditor.changeRightComponent(
								PolicyEditor.RECIPIENTS_DISPLAY_PANEL,
								versionInfo);
					} else if (selectedNode.getUserObject().toString().equals(
							ResourceManager.getResource("pt.acc"))) {
						DefaultMutableTreeNode databaseNode = (DefaultMutableTreeNode) selectedNode
								.getParent();

						Vector versionInfo = new Vector(4, 2);
						versionInfo.add(databaseNode.getUserObject());
						policyEditor.changeRightComponent(
								PolicyEditor.ACCESSORS_DISPLAY_PANEL,
								versionInfo);
					} else
						policyEditor
								.changeRightComponent(PolicyEditor.BLANK_PANEL);
				}
			}
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

}
