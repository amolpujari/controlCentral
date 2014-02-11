package controlCenter;

import java.util.Hashtable;

import org.apache.log4j.Logger;

public class AuditQueryWizard extends NewWizard {

	private Task task;

	private Task oldTask;

	private NamePanel newAuditPanel;

	private final NamePanel namePanel;

	private final ColumnsPanel columnsPanel;

	private final ChoosePanel purposesPanel;

	private final ChoosePanel recipientsPanel;

	private final ChoosePanel accessorsPanel;

	private final ConditionPanel conditionPanel;

	private final IntervalPanel intervalPanel;

	private final ResourceManager resourceManager;

	private final String databaseName;

	private final boolean newAudit;

	private final Logger logger = Logger.getLogger(getClass().getName());

	public AuditQueryWizard(final boolean newAudit, final Task task,
			final Task oldTask, final ResourceManager resourceManager,
			final String databaseName) {

		super(ResourceManager.getResource("aqw.title"), 7 + ((newAudit) ? 1 : 0),
				resourceManager.controlCenter, true, false);

		logger.debug("started initiating audit query wizard");

		this.newAudit = newAudit;
		this.task = task;
		this.oldTask = oldTask;
		this.resourceManager = resourceManager;
		this.databaseName = databaseName;
		
		String[] panelInfo = new String[14];
		
		for(int i=0; i<panelInfo.length; i++)
			panelInfo[i] = ResourceManager.getResource("aqw.pan."+(i+1));

		if (oldTask != null)
			panelInfo[0] = ResourceManager.getResource("aqw.pan.0");

		if (newAudit) {
			newAuditPanel = new NamePanel(this, NamePanel.NORMAL, "audit",
					resourceManager.getExistingAudits(databaseName),
					task.policyName, null, Version.TYPE_COMPLEX);

			String newInfo[] = new String[panelInfo.length + 2];

			newInfo[0] = ResourceManager.getResource("aqw.pan.15");
			newInfo[1] = ResourceManager.getResource("aqw.pan.16");

			for (int i = 0; i < panelInfo.length; i++)
				newInfo[i + 2] = panelInfo[i];

			panelInfo = newInfo;

		}

		String auditName = task.policyName;

		if (oldTask != null && oldTask.policyName != null)
			auditName = oldTask.policyName;

		namePanel = new NamePanel(this, NamePanel.NORMAL, ResourceManager.getResource("label.task"),
				resourceManager.getTaskNames(databaseName, auditName),
				task.name, null, Version.TYPE_COMPLEX);

		columnsPanel = new ColumnsPanel(this, ColumnsPanel.TYPE_AUDIT, null,
				task.columns,
				resourceManager.listRelevantSchemas(databaseName),
				resourceManager.getDatabaseTablesInfo(databaseName, true),resourceManager,databaseName);

		purposesPanel = new ChoosePanel(this, ChoosePanel.AUDIT,
				ChoosePanel.PURPOSES, task.purposes, databaseName,
				resourceManager);

		recipientsPanel = new ChoosePanel(this, ChoosePanel.AUDIT,
				ChoosePanel.RECIPIENTS, task.recipients, databaseName,
				resourceManager);

		accessorsPanel = new ChoosePanel(this, ChoosePanel.AUDIT,
				ChoosePanel.ACCESSORS, task.accessors, databaseName,
				resourceManager);

		conditionPanel = new ConditionPanel(this, resourceManager
				.getDisplayableCondition(task.condition), resourceManager
				.getDatabaseTablesInfo(databaseName, true));

		intervalPanel = new IntervalPanel(this, (newAudit) ? null : task.begin,
				(newAudit) ? null : task.end);

		final NewWizardPanel[] panels = new NewWizardPanel[7 + ((newAudit) ? 1
				: 0)];

		panels[0 + ((newAudit) ? 1 : 0)] = namePanel;
		panels[1 + ((newAudit) ? 1 : 0)] = columnsPanel;
		panels[2 + ((newAudit) ? 1 : 0)] = purposesPanel;
		panels[3 + ((newAudit) ? 1 : 0)] = recipientsPanel;
		panels[4 + ((newAudit) ? 1 : 0)] = accessorsPanel;
		panels[5 + ((newAudit) ? 1 : 0)] = conditionPanel;
		panels[6 + ((newAudit) ? 1 : 0)] = intervalPanel;

		if (newAudit)
			panels[0] = newAuditPanel;

		set(panelInfo, panels);
	}

	public void finished() {
		boolean response = true;
		String msg = "";
		String title = ResourceManager.getResource("label.success");

		task.name = namePanel.getName();
		task.columns = columnsPanel.getSelectedColumns();
		task.purposes = purposesPanel.getSelection();
		task.recipients = recipientsPanel.getSelection();
		task.accessors = accessorsPanel.getSelection();
		task.condition = conditionPanel.getCondition();
		task.begin = intervalPanel.getBegin();
		task.end = intervalPanel.getEnd();

		if (newAudit) {
			task.policyName = newAuditPanel.getName();
			task.version = "1";

			response = resourceManager.addAudit(new Version(databaseName,
					task.policyName, task.version));

			if (!response) {
				msg += ResourceManager.getResource("aqw.pan.17");
				msg += ResourceManager.getResource("aqw.pan.18");
				title = ResourceManager.getResource("label.error");
			}
		}

		if (response) {
			if (oldTask == null)
				response = resourceManager.addTask(new Version(databaseName,
						task.policyName, task.version), task);
			else
				response = resourceManager.addTask(new Version(databaseName,
						task.policyName, task.version), task, oldTask);

			if (response) {
				// msg +="<br>&nbsp;&nbsp;&nbsp;&nbsp; Audit task added
				// successfully! &nbsp;&nbsp;&nbsp;&nbsp;";
				// to avoid user extra user interaction if things are going fine
				msg = "";
			} else {
				msg += ResourceManager.getResource("aqw.pan.19");
				msg += ResourceManager.getResource("aqw.pan.20");
				title = ResourceManager.getResource("label.error");
			}
		}

		MessageBox.stopBusy();
		MessageBox.show(resourceManager.controlCenter, title, msg,MessageBox.ICON_SUC);
	}
	
	// will be called by condition panel
	public Hashtable getUpdatedList() {
//	this is in case when to suppress tables.columns not selected		
//		return resourceManager.getDatabaseTablesFiltered(databaseName,
//				columnsPanel.getSelectedColumns(),
//				false);
		
		return resourceManager.getDatabaseTablesInfo(databaseName, true);
		
	}

}
