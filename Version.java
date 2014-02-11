package controlCenter;

public class Version {

	public final String databaseName;

	public final static int TYPE_COMPLEX = 0;

	public final static int TYPE_SIMPLE = 1;

	// For audits, this class represents a collection of audit tasks, and
	// for policies, it represents a collection of policy rules.
	public final String collectionName;

	public String versionName;

	public int type = 0;

	public int enabled = 1;

	public Version(String databaseName, String collectionName,
			String versionName) {
		this.databaseName = databaseName;
		this.collectionName = collectionName;
		this.versionName = versionName;
	}

	public Version(String databaseName, String collectionName,
			String versionName, int type, int enabled) {
		this.databaseName = databaseName;
		this.collectionName = collectionName;
		this.versionName = versionName;
		this.type = type;
		this.enabled = enabled;
	}

	public String toString() {
		return "[" + "database = " + databaseName + ", " + "collection = "
				+ collectionName + ", " + "version = " + versionName + "]";
	}
}