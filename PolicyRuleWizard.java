package controlCenter;

import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Logger;

public class PolicyRuleWizard extends NewWizard {
	private Rule rule;

	private Rule oldRule;

	private NamePanel newPolicyPanel;

	private ColumnsPanel columnsInScopePanel;

	private final NamePanel namePanel;

	private final ChoosePanel entitiesPanel;

	private final ColumnsPanel columnsPanel;

	private final ChoosePanel purposesPanel;

	private final ChoosePanel recipientsPanel;

	private final ChoosePanel accessorsPanel;

	private final ConditionPanel conditionPanel;

	private final ResourceManager resourceManager;

	private final String databaseName;

	private final boolean newPolicy;

	private final Logger logger = Logger.getLogger(getClass().getName());

	// 01/03/2007 jk
	// simple versioning does not require entities
	//

	public PolicyRuleWizard(final boolean newPolicy, Rule rule,
			final Rule oldRule, final ResourceManager resourceManager,
			final String databaseName) {
		super(ResourceManager.getResource("prw.title"), 7 + ((newPolicy) ? 2
				: 0), resourceManager.controlCenter, true, false);

		logger.debug("started initiating policy rule wizard");

		this.newPolicy = newPolicy;
		this.rule = rule;
		this.oldRule = oldRule;
		this.resourceManager = resourceManager;
		this.databaseName = databaseName;

		String[] panelInfo = new String[14];

		for (int i = 0; i < panelInfo.length; i++)
			panelInfo[i] = ResourceManager.getResource("prw.pan." + (i + 1));

		if (oldRule != null)
			panelInfo[0] = ResourceManager.getResource("prw.pan.0");

		if (newPolicy) {
			newPolicyPanel = new NamePanel(this, NamePanel.NEW_POLICY_VERSION,
					ResourceManager.getResource("prw.policy"), resourceManager
							.getExistingPolicies(databaseName),
					rule.policyName, rule.version, Version.TYPE_COMPLEX);

			int[] checks = new int[1];

			columnsInScopePanel = new ColumnsPanel(this,
					ColumnsPanel.TYPE_NONE, null, new Vector(), resourceManager
							.listRelevantSchemas(databaseName), resourceManager
							.getDatabaseTablesInfo(databaseName, false),
					resourceManager, databaseName);

			String newInfo[] = new String[panelInfo.length + 4];

			newInfo[0] = ResourceManager.getResource("prw.pan.15");
			newInfo[1] = ResourceManager.getResource("prw.pan.16");

			newInfo[2] = ResourceManager.getResource("prw.pan.17");
			newInfo[3] = ResourceManager.getResource("prw.pan.18");

			for (int i = 0; i < panelInfo.length; i++)
				newInfo[i + 4] = panelInfo[i];

			panelInfo = newInfo;
		}

		final int[] checks = new int[rule.columns.size()];

		for (int i = 0; i < checks.length; i++)
			checks[i] = (((ColumnDescriptor) rule.columns.get(i)).pseudonym) ? 1
					: 0;

		namePanel = new NamePanel(this, NamePanel.NORMAL, ResourceManager
				.getResource("prw.rule"), new Vector(), rule.name, null,
				Version.TYPE_COMPLEX);

		entitiesPanel = new ChoosePanel(this, ChoosePanel.POLICY,
				ChoosePanel.SELECT_ENTITIES, resourceManager.parseEntities(
						rule.condition, databaseName, true), databaseName,
				resourceManager);

		columnsPanel = new ColumnsPanel(this,
				(newPolicy) ? ColumnsPanel.TYPE_NEW_POLICY_RULE_COLUMNS
						: ColumnsPanel.TYPE_NONE, checks, rule.columns,
				resourceManager.listRelevantSchemas(databaseName),
				(newPolicy) ? new Hashtable() : resourceManager
						.getDatabaseTablesInfoWithColumnsInScope(databaseName,
								rule.policyName), resourceManager, databaseName);

		purposesPanel = new ChoosePanel(this, ChoosePanel.POLICY,
				ChoosePanel.PURPOSES, rule.purposes, databaseName,
				resourceManager);

		recipientsPanel = new ChoosePanel(this, ChoosePanel.POLICY,
				ChoosePanel.RECIPIENTS, rule.recipients, databaseName,
				resourceManager);

		accessorsPanel = new ChoosePanel(this, ChoosePanel.POLICY,
				ChoosePanel.ACCESSORS, rule.accessors, databaseName,
				resourceManager);

		conditionPanel = new ConditionPanel(this,
						    resourceManager.getDisplayableCondition(rule.condition),
						    resourceManager.getDatabaseTablesInfo(databaseName, false));

		final NewWizardPanel[] panels = new NewWizardPanel[7 + ((newPolicy) ? 2
				: 0)];

		int index = 0;
		panels[index + ((newPolicy) ? 2 : 0)] = namePanel;
		index++;
		panels[index + ((newPolicy) ? 2 : 0)] = entitiesPanel;
		index++;
		panels[index + ((newPolicy) ? 2 : 0)] = columnsPanel;
		index++;
		panels[index + ((newPolicy) ? 2 : 0)] = purposesPanel;
		index++;
		panels[index + ((newPolicy) ? 2 : 0)] = recipientsPanel;
		index++;
		panels[index + ((newPolicy) ? 2 : 0)] = accessorsPanel;
		index++;
		panels[index + ((newPolicy) ? 2 : 0)] = conditionPanel;
		index++;

		if (newPolicy) {
			panels[0] = newPolicyPanel;
			panels[1] = columnsInScopePanel;
		}

		// 01/08/2007 amol pujari
		// simple versioning does not require entity panel
		//displayEntityPanel(rule.versioningType == Version.TYPE_COMPLEX);

		set(panelInfo, panels);
	}

	public void finished() {
		String msg = "";
		String title = ResourceManager.getResource("label.message");

		rule.name = namePanel.getName();
		rule.entities = entitiesPanel.getSelection();
		rule.columns = columnsPanel.getSelectedColumns();
		rule.purposes = purposesPanel.getSelection();
		rule.recipients = recipientsPanel.getSelection();
		rule.accessors = accessorsPanel.getSelection();
		rule.condition = conditionPanel.getCondition();

		final int checked[] = columnsPanel.getChecks();

		for (int i = 0; i < checked.length; i++)
			((ColumnDescriptor) rule.columns.get(i)).pseudonym = (checked[i] == 1);

		if (newPolicy) {
			rule.policyName = newPolicyPanel.getName();
			rule.version = newPolicyPanel.getVersion();
			rule.versioningType = newPolicyPanel.getType();

			msg = resourceManager.addPolicy(new Version(databaseName,
					rule.policyName, rule.version, rule.versioningType, 1));
		}

		if (msg.length() == 0) {

			if (oldRule == null)
				msg += resourceManager.addRule(new Version(databaseName,
						rule.policyName, rule.version), rule);
			else
				msg += resourceManager.addRule(new Version(databaseName,
						rule.policyName, rule.version), rule, oldRule);

			if (newPolicy) {
				// saving scope
				final Version version = new Version(databaseName,
						newPolicyPanel.getName(), newPolicyPanel.getVersion(),
						newPolicyPanel.getType(), 1);

				final String msgStr = resourceManager.updateScope(version,
						columnsInScopePanel.getSelectedColumns(),
						version.versionName);

				// MessageBox.stopBusy();
				MessageBox.show(resourceManager.controlCenter, ResourceManager
						.getResource("label.message"), msgStr,
						MessageBox.ICON_INFO);
			}

		}

		// MessageBox.stopBusy();
		MessageBox.show(resourceManager.controlCenter, title, msg,
				MessageBox.ICON_INFO);
	}

	// will be called by columnsPanel only if newPolicy
	public Hashtable getUpdatedList() {
		return resourceManager.getDatabaseTablesFiltered(databaseName,
				columnsInScopePanel.getSelectedColumns());
	}

	public Vector filterColumnsInScope(Vector data) {
		logger.debug("filtering columns in scope:" + data);

		ColumnDescriptor column;
		final Vector filteredData = new Vector(4, 2);
		Vector columnsInScope = columnsInScopePanel.getSelectedColumns();

		for (int i = 0; i < data.size(); i++) {
			column = (ColumnDescriptor) data.get(i);

			if (columnsInScope.contains(column))
				filteredData.add(column);
		}

		logger.debug("filtered columns in scope:" + filteredData);
		return filteredData;
	}

	public void displayEntityPanel(boolean display) {
		// 01/08/2007 amol pujari
		// simple versioning does not require entity panel
		entitiesPanel.hasToDisplay = display;
		panelRefreshed(0);
		refreshLeftPanel();
	}
}
