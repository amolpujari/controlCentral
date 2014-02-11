package controlCenter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

public class InfoPanel extends JPanel {
	public final static int TYPE_AUDIT_QUERY = -1;

	public final static int TYPE_AUDIT_TASK = 0;

	public final static int TYPE_POLICY_RULE = 1;

	private final int type;

	private final DisplayPanel pan;

	public InfoPanel(final int type) {
		this.type = type;
		pan = new DisplayPanel(type);
	}

	public void reset() {
		pan.setVisible(false);
	}

	public void setRule(final Rule rule) {
		if (rule == null)
			return;

		pan.columnsTextField.setText(" "
				+ ResourceManager.getResource("ip.columns"));

		pan.mainTextField.setText("      "
				+ ResourceManager.getResource("ip.details.rule"));
		pan.nameValueTextField.setText(rule.policyName
				+ " / "
				+ rule.version
				+ " / "
				+ rule.name
				+ ((rule.versioningType == Version.TYPE_SIMPLE) ? " "
						+ ResourceManager.getResource("ip.rule.type.simple")
						: " "
								+ ResourceManager
										.getResource("ip.rule.type.complx")));
		pan.columnsArea.setText(rule.getColumnsString());
		pan.purposeArea.setText(getDisplayable(rule.purposes));
		pan.recipientArea.setText(getDisplayable(rule.recipients));
		pan.acessorArea.setText(getDisplayable(rule.accessors));
		pan.conditionArea.setText(rule.condition);
		pan.entityArea.setText(rule.entities.toString());

		pan.pan2.setPreferredSize(new Dimension(700, 400));

		setLayout(new BorderLayout());
		add(pan, BorderLayout.CENTER);
		pan.setVisible(true);
	}

	public void setTask(final Task task) {
		if (task == null)
			return;

		pan.columnsTextField.setText(" "
				+ ResourceManager.getResource("ip.columns"));

		pan.mainTextField.setText("      "
				+ ResourceManager.getResource("ip.details.task"));
		pan.nameValueTextField.setText(task.policyName + " / " + task.name);
		pan.columnsArea.setText(task.getColumnsString());
		pan.purposeArea
				.setText((task.purposes.size() == 1 && (((String) task.purposes
						.get(0)).indexOf("%")) >= 0) ? task
						.getDisplayableEntry((String) task.purposes.get(0))
						: getDisplayable(task.purposes));
		pan.recipientArea
				.setText((task.recipients.size() == 1 && (((String) task.recipients
						.get(0)).indexOf("%")) >= 0) ? task
						.getDisplayableEntry((String) task.recipients.get(0))
						: getDisplayable(task.recipients));
		pan.acessorArea
				.setText((task.accessors.size() == 1 && (((String) task.accessors
						.get(0)).indexOf("%")) >= 0) ? task
						.getDisplayableEntry((String) task.accessors.get(0))
						: getDisplayable(task.accessors));
		pan.conditionArea.setText(task.condition);
		pan.timeValueTextField.setText(task.begin + " --> " + task.end);
		pan.sqlArea.setText(task.getSQLAuditQuery());

		pan.pan2.setPreferredSize(new Dimension(700, 480));

		setLayout(new BorderLayout());
		add(pan, BorderLayout.CENTER);
		pan.setVisible(true);
	}

	public void setQuery(final Query query) {
		if (query == null)
			return;

		pan.columnsTextField.setText(" "
				+ ResourceManager.getResource("ip.iso"));

		pan.mainTextField.setText("      "
				+ ResourceManager.getResource("ip.details.query") + " "
				+ query.verdict);
		pan.nameValueTextField.setText(" " + query.name);
		pan.columnsArea.setText(" " + query.isolationText);
		pan.purposeArea.setText(" " + query.purpose);
		pan.recipientArea.setText(" " + query.recipient);
		pan.acessorArea.setText(" " + query.accessor);
		pan.timeValueTextField.setText(" " + query.begin + " --> " + query.end);
		pan.sqlArea.setText(" " + query.text);
		pan.resultAria.setText(query.result);

		pan.pan2.setPreferredSize(new Dimension(700, 380));

		setLayout(new BorderLayout());
		add(pan, BorderLayout.CENTER);
		pan.setVisible(true);
	}

	private String getDisplayable(final Vector data) {
		StringBuffer str = new StringBuffer();

		for (int i = 0; i < data.size(); i++)
			str.append(data.get(i) + ", ");

		str.delete(str.length() - 2, str.length());

		return str.toString();
	}
}

class DisplayPanel extends JPanel {
	// common to policy and audit
	final public JLabel mainTextField = new JLabel(" "
			+ ResourceManager.getResource("ip.details"));

	final public JTextField columnsTextField = new JTextField(" "
			+ ResourceManager.getResource("ip.columns"));

	// policy related
	public JTextField entityTextField;

	// audit related
	public JTextField timeTextField;

	public JTextField sqlTextField;

	// -----
	public JTextArea resultAria;

	final public JTextField nameValueTextField = new JTextField();

	final public JTextArea columnsArea = new JTextArea();

	final public JTextArea purposeArea = new JTextArea();

	final public JTextArea recipientArea = new JTextArea();

	final public JTextArea acessorArea = new JTextArea();

	final public JTextArea conditionArea = new JTextArea();

	// policy related
	public JTextArea entityArea;

	public JScrollPane entityPane;

	// audit related
	public JTextField timeValueTextField;

	public JTextArea sqlArea;

	public JScrollPane sqlPane;

	// Query related
	public JLabel qresultLabel;

	private final int type;

	private final static Color COLOR_BACKGROUND = new Color(250, 250, 252);

	private final static Color COLOR_BORDER = new Color(230, 230, 232);

	private final static Color COLOR_LABEL_FORE = new Color(69, 69, 140);

	private Query query;

	public JPanel queryResultPanel;

	public final JPanel pan2 = new JPanel();

	public JPanel pan3 = new JPanel();

	public JPanel pan4 = new JPanel();

	public DisplayPanel(final int type) {
		this.type = type;
		initiate();
	}

	public void initiate() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		mainTextField.setFont(new Font("Default", Font.BOLD, 12));

		final JTextField nameTextField = new JTextField(" "
				+ ResourceManager.getResource("ip.id"));
		nameTextField.setBounds(4, 4, 150, 30);
		columnsTextField.setBounds(4, 35, 150, 50);
		final JTextField purposeTextField = new JTextField(" "
				+ ResourceManager.getResource("ip.pur"));
		purposeTextField.setBounds(4, 86, 150, 50);
		final JTextField recipientTextField = new JTextField(" "
				+ ResourceManager.getResource("ip.rec"));
		recipientTextField.setBounds(4, 137, 150, 50);
		final JTextField acessorTextField = new JTextField(" "
				+ ResourceManager.getResource("ip.acc"));
		acessorTextField.setBounds(4, 188, 150, 50);

		// --
		nameValueTextField.setBounds(0, 4, 500, 30);
		final JScrollPane sp1 = new JScrollPane(columnsArea);
		sp1.setBounds(0, 35, 500, 50);
		final JScrollPane sp2 = new JScrollPane(purposeArea);
		sp2.setBounds(0, 86, 500, 50);
		final JScrollPane sp3 = new JScrollPane(recipientArea);
		sp3.setBounds(0, 137, 500, 50);
		final JScrollPane sp4 = new JScrollPane(acessorArea);
		sp4.setBounds(0, 188, 500, 50);

		columnsArea.setAutoscrolls(true);
		purposeArea.setAutoscrolls(true);
		recipientArea.setAutoscrolls(true);
		acessorArea.setAutoscrolls(true);

		columnsArea.setBackground(getBackground());
		purposeArea.setBackground(getBackground());
		recipientArea.setBackground(getBackground());
		acessorArea.setBackground(getBackground());

		columnsArea.setEditable(false);
		purposeArea.setEditable(false);
		recipientArea.setEditable(false);
		acessorArea.setEditable(false);

		nameTextField.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
		columnsTextField
				.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
		purposeTextField
				.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
		recipientTextField.setBorder(BorderFactory
				.createLineBorder(COLOR_BORDER));
		acessorTextField
				.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
		nameValueTextField.setBorder(BorderFactory
				.createLineBorder(COLOR_BORDER));
		columnsArea.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
		purposeArea.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
		recipientArea.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
		acessorArea.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
		conditionArea.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));

		columnsArea.setBackground(Color.WHITE);
		purposeArea.setBackground(Color.WHITE);
		recipientArea.setBackground(Color.WHITE);
		acessorArea.setBackground(Color.WHITE);

		final JPanel pan1 = new JPanel();

		pan1.setLayout(new BorderLayout());

		pan1.setMinimumSize(new Dimension(400, 40));
		pan1.setMaximumSize(new Dimension(9000, 40));
		pan1.setPreferredSize(new Dimension(600, 40));

		pan2.setMinimumSize(new Dimension(400, 280));
		pan2.setMaximumSize(new Dimension(9000, 480));
		pan2.setPreferredSize(new Dimension(700, 480));

		mainTextField.setMaximumSize(new Dimension(200, 20));
		mainTextField.setPreferredSize(new Dimension(200, 20));
		pan1.add("West", mainTextField);

		pan3.setLayout(null);
		pan4.setLayout(null);
		pan3.setBorder(null);
		pan4.setBorder(null);

		pan3.setMinimumSize(new Dimension(154, 600));
		pan3.setMaximumSize(new Dimension(154, 600));
		pan3.setPreferredSize(new Dimension(154, 600));

		pan4.setMinimumSize(new Dimension(510, 600));
		pan4.setMaximumSize(new Dimension(9450, 600));
		pan4.setPreferredSize(new Dimension(510, 600));

		pan2.add(pan3);
		pan2.add(pan4);

		pan2.setBackground(Color.white);
		pan3.setBackground(Color.white);
		pan4.setBackground(Color.white);

		add(pan1);
		final JScrollPane sp = new JScrollPane(pan2,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		add(sp);

		pan3.add(nameTextField);
		pan3.add(columnsTextField);
		pan3.add(purposeTextField);
		pan3.add(recipientTextField);
		pan3.add(acessorTextField);

		pan4.add(nameValueTextField);
		pan4.add(sp1);
		pan4.add(sp2);
		pan4.add(sp3);
		pan4.add(sp4);

		if (type != InfoPanel.TYPE_AUDIT_QUERY) {
			final JTextField conditionTextField = new JTextField(" "
					+ ResourceManager.getResource("ip.cond"));
			conditionTextField.setBounds(4, 239, 150, 100);
			final JScrollPane sp5 = new JScrollPane(conditionArea);
			sp5.setBounds(0, 239, 500, 100);
			conditionArea.setAutoscrolls(true);
			conditionArea.setBackground(getBackground());
			conditionArea.setEditable(false);
			conditionTextField.setBorder(BorderFactory
					.createLineBorder(COLOR_BORDER));
			conditionArea.setBackground(COLOR_BACKGROUND);
			pan3.add(conditionTextField);
			pan4.add(sp5);
			sp5.setBorder(null);
			conditionTextField.setBackground(COLOR_BACKGROUND);
			conditionTextField.setEnabled(false);
		} else {

			conditionArea.setVisible(false);
			qresultLabel = new JLabel(ResourceManager
					.getResource("ip.result.query"));
			qresultLabel.setForeground(Color.BLUE);
			qresultLabel.setMaximumSize(new Dimension(100, 20));
			qresultLabel.setPreferredSize(new Dimension(100, 20));
			qresultLabel.setCursor(Cursor
					.getPredefinedCursor(Cursor.HAND_CURSOR));
			qresultLabel.addMouseListener(new MouseListener() {
				public void mouseClicked(MouseEvent arg0) {

					if (queryResultPanel.isVisible()) {
						queryResultPanel.setVisible(false);
						pan3.setVisible(true);
						pan4.setVisible(true);
						qresultLabel.setText(ResourceManager
								.getResource("ip.result.query"));
					} else {
						pan3.setVisible(false);
						pan4.setVisible(false);
						queryResultPanel.setVisible(true);
						qresultLabel.setText(ResourceManager
								.getResource("ip.query.details"));
					}
				}

				public void mouseEntered(MouseEvent arg0) {
				}

				public void mouseExited(MouseEvent arg0) {
				}

				public void mousePressed(MouseEvent arg0) {
				}

				public void mouseReleased(MouseEvent arg0) {
				}
			});

			prepareQueryResultPanel();
			pan1.add("East", qresultLabel);
			pan2.add(queryResultPanel);
		}

		if (type == InfoPanel.TYPE_POLICY_RULE) {
			entityTextField = new JTextField(" "
					+ ResourceManager.getResource("ip.ent"));
			entityArea = new JTextArea();
			entityPane = new JScrollPane(entityArea);

			entityTextField.setBounds(4, 340, 150, 50);
			entityPane.setBounds(0, 340, 500, 50);

			entityArea.setAutoscrolls(true);
			entityArea.setBackground(getBackground());
			entityTextField.setBorder(BorderFactory
					.createLineBorder(COLOR_BORDER));
			entityArea.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));

			entityArea.setEditable(false);

			pan4.add(entityPane);
			pan3.add(entityTextField);

			entityPane.setBorder(null);
			entityArea.setBackground(Color.WHITE);

			entityArea.setBackground(COLOR_BACKGROUND);

			entityTextField.setBackground(COLOR_BACKGROUND);
			entityTextField.setEnabled(false);

		} else// if(type==InfoPanel.TYPE_AUDIT_TASK) ||
		// type==InfoPanel.TYPE_AUDIT_QUERY
		{
			timeTextField = new JTextField(" "
					+ ResourceManager.getResource("ip.intv"));
			sqlTextField = new JTextField(" "
					+ ResourceManager.getResource("ip.sql"));
			timeValueTextField = new JTextField();
			sqlArea = new JTextArea();
			sqlPane = new JScrollPane(sqlArea);

			timeTextField.setBounds(4, 340, 150, 30);
			sqlTextField.setBounds(4, 371, 150, 100);
			timeValueTextField.setBounds(0, 340, 500, 30);
			sqlPane.setBounds(0, 371, 500, 100);

			sqlArea.setAutoscrolls(true);
			sqlArea.setBackground(getBackground());

			timeTextField.setBorder(BorderFactory
					.createLineBorder(COLOR_BORDER));
			sqlTextField
					.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
			timeValueTextField.setBorder(BorderFactory
					.createLineBorder(COLOR_BORDER));
			sqlArea.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));

			sqlArea.setEditable(false);

			pan3.add(timeTextField);
			pan3.add(sqlTextField);
			pan4.add(timeValueTextField);
			pan4.add(sqlPane);

			sqlPane.setBorder(null);
			sqlArea.setBackground(Color.WHITE);

			sqlArea.setBackground(COLOR_BACKGROUND);
			timeValueTextField.setBackground(COLOR_BACKGROUND);

			sqlTextField.setBackground(COLOR_BACKGROUND);
			sqlTextField.setEnabled(false);
			timeTextField.setBackground(COLOR_BACKGROUND);
			timeTextField.setEnabled(false);

			if (type == InfoPanel.TYPE_AUDIT_QUERY) {
				timeTextField.setBounds(4, 240, 150, 30);
				sqlTextField.setBounds(4, 271, 150, 100);
				timeValueTextField.setBounds(0, 240, 500, 30);
				sqlPane.setBounds(0, 271, 500, 100);
			}
		}

		sp1.setBorder(null);
		sp2.setBorder(null);
		sp3.setBorder(null);
		sp4.setBorder(null);
		setBackground(Color.WHITE);

		mainTextField.setForeground(COLOR_LABEL_FORE);

		nameTextField.setBackground(COLOR_BACKGROUND);
		columnsTextField.setBackground(COLOR_BACKGROUND);
		purposeTextField.setBackground(COLOR_BACKGROUND);
		recipientTextField.setBackground(COLOR_BACKGROUND);
		acessorTextField.setBackground(COLOR_BACKGROUND);
		nameValueTextField.setBackground(COLOR_BACKGROUND);

		nameTextField.setEnabled(false);
		columnsTextField.setEnabled(false);
		purposeTextField.setEnabled(false);
		recipientTextField.setEnabled(false);
		acessorTextField.setEnabled(false);
		nameValueTextField.setEnabled(false);

		columnsArea.setBackground(COLOR_BACKGROUND);
		purposeArea.setBackground(COLOR_BACKGROUND);
		recipientArea.setBackground(COLOR_BACKGROUND);
		acessorArea.setBackground(COLOR_BACKGROUND);
		conditionArea.setBackground(COLOR_BACKGROUND);
	}

	private void prepareQueryResultPanel() {
		queryResultPanel = new JPanel();
		queryResultPanel.setLayout(null);
		queryResultPanel.setBorder(null);
		queryResultPanel.setMinimumSize(new Dimension(400, 280));
		queryResultPanel.setMaximumSize(new Dimension(9000, 480));
		queryResultPanel.setPreferredSize(new Dimension(700, 480));
		resultAria = new JTextArea();
		resultAria.setAutoscrolls(true);
		resultAria.setBackground(getBackground());
		resultAria.setEditable(false);
		resultAria.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
		resultAria.setBackground(COLOR_BACKGROUND);
		final JScrollPane sp = new JScrollPane(resultAria);
		sp.setBounds(20, 4, 655, 367);
		sp.setBorder(null);
		queryResultPanel.add(sp);
		queryResultPanel.setBackground(Color.WHITE);
		queryResultPanel.setVisible(false);
	}

	public void setQuery(Query query) {
		this.query = query;
	}
}
