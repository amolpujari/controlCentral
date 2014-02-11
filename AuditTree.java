package controlCenter;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
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

public class AuditTree extends Tree implements ResourceListener {
	private Auditor auditor;

	private DefaultMutableTreeNode selectedNode;

	private TreePath selectionPath;

	public AuditTree(ControlCenter controlCenter, Auditor auditor) {
		this.controlCenter = controlCenter;
		this.auditor = auditor;
		controlCenter.resourceManager.addResourceListener(this);

		jTree = new JTree();
		// Create a tree that allows one selection at a time.
		jTree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		// Listen for when the selection changes.
		AuditTreeListener listener = new AuditTreeListener();
		jTree.addTreeSelectionListener(listener);
		jTree.addMouseListener(listener);

		treeModel = new DefaultTreeModel(construct());
		jTree.setModel(treeModel);
		setViewportView(jTree);
		jTree.setCellRenderer(controlCenter.resourceManager.getTreeRenderer());
		Dimension controlCenterSize = controlCenter.getSize();
		setPreferredSize(new Dimension(
				(int) (controlCenterSize.getWidth() / 3),
				(int) (controlCenterSize.getHeight())));

		jTree.setRowHeight(jTree.getRowHeight() + 8);
	}

	public boolean isManagingFocus() {
		return true;
	}

	public DefaultTreeModel getAuditTreeModel() {
		return treeModel;
	}

	// public ChooseDialog getBacklogTablesSelectionDialog() {
	// return backlogTablesSelectionDialog;
	// }

	public void auditChanged(Version version) {
		final DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) treeModel
				.getRoot();
		DefaultMutableTreeNode databaseNodeToUpdate = null;

		final int numDatabases = treeModel.getChildCount(rootNode);
		for (int i = 0; i < numDatabases && databaseNodeToUpdate == null; i++) {
			final DefaultMutableTreeNode databaseNode = (DefaultMutableTreeNode) treeModel
					.getChild(rootNode, i);
			if (((String) databaseNode.getUserObject())
					.equalsIgnoreCase(version.databaseName))
				databaseNodeToUpdate = databaseNode;
		}

		saveExpansion();
		refreshDatabaseNameNode(databaseNodeToUpdate);
		applyExpansion();
		jTree.scrollPathToVisible(new TreePath(databaseNodeToUpdate
				.getLastLeaf().getPath()));
	}

	public void policyChanged(Version version) {
		// Nothing to do.
	}

	public void refreshDatabaseNameNode(final String databaseName) {
	}

	public void refreshDatabaseNameNode(DefaultMutableTreeNode databaseNode) {
		// get rid of all children first
		databaseNode.removeAllChildren();
		treeModel.reload();

		final TreeMap audits = controlCenter.resourceManager
				.getAudits((String) databaseNode.getUserObject());

		if (audits != null) {
			final DefaultMutableTreeNode collectionNameHolder = new DefaultMutableTreeNode(
					ResourceManager.getResource("at.audits"));

			treeModel.insertNodeInto(collectionNameHolder, databaseNode,
					databaseNode.getChildCount());

			final Set auditNames = audits.keySet();
			final Iterator iterator = auditNames.iterator();

			while (iterator.hasNext()) {
				final String auditName = (String) iterator.next();
				final DefaultMutableTreeNode taskNode = new DefaultMutableTreeNode(
						auditName);
				treeModel.insertNodeInto(taskNode, collectionNameHolder,
						collectionNameHolder.getChildCount());
			}
		}

		if (controlCenter.resourceManager
				.hasBacklogMetaData((String) databaseNode.getUserObject())) {
			final DefaultMutableTreeNode backlogNode = new DefaultMutableTreeNode(
					ResourceManager.getResource("at.backlogs"));
			treeModel.insertNodeInto(backlogNode, databaseNode, databaseNode
					.getChildCount());
		}

		if (controlCenter.resourceManager
				.hasMetadataTables((String) databaseNode.getUserObject())
				|| controlCenter.resourceManager
						.hasBacklogMetaData((String) databaseNode
								.getUserObject())) {
			final DefaultMutableTreeNode purposesNode = new DefaultMutableTreeNode(
					ResourceManager.getResource("at.purposes"));
			treeModel.insertNodeInto(purposesNode, databaseNode, databaseNode
					.getChildCount());

			final DefaultMutableTreeNode recipientsNode = new DefaultMutableTreeNode(
					ResourceManager.getResource("at.recipients"));
			treeModel.insertNodeInto(recipientsNode, databaseNode, databaseNode
					.getChildCount());

			final DefaultMutableTreeNode accessorsNode = new DefaultMutableTreeNode(
					ResourceManager.getResource("at.accessors"));
			treeModel.insertNodeInto(accessorsNode, databaseNode, databaseNode
					.getChildCount());
		}

	}

	public void refreshAuditsNode(DefaultMutableTreeNode auditsNode) {
		// get rid of all children first
		auditsNode.removeAllChildren();
		treeModel.reload();
		final DefaultMutableTreeNode databaseNode = (DefaultMutableTreeNode) auditsNode
				.getParent();
		final TreeMap audits = controlCenter.resourceManager
				.getAudits((String) databaseNode.getUserObject());

		if (audits != null) {
			final Set auditNames = audits.keySet();
			final Iterator iterator = auditNames.iterator();
			while (iterator.hasNext()) {
				final String auditName = (String) iterator.next();
				final DefaultMutableTreeNode auditNode = new DefaultMutableTreeNode(
						auditName);
				treeModel.insertNodeInto(auditNode, auditsNode, auditsNode
						.getChildCount());
			}
		}
	}

	class PopupMenus {
		JPopupMenu databaseNodePopup;

		JPopupMenu databaseNamePopup;

		JPopupMenu auditPopup;

		JPopupMenu auditNamePopup;

		JPopupMenu versionPopup;

		JPopupMenu versionNamePopup;

		JPopupMenu backlogPopup;

		JPopupMenu purposePopup;

		JPopupMenu recipientPopup;

		JPopupMenu accessorPopup;

		JMenuItem connectToDatabaseMenuItem;

		JMenuItem createBacklogMenuItem;

		JMenuItem dropBacklogMenuItem;

		JMenuItem defineBacklogTablesMenuItem;

		JMenuItem checkForSchemaChangeMenuItem;

		JMenuItem disconnectFromDatabaseMenuItem;

		JMenuItem addExampleDataMenuItem;

		JMenuItem expandEntireTreeMenuItem;

		JMenuItem collapseEntireTreeMenuItem;

		JMenuItem addAuditMenuItem;

		JMenuItem deleteAllAuditsMenuItem;

		JMenuItem deleteAuditMenuItem;

		JMenuItem renameAuditMenuItem;

		JMenuItem addVersionMenuItem;

		JMenuItem deleteAllVersionsMenuItem;

		JMenuItem addTaskMenuItem;

		JMenuItem deleteVersionMenuItem;

		JMenuItem renameVersionMenuItem;

		JMenuItem definePurposeMenuItem;

		JMenuItem deleteAllPurposeMenuItem;

		JMenuItem defineRecipientMenuItem;

		JMenuItem deleteAllRecipientMenuItem;

		JMenuItem defineAccessorMenuItem;

		JMenuItem deleteAllAccessorMenuItem;

		JMenuItem defineBacklogTablesMenuItem2;

		JMenuItem deleteAllBacklogTablesMenuItem;

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

			createBacklogMenuItem = new JMenuItem(ResourceManager
					.getResource("at.install.metadata"));
			createBacklogMenuItem.addActionListener(listener);
			dropBacklogMenuItem = new JMenuItem(ResourceManager
					.getResource("at.uninstall.metadata"));
			dropBacklogMenuItem.addActionListener(listener);
			defineBacklogTablesMenuItem = new JMenuItem(ResourceManager
					.getResource("at.define.backlog.tables"));
			defineBacklogTablesMenuItem.addActionListener(listener);

			disconnectFromDatabaseMenuItem = new JMenuItem(ResourceManager
					.getResource("at.disconnect"));
			disconnectFromDatabaseMenuItem.addActionListener(listener);
			checkForSchemaChangeMenuItem = new JMenuItem(ResourceManager
					.getResource("at.check.for.schema.change"));
			checkForSchemaChangeMenuItem.addActionListener(listener);
			addExampleDataMenuItem = new JMenuItem(ResourceManager
					.getResource("at.add.example.data"));
			addExampleDataMenuItem.addActionListener(listener);

			databaseNamePopup.add(createBacklogMenuItem);
			databaseNamePopup.add(dropBacklogMenuItem);
			databaseNamePopup.add(defineBacklogTablesMenuItem);
			databaseNamePopup.add(checkForSchemaChangeMenuItem);
			databaseNamePopup.addSeparator();
			databaseNamePopup.add(disconnectFromDatabaseMenuItem);

			databaseNamePopup.addSeparator();
			databaseNamePopup.add(addExampleDataMenuItem);

			auditPopup = new JPopupMenu();
			addAuditMenuItem = new JMenuItem(ResourceManager
					.getResource("at.add.audit"));
			addAuditMenuItem.addActionListener(listener);
			deleteAllAuditsMenuItem = new JMenuItem(ResourceManager
					.getResource("at.delete.all.audits"));
			deleteAllAuditsMenuItem.addActionListener(listener);
			auditPopup.add(addAuditMenuItem);
			auditPopup.add(deleteAllAuditsMenuItem);

			auditNamePopup = new JPopupMenu();
			addTaskMenuItem = new JMenuItem(ResourceManager
					.getResource("at.add.audit.query"));
			addTaskMenuItem.addActionListener(listener);
			deleteAuditMenuItem = new JMenuItem(ResourceManager
					.getResource("label.delete"));
			deleteAuditMenuItem.addActionListener(listener);
			renameAuditMenuItem = new JMenuItem(ResourceManager
					.getResource("label.rename"));
			renameAuditMenuItem.addActionListener(listener);
			auditNamePopup.add(addTaskMenuItem);
			auditNamePopup.add(deleteAuditMenuItem);
			auditNamePopup.add(renameAuditMenuItem);

			backlogPopup = new JPopupMenu();
			defineBacklogTablesMenuItem2 = new JMenuItem(ResourceManager
					.getResource("at.define.backlog.tables"));
			defineBacklogTablesMenuItem2.addActionListener(listener);
			deleteAllBacklogTablesMenuItem = new JMenuItem(ResourceManager
					.getResource("at.delete.all.tables"));
			deleteAllBacklogTablesMenuItem.addActionListener(listener);
			backlogPopup.add(defineBacklogTablesMenuItem2);
			backlogPopup.addSeparator();
			backlogPopup.add(deleteAllBacklogTablesMenuItem);

			purposePopup = new JPopupMenu();
			definePurposeMenuItem = new JMenuItem(ResourceManager
					.getResource("at.define.purpose"));
			definePurposeMenuItem.addActionListener(listener);
			deleteAllPurposeMenuItem = new JMenuItem(ResourceManager
					.getResource("at.delete.all.purposes"));
			deleteAllPurposeMenuItem.addActionListener(listener);
			purposePopup.add(definePurposeMenuItem);
			purposePopup.addSeparator();
			purposePopup.add(deleteAllPurposeMenuItem);

			recipientPopup = new JPopupMenu();
			defineRecipientMenuItem = new JMenuItem(ResourceManager
					.getResource("at.define.recipient"));
			defineRecipientMenuItem.addActionListener(listener);
			deleteAllRecipientMenuItem = new JMenuItem(ResourceManager
					.getResource("at.delete.all.recipients"));
			deleteAllRecipientMenuItem.addActionListener(listener);
			recipientPopup.add(defineRecipientMenuItem);
			recipientPopup.addSeparator();
			recipientPopup.add(deleteAllRecipientMenuItem);

			accessorPopup = new JPopupMenu();
			defineAccessorMenuItem = new JMenuItem(ResourceManager
					.getResource("at.define.accessor"));
			defineAccessorMenuItem.addActionListener(listener);
			deleteAllAccessorMenuItem = new JMenuItem(ResourceManager
					.getResource("at.delete.all.accessors"));
			deleteAllAccessorMenuItem.addActionListener(listener);
			accessorPopup.add(defineAccessorMenuItem);
			accessorPopup.addSeparator();
			accessorPopup.add(deleteAllAccessorMenuItem);

		}
	}

	class AuditTreeListener extends MouseAdapter implements ActionListener,
			TreeSelectionListener {
		private PopupMenus popupMenus;

		public AuditTreeListener() {
			popupMenus = new PopupMenus(this);

		}

		private String getDatabaseName(DefaultMutableTreeNode seleectedNode) {
			TreeNode[] tn = seleectedNode.getPath();
			return tn[1].toString();
		}

		public void actionPerformed(ActionEvent e) {
			final Object source = e.getSource();

			if (source == popupMenus.connectToDatabaseMenuItem)
				auditor.changeRightComponent(Auditor.DATABASE_CONNECTION_PANEL);

			else if (source == popupMenus.expandEntireTreeMenuItem)
				Utility.expandTree(jTree);

			else if (source == popupMenus.collapseEntireTreeMenuItem)
				Utility.collapseTree(jTree);

			else if (source == popupMenus.disconnectFromDatabaseMenuItem) {
				final String databaseName = (String) selectedNode
						.getUserObject();

				new Thread(new Runnable() {
					public void run() {
						try {
							controlCenter.resourceManager
									.closeConnection(databaseName);
							MessageBox.stopBusy();
						} catch (SQLException exp) {
							MessageBox.stopBusy();
							MessageBox
									.show(
											controlCenter,
											ResourceManager
													.getResource("label.error"),
											ResourceManager
													.getResource("at.could.not.reset.connection")
													+ " " + databaseName,
											MessageBox.ICON_ERR);
						}
					}
				}).start();
				MessageBox.showBusy(controlCenter, ResourceManager
						.getResource("at.dissconnecting.from")
						+ " " + databaseName);

			} else if (source == popupMenus.createBacklogMenuItem) {

				new Thread(new Runnable() {
					public void run() {
						if (controlCenter.resourceManager
								.createBacklogMetaData((String) selectedNode
										.getUserObject())) {
							MessageBox
									.show(
											controlCenter,
											ResourceManager
													.getResource("label.success"),
											ResourceManager
													.getResource("at.audit.metadata.installed.successfully"),
											MessageBox.ICON_SUC);

							saveExpansion();
							refreshDatabaseNameNode(selectedNode);
							applyExpansion();

						} else
							MessageBox
									.show(
											controlCenter,
											ResourceManager
													.getResource("label.error"),
											ResourceManager
													.getResource("at.could.not.install.metadata"),
											MessageBox.ICON_ERR);

						controlCenter.policyEditor.getPolicyTree().refresh();
						controlCenter.policyEditor
								.changeRightComponent(Auditor.BLANK_PANEL);
						MessageBox.stopBusy();
					}
				}).start();
				MessageBox.showBusy(controlCenter, ResourceManager
						.getResource("at.creating.backlog"));

			} else if (source == popupMenus.dropBacklogMenuItem) {

				new Thread(new Runnable() {
					public void run() {
						final String databaseName = (String) selectedNode
								.getUserObject();
						if (MessageBox.result(controlCenter, ResourceManager
								.getResource("label.confirm"), ResourceManager
								.getResource("at.sure.drop.metadata"),
								MessageBox.ICON_WARN) == MessageBox.BUTTON_YES) {
							if (!controlCenter.resourceManager
									.dropBacklog(databaseName))
								MessageBox
										.show(
												controlCenter,
												ResourceManager
														.getResource("label.error"),
												ResourceManager
														.getResource("at.could.not.drop.metadata"),
												MessageBox.ICON_ERR);
							else {
								final DefaultTreeModel auditTreeModel = controlCenter.auditor
										.getAuditTree().getAuditTreeModel();

								DefaultMutableTreeNode taskNameNode = (DefaultMutableTreeNode) auditTreeModel
										.getChild(selectedNode, 0);
								auditTreeModel
										.removeNodeFromParent(taskNameNode);

								taskNameNode = (DefaultMutableTreeNode) auditTreeModel
										.getChild(selectedNode, 0);
								auditTreeModel
										.removeNodeFromParent(taskNameNode);

								if (!controlCenter.resourceManager
										.hasMetadataTables(databaseName)) {
									taskNameNode = (DefaultMutableTreeNode) treeModel
											.getChild(selectedNode, 0);
									treeModel
											.removeNodeFromParent(taskNameNode);

									taskNameNode = (DefaultMutableTreeNode) treeModel
											.getChild(selectedNode, 0);
									treeModel
											.removeNodeFromParent(taskNameNode);

									taskNameNode = (DefaultMutableTreeNode) treeModel
											.getChild(selectedNode, 0);
									treeModel
											.removeNodeFromParent(taskNameNode);
								}

								MessageBox
										.show(
												controlCenter,
												ResourceManager
														.getResource("label.success"),
												ResourceManager
														.getResource("at.audit.metadata.deleted.successfully"),
												MessageBox.ICON_SUC);

								controlCenter.policyEditor.getPolicyTree()
										.refresh();
								controlCenter.policyEditor
										.changeRightComponent(Auditor.BLANK_PANEL);

							}
						}
						MessageBox.stopBusy();
					}
				}).start();
				MessageBox.showBusy(controlCenter, ResourceManager
						.getResource("at.uninstalling.metadata"));

			} else if (source == popupMenus.checkForSchemaChangeMenuItem) {
				final String databaseName = (String) selectedNode
						.getUserObject();

				if (controlCenter.resourceManager
						.hasBacklogTableSchemaChange(databaseName)) {
					final BacklogUpdateSelectionDialog dialog = new BacklogUpdateSelectionDialog(
							controlCenter.resourceManager, databaseName);
					dialog.setLocationRelativeTo(null);
					dialog.show();
				} else {
					MessageBox.show(controlCenter, ResourceManager
							.getResource("at.schema.reconciliation"), " \""
							+ databaseName + "\" "
							+ ResourceManager.getResource("at.are.up.to.date"),
							MessageBox.ICON_INFO);
				}

			} else if (source == popupMenus.defineBacklogTablesMenuItem
					|| source == popupMenus.defineBacklogTablesMenuItem2) {
				final String databaseName;

				if (source == popupMenus.defineBacklogTablesMenuItem2)
					databaseName = (String) ((DefaultMutableTreeNode) selectedNode
							.getParent()).getUserObject();
				else
					databaseName = (String) selectedNode.getUserObject();

				new NewEntryWizard(ChoosePanel.BACKLOG, ResourceManager
						.getResource("at.select.tables.for.backlog"),
						databaseName, controlCenter.resourceManager, null);

			} else if (source == popupMenus.deleteAllBacklogTablesMenuItem) {
				if (MessageBox.result(controlCenter, ResourceManager
						.getResource("label.confirm"), ResourceManager
						.getResource("at.sure.drop.backlog"),
						MessageBox.ICON_WARN) == MessageBox.BUTTON_YES) {
					MessageBox.showBusy(controlCenter, ResourceManager
							.getResource("at.dropping.all.backlogs"));
					new Thread(new Runnable() {
						public void run() {
							final String databaseName = (String) ((DefaultMutableTreeNode) selectedNode
									.getParent()).getUserObject();
							final String msg = controlCenter.resourceManager
									.dropAllBackogTables(databaseName);
							MessageBox.stopBusy();
							MessageBox.show(controlCenter, ResourceManager
									.getResource("label.message"), msg,
									MessageBox.ICON_SUC);
						}
					}).start();
				}
			} else if (source == popupMenus.addExampleDataMenuItem) {

				final int result = controlCenter.resourceManager
						.addExampleData((String) selectedNode.getUserObject());

				if (result == 0) {
				} else if (result == 23505)// [IBM][CLI Driver][DB2/NT]
					// SQL0803N One or more values in
					// the INSERT statement, UPDATE
					// statement, or foreign key update
					// caused by a DELETE statement are
					// not valid because the primary
					// key, unique constraint or unique
					// index identified by "1"
					// constrains table
					// "HDBADMIN.PURPOSES" from having
					// duplicate rows for those columns.
					// SQLSTATE=23505

					MessageBox.show(controlCenter, ResourceManager
							.getResource("label.message"), ResourceManager
							.getResource("at.example.data.already.added"),
							MessageBox.ICON_SUC);
				else
					MessageBox.show(controlCenter, ResourceManager
							.getResource("label.error"), ResourceManager
							.getResource("at.cannot.add.example"),
							MessageBox.ICON_ERR);
			} else if (source == popupMenus.addAuditMenuItem) {
				final DefaultMutableTreeNode parent = (DefaultMutableTreeNode) selectedNode
						.getParent();
				final String databaseName = (String) parent.getUserObject();

				MessageBox.showBusy(controlCenter, ResourceManager
						.getResource("at.starting.atw"));
				new Thread(new Runnable() {
					public void run() {
						new AuditQueryWizard(true, new Task(), null,
								controlCenter.resourceManager, databaseName);
					}
				}).start();

			} else if (source == popupMenus.deleteAllAuditsMenuItem) {
				if (MessageBox.result(controlCenter, ResourceManager
						.getResource("label.confirm"), ResourceManager
						.getResource("at.sure.delete.audits"),
						MessageBox.ICON_WARN) == MessageBox.BUTTON_YES) {
					new Thread(new Runnable() {
						public void run() {
							if (!controlCenter.resourceManager
									.deleteAudit(new Version(
											getDatabaseName(selectedNode),
											null, null)))
								MessageBox
										.show(
												controlCenter,
												ResourceManager
														.getResource("label.error"),
												ResourceManager
														.getResource("at.could.not.delete.audits"),
												MessageBox.ICON_ERR);
							MessageBox.stopBusy();
						}
					}).start();
					MessageBox.showBusy(controlCenter, ResourceManager
							.getResource("at.deleting.audits"));

				}

			} else if (source == popupMenus.deleteAuditMenuItem) {
				final String auditName = (String) selectedNode.getUserObject();
				final Version version = new Version(
						getDatabaseName(selectedNode), auditName, null);

				if (MessageBox.result(controlCenter, ResourceManager
						.getResource("label.confirm"), ResourceManager
						.getResource("at.sure.delete.audit")
						+ " \"" + auditName + "\"?", MessageBox.ICON_CON) == MessageBox.BUTTON_YES)
					if (!controlCenter.resourceManager.deleteAudit(version))
						MessageBox.show(controlCenter, ResourceManager
								.getResource("label.error"), ResourceManager
								.getResource("at.could.not.delete.audit"),
								MessageBox.ICON_ERR);

			} else if (source == popupMenus.renameAuditMenuItem) {

				final String databaseName = (String) ((DefaultMutableTreeNode) selectedNode
						.getParent().getParent()).getUserObject();

				final Version version = new Version(databaseName,
						(String) selectedNode.getUserObject(), null);

				new NewEntryWizard(NewEntryWizard.TYPE_AUDIT_RENAME,
						ResourceManager.getResource("at.rename.audit"),
						databaseName, controlCenter.resourceManager, version);

			} else if (source == popupMenus.addTaskMenuItem) {

				MessageBox.showBusy(controlCenter, ResourceManager
						.getResource("at.starting.aqw"));

				DefaultMutableTreeNode databaseNode = (DefaultMutableTreeNode) selectedNode
						.getParent().getParent();
				DefaultMutableTreeNode taskNameNode = (DefaultMutableTreeNode) selectedNode;
				final String databaseName = (String) databaseNode
						.getUserObject();
				final String auditName = (String) taskNameNode.getUserObject();
				final String versionName = "1";
				final Version version = new Version(databaseName, auditName,
						versionName);

				final Task newTask = new Task();
				newTask.version = version.versionName;
				newTask.policyName = version.collectionName;

				MessageBox.showBusy(controlCenter, ResourceManager
						.getResource("at.starting.atw"));
				new Thread(new Runnable() {
					public void run() {
						new AuditQueryWizard(false, newTask, null,
								controlCenter.resourceManager, databaseName);
					}
				}).start();

			} else if (source == popupMenus.addVersionMenuItem) {
				final DefaultMutableTreeNode databaseNameNode = (DefaultMutableTreeNode) selectedNode
						.getParent().getParent().getParent();
				final DefaultMutableTreeNode auditNameNode = (DefaultMutableTreeNode) selectedNode
						.getParent();
				final String databaseName = (String) databaseNameNode
						.getUserObject();
				final String auditName = (String) auditNameNode.getUserObject();
				final Version version = new Version(databaseName, auditName,
						null);
				new NewEntryWizard(NewEntryWizard.TYPE_NEW_VERSION,
						ResourceManager.getResource("at.add.new.version"),
						databaseName, controlCenter.resourceManager, version);
			} else if (source == popupMenus.deleteVersionMenuItem) {
			} else if (source == popupMenus.deleteAllPurposeMenuItem) {
				if (MessageBox.result(controlCenter, ResourceManager
						.getResource("label.confirm"), ResourceManager
						.getResource("at.sure.del.pur"), MessageBox.ICON_WARN) == MessageBox.BUTTON_YES)
					MessageBox.show(controlCenter, ResourceManager
							.getResource("label.message"),
							controlCenter.resourceManager.deleteAllEntries(
									getDatabaseName(selectedNode),
									ResourceManager.DELETE_ALL_PURPOSES),
							MessageBox.ICON_INFO);
			} else if (source == popupMenus.deleteAllRecipientMenuItem) {
				if (MessageBox.result(controlCenter, ResourceManager
						.getResource("label.confirm"), ResourceManager
						.getResource("at.sure.del.rec"), MessageBox.ICON_WARN) == MessageBox.BUTTON_YES)
					MessageBox.show(controlCenter, ResourceManager
							.getResource("label.message"),
							controlCenter.resourceManager.deleteAllEntries(
									getDatabaseName(selectedNode),
									ResourceManager.DELETE_ALL_RECIPIENTS),
							MessageBox.ICON_INFO);

			} else if (source == popupMenus.deleteAllAccessorMenuItem) {
				if (MessageBox.result(controlCenter, ResourceManager
						.getResource("label.confirm"), ResourceManager
						.getResource("at.sure.del.acc"), MessageBox.ICON_WARN) == MessageBox.BUTTON_YES)
					MessageBox.show(controlCenter, ResourceManager
							.getResource("label.message"),
							controlCenter.resourceManager.deleteAllEntries(
									getDatabaseName(selectedNode),
									ResourceManager.DELETE_ALL_ACCESSORS),
							MessageBox.ICON_INFO);
			} else if (source == popupMenus.definePurposeMenuItem) {
				new NewEntryWizard(NamePanel.PURPOSE, ResourceManager
						.getResource("at.add.new.pur"),
						(String) ((DefaultMutableTreeNode) selectedNode
								.getParent()).getUserObject(),
						controlCenter.resourceManager, null);
			} else if (source == popupMenus.defineRecipientMenuItem) {
				new NewEntryWizard(NamePanel.RECIPIENT, ResourceManager
						.getResource("at.add.new.rec"),
						(String) ((DefaultMutableTreeNode) selectedNode
								.getParent()).getUserObject(),
						controlCenter.resourceManager, null);
			} else if (source == popupMenus.defineAccessorMenuItem) {
				new NewEntryWizard(NamePanel.ACCESSOR, ResourceManager
						.getResource("at.add.new.acc"),
						(String) ((DefaultMutableTreeNode) selectedNode
								.getParent()).getUserObject(),
						controlCenter.resourceManager, null);
			}
		}

		public void mouseReleased(MouseEvent e) {
			if ((e.getButton() != MouseEvent.BUTTON1) && (selectedNode != null)) {
				final TreePath selectedPath = jTree.getSelectionPath();

				if (selectedPath != null) {
					final int pathCount = selectedPath.getPathCount();

					if (pathCount == 1) {
						popupMenus.databaseNodePopup.show(e.getComponent(), e
								.getX() + 10, e.getY() + 10);

						final MenuElement[] menuItems = popupMenus.databaseNodePopup
								.getSubElements();
						((JMenuItem) menuItems[0]).setEnabled(true);
						((JMenuItem) menuItems[1]).setEnabled(!Utility
								.isFullyExpanded(jTree));
						((JMenuItem) menuItems[2]).setEnabled(!Utility
								.isFullyCollapsed(jTree));
					} else if (pathCount == 2) {
						popupMenus.databaseNamePopup.show(e.getComponent(), e
								.getX() + 10, e.getY() + 10);
						final MenuElement[] menuItems = popupMenus.databaseNamePopup
								.getSubElements();
						final boolean hasBacklogTables = controlCenter.resourceManager
								.hasBacklogMetaData((String) selectedNode
										.getUserObject());
						((JMenuItem) menuItems[0])
								.setEnabled(!hasBacklogTables);
						((JMenuItem) menuItems[1]).setEnabled(hasBacklogTables);
						((JMenuItem) menuItems[2]).setEnabled(hasBacklogTables);
						((JMenuItem) menuItems[3]).setEnabled(hasBacklogTables);
						((JMenuItem) menuItems[5])
								.setEnabled(hasBacklogTables
										&& controlCenter.resourceManager
												.hasMetadataTables((String) selectedNode
														.getUserObject()));
					} else if (pathCount == 3) {

						if (selectedNode.getUserObject().toString().equals(
								ResourceManager.getResource("at.purposes"))) {
							popupMenus.purposePopup.show(e.getComponent(), e
									.getX() + 10, e.getY() + 10);
						} else if (selectedNode.getUserObject().toString()
								.equals(
										ResourceManager
												.getResource("at.recipients"))) {
							popupMenus.recipientPopup.show(e.getComponent(), e
									.getX() + 10, e.getY() + 10);
						} else if (selectedNode.getUserObject().toString()
								.equals(
										ResourceManager
												.getResource("at.accessors"))) {
							popupMenus.accessorPopup.show(e.getComponent(), e
									.getX() + 10, e.getY() + 10);
						} else if (selectedNode.getUserObject().toString()
								.equals(
										ResourceManager
												.getResource("at.backlogs"))) {
							popupMenus.deleteAllBacklogTablesMenuItem
									.setEnabled((controlCenter.resourceManager
											.getTablesWithBacklog(
													getDatabaseName(selectedNode))
											.size() > 0));
							popupMenus.backlogPopup.show(e.getComponent(), e
									.getX() + 10, e.getY() + 10);
						} else {
							popupMenus.auditPopup.show(e.getComponent(), e
									.getX() + 10, e.getY() + 10);
							MenuElement[] menuItems = popupMenus.auditPopup
									.getSubElements();
							final boolean hasAudits = selectedNode.isLeaf();

							((JMenuItem) menuItems[1]).setEnabled(!hasAudits);
						}
					} else if (pathCount == 4) {
						popupMenus.auditNamePopup.show(e.getComponent(), e
								.getX() + 10, e.getY() + 10);
					}

				}
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

		public void valueChanged(TreeSelectionEvent e) {
			selectedNode = (DefaultMutableTreeNode) jTree
					.getLastSelectedPathComponent();

			if (selectedNode != null) {
				selectionPath = jTree.getSelectionPath();

				if (selectionPath != null) {
					int path = selectionPath.getPathCount();

					// if (path == 6) { // i.e. looking at a task now
					if (path == 4) { // i.e. looking at a task now

						final DefaultMutableTreeNode databaseNode = (DefaultMutableTreeNode) selectedNode
								.getParent().getParent();
						final Version version = new Version(
								(String) databaseNode.getUserObject(),
								(String) selectedNode.getUserObject(), "1");

						auditor.changeRightComponent(
								Auditor.TASK_DISPLAY_PANEL, version);
					} else if (selectedNode.getUserObject().toString().equals(
							ResourceManager.getResource("at.backlogs"))) {
						final DefaultMutableTreeNode databaseNode = (DefaultMutableTreeNode) selectedNode
								.getParent();
						final Version version = new Version(
								(String) databaseNode.getUserObject(),
								(String) selectedNode.getUserObject(), "1");

						Vector versionInfo = new Vector(4, 2);
						versionInfo.add(databaseNode.getUserObject());
						auditor.changeRightComponent(
								Auditor.BACKLOGS_DISPLAY_PANEL, version);
					} else if (selectedNode.getUserObject().toString().equals(
							ResourceManager.getResource("at.purposes"))) {
						final DefaultMutableTreeNode databaseNode = (DefaultMutableTreeNode) selectedNode
								.getParent();
						final Version version = new Version(
								(String) databaseNode.getUserObject(),
								(String) selectedNode.getUserObject(), "1");

						Vector versionInfo = new Vector(4, 2);
						versionInfo.add(databaseNode.getUserObject());
						auditor.changeRightComponent(
								Auditor.PURPOSES_DISPLAY_PANEL, version);
					} else if (selectedNode.getUserObject().toString().equals(
							ResourceManager.getResource("at.recipients"))) {
						final DefaultMutableTreeNode databaseNode = (DefaultMutableTreeNode) selectedNode
								.getParent();
						final Version version = new Version(
								(String) databaseNode.getUserObject(),
								(String) selectedNode.getUserObject(), "1");

						Vector versionInfo = new Vector(4, 2);
						versionInfo.add(databaseNode.getUserObject());
						auditor.changeRightComponent(
								Auditor.RECIPIENTS_DISPLAY_PANEL, version);
					} else if (selectedNode.getUserObject().toString().equals(
							ResourceManager.getResource("at.accessors"))) {
						final DefaultMutableTreeNode databaseNode = (DefaultMutableTreeNode) selectedNode
								.getParent();
						final Version version = new Version(
								(String) databaseNode.getUserObject(),
								(String) selectedNode.getUserObject(), "1");

						Vector versionInfo = new Vector(4, 2);
						versionInfo.add(databaseNode.getUserObject());
						auditor.changeRightComponent(
								Auditor.ACCESSORS_DISPLAY_PANEL, version);
					} else
						auditor.changeRightComponent(Auditor.BLANK_PANEL);
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