package controlCenter;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class ApplicationUsageDialog extends JDialog implements
		KeyChangedInformer {

	private ControlCenter controlCenter;

	private Vector applicationUsageVector;

	private ApplicationUsage selectedApplicationUsage;

	private ApplicationUsage selectedCombination;

	private Vector originalCombinations;

	private String databaseName; // database name for which this dialog is

	// working on

	private JList applicationList;

	private JList purposeList;

	private JList recipientList;

	private JList accessorList;

	private JList selectedCombinationList;

	private JButton createNewButton;

	private JButton cancelButton;

	private JButton okButton;

	private JButton addButton;

	private JButton deleteButton;

	private JTextField createNewTextField;
	
	private Vector appNames = new Vector(4,2);

	public ApplicationUsageDialog(ControlCenter controlCenter) {
		super(controlCenter, true);
		this.controlCenter = controlCenter;
		applicationUsageVector = new Vector(4, 2);

		Container contentPane = this.getContentPane();
		ActionAdapter actionAdapter = new ActionAdapter();
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
		setTitle(ResourceManager.getResource("label.au"));

		JLabel topLabel = new JLabel(ResourceManager
				.getResource("label.au.define"));
		topLabel.setHorizontalAlignment(JLabel.LEFT);
		JPanel labelPanel = new JPanel();
		labelPanel.setLayout(new FlowLayout());
		labelPanel.add(topLabel);

		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new FlowLayout());

		JPanel applicationNamePanel = new JPanel();
		applicationNamePanel
				.setBorder(new TitledBorder(ResourceManager.BORDER,ResourceManager
								.getResource("label.application")));
		applicationList = new JList();
		applicationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		applicationList.addListSelectionListener(actionAdapter);
		JScrollPane applicationNamePane = new JScrollPane(applicationList);
		applicationNamePane.setPreferredSize(new Dimension(
				ResourceManager.SCROLL_PANEL_DIMENSION_X,
				ResourceManager.SCROLL_PANEL_DIMENSION_Y));
		applicationNamePanel.add(applicationNamePane);
		centerPanel.add(applicationNamePanel);

		JPanel purposePanel = new JPanel();
		purposePanel.setBorder(new TitledBorder(ResourceManager.BORDER,ResourceManager.getResource("label.purpose")));
		purposeList = new JList();
		purposeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		purposeList.addListSelectionListener(actionAdapter);
		JScrollPane purposePane = new JScrollPane(purposeList);
		purposePane.setPreferredSize(new Dimension(
				ResourceManager.SCROLL_PANEL_DIMENSION_X,
				ResourceManager.SCROLL_PANEL_DIMENSION_Y));
		purposePanel.add(purposePane);
		centerPanel.add(purposePanel);

		JPanel accessorPanel = new JPanel();
		accessorPanel.setBorder(new TitledBorder(ResourceManager.BORDER,ResourceManager.getResource("label.accessor")));
		accessorList = new JList();
		accessorList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		accessorList.addListSelectionListener(actionAdapter);
		JScrollPane accessorPane = new JScrollPane(accessorList);
		accessorPane.setPreferredSize(new Dimension(
				ResourceManager.SCROLL_PANEL_DIMENSION_X,
				ResourceManager.SCROLL_PANEL_DIMENSION_Y));
		accessorPanel.add(accessorPane);
		centerPanel.add(accessorPanel);

		JPanel recipientPanel = new JPanel();
		recipientPanel.setBorder(new TitledBorder(ResourceManager.BORDER,ResourceManager.getResource("label.recipient")));
		recipientList = new JList();
		recipientList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		recipientList.addListSelectionListener(actionAdapter);
		JScrollPane recipientPane = new JScrollPane(recipientList);
		recipientPane.setPreferredSize(new Dimension(
				ResourceManager.SCROLL_PANEL_DIMENSION_X,
				ResourceManager.SCROLL_PANEL_DIMENSION_Y));
		recipientPanel.add(recipientPane);
		centerPanel.add(recipientPanel);

		JPanel createNewPanel = new JPanel();
		createNewPanel.setLayout(new FlowLayout());
		createNewButton = new JButton(ResourceManager
				.getResource("label.create"));
		createNewButton.addActionListener(actionAdapter);
		createNewTextField = new JTextField(ResourceManager.TEXT_FIELD_SIZE);
		createNewPanel.add(new JLabel(ResourceManager
				.getResource("label.au.new.application.name")));
		createNewPanel.add(createNewTextField);
		createNewPanel.add(createNewButton);

		createNewTextField.addActionListener(actionAdapter);
		createNewTextField.addKeyListener(new CommonKeyListener(this));

		// 01/05/2007 jk lcase ok
		// 				CommonKeyListener.CHECK_CAPS));

		JPanel selectionButtonPanel = new JPanel();
		selectionButtonPanel.setLayout(new FlowLayout());
		// selectionButtonPanel.setBorder(BorderFactory.createEtchedBorder());
		addButton = new JButton(ResourceManager
				.getResource("label.add"));
		addButton.addActionListener(actionAdapter);
		deleteButton = new JButton(ResourceManager
				.getResource("label.delete"));
		deleteButton.addActionListener(actionAdapter);
		selectionButtonPanel.add(addButton);
		selectionButtonPanel.add(deleteButton);

		JPanel combinationPanel = new JPanel();
		combinationPanel.setBorder(new TitledBorder(ResourceManager.BORDER,ResourceManager
						.getResource("label.au.selected.combinations")));
		selectedCombinationList = new JList();
		selectedCombinationList
				.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		selectedCombinationList.addListSelectionListener(actionAdapter);
		JScrollPane combinationPane = new JScrollPane(selectedCombinationList);
		combinationPane.setPreferredSize(new Dimension(
				4 * ResourceManager.SCROLL_PANEL_DIMENSION_X,
				ResourceManager.SCROLL_PANEL_DIMENSION_Y / 2));
		combinationPanel.add(combinationPane);

		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new FlowLayout());
		// bottomPanel.setBorder(BorderFactory.createEtchedBorder());
		okButton = new JButton(ResourceManager
				.getResource("label.ok"));
		okButton.addActionListener(actionAdapter);
		cancelButton = new JButton(ResourceManager
				.getResource("label.cancel"));
		cancelButton.addActionListener(actionAdapter);
		bottomPanel.add(okButton);
		bottomPanel.add(cancelButton);

		labelPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		centerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		createNewPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		selectionButtonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		combinationPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		bottomPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

		contentPane.add(labelPanel);
		contentPane.add(centerPanel);
		contentPane.add(createNewPanel);
		contentPane.add(selectionButtonPanel);
		contentPane.add(combinationPanel);
		contentPane.add(bottomPanel);

		pack();
		setModal(true);
		setResizable(false);
		updateButtons();
		setLocationRelativeTo(null);

	}

	public Vector getApplicationUsageVector() {
		return applicationUsageVector;
	}

	public void showDialog(String databaseName) {
		this.databaseName = databaseName;
		this.applicationUsageVector.clear();

		final Vector applications = controlCenter.resourceManager
		.getApplicationNames(databaseName);

		appNames = controlCenter.resourceManager
		.getApplicationNames(databaseName);

		final Vector purposes = controlCenter.resourceManager.getEntries(
				databaseName, ResourceManager
						.getResource("metadata.table.purposes"));
		final Vector accessors = controlCenter.resourceManager.getEntries(
				databaseName, ResourceManager
						.getResource("metadata.table.accessors"));
		final Vector recipients = controlCenter.resourceManager.getEntries(
				databaseName, ResourceManager
						.getResource("metadata.table.recipients"));
		originalCombinations = controlCenter.resourceManager
				.getApplicationUsage(databaseName);
		final Vector selectedCombinations = controlCenter.resourceManager
				.getApplicationUsage(databaseName);

		Collections.sort(applications);
		Collections.sort(purposes);
		Collections.sort(accessors);
		Collections.sort(recipients);
		Collections.sort(originalCombinations);
		Collections.sort(selectedCombinations);

		applicationList.setListData(applications);
		purposeList.setListData(purposes);
		accessorList.setListData(accessors);
		recipientList.setListData(recipients);
		selectedCombinationList.setListData(selectedCombinations);

		updateButtons();
		setVisible(true);
	}

	class ActionAdapter implements ActionListener, ListSelectionListener {
		public void valueChanged(ListSelectionEvent e) {

			final Object source = e.getSource();

			selectedCombination = (ApplicationUsage) selectedCombinationList
					.getSelectedValue();

			if (selectedApplicationUsage != null) {
				if (source == applicationList)
					selectedApplicationUsage.name = (String) applicationList
							.getSelectedValue();
				else if (source == purposeList)
					selectedApplicationUsage.purpose = (String) purposeList
							.getSelectedValue();
				else if (source == accessorList)
					selectedApplicationUsage.accessor = (String) accessorList
							.getSelectedValue();
				else if (source == recipientList)
					selectedApplicationUsage.recipient = (String) recipientList
							.getSelectedValue();
			}

			updateButtons();
		}

		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == createNewButton) {
				final String newApplicationName = createNewTextField.getText()
						.trim();

				//if (!newApplicationName.equals("")) 
				//{

					final ListModel oldApplicationList = applicationList
							.getModel();
					final DefaultListModel newApplicationList = new DefaultListModel();

					final Vector vector = new Vector(4, 2);
					vector.add(newApplicationName);

					for (int i = 0; i < oldApplicationList.getSize(); i++)
						vector.add(oldApplicationList.getElementAt(i));

					Collections.sort(vector);
					for (int i = 0; i < vector.size(); i++)
						newApplicationList.addElement(vector.get(i));
					applicationList.setModel(newApplicationList);

					createNewTextField.setText("");
					
					appNames.add(newApplicationName);
					
				//} 
//				else {
//					hide();
//					MessageBox.show(controlCenter, "Error",
//							"Cannot create new entry.",MessageBox.ICON_ERR);
//					// new NotificationDialog("Error", "Cannot create new
//					// entry.", controlCenter,databaseName);
//				}
			} else if (e.getSource() == okButton) {
				final ListModel combinationListModel = selectedCombinationList
						.getModel();
				boolean hasError = false;

				for (int i = 0; (i < combinationListModel.getSize())
						&& !hasError; i++) {
					final ApplicationUsage combination = (ApplicationUsage) combinationListModel
							.getElementAt(i);

					if (!combination.isEmpty())
						applicationUsageVector.add(combination);
					else {
						hasError = true;

						String text = ResourceManager.getResource("label.error.processing.app")+" "
								+ combination.toStringVerbose() + ".";

						hide();
						MessageBox.show(controlCenter, ResourceManager.getResource("label.error"), text,MessageBox.ICON_ERR);
					}
				}

				if (!hasError) {
					hide();

					final Vector applicationUsageVector = controlCenter.policyEditor
							.getPolicyTree().getApplicationUsageDialog()
							.getApplicationUsageVector();

					String comment = "";
					for (int i = 0; i < applicationUsageVector.size(); i++) {
						final ApplicationUsage applicationUsage = (ApplicationUsage) applicationUsageVector
								.get(i);
						comment += applicationUsage.toStringDetails() + "<br>";
					}

					// if(MessageBox.result(controlCenter,"Confirm",question+"<br><br>"+comment)==MessageBox.BUTTON_YES)
					{
						MessageBox.show(controlCenter, ResourceManager.getResource("label.error"),
								controlCenter.resourceManager
										.updateApplicationUsage(databaseName,
												applicationUsageVector),MessageBox.ICON_ERR);
					}
				}
			} else if (e.getSource() == cancelButton) {
				// setVisible(false);
				createNewTextField.setText("");
				hide();
			} else if (e.getSource() == addButton) {

				//
				// Retrieve the old elements from the list.
				//
				final ListModel listModel = selectedCombinationList.getModel();
				final Vector combinations = new Vector(4, 2);
				for (int i = 0; i < listModel.getSize(); i++) {
					final ApplicationUsage combination = (ApplicationUsage) listModel
							.getElementAt(i);
					combinations.add(combination);
				}

				//
				// Add new element.
				//
				combinations.add(selectedApplicationUsage);

				//
				// Sort updated list.
				//
				Collections.sort(combinations);

				//
				// Assign updated list.
				//
				selectedCombinationList.setListData(combinations);
			} else if (e.getSource() == deleteButton) {
				// Retrieve the old elements from the list.
				final ListModel listModel = selectedCombinationList.getModel();
				final Vector combinations = new Vector(4, 2);

				for (int i = 0; i < listModel.getSize(); i++)
					combinations.add(listModel.getElementAt(i));

				// Remove selected element.
				combinations.remove(selectedCombination);

				// Assign updated list.
				selectedCombinationList.setListData(combinations);
			}
			updateButtons();
		}
	}
	
	

	/**
	 * Should be called after each interaction and when a dialog is displayed
	 * for the first time. Checks which buttons should be enabled and disabled.
	 */
	private void updateButtons() {

		deleteButton
				.setEnabled((selectedCombinationList.getModel().getSize() != 0)
						&& !selectedCombinationList.isSelectionEmpty());
		cancelButton.setEnabled(true);
		
		createNewButton.setEnabled(!createNewTextField.getText().trim().equals(
				"") &&(!appNames.contains(createNewTextField.getText().trim())) );
		
		final Vector selectedCombinations = new Vector(4, 2);

		ListModel listModel = selectedCombinationList.getModel();

		for (int i = 0; i < listModel.getSize(); i++)
			selectedCombinations.add(listModel.getElementAt(i));

		okButton.setEnabled(!selectedCombinations.equals(originalCombinations));

		final boolean allSelected = ((applicationList.getModel().getSize() != 0) && !applicationList
				.isSelectionEmpty())
				&& ((purposeList.getModel().getSize() != 0) && !purposeList
						.isSelectionEmpty())
				&& ((accessorList.getModel().getSize() != 0) && !accessorList
						.isSelectionEmpty())
				&& ((recipientList.getModel().getSize() != 0) && !recipientList
						.isSelectionEmpty());

		if (allSelected) {
			// Retrieve the old elements from the list.
			listModel = selectedCombinationList.getModel();
			final TreeSet combinations = new TreeSet();

			for (int i = 0; i < listModel.getSize(); i++)
				combinations.add(listModel.getElementAt(i));

			// Construct a new application usage.
			selectedApplicationUsage = controlCenter.resourceManager
					.newApplicationUsage((String) applicationList
							.getSelectedValue(), (String) purposeList
							.getSelectedValue(), (String) accessorList
							.getSelectedValue(), (String) recipientList
							.getSelectedValue(), true);

			addButton.setEnabled(!combinations
					.contains(selectedApplicationUsage));
		} else
			addButton.setEnabled(false);
	}

	public void checkInput() {
		final String value = createNewTextField.getText().trim();

		final ListModel listModel = applicationList.getModel();
		final Vector applications = new Vector(4, 2);

		for (int i = 0; i < listModel.getSize(); i++)
			applications.add(listModel.getElementAt(i));

		createNewButton.setEnabled(!value.equals("")
				&& !applications.contains(value));
	}

}
