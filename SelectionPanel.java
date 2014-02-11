package controlCenter;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.LinkedList;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import Utility;

/**
 * this is a selection panel commonly used in new wizard. it could be used any
 * where.
 */
public class SelectionPanel extends JPanel {

	private final JList leftList = new JList();

	private final JList rightList;

	private final JPanel leftPanel = new JPanel();

	private final JPanel rightPanel = new JPanel();

	private final JPanel buttonPanel = new JPanel();

	private final JButton rightButton = new JButton(">");

	private final JButton leftButton = new JButton("<");

	private final JButton right2Button = new JButton(">>");

	private final JButton left2Button = new JButton("<<");

	private Vector leftData;

	private Vector rightData;

	private final boolean isTables;

	private final SelectionListener informer;

	private int[] checks;

	private Vector coloredData = new Vector();

	/**
	 * 
	 */
	public SelectionPanel(final int[] checks,// in case of check boxes
			final SelectionListener informer, final String availableTitle,
			final String selectedTitle, final Vector initialLeftData,
			final Vector initialRightData, final boolean isTables, final boolean singletonSelection) {
		super();

		if (singletonSelection)
		    leftList.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
		rightList = (checks == null) ? new JList() : new JCheckBoxList();

		this.isTables = isTables;
		this.informer = informer;
		this.checks = checks;

		buttonPanel.setLayout(null);

		right2Button.setBounds(5, 65, 60, 26);
		rightButton.setBounds(5, 97, 60, 26);
		leftButton.setBounds(5, 129, 60, 26);
		left2Button.setBounds(5, 161, 60, 26);

		buttonPanel.add(right2Button);
		buttonPanel.add(rightButton);
		buttonPanel.add(leftButton);
		buttonPanel.add(left2Button);
		
		leftList.setName("leftList");
		rightList.setName("rightList");

		setLayout(null);

		final JScrollPane leftScrollPane = new JScrollPane(leftList,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		leftScrollPane.setBounds(4, 18, 150, 200);
		leftList.setBounds(0, 0, 150, 200);

		final JScrollPane rightScrollPane = new JScrollPane(rightList,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		rightScrollPane.setBounds(4, 18, 150, 200);
		rightList.setBounds(0, 0, 150, 200);

		leftPanel.setBounds(8, 5, 159, 223);
		buttonPanel.setBounds(167, 5, 70, 223);
		rightPanel.setBounds(237, 5, 159, 223);

		setBounds(5, 5, 400, 227);

		leftPanel.setLayout(null);
		rightPanel.setLayout(null);

		leftList.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent arg0) {
			}

			public void mouseEntered(MouseEvent arg0) {
			}

			public void mouseExited(MouseEvent arg0) {
			}

			public void mousePressed(MouseEvent arg0) {
			}

			// 01/03/2007 jk
			// fix for single entity selection
			//
			public void mouseReleased(MouseEvent arg0) {
				if (arg0.isControlDown()) {
					rightButton
							.setEnabled(leftList.getSelectedValues().length > 0 && (leftList.getSelectionMode() != DefaultListSelectionModel.SINGLE_SELECTION || rightList.getModel().getSize() == 0));
				}
			}
		});

		rightList.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent arg0) {
			}

			public void mouseEntered(MouseEvent arg0) {
			}

			public void mouseExited(MouseEvent arg0) {
			}

			public void mousePressed(MouseEvent arg0) {
			}

			public void mouseReleased(MouseEvent arg0) {
				if (arg0.isControlDown()) {
					leftButton
							.setEnabled(rightList.getSelectedValues().length > 0);
				}
			}
		});

		// 01/03/2007 jk
		// fix for single entity selection
		//
		leftList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent arg0) {
				rightButton.setEnabled(leftList.getSelectionMode() != DefaultListSelectionModel.SINGLE_SELECTION || rightList.getModel().getSize() == 0);
			}
		});

		rightList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent arg0) {
				leftButton.setEnabled(true);
			}
		});

		leftPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(EtchedBorder.LOWERED), " " + availableTitle
				+ " "));
		rightPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(EtchedBorder.LOWERED), " " + selectedTitle
				+ " "));

		leftList.setBorder(BorderFactory.createLoweredBevelBorder());
		rightList.setBorder(BorderFactory.createLoweredBevelBorder());

		setBorder(BorderFactory.createLoweredBevelBorder());
		setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

		leftPanel.add(leftScrollPane);
		rightPanel.add(rightScrollPane);

		add(leftPanel);
		add(buttonPanel);
		add(rightPanel);

		leftList.setCellRenderer(new CustomRenderer());

		rightButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				rightButtonAction();
			}
		});

		leftButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				leftButtonAction();
			}
		});

		right2Button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				for (int i = 0; i < leftData.size(); i++)
					rightData.add(leftData.get(i));

				leftData.removeAllElements();
				setListData();
				setButtons();
				informer.selectionChanged(rightData);

			}
		});

		left2Button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				for (int i = 0; i < rightData.size(); i++)
					leftData.add(rightData.get(i));

				rightData.removeAllElements();

				setButtons();

				informer.selectionChanged(rightData);

			}
		});

		rightData = initialRightData;
		setAvailableData(initialLeftData);

		if (checks != null)
			((JCheckBoxList) rightList).setChecks(checks);
	}

	class CustomRenderer extends DefaultListCellRenderer {
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {

			super.getListCellRendererComponent(list, value, index, isSelected,
					cellHasFocus);

			if (coloredData.contains(value))
				setForeground(Color.RED);

			return this;
		}
	}

	private void rightButtonAction() {
		int[] selection = leftList.getSelectedIndices();
		Vector dataMoved = new Vector(4, 2);
		Object data;// this could be Strings or ColumnDescriptor

		for (int i = 0; i < selection.length; i++) {
			data = leftData.get(selection[i]);
			rightData.add(data);
			dataMoved.add(data);
		}

		for (int i = 0; i < dataMoved.size(); i++)
			leftData.remove(dataMoved.get(i));

		setButtons();

		informer.selectionChanged(rightData);
	}

	private void leftButtonAction() {
		int[] selection = rightList.getSelectedIndices();
		Vector dataMoved = new Vector(4, 2);
		Object data;

		int[] oldChecks = new int[0];

		if (rightList instanceof JCheckBoxList)
			oldChecks = ((JCheckBoxList) rightList).getChecks();

		for (int i = 0; i < selection.length; i++) {
			data = rightData.get(selection[i]);
			leftData.add(data);
			dataMoved.add(data);
		}

		for (int i = 0; i < dataMoved.size(); i++)
			rightData.remove(dataMoved.get(i));

		setButtons();

		if (checks != null) {
			try {
				LinkedList oldList = new LinkedList();

				for (int i = 0; i < oldChecks.length; i++)
					oldList.add(i, new Integer(oldChecks[i]));

				for (int i = 0; i < selection.length; i++)
					oldList.remove(selection[i] - i);

				int[] newChecks = new int[rightData.size()];

				for (int i = 0; i < newChecks.length; i++)
					newChecks[i] = ((Integer) oldList.get(i)).intValue();

				((JCheckBoxList) rightList).setChecks(newChecks);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		informer.selectionChanged(rightData);
	}

	/**
	 * @return
	 */
	public Vector getSelectedData() {
		return rightData;
	}

	public Vector getUnselectedData() {
		return leftData;
	}

	public int[] getChecks() {
		return ((JCheckBoxList) rightList).getChecks();
	}

	/**
	 * @param data
	 */
	public void setAvailableData(final Vector data) {
		for (int i = 0; i < rightData.size(); i++)
			if (data.contains(rightData.get(i)))
				data.remove(rightData.get(i));

		leftData = Utility.sortVector(data);

		setButtons();
	}

	public void setSelection(final Vector data) {
		rightData = Utility.sortVector(data);
	}

	private Vector getStringData(final Vector data) {
		final Vector strData = new Vector(4, 2);

		for (int i = 0; i < data.size(); i++)
			strData.add(data.get(i).toString());

		return strData;
	}

	/**
	 * 
	 */
	private void setButtons() {
		setListData();

		rightButton.setEnabled(false);
		leftButton.setEnabled(false);

		// 01/03/2007 jk
		// fix for single entity selection
		//
		right2Button.setEnabled(!leftData.isEmpty() && leftList.getSelectionMode() != DefaultListSelectionModel.SINGLE_SELECTION);
		left2Button.setEnabled(!rightData.isEmpty());
	}

	public void clearSelection() {
		rightData = new Vector();
		rightData.removeAllElements();
		setButtons();
		informer.selectionChanged(rightData);
	}

	/**
	 * @param list
	 * @param data
	 */
	private void setLeftListData() {
		final Vector data = getStringData(leftData);
		leftList.setListData(data);
	}

	private void setRightListData() {
		final Vector data = getStringData(rightData);

		if (checks != null) {
			// get updated checks
			checks = ((JCheckBoxList) rightList).getChecks();
			((JCheckBoxList) rightList).setChecks(checks);
		}

		rightList.setListData(data);
	}

	private void setListData() {
		setLeftListData();
		setRightListData();
	}

	public void setEnabled(final boolean arg0) {
		super.setEnabled(arg0);
		leftList.setEnabled(arg0);
		rightList.setEnabled(arg0);

		if (arg0) {

		} else {
			leftButton.setEnabled(false);
			left2Button.setEnabled(false);
			rightButton.setEnabled(false);
			right2Button.setEnabled(false);
		}

	}

	public void setColoredData(Vector coloredData) {
		this.coloredData = Utility.sortVector(coloredData);
	}
}
