package controlCenter;

import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

import org.apache.log4j.Logger;

public abstract class NewWizard extends JDialog {

	private final JPanel msgPanel = new JPanel();

	private final JLabel msgLabel = new JLabel();

	private JPanel leftPanel;

	private JLabel[] labelArray;

	private final JPanel rightPanel;

	private final JButton cancelButton = new JButton(ResourceManager
			.getResource("button.cancel"));

	private final JButton backButton = new JButton(ResourceManager
			.getResource("button.back"));

	private final JButton nextButton = new JButton(ResourceManager
			.getResource("button.next"));

	private final JButton finishButton = new JButton(ResourceManager
			.getResource("button.finish"));

	private int currentState = 0;

	private final int MAX_STATE;

	private NewWizardPanel[] panels;

	private final boolean isSmall;

	private final Logger logger = Logger.getLogger(getClass().getName());

	public void set(final String[] panelInfo, final NewWizardPanel[] panels) {
		logger.debug("setting panels");

		this.panels = panels;

		for (int i = 0; i < panels.length; i++)
			rightPanel.add(this.panels[i]);

		if (MAX_STATE == 1) {
			msgLabel.setText(panelInfo[0]);
			panels[0].setSize(panels[0].getWidth() - ((isSmall) ? 160 : 0),
					panels[0].getHeight());
			panels[0].setVisible(true);
		} else {
			for (int i = 0; i < panelInfo.length; i++)
				labelArray[i].setText(panelInfo[i]);

			setPanels(0, -1);
		}

		panelRefreshed(0);
		MessageBox.stopBusy();
		show();
	}

	public NewWizard(final String title, final int count, final Frame parent,
			final boolean modal, final boolean small) {
		super(parent, modal);
		logger.debug("started initiating new wizard");
		setTitle(title);
		MAX_STATE = count;
		isSmall = small;

		int widthDiff = 0;
		int internalWidthDiff = 0;

		rightPanel = new JPanel();
		leftPanel = new JPanel();

		logger.debug("wizard include " + count + " steps");

		if (MAX_STATE == 1) {
			leftPanel = null;

			widthDiff = 166;

			if (small)
				internalWidthDiff = 160;

			backButton.setEnabled(false);
			nextButton.setEnabled(false);

		} else {
			labelArray = new JLabel[(count * 2)];
			leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
			for (int i = 0; i < labelArray.length; i++) {
				labelArray[i] = new JLabel();
				labelArray[i].setForeground(Color.gray);
				labelArray[i].setSize(150, 50);

				if (i % 2 != 0) {
					labelArray[i].setVisible(false);
					labelArray[i].setFont(new Font("Default", Font.PLAIN, 12));
				} else
					labelArray[i].setFont(new Font("Default", Font.BOLD, 12));

				leftPanel.add(labelArray[i]);
			}

			setSize(738, 384);
			leftPanel.setBounds(0, 0, 165, 381);
			leftPanel.setBackground(new Color(69, 69, 138));

			backButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent arg0) {

					if (currentState > 0) {
						currentState -= 2;

						int lastState = currentState + 2;

						while (!panels[currentState / 2].hasToDisplay)
							currentState -= 2;

						setPanels(currentState, lastState);

						if (currentState < (MAX_STATE * 2 - 2))
							nextButton.setEnabled(true);

						panelRefreshed(currentState / 2);
					}

				}
			});

			nextButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent arg0) {

					if (currentState < (MAX_STATE * 2 - 2)) {
						currentState += 2;

						int lastState = currentState - 2;

						while (!panels[currentState / 2].hasToDisplay)
							currentState += 2;

						setPanels(currentState, lastState);

						if (currentState == (MAX_STATE * 2 - 2))
							nextButton.setEnabled(false);

						panelRefreshed(currentState / 2);
					}
				}
			});

			getContentPane().add(leftPanel);
		}

		setSize(738 - widthDiff - internalWidthDiff, 384);

		rightPanel.setLayout(null);

		msgPanel.setBounds(168 - widthDiff, 0, 560 - internalWidthDiff, 40);
		msgPanel.setLayout(null);

		msgLabel.setBounds(20, 4, 550 - internalWidthDiff, 35);
		msgPanel.add(msgLabel);

		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		setResizable(false);
		getContentPane().setLayout(null);

		widthDiff += 1;

		rightPanel.setBounds(168 - widthDiff, 41, 564 - internalWidthDiff, 280);
		rightPanel.setBorder(BorderFactory
				.createEtchedBorder(EtchedBorder.LOWERED));

		cancelButton.setMnemonic('C');
		backButton.setMnemonic('B');
		nextButton.setMnemonic('N');
		finishButton.setMnemonic('F');

		cancelButton
				.setBounds(390 - widthDiff - internalWidthDiff, 326, 80, 23);
		backButton.setBounds(475 - widthDiff - internalWidthDiff, 326, 80, 23);
		nextButton.setBounds(560 - widthDiff - internalWidthDiff, 326, 80, 23);
		finishButton
				.setBounds(645 - widthDiff - internalWidthDiff, 326, 80, 23);

		finishButton.setEnabled(false);

		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				logger.debug("disposing new wizard");
				dispose();
				canceled();
			}
		});

		finishButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				logger.debug("disposing new wizard");
				dispose();
				logger.debug("calling finished()");
				finished();
			}
		});

		getContentPane().add(msgPanel);
		getContentPane().add(rightPanel);
		getContentPane().add(cancelButton);
		getContentPane().add(backButton);
		getContentPane().add(nextButton);
		getContentPane().add(finishButton);
	}

	/**
	 * @param state
	 * @param lastState
	 */
	private void setPanels(final int state, final int lastState) {
		// gray the last labels
		if (lastState >= 0) {
			labelArray[lastState].setForeground(Color.gray);
			labelArray[lastState + 1].setForeground(Color.gray);
			labelArray[lastState + 1].setVisible(false);
			labelArray[lastState].setText(labelArray[lastState].getText()
					.substring(10));
			labelArray[lastState].setText("<html>"
					+ labelArray[lastState].getText());

			panels[lastState / 2].setVisible(false);
		}

		// shine the new lables
		labelArray[state].setText(labelArray[state].getText().substring(6));
		labelArray[state].setText("<html><br>" + labelArray[state].getText());
		labelArray[state].setForeground(Color.white);
		labelArray[state + 1].setForeground(Color.white);
		labelArray[state + 1].setVisible(true);

		panels[state / 2].setVisible(true);

		backButton.setEnabled((state != 0));

		refreshLeftPanel();
	}

	public void refreshLeftPanel() {
		try {
			for (int i = 0; i < panels.length; i++) {
				labelArray[(i * 2)].setVisible(panels[i].hasToDisplay);
				// labelArray[(i*2)+1].setVisible(panels[i].hasToDisplay);
			}
		} catch (NullPointerException e) {
			// this could raise at the first time
			// when other panels are not yet initialized
		}
	}

	public void setMessage(final String msg, final boolean nextEnabled) {
		msgLabel.setText(msg);
		nextButton.setEnabled(nextEnabled
				&& !(currentState == (MAX_STATE * 2 - 2)));
		msgLabel.setIcon(null);
	}

	public void setFinishEnabled(final boolean finishEnabled) {
		finishButton.setEnabled(finishEnabled);
	}

	public void setWarningIcon() {
		msgLabel.setIcon(ResourceManager.ICON_warningSmall);
	}

	public void setErrorIcon() {
		msgLabel.setIcon(ResourceManager.ICON_errorSmall);
	}

	public void setMessage(final String msg) {
		msgLabel.setText(msg);
		msgLabel.setIcon(null);
	}

	public void panelRefreshed(final int count) {
		setFinishEnabled(false);

		try {
			if (count >= 0)
				panels[count].refresh();

			for (int i = 0; i < panels.length; i++) {
				if (!panels[i].isFinished())
					if (panels[i].hasToDisplay)
						return;
			}

		} catch (NullPointerException e) {
			/*
			 * this is the case when initially not all the panels are
			 * constructed but those which being construct they would call this
			 * function and it would throw null pointer exception in case other
			 * panels ane not constrcuted yet
			 */
			return;
		}

		setFinishEnabled(true);
	}

	abstract public void finished();

	public void canceled() {
	}
}
