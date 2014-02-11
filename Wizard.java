package controlCenter;

import javax.swing.JDialog;
import java.util.Vector;

public class Wizard {
    public static final int TASK_WIZARD_TYPE = 1;
    public static final int RULE_WIZARD_TYPE = 2;

    public static final int ENTER_NAME = 1;
    public static final int ENTER_COLUMNS = 2;
    public static final int ENTER_ENTITIES = 3;
    public static final int ENTER_PURPOSES = 4;
    public static final int ENTER_RECIPIENTS = 5;
    public static final int ENTER_ACCESSORS = 6;
    public static final int ENTER_CONDITIONS_ADVANCED = 7;
    public static final int ENTER_SCHEDULE = 8;
    public static final int FINISHED = 9;

    private ResourceManager resourceManager;
    
    /*
     * amol pujari 26/09/2006
     * added this controlCenter to check whether a policy rule already exist or not
     * to access the rules populated inside table like
     * controlCenter.policyEditor.getRuleDisplayPanel().getRuleTableModel().getRules();
     */
    
    public ControlCenter controlCenter = null;
    
    private int type;

    //private String dbName;
    //private String policyName;
    //private String versionName;
    private Version version;

    private final String wizardObjectName;
    private final String wizardObjectNameUppercase;

    private NameDialog nameDialog;
    private ChooseDialog chooseColumnsDialog;
	private ChooseDialog choosePseudonymsDialog;
    private ChooseDialog chooseEntityDialog;
    private ChooseDialog choosePurposeDialog;
    private ChooseDialog chooseRecipientDialog;
    private ChooseDialog chooseAccessorDialog;
    private AdvancedConditionDialog advancedConditionDialog;
    private ScheduleDialog scheduleDialog;

    private int currentDialogNumber;
    private JDialog currentDialog;
    private ResourceManager.Rule currentRule = null;
    private ResourceManager.Rule previousRule = null;

    public Wizard(int type, ResourceManager resourceManager) {
        this.type = type;
        this.resourceManager = resourceManager;

        if (type == TASK_WIZARD_TYPE) {
            wizardObjectName = "audit query";
            wizardObjectNameUppercase = "Audit query";
        }
        else {
            wizardObjectName = "rule";
            wizardObjectNameUppercase = "Rule";
        }

        this.nameDialog = new NameDialog(resourceManager, this);
        this.chooseColumnsDialog = new ChooseDialog(resourceManager, this, ChooseDialog.COLUMN_CHOOSE_TYPE);
        this.chooseEntityDialog = new ChooseDialog(resourceManager, this, ChooseDialog.ENTITY_CHOOSE_TYPE);
        this.choosePurposeDialog = new ChooseDialog(resourceManager, this, ChooseDialog.PURPOSE_CHOOSE_TYPE);
        this.chooseRecipientDialog = new ChooseDialog(resourceManager, this, ChooseDialog.RECIPIENT_CHOOSE_TYPE);
        this.chooseAccessorDialog = new ChooseDialog(resourceManager, this, ChooseDialog.ACCESSOR_CHOOSE_TYPE);
        this.advancedConditionDialog = new AdvancedConditionDialog(resourceManager, this);
        this.scheduleDialog = new ScheduleDialog(resourceManager, this);
    }

    public int getType() {
        return type;
    }

    public String getObjectName() {
        return wizardObjectName;
    }

    public String getObjectNameUppercase() {
        return wizardObjectNameUppercase;
    }

    // show dialog with a new task
    public void showDialog(Version version) {
        // Added 2006-01-20
        this.version = version;

        //showDialog(version, null);
        this.previousRule = null;

        final ResourceManager.Rule newRule;
        if (type == RULE_WIZARD_TYPE)
            newRule = resourceManager.newRule();
        else if (type == TASK_WIZARD_TYPE)
            newRule = resourceManager.newTask();
        else
            newRule = null;
        this.currentRule = newRule;
        
        currentRule.policyName = version.collectionName;
        currentRule.version = version.versionName;

        changeDialog(ENTER_NAME, this.currentRule);
    }

    // show dialog with a preset task
    public void showEditDialog(Version version, ResourceManager.Rule previousRule, ResourceManager.Rule currentRule) {
        this.version = version;
        this.previousRule = previousRule;
        this.currentRule = currentRule;

        this.currentDialogNumber = ENTER_NAME;
        this.currentDialog = nameDialog;
        //System.out.println("1 ################    " + version);
        changeDialog(ENTER_NAME, this.currentRule);
    }

    public int getCurrentDialogNumber() {
        return currentDialogNumber;
    }

    public void changeDialog(int nextDialogNumber, ResourceManager.Rule currentRule) {
        //for (int i=0; i<((ResourceManager.Rule)currentRule).columns.size();
        // i++)
        //    System.out.println(nextDialogNumber+": " +currentRule);

        currentDialogNumber = nextDialogNumber;

        if (nextDialogNumber == ENTER_NAME) {
            final Vector existingNames = new Vector();

            if (type == TASK_WIZARD_TYPE) {
                final Vector existingTasks = resourceManager.getTasksWithNestedColumns(version);
                for (int i = 0; i < existingTasks.size(); i++) {
                    final ResourceManager.Task task = (ResourceManager.Task) existingTasks.get(i);
                    /*
                     * amol pujari 09/10/2006
                     * added this part to allow user enter existing name of the task when editing and not other existings
                     */
                	if(currentRule.name.length()>0)
                	{
                		if( ! currentRule.name.equalsIgnoreCase(task.name) )
                			existingNames.add(task.name);
                		
                	}
                	else
                    
                    existingNames.add(task.name);
                }
            }
            
            /*
             * amol pujari 26/09/2006
             * added this controlCenter to check whether a policy rule already exist or not
             */
            else if(type == RULE_WIZARD_TYPE)
            {
            	try
            	{
            		final Vector rules = resourceManager.getRulesForPolicyVersion(currentRule.policyName,currentRule.version,currentRule.name ,version.databaseName);
            		for(int i=0; i<rules.size(); i++)
            			existingNames.add(rules.get(i));
            	}
            	catch(Exception e)
            	{
            		e.printStackTrace();
            	}
            }
            	

            nameDialog.showDialog(currentRule, existingNames);
        }
        else if (nextDialogNumber == ENTER_ENTITIES)
            chooseEntityDialog.showDialog(version.databaseName, currentRule);
        else if (nextDialogNumber == ENTER_COLUMNS)
            chooseColumnsDialog.showDialog(version.databaseName, currentRule);
        else if (nextDialogNumber == ENTER_PURPOSES)
            choosePurposeDialog.showDialog(version.databaseName, currentRule);
        else if (nextDialogNumber == ENTER_RECIPIENTS)
            chooseRecipientDialog.showDialog(version.databaseName, currentRule);
        else if (nextDialogNumber == ENTER_ACCESSORS)
            chooseAccessorDialog.showDialog(version.databaseName, currentRule);
        else if (nextDialogNumber == ENTER_CONDITIONS_ADVANCED)
            advancedConditionDialog.showDialog(version.databaseName, currentRule);
        else if (nextDialogNumber == ENTER_SCHEDULE) {
            if (type == TASK_WIZARD_TYPE) {
                scheduleDialog.showDialog((ResourceManager.Task) currentRule);
            }
            else
                changeDialog(FINISHED, currentRule);
        }
        else if (nextDialogNumber == FINISHED) {
            final NotificationDialog waitDialog = new NotificationDialog(NotificationDialog.PLEASE_WAIT_DIALOG_TYPE,
                    "Processing", "Please be patient.",controlCenter);
            waitDialog.show();

            //						try {
            //						    Thread.sleep(1000);
            //						}
            //						catch (InterruptedException e) {
            //						    e.printStackTrace();
            //						}

            if (type == RULE_WIZARD_TYPE) {
                final NotificationDialog notificationDialog = new NotificationDialog("Error", "Cannot add policy rule.",controlCenter);
                if (previousRule != null) {
                    // need to remove previousRule, i.e., an update of a rule occurred
                    if (!resourceManager.addRule(version, currentRule, previousRule))
                        notificationDialog.show();
                }
                else {
                    if (!resourceManager.addRule(version, currentRule))
                        notificationDialog.show();
                }
            }
            else if (type == TASK_WIZARD_TYPE) {
                final NotificationDialog notificationDialog = new NotificationDialog("Error", "Cannot add audit query.",controlCenter);
                if (previousRule != null) {
                    // need to remove previousRule, i.e., an update of a task occurred
                    if (!resourceManager.addTask(version, (ResourceManager.Task) currentRule,
                            (ResourceManager.Task) previousRule))
                        notificationDialog.show();
                }
                else {
                    if (!resourceManager.addTask(version, (ResourceManager.Task) currentRule))
                        notificationDialog.show();
                }
            }
            waitDialog.hide();
            final NotificationDialog successDialog = new NotificationDialog("Success", "Objects created successfully.",controlCenter);
            successDialog.show();
        }
    }
}
