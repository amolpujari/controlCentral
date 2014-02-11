package controlCenter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EtchedBorder;

import org.apache.log4j.Logger;

class ConditionPanel extends NewWizardPanel implements KeyChangedInformer {
	private Hashtable tableInfo;

	private final JTextArea condArea;

	private final JComboBox tableBox = new JComboBox();

	private final JComboBox columnBox = new JComboBox();

	private final Logger logger = Logger.getLogger(getClass().getName());

	private final JTextField valText;

	private final JButton addCondButton;

	private final JCheckBox notCheck;

	private final JCheckBox openCheck;

	private final JCheckBox closeCheck;

    public ConditionPanel(final NewWizard wiz, final String initialCondition, final Hashtable tableInfo) {
		super();
		logger.debug("started initiating condition panel");

		this.wiz = wiz;
		this.tableInfo = tableInfo;
		finished = true;

		final JPanel predicatePanel = new JPanel();
		predicatePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(EtchedBorder.LOWERED), " "
				+ ResourceManager.getResource("cnp.predicates") + " "));
		predicatePanel.setLayout(null);
		predicatePanel.setBounds(5, 5, 240, 270);
		final JRadioButton andButton = new JRadioButton("AND", true);
		final JRadioButton orButton = new JRadioButton("OR");
		final ButtonGroup group = new ButtonGroup();
		group.add(andButton);
		group.add(orButton);
		andButton.setBounds(15, 30, 50, 23);
		orButton.setBounds(70, 30, 50, 23);
		notCheck = new JCheckBox("NOT");
		notCheck.setBounds(130, 30, 50, 23);
		openCheck = new JCheckBox(" (");
		closeCheck = new JCheckBox(" )");
		openCheck.setBounds(15, 64, 50, 23);
		closeCheck.setBounds(75, 64, 50, 23);
		final JLabel tableLabel = new JLabel(ResourceManager
				.getResource("cnp.table"));
		final JLabel columnLabel = new JLabel(ResourceManager
				.getResource("cnp.column"));
		final JLabel opLabel = new JLabel(ResourceManager
				.getResource("cnp.opr"));
		final JLabel valLabel = new JLabel(ResourceManager
				.getResource("cnp.value"));
		tableLabel.setBounds(10, 105, 60, 20);
		columnLabel.setBounds(10, 135, 60, 20);
		opLabel.setBounds(10, 165, 60, 20);
		valLabel.setBounds(10, 195, 60, 20);
		final JComboBox opBox = new JComboBox();
		valText = new JTextField();
		tableBox.setBounds(65, 105, 160, 20);
		columnBox.setBounds(65, 135, 160, 20);
		opBox.setBounds(65, 165, 160, 20);
		valText.setBounds(65, 195, 160, 20);
		addCondButton = new JButton(ResourceManager.getResource("label.add"));
		addCondButton.setEnabled(false);
		addCondButton.setBounds(154, 234, 70, 23);
		predicatePanel.add(andButton);
		predicatePanel.add(orButton);
		predicatePanel.add(notCheck);
		predicatePanel.add(openCheck);
		predicatePanel.add(closeCheck);
		predicatePanel.add(tableLabel);
		predicatePanel.add(columnLabel);
		predicatePanel.add(opLabel);
		predicatePanel.add(valLabel);
		predicatePanel.add(tableBox);
		predicatePanel.add(columnBox);
		predicatePanel.add(opBox);
		predicatePanel.add(valText);
		predicatePanel.add(addCondButton);
		final JPanel ccPanel = new JPanel();
		ccPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(EtchedBorder.LOWERED), " "
				+ ResourceManager.getResource("cnp.cond") + " "));
		ccPanel.setLayout(null);
		ccPanel.setBounds(250, 5, 302, 270);
		condArea = new JTextArea(15, 50);
		condArea.setText(layout(initialCondition));
		condArea.setBorder(BorderFactory.createLoweredBevelBorder());
		final JScrollPane condScrollPane = new JScrollPane(condArea,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		condScrollPane.setBounds(4, 20, 294, 246);
		condArea.setBounds(0, 0, 294, 246);
		ccPanel.add(condScrollPane);
		add(predicatePanel);
		add(ccPanel);

		tableBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				refreshColumnBox();
			}
		});

		updateTableList();

		final String[] ops = { "=", "<>", "<", ">", "<=", ">=" };

		for (int i = 0; i < ops.length; i++)
			opBox.addItem(ops[i]);

		addCondButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {

				final int len = condArea.getText().trim().length();
				String text = "\n ";

				if (len > 4) // text is a valid condition so need to append
				// AND/OR
				{
					if (andButton.isSelected())
						text += " AND"
								+ ((notCheck.isSelected()) ? " NOT ( " : " ");
					else
						text += " OR"
								+ ((notCheck.isSelected()) ? " NOT ( " : " ");
				}

				text += tableBox.getSelectedItem().toString() + "."
						+ columnBox.getSelectedItem().toString();
				text += " " + opBox.getSelectedItem().toString() + " "
						+ valText.getText().trim()
						+ ((notCheck.isSelected() && (len > 4)) ? " ) " : " ");
				text = condArea.getText().trim() + text;

				if (openCheck.isSelected())
					text = " ( " + text;

				if (closeCheck.isSelected())
					text = text + " ) ";

				condArea.setText(layout(text));
			}
		});

		valText.addKeyListener(new CommonKeyListener(this));

		openCheck.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				closeCheck.setSelected(false);
			}
		});
		closeCheck.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				openCheck.setSelected(false);
			}
		});
	}

	private String layout(String input) {
		String output = "";
		int nesting = 0;
		String previousToken = "";

		StringTokenizer tokenizer = new StringTokenizer(input);

		while (tokenizer.hasMoreElements()) {
			final String token = tokenizer.nextToken();

			if (token.equals("AND") || token.equals("OR"))
				output += token + "\n";
			else if (token.equals("(")) {
				output += indentation(nesting);
				output += token + "\n";
				nesting++;
			} else if (token.equals(")")) {
				nesting--;
				output += "\n";
				output += indentation(nesting);
				output += token + " ";
			} else if (token.equals("=") || token.equals("<>")
					|| token.equals("<") || token.equals("<=")
					|| token.equals(">") || token.equals(">=")) {
				// No indentation.
				output += token + " ";
			} else if (previousToken.equals("=") || previousToken.equals("<>")
					|| previousToken.equals("<") || previousToken.equals("<=")
					|| previousToken.equals(">") || previousToken.equals(">=")) {
				// No indentation.
				output += token + " ";
			}
			// else if (previousToken.equals("NOT")) {
			// // No indentation.
			// output += token + " ";
			// }
			else {
				output += indentation(nesting) + token + " ";
			}
			previousToken = token;
		}
		return output;
	}

	private String indentation(int nesting) {
		String output = "";
		for (int i = 0; i < nesting; i++)
			output += "    ";
		return output;
	}

	public String getCondition() {
		String cond = condArea.getText().trim();

		if (cond.length() < 4)
			cond = "";
		else
			cond = " ( " + cond + " ) ";

		return layout(cond);
	}

	/**
	 * Get called when this panel get activated
	 */
	public void refresh() {
		try {
			if (wiz instanceof AuditQueryWizard) {
				tableInfo = ((AuditQueryWizard) wiz).getUpdatedList();
				updateTableList();
			}

		} catch (NullPointerException e) {
			/*
			 * this is the case when initially not all the panels are
			 * constructed but those which being construct they would call this
			 * function and it would throw null pointer exception in case other
			 * panels ane not constrcuted yet
			 */
		}

		wiz.setMessage(ResourceManager.getResource("cnp.msg.1"), true);
	}

	private void updateTableList() {
		tableBox.removeAllItems();
		Enumeration e = tableInfo.keys();
		while (e.hasMoreElements())
			tableBox.addItem(e.nextElement().toString());

		if (tableBox.getItemCount() > 0)
			tableBox.setSelectedIndex(0);

		refreshColumnBox();
	}

	private void refreshColumnBox() {
		columnBox.removeAllItems();

		try {
			final Vector columns = (Vector) tableInfo.get(tableBox
					.getSelectedItem());
			for (int i = 0; i < columns.size(); i++) {
				String val = columns.get(i).toString();
				val = val.substring(val.lastIndexOf(".") + 1);
				columnBox.addItem(val);
			}
		} catch (NullPointerException e) {
			/*
			 * this is the case when initially not all the panels are
			 * constructed but those which being construct they would call this
			 * function and it would throw null pointer exception in case other
			 * panels ane not constrcuted yet
			 */
		}
	}

	public void checkInput() {
		final String text = valText.getText().trim();
		addCondButton.setEnabled(false);
		wiz.setMessage("");
		if (text.length() > 32) {
			wiz.setMessage(ResourceManager.getResource("cnp.msg.2"));
			wiz.setErrorIcon();
		} else if (text.length() > 0) {
			addCondButton.setEnabled(true);
		}
	}

}
