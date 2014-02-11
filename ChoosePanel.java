package controlCenter;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;

import org.apache.log4j.Logger;

class ChoosePanel extends NewWizardPanel implements SelectionListener,
		KeyChangedInformer {
	public static final int AUDIT = -2;

	public static final int POLICY = -1;

	public static final int PURPOSES = 0;

	public static final int RECIPIENTS = 1;

	public static final int ACCESSORS = 2;

	public static final int SELECT_ENTITIES = 3;

	public static final int BACKLOG = 4;

	public static final int ADD_ENTITIES = 5;

	private final SelectionPanel selectionPanel;

	private final int type;

	private final int baseType; // AUDIT or POLICY

	private String table;

	private JTextField newText;

	private JTextField wildcardText;

	private JButton addButton;

	private String item;

	private Vector items;

	private Vector allItems;

	private JComboBox predicatesBox;

	private final String[] predicates = {
			ResourceManager.getResource("cp.predicates.1"),
			ResourceManager.getResource("cp.predicates.2"),
			ResourceManager.getResource("cp.predicates.3"),
			ResourceManager.getResource("cp.predicates.4") };

	private JTextField predicateText;

	private JCheckBox usePrdicateCheckBox;

	private JLabel usePrdicateLabel;

	private String predicate = "%";

	private final Logger logger = Logger.getLogger(getClass().getName());

	private JLabel defineEntitiesButton;

	public ChoosePanel(final NewWizard wiz, final int baseType, final int type,
			Vector selected, final String databaseName,
			final ResourceManager resourceManager) {
		super();
		this.wiz = wiz;
		this.baseType = baseType;
		this.type = type;
		boolean singletonSelection = false;

		int xDiff = 0;
		int yDiff = 0;

		switch (type) {
		case PURPOSES:
			item = ResourceManager.getResource("cp.item.pur");
			table = ResourceManager.getResource("metadata.table.purposes");
			items = resourceManager.getEntries(databaseName, table);
			allItems = resourceManager.getEntries(databaseName, table);
			break;
		case RECIPIENTS:
			item = ResourceManager.getResource("cp.item.rec");
			table = ResourceManager.getResource("metadata.table.recipients");
			items = resourceManager.getEntries(databaseName, table);
			allItems = resourceManager.getEntries(databaseName, table);
			break;
		case ACCESSORS:
			item = ResourceManager.getResource("cp.item.acc");
			table = ResourceManager.getResource("metadata.table.accessors");
			items = resourceManager.getEntries(databaseName, table);
			allItems = resourceManager.getEntries(databaseName, table);
			break;
		case SELECT_ENTITIES:
		    singletonSelection = true;
			item = ResourceManager.getResource("cp.item.entitie");
			table = ResourceManager.getResource("metadata.table.entities");
			items = resourceManager.getEntries(databaseName, table);
			allItems = resourceManager.getEntries(databaseName, table);
			defineEntitiesButton = new JLabel(ResourceManager
					.getResource("cp.define.entities"));
			defineEntitiesButton.setForeground(Color.BLUE);
			defineEntitiesButton.setBounds(440, 240, 100, 20);
			defineEntitiesButton.setCursor(Cursor
					.getPredefinedCursor(Cursor.HAND_CURSOR));
			defineEntitiesButton.addMouseListener(new MouseListener() {
				public void mouseClicked(MouseEvent arg0) {
					new NewEntryWizard(ChoosePanel.ADD_ENTITIES,
							ResourceManager
									.getResource("cp.select.entity.tables"),
							databaseName, resourceManager, null);
					items = resourceManager.getEntries(databaseName, table);

					final Vector selected = selectionPanel.getSelectedData();
					final Vector newSelection = new Vector(4, 2);

					for (int i = 0; i < selected.size(); i++)
					    if (items.contains(selected.get(i)))
						newSelection.add(selected.get(i));
					
					selectionPanel.setSelection(newSelection);
					selectionPanel.setAvailableData(items);
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

			add(defineEntitiesButton);
			break;
		case BACKLOG:
			item = ResourceManager.getResource("cp.item.backlog");
			xDiff = 165;
			yDiff = 20;
			table = ResourceManager.getResource("metadata.table.purposes");
			items = resourceManager.getConnectedDatabaseTables(databaseName);
			allItems = resourceManager.getConnectedDatabaseTables(databaseName);
			break;
		case ADD_ENTITIES:
			item = ResourceManager.getResource("cp.item.table");
			xDiff = 165;
			yDiff = 20;
			table = ResourceManager.getResource("metadata.table.entities");
			items = resourceManager.getNonEntities(databaseName);
			allItems = resourceManager.getNonEntities(databaseName);
			selected = resourceManager.getEntities(databaseName);
		}

		logger.debug("started initiating choose panel:" + item);

		selectionPanel = new SelectionPanel(null, this, " "
				+ ResourceManager.getResource("label.available") + " " + item
				+ "s ", ResourceManager.getResource("label.selected") + " "
				+ item + "s ", items, selected, false, singletonSelection);

		selectionPanel.setBounds(161 - xDiff, 0 + yDiff, 400, 227);
		selectionPanel.setBorder(null);
		add(selectionPanel);

		selectionChanged(selected);

		if (type < 3) {
			final JPanel panel = new JPanel();

			newText = new JTextField();
			addButton = new JButton(ResourceManager.getResource("label.add"));
			final JLabel newLabel = new JLabel(ResourceManager
					.getResource("label.add.new")
					+ " " + item + ": ");
			addButton.setEnabled(false);

			newText.addFocusListener(new FocusListener() {
				public void focusGained(FocusEvent arg0) {
					if (newText.getText().trim().length() < 1)
						wiz.setMessage(ResourceManager
								.getResource("label.please.enter.the")
								+ " " + item);
				}

				public void focusLost(FocusEvent arg0) {
				}
			});

			newLabel.setBounds(10, 8, 110, 20);
			newText.setBounds(120, 10, 340, 20);
			addButton.setBounds(464, 8, 70, 23);

			panel.setBorder(BorderFactory
					.createEtchedBorder(EtchedBorder.LOWERED));
			panel.setLayout(null);

			panel.setBounds(7, 230, getWidth() - 10, 40);

			panel.add(newLabel);
			panel.add(newText);
			panel.add(addButton);

			add(panel);

			addButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent arg0) {
					resourceManager.addEntry(databaseName, table, newText
							.getText());
					wiz.setMessage(ResourceManager.getResource("label.entry")
							+ " "
							+ newText.getText()
							+ " "
							+ ResourceManager
									.getResource("label.added.successfully"));
					newText.setText("");
					items = resourceManager.getEntries(databaseName, table);
					allItems = resourceManager.getEntries(databaseName, table);
					selectionPanel.setAvailableData(items);
					addButton.setEnabled(false);
				}
			});

			newText.addKeyListener(new CommonKeyListener(this));

			final JPanel panel2 = new JPanel();
			panel2.setBorder(BorderFactory
					.createEtchedBorder(EtchedBorder.LOWERED));
			panel2.setLayout(null);
			panel2.setBounds(7, 13, 155, 60);

			final JLabel wildcardLabel = new JLabel(" "
					+ ResourceManager.getResource("label.filter") + " " + item
					+ "s");
			wildcardText = new JTextField("*");

			wildcardLabel.setBounds(10, 5, 120, 20);
			wildcardText.setBounds(10, 30, 130, 20);

			panel2.add(wildcardLabel);
			panel2.add(wildcardText);

			add(panel2);

			wildcardText.addKeyListener(new CommonKeyListener(this));

			if (baseType == AUDIT) {
				final JPanel panel3 = new JPanel();
				panel3.setBorder(BorderFactory
						.createEtchedBorder(EtchedBorder.LOWERED));
				panel3.setLayout(null);
				panel3.setBounds(7, 80, 155, 146);
				add(panel3);
				usePrdicateCheckBox = new JCheckBox();
				usePrdicateCheckBox.setText(ResourceManager
						.getResource("cp.user.predicate"));
				usePrdicateCheckBox.setBounds(5, 5, 110, 20);
				panel3.add(usePrdicateCheckBox);
				predicatesBox = new JComboBox();
				for (int i = 0; i < predicates.length; i++)
					predicatesBox.addItem(predicates[i]);
				predicatesBox.setBounds(9, 30, 138, 20);
				panel3.add(predicatesBox);
				predicatesBox.setSelectedIndex(0);
				predicateText = new JTextField();
				predicateText.setBounds(9, 60, 138, 20);
				panel3.add(predicateText);
				usePrdicateLabel = new JLabel();
				usePrdicateLabel.setBounds(9, 74, 138, 70);
				panel3.add(usePrdicateLabel);
				usePrdicateLabel.setText(ResourceManager
						.getResource("cp.predicate.label")
						+ " \"%\"</html>");

				usePrdicateCheckBox.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						predicatesListRefreshed();
					}
				});

				predicatesBox.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						predecateChanged();
					}
				});

				predicateText.addKeyListener(new CommonKeyListener(this));

				if (selected.size() == 1) {
					if (((String) selected.get(0)).indexOf("%") >= 0) {
						usePrdicateCheckBox.setSelected(true);
						final String predicateStr = (String) selected.get(0);
						predicate = predicateStr;

						int index = predicate.indexOf("%");
						if (index > 0)
							predicatesBox.setSelectedIndex(1);// starts with
						else {
							if (predicate.length() == 1) {
								predicatesBox.setSelectedIndex(0);// select
								// all
								predicateText.setEnabled(false);
							} else {
								index = predicate.substring(1).lastIndexOf("%");
								if (index == -1)
									predicatesBox.setSelectedIndex(2);// ends
								// with
								else
									predicatesBox.setSelectedIndex(3);// contains
							}
						}
						predicate = predicateStr;
						predicateText.setText(predicate.replaceAll("%", ""));
						usePrdicateLabel.setText(ResourceManager
								.getResource("cp.predicate.label")
								+ " \"" + predicate + "\"</html>");
						selectionPanel.setSelection(new Vector());
						predicatesListRefreshed();
					}
				}
			}
		}
	}

	public Vector getSelection() {
		if (baseType == AUDIT && usePrdicateCheckBox != null
				&& usePrdicateCheckBox.isSelected()) {
			final Vector data = new Vector();
			data.add(predicate);
			return data;
		} else
			return selectionPanel.getSelectedData();
	}

	public Vector getNonSelection() {
		return selectionPanel.getUnselectedData();
	}

	public void selectionChanged(Vector selection) {
		if (type > 3) {
			finished = true;
			return;
		}

		if (baseType == AUDIT)
			if (usePrdicateCheckBox != null && usePrdicateCheckBox.isSelected()) {
				finished = true;
				wiz.panelRefreshed(-1);
				wiz.setMessage("", true);
				return;
			}

		if (selection.isEmpty()) {
			finished = false;
			wiz.setFinishEnabled(false);
			wiz.setMessage(ResourceManager
					.getResource("label.please.enter.the")
					+ " " + item + "s", false);
		} else {
			finished = true;
			wiz.panelRefreshed(-1);
			wiz.setMessage("", true);
		}
	}

	/**
	 * Get called when this panel get activated
	 */
	public void refresh() {
		checkInput();
	}

	public void checkInput() {
		selectionChanged(selectionPanel.getSelectedData());


		if (type > 3) {
			finished = true;
			return;
		}

		if (baseType == AUDIT)
			if (usePrdicateCheckBox != null && usePrdicateCheckBox.isSelected()) {
				predecateChanged();
				if (predicateText.getText().trim().length() > 24) {
					wiz.setMessage(ResourceManager
							.getResource("cp.predicate.too.long"), false);
					wiz.setErrorIcon();
				} else
					wiz.setMessage("");
			}

		if (type < 3) {
			final String text = newText.getText().trim();
			addButton.setEnabled(false);
			if (text.length() > 32) {
				wiz
						.setMessage(ResourceManager
								.getResource("cp.entry.too.long"));
				wiz.setErrorIcon();
			} else if (text.length() > 0) {
				if (allItems.contains(text))
					wiz.setMessage(ResourceManager.getResource("cp.duplicate")
							+ " " + table + "!");
				else
					addButton.setEnabled(true);
			}
		}


		// apply filter
		Vector data = new Vector(4, 2);
		String str = "";
		String filter = wildcardToRegex(wildcardText.getText().trim());

		for (int i = 0; i < items.size(); i++) {
			try {
				str = items.get(i).toString();

				if (Pattern.matches(filter, str))
					data.add(str);
			} catch (PatternSyntaxException e) {
				e.printStackTrace();
			}
		}

		selectionPanel.setAvailableData(data);
		predicatesListRefreshed();
	}

	public String wildcardToRegex(String wildcard) {
		StringBuffer s = new StringBuffer(wildcard.length());
		s.append('^');
		for (int i = 0, is = wildcard.length(); i < is; i++) {
			char c = wildcard.charAt(i);
			switch (c) {
			case '*':
				s.append(".*");
				break;
			case '?':
				s.append(".");
				break;
			// escape special regexp-characters
			case '(':
			case ')':
			case '[':
			case ']':
			case '$':
			case '^':
			case '.':
			case '{':
			case '}':
			case '|':
			case '\\':
				s.append("\\");
				s.append(c);
				break;
			default:
				s.append(c);
				break;
			}
		}
		s.append('$');
		return (s.toString());
	}

	private void predecateChanged() {
		final int index = predicatesBox.getSelectedIndex();
		switch (index) {
		case 0:// select all
			predicateText.setText("");
			predicateText.setEnabled(false);
			predicate = "%";
			break;
		case 1:// starts with
			predicateText.setEnabled(true);
			predicate = predicateText.getText().trim() + "%";
			break;
		case 2:// ends with
			predicateText.setEnabled(true);
			predicate = "%" + predicateText.getText().trim();
			break;
		case 3:// contains
			predicateText.setEnabled(true);
			predicate = "%" + predicateText.getText().trim() + "%";
			break;
		default://
			break;
		}

		usePrdicateLabel.setText(ResourceManager
				.getResource("cp.predicate.label")
				+ " \"" + predicate + "\"</html>");
	}

	private void predicatesListRefreshed() {
		if (usePrdicateCheckBox == null)
			return;

		final boolean checked = usePrdicateCheckBox.isSelected();
		wildcardText.setEnabled(!checked);
		selectionPanel.setEnabled(!checked);
		predicatesBox.setEnabled(checked);
		predicateText
				.setEnabled((predicatesBox.getSelectedIndex() == 0) ? false
						: checked);

		finished = checked;

		if (finished) {
			wiz.panelRefreshed(-1);
			wiz.setMessage("", true);
		} else
			selectionChanged(selectionPanel.getSelectedData());
	}

	public void setColoredData(Vector coloredData) {
		selectionPanel.setColoredData(coloredData);
	}
}
