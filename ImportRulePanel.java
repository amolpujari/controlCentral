package controlCenter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class ImportRulePanel extends NewWizardPanel {
	private Vector dataSelection;

	private Vector allRules;

	final private JCheckBoxList list = new JCheckBoxList();

	final private JComboBox versionBox = new JComboBox();

	final private NewWizard wiz;

	public ImportRulePanel(final NewWizard wiz, final Vector initialData,// rules
			final Version version,// policy name
			final Vector otherVersions, final Vector allRules) {
		dataSelection = initialData;
		this.wiz = wiz;

		final JLabel label = new JLabel(ResourceManager
				.getResource("irp.old.versions"));
		label.setBounds(6, 4, 80, 20);

		versionBox.setBounds(84, 4, 472, 20);

		final JScrollPane rightScrollPane = new JScrollPane(list,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		list.setBorder(BorderFactory.createLoweredBevelBorder());

		list.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent arg0) {
				updateSelection();
			}
		});

		final JPanel pan = new JPanel();
		pan.setBounds(2, 30, 556, 244);
		pan.add(rightScrollPane);
		pan.setLayout(null);
		rightScrollPane.setBounds(4, 20, 546, 190);

		pan.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(EtchedBorder.LOWERED), " "
				+ ResourceManager.getResource("label.rules") + " "));

		for (int i = 0; i < otherVersions.size(); i++)
			versionBox.addItem(otherVersions.get(i));

		this.allRules = allRules;

		versionBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				updateSelection();
				updateListData();
			}
		});

		if (versionBox.getItemCount() > 0)
			versionBox.setSelectedIndex(0);

		final JButton selectAllButton = new JButton(ResourceManager
				.getResource("label.select.all"));
		final JButton deselectAllButton = new JButton(ResourceManager
				.getResource("label.deselect.all"));

		selectAllButton.setBounds(337, 213, 100, 23);
		deselectAllButton.setBounds(442, 213, 100, 23);

		selectAllButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				list.checkAll();
				updateSelection();
			}
		});

		deselectAllButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				list.unCheckAll();
				updateSelection();
			}
		});

		pan.add(selectAllButton);
		pan.add(deselectAllButton);

		add(versionBox);
		add(label);
		add(pan);

		finished = true;
		wiz.setFinishEnabled(finished);
	}

	public void refresh() {
	}// nothing to do here

	public Vector getRuleSelection() {
		return dataSelection;
	}

	private void updateListData() {
		final String selectedVersion = (String) versionBox.getSelectedItem();
		final Vector data = new Vector(4, 2);
		for (int i = 0; i < allRules.size(); i++)
			if (((Rule) allRules.get(i)).version.equals(selectedVersion))
				data.add(allRules.get(i));

		final int[] checks = new int[data.size()];
		for (int i = 0; i < checks.length; i++)
			checks[i] = (dataSelection.contains(data.get(i))) ? 1 : 0;

		list.setListData(data);
		list.setChecks(checks);
	}

	private void updateSelection() {
		Object data[] = list.getCheckedData();

		for (int i = 0; i < data.length; i++)
			if (!dataSelection.contains(data[i]))
				dataSelection.add(data[i]);

		data = list.getUnCheckedData();

		for (int i = 0; i < data.length; i++)
			dataSelection.remove(data[i]);
	}
}
