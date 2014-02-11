package controlCenter;

public interface ResourceListener {
	public void databaseConnected(final String dbname);

	public void databaseDisconnected(final String dbname);

	public void policyChanged(final Version version);

	public void auditChanged(final Version version);

	public void applicationChanged(final String databaseName);

	public void entitiesChanged(final String databaseName);

	public void purposesChanged(final String databaseName);

	public void recipientsChanged(final String databaseName);

	public void accessorsChanged(final String databaseName);

	public void columnsInScopeChanged(final String databaseName,
			final String policyName);

	public void backlogChanged(final String databaseName);
}