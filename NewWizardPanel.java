package controlCenter;

import javax.swing.JPanel;

public abstract class NewWizardPanel extends JPanel {
	protected boolean finished;

	protected NewWizard wiz;

	protected boolean hasToDisplay = true;

	public NewWizardPanel() {
		super();
		setBounds(2, 2, 558, 272);
		setLayout(null);
		setVisible(false);
	}

	public final boolean isFinished() {
		return finished;
	}

	abstract public void refresh();
}
