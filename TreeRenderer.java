package controlCenter;

import java.awt.Component;
import java.util.Hashtable;
import java.util.StringTokenizer;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

public class TreeRenderer extends DefaultTreeCellRenderer {
	private final Hashtable icons = new Hashtable();

	private final ResourceManager rm;

	public TreeRenderer(ResourceManager rm) {
		final StringTokenizer stringTokenizer = new StringTokenizer(rm
				.getResource("tree.icons"), ",");
		String iconString = "";
		while (stringTokenizer.hasMoreTokens()) {
			iconString = stringTokenizer.nextToken().trim();
			icons.put(iconString, new ImageIcon(rm.getResource("tree.icon."
					+ iconString)));
		}
		this.rm = rm;
	}

	public Component getTreeCellRendererComponent(JTree tree, Object object,
			boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {

		super.getTreeCellRendererComponent(tree, object, sel, expanded, leaf,
				row, hasFocus);

		ImageIcon icon = (ImageIcon) icons
				.get(((String) ((DefaultMutableTreeNode) object)
						.getUserObject()).replaceAll(" ", ""));

		if (icon == null) {
			icon = (ImageIcon) icons
					.get((String) ((DefaultMutableTreeNode) ((DefaultMutableTreeNode) object)
							.getParent()).getUserObject()
							+ "Children");
		}

		String str = "";

		try {
			str = (String) ((DefaultMutableTreeNode) ((DefaultMutableTreeNode) object)
					.getParent()).getUserObject();
		} catch (NullPointerException e) {

		}

		if (str.equals("Policies")) {
			final DefaultMutableTreeNode policiesNode = (DefaultMutableTreeNode) ((DefaultMutableTreeNode) object)
					.getParent();
			final DefaultMutableTreeNode dbNode = (DefaultMutableTreeNode) policiesNode
					.getParent();
			final String databaseName = (String) dbNode.getUserObject();
			final String policyName = (String) ((DefaultMutableTreeNode) object)
					.getUserObject();
			final Version version = rm.getPolicyVersionType(new Version(
					databaseName, policyName, null));

			if (version.type == Version.TYPE_COMPLEX)
				icon = (ImageIcon) icons.get("PoliciesChildrenComplex");
			else
				icon = (ImageIcon) icons.get("PoliciesChildrenSimple");
		} else if (str.equals("Versions")) {
			final DefaultMutableTreeNode versionNode = (DefaultMutableTreeNode) object;
			final DefaultMutableTreeNode policyNode = (DefaultMutableTreeNode) versionNode
					.getParent().getParent();
			final DefaultMutableTreeNode dbNode = (DefaultMutableTreeNode) policyNode
					.getParent().getParent();
			final String databaseName = (String) dbNode.getUserObject();
			final String policyName = (String) policyNode.getUserObject();
			final String versionName = (String) versionNode.getUserObject();
			final Version version = rm.getPolicyVersionType(new Version(
					databaseName, policyName, versionName));

			if (version.type == Version.TYPE_COMPLEX) {
				if (version.enabled == 1)
					icon = (ImageIcon) icons.get("VersionsChildrenEnabled");
				else
					icon = (ImageIcon) icons.get("VersionsChildrenDisabled");
			} else {
				if (version.enabled == 1)
					icon = (ImageIcon) icons.get("VersionsChildrenActivated");
				else
					icon = (ImageIcon) icons
							.get("VersionsChildrenNotActivated");

			}
		}

		if (icon != null)
			setIcon(icon);
		// setToolTipText("tool tip text here");

		return this;
	}
}
