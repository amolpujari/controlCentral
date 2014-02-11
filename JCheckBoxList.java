package controlCenter;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

public class JCheckBoxList extends JList {
	protected static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

	private JCheckBox checkBoxes[];

	private Object rawData[];

	private int[] checks;

	public JCheckBoxList() {
		setCellRenderer(new CellRenderer());

		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				int index = locationToIndex(e.getPoint());

				if (e.getPoint().x < 16)
					if (index != -1) {
						JCheckBox checkbox = (JCheckBox) getModel()
								.getElementAt(index);
						checkbox.setSelected(!checkbox.isSelected());
						repaint();
					}
			}
		});

		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}

	public void setChecks(final int[] checks) {
		this.checks = checks;
		applyChecks();
	}

	private void applyChecks() {
		if (checkBoxes != null)
			for (int i = 0; i < checks.length; i++)
				if (checkBoxes[i] != null)
					checkBoxes[i].setSelected((checks[i] == 1) ? true : false);
	}

	public void setListData(Vector arg0) {
		final int size = arg0.size();

		final Vector data = new Vector(4, 2);

		checkBoxes = new JCheckBox[size];
		rawData = new Object[size];

		for (int i = 0; i < arg0.size(); i++) {
			checkBoxes[i] = new JCheckBox(arg0.get(i).toString(),
					((checks == null)) ? false : ((checks.length <= i) ? false
							: ((checks[i] == 1) ? true : false)));

			data.add(checkBoxes[i]);
			rawData[i] = arg0.get(i);
		}

		super.setListData(data);
	}

	public void checkAll() {
		final int[] selectionIndexes = new int[checks.length];
		for (int i = 0; i < checks.length; i++) {
			selectionIndexes[i] = i;
			checks[i] = 1;
		}
		applyChecks();
		setSelectedIndices(selectionIndexes);
	}

	public void unCheckAll() {
		final int[] selectionIndexes = new int[checks.length];
		for (int i = 0; i < checks.length; i++) {
			selectionIndexes[i] = i;
			checks[i] = 0;
		}
		applyChecks();
		setSelectedIndices(selectionIndexes);
	}

	public int[] getChecks() {
		final int checks[] = new int[(checkBoxes == null) ? 0
				: checkBoxes.length];

		if (checkBoxes != null)
			for (int i = 0; i < checkBoxes.length; i++)
				checks[i] = checkBoxes[i].isSelected() ? 1 : 0;

		return checks;
	}

	public Object[] getCheckedData() {
		final int[] checks = getChecks();

		int checkedCount = 0;

		for (int i = 0; i < checks.length; i++)
			checkedCount += checks[i];

		final Object[] checkedData = new Object[checkedCount];

		int j = 0;

		for (int i = 0; i < checks.length; i++)
			if (checks[i] == 1) {
				checkedData[j] = rawData[i];
				j++;
			}

		return checkedData;
	}

	public Object[] getUnCheckedData() {
		final int[] checks = getChecks();

		int checkedCount = checks.length;

		for (int i = 0; i < checks.length; i++)
			checkedCount -= checks[i];

		final Object[] unCheckedData = new Object[checkedCount];

		int j = 0;

		for (int i = 0; i < checks.length; i++)
			if (checks[i] == 0) {
				unCheckedData[j] = rawData[i];
				j++;
			}

		return unCheckedData;
	}

	protected class CellRenderer implements ListCellRenderer {
		public Component getListCellRendererComponent(final JList list,
				final Object value, final int index, final boolean isSelected,
				final boolean cellHasFocus) {
			JCheckBox checkbox = (JCheckBox) value;

			checkbox.setBackground(isSelected ? getSelectionBackground()
					: getBackground());
			checkbox.setForeground(isSelected ? getSelectionForeground()
					: getForeground());

			checkbox.setEnabled(isEnabled());
			checkbox.setFont(getFont());
			checkbox.setFocusPainted(false);
			checkbox.setBorderPainted(true);
			checkbox
					.setBorder(isSelected ? UIManager
							.getBorder("List.focusCellHighlightBorder")
							: noFocusBorder);
			return checkbox;
		}
	}
}