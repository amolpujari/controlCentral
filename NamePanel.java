package controlCenter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;

import org.apache.log4j.Logger;

class NamePanel extends NewWizardPanel implements KeyChangedInformer {
	public final static int NORMAL = 0;

	public final static int NEW_POLICY_VERSION = 1;

	public final static int PURPOSE = 20;

	public final static int RECIPIENT = 30;

	public final static int ACCESSOR = 40;

	private final JTextField nameText;

	private JTextField versionText;

	private final String item;

	private final Vector existingNames;

	private JRadioButton simpleVersion;

	private final int type;

	private boolean visitedVersionField = false;

	private final Logger logger = Logger.getLogger(getClass().getName());

	public NamePanel(final NewWizard wiz, final int type, final String item,
			final Vector existingNames, final String name,
			final String version, final int versioningType) {
		super();
		logger.debug("started initiating name panel:" + item);
		this.wiz = wiz;
		this.item = item;
		this.type = type;
		this.existingNames = existingNames;
		final JLabel nameLabel = new JLabel(ResourceManager
				.getResource("np.Name"));
		nameText = new JTextField(name);
		nameLabel.setBounds(10, 10, 50, 20);
		nameText.setBounds(70, 10, 470, 20);
		add(nameLabel);
		add(nameText);
		setBounds(2, 10, getWidth() - 2, getHeight() - 10);

		nameText.addKeyListener(new CommonKeyListener(this));

		nameText.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent arg0) {
				if (nameText.getText().trim().length() < 1)
					wiz.setMessage(ResourceManager.getResource("np.enter")
							+ " " + item + " "
							+ ResourceManager.getResource("np.name"));
			}

			public void focusLost(FocusEvent arg0) {
				if (nameText.getText().trim().length() < 1) {
					wiz
							.setMessage(ResourceManager
									.getResource("np.name.empty"));
					wiz.setErrorIcon();
				}
			}
		});

		if (name.length() < 1) {
			wiz.setMessage(ResourceManager.getResource("np.enter") + " " + item
					+ " " + ResourceManager.getResource("np.name"), false);
			finished = false;
			wiz.setFinishEnabled(false);
		} else {
			finished = true;
			wiz.panelRefreshed(-1);
			wiz.setMessage("", true);
			existingNames.remove(name);
		}

		if (type == NEW_POLICY_VERSION) {
			versionText = new JTextField(version);
			versionText.setBounds(70, 35, 470, 20);
			versionText.addKeyListener(new CommonKeyListener(this));

			final JLabel versionLabel = new JLabel(ResourceManager
					.getResource("np.ver"));
			versionLabel.setBounds(10, 35, 50, 20);

			versionText.addFocusListener(new FocusListener() {
				public void focusGained(FocusEvent arg0) {
					if (versionText.getText().trim().length() < 1)
						wiz.setMessage(ResourceManager
								.getResource("np.enter.ver"));
				}

				public void focusLost(FocusEvent arg0) {
					if (versionText.getText().trim().length() < 1) {
						wiz.setMessage(ResourceManager
								.getResource("np.ver.empty"));
						wiz.setErrorIcon();
						visitedVersionField = true;
					}
				}
			});

			add(versionLabel);
			add(versionText);

			final JPanel pan = new JPanel();

			pan.setLayout(null);

			pan.setBounds(2, 64, 555, 70);

			pan.setBorder(BorderFactory.createTitledBorder(BorderFactory
					.createEtchedBorder(EtchedBorder.LOWERED), " "
					+ ResourceManager.getResource("np.ver.detail") + " "));

			simpleVersion = new JRadioButton(" "
					+ ResourceManager.getResource("np.simple.ver") + " ");
			final JRadioButton compleVersion = new JRadioButton(" "
					+ ResourceManager.getResource("np.cmplx.ver") + " ", true);
			final ButtonGroup group = new ButtonGroup();
			group.add(simpleVersion);
			group.add(compleVersion);
			simpleVersion.setBounds(10, 20, 200, 20);
			compleVersion.setBounds(10, 40, 200, 20);

			// this is the code needed to avoid entoty selection for 
			// simple versining
//			simpleVersion.addActionListener(new ActionListener() {
//				public void actionPerformed(ActionEvent arg0) {
//					((PolicyRuleWizard) wiz).displayEntityPanel(!simpleVersion
//							.isSelected());
//				}
//			});
//
//			compleVersion.addActionListener(new ActionListener() {
//				public void actionPerformed(ActionEvent arg0) {
//					((PolicyRuleWizard) wiz).displayEntityPanel(compleVersion
//							.isSelected());
//				}
//			});

			if (versioningType == Version.TYPE_COMPLEX)
				compleVersion.setEnabled(true);
			else
				simpleVersion.setEnabled(true);

			pan.add(simpleVersion);
			pan.add(compleVersion);

			add(pan);
		}
	}

	public String getName() {
		return nameText.getText().trim();
	}

	public String getVersion() {
		return versionText.getText().trim();
	}

	public int getType() {
		return (simpleVersion.isSelected()) ? Version.TYPE_SIMPLE
				: Version.TYPE_COMPLEX;
	}

	public void checkInput() {

		final String text = nameText.getText().trim();
		finished = false;
		wiz.setFinishEnabled(false);

		if (text.length() > 32) {
			wiz.setMessage(ResourceManager.getResource("np.name.long"), false);
			wiz.setErrorIcon();
		} else if (text.length() < 1) {
			wiz.setMessage(ResourceManager.getResource("np.enter") + " " + item
					+ " " + ResourceManager.getResource("np.name"), false);
		} else if (existingNames.contains(text)) {
			wiz.setMessage(ResourceManager.getResource("np.dup") + " " + item
					+ " " + ResourceManager.getResource("np.name"), false);
			wiz.setErrorIcon();
		} else {
			if (type == NEW_POLICY_VERSION) {
				final String text2 = versionText.getText().trim();

				if (text2.length() > 32) {
					wiz.setMessage(ResourceManager.getResource("np.ver.long"),
							false);
					wiz.setErrorIcon();
				} else if (text2.length() < 1) {
					if (visitedVersionField) {
						wiz.setMessage(ResourceManager
								.getResource("np.ver.empty"), false);
						wiz.setErrorIcon();
					}
				} else {
					finished = true;
					wiz.panelRefreshed(-1);
					wiz.setMessage("", true);
				}
			} else {
				finished = true;
				wiz.panelRefreshed(-1);
				wiz.setMessage("", true);
			}
		}
	}

	/**
	 * Get called when this panel get activated
	 */
	public void refresh() {
		checkInput();
	}

	public void setNameTextWidth(int width) {
		nameText.setSize(width, nameText.getHeight());
	}

}
