package controlCenter;

import java.awt.Frame;
import java.util.Vector;

import org.apache.log4j.Logger;

public class ImportRuleWizard extends NewWizard {
	final private ImportRulePanel importRulePanel;

	private Vector data;

	private Vector initialData;

	private final Logger logger = Logger.getLogger(getClass().getName());

	public ImportRuleWizard(final String title, final Frame parent,
			final Version version, final Vector initialData,
			final Vector otherVersions, final Vector allRules) {
		super(title, 1, parent, true, false);

		final NewWizardPanel[] panels = new NewWizardPanel[1];
		String[] panelInfo = new String[1];

		this.data = initialData;
		this.initialData = new Vector(4, 2);

		for (int i = 0; i < initialData.size(); i++)
			this.initialData.add(initialData.get(i));

		importRulePanel = new ImportRulePanel(this, data, version,
				otherVersions, allRules);

		panelInfo[0] = " "+ ResourceManager.getResource("label.select.rule") + " ";// message

		panels[0] = importRulePanel;
		set(panelInfo, panels);
	}

	public void finished() {
		data = importRulePanel.getRuleSelection();
	}

	public void canceled() {
		data = initialData;
	}

	public Vector getSelectedRules() {
		return data;
	}

}
