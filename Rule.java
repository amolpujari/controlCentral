/**
 * 
 */
package controlCenter;

import java.util.Vector;

import controlCenter.ResourceManager.ChoiceDescriptor;

class Rule implements Cloneable {
	// intended for rules with 1 purpose, accessor, recipient, and
	// column only
	
	public String databaseName = "";
	
	public String policyName = "";

	public String version = "";

	public String name = "";

	public Vector columns = new Vector(4, 2);

	public Vector purposes = new Vector(4, 2);

	public Vector accessors = new Vector(4, 2);

	public Vector recipients = new Vector(4, 2);

	public String condition = "";

	public Vector entities = new Vector(4, 2);

	public Vector optInChoices = new Vector(4, 2);

	public Vector optOutChoices = new Vector(4, 2);

	public int versioningType = Version.TYPE_COMPLEX;

	public int enabled = 1;

	public String toString() {
		return name + " ( " + columns + " / " + purposes + " / " + accessors
				+ " / " + recipients + ") - " + version + " / " + policyName;
	}

	/**
	 * Deep copy.
	 */
	public Object clone() throws CloneNotSupportedException {
		final Rule other = (Rule) super.clone();

		other.databaseName = databaseName;
		other.policyName = policyName;
		other.version = version;
		other.name = name;

		other.columns = new Vector(4, 2);
		for (int i = 0; i < columns.size(); i++)
			other.columns.add(i, ((ColumnDescriptor) columns.get(i))
					.clone(true));

		other.purposes = new Vector(4, 2);
		for (int i = 0; i < purposes.size(); i++)
			other.purposes.add(i, (String) purposes.get(i));

		other.accessors = new Vector(4, 2);
		for (int i = 0; i < accessors.size(); i++)
			other.accessors.add(i, (String) accessors.get(i));

		other.recipients = new Vector(4, 2);
		for (int i = 0; i < recipients.size(); i++)
			other.recipients.add(i, (String) recipients.get(i));

		other.condition = condition;

		other.entities = new Vector(4, 2);
		for (int i = 0; i < entities.size(); i++)
			other.entities.add(i, (String) entities.get(i));

		other.optInChoices = new Vector(4, 2);
		for (int i = 0; i < optInChoices.size(); i++)
			other.optInChoices.add(i, ((ChoiceDescriptor) optInChoices.get(i))
					.clone());

		other.optOutChoices = new Vector(4, 2);
		for (int i = 0; i < optOutChoices.size(); i++)
			other.optOutChoices.add(i,
					((ChoiceDescriptor) optOutChoices.get(i)).clone());

		return other;
	}

	public String getColumnsString() {
		StringBuffer str = new StringBuffer();

		for (int i = 0; i < columns.size(); i++)
			str
					.append(((ColumnDescriptor) columns.get(i))
							.getDisplayable(true));

		return str.toString();
	}
}
