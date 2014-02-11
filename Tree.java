package controlCenter;

import java.util.Enumeration;

import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

public abstract class Tree extends JScrollPane {
	protected ControlCenter controlCenter;

	protected JTree jTree;

	protected DefaultTreeModel treeModel;

	protected DefaultMutableTreeNode rootNode;

	protected DefaultMutableTreeNode commonDatabaseConnected(String databaseName) {
		final DefaultMutableTreeNode newChild = new DefaultMutableTreeNode(
				databaseName);

		if (rootNode.getChildCount() == 0)
			rootNode.insert(newChild, 0);
		else {
			final Enumeration enumeration = rootNode.children();
			boolean found = false;
			int i;
			for (i = 0; !found && enumeration.hasMoreElements(); i++) {
				final DefaultMutableTreeNode child = (DefaultMutableTreeNode) enumeration
						.nextElement();
				final String otherDatabaseName = (String) child.getUserObject();

				if (databaseName.compareTo(otherDatabaseName) <= 0)
					found = true;
			}
			if (found)
				rootNode.insert(newChild, i - 1);
			else
				rootNode.add(newChild);
		}

		/***********************************************************************
		 * Vector sortedVector = new Vector(4,2); Enumeration enumeration =
		 * rootNode.children(); while (enumeration.hasMoreElements())
		 * sortedVector.add((DefaultMutableTreeNode) enumeration.nextElement());
		 * 
		 * rootNode.removeAllChildren();
		 * 
		 * sortedVector.add(childNode); Collections.sort(sortedVector);
		 * 
		 * enumeration = sortedVector.elements(); while
		 * (enumeration.hasMoreElements()) rootNode.add((DefaultMutableTreeNode)
		 * enumeration.nextElement());
		 **********************************************************************/

		refreshDatabaseNameNode(newChild);
		return newChild;
	}

	public abstract void refreshDatabaseNameNode(
			DefaultMutableTreeNode databaseNode);

	public void refresh() {
		treeModel.reload();
		final DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel
				.getRoot();
		for (int i = 0; i < treeModel.getChildCount(root); i++) {
			final DefaultMutableTreeNode databaseNode = (DefaultMutableTreeNode) treeModel
					.getChild(root, i);
			refreshDatabaseNameNode(databaseNode);
		}
	}

	// might not need later
	protected DefaultMutableTreeNode construct() {
		rootNode = new DefaultMutableTreeNode("Databases");
		return rootNode;
	}

	public void makeVisible(TreePath treePath) {
		jTree.makeVisible(treePath);
	}

	public void databaseDisconnected(String databaseName) {
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel
				.getRoot();
		int numDatabaseConnected = treeModel.getChildCount(root);

		boolean databaseNodeFound = false;

		for (int i = 0; i < numDatabaseConnected && !databaseNodeFound; i++) {
			DefaultMutableTreeNode databaseNode = (DefaultMutableTreeNode) treeModel
					.getChild(root, i);
			if (databaseNode.getUserObject().equals(databaseName)) {
				databaseNodeFound = true;
				treeModel.removeNodeFromParent(databaseNode);
				treeModel.reload();
			}
		}
	}

	public void databaseConnected(String databaseName) {
		final DefaultMutableTreeNode newChild = commonDatabaseConnected(databaseName);
	}
	
	private boolean [] expansion;
	
	public void saveExpansion()
	{
		expansion = new boolean[jTree.getRowCount()];
		for(int i=0; i<expansion.length; i++)
		expansion[i] = jTree.isExpanded(i);
	}
	
	public void applyExpansion()
	{
		for(int i=0; i<expansion.length; i++)
			if(expansion[i])
			jTree.expandRow(i);
	}


}
