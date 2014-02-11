package controlCenter;

import java.awt.Color;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import org.apache.log4j.Logger;

import Utility;

public class ResourceManager {

	public DBCon con = new DBCon();

	public ControlCenter controlCenter;

	private final Vector resourceListeners;

	private final Vector excludedSchemaNames;

	private static final ResourceBundle bundle = ResourceBundle
			.getBundle("resources.hdb");

	public static final int SINGLE_ROW_SCROLL_PANEL_DIMENSION_X = 60;

	public static final int SCROLL_PANEL_DIMENSION_X = 170;

	public static final int SCROLL_PANEL_DIMENSION_Y = 200;

	public static final int INSETS_PADDING = 2;

	public static final int TEXT_FIELD_SIZE = 20;

	public final static Color BACKGROUND_COLOR = (new JPanel()).getBackground();

	public final static Color TABLE_BACKGROUND_COLOR = new Color(247, 247, 249);// new

	// Color(240,240,240);

	public static final String RIGHT_ARROW = "\u25ba";

	public static final String LEFT_ARROW = "\u25c4";

	public static final Border BORDER = BorderFactory.createEtchedBorder(
			Color.white, Color.gray);

	public final static Font NORMAL_FONT = (new JLabel()).getFont();

	public final static Font BOLD_FONT = new Font(NORMAL_FONT.getFamily(),
			Font.BOLD, NORMAL_FONT.getSize());

	public final static Font COURIER_FONT = new Font("Courier", NORMAL_FONT
			.getStyle(), NORMAL_FONT.getSize());

	private final Logger logger = Logger.getLogger(getClass().getName());

	private TreeRenderer treeRenderer;

	static public ImageIcon ICON_confirmSmall;

	static public ImageIcon ICON_confirmBig;

	static public ImageIcon ICON_warningSmall;

	static public ImageIcon ICON_warningBig;

	static public ImageIcon ICON_errorSmall;

	static public ImageIcon ICON_errorBig;

	static public ImageIcon ICON_successSmall;

	static public ImageIcon ICON_successBig;

	static public ImageIcon ICON_failureSmall;

	static public ImageIcon ICON_failureBig;

	static public ImageIcon ICON_Busy;

	static public ImageIcon ICON_ascending;

	static public ImageIcon ICON_descending;

	public TreeRenderer getTreeRenderer() {
		return treeRenderer;
	}

    public class ConditionGenerationException extends Exception {
	/**
	 * Constructs an exception with a given error message.
	 *
	 * @param message The detail error message 
	 */
	public ConditionGenerationException (String message) {        
	    super(message);
	}
    }

	public ResourceManager(ControlCenter controlCenter) {

		ICON_ascending = new ImageIcon(getResource("icon.asc"));
		ICON_descending = new ImageIcon(getResource("icon.desc"));
		ICON_confirmSmall = new ImageIcon(getResource("icon.small.confirm"));
		ICON_confirmBig = new ImageIcon(getResource("icon.big.confirm"));
		ICON_warningSmall = new ImageIcon(getResource("icon.small.warning"));
		ICON_warningBig = new ImageIcon(getResource("icon.big.warning"));
		ICON_errorSmall = new ImageIcon(getResource("icon.small.error"));
		ICON_errorBig = new ImageIcon(getResource("icon.big.error"));
		ICON_successSmall = new ImageIcon(getResource("icon.small.success"));
		ICON_successBig = new ImageIcon(getResource("icon.big.success"));
		ICON_failureSmall = new ImageIcon(getResource("icon.small.failure"));
		ICON_failureBig = new ImageIcon(getResource("icon.big.failure"));
		ICON_Busy = new ImageIcon(getResource("icon.busy"));

		logger.debug("Initiating resource manager");

		this.controlCenter = controlCenter;
		resourceListeners = new Vector(4, 2);
		excludedSchemaNames = new Vector(4, 2);
		final StringTokenizer stringTokenizer = new StringTokenizer(
				getResource("metadata.schemas.excluded"), ",");
		while (stringTokenizer.hasMoreTokens())
			excludedSchemaNames.add(stringTokenizer.nextToken().trim());

		treeRenderer = new TreeRenderer(this);
	}

	public static String getResource(final String key) {
		String value = "_";

		try {
			value = bundle.getString(key);
		} catch (MissingResourceException e) {
			System.err.println(e.getMessage());
		}

		return value;
	}

	public char getMnemonic(String key) {
		return (getResource(key)).charAt(0);
	}

	// Needed for creating input files for Clio.
	public DBCon getConnection(String databaseName) {
		if (con == null)
			return null;
		else
			return con.getDBCon(databaseName);
	}

	public String openConnection(String databaseName, String username,
			String password) {
		StringBuffer str = new StringBuffer();

		if (con != null && con.isConnected(databaseName)) {
			str.append(databaseName);
			str.append(" ");
			str.append(getResource("label.already.activated"));
		} else {
			try {
				boolean useJcc = getResource("jdbc.usejcc").equalsIgnoreCase(
						"true");
				// load both drivers for remote connections
				// type 2 drivers

				if (!useJcc) {
					con = new DBCon(username, password, databaseName,
							getResource("jdbc.url.app"), "jdbc:db2:"
									+ databaseName, SQLs);
					con.loadAnotherDriver(getResource("jdbc.url.net"));
				} else
					con = new DBCon(username, password, databaseName,
							getResource("jdbc.url.jcc"), "jdbc:db2://"
									+ databaseName, SQLs);

				fireDatabaseConnected(databaseName);
				hasBacklogTableSchemaChange(databaseName);
			} catch (ClassNotFoundException e) {
				str.append(getResource("label.could.not.laod.driver"));
				str.append(" ");
				str.append(getResource("label.see.err"));
			} catch (SQLException e) {
				final String msg = e.getMessage();
				str.append(msg.substring(27, msg.indexOf(".") + 1));
			} catch (Exception e) {
				str.append(e.getMessage());
				logger.fatal(str.toString(), e);
			}
		}

		return str.toString();
	}

	// assume all connections are active (i.e. not closed)
	public Enumeration getConnectedDatabaseNames() {
		if (con == null)
			return null;
		else
			return con.getDBNames();
	}

	// public Hashtable getConnectedDatabaseTables()
	// {
	// Hashtable tables = new Hashtable();
	//        
	// if(con==null)
	// return tables;
	//
	// Enumeration databaseNames = con.getDBNames();
	//
	// if(databaseNames==null)
	// return tables;
	//
	// try {
	// while (databaseNames.hasMoreElements()) {
	// String databaseName = (String) databaseNames.nextElement();
	// DatabaseMetaData metaData = con.getMetaData(databaseName);
	//
	// // get table types from this database
	// ResultSet rs = metaData.getTableTypes();
	// Vector tableTypes = new Vector(4,2);
	// while (rs.next())
	// tableTypes.add(rs.getString(1));
	//
	// int numTableTypes = tableTypes.size();
	// String[] tableTypesArray = new String[numTableTypes];
	// for (int i = 0; i < numTableTypes; i++)
	// tableTypesArray[i] = (String) tableTypes.elementAt(i);
	//
	// String[] testTypes = { "ALIAS", "HIERARCHY TABLE", "MATERIALIZED QUERY
	// TABLE", "NICKNAME", "TABLE",
	// "TYPED TABLE", "TYPED VIEW", "VIEW" };
	// // get tables from this database
	// rs = metaData.getTables(null, "%", "%", testTypes);
	// Vector tableNames = new Vector(4,2);
	// while (rs.next())
	// tableNames.add(rs.getString(3));
	//
	// tables.put(databaseName, tableNames);
	// }
	// }
	// catch (SQLException e) {
	// e.printStackTrace();
	// }
	//
	// return tables;
	// }

	public Hashtable getDatabaseTablesFiltered(final String databaseName,
			final Vector columns, final boolean columnsFiltered) {
		logger.debug("filtering table columns");
		Hashtable tables = getDatabaseTablesInfo(databaseName, false);
		Hashtable filteredData = new Hashtable();
		Enumeration tableNames = tables.keys();
		Vector tableColumns;
		Vector filterColumns;
		String tableName;
		ColumnDescriptor column;

		boolean matches = false;

		while (tableNames.hasMoreElements()) {
			tableName = (String) tableNames.nextElement();
			tableColumns = (Vector) tables.get(tableName);
			filterColumns = new Vector(4, 2);

			for (int i = 0; i < tableColumns.size(); i++) {
				column = (ColumnDescriptor) tableColumns.get(i);
				if (columns.contains(column))
					try {
						matches = true;

						if (columnsFiltered)
							filterColumns.add(column.clone(false));
						else
							break;
					} catch (CloneNotSupportedException e) {
						logger.error("Could not clone column " + column, e);
					}
			}

			if (!columnsFiltered && matches) {
				for (int i = 0; i < tableColumns.size(); i++) {
					column = (ColumnDescriptor) tableColumns.get(i);
					try {
						filterColumns.add(column.clone(false));
					} catch (CloneNotSupportedException e) {
						logger.error("Could not clone column " + column, e);
					}
				}
			}

			matches = false;

			if (filterColumns.size() > 0)
				filteredData.put(tableName, filterColumns);

		}

		logger.debug("filtered table columns" + filteredData);
		return filteredData;
	}

	public Hashtable getDatabaseTablesFiltered(final String databaseName,
			final Vector columns) {
		return getDatabaseTablesFiltered(databaseName, columns, true);
	}

	public Hashtable getDatabaseTablesInfoWithColumnsInScope(
			final String databaseName, final String policyName) {
		logger.debug("getting database tables with columns in scope");
		Hashtable allTables = getDatabaseTablesInfo(databaseName, false);
		Hashtable info = new Hashtable();
		String table;
		ColumnDescriptor column;
		Vector columns;
		Vector columnsInScope;

		Enumeration tables = allTables.keys();

		while (tables.hasMoreElements()) {
			table = (String) tables.nextElement();
			columns = (Vector) allTables.get(table);
			columnsInScope = new Vector(4, 2);

			for (int j = 0; j < columns.size(); j++) {
				column = (ColumnDescriptor) columns.get(j);
				if (isInScope(policyName, column))
					columnsInScope.add(column);
			}

			if (columnsInScope.size() > 0)
				info.put(table, columnsInScope);
		}

		logger.debug("database tables with columns in scope:" + info);
		return info;
	}

	/**
	 * @param databaseName
	 * @return Hashtable containing key as table names and value as table
	 *         columns
	 */
	public Hashtable getDatabaseTablesInfo(final String databaseName, boolean includeDroppedColumns) {
		return getDatabaseTablesInfo(databaseName, false, includeDroppedColumns);
	}

	public Hashtable getDatabaseTablesInfo(final String databaseName,
			final boolean backlogOnly, boolean includeDroppedColumns) {

		logger.debug("getting database tables info");

		Hashtable info = new Hashtable();

		Vector tables = getConnectedDatabaseTables(databaseName);

		for (int i = 0; i < tables.size(); i++) {
			final TableDescriptor table = (TableDescriptor) tables.get(i);

			if (backlogOnly)
				if (!hasBacklogTable(table))
					continue;

			info.put(table.toString(), getDatabaseColumns(table, includeDroppedColumns));// string,
			// columnDescriptor
			// {
			// final Vector columnNames = getDatabaseColumns(table);
			// final Vector columns = new Vector(4,2);
			// for(int j=0; j<columnNames.size(); j++)
			// columns.add(table.schemaName+"."+table.tableName+"."+columnNames.get(j));
			// info.put(table.toString(),columns);
			// }
		}

		logger.debug("database tables info:" + info);

		return info;
	}

	public Vector getConnectedDatabaseTables(String databaseName) {
		return getConnectedDatabaseTables(databaseName, true);
	}

	// rs is filtered to not include any system tables or HDB related
	// tables
	private Vector getConnectedDatabaseTables(String databaseName,
			boolean filterHDBTables) {
		Vector tableNames = new Vector(4, 2);

		logger.debug("getting connected database tables");

		try {
			final DatabaseMetaData metadata = con.getMetaData(databaseName);

			// get table types from this database
			ResultSet rs = metadata.getTableTypes();
			Vector tableTypes = new Vector(4, 2);
			while (rs.next())
				tableTypes.add(rs.getString(1));

			rs.close();

			int numTableTypes = tableTypes.size();
			String[] tableTypesArray = new String[numTableTypes];
			for (int i = 0; i < numTableTypes; i++)
				tableTypesArray[i] = (String) tableTypes.elementAt(i);

			final Vector tableTypesResource = new Vector(4, 2);
			final StringTokenizer stringTokenizer = new StringTokenizer(
					getResource("metadata.table.types"), ",");
			while (stringTokenizer.hasMoreTokens())
				tableTypesResource.add(stringTokenizer.nextToken().trim());

			final String[] tableNameTypes = new String[tableTypesResource
					.size() + 1];

			for (int i = 0; i < tableTypesResource.size(); i++)
				tableNameTypes[i] = (String) tableTypesResource.get(i);

			// get tables from this database
			rs = metadata.getTables(null, "%", "%", tableNameTypes);

			while (rs.next()) {

				String schemaName = rs.getString("table_schem");
				String tableName = rs.getString("table_name");

				if (!excludedSchemaNames.contains(schemaName))
					if (!filterHDBTables
							|| (!schemaName
							    .equalsIgnoreCase(getResource("metadata.schema"))
							    // 01/05/2007 jk && !tableName.endsWith(getResource("metadata.table.suffix.association"))
							    // 01/05/2007 jk && !tableName.endsWith(getResource("metadata.table.suffix.choices"))
							    && !tableName.startsWith(getResource("backlog.table.prefix1"))))
						tableNames.add(new TableDescriptor(databaseName, rs
								.getString("TABLE_SCHEM"), tableName));
			}
			rs.close();

		} catch (SQLException e) {
			logger.error("getting connected database(\"" + databaseName
					+ "\") tables, ", e);
		}

		logger.debug("connected database tables:" + tableNames);

		return Utility.sortVector(tableNames);
	}

	public boolean hasBacklogMetaData(String databaseName) {
		try {
			final String[] tables = {
					getResource("metadata.table.audit.descriptions"),
					getResource("metadata.table.audit"),
					getResource("metadata.table.audit.purposes"),
					getResource("metadata.table.audit.accessors"),
					getResource("metadata.table.audit.recipients"),
					getResource("metadata.table.audit.projection_columns"),
					getResource("metadata.table.audit.query_log"),
					getResource("metadata.table.audit.views"),
					getResource("metadata.table.audit.dropped_columns") };

			final DatabaseMetaData metaData = con.getMetaData(databaseName);
			ResultSet rs;

			logger.debug("checking for audit metadata");

			for (int i = 0; i < tables.length; i++) {
				rs = metaData.getTables("", getResource("metadata.schema"),
						tables[i], null);
				if (rs.next()) {
					rs.close();
					return true;
				}
			}

			return false;
		} catch (SQLException e) {
			logger.error("checking backlog metadata, ", e);
			return false;
		}
	}

	public boolean hasBacklogTable(String databaseName, String schemaName,
			String tableName) {
		final TableDescriptor table = new TableDescriptor(databaseName,
				schemaName, tableName);
		return hasBacklogTable(table);
	}

	public boolean hasBacklogTable(TableDescriptor table) {
		if (!hasBacklogMetaData(table.databaseName))
			return false;
		else
			return (getBacklogTableCount(table) > 0);

	}

	public Vector getBacklogTablesWithSchemaChange(String databaseName) {
		logger.debug("getting backlog tables with schema change");
		final Vector changedTables = new Vector(4, 2);
		final Vector tables = getConnectedDatabaseTables(databaseName);

		for (int i = 0; i < tables.size(); i++) {
			final TableDescriptor table = (TableDescriptor) tables.get(i);
			if (!isBacklogTable(table)) {
				if (hasBacklogTableSchemaChange(table))
					changedTables.add(table);
			}
		}
		logger.debug("backlog tables with schema change:" + changedTables);
		return Utility.sortVector(changedTables);
	}

	public boolean hasBacklogTableSchemaChange(String databaseName) {
		final Vector tables = getBacklogTablesWithSchemaChange(databaseName);

		if (!tables.isEmpty())
			MessageBox.show(controlCenter,
					getResource("label.schema.change.captured"),
					getResource("label.schema.change") + "<br>" + tables,
					MessageBox.ICON_INFO);

		return !tables.isEmpty();
	}

	/**
	 * @param table
	 *            Table to be tested.
	 * @return true iff the schema (column names and column types) of the given
	 *         table and the schema of the corresponding backlog table differ.
	 *         We ignore the extra meta data columns of the backlog table for
	 *         the comparison. Returns false, if the backlog of the table does
	 *         not exist.
	 */
	public boolean hasBacklogTableSchemaChange(TableDescriptor table) {
		logger.debug("checking table for schema change, table=" + table);

		// A missing backlog for a table means a schema change.
		if (!hasBacklogTable(table))
			return false;

		final TableDescriptor backlog = getBacklogOfTableMostRecent(table);

		if (backlog == null)
			return false;
		else {
			final Vector columns = getDatabaseColumns(table, false);
			final String[] ignoredBacklogTableMetaDateColumnNames = Utility
					.parseArray(getResource("backlog.table.metadata.columns"));
			final Vector backlogColumns = getDatabaseColumns(backlog,
					ignoredBacklogTableMetaDateColumnNames, false);

			//
			// Replace the table name of the backlog table with the one of the
			// original table.
			//
			for (int i = 0; i < backlogColumns.size(); i++) {
				final ColumnDescriptor backlogColumn = (ColumnDescriptor) backlogColumns
						.get(i);
				backlogColumn.setTableName(table.tableName);
			}

			final TreeSet columnSet = new TreeSet(columns);
			final TreeSet backlogColumnSet = new TreeSet(backlogColumns);

			return !columnSet.equals(backlogColumnSet);
		}
	}

	public boolean hasMetadataTables(String databaseName) {
		try {
			final String[] tables = {
					getResource("metadata.table.policy.scope"),
					getResource("metadata.table.policy"),
					getResource("metadata.table.policy.application_usage"),
					getResource("metadata.table.policy.descriptions"),
					// getResource("metadata.table.accessors"),
					// getResource("metadata.table.purposes"),
					// getResource("metadata.table.recipients"),
					getResource("metadata.table.entities") };

			final DatabaseMetaData metaData = con.getMetaData(databaseName);
			ResultSet rs;

			logger.debug("checking for policy metaddata");

			for (int i = 0; i < tables.length; i++) {
				rs = metaData.getTables("", getResource("metadata.schema"),
						tables[i], null);
				if (rs.next()) {
					rs.close();
					return true;
				}
			}

			return false;
		} catch (SQLException e) {
			logger.error("checking metadata", e);
			return false;
		}
	}

	/*
	 * Added by amol pujari 09/10/2006 to prevent user entering existing policy
	 */

	public Vector getExistingPolicies(String databaseName) {
		logger.debug("getting existing policies names");
		final Vector existingPolicies = new Vector(4, 2);
		try {
			// 001
			// String sql = "";
			// sql += "SELECT DISTINCT name \n";
			// sql += "FROM " + getResource("metadata.schema") + "." +
			// getResource("metadata.table.policy.descriptions");
			// ResultSet rs = con.query(databaseName,sql);
			ResultSet rs = con.getPreparedStatement(databaseName, 0)
					.executeQuery();

			while (rs.next())
				existingPolicies.add(rs.getString("name"));
			rs.close();
		} catch (SQLException e) {
			logger.error("getting existing policy names", e);
		}
		logger.debug("existing policies:" + existingPolicies);
		return existingPolicies;
	}

	/*
	 * Added by amol pujari 09/10/2006 to prevent user entering existing audit
	 */

	public Vector getExistingAudits(String databaseName) {
		logger.debug("getting existing audits names");
		final Vector existingAudits = new Vector(4, 2);
		try {
			// 002
			// String sql = "";
			// sql += "SELECT name \n";
			// sql += "FROM " + getResource("metadata.schema") +
			// ".audit_descriptions";
			// ResultSet rs = con.query(databaseName,sql);
			ResultSet rs = con.getPreparedStatement(databaseName, 1)
					.executeQuery();

			while (rs.next())
				existingAudits.add(rs.getString("name"));
			rs.close();
		} catch (SQLException e) {
			logger.error("getting existing audit names", e);
		}
		logger.debug("existing audits:" + existingAudits);
		return existingAudits;
	}

	public Hashtable getPolicies(String databaseName) {
		Hashtable policies = new Hashtable();

		logger.debug("getting policies");
		try {
			if (!hasMetadataTables(databaseName))
				return null;

			// 003
			// String sql = "";
			// sql += "SELECT name, version\n";
			// sql += "FROM " + getResource("metadata.schema") + "." +
			// getResource("metadata.table.policy.descriptions") + "\n";
			// sql += "ORDER BY name, version";
			// ResultSet rs = con.query(databaseName,sql);
			ResultSet rs = con.getPreparedStatement(databaseName, 2)
					.executeQuery();

			while (rs.next()) {
				final String policyName = rs.getString("name");
				final String versionName = rs.getString("version");

				Vector foundVersions = (Vector) policies.get(policyName);
				if (foundVersions == null)
					foundVersions = new Vector(4, 2);

				foundVersions.add(versionName);
				policies.put(policyName, foundVersions);
			}
			rs.close();
		} catch (SQLException e) {
			logger.error("getting existing policy names, versions", e);
		}

		logger.debug("policies:" + policies);
		return policies;
	}

	public Vector getDatabaseColumns(TableDescriptor table, boolean includeDroppedColumns) {
		return getDatabaseColumns(table, null, includeDroppedColumns);
	}

	public Vector getDatabaseColumns(TableDescriptor table,
			String[] ignoredColumnNames, boolean includeDroppedColumns) {
		logger.debug("getting database columns for table:" + table + ", droppedColumnsIncluded: "+includeDroppedColumns);

		Vector columns = new Vector(4, 2);

		try {
			final DatabaseMetaData metaData = con
					.getMetaData(table.databaseName);

			if (includeDroppedColumns) {
			    final PreparedStatement ps = con.getPreparedStatement(table.databaseName, 61);
			    setStringParameter(ps, 1, table.schemaName);
			    setStringParameter(ps, 2, table.tableName);
			    ResultSet rs = ps.executeQuery();
			    
			    while (rs.next()) {
				final String columnName = rs.getString(1);
				final int dataType = rs.getInt(2);
				final String typeName = rs.getString(3);
				final int colsize = rs.getInt(4);
				final int scale = rs.getInt(5);
				final int pos = rs.getInt(6);
				final String isnullable = rs.getString(7);
				
				boolean found = false;
				if (ignoredColumnNames != null)
				    for (int i = 0; !found && (i < ignoredColumnNames.length); i++)
					found = columnName.trim().toLowerCase().equals(
										       ignoredColumnNames[i].trim().toLowerCase());
				
				if (!found) {
				    final ColumnDescriptor columnDescriptor = new ColumnDescriptor(table.databaseName, table.schemaName, table.tableName, columnName,
												   dataType, typeName, colsize, scale, pos, isnullable);
				    columns.add(columnDescriptor);
				}
			    }
			    rs.close();
			}
			final ResultSet rs = metaData.getColumns("", table.schemaName, table.tableName, "%");

			while (rs.next()) {
				final String columnName = rs.getString("column_name");
				final int dataType = rs.getInt("data_type");
				final String typeName = rs.getString("type_name");
				final int colSize = rs.getInt("column_size");
				final int scale = rs.getInt("decimal_digits");
				final int pos = rs.getInt("ordinal_position");
				final String isNullable = rs.getString("is_nullable");

				boolean found = false;
				if (ignoredColumnNames != null)
					for (int i = 0; !found && (i < ignoredColumnNames.length); i++)
						found = columnName.trim().toLowerCase().equals(
								ignoredColumnNames[i].trim().toLowerCase());

				if (!found) {
					// if (columns == null)
					// columns = new Vector(4,2);
					//                    
					final ColumnDescriptor columnDescriptor = new ColumnDescriptor(
							table.databaseName, table.schemaName,
							table.tableName, columnName, dataType, typeName, colSize, scale, pos, isNullable);
					columns.add(columnDescriptor);
				}
			}
			rs.close();
		} catch (SQLException e) {
			logger.error("getting database columns for table(\"" + table
					+ "\"), ", e);
		}
		logger.debug("database columns" + columns);
		return Utility.sortVector(columns);
	}

	public boolean isInScope(String policyName, ColumnDescriptor column) {
		logger
				.debug("checking column in scope for" + policyName + "/"
						+ column);

		try {

			// 004
			// String sql = "";
			// sql += "SELECT 1\n";
			// sql += "FROM " + getResource("metadata.schema") + "." +
			// getResource("metadata.table.policy.scope") + "\n";
			// sql += "WHERE policyid = '" + toSQL(policyName) + "'" + " AND\n";
			// sql += " schema = '" + column.table.schemaName + "'" + " AND\n";
			// sql += " table = '" + column.table.tableName + "'" + " AND\n";
			// sql += " column = '" + column.columnName + "'" + " AND\n";
			// sql += " type = '" + column.typeName + "'";
			// ResultSet rs = con.query(column.table.databaseName,sql);
			final PreparedStatement ps = con.getPreparedStatement(
					column.table.databaseName, 3);
			ps.clearParameters();
			setStringParameter(ps, 1, policyName);
			setStringParameter(ps, 2, column.table.schemaName);
			setStringParameter(ps, 3, column.table.tableName);
			setStringParameter(ps, 4, column.columnName);
			// setStringParameter(ps, 5, column.typeName);
			ResultSet rs = ps.executeQuery();

			boolean inScope = rs.next();
			rs.close();

			logger.debug("column in scope for" + policyName + "/" + column
					+ "=true");
			return inScope;
		} catch (SQLException e) {
			logger.error("checking is in scope policy for policy(\""
					+ policyName + "\"), ", e);
		}

		logger.debug("column in scope for" + policyName + "/" + column
				+ "=false");
		return false;
	}

	public boolean isColumnInScopeUsed(String databaseName, String policyName,
			ColumnDescriptor column) {
		logger.debug("isColumnInScopeUsed:policyName:" + policyName);
		logger.debug("for column:" + column);

		try {
			final PreparedStatement ps = con.getPreparedStatement(
					column.table.databaseName, 59);
			ps.clearParameters();
			setStringParameter(ps, 1, policyName);
			setStringParameter(ps, 2, column.table.schemaName);
			setStringParameter(ps, 3, column.table.tableName);
			setStringParameter(ps, 4, column.columnName);
			ResultSet rs = ps.executeQuery();
			return rs.next();
		} catch (Exception e) {
			return false;
		}
	}

	public Vector getColumnsInScope(String databaseName, String policyName) {
		logger.debug("getting columns in scope for policy:" + policyName);

		Vector scopeColumns = new Vector(4, 2);

		try {
			// 005
			// String sql = "";
			// sql += "SELECT *\n";
			// sql += "FROM " + getResource("metadata.schema") + "." +
			// getResource("metadata.table.policy.scope") + "\n";
			// sql += "WHERE policyid = '" + toSQL(policyName) + "'";
			// ResultSet rs = con.query(databaseName,sql);
			ResultSet rs;
			if (policyName.length() > 0) {
				final PreparedStatement ps = con.getPreparedStatement(
						databaseName, 4);
				setStringParameter(ps, 1, policyName);
				rs = ps.executeQuery();
			} else
				rs = con.query(databaseName, "SELECT * FROM "
						+ getResource("metadata.schema") + "."
						+ getResource("metadata.table.policy.scope"));

			while (rs.next()) {
				scopeColumns.add(new ColumnDescriptor(databaseName, rs
						.getString("schema"), rs.getString("table"), rs
						.getString("column"), ""// rs.getString("type")
				));
			}

			rs.close();
		} catch (SQLException e) {
			logger.error("getting columns in scope for  policy(\"" + policyName
					+ "\"), ", e);
		}

		logger.debug("columns in scope" + scopeColumns);
		return Utility.sortVector(scopeColumns);
	}

	public boolean isEntityTable(TableDescriptor table) {
		logger.debug("is entity table:" + table);
		try {
			final DatabaseMetaData metadata = con
					.getMetaData(table.databaseName);
			boolean ret;

			ResultSet rs = metadata.getTables("",
					getResource("metadata.schema"),
					getResource("metadata.table.entities"), null);

			ret = rs.next();
			rs.close();

			if (!ret)
				return false; // TABLE_ENTITIES table
			// doesn't exist

			// 006
			// String sql = "";
			// sql += "SELECT 1\n";
			// sql += "FROM " + getResource("metadata.schema") + "." +
			// getResource("metadata.table.entities") + "\n";
			// sql += "WHERE schema = '" + table.schemaName + "' AND\n";
			// sql += " table = '" + table.tableName + "'";
			// rs = con.query(table.databaseName,sql);
			final PreparedStatement ps = con.getPreparedStatement(
					table.databaseName, 5);
			setStringParameter(ps, 1, table.schemaName);
			setStringParameter(ps, 2, table.tableName);
			rs = ps.executeQuery();

			ret = rs.next();
			rs.close();

			logger.debug("entity table:" + table + " :" + ret);
			return ret;

		} catch (SQLException e) {
			logger.error("checking any of entity for table(\"" + table
					+ "\"), ", e);
		}

		logger.debug("entity table:" + table + " :false");
		return false;
	}

	public Vector getEntities(String databaseName) {
		logger.debug("getting entities");
		final Vector allTables = getConnectedDatabaseTables(databaseName);
		final Vector resultTables = new Vector(4, 2);

		for (int i = 0; i < allTables.size(); i++) {
			final TableDescriptor table = (TableDescriptor) allTables
					.elementAt(i);

			if (isEntityTable(table))
				resultTables.add(table);
		}
		logger.debug("entities:" + resultTables);
		return Utility.sortVector(resultTables);
	}

	/*
	 * added by amol pujari 06/10/2006 to prevent user okaying existing version
	 * name
	 */

	public Vector getVersions(String databaseName, String policyName) {
		logger.debug("getting versions");
		final Vector existingVersions = new Vector(4, 2);
		try {
			// 007
			// String sql = "";
			// sql += "SELECT VERSION \n";
			// sql += "FROM " + getResource("metadata.schema") + "." +
			// getResource("metadata.table.policy.descriptions") + " \n" ;
			// sql += "WHERE NAME = '"+policyName+"' \n";
			// ResultSet rs = con.query(databaseName,sql);
			final PreparedStatement ps = con.getPreparedStatement(databaseName,
					6);
			setStringParameter(ps, 1, policyName);
			ResultSet rs = ps.executeQuery();

			while (rs.next())
				existingVersions.add(rs.getString("VERSION"));

			rs.close();
		} catch (SQLException e) {
			logger.error("getting versions for  policy(\"" + policyName
					+ "\"), ", e);
		}
		logger.debug("versions:" + existingVersions);
		return existingVersions;
	}

	public Vector getNonEntities(String databaseName) {
		logger.debug("getting non entities");
		final Vector allTables = getConnectedDatabaseTables(databaseName);
		final Vector resultTables = new Vector(4, 2);

		for (int i = 0; i < allTables.size(); i++) {
			final TableDescriptor table = (TableDescriptor) allTables
					.elementAt(i);
			if (!isEntityTable(table)) {
				resultTables.add(table);
			}
		}
		logger.debug("non entities:" + resultTables);
		return Utility.sortVector(resultTables);
	}

	public Vector getApplicationNames(String databaseName) {
		logger.debug("getting application names");
		final Vector entries = new Vector(4, 2);
		try {

			// 008
			// String sql = "";
			// sql += "SELECT DISTINCT appid\n";
			// sql += "FROM " + getResource("metadata.schema") + "." +
			// getResource("metadata.table.policy.application_usage");
			// ResultSet rs = con.query(databaseName,sql);
			ResultSet rs = con.getPreparedStatement(databaseName, 7)
					.executeQuery();

			while (rs.next())
				entries.add(rs.getString("appid"));

			rs.close();
		} catch (SQLException e) {
			logger.error("getting application names", e);
		}
		logger.debug("application names:" + entries);
		return Utility.sortVector(entries);
	}

	public Vector getEntries(String databaseName, String tableName) {

		logger.debug("getting entries for table:" + tableName);
		Vector entries = new Vector(4, 2);

		try {

			// 009
			String sql = "";
			sql += "SELECT * \n";
			sql += "FROM " + getResource("metadata.schema") + "." + tableName;
			ResultSet rs = con.query(databaseName, sql);

			// tableName= getResource("metadata.schema")+"."+tableName;
			// ps[8].setString(1,tableName);
			// ResultSet rs = ps[8].executeQuery();

			if (!tableName
					.equalsIgnoreCase(getResource("metadata.table.entities")))
				while (rs.next())
					entries.add(rs.getString("name"));
			else
				while (rs.next())
					entries.add(new TableDescriptor(databaseName, rs
							.getString("schema"), rs.getString("table")));

			rs.close();
		} catch (SQLException e) {
			logger.error("getting entries for  table(\"" + tableName + "\"), ",
					e);

		}

		logger.debug("entries:" + entries);
		return Utility.sortVector(entries);
	}

	public Vector getTablesWithBacklog(String databaseName) {
		logger.debug("getting backlog tables");
		Vector resultTables = new Vector(4, 2);

		try {
			// 010
			// String sql = " SELECT DISTINCT TSCHEMA, TNAME FROM "+
			// getResource("metadata.schema")
			// +"."+getResource("metadata.table.audit.views");
			// final ResultSet rs = con.query(databaseName,sql);
			ResultSet rs = con.getPreparedStatement(databaseName, 9)
					.executeQuery();

			while (rs.next()) {
				final TableDescriptor table = new TableDescriptor(databaseName,
						rs.getString("TSCHEMA").trim(), rs.getString("TNAME")
								.trim());
				resultTables.add(table);
			}
			rs.close();
		} catch (SQLException e) {
			logger.error("getting tables with backlog", e);
		}

		logger.debug("backlog tables:" + resultTables);
		return Utility.sortVector(resultTables);
	}

	public boolean isBacklogTable(TableDescriptor table) {
		final String backlogTableNamePrefix = getResource("backlog.table.prefix1");
		return table.tableName.startsWith(backlogTableNamePrefix);
	}

	public String importPolicyRules(final Version version, final Vector rules) {
		logger.debug("importing rules to:" + version.collectionName + " / "
				+ version.versionName);
		String msg = "";
		Rule rule;

		try {
			PreparedStatement ps = con.getPreparedStatement(
					version.databaseName, 57);

			for (int i = 0; i < rules.size(); i++) {
				rule = (Rule) rules.get(i);

				try {
					logger.debug("importing rule:" + rule);

					rule.condition = rule.condition.replaceAll("'"
							+ rule.version + "'", "'" + version.versionName
							+ "'");

					setStringParameter(ps, 1, rule.policyName);
					setStringParameter(ps, 2, rule.name);
					setStringParameter(ps, 3, version.versionName);
					setStringParameter(ps, 4, rule.purposes.get(0).toString());
					setStringParameter(ps, 5, rule.accessors.get(0).toString());
					setStringParameter(ps, 6, rule.recipients.get(0).toString());
					setStringParameter(
							ps,
							7,
							(((ColumnDescriptor) (rule.columns.get(0))).table.schemaName));
					setStringParameter(
							ps,
							8,
							(((ColumnDescriptor) (rule.columns.get(0))).table.tableName));
					setStringParameter(
							ps,
							9,
							(((ColumnDescriptor) (rule.columns.get(0))).columnName));
					setIntParameter(ps, 10, (((ColumnDescriptor) (rule.columns
							.get(0))).pseudonym) ? 1 : 0);
					setStringParameterNotToSQL(ps, 11, rule.condition);
					ps.executeUpdate();
					con.commit(version.databaseName);

				} catch (SQLException e) {
					final int index = e.getMessage().indexOf(
							"[IBM][CLI Driver][DB2/NT] SQL0803N");

					if (index >= 0) {
						msg += "<br>&nbsp;&nbsp;&nbsp;&nbsp; Matching Rule found for rule:"
								+ rule;
					} else {
						msg += "<br><br>&nbsp;&nbsp;&nbsp;&nbsp; Could not import rule "
								+ rule
								+ " &nbsp;&nbsp;&nbsp;&nbsp;<br>&nbsp;&nbsp;&nbsp;&nbsp; Please see the error log for more detail";
					}
					con.rollback(version.databaseName);
				}
			}
		} catch (SQLException e) {
			msg += "<br><br>&nbsp;&nbsp;&nbsp;&nbsp; Could not start importing "
					+ " &nbsp;&nbsp;&nbsp;&nbsp;<br>&nbsp;&nbsp;&nbsp;&nbsp; Please see the error log for more detail";
			con.rollback(version.databaseName);
		}

		return msg;
	}

	public Vector getPolicyVersions(Version version) {
		logger.debug("getting policy versions for " + version);
		Vector versionNames = new Vector(4, 2);

		if (version.collectionName != null) {
			try {

				// 011
				// String sql = "";
				// sql += "SELECT VERSION\n";
				// sql += "FROM " + getResource("metadata.schema") + "." +
				// getResource("metadata.table.policy.descriptions") + "\n";
				// sql += "WHERE NAME = '" + toSQL(version.collectionName) +
				// "'";
				// ResultSet rs = con.query(version.databaseName,sql);
				final PreparedStatement ps = con.getPreparedStatement(
						version.databaseName, 10);
				setStringParameter(ps, 1, toSQL(version.collectionName));
				ResultSet rs = ps.executeQuery();

				while (rs.next()) {
					final String versionName = rs.getString("VERSION");
					versionNames.add(versionName);
				}

				rs.close();
			} catch (SQLException e) {
				logger.error("getting policy versions for (\"" + version
						+ "\"), ", e);

			}
		}
		logger.debug("policy versions:" + versionNames);
		return Utility.sortVector(versionNames);
	}

	public Version getPolicyVersionType(final Version version) {
		logger.debug("getting policy version type for policy:" + version);
		if (version.collectionName != null) {
			try {

				// 012
				// String sql = "";
				// sql += "SELECT type, enabled \n";
				// sql += "FROM " + getResource("metadata.schema") + "." +
				// getResource("metadata.table.policy.descriptions") + "\n";
				// sql += "WHERE NAME = '" + toSQL(version.collectionName) + "'
				// ";
				//                		
				// if(version.versionName!=null)
				// //013
				// sql += "AND VERSION = '" + toSQL(version.versionName) + "' ";
				//                
				// sql+=" FETCH FIRST 1 ROW ONLY ";
				//
				// ResultSet rs = con.query(version.databaseName,sql);

				ResultSet rs;
				if (version.versionName != null) {
					final PreparedStatement ps = con.getPreparedStatement(
							version.databaseName, 12);
					setStringParameter(ps, 1, version.collectionName);
					setStringParameter(ps, 2, toSQL(version.versionName));
					rs = ps.executeQuery();
				} else {
					final PreparedStatement ps = con.getPreparedStatement(
							version.databaseName, 11);
					setStringParameter(ps, 1, version.collectionName);
					rs = ps.executeQuery();
				}

				rs.next();

				version.type = rs.getInt(1);
				logger.debug("type:" + version.type);

				if (version.versionName != null)
					version.enabled = rs.getInt(2);

				logger.debug("enabled:" + version.enabled);
				rs.close();
			} catch (SQLException e) {
				logger.error(
						"getting type, enabled information for policy for (\""
								+ version.collectionName + "\"), ", e);

			}
		}
		return version;
	}

	public Rule getPolicyVersionType(final String databaseName, final Rule rule) {
		logger.debug("getting policy version type for rule:" + rule);
		if (rule.policyName != null) {
			try {
				// 014
				// String sql = "";
				// sql += "SELECT type, enabled \n";
				// sql += "FROM " + getResource("metadata.schema") + "." +
				// getResource("metadata.table.policy.descriptions") + "\n";
				// sql += "WHERE NAME = '" + toSQL(rule.policyName) + "' AND
				// VERSION = '" + toSQL(rule.version) + "' ";
				// ResultSet rs = con.query(databaseName,sql);
				final PreparedStatement ps = con.getPreparedStatement(
						databaseName, 13);
				setStringParameter(ps, 1, rule.policyName);
				setStringParameter(ps, 2, rule.version);
				ResultSet rs = ps.executeQuery();

				rs.next();

				rule.versioningType = rs.getInt(1);
				rule.enabled = rs.getInt(2);

				logger.debug("rule.versioningType:" + rule.versioningType);
				logger.debug("rule.enabled:" + rule.enabled);

				rs.close();
			} catch (SQLException e) {
				logger.error(
						"getting type, enabled information for policy for (\""
								+ rule.policyName + "\"), ", e);

			}
		}

		return rule;
	}

	public Vector getPolicyRules(Version version) {
		logger.debug("getting policy rules for:" + version);
		Vector rules = new Vector(4, 2);
		try {

			// String sql = "";
			// //015
			// sql += "SELECT *\n";
			// sql += "FROM " + getResource("metadata.schema") + "." +
			// getResource("metadata.table.policy") + "\n";
			// sql += "WHERE policyid = " + "'" + toSQL(version.collectionName)
			// + "'";
			//            
			// if (version.versionName != null)
			// {
			// //016
			// sql += " AND\n version = " + "'" + toSQL(version.versionName) +
			// "'";
			// }
			//
			// ResultSet rs = con.query(version.databaseName,sql);

			ResultSet rs;
			if (version.versionName != null) {
				final PreparedStatement ps = con.getPreparedStatement(
						version.databaseName, 15);
				setStringParameter(ps, 1, version.collectionName);
				setStringParameter(ps, 2, version.versionName);
				rs = ps.executeQuery();
			} else {
				final PreparedStatement ps = con.getPreparedStatement(
						version.databaseName, 14);
				setStringParameter(ps, 1, version.collectionName);
				rs = ps.executeQuery();
			}

			while (rs.next()) {
				Rule rule = new Rule();
				rule.databaseName = version.databaseName;
				rule.name = rs.getString("ruleid");
				rule.version = rs.getString("VERSION");
				rule.policyName = version.collectionName;
				final String typeNameDontCare = "";
				String pseudonymString = rs.getString("pseudonym");
				ColumnDescriptor column = new ColumnDescriptor(
						version.databaseName, rs.getString("schema"), rs
								.getString("table"), rs.getString("column"),
						pseudonymString.equals("1"), typeNameDontCare);
				rule.columns.add(column);
				rule.purposes.add(rs.getString("purpose"));
				rule.accessors.add(rs.getString("accessor"));
				rule.recipients.add(rs.getString("recipient"));
				rule.condition = rs.getString("condition");
				rule.entities = parseEntities(rule.condition,
						version.databaseName, false);

				rule = getPolicyVersionType(version.databaseName, rule);

				rules.add(rule);
			}

			rs.close();

		} catch (SQLException e) {
			logger.error("getting policy rules for (\"" + version + "\"), ", e);

		}

		logger.debug("policy rules:" + rules);
		return Utility.sortVector(rules);
	}

	public Vector getTasks(Version version) {
		logger.debug("getting tasks for:" + version);
		final Vector tasks = new Vector(4, 2);

		try {

			// 017
			// String sql = "";
			// sql += "SELECT a.auditid, a.taskid, a.version, a.purpose,
			// a.accessor, a.recipient, c.schema, c.table, c.column,
			// a.condition, a.begin, a.end\n";
			// sql += "FROM " + getResource("metadata.schema") + "." +
			// getResource("metadata.table.audit") + " AS a, " +
			// getResource("metadata.schema") + "."
			// + getResource("metadata.table.audit.projection_columns") + " AS
			// c\n";
			// sql += "WHERE a.auditid = " + "'" + toSQL(version.collectionName)
			// + "' AND\n";
			// sql += " a.version = " + "'" + toSQL(version.versionName) + "'
			// AND\n";
			// sql += " a.auditid = c.auditid AND\n";
			// sql += " a.taskid = c.taskid AND\n";
			// sql += " a.version = c.version\n";
			//
			// ResultSet rs = con.query(version.databaseName,sql);
			final PreparedStatement ps = con.getPreparedStatement(
					version.databaseName, 16);
			setStringParameter(ps, 1, version.collectionName);
			setStringParameter(ps, 2, version.collectionName);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				Task task = new Task();
				task.databaseName = version.databaseName;
				task.policyName = rs.getString("auditid");
				task.version = rs.getString("version");
				task.name = rs.getString("taskid");
				task.purposes.add(rs.getString("purpose"));
				task.accessors.add(rs.getString("accessor"));
				task.recipients.add(rs.getString("recipient"));
				task.condition = rs.getString("condition");
				task.begin = rs.getTimestamp("BEGIN");
				task.end = rs.getTimestamp("END");
				final String typeNameDontCare = "";
				ColumnDescriptor column = new ColumnDescriptor(
						version.databaseName, rs.getString("schema"), rs
								.getString("table"), rs.getString("column"),
						typeNameDontCare);
				task.columns.add(column);
				tasks.add(task);
			}

			rs.close();
		} catch (SQLException e) {
			logger.error("getting tasks for (\"" + version + "\"), ", e);

		}
		logger.debug("tasks:" + tasks);
		return Utility.sortVector(tasks);
	}

	// public Vector getRulesNames()
	// {
	// final Vector existingNames = new Vector(4,2);
	// return existingNames;
	// }

	public Vector getRulesForPolicyVersion(String policy, String version,
			String databaseName) {

		logger.debug("getting rules for policy version:" + policy + "/"
				+ version);
		final Vector existingRules = new Vector(4, 2);
		try {
			// 018
			// String sql = "";
			// sql += "SELECT RULEID \n";
			// sql += "FROM " + getResource("metadata.schema") + "." +
			// getResource("metadata.table.policy") + "\n";
			//            
			// if(policy.length()>0)
			// //019
			// sql += " WHERE POLICYID = '"+toSQL(policy)+"' AND VERSION =
			// '"+toSQL(version)+"' ";
			//            
			// final ResultSet rs = con.query(databaseName,sql);

			ResultSet rs;
			if (policy.length() > 0) {
				final PreparedStatement ps = con.getPreparedStatement(
						databaseName, 18);
				setStringParameter(ps, 1, policy);
				setStringParameter(ps, 2, version);
				rs = ps.executeQuery();
			} else {
				rs = con.getPreparedStatement(databaseName, 17).executeQuery();
			}

			while (rs.next())
				existingRules.add(rs.getString("RULEID"));

			rs.close();
		} catch (SQLException e) {
			logger.error("getting rules for policy(\"" + policy
					+ "\"), version(\"" + version + "\")", e);
		}
		logger.debug("rules:" + existingRules);
		return Utility.sortVector(existingRules);
	}

	public Vector getTaskNames(String databaseName, String audit) {
		logger.debug("getting tasks names for audit:" + audit);
		final Vector taskNames = new Vector(4, 2);

		try {
			// 020
			// final String sql = "SELECT taskid FROM " +
			// getResource("metadata.schema") +
			// "."+getResource("metadata.table.audit")+" WHERE
			// auditid='"+audit+"' ";
			// final ResultSet rs = con.query(databaseName,sql);
			final PreparedStatement ps = con.getPreparedStatement(databaseName,
					19);
			setStringParameter(ps, 1, audit);
			final ResultSet rs = ps.executeQuery();

			while (rs.next())
				taskNames.add(rs.getString(1));
		} catch (SQLException e) {
			logger.error("getting tasks for audit(\"" + audit
					+ "\"), database(\"" + databaseName + "\"), ", e);
		}

		logger.debug("tasks:" + taskNames);
		return Utility.sortVector(taskNames);
	}

	public Vector getTasksWithNestedColumns(Version version) {
		logger.debug("getting tasks with nested columns for:" + version);
		final Vector tasks = new Vector(4, 2);

		try {

			String sql = "";
			sql += "SELECT auditid, taskid, version, condition, begin, end\n";
			sql += "FROM   " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.audit") + "\n";

			if ((version.collectionName != null)
					|| (version.versionName != null)) {
				sql += "WHERE\n";

				if (version.collectionName != null)
					sql += "auditid = " + "'" + toSQL(version.collectionName)
							+ "'";

				if ((version.collectionName != null)
						&& (version.versionName != null))
					sql += " AND\n";

				if (version.versionName != null)
					sql += "version = " + "'" + toSQL(version.versionName)
							+ "'";
			}

			ResultSet rs = con.query(version.databaseName, sql);

			while (rs.next()) {

				Task task = new Task();
				task.databaseName = version.databaseName;
				task.policyName = rs.getString("auditid");
				task.version = rs.getString("version");
				task.condition = rs.getString("condition");
				task.begin = rs.getTimestamp("begin");
				task.end = rs.getTimestamp("end");

				final Version tempVersion = new Version(version.databaseName,
						task.policyName, task.version);

				final String taskName = rs.getString("taskid");
				final Vector purposes = getTaskPurposes(tempVersion, taskName);
				final Vector accessors = getTaskAccessors(tempVersion, taskName);
				final Vector recipients = getTaskRecipients(tempVersion,
						taskName);
				final Vector columns = getTaskColumns(tempVersion, taskName);

				task.name = taskName;
				task.purposes = purposes;
				task.accessors = accessors;
				task.recipients = recipients;
				task.columns = columns;

				tasks.add(task);
			}

			rs.close();
		} catch (SQLException e) {
			logger.error("getting tasks with nested columns for version(\""
					+ version + "\"), ", e);

		}
		logger.debug("tasks:" + tasks);
		return Utility.sortVector(tasks);
	}

	private Vector getTaskPurposes(Version version, String taskName) {
		logger.debug("getting tasks purposes for:" + version + "/" + taskName);
		final Vector purposes = new Vector(4, 2);

		try {
			// 021
			// sql += "SELECT purpose\n";
			// sql += "FROM " + getResource("metadata.schema") +
			// ".audit_purposes\n";
			// sql += "WHERE auditid = " + "'" + toSQL(version.collectionName) +
			// "' AND\n";
			// sql += " version = " + "'" + toSQL(version.versionName) + "'
			// AND\n";
			// sql += " taskid = " + "'" + toSQL(taskName) + "'";
			// ResultSet rs = con.query(version.databaseName,sql);
			final PreparedStatement ps = con.getPreparedStatement(
					version.databaseName, 20);
			setStringParameter(ps, 1, version.collectionName);
			setStringParameter(ps, 2, version.versionName);
			setStringParameter(ps, 3, taskName);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				final String value = rs.getString("purpose");
				purposes.add(value);
			}

			rs.close();
		} catch (SQLException e) {
			logger.error("getting task purposes for version(\"" + version
					+ "\") and task(\"" + taskName + "\"), ", e);

		}
		logger.debug("purposes:" + purposes);
		return purposes;
	}

	private Vector getTaskAccessors(Version version, String taskName) {
		logger.debug("getting task accessors for:" + version + "/" + taskName);
		final Vector accessors = new Vector(4, 2);
		try {

			// 022
			// String sql = "";
			// sql += "SELECT accessor\n";
			// sql += "FROM " + getResource("metadata.schema") +
			// ".audit_accessors\n";
			// sql += "WHERE auditid = " + "'" + toSQL(version.collectionName) +
			// "' AND\n";
			// sql += " version = " + "'" + toSQL(version.versionName) + "'
			// AND\n";
			// sql += " taskid = " + "'" + toSQL(taskName) + "'";
			// ResultSet rs = con.query(version.databaseName,sql);
			final PreparedStatement ps = con.getPreparedStatement(
					version.databaseName, 21);
			setStringParameter(ps, 1, version.collectionName);
			setStringParameter(ps, 2, version.versionName);
			setStringParameter(ps, 3, taskName);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				final String value = rs.getString("accessor");
				accessors.add(value);
			}

			rs.close();
		} catch (SQLException e) {
			logger.error("getting task accessors for version(\"" + version
					+ "\") and task(\"" + taskName + "\"), ", e);

		}
		logger.debug("accessors:" + accessors);
		return accessors;
	}

	private Vector getTaskRecipients(Version version, String taskName) {
		logger.debug("getting task recipients for:" + version + "/" + taskName);
		final Vector recipients = new Vector(4, 2);
		try {

			// 023
			// String sql = "";
			// sql += "SELECT recipient\n";
			// sql += "FROM " + getResource("metadata.schema") +
			// ".audit_recipients\n";
			// sql += "WHERE auditid = " + "'" + toSQL(version.collectionName) +
			// "' AND\n";
			// sql += " version = " + "'" + toSQL(version.versionName) + "'
			// AND\n";
			// sql += " taskid = " + "'" + toSQL(taskName) + "'";
			// ResultSet rs = con.query(version.databaseName,sql);
			final PreparedStatement ps = con.getPreparedStatement(
					version.databaseName, 22);
			setStringParameter(ps, 1, version.collectionName);
			setStringParameter(ps, 2, version.versionName);
			setStringParameter(ps, 3, taskName);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				final String value = rs.getString("recipient");
				recipients.add(value);
			}

			rs.close();
		} catch (SQLException e) {
			logger.error("getting task recipients for version(\"" + version
					+ "\") and task(\"" + taskName + "\"), ", e);

		}
		logger.debug("recipients:" + recipients);
		return recipients;
	}

	private Vector getTaskColumns(Version version, String taskName) {
		logger.debug("getting task columns for:" + version + "/" + taskName);
		final Vector columns = new Vector(4, 2);
		try {

			// 024
			// String sql = "";
			// sql += "SELECT schema, table, column\n";
			// sql += "FROM " + getResource("metadata.schema") +
			// ".audit_projection_columns\n";
			// sql += "WHERE auditid = " + "'" + toSQL(version.collectionName) +
			// "' AND\n";
			// sql += " version = " + "'" + toSQL(version.versionName) + "'
			// AND\n";
			// sql += " taskid = " + "'" + toSQL(taskName) + "'";
			// ResultSet rs = con.query(version.databaseName,sql);
			final PreparedStatement ps = con.getPreparedStatement(
					version.databaseName, 23);
			setStringParameter(ps, 1, version.collectionName);
			setStringParameter(ps, 2, version.versionName);
			setStringParameter(ps, 3, taskName);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				final String databaseName = version.databaseName;
				final String schemaName = rs.getString("schema");
				final String tableName = rs.getString("table");
				final String columnName = rs.getString("column");

				//
				// Determine the type name.
				//
				final DatabaseMetaData metaData = con
						.getMetaData(version.databaseName);
				final ResultSet columnsResultSet = metaData.getColumns("",
						schemaName, tableName,
						columnName);

				if (columnsResultSet.next()) {
				    final int dataType = columnsResultSet.getInt("data_type");
				    final String typeName = columnsResultSet.getString("type_name");
				    final int colsize = columnsResultSet.getInt("column_size");
				    final int scale = columnsResultSet.getInt("decimal_digits");
				    final int pos = columnsResultSet.getInt("ordinal_position");
				    final String isnullable = columnsResultSet.getString("is_nullable");
				    
				    final ColumnDescriptor columnDescriptor = new ColumnDescriptor(
												   databaseName, schemaName, tableName, columnName,
												   dataType, typeName, colsize, scale, pos, isnullable);
				    columns.add(columnDescriptor);
				    columnsResultSet.close();
				}
				else {
				    columnsResultSet.close();
				    final PreparedStatement ps2 = con.getPreparedStatement(databaseName, 62);
				    setStringParameter(ps2, 1, schemaName);
				    setStringParameter(ps2, 2, tableName);
				    setStringParameter(ps2, 3, columnName);
				    ResultSet rs2 = ps2.executeQuery();
				    
				    if (rs2.next()) {
					final int dataType = rs2.getInt(1);
					final String typeName = rs2.getString(2);
					final int colsize = rs2.getInt(3);
					final int scale = rs2.getInt(4);
					final int pos = rs2.getInt(5);
					final String isnullable = rs2.getString(6);
					
					final ColumnDescriptor columnDescriptor = new ColumnDescriptor(databaseName, schemaName, tableName, columnName,
												       dataType, typeName, colsize, scale, pos, isnullable);
					columns.add(columnDescriptor);
					rs2.close();
				    }
				    else {
					rs2.close();
					throw new SQLException("Column "+schemaName+"."+tableName+"."+columnName+" not found!");
				    }
				}

			}

			rs.close();
		} catch (SQLException e) {
			logger.error("getting task columns for version(\"" + version
					+ "\") and task(\"" + taskName + "\"), ", e);

		}
		logger.debug("columns:" + columns);
		return columns;
	}

    public Vector getDroppedColumns(String databaseName, String schemaName, String tableName) {
	logger.debug("getting dropped columns for:" + schemaName+"."+tableName);
	final Vector columns = new Vector(4, 2);
	try {
	    final PreparedStatement ps = con.getPreparedStatement(databaseName, 61);
	    setStringParameter(ps, 1, schemaName);
	    setStringParameter(ps, 2, tableName);
	    ResultSet rs = ps.executeQuery();
	    
	    while (rs.next()) {
		final String columnName = rs.getString(1);
		final int dataType = rs.getInt(2);
		final String typeName = rs.getString(3);
		final int colsize = rs.getInt(4);
		final int scale = rs.getInt(5);
		final int pos = rs.getInt(6);
		final String isnullable = rs.getString(7);
		
		final ColumnDescriptor columnDescriptor = new ColumnDescriptor(databaseName, schemaName, tableName, columnName,
									       dataType, typeName, colsize, scale, pos, isnullable);
		columns.add(columnDescriptor);
	    }
	    
	    rs.close();
	} catch (SQLException e) {
	    logger.error("getting dropped columns for "+ schemaName+"."+tableName, e);
	    
	}
	logger.debug("columns:" + columns);
	return columns;
    }
    
	public TreeMap getAudits(String databaseName) {
		logger.debug("getting audits");
		TreeMap audits = null;

		try {
			if (!hasBacklogMetaData(databaseName))
				return null;

			// 025
			// String sql = "";
			// sql += "SELECT name, version FROM " +
			// getResource("metadata.schema") + ".audit_descriptions";
			// ResultSet rs = con.query(databaseName,sql);
			ResultSet rs = con.getPreparedStatement(databaseName, 24)
					.executeQuery();

			// If we use the TreeMap class, the keys are sorted.
			audits = new TreeMap();
			while (rs.next()) {
				String auditName = rs.getString("name");
				String versionName = rs.getString("version");

				Vector foundVersions = (Vector) audits.get(auditName);
				if (foundVersions == null)
					foundVersions = new Vector(4, 2);

				foundVersions.add(versionName);
				audits.put(auditName, foundVersions);
			}
			rs.close();
		} catch (SQLException e) {
			logger.error("getting audits", e);

		}
		logger.debug("audits:" + audits);
		return audits;
	}

	public Vector getAuditResultQueries(String databaseName,
			Vector resultQueryIdentifiers) {
		logger.debug("getting audit result queries");
		final Vector queries = new Vector(4, 2);

		if (resultQueryIdentifiers.size() > 0) {
			try {

				String idList = "";
				for (int i = 0; i < resultQueryIdentifiers.size(); i++) {
					if (i > 0)
						idList += ", ";
					idList += resultQueryIdentifiers.get(i);
				}

				// 026
				String sql = "";
				sql += "SELECT idkey, query, purpose, usr, recipient, spec_recip, isolation, tim1, tim2\n";
				sql += "FROM   " + getResource("metadata.schema") + "."
						+ getResource("metadata.table.audit.query_log") + "\n";
				sql += "WHERE  idkey IN (" + idList + ")";
				final ResultSet rs = con.query(databaseName, sql);

				while (rs.next()) {
					final Query query = new Query();
					query.name = rs.getString("idkey");
					query.text = rs.getString("query");
					query.purpose = rs.getString("purpose");
					query.accessor = rs.getString("usr");
					query.recipient = rs.getString("recipient");
					query.specificRecipient = rs.getString("spec_recip");
					query.isolation = new Integer(rs.getInt("isolation"));
					query.begin = rs.getString("tim1");
					query.end = rs.getString("tim2");
					switch (query.isolation.intValue()) {
					case 0:
						query.isolationText = "NONE";
						break;
					case 1:
						query.isolationText = "READ_UNCOMMITTED";
						break;
					case 2:
						query.isolationText = "READ_COMMITTED";
						break;
					case 4:
						query.isolationText = "REPEATABLE_READ";
						break;
					case 8:
						query.isolationText = "SERIALIZABLE";
						break;
					}
					queries.add(query);
				}

				rs.close();
			} catch (SQLException e) {
				logger.error("getting audit queries database(\"" + databaseName
						+ "\"), ", e);
			}
		}
		logger.debug("audit result queries:" + queries);
		return Utility.sortVector(queries);
	}

	public Vector getApplicationUsage(String databaseName) {
		logger.debug("getting application usages");
		Vector applicationUsages = new Vector(4, 2);

		try {

			// String sql = "";
			// //027
			// sql += "SELECT * FROM " + getResource("metadata.schema") + "." +
			// getResource("metadata.table.policy.application_usage");
			// ResultSet rs = con.query(databaseName,sql);
			ResultSet rs = con.getPreparedStatement(databaseName, 26)
					.executeQuery();

			final boolean verbose = true;
			while (rs.next()) {
				ApplicationUsage applicationUsage = new ApplicationUsage(rs
						.getString("appid"), rs.getString("purpose"), rs
						.getString("accessor"), rs.getString("recipient"),
						verbose);
				applicationUsages.add(applicationUsage);
			}

			rs.close();

		} catch (SQLException e) {
			logger.error("getting application usage", e);
		}

		logger.debug("application usages:" + applicationUsages);
		return Utility.sortVector(applicationUsages);
	}

	// need to get entities, then get the choice tables from the entities,
	// and then select the potential choices
	public Vector getChoices(String databaseName) {
		logger.debug("getting choices");
		Vector choices = new Vector(4, 2);

		try {
			Vector entities = getEntries(databaseName,
					getResource("metadata.table.entities"));
			int numEntities = entities.size();

			for (int i = 0; i < numEntities; i++) {
				TableDescriptor table = (TableDescriptor) entities.elementAt(i);

				// String sql = "";
				// //028 only 1 param
				// sql += " SELECT DISTINCT choiceid FROM " + table.schemaName +
				// "." + table.tableName + "";
				// ResultSet rs = con.query(databaseName,sql);
				final PreparedStatement ps = con.getPreparedStatement(
						databaseName, 27);
				setStringParameter(ps, 1, table.schemaName + "."
						+ table.tableName);
				ResultSet rs = ps.executeQuery();

				while (rs.next()) {
					ChoiceDescriptor choice = new ChoiceDescriptor(
							databaseName, table.schemaName, table.tableName, rs
									.getString("CHOICEID"));
					choices.add(choice);
				}

				rs.close();
			}
		} catch (SQLException e) {
			logger.error("getting choices", e);
		}

		logger.debug("choices:" + choices);
		return Utility.sortVector(choices);
	}

	public boolean dropMetadata(String databaseName) {
		logger.debug("dropping policy metadata");

		final Vector tablesToBeDropped = new Vector(4, 2);
		String sql = "";

		// drop association and choice tables

		try {
			// sql = "";
			// //029
			// sql += " SELECT * FROM " + getResource("metadata.schema") + "." +
			// getResource("metadata.table.entities");
			// final ResultSet rs = con.query(databaseName,sql);
			final ResultSet rs = con.getPreparedStatement(databaseName, 28)
					.executeQuery();

			while (rs.next()) {
				tablesToBeDropped
						.add((rs.getString("schema") + "."
								+ rs.getString("table") + getResource("metadata.table.suffix.association")));
				tablesToBeDropped
						.add((rs.getString("schema") + "."
								+ rs.getString("table") + getResource("metadata.table.suffix.choices")));
			}
			rs.close();
		} catch (SQLException e) {
			logger.error("getting entity information", e);
		}

		tablesToBeDropped.add(getResource("metadata.schema") + "."
				+ getResource("metadata.table.policy.scope"));
		tablesToBeDropped.add(getResource("metadata.schema") + "."
				+ getResource("metadata.table.policy"));
		tablesToBeDropped.add(getResource("metadata.schema") + "."
				+ getResource("metadata.table.policy.application_usage"));
		tablesToBeDropped.add(getResource("metadata.schema") + "."
				+ getResource("metadata.table.policy.descriptions"));
		tablesToBeDropped.add(getResource("metadata.schema") + "."
				+ getResource("metadata.table.entities"));

		if (!hasBacklogMetaData(databaseName)) {
			tablesToBeDropped.add(getResource("metadata.schema") + "."
					+ getResource("metadata.table.purposes"));
			tablesToBeDropped.add(getResource("metadata.schema") + "."
					+ getResource("metadata.table.recipients"));
			tablesToBeDropped.add(getResource("metadata.schema") + "."
					+ getResource("metadata.table.accessors"));
		}

		for (int i = 0; i < tablesToBeDropped.size(); i++) {
			try {
				sql = " DROP TABLE " + tablesToBeDropped.get(i);
				con.execute(databaseName, sql);
			} catch (SQLException e) {
				// table undefined
				logger.debug("undefined table:" + tablesToBeDropped.get(i));
			}
		}

		try {
			sql = " drop function decode64";
			con.execute(databaseName, sql);
		} catch (SQLException e) {
			// function undefined
			logger.debug("undefined function:decode64");
		}

		try {
			sql = " drop function encode64";
			con.execute(databaseName, sql);
		} catch (SQLException e) {
			// function undefined
			logger.debug("undefined function:encode64");
		}

		con.commit(databaseName);
		return true;
	}

	/**
	 * Description: This generates a SQL string which will create the shadow
	 * tables to backlog the regular tables. It will also create the SQL
	 * neccesary to create triggers.
	 * 
	 * @param databaseName
	 *            This is where the user tables are stored.
	 * @return true if the creation succeeded.
	 * @throws SQLException
	 *             if something is wrong with the database it is trying to
	 *             analyze.
	 */
	public boolean createBacklogMetaData(String databaseName) {
		logger.debug("creating audit metadata");
		boolean result = true;

		try {

			String sql;

			sql = "CREATE TABLE " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.audit.descriptions") + " (\n";
			sql += "  name    Varchar(32) NOT NULL,\n";
			sql += "  version Varchar(32) NOT NULL,\n";
			sql += "  PRIMARY KEY (name, version)\n";
			sql += ")";
			con.execute(databaseName, sql);

			sql = "CREATE TABLE " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.audit") + " (\n";
			sql += "  auditid   Varchar(32)   NOT NULL,\n";
			sql += "  taskid    Varchar(32)   NOT NULL,\n";
			sql += "  version   Varchar(32)   NOT NULL,\n";
			sql += "  condition Varchar(1024) NOT NULL,\n";
			sql += "  begin     Timestamp     NOT NULL,\n";
			sql += "  end       Timestamp     NOT NULL,\n";
			sql += "  PRIMARY KEY (auditid, taskid, version)\n";
			sql += ")";
			con.execute(databaseName, sql);

			sql = "CREATE TABLE " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.audit.purposes") + " (\n";
			sql += "  auditid Varchar(32) NOT NULL,\n";
			sql += "  taskid  Varchar(32) NOT NULL,\n";
			sql += "  version Varchar(32) NOT NULL,\n";
			sql += "  purpose Varchar(32) NOT NULL,\n";
			sql += "  PRIMARY KEY (auditid, taskid, version, purpose)\n";
			// sql += " FOREIGN KEY (auditid, taskid, version) REFERENCES " +
			// getResource("metadata.schema") + ".audit\n";
			// sql += " ON DELETE CASCADE\n";
			sql += ")";
			con.execute(databaseName, sql);

			sql = "CREATE TABLE " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.audit.accessors") + " (\n";
			sql += "  auditid  Varchar(32) NOT NULL,\n";
			sql += "  taskid   Varchar(32) NOT NULL,\n";
			sql += "  version  Varchar(32) NOT NULL,\n";
			sql += "  accessor Varchar(32) NOT NULL,\n";
			sql += "  PRIMARY KEY (auditid, taskid, version, accessor)\n";
			// sql += " FOREIGN KEY (auditid, taskid, version) REFERENCES " +
			// getResource("metadata.schema") + ".audit\n";
			// sql += " ON DELETE CASCADE\n";
			sql += ")";
			con.execute(databaseName, sql);

			sql = "CREATE TABLE " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.audit.recipients") + " (\n";
			sql += "  auditid   Varchar(32) NOT NULL,\n";
			sql += "  taskid    Varchar(32) NOT NULL,\n";
			sql += "  version   Varchar(32) NOT NULL,\n";
			sql += "  recipient Varchar(32) NOT NULL,\n";
			sql += "  PRIMARY KEY (auditid, taskid, version, recipient)\n";
			// sql += " FOREIGN KEY (auditid, taskid, version) REFERENCES " +
			// getResource("metadata.schema") + ".audit\n";
			// sql += " ON DELETE CASCADE\n";
			sql += ")";
			con.execute(databaseName, sql);

			sql = "CREATE TABLE " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.audit.projection_columns")
					+ " (\n";
			sql += "  auditid Varchar(32) NOT NULL,\n";
			sql += "  taskid  Varchar(32) NOT NULL,\n";
			sql += "  version Varchar(32) NOT NULL,\n";
			sql += "  schema  Varchar(128) NOT NULL,\n";
			sql += "  table   Varchar(128) NOT NULL,\n";
			sql += "  column  Varchar(30) NOT NULL,\n";
			sql += "  PRIMARY KEY (auditid, taskid, version, schema, table, column)\n";
			// sql += " FOREIGN KEY (auditid, taskid, version) REFERENCES " +
			// getResource("metadata.schema") + ".audit\n";
			// sql += " ON DELETE CASCADE\n";
			sql += ")";
			con.execute(databaseName, sql);

			sql = "CREATE TABLE " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.audit.query_log") + " (\n";
			sql += "  query        clob(32627)	not null,\n";
			sql += "  tim1         timestamp		not null,\n";
			sql += "  tim2         timestamp		not null,\n";
			sql += "  isolation    int				not null,\n";
			sql += "  usr          varchar(128)		not null,\n";
			sql += "  purpose      varchar(128)		not null,\n";
			sql += "  recipient    varchar(128)		not null,\n";
			sql += "  spec_recip   varchar(128)		not null,\n";
			sql += "  idkey bigint not null generated always as identity (start with 1,increment by 1)\n";

			sql += ")";
			con.execute(databaseName, sql);

			// tschema varchar(128), tname varchar(128), bschema varchar(128),
			// bname varchar(128), tim timestamp, abv varchar(2000), idkey
			// bigint not null generated always as identity (start with 1,
			// increment by 1));

			sql = "CREATE TABLE " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.audit.views") + " (\n";
			sql += "  tschema Varchar(128) NOT NULL,\n";
			sql += "  tname   Varchar(128) NOT NULL,\n";
			sql += "  bschema Varchar(128) NOT NULL,\n";
			sql += "  bname   Varchar(128) NOT NULL,\n";
			sql += "  tim     Timestamp    NOT NULL,\n";
			sql += "  abv     clob(32627) NOT NULL,\n";
			sql += "  idkey   BigInt       NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),\n";
			sql += "  PRIMARY KEY (idkey)\n";
			sql += ")";
			con.execute(databaseName, sql);

			sql = "CREATE TABLE " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.audit.dropped_columns")
					+ " (\n";
			sql += "  bschema    Varchar(128) NOT NULL,\n";
			sql += "  bname      Varchar(128) NOT NULL,\n";
			sql += "  bcolumn    Varchar(128) NOT NULL,\n";
			sql += "  datatype   Smallint     NOT NULL,\n";
			sql += "  typename   Varchar(128) NOT NULL,\n";
			sql += "  colsize    Integer      NOT NULL,\n";
			sql += "  scale      Integer      NOT NULL,\n";
			sql += "  pos        Integer      NOT NULL,\n";
			sql += "  isnullable Varchar(4)   NOT NULL,\n";
			sql += "  tim        timestamp    NOT NULL,\n";
			sql += "  PRIMARY KEY (bschema, bname, bcolumn)\n";
			sql += ")";
			con.execute(databaseName, sql);

			if (!hasMetadataTables(databaseName)) {
				sql = "";
				sql += "CREATE TABLE " + getResource("metadata.schema") + "."
						+ getResource("metadata.table.purposes") + " (\n";
				sql += "  name Varchar(32) NOT NULL,\n";
				sql += "  PRIMARY KEY(name)";
				sql += ")";
				con.execute(databaseName, sql);

				sql = "";
				sql += "CREATE TABLE " + getResource("metadata.schema") + "."
						+ getResource("metadata.table.accessors") + " (\n";
				sql += "  name Varchar(32) NOT NULL,\n";
				sql += "  PRIMARY KEY(name)\n";
				sql += ")";
				con.execute(databaseName, sql);

				sql = "";
				sql += "CREATE TABLE " + getResource("metadata.schema") + "."
						+ getResource("metadata.table.recipients") + " (\n";
				sql += "  name Varchar(32) NOT NULL,\n";
				sql += "  PRIMARY KEY(name)";
				sql += ")";
				con.execute(databaseName, sql);
			}

			con.commit(databaseName);

			logger.debug("audit metadata creation committed");

		} catch (SQLException e) {
			logger.error("creating audit metadata", e);
			con.rollback(databaseName);
			result = false;
		}

		return result;
	}

	public TableDescriptor createBacklogTableFirst(TableDescriptor table,
			boolean isFromBacklogCreationWizard) {
		return createBacklogTable(table, null, isFromBacklogCreationWizard);
	}

	public TableDescriptor createBacklogTable(TableDescriptor table,
			String sqlBacklogReconciliation, boolean isFromBacklogCreationWizard) {
		logger.debug("creating backlog table:" + table);
		logger.debug("sqlBacklogReconciliation:" + sqlBacklogReconciliation);

		// if (!hasBacklogMetaData(table.databaseName))
		// {
		// logger.debug("no audit metadata installed");
		// return null;
		// }

		if (isFromBacklogCreationWizard)
			if (getBacklogTableCount(table) > 0) {
				TableDescriptor backlog = new TableDescriptor();
				backlog.databaseName = "";
				return backlog;
			}

		TableDescriptor backlog = null;

		try {
			String sql;

			//
			// Gets list of all columns in given schema.
			//
			logger.debug("getting list of all columns in given schema");
			final DatabaseMetaData databaseMetaData = con
					.getMetaData(table.databaseName);

			final String timeName = getResource("backlog.table.column.time");
			
			// Create primary key column list
			//
			StringBuffer primaryKey = new StringBuffer();
			ResultSet rs = databaseMetaData.getPrimaryKeys("", table.schemaName, table.tableName);
			boolean found = false;
			while (rs.next()) {
			    primaryKey.append("\"" + rs.getString(4) + "\", ");
			    found=true;
			}
			rs.close();
			primaryKey.append(timeName);
			if (found==false) {
			    String err = "Table "+table.schemaName+"."+table.tableName+ " has no primary key";
			    logger.error(err);
			    con.rollback(table.databaseName);
			    return null;
			}

			// Create column list
			//
			rs = databaseMetaData.getColumns("", table.schemaName, table.tableName, null);
			if (rs.next()) {
				final String indentation = "";
				// final Vector sqlStatements = new Vector(4,2);
				StringBuffer tableColumnsWithTypes = new StringBuffer();
				StringBuffer tableColumnsWithoutTypes = new StringBuffer();

				boolean hasMore = true;
				while (hasMore) {
					// Gets all data necessary to
					// define shadow tables and
					// replicate columns.
					final String tableName = rs.getString("table_name");
					if (tableName == null)
						throw new SQLException();

					final String column = rs.getString("column_name");
					final int columnSize = rs.getInt("column_size");
					final String dataType = rs.getString("type_name");
					final int nullable = rs.getInt("nullable");

					boolean lastInTable = false;
					// If rs has more columns
					// then check whether the next
					// column's table is different
					// from this column.
					if (rs.next()) {
						if (!tableName.equals(rs.getString("table_name")))
							lastInTable = true;
					} else { // If there are no more
						// columns, create last
						// table and exit loop.
						hasMore = false;
						lastInTable = true;
					}

					//
					// Append this column to list of
					// columns.
					//
					final String columnDefinition = "\""
							+ indentation
							+ column
							+ "\" "
							+ dataType
							+ ((columnSize == -1)
									|| !(dataType.toUpperCase().equals(
											"VARCHAR") || dataType
											.toUpperCase().equals("CHAR")) ? " "
									: "(" + columnSize + ") ") 
					    + ((nullable == DatabaseMetaData.columnNoNulls) ? " NOT NULL " : " ")
					    + ",\n";
					tableColumnsWithTypes.append(columnDefinition);
					tableColumnsWithoutTypes.append("\"" + column + "\", ");

					// If the table label associated
					// with this column is different
					// from the last one, create a
					// new table.
					if (lastInTable) {
						final int id = getBacklogTableCount(table) + 1;
						final String tablePrefixName = getResource("backlog.table.prefix1")
								+ id + getResource("backlog.table.prefix2");
						//final String userName = getResource("backlog.table.column.user");
						final String operationName = getResource("backlog.table.column.operation");
						//final String keyName = getResource("backlog.table.column.key");
						final String operationCodeInsert = getResource("operation.code.insert");
						final String operationCodeDelete = getResource("operation.code.delete");
						final String operationCodeUpdate = getResource("operation.code.update");
						final String triggerPrefixInsert = getResource("trigger.prefix.insert");
						final String triggerPrefixDelete = getResource("trigger.prefix.delete");
						final String triggerPrefixUpdate = getResource("trigger.prefix.update");
						// final String outerStatementDelimiter = "";
						final String innerStatementDelimiter = ";";

						final String backlogTableName = tablePrefixName
								+ tableName;

						backlog = new TableDescriptor(table.databaseName,
								table.schemaName, backlogTableName);

						sql = "CREATE TABLE \"" + table.schemaName + "\".\""
								+ backlogTableName + "\" (\n";
						sql += tableColumnsWithTypes + "\n";
//  						sql += indentation + userName
//  								+ " Varchar(128) NOT NULL,\n";
//  						sql += indentation
//  								+ keyName
//  								+ " Bigint NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),\n";
						sql += indentation + operationName
								+ " Char(1) NOT NULL,\n";
						sql += indentation + timeName
								+ " Timestamp NOT NULL,\n";
						sql += indentation + "PRIMARY KEY (" + primaryKey + ")\n";
						sql += ")";// + outerStatementDelimiter;

						MessageBox.setBusyMessage("Creating backlog \""
								+ table.schemaName + "\".\"" + backlogTableName
								+ "\"");

						con.execute(table.databaseName, sql);

						int length = tableColumnsWithoutTypes.length();
						tableColumnsWithoutTypes.delete(length - 2, length);

						//
						// Create triggers.
						// Prepend a tuple
						// variable prefix (such
						// as "a.") to each
						// column name.
						//
						StringBuffer tableColumnsWithTupleVariable = new StringBuffer();

						final String tupleVariable = (tableName
								.equalsIgnoreCase("t")) ? "k" : "t";

						StringTokenizer stringTokenizer = new StringTokenizer(
								tableColumnsWithoutTypes.toString());
						while (stringTokenizer.hasMoreTokens())
							tableColumnsWithTupleVariable.append(" "
									+ tupleVariable + "."
									+ stringTokenizer.nextToken());

//  						ResultSet primaryKeysResultSet = databaseMetaData
//  								.getPrimaryKeys("", table.schemaName
//  										.toUpperCase(), tableName.toUpperCase());
//  						StringBuffer primaryKeyList = new StringBuffer();
//  						while (primaryKeysResultSet.next()) {
//  							String tmp = primaryKeysResultSet
//  									.getString("column_name");
//  							primaryKeyList.append(tmp + " = " + tupleVariable
//  									+ "." + tmp + " AND ");
//  						}

//  						primaryKeysResultSet.close();

//  						length = primaryKeyList.length();

//  						if (length > 5)
//  							primaryKeyList.delete(length - 5, length);

						//
						// If this is not the first backlog then remove the
						// triggers first
						// from the previous backlog before they are created for
						// the new
						// backlog using the same trigger names.
						//
						final boolean triggersExist = (id > 1);

						if (triggersExist) {
							final String[] triggerNames = {
									getDB2ShortIdentifier(tableName,
											triggerPrefixInsert),
									getDB2ShortIdentifier(tableName,
											triggerPrefixUpdate),
									getDB2ShortIdentifier(tableName,
											triggerPrefixDelete) };

							for (int i = 0; i < triggerNames.length; i++) {
								try {
									sql = "";
									sql += "DROP TRIGGER \"" + table.schemaName
											+ "\"." + triggerNames[i];
									con.execute(table.databaseName, sql);
								} catch (SQLException e) {
									// undefined trigger
								}
							}
						}

						MessageBox.setBusyMessage("Creating triggers");

						//
						// Insert trigger
						// inserts a new row
						// into shadow table
						// with the start time
						// being the
						// current time and the
						// end time undefined.
						//
						String triggerName = getDB2ShortIdentifier(tableName,
								triggerPrefixInsert);

						sql =
						// "--#SET
						// DELIMITER " +
						// outerStatementDelimiter
						// +
						"CREATE TRIGGER \"" + table.schemaName + "\"."
								+ triggerName + "\n";
						sql += "AFTER INSERT ON \"" + table.schemaName
								+ "\".\"" + tableName + "\"\n";
						sql += "REFERENCING NEW AS " + tupleVariable + "\n";
						sql += "FOR EACH ROW\n";
						sql += "MODE db2sql\n";
						sql += "BEGIN ATOMIC\n";
						sql += getTriggerInsertStatement("\""
								+ table.schemaName + "\".\"" + backlogTableName
								+ "\"", tableColumnsWithoutTypes,
								tableColumnsWithTupleVariable,
								operationCodeInsert, indentation)
								+ innerStatementDelimiter + "\n";
						sql += "END";
						// sql += outerStatementDelimiter;
						con.execute(table.databaseName, sql);

						//
						// Update trigger
						// updates current row
						// in shadow table by
						// specifying present as
						// end time
						// and inserts a new row
						// with present as start
						// time.
						triggerName = getDB2ShortIdentifier(tableName,
								triggerPrefixUpdate);

						sql =
						// "--#SET
						// DELIMITER " +
						// outerStatementDelimiter
						// +
						"CREATE TRIGGER " + table.schemaName + "."
								+ triggerName + "\n";
						sql += "AFTER UPDATE ON " + table.schemaName + "."
								+ tableName + "\n";
						sql += "REFERENCING NEW AS " + tupleVariable + "\n";
						sql += "FOR EACH ROW\n";
						sql += "MODE db2sql\n";
						sql += "BEGIN ATOMIC\n";
						sql += getTriggerInsertStatement(table.schemaName + "."
								+ backlogTableName, tableColumnsWithoutTypes,
								tableColumnsWithTupleVariable,
								operationCodeUpdate, indentation)
								+ innerStatementDelimiter + "\n";
						sql += "END";
						// sql += outerStatementDelimiter;
						con.execute(table.databaseName, sql);

						//
						// Delete trigger
						// updates current row
						// in shadow table by
						// specifying present as
						// end time.
						//
						triggerName = getDB2ShortIdentifier(tableName,
								triggerPrefixDelete);

						sql =
						// "--#SET
						// DELIMITER " +
						// outerStatementDelimiter
						// +
						"CREATE TRIGGER " + table.schemaName + "."
								+ triggerName + "\n";
						sql += "AFTER DELETE ON " + table.schemaName + "."
								+ tableName + "\n";
						sql += "REFERENCING OLD AS " + tupleVariable + "\n";
						sql += "FOR EACH ROW\n";
						sql += "MODE db2sql\n";
						sql += "BEGIN ATOMIC\n";
						sql += getTriggerInsertStatement(table.schemaName + "."
								+ backlogTableName, tableColumnsWithoutTypes,
								tableColumnsWithTupleVariable,
								operationCodeDelete, indentation)
								+ innerStatementDelimiter + "\n";
						sql += "END";
						// sql += outerStatementDelimiter;
						con.execute(table.databaseName, sql);

						con.commit(table.databaseName);
						logger
								.debug("backlog creation successfully for table: "
										+ table);
						logger.debug("populating backlog table: \""
								+ table.schemaName + "\".\"" + backlogTableName
								+ "\"");

						// now populate backlog table

						MessageBox.setBusyMessage("Populating backlog \""
								+ table.schemaName + "\".\"" + backlogTableName
								+ "\"");

						// select operation code
						String operationCode;
						if (isFromBacklogCreationWizard) {
							operationCode = operationCodeInsert;
						} else {
							operationCode = operationCodeUpdate;
						}

						DatabaseMetaData meta = con
								.getMetaData(table.databaseName);
						if (meta.getDatabaseProductName().equals("DB2/NT")) {
							// let use bulk load with DB2

							sql = "DECLARE HDBCURS CURSOR FOR SELECT CURRENT TIMESTAMP,'"
									+ operationCode
									+ "', t.* FROM \""
									+ table.schemaName
									+ "\".\""
									+ tableName
									+ "\" AS t; \n";

							sql += "LOAD FROM HDBCURS OF CURSOR INSERT INTO \""
									+ table.schemaName + "\".\""
									+ backlogTableName + "\" (";
							//sql += userName + ", " + timeName + ", "
							sql +=  timeName + ", "
									+ operationName + ", "
									+ tableColumnsWithoutTypes + ");";

							try {
								fileExecute(table.databaseName, sql);
							} catch (IOException e) {
							} catch (Exception e) {
								logger.debug("while bulk loading", e);
							}
						} else {
							//
							// Insert all existing
							// rows into shadow
							// table.
							//

							sql = "INSERT\n" + "INTO   \"" + table.schemaName
									+ "\".\"" + backlogTableName + "\" (";
							//sql += userName + ", " + timeName + ", "
							sql += timeName + ", "
									+ operationName + ", "
									+ tableColumnsWithoutTypes + ")\n";
							sql += "SELECT CURRENT TIMESTAMP, " + "'"
									+ operationCode + "'" + ", " + "t.*\n";
							sql += "FROM   \"" + table.schemaName + "\".\""
									+ tableName + "\" AS t";
							// sql += outerStatementDelimiter;
							con.execute(table.databaseName, sql);
						}

						con.commit(table.databaseName);
						logger
								.debug("backlog populated successfully for table: "
										+ table);

						tableColumnsWithTypes = new StringBuffer();
						tableColumnsWithoutTypes = new StringBuffer();

					}
				}
			}

			//
			// Update the meta data.
			//
			if (sqlBacklogReconciliation == null) {
				// This is the first backlog table.
				sqlBacklogReconciliation = "";
				sqlBacklogReconciliation += "SELECT *\n";
				sqlBacklogReconciliation += "FROM   " + backlog.schemaName
						+ "." + backlog.tableName;
			}

			sql = "";
			sql += "INSERT INTO " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.audit.views")
					+ " (tschema, tname, bschema, bname, tim, abv)\n";
			sql += "VALUES (";
			sql += "'" + table.schemaName + "', ";
			sql += "'" + table.tableName + "', ";
			sql += "'" + backlog.schemaName + "', ";
			sql += "'" + backlog.tableName + "', ";
			sql += "CURRENT TIMESTAMP,\n";
			sql += "'" + toSQL(sqlBacklogReconciliation) + "'";
			sql += ")";
			con.execute(table.databaseName, sql);

			rs.close();

			con.commit(table.databaseName);

		} catch (SQLException e) {
			logger.error("creating backlog table " + table, e);
			con.rollback(table.databaseName);
			return null;
		}

		logger.debug("backlog:" + backlog);
		return backlog;
	}

	private ColumnsPanel columnsPanel;

	public void setColumnsPanel(ColumnsPanel columnsPanel) {
		this.columnsPanel = columnsPanel;
	}

	private void fileExecute(String databaseName, String sql)
			throws SQLException, IOException, InterruptedException {

		File f = new File("hdb.sql");
		java.io.FileOutputStream fos = new java.io.FileOutputStream(f);
		fos.write(("CONNECT TO " + databaseName + ";\n").getBytes());
		fos.write(sql.getBytes());
		fos.write("\n CONNECT RESET;\n".getBytes());
		fos.flush();
		fos.close();
		String command = "db2cmd -c \"db2 -tvf \\\""
				+ System.getProperty("user.dir") + "\\hdb.sql\\\" ";
		logger.debug("executing:" + command);
		final Runtime runtime = Runtime.getRuntime();
		final Process process = runtime.exec(command);
		BufferedReader br = new BufferedReader(new InputStreamReader(process
				.getInputStream()));
		String line;

		while ((line = br.readLine()) != null)
			logger.debug(line);

		Thread.sleep(2000);
	}

	public TableDescriptor updateBacklogTable(TableDescriptor backlog,
			String sqlBacklogReconciliation) {
		logger.debug("updating backlog table:" + backlog);
		logger.debug("sqlBacklogReconciliation:" + sqlBacklogReconciliation);
		// String sql;

		//
		// Update the meta data.
		//
		// 034
		// sql = "";
		// sql += "UPDATE " + getResource("metadata.schema") + "." +
		// getResource("metadata.table.audit.views") + "\n";
		// sql += "SET abv = '" + toSQL(sqlBacklogReconciliation) + "'\n";
		// sql += "WHERE bschema = '" + backlog.schemaName.toUpperCase() + "'
		// AND\n";
		// sql += " bname = '" + backlog.tableName.toUpperCase() + "'";

		try {
			// con.execute(backlog.databaseName,sql);
			final PreparedStatement ps = con.getPreparedStatement(
					backlog.databaseName, 33);
			setStringParameterNotToSQL(ps, 1, sqlBacklogReconciliation);
			setStringParameter(ps, 2, backlog.schemaName);
			setStringParameter(ps, 3, backlog.tableName);
			ps.executeUpdate();
			con.commit(backlog.databaseName);
		} catch (SQLException e) {
			logger.error("updating backlog table " + backlog, e);
			con.rollback(backlog.databaseName);
		}
		return backlog;
	}

	public String getFullBacklogViewSQL(String clioSQL, String schemaName,
			String tableName) {
		StringBuffer str = new StringBuffer();

		str.append("\n SELECT * FROM ( ");
		str.append(clioSQL);
		str.append(" ) q\n UNION \n SELECT * FROM ");
		str.append(schemaName);
		str.append(".");
		str.append(tableName);

		return str.toString();

		// String sql = "";
		// sql += "SELECT *\n";
		// sql += "FROM (\n";
		// sql += toSQL(clioSQL);
		// sql += ") q\n";
		// sql += "UNION\n";
		// sql += "SELECT *\n";
		// sql += "FROM " + schemaName + "." + tableName;
		// return sql;
	}

	public boolean populateBacklog(String databaseName, String operationCode,
			String tableColumnsWithoutTypes) {

		return false;
	}

	/**
	 * Drop the backlog tables and the associated triggers.
	 * 
	 * @return success
	 */
	public boolean dropBacklog(String databaseName) {
		logger.debug("dropping audit metadata");

		boolean result = true;

		final Vector tablesToBeDropped = new Vector(4, 2);
		String sql = "";

		final TableDescriptor table = new TableDescriptor(databaseName, null,
				null);
		dropBacklogTable(table);

		tablesToBeDropped.add(getResource("metadata.schema") + "."
				+ getResource("metadata.table.audit.descriptions"));
		tablesToBeDropped.add(getResource("metadata.schema") + "."
				+ getResource("metadata.table.audit"));
		tablesToBeDropped.add(getResource("metadata.schema") + "."
				+ getResource("metadata.table.audit.purposes"));
		tablesToBeDropped.add(getResource("metadata.schema") + "."
				+ getResource("metadata.table.audit.accessors"));
		tablesToBeDropped.add(getResource("metadata.schema") + "."
				+ getResource("metadata.table.audit.recipients"));
		tablesToBeDropped.add(getResource("metadata.schema") + "."
				+ getResource("metadata.table.audit.projection_columns"));
		tablesToBeDropped.add(getResource("metadata.schema") + "."
				+ getResource("metadata.table.audit.query_log"));
		tablesToBeDropped.add(getResource("metadata.schema") + "."
				+ getResource("metadata.table.audit.views"));
		tablesToBeDropped.add(getResource("metadata.schema") + "."
				+ getResource("metadata.table.audit.dropped_columns"));

		if (!hasMetadataTables(databaseName)) {
			tablesToBeDropped.add(getResource("metadata.schema") + "."
					+ getResource("metadata.table.purposes"));
			tablesToBeDropped.add(getResource("metadata.schema") + "."
					+ getResource("metadata.table.recipients"));
			tablesToBeDropped.add(getResource("metadata.schema") + "."
					+ getResource("metadata.table.accessors"));
		}

		for (int i = 0; i < tablesToBeDropped.size(); i++) {
			try {
				sql = " DROP TABLE " + tablesToBeDropped.get(i);
				con.execute(databaseName, sql);
			} catch (SQLException e) {
				logger.debug("undefined table:" + tablesToBeDropped.get(i));
				// undefined table
			}
		}

		con.commit(databaseName);

		return result;
	}

	public boolean dropTriggers(String databaseName) {
		logger.debug("dropping triggers");
		try {
			String sql = "";
			// 030
			// sql += " SELECT DISTINCT TSCHEMA, TNAME FROM "+
			// getResource("metadata.schema")
			// +"."+getResource("metadata.table.audit.views");
			// final ResultSet rs = con.query(databaseName,sql);
			final ResultSet rs = con.getPreparedStatement(databaseName, 29)
					.executeQuery();

			String tschema = "";
			String tname = "";
			while (rs.next()) {
				tschema = rs.getString("TSCHEMA").trim();
				tname = rs.getString("TNAME").trim();

				final String triggerPrefixInsert = getResource("trigger.prefix.insert");
				final String triggerPrefixDelete = getResource("trigger.prefix.delete");
				final String triggerPrefixUpdate = getResource("trigger.prefix.update");
				final String[] triggerNames = {
						getDB2ShortIdentifier(tname, triggerPrefixInsert),
						getDB2ShortIdentifier(tname, triggerPrefixUpdate),
						getDB2ShortIdentifier(tname, triggerPrefixDelete) };

				for (int j = 0; j < triggerNames.length; j++) {
					try {
						sql = "DROP TRIGGER " + tschema + "." + triggerNames[j];
						con.execute(databaseName, sql);
					} catch (SQLException e) {
						logger.debug("undefined trigger:" + tschema + "."
								+ triggerNames[j]);
						// undefined trigger
					}
				}
			}
			rs.close();

			con.commit(databaseName);

		} catch (SQLException e) {

			return false;
		}
		return true;
	}

	public String dropAllBackogTables(final String databaseName) {
		logger.debug("dropping all backlog tables");
		String msg = "";

		final Vector backlogs = controlCenter.resourceManager
				.getTablesWithBacklog(databaseName);

		for (int i = 0; i < backlogs.size(); i++)
			if (!dropBacklogTable((TableDescriptor) backlogs.get(i)))
				msg += "<br><br>&nbsp;&nbsp;&nbsp;&nbsp; Could not drop backlog for "
						+ backlogs.get(i).toString()
						+ " &nbsp;&nbsp;&nbsp;&nbsp;<br>&nbsp;&nbsp;&nbsp;&nbsp; Please see the error log for more detail";

		return msg;
	}

	/**
	 * Drop the backlog tables and the associated triggers.
	 * 
	 * @return success
	 */
	public boolean dropBacklogTable(TableDescriptor table) {
		logger.debug("dropping backlog tables of:" + table);
		boolean result = true;

		if (table.schemaName != null)
			if (getBacklogTableCount(table) < 1)
				return true;

		try {
			//
			// Find all backlog tables.
			//
			logger.debug("listing all backlogs");
			String sql = "";
			sql += "SELECT bschema, bname\n";
			sql += "FROM   " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.audit.views") + "\n";

			if ((table.schemaName != null) || (table.tableName != null)) {
				sql += "WHERE  ";
				if (table.schemaName != null) {
					sql += "tschema = '" + table.schemaName + "'";
				}
				if ((table.schemaName != null) || (table.tableName != null)) {
					sql += " AND\n";
				}
				if (table.schemaName != null) {
					sql += "tname = '" + table.tableName + "'";
				}
			}

			final ResultSet rs = con.query(table.databaseName, sql);
			final Vector backlogTables = new Vector(4, 2);
			while (rs.next()) {
				final TableDescriptor backlogTable = new TableDescriptor(
						table.databaseName, rs.getString("bschema"), rs
								.getString("bname"));
				backlogTables.add(backlogTable);
			}

			rs.close();

			for (int i = 0; i < backlogTables.size(); i++) {
				final TableDescriptor backlogTable = (TableDescriptor) backlogTables
						.get(i);

				try {
					sql = "DROP TABLE " + backlogTable.schemaName + "."
							+ backlogTable.tableName;
					MessageBox.setBusyMessage("Dropping backlog "
							+ backlogTable.schemaName + "."
							+ backlogTable.tableName);
					con.execute(table.databaseName, sql);
				} catch (SQLException e) {
					logger.debug("undefined table:" + backlogTable.schemaName
							+ "." + backlogTable.tableName);
					// undefined table
				}

			}

			logger.debug("deleting records from view");
			// deleting records from view
			if (table.schemaName != null)
				try {
					sql = " DELETE FROM " + getResource("metadata.schema")
							+ "." + getResource("metadata.table.audit.views");
					sql += " WHERE TSCHEMA = '" + table.schemaName
							+ "' AND TNAME = '" + table.tableName + "' ";
					con.execute(table.databaseName, sql);
				} catch (SQLException e) {
				}

			// deleting triggers
			logger.debug("dropping triggers");

			if (table.schemaName == null)
				// in this case all triggers should get deleted as it has to
				// uninstall audit metadata
				dropTriggers(table.databaseName);
			else
			// drop trigger only for specified table when called during define
			// backlog wizard
			{
				final String triggerPrefixInsert = getResource("trigger.prefix.insert");
				final String triggerPrefixDelete = getResource("trigger.prefix.delete");
				final String triggerPrefixUpdate = getResource("trigger.prefix.update");
				final String[] triggerNames = {
						getDB2ShortIdentifier(table.tableName,
								triggerPrefixInsert),
						getDB2ShortIdentifier(table.tableName,
								triggerPrefixUpdate),
						getDB2ShortIdentifier(table.tableName,
								triggerPrefixDelete) };

				for (int j = 0; j < triggerNames.length; j++) {
					try {

						sql = "DROP TRIGGER " + table.schemaName + "."
								+ triggerNames[j];

						MessageBox.setBusyMessage("Dropping trigger "
								+ table.schemaName + "." + triggerNames[j]);

						con.execute(table.databaseName, sql);

					} catch (SQLException e) {
						logger.debug("undefined trigger:" + table.schemaName
								+ "." + triggerNames[j]);
						// undefined trigger
					}
				}
			}

			fireBacklogChanged(table.databaseName);
			con.commit(table.databaseName);
		} catch (SQLException e) {
			logger.error("dropping backlogs for table " + table, e);
			return false;
		}
		return result;
	}

	/**
	 * This method is needed when we use (create/drop) triggers. In DB2, the
	 * maximum length of a trigger is 18 characters. Table names may be longer,
	 * so we use this method for triggers.
	 * 
	 * @param name
	 *            The original name of the identifier
	 * @param prefix
	 *            The prefix for the short name
	 * @return A string that has a maximum size of 18 characters and has a
	 *         complete prefix. However, the prefix (the original name) may need
	 *         to be truncated to make space for the prefix. Examples: (1) name
	 *         is "123456789012345678" and prefix is "abc", then
	 *         "abc123456789012345" is returned. (2) name is "1234567890" and
	 *         prefix is "abc", then "abc1234567890" is returned. (3) If prefix
	 *         is too long, an empty string is returned.
	 */
	String getDB2ShortIdentifier(String name, String prefix) {
		logger.debug("getting db2 short identifier");
		final int maximumLength = 18;

		if (name == null)
			name = "";

		if (prefix.length() > maximumLength)
			return "";
		else if (name.length() + prefix.length() > maximumLength)
			return prefix + name.substring(0, maximumLength - prefix.length());
		else
			return prefix + name;
	}

	/**
	 * Helper method to avoid redundant code when defining an SQL trigger
	 * statement. Uswed for the insert, delete and update trigger definitions,
	 * where operationCode has one of the values "I", "D", and "U".
	 */
	private String getTriggerInsertStatement(String backlogTableName,
			StringBuffer tableColumnsWithoutTypes,
			StringBuffer tableColumnsWithTupleVariable, String operationCode,
			String indentation) {
		logger.debug("getting trigger insert statement");

		//final String userName = getResource("backlog.table.column.user");
		final String timeName = getResource("backlog.table.column.time");
		final String operationName = getResource("backlog.table.column.operation");

		final String sql = "" + indentation + "INSERT INTO " + backlogTableName
				+ " (" + timeName + ", " + operationName
				+ ", " + tableColumnsWithoutTypes + ")\n" + indentation
				+ "VALUES (CURRENT TIMESTAMP, '" + operationCode + "',"
				+ tableColumnsWithTupleVariable + ")";
		return sql;
	}

	public boolean createMetadata(String databaseName) {
		logger.debug("installing policy metadata");
		boolean success = true;

		try {

			String sql = null;

			sql = "CREATE TABLE " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.policy.scope") + " (\n";
			sql += "policyid Varchar(32) NOT NULL,\n";
			sql += "schema Varchar(128) NOT NULL,\n";
			sql += "table Varchar(128) NOT NULL,\n";
			sql += "column Varchar(32) NOT NULL,\n";
			// sql += "type Varchar(32) NOT NULL,\n";
			sql += "version Varchar(32),\n";
			sql += "PRIMARY KEY(policyid,schema,table,column))\n";
			sql += "IN USERSPACE1";

			con.execute(databaseName, sql);

			sql = "CREATE INDEX " + getResource("metadata.schema")
					+ ".SCOPE_INDEX ON " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.policy.scope")
					+ " (schema ASC, table ASC, column ASC)";

			con.execute(databaseName, sql);

			sql = "";
			sql += "CREATE TABLE " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.policy") + " (\n";
			sql += "  policyid  Varchar(32) NOT NULL,\n";
			sql += "  ruleid    Varchar(32) NOT NULL,\n";
			sql += "  version   Varchar(32) NOT NULL,\n";
			sql += "  purpose   Varchar(32) NOT NULL,\n";
			sql += "  accessor  Varchar(32) NOT NULL,\n";
			sql += "  recipient Varchar(32) NOT NULL,\n";
			sql += "  schema    Varchar(128) NOT NULL,\n";
			sql += "  table     Varchar(128) NOT NULL,\n";
			sql += "  column    Varchar(30) NOT NULL,\n";
			sql += "  pseudonym Integer NOT NULL,\n";
			sql += "  condition clob(32627) NOT NULL,\n";
			sql += "PRIMARY KEY(policyid, ruleid, version, purpose, accessor, recipient, schema, table, column)";
			sql += ")\n";
			sql += "IN USERSPACE1";
			con.execute(databaseName, sql);

			sql = "CREATE INDEX " + getResource("metadata.schema")
					+ ".POLICY_INDEX ON " + getResource("metadata.schema")
					+ "." + getResource("metadata.table.policy") + " (\n";
			sql += "  policyid ASC,\n";
			sql += "  purpose ASC,\n";
			sql += "  accessor ASC,\n";
			sql += "  recipient ASC,\n";
			sql += "  schema ASC,\n";
			sql += "  table ASC,\n";
			sql += "  column ASC\n";
			sql += ")";

			con.execute(databaseName, sql);

			sql = "";
			sql += "CREATE TABLE " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.policy.application_usage")
					+ " (\n";
			sql += "  appid     Varchar(32) NOT NULL,\n";
			sql += "  purpose   Varchar(32) NOT NULL,\n";
			sql += "  accessor  Varchar(32) NOT NULL,\n";
			sql += "  recipient Varchar(32) NOT NULL,\n";
			sql += "  PRIMARY KEY(appid, purpose, accessor, recipient)\n";
			sql += ")";
			con.execute(databaseName, sql);

			sql = "";
			sql += "CREATE TABLE " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.entities") + " (\n";
			sql += "  schema Varchar(128) NOT NULL,\n";
			sql += "  table Varchar(128) NOT NULL,\n";
			sql += "  column_name VARCHAR(30) NOT NULL,\n";
			sql += "  PRIMARY KEY(schema, table, column_name)\n";
			sql += ")";
			con.execute(databaseName, sql);

			sql = "";
			sql += "CREATE TABLE " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.policy.descriptions")
					+ " (\n";
			sql += "  name    Varchar(32) NOT NULL,\n";
			sql += "  version Varchar(32) NOT NULL,\n";
			sql += "  type integer NOT NULL,\n";
			sql += "  enabled integer NOT NULL,\n";
			sql += "  PRIMARY KEY(name, version)\n";
			sql += ")";
			// sql += "\nIN USERSPACE1";
			con.execute(databaseName, sql);

			if (!hasBacklogMetaData(databaseName)) {
				sql = "";
				sql += "CREATE TABLE " + getResource("metadata.schema") + "."
						+ getResource("metadata.table.purposes") + " (\n";
				sql += "  name Varchar(32) NOT NULL,\n";
				sql += "  PRIMARY KEY(name)";
				sql += ")";
				con.execute(databaseName, sql);

				sql = "";
				sql += "CREATE TABLE " + getResource("metadata.schema") + "."
						+ getResource("metadata.table.accessors") + " (\n";
				sql += "  name Varchar(32) NOT NULL,\n";
				sql += "  PRIMARY KEY(name)\n";
				sql += ")";
				con.execute(databaseName, sql);

				sql = "";
				sql += "CREATE TABLE " + getResource("metadata.schema") + "."
						+ getResource("metadata.table.recipients") + " (\n";
				sql += "  name Varchar(32) NOT NULL,\n";
				sql += "  PRIMARY KEY(name)";
				sql += ")";
				con.execute(databaseName, sql);
			}

			logger.debug("copying function file");

			if (getEnvVar("DB2TEMPDIR") == null) {
				MessageBox
						.show(
								controlCenter,
								"Message",
								"Could not found DB2TEMPDIR environment variable.<br>DB2TEMPDIR environment variable should point to SQLLIB directory of DB2",
								MessageBox.ICON_FAIL);
				con.rollback(databaseName);
				success = false;
				return success;
			}

			final Runtime runtime = Runtime.getRuntime();
			final String command = getSystemCommand() + " /c XCOPY /STER \""
					+ System.getProperty("user.dir")
					+ "\\resources\\functions\"  \"" + getEnvVar("DB2TEMPDIR")
					+ "\\FUNCTION\"  &  XCOPY /SR  \""
					+ System.getProperty("user.dir")
					+ "\\resources\\functions\\base64\"   \""
					+ getEnvVar("DB2TEMPDIR") + "\\FUNCTION\\base64\" ";
			logger.debug("command:" + command);

			try {

				try {
					sql = " drop function " + getResource("metadata.schema")
							+ ".decode64";
					con.execute(databaseName, sql);
				} catch (SQLException e) {
					// function undefined
					logger.debug("undefined function:decode64");
				}

				try {
					sql = " drop function " + getResource("metadata.schema")
							+ ".encode64";
					con.execute(databaseName, sql);
				} catch (SQLException e) {
					// function undefined
					logger.debug("undefined function:encode64");
				}

				runtime.exec(command);

				sql = " create function " + getResource("metadata.schema")
						+ ".decode64(varchar(128))"
						+ " returns varchar(128) for bit data" + " fenced"
						+ " language java" + " parameter style db2general"
						+ " external name 'base64.base64UDFs.decode64'"
						+ " no external action" + " returns null on null input"
						+ " deterministic" + " no sql";
				con.execute(databaseName, sql);

				sql = " create function " + getResource("metadata.schema")
						+ ".encode64(varchar(128) for bit data)"
						+ " returns varchar(128)" + " fenced"
						+ " language java" + " parameter style db2general"
						+ " external name 'base64.base64UDFs.encode64'"
						+ " no external action" + " returns null on null input"
						+ " deterministic" + " no sql";
				con.execute(databaseName, sql);

			} catch (IOException e) {
				logger.error("could not copy function class file", e);
			}

			con.commit(databaseName);

			logger.debug("policy metadata installed");

		} catch (SQLException e) {
			logger.error("installing policy metadata", e);
			success = false;
		}
		return success;
	}

	public String getEnvVar(final String var) {
		String val = null;
		final Runtime runtime = Runtime.getRuntime();
		final String command = getSystemCommand() + " /c set " + var;
		try {
			final Process process = runtime.exec(command);
			BufferedReader br = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
			final String line = br.readLine();
			val = line.substring(line.indexOf("=") + 1, line.length());
		} catch (IOException e) {
			return val;
		} catch (Exception e) {
			return val;
		}
		return val;
	}

	private String getSystemCommand() {
		final String OS = System.getProperty("os.name").toLowerCase();
		String command;
		if (OS.indexOf("windows 9") > -1) {
			command = "command.com";
		} else if ((OS.indexOf("nt") > -1) || (OS.indexOf("windows 2000") > -1)
				|| (OS.indexOf("windows xp") > -1)) {
			command = "cmd.exe";
		} else {
			command = "env";
		}
		return command;
	}

	public String createBacklogs(final String databaseName,
			final Vector selection, final Vector dropTables) {
		logger.debug("creating backlogs");
		logger.debug("selection:" + selection);

		String msg = "";

		for (int i = 0; i < selection.size(); i++) {
			final TableDescriptor tableDescriptor = (TableDescriptor) selection
					.get(i);

			final TableDescriptor tableDescriptor2 = controlCenter.resourceManager
					.createBacklogTableFirst(tableDescriptor, true);

			if (tableDescriptor2 == null)
				msg += "<br><br>&nbsp;&nbsp;&nbsp;&nbsp; Could not create backlog for "
						+ tableDescriptor
						+ " &nbsp;&nbsp;&nbsp;&nbsp;<br>&nbsp;&nbsp;&nbsp;&nbsp; Please see the error log for more detail";
			else if (tableDescriptor2.databaseName.length() > 0)
				msg += "<br><br>&nbsp;&nbsp;&nbsp;&nbsp; Backlog created for "
						+ tableDescriptor + " &nbsp;&nbsp;&nbsp;&nbsp;";
			else
				msg += "<br><br>&nbsp;&nbsp;&nbsp;&nbsp; Backlog already exist for "
						+ tableDescriptor + " &nbsp;&nbsp;&nbsp;&nbsp;";

		}

		logger.debug("dropTables:" + dropTables);

		for (int i = 0; i < dropTables.size(); i++) {
			final TableDescriptor tableDescriptor = (TableDescriptor) dropTables
					.get(i);

			if (getBacklogTableCount(tableDescriptor) < 1)
				continue;

			if (!controlCenter.resourceManager
					.dropBacklogTable(tableDescriptor))
				msg += "<br><br>&nbsp;&nbsp;&nbsp;&nbsp; Could not drop backlog for "
						+ tableDescriptor
						+ " &nbsp;&nbsp;&nbsp;&nbsp;<br>&nbsp;&nbsp;&nbsp;&nbsp; Please see the error log for more detail";
			else
				msg += "<br><br>&nbsp;&nbsp;&nbsp;&nbsp; Backlog dropped for "
						+ tableDescriptor + " &nbsp;&nbsp;&nbsp;&nbsp;";
		}

		fireBacklogChanged(databaseName);

		if (columnsPanel != null) {
			columnsPanel.backlogChanged(getDatabaseTablesInfo(databaseName,
					true, false));
			columnsPanel = null;
		}

		return msg;
	}

	public String dropEntities(final String databaseName,
			final Vector entitiesToBedropped) {
		logger.debug("dropping entities:" + entitiesToBedropped);

		String msg = "";
		String sql = "";

		for (int i = 0; i < entitiesToBedropped.size(); i++) {

			final String schemaName = ((TableDescriptor) entitiesToBedropped
					.get(i)).schemaName;
			final String tableName = ((TableDescriptor) entitiesToBedropped
					.get(i)).tableName;

			try {
				sql = " DELETE FROM " + getResource("metadata.schema") + "."
						+ getResource("metadata.table.entities");
				sql += " WHERE SCHEMA='" + schemaName + "' AND TABLE='"
						+ tableName + "' ";

				con.execute(databaseName, sql);

				sql = " DROP TABLE " + schemaName + "." + tableName
						+ getResource("metadata.table.suffix.choices");

				con.execute(databaseName, sql);

				sql = " DROP TABLE " + schemaName + "." + tableName
						+ getResource("metadata.table.suffix.association");

				con.execute(databaseName, sql);

				msg += "<br>&nbsp;&nbsp;&nbsp;&nbsp;" + schemaName + "."
						+ tableName
						+ " ( Entity dropped ) &nbsp;&nbsp;&nbsp;&nbsp;";

				con.commit(databaseName);

			} catch (SQLException e) {
				msg += "<br>&nbsp;&nbsp;&nbsp;&nbsp;"
						+ schemaName
						+ "."
						+ tableName
						+ " ( Error dropping ) &nbsp;&nbsp;&nbsp;&nbsp;<br>&nbsp;&nbsp;&nbsp;&nbsp; Please see the error log for more detail";
				logger.error(schemaName + "." + tableName
						+ " ( Error dropping )", e);
				con.rollback(databaseName);
			}
		}

		fireEntitiesChanged(databaseName);

		return msg;
	}

	public String createAssociationAndChoiceTables(String databaseName,
			Vector selectedTables) {
		logger.debug("creating association and choice tables for:"
				+ selectedTables);
		String msg = "";

		try {
			String sql = "";

			Vector existingEntities = getEntries(databaseName,
					getResource("metadata.table.entities"));
			Vector entitiesToBeDropped = new Vector();

			for (int i = 0; i < existingEntities.size(); i++) {
				TableDescriptor table = (TableDescriptor) existingEntities
						.get(i);

				int j = 0;
				int size = selectedTables.size();
				boolean matches = false;

				for (j = 0; j < size; j++) {
					TableDescriptor table2 = (TableDescriptor) selectedTables
							.get(j);

					if (table.equalsTable(table2)) {
						selectedTables.remove(table2);
						msg += "<br>&nbsp;&nbsp;&nbsp;&nbsp;"
								+ table2
								+ " ( Existing entity ) &nbsp;&nbsp;&nbsp;&nbsp;";
						matches = true;
						break;
					}
				}

				if (!matches)
					entitiesToBeDropped.add(table);
			}

			msg += dropEntities(databaseName, entitiesToBeDropped);

			final int numEntities = selectedTables.size();

			for (int i = 0; i < numEntities; i++) {
				final TableDescriptor table = (TableDescriptor) selectedTables
						.elementAt(i);
				final String schemaName = table.schemaName;
				final String tableName = table.tableName;
				String allPrimaryKeys = "";
				String allPrimaryKeysWithTypeNames = "";

				// get primary keys and insert into entities metadata table
				final DatabaseMetaData metadata = con.getMetaData(databaseName);

				ResultSet primaryKeys = null;
				if (table.userObject == null)
					primaryKeys = metadata.getPrimaryKeys("", schemaName, tableName);
				else
					primaryKeys = metadata.getColumns("", schemaName, tableName,
							((String) table.userObject));

				// create a string that concats all primary key column names
				while (primaryKeys.next()) {
					String keyName = primaryKeys.getString("column_name");
					// find column type from original table definition
					ResultSet keyDescription = metadata.getColumns("",
							schemaName, tableName,
							keyName);
					keyDescription.next();
					String keyType = keyDescription.getString("type_name");
					String keyTypePrecision = keyDescription
							.getString("column_size");
					String keyTypeScale = keyDescription
							.getString("DECIMAL_DIGITS");
					keyDescription.close();

					// 052
					// String sql2 = "";
					// sql2 += "INSERT INTO " + getResource("metadata.schema") +
					// "." + getResource("metadata.table.entities") + " (schema,
					// table, column_name)\n";
					// sql2 += "VALUES (" + "'" + schemaName + "', " + "'" +
					// tableName + "', " + "'" + keyName + "')";
					// con.execute(databaseName,sql2);
					final PreparedStatement ps = con.getPreparedStatement(
							databaseName, 51);
					setStringParameter(ps, 1, schemaName);
					setStringParameter(ps, 2, tableName);
					setStringParameter(ps, 3, keyName);
					ps.executeUpdate();

					if (keyType.equalsIgnoreCase("DECIMAL")
							|| keyType.equalsIgnoreCase("Varchar")) {
						if (keyTypePrecision != null)
							keyType += "(" + keyTypePrecision;
						if (keyTypeScale != null)
							keyType += ", " + keyTypeScale;
						keyType += ")";
					}

					allPrimaryKeys += " " + keyName + ",";
					allPrimaryKeysWithTypeNames += " " + keyName + " "
							+ keyType + " NOT NULL,";
				}

				primaryKeys.close();

				boolean hasPrimaryKeys = (allPrimaryKeys.length() != 0);

				if (!hasPrimaryKeys) {
					msg += "<br>&nbsp;&nbsp;&nbsp;&nbsp;"
							+ table
							+ " ( No primary key found ) &nbsp;&nbsp;&nbsp;&nbsp;";
					continue;
				}

				// create association and choice tables for new entities

				sql = "";
				sql += "CREATE TABLE " + schemaName + "." + tableName
						+ getResource("metadata.table.suffix.choices") + " (\n";
				sql += "  " + allPrimaryKeysWithTypeNames + "\n";
				sql += "  choiceid Varchar(32) NOT NULL,\n";
				sql += "  value Varchar(32),\n";
				sql += "  PRIMARY KEY (" + allPrimaryKeys + " choiceid)\n";
				sql += ")";

				con.execute(databaseName, sql);

				sql = "";
				sql += "CREATE TABLE " + schemaName + "." + tableName
						+ getResource("metadata.table.suffix.association")
						+ " (\n";
				sql += "  ";
				allPrimaryKeys = allPrimaryKeys.substring(0, allPrimaryKeys
						.length() - 1);
				sql += allPrimaryKeysWithTypeNames + "\n";
				sql += "  policyid Varchar(32) NOT NULL,\n";
				sql += "  version Varchar(32) NOT NULL,\n";
				sql += "  PRIMARY KEY (" + allPrimaryKeys + ", policyid)\n";
				sql += ") IN USERSPACE1";
				con.execute(databaseName, sql);

				con.commit(databaseName);

				msg += "<br>&nbsp;&nbsp;&nbsp;&nbsp;" + table
						+ " ( Entity created ) ";
			}
		} catch (SQLException e) {
			logger
					.error("defining association and choices table for entity",
							e);
			msg += "<br><br>&nbsp;&nbsp;&nbsp;&nbsp; Error occured &nbsp;&nbsp;&nbsp;&nbsp;<br>&nbsp;&nbsp;&nbsp;&nbsp; Please see the error log for more detail";
			con.rollback(databaseName);
		}

		fireEntitiesChanged(databaseName);

		return msg;
	}

	public boolean addAudit(Version version) {
		logger.debug("adding audit" + version);

		if ((version.collectionName == null)
				|| version.collectionName.equals("")
				|| (version.versionName == null)
				|| version.versionName.equals(""))
			return false;

		try {
			// 053
			// String sql = "";
			// sql += "INSERT INTO " + getResource("metadata.schema") + "." +
			// getResource("metadata.table.audit.descriptions") + " (name,
			// version)\n";
			// sql += "VALUES (" + "'" + toSQL(version.collectionName) + "', " +
			// "'" + toSQL(version.versionName) + "')";
			// con.execute(version.databaseName,sql);
			final PreparedStatement ps = con.getPreparedStatement(
					version.databaseName, 52);
			setStringParameter(ps, 1, version.collectionName);
			setStringParameter(ps, 2, version.versionName);
			ps.executeUpdate();

			con.commit(version.databaseName);
			fireAuditChanged(version);
		} catch (SQLException e) {
			logger.error("adding audit " + version.collectionName, e);
			con.rollback(version.databaseName);
			return false;
		}
		return true;
	}


    public boolean addDroppedColumn(TableDescriptor table, ColumnDescriptor droppedCol) {
		logger.debug("adding dropped column" + droppedCol.columnName);


		try {
			final PreparedStatement ps = con.getPreparedStatement(
					table.databaseName, 60);
			setStringParameter(ps, 1, table.schemaName);
			setStringParameter(ps, 2, table.tableName);
			setStringParameter(ps, 3, droppedCol.columnName);
			setIntParameter(ps, 4, droppedCol.dataType);
			setStringParameter(ps, 5, droppedCol.typeName);
			setIntParameter(ps, 6, droppedCol.colSize);
			setIntParameter(ps, 7, droppedCol.scale);
			setIntParameter(ps, 8, droppedCol.pos);
			setStringParameter(ps, 9, droppedCol.isNullable);
			ps.executeUpdate();
			con.commit(table.databaseName);

		} catch (SQLException e) {
			logger.error("adding dropped column " + droppedCol.columnName, e);
			con.rollback(table.databaseName);
			return false;
		}
		return true;
	}

	public String renameAudit(Version version, String newAuditName) {
		logger.debug("renaming audit" + version);

		String msg = "";

		if (version.collectionName.equals("") || newAuditName.equals(""))
			return msg;

		try {

			// 035
			// String sql = "";
			// sql += "UPDATE " + getResource("metadata.schema") + "." +
			// getResource("metadata.table.audit.descriptions") + "\n";
			// sql += "SET name = " + "'" + toSQL(newAuditName) + "'\n";
			// sql += "WHERE name = '" + toSQL(version.collectionName) + "'";
			// con.execute(version.databaseName,sql);
			PreparedStatement ps = con.getPreparedStatement(
					version.databaseName, 34);
			setStringParameter(ps, 1, newAuditName);
			setStringParameter(ps, 2, version.collectionName);
			ps.executeUpdate();

			// String sqlSuffix = "";
			// sqlSuffix += "SET auditid = " + "'" + toSQL(newAuditName) +
			// "'\n";
			// sqlSuffix += "WHERE auditid = '" + toSQL(version.collectionName)
			// + "'";
			//            
			// //036
			// sql = "";
			// sql += "UPDATE " + getResource("metadata.schema") + "." +
			// getResource("metadata.table.audit") + "\n";
			// sql += sqlSuffix;
			// con.execute(version.databaseName,sql);
			ps = con.getPreparedStatement(version.databaseName, 35);
			setStringParameter(ps, 1, newAuditName);
			setStringParameter(ps, 2, version.collectionName);
			ps.executeUpdate();

			// //037
			// sql = "";
			// sql += "UPDATE " + getResource("metadata.schema") + "." +
			// getResource("metadata.table.audit.purposes") + "\n";
			// sql += sqlSuffix;
			// con.execute(version.databaseName,sql);
			ps = con.getPreparedStatement(version.databaseName, 36);
			setStringParameter(ps, 1, newAuditName);
			setStringParameter(ps, 2, version.collectionName);
			ps.executeUpdate();

			// //038
			// sql = "";
			// sql += "UPDATE " + getResource("metadata.schema") + "." +
			// getResource("metadata.table.audit.accessors") + "\n";
			// sql += sqlSuffix;
			// con.execute(version.databaseName,sql);
			ps = con.getPreparedStatement(version.databaseName, 37);
			setStringParameter(ps, 1, newAuditName);
			setStringParameter(ps, 2, version.collectionName);
			ps.executeUpdate();

			// //039
			// sql = "";
			// sql += "UPDATE " + getResource("metadata.schema") + "." +
			// getResource("metadata.table.audit.recipients") + "\n";
			// sql += sqlSuffix;
			// con.execute(version.databaseName,sql);
			ps = con.getPreparedStatement(version.databaseName, 38);
			setStringParameter(ps, 1, newAuditName);
			setStringParameter(ps, 2, version.collectionName);
			ps.executeUpdate();

			// //040
			// sql = "";
			// sql += "UPDATE " + getResource("metadata.schema") + "." +
			// getResource("metadata.table.audit.projection_columns") + "\n";
			// sql += sqlSuffix;
			// con.execute(version.databaseName,sql);
			ps = con.getPreparedStatement(version.databaseName, 39);
			setStringParameter(ps, 1, newAuditName);
			setStringParameter(ps, 2, version.collectionName);
			ps.executeUpdate();

			con.commit(version.databaseName);

			fireAuditChanged(version);

			logger.debug("audit renamed:" + newAuditName);
		} catch (SQLException e) {
			msg = "<br>&nbsp;&nbsp;&nbsp;&nbsp; Could not rename audit &nbsp;&nbsp;&nbsp;&nbsp;<br>&nbsp;&nbsp;&nbsp;&nbsp; Please see the error log for more detial";
			logger.error("renaming audit " + version.collectionName, e);
			con.rollback(version.databaseName);
		}

		return msg;
	}

	public String renamePolicy(Version version, String newPolicyName) {
		logger.debug("renaming policy:" + version);

		String msg = "";

		if (version.collectionName.equals("") || newPolicyName.equals(""))
			return "incorrect version";

		try {

			// 041
			// String sql = "";
			// sql += "UPDATE " + getResource("metadata.schema") + "." +
			// getResource("metadata.table.policy.descriptions") + "\n";
			// sql += "SET name = '" + toSQL(newPolicyName) + "'\n";
			// sql += "WHERE name = '" + toSQL(version.collectionName) + "'";
			// con.execute(version.databaseName,sql);
			PreparedStatement ps = con.getPreparedStatement(
					version.databaseName, 40);
			setStringParameter(ps, 1, newPolicyName);
			setStringParameter(ps, 2, version.collectionName);
			ps.executeUpdate();

			// 042
			// sql = "";
			// sql += "UPDATE " + getResource("metadata.schema") + "." +
			// getResource("metadata.table.policy") + "\n";
			// sql += "SET policyid = '" + toSQL(newPolicyName) + "'\n";
			// sql += "WHERE policyid = '" + toSQL(version.collectionName) +
			// "'";
			// con.execute(version.databaseName,sql);
			ps = con.getPreparedStatement(version.databaseName, 41);
			setStringParameter(ps, 1, newPolicyName);
			setStringParameter(ps, 2, version.collectionName);
			ps.executeUpdate();

			// 043
			// sql = "";
			// sql += "UPDATE " + getResource("metadata.schema") + "." +
			// getResource("metadata.table.policy.scope") + "\n";
			// sql += "SET policyid = '" + toSQL(newPolicyName) + "'\n";
			// sql += "WHERE policyid = '" + toSQL(version.collectionName) +
			// "'";
			// con.execute(version.databaseName,sql);
			ps = con.getPreparedStatement(version.databaseName, 42);
			setStringParameter(ps, 1, newPolicyName);
			setStringParameter(ps, 2, version.collectionName);
			ps.executeUpdate();

			con.commit(version.databaseName);

			final Version updatedVersion = new Version(version.databaseName,
					newPolicyName, version.versionName);
			firePolicyChanged(version, updatedVersion);

			logger.debug("policy renamed:" + newPolicyName);
		} catch (SQLException e) {
			logger.error("renaming policy " + version.collectionName, e);
			msg = "<br>&nbsp;&nbsp;&nbsp;&nbsp; Could not rename policy &nbsp;&nbsp;&nbsp;&nbsp;<br>&nbsp;&nbsp;&nbsp;&nbsp; Please see the error log for more detial";
			con.rollback(version.databaseName);
		}
		return msg;
	}

	// public boolean renameAuditVersion(Version version, String newVersionName)
	// {
	// if (version.versionName.equals("") || newVersionName.equals(""))
	// return false;
	//
	// try
	// {
	//
	// //044
	// // String sql = "";
	// // sql += "UPDATE " + getResource("metadata.schema") + "." +
	// getResource("metadata.table.audit.descriptions") + "\n";
	// // sql += "SET version = " + "'" + toSQL(newVersionName) + "'\n";
	// // sql += "WHERE version = '" + toSQL(version.versionName) + "' AND\n";
	// // sql += " name = '" + toSQL(version.collectionName) + "'";
	// // con.execute(version.databaseName,sql);
	// PreparedStatement ps = con.getPreparedStatement(version.databaseName,43);
	// setStringParameter(ps,1,newVersionName);
	// setStringParameter(ps,2,version.versionName);
	// setStringParameter(ps,3,version.collectionName);
	// ps.executeUpdate();
	//            
	// // //045
	// // sql = "";
	// // sql += "UPDATE " + getResource("metadata.schema") + "." +
	// getResource("metadata.table.audit") + "\n";
	// // sql += "SET version = " + "'" + toSQL(newVersionName) + "'\n";
	// // sql += "WHERE version = '" + toSQL(version.versionName) + "' AND\n";
	// // sql += " auditid = '" + toSQL(version.collectionName) + "'";
	// // con.execute(version.databaseName,sql);
	// ps = con.getPreparedStatement(version.databaseName,44);
	// setStringParameter(ps,1,newVersionName);
	// setStringParameter(ps,2,version.versionName);
	// setStringParameter(ps,3,version.collectionName);
	// ps.executeUpdate();
	//            
	// con.commit(version.databaseName);
	// fireAuditChanged(version);
	// }
	// catch (SQLException e)
	// {
	// logger.error("renaming audit version "+version.collectionName+" /
	// "+version.versionName+" for database("+version.databaseName+") ",e);
	// con.rollback(version.databaseName);
	// return false;
	// }
	// return true;
	// }

	public String renamePolicyVersion(Version version, String newVersionName) {
		logger.debug("renaming policy version:" + version);

		String msg = "";

		if (version.versionName.equals("") || newVersionName.equals(""))
			return "version is incorrect";

		try {

			// 046
			// String sql = "";
			// sql += "UPDATE " + getResource("metadata.schema") + "." +
			// getResource("metadata.table.policy.descriptions") + "\n";
			// sql += "SET version = " + "'" + toSQL(newVersionName) + "'\n";
			// sql += "WHERE version = '" + toSQL(version.versionName) + "'
			// AND\n";
			// sql += " name = '" + toSQL(version.collectionName) + "'";
			// con.execute(version.databaseName,sql);
			PreparedStatement ps = con.getPreparedStatement(
					version.databaseName, 45);
			setStringParameter(ps, 1, newVersionName);
			setStringParameter(ps, 2, version.versionName);
			setStringParameter(ps, 3, version.collectionName);
			ps.executeUpdate();

			// 047
			// sql = "";
			// sql += "UPDATE " + getResource("metadata.schema") + "." +
			// getResource("metadata.table.policy") + "\n";
			// sql += "SET version = " + "'" + toSQL(newVersionName) + "'\n";
			// sql += "WHERE version = '" + toSQL(version.versionName) + "'
			// AND\n";
			// sql += " policyid = '" + toSQL(version.collectionName) + "'";
			// con.execute(version.databaseName,sql);
			ps = con.getPreparedStatement(version.databaseName, 46);
			setStringParameter(ps, 1, newVersionName);
			setStringParameter(ps, 2, version.versionName);
			setStringParameter(ps, 3, version.collectionName);
			ps.executeUpdate();

			if (version.type == Version.TYPE_SIMPLE) {
				// 048
				// sql = "";
				// sql += "UPDATE " + getResource("metadata.schema") + "." +
				// getResource("metadata.table.policy.scope") + "\n";
				// sql += "SET version = " + "'" + toSQL(newVersionName) +
				// "'\n";
				// sql += "WHERE version = '" + toSQL(version.versionName) + "'
				// AND\n";
				// sql += " policyid = '" + toSQL(version.collectionName) + "'";
				// con.execute(version.databaseName,sql);
				ps = con.getPreparedStatement(version.databaseName, 47);
				setStringParameter(ps, 1, newVersionName);
				setStringParameter(ps, 2, version.versionName);
				setStringParameter(ps, 3, version.collectionName);
				ps.executeUpdate();
			}

			con.commit(version.databaseName);

			final Version updatedVersion = new Version(version.databaseName,
					version.collectionName, newVersionName);
			firePolicyChanged(version, updatedVersion);

			logger.debug("policy version renamed" + newVersionName);
		} catch (SQLException e) {
			logger.error("renaming policy version " + version.collectionName
					+ " / " + version.versionName, e);
			msg = "<br>&nbsp;&nbsp;&nbsp;&nbsp; Could not rename version &nbsp;&nbsp;&nbsp;&nbsp;<br>&nbsp;&nbsp;&nbsp;&nbsp; Please see the error log for more detial";
			con.rollback(version.databaseName);
		}
		return msg;
	}

	public String addPolicy(Version version) {
		logger.debug("adding policy:" + version);

		String msg = "";

		if (version.collectionName.equals("") || version.versionName.equals(""))
			return msg;

		try {

			// 054
			// String sql = "INSERT INTO " + getResource("metadata.schema") +
			// "." + getResource("metadata.table.policy.descriptions")
			// + " (name, version, TYPE, ENABLED) VALUES (" + "'" +
			// toSQL(version.collectionName) + "', " + "'"
			// + toSQL(version.versionName) +
			// "',"+version.type+","+version.enabled+")";
			// con.execute(version.databaseName,sql);
			final PreparedStatement ps = con.getPreparedStatement(
					version.databaseName, 53);
			setStringParameter(ps, 1, version.collectionName);
			setStringParameter(ps, 2, version.versionName);
			setIntParameter(ps, 3, version.type);
			setIntParameter(ps, 4, version.enabled);
			ps.executeUpdate();

			con.commit(version.databaseName);
			firePolicyChanged(version, version);
		} catch (SQLException e) {
			msg += "<br>&nbsp;&nbsp;&nbsp;&nbsp; Could not add policy &nbsp;&nbsp;&nbsp;&nbsp;";
			msg += "<br>&nbsp;&nbsp;&nbsp;&nbsp; Please see the error log for more detail &nbsp;&nbsp;&nbsp;&nbsp;";
			logger.error("adding policy " + version.collectionName, e);
			con.rollback(version.databaseName);
		}

		return msg;
	}

	public String deletePolicy(Version version) {
		logger.debug("deleting policy:" + version);

		String msg = "";

		try {
			String sql = "DELETE FROM " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.policy.descriptions") + " ";

			if (version.collectionName != null) {
				sql += " WHERE name =  " + "'" + toSQL(version.collectionName)
						+ "'";
				if (version.versionName != null)
					sql += " AND version = " + "'" + toSQL(version.versionName)
							+ "'";
			}

			con.execute(version.databaseName, sql);

			sql = "DELETE FROM " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.policy") + " ";
			if (version.collectionName != null) {
				sql += " WHERE policyid = " + "'"
						+ toSQL(version.collectionName) + "'";
				if (version.versionName != null)
					sql += " AND version = " + "'" + toSQL(version.versionName)
							+ "'";
			}

			con.execute(version.databaseName, sql);

			sql = "DELETE FROM " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.policy.scope") + " ";
			if (version.collectionName != null) {
				sql += " WHERE policyid = " + "'"
						+ toSQL(version.collectionName) + "'";
				if (version.versionName != null)
					sql += " AND version = " + "'" + toSQL(version.versionName)
							+ "'";
			}

			con.execute(version.databaseName, sql);
			firePolicyChanged(version, version);
			con.commit(version.databaseName);
		} catch (SQLException e) {
			logger.error("deleting entry " + version.collectionName, e);
			msg = "<br>&nbsp;&nbsp;&nbsp;&nbsp; Could not delete entry &nbsp;&nbsp;&nbsp;&nbsp;<br>&nbsp;&nbsp;&nbsp;&nbsp; Please see the error log for more detial";
			con.rollback(version.databaseName);
		}
		return msg;
	}

	public String getActivatedVersion(final String databaseName,
			final Version version) {
		logger.debug("getting activated version for:" + version);

		String activtedVersion = null;

		if (version.type == Version.TYPE_SIMPLE) {
			try {
				// String sql = "";
				// //031
				// sql += "SELECT VERSION \n";
				// sql += "FROM " + getResource("metadata.schema") + "." +
				// getResource("metadata.table.policy.descriptions") + "\n";
				// sql += "WHERE NAME = '" + toSQL(version.collectionName) + "'
				// AND ENABLED = 1 ";
				// ResultSet rs = con.query(databaseName,sql);
				final PreparedStatement ps = con.getPreparedStatement(
						databaseName, 30);
				setStringParameter(ps, 1, version.collectionName);
				ResultSet rs = ps.executeQuery();

				rs.next();

				activtedVersion = rs.getString(1);

				rs.close();
			} catch (SQLException e) {
				logger.error("getting activated version for policy for (\""
						+ version.collectionName + "\"), ", e);

			}
		}

		logger.debug("activated version:" + activtedVersion);
		return activtedVersion;
	}

	public String deleteScope(Version version, Vector scopeEntries) {
		logger.debug("deleting scope for:" + version);
		String msg = "";
		String sql = "";
		ColumnDescriptor column;

		logger.debug("scopeEntries:" + scopeEntries);

		for (int i = 0; i < scopeEntries.size(); i++) {
			column = (ColumnDescriptor) scopeEntries.get(i);

			if (isColumnInScopeUsed(version.databaseName,
					version.collectionName, column)) {
				if (MessageBox
						.result(
								controlCenter,
								"Confirm",
								"The column \""
										+ column
										+ "\" is in use<br>Are you sure you still want to remove this from scope?",
								MessageBox.ICON_WARN) != MessageBox.BUTTON_YES)
					break;
			}

			try {
				sql += " DELETE FROM " + getResource("metadata.schema") + "."
						+ getResource("metadata.table.policy.scope") + "\n";
				sql += " WHERE policyid = '" + toSQL(version.collectionName)
						+ "' ";
				sql += " AND SCHEMA = '" + column.table.schemaName + "' ";
				sql += " AND TABLE = '" + column.table.tableName + "' ";
				sql += " AND COLUMN = '" + column.columnName + "' ";

				con.execute(version.databaseName, sql);
				con.commit(version.databaseName);

			} catch (SQLException e) {
				logger.error("deleting scope for policy "
						+ version.collectionName, e);
				msg = "<br>&nbsp;&nbsp;&nbsp;&nbsp; Could not delete \""
						+ column
						+ "\" in scope &nbsp;&nbsp;&nbsp;&nbsp;<br>&nbsp;&nbsp;&nbsp;&nbsp; Please see the error log for more detial";
				con.rollback(version.databaseName);
			}
		}

		fireColumnsInScopeChanged(version.databaseName, version.collectionName);

		return msg;
	}

	public String deleteScope(Version version) {
		logger.debug("deleting scope for:" + version);

		String msg = "";
		String sql = "";

		try {
			sql += " DELETE FROM " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.policy.scope") + "\n";
			sql += " WHERE policyid = '" + toSQL(version.collectionName) + "' ";

			con.execute(version.databaseName, sql);
			con.commit(version.databaseName);

		} catch (SQLException e) {
			logger.error("deleting scope for policy " + version.collectionName,
					e);
			msg = "<br>&nbsp;&nbsp;&nbsp;&nbsp; Could not delete columns in scope for policy "
					+ version.collectionName
					+ "&nbsp;&nbsp;&nbsp;&nbsp;<br>&nbsp;&nbsp;&nbsp;&nbsp; Please see the error log for more detial";
			con.rollback(version.databaseName);
		}

		fireColumnsInScopeChanged(version.databaseName, version.collectionName);

		return msg;
	}

	public String updateScope(Version version, Vector scopeEntries,
			String activtedVersion) {
		logger.debug("updating scope for:" + version);

		String msg = "";

		if (activtedVersion == null)
			activtedVersion = getActivatedVersion(version.databaseName, version);

		try {

			String sql = "";
			sql += "DELETE FROM " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.policy.scope") + "\n";
			sql += " WHERE policyid = '" + toSQL(version.collectionName) + "'";
			con.execute(version.databaseName, sql);

			// 055
			// sql = "";
			// sql += "INSERT INTO " + getResource("metadata.schema") + "." +
			// getResource("metadata.table.policy.scope") + " (policyid, schema,
			// table, column, type, VERSION)\n";
			// sql += "VALUES\n";

			logger.debug("scopeEntries:" + scopeEntries);

			int numScopeEntries = scopeEntries.size();

			if (numScopeEntries != 0) {

				final PreparedStatement ps = con.getPreparedStatement(
						version.databaseName, 54);

				for (int i = 0; i < numScopeEntries; i++) {
					ColumnDescriptor column = (ColumnDescriptor) scopeEntries
							.elementAt(i);

					// sql += "('" + toSQL(version.collectionName) + "', " + "'"
					// + toSQL(column.table.schemaName) + "', " + "'"
					// + toSQL(column.table.tableName) + "', " + "'" +
					// toSQL(column.columnName) + "', " + "'"
					// + toSQL(column.typeName) + "'" + ",";

					setStringParameter(ps, 1, version.collectionName);
					setStringParameter(ps, 2, column.table.schemaName);
					setStringParameter(ps, 3, column.table.tableName);
					setStringParameter(ps, 4, column.columnName);
					// setStringParameter(ps, 5, column.typeName);

					if (activtedVersion == null)
						// sql += "null)";
						setStringParameter(ps, 5, "");
					else {
						if (version.type == Version.TYPE_SIMPLE)
							// sql += "'"+activtedVersion+"')";
							setStringParameter(ps, 5, activtedVersion);
						else
							setStringParameter(ps, 5, "");
						// sql += "null)";
					}

					// if (i != (numScopeEntries - 1))
					// sql += ", ";

					ps.executeUpdate();

				}

				// con.execute(version.databaseName,sql);

				// msg ="<br>&nbsp;&nbsp;&nbsp;&nbsp; Columns updated in scope
				// &nbsp;&nbsp;&nbsp;&nbsp;";

			}

			fireColumnsInScopeChanged(version.databaseName,
					version.collectionName);
			con.commit(version.databaseName);

		} catch (SQLException e) {
			logger.error("updating scope for policy " + version.collectionName,
					e);
			msg = "<br>&nbsp;&nbsp;&nbsp;&nbsp; Could not update columns in scope &nbsp;&nbsp;&nbsp;&nbsp;<br>&nbsp;&nbsp;&nbsp;&nbsp; Please see the error log for more detial";
			con.rollback(version.databaseName);
		}

		return msg;
	}

	public String updateScope(Version version, Vector scopeEntries) {
		return updateScope(version, scopeEntries, null);
	}

	// public String updateScope(String databaseName, String policyName, Vector
	// scopeEntries)
	// {
	// return updateScope(databaseName,policyName,null,scopeEntries);
	// }

	public String deleteApplicationUsage(String databaseName,
			String[] applicationNames) {
		String msg = "";

		try {
			final PreparedStatement ps = con.getPreparedStatement(databaseName,
					58);

			for (int i = 0; i < applicationNames.length; i++) {
				logger.debug("deleting application usages:"
						+ applicationNames[i]);
				setStringParameterNotToSQL(ps, 1, applicationNames[i]);
				ps.executeUpdate();
			}
			con.commit(databaseName);

		} catch (SQLException e) {
			msg = "Could not delete application(s)<br>Please see the error log for more detail";
			con.rollback(databaseName);
		}
		fireApplicationChanged(databaseName);

		return msg;
	}

	public String updateApplicationUsage(String databaseName,
			Vector applications) {
		logger.debug("updating application usages:" + applications);

		try {

			String sql = "DELETE\n";
			sql += "FROM " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.policy.application_usage");
			con.execute(databaseName, sql);

			// 056
			// sql = "INSERT INTO " + getResource("metadata.schema") + "." +
			// getResource("metadata.table.policy.application_usage")
			// + " (appid, recipient, purpose, accessor) VALUES ";

			final int numApplications = applications.size();

			if (numApplications != 0) {

				final PreparedStatement ps = con.getPreparedStatement(
						databaseName, 55);

				for (int i = 0; i < numApplications; i++) {
					ApplicationUsage applicationUsage = (ApplicationUsage) applications
							.elementAt(i);

					// sql += "('" + toSQL(applicationUsage.name) + "', " + "'"
					// + toSQL(applicationUsage.recipient)
					// + "', " + "'" + toSQL(applicationUsage.purpose) + "', " +
					// "'"
					// + toSQL(applicationUsage.accessor) + "')";

					setStringParameter(ps, 1, applicationUsage.name);
					setStringParameter(ps, 2, applicationUsage.recipient);
					setStringParameter(ps, 3, applicationUsage.purpose);
					setStringParameter(ps, 4, applicationUsage.accessor);

					ps.executeUpdate();

					// if (i != (numApplications - 1))
					// sql += ", ";
				}

				// con.execute(databaseName,sql);
			}

			con.commit(databaseName);

			fireApplicationChanged(databaseName);

		} catch (SQLException e) {
			logger.error("updating application usages", e);
			con.rollback(databaseName);
			return "<br>&nbsp;&nbsp;&nbsp;&nbsp; Could not update application usages &nbsp;&nbsp;&nbsp;&nbsp;<br>&nbsp;&nbsp;&nbsp;&nbsp; Please see the error log for more detial";
		}

		return "";
	}

	// combine this to create entities as well
	public boolean addEntry(String databaseName, String tableName,
			String newEntry) {
		try {

			String sql = "";
			sql += "INSERT INTO " + getResource("metadata.schema") + "."
					+ tableName + " (name)\n";
			sql += "VALUES ('" + toSQL(newEntry) + "')";

			con.execute(databaseName, sql);
			con.commit(databaseName);

			firePurposesChanged(databaseName);
			fireAccessorsChanged(databaseName);
			fireRecipientsChanged(databaseName);

		} catch (SQLException e) {
			logger.error(
					"adding entry (" + newEntry + " -> " + tableName + ")", e);
			con.rollback(databaseName);
			return false;
		}
		return true;
	}

	public boolean addTask(Version version, Task taskToAdd, Task taskToDelete) {
		return (deleteTask(version, taskToDelete) && addTask(version, taskToAdd));
	}

	public boolean addTask(Version version, Task task) {
		logger.debug("adding task for:" + version);
		logger.debug("task:" + task);

		try {
			final DatabaseMetaData metaData = con
					.getMetaData(version.databaseName);
			final Hashtable entityTableToQuantifier = new Hashtable();

			final int numEntities = task.entities.size();

			logger.debug("gererating condition");

			String defaultCondition = "";
			if (numEntities > 0)
				defaultCondition += "EXISTS (\n" + "SELECT 1\n" + "FROM ";

			for (int i = 0; i < numEntities; i++) {
				final TableDescriptor entity = (TableDescriptor) task.entities
						.elementAt(i);

				final ResultSet primaryKeys = metaData.getPrimaryKeys("",
						entity.schemaName, entity.tableName);

				final String quantifierName = "A" + i;

				defaultCondition += "" + entity.schemaName + "."
						+ entity.tableName
						+ getResource("metadata.table.suffix.association")
						+ " " + quantifierName + "\nWHERE " + quantifierName
						+ ".auditid = " + "'" + toSQL(version.collectionName)
						+ "' AND\n" + quantifierName + ".version = " + "'"
						+ toSQL(version.versionName) + "'";

				entityTableToQuantifier.put(entity, quantifierName);

				while (primaryKeys.next()) {
					final String keyName = primaryKeys.getString("column_name");

					defaultCondition += " AND\n" + entity.schemaName + "."
							+ entity.tableName + "." + keyName + " = "
							+ quantifierName + "." + keyName;
				}
				primaryKeys.close();
			}

			logger.debug("defaultCondition:" + defaultCondition);

			final int numPurposes = task.purposes.size();
			final int numAccessors = task.accessors.size();
			final int numRecipients = task.recipients.size();
			final int numColumns = task.columns.size();

			logger
					.debug("generate condition = optin / out + advanced condition");
			// generate condition = optin / out + advanced condition
			String optInOptOutCondition = "";
			final int numOptInChoices = task.optInChoices.size();
			final int numOptOutChoices = task.optOutChoices.size();

			logger.debug("generate the string to check the primary keys");
			if (numOptInChoices != 0) {

				// generate the string to check the primary keys
				String optInChoicesCondition = "";

				for (int i = 0; i < numOptInChoices; i++) {
					final ChoiceDescriptor optInColumn = (ChoiceDescriptor) task.optInChoices
							.elementAt(i);

					final String quantifierName = (String) entityTableToQuantifier
							.get(optInColumn.entityTableDescriptor);

					final ResultSet primaryKeys = metaData.getPrimaryKeys("",
							optInColumn.entityTableDescriptor.schemaName,
							optInColumn.entityTableDescriptor.tableName);

					while (primaryKeys.next()) {
						String keyName = primaryKeys.getString("column_name");

						optInChoicesCondition += " AND\n" + quantifierName
								+ "." + keyName + " = "
								+ optInColumn.entityTableDescriptor.schemaName
								+ "."
								+ optInColumn.entityTableDescriptor.tableName
								+ getResource("metadata.table.suffix.choices")
								+ "." + keyName + " ";
					}

					primaryKeys.close();

					optInChoicesCondition += " AND\n"
							+ optInColumn.entityTableDescriptor.schemaName
							+ "." + optInColumn.entityTableDescriptor.tableName
							+ getResource("metadata.table.suffix.choices")
							+ "." + "choiceid" + " = " + "''"
							+ optInColumn.name + "''" + " AND\n"
							+ optInColumn.entityTableDescriptor.schemaName
							+ "." + optInColumn.entityTableDescriptor.tableName
							+ getResource("metadata.table.suffix.choices")
							+ "." + "VALUE" + " = " + "''1''";
				}

				optInOptOutCondition += optInChoicesCondition;
			}

			if (numOptOutChoices != 0) {
				// generate the string to check the primary keys
				String optOutChoicesCondition = "";

				for (int i = 0; i < numOptOutChoices; i++) {
					ChoiceDescriptor optOutColumn = (ChoiceDescriptor) task.optOutChoices
							.elementAt(i);

					String quantifierName = (String) entityTableToQuantifier
							.get(optOutColumn.entityTableDescriptor);

					final ResultSet primaryKeys = metaData.getPrimaryKeys("",
							optOutColumn.entityTableDescriptor.schemaName,
							optOutColumn.entityTableDescriptor.tableName);

					while (primaryKeys.next()) {
						String keyName = primaryKeys.getString("column_name");

						optOutChoicesCondition += " AND\n" + quantifierName
								+ "." + keyName + " = "
								+ optOutColumn.entityTableDescriptor.schemaName
								+ "."
								+ optOutColumn.entityTableDescriptor.tableName
								+ getResource("metadata.table.suffix.choices")
								+ "." + keyName + " ";
					}
					primaryKeys.close();

					optOutChoicesCondition += " AND\n"
							+ optOutColumn.entityTableDescriptor.schemaName
							+ "."
							+ optOutColumn.entityTableDescriptor.tableName
							+ getResource("metadata.table.suffix.choices")
							+ "." + "choiceid" + " = " + "''"
							+ optOutColumn.name + "''" + " AND\n"
							+ optOutColumn.entityTableDescriptor.schemaName
							+ "."
							+ optOutColumn.entityTableDescriptor.tableName
							+ getResource("metadata.table.suffix.choices")
							+ "." + "value" + " = " + "''0''";
				}

				optInOptOutCondition += optOutChoicesCondition;
			}

			String condition = "";
			if (numEntities > 0) {
				condition += defaultCondition + "    " + optInOptOutCondition;
				if (!task.condition.equals(""))
					condition += "    AND\n" + task.condition;

				condition += ")";
			} else
				condition += "\n" + task.condition;

			logger.debug("condition:" + condition);

			// 057
			String sql = "";
			// sql += "INSERT INTO " + getResource("metadata.schema") + "." +
			// getResource("metadata.table.audit")
			// + " (auditid, taskid, version, begin, end, condition)\n";
			// sql += "VALUES (";
			// sql += "'" + toSQL(version.collectionName) + "', ";
			// sql += "'" + toSQL(task.name) + "', ";
			// sql += "'" + toSQL(version.versionName) + "', ";
			// sql += "'" + task.begin + "', ";
			// sql += "'" + task.end + "', ";
			// sql += "'" + toSQL(condition) + "')";
			// con.execute(version.databaseName,sql);
			PreparedStatement ps = con.getPreparedStatement(
					version.databaseName, 56);
			setStringParameter(ps, 1, version.collectionName);
			setStringParameter(ps, 2, task.name);
			setStringParameter(ps, 3, version.versionName);
			setStringParameter(ps, 4, task.begin.toString());
			setStringParameter(ps, 5, task.end.toString());
			setStringParameterNotToSQL(ps, 6, condition);
			ps.executeUpdate();

			//
			// Purposes
			//
			sql = "INSERT INTO " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.audit.purposes")
					+ " (auditid, taskid, version, purpose)\n";
			sql += "VALUES\n";
			for (int i = 0; i < numPurposes; i++) {
				sql += "(";
				sql += "'" + toSQL(version.collectionName) + "', ";
				sql += "'" + toSQL(task.name) + "', ";
				sql += "'" + toSQL(version.versionName) + "', ";
				final String purpose = (String) task.purposes.elementAt(i);
				sql += "'" + toSQL(purpose) + "'";
				sql += ")";
				if (i < numPurposes - 1)
					sql += ",\n";
			}
			con.execute(version.databaseName, sql);

			//
			// Accessors
			//
			sql = "INSERT INTO " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.audit.accessors")
					+ " (auditid, taskid, version, accessor)\n";
			sql += "VALUES\n";
			for (int i = 0; i < numAccessors; i++) {
				sql += "(";
				sql += "'" + toSQL(version.collectionName) + "', ";
				sql += "'" + toSQL(task.name) + "', ";
				sql += "'" + toSQL(version.versionName) + "', ";
				final String accessor = (String) task.accessors.elementAt(i);
				sql += "'" + toSQL(accessor) + "'";
				sql += ")";
				if (i < numAccessors - 1)
					sql += ",\n";
			}
			con.execute(version.databaseName, sql);

			//
			// Recipients
			//
			sql = "INSERT INTO " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.audit.recipients")
					+ " (auditid, taskid, version, recipient)\n";
			sql += "VALUES\n";
			for (int i = 0; i < numRecipients; i++) {
				sql += "(";
				sql += "'" + toSQL(version.collectionName) + "', ";
				sql += "'" + toSQL(task.name) + "', ";
				sql += "'" + toSQL(version.versionName) + "', ";
				final String recipient = (String) task.recipients.elementAt(i);
				sql += "'" + toSQL(recipient) + "'";
				sql += ")";
				if (i < numRecipients - 1)
					sql += ",\n";
			}

			con.execute(version.databaseName, sql);

			//
			// Columns
			//

			sql = "INSERT INTO " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.audit.projection_columns")
					+ " (auditid, taskid, version, schema, table, column)\n";
			sql += "VALUES\n";
			for (int i = 0; i < numColumns; i++) {
				sql += "(";
				sql += "'" + toSQL(version.collectionName) + "', ";
				sql += "'" + toSQL(task.name) + "', ";
				sql += "'" + toSQL(version.versionName) + "', ";
				final ColumnDescriptor column = (ColumnDescriptor) task.columns
						.elementAt(i);
				sql += "'" + column.table.schemaName + "', ";
				sql += "'" + column.table.tableName + "', ";
				sql += "'" + column.columnName + "'";
				sql += ")";
				if (i < numColumns - 1)
					sql += ",\n";
			}

			con.execute(version.databaseName, sql);
			con.commit(version.databaseName);

		} catch (SQLException e) {
			logger.error("adding task " + task, e);
			con.rollback(version.databaseName);
			return false;
		}
		fireAuditChanged(version);
		return true;
	}

	public boolean deleteTask(Version version, Task task) {
		logger.debug("deleting task for:" + version);
		logger.debug("task:" + task);

		try {

			String sqlSuffix = "";
			if (version.collectionName != null) {
				sqlSuffix += "WHERE auditid = " + "'" + toSQL(task.policyName)
						+ "'";
				sqlSuffix += " AND\n";
				sqlSuffix += "      taskid = " + "'" + toSQL(task.name) + "'";
				sqlSuffix += " AND\n";
				sqlSuffix += "      version = " + "'" + toSQL(task.version)
						+ "'";
			}

			String sql = "";
			sql += "DELETE FROM " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.audit") + "\n";
			sql += sqlSuffix;

			con.execute(version.databaseName, sql);

			sql = "";
			sql += "DELETE FROM " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.audit.purposes") + "\n";
			sql += sqlSuffix;

			con.execute(version.databaseName, sql);

			sql = "";
			sql += "DELETE FROM " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.audit.accessors") + "\n";
			sql += sqlSuffix;

			con.execute(version.databaseName, sql);

			sql = "";
			sql += "DELETE FROM " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.audit.recipients") + "\n";
			sql += sqlSuffix;

			con.execute(version.databaseName, sql);

			sql = "";
			sql += "DELETE FROM " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.audit.projection_columns")
					+ "\n";
			sql += sqlSuffix;

			con.execute(version.databaseName, sql);

			final Version newVersion = new Version(version.databaseName,
					task.policyName, version.versionName);
			fireAuditChanged(newVersion);
			con.commit(version.databaseName);

		} catch (SQLException e) {
			logger.error("deleting task " + task, e);
			con.rollback(version.databaseName);
			return false;
		}
		return true;
	}

	/**
	 * If collectionName == null then the entire audit is deleted. If
	 * versionNAme == nul then the entire version is deleted.
	 */
	public boolean deleteAudit(Version version) {
		logger.debug("deleting audit:" + version);

		try {
			String sql = "DELETE FROM " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.audit.descriptions") + "\n";
			if (version.collectionName != null) {
				sql += " WHERE name =  " + "'" + toSQL(version.collectionName)
						+ "'";
				if (version.versionName != null) {
					sql += " AND\n";
					sql += "      version = " + "'"
							+ toSQL(version.versionName) + "'";
				}
			}

			con.execute(version.databaseName, sql);

			String sqlSuffix = "";
			if (version.collectionName != null) {
				sqlSuffix += " WHERE auditid = " + "'"
						+ toSQL(version.collectionName) + "'";
				if (version.versionName != null) {
					sqlSuffix += " AND\n";
					sqlSuffix += "      version = " + "'"
							+ toSQL(version.versionName) + "'";
				}
			}

			sql = "";
			sql += "DELETE FROM " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.audit") + "\n";
			sql += sqlSuffix;

			con.execute(version.databaseName, sql);

			sql = "";
			sql += "DELETE FROM " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.audit.purposes") + "\n";
			sql += sqlSuffix;

			con.execute(version.databaseName, sql);

			sql = "";
			sql += "DELETE FROM " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.audit.accessors") + "\n";
			sql += sqlSuffix;

			con.execute(version.databaseName, sql);

			sql = "";
			sql += "DELETE FROM " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.audit.recipients") + "\n";
			sql += sqlSuffix;

			con.execute(version.databaseName, sql);

			sql = "";
			sql += "DELETE FROM " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.audit.projection_columns")
					+ "\n";
			sql += sqlSuffix;

			con.execute(version.databaseName, sql);

			fireAuditChanged(version);

			con.commit(version.databaseName);

		} catch (SQLException e) {
			logger.error("deleting audit " + version, e);
			con.rollback(version.databaseName);
			return false;
		}
		return true;
	}

	public Vector listRelevantSchemas(String databaseName) {
		logger.debug("listing relevant schemas");
		final Vector schemas = new Vector(4, 2);
		try {
			final DatabaseMetaData metaData = con.getMetaData(databaseName);
			ResultSet rs = metaData.getSchemas();
			String str = "";
			while (rs.next()) {
				str = rs.getString(1).trim();
				if (!excludedSchemaNames.contains(str))
					schemas.add(str);
			}
			rs.close();

		} catch (SQLException e) {
			logger.error("listing schemas", e);
		}
		logger.debug("relevant schemas:" + schemas);
		return Utility.sortVector(schemas);
	}

	/*
	 * added this function to find out tables present within condition text
	 * these tables need to be added in from cluase later while compiling
	 * compound condition
	 */
	public Vector getTablesFromText(String text, String databaseName) {
		logger
				.debug("listing additional tables from condition text to include them in FROM clause");
		final Vector tables = new Vector(4, 2);
		final Vector schemas = listRelevantSchemas(databaseName);
		String schema = "";
		String table = "";
		String str = "";
		int index = -1;
		boolean hasMore = true;

		// in case user condition contains from cluase itself then no need to
		// add these tables
		if (text.indexOf("from") > 8 || text.indexOf("FROM") > 8)
			return tables;

		for (int i = 0; i < schemas.size(); i++) {
			schema = (String) schemas.get(i);

			str = text;

			while (hasMore) {

				try {
					index = str.indexOf(schema + ".");
				} catch (StringIndexOutOfBoundsException e) {
					hasMore = false;
				}

				if (index >= 0) {
					try {
						table = str.substring(index, str.indexOf(".", (str
								.indexOf(".", index) + 1)));
						if (!tables.contains(table))
							tables.add(table);
					} catch (StringIndexOutOfBoundsException e) {
					}
					try {
						str = str.substring(index + 1);
					} catch (StringIndexOutOfBoundsException e) {
						str = "";
					}
					index = -1;
				} else
					hasMore = false;
			}

			hasMore = true;

		}

		logger.debug("additional tables:" + tables);
		return tables;
	}

	public String addRule(Version version, Rule ruleToAdd, Rule ruleToDelete) {

		String msg = "";

		if (deleteRule(version, ruleToDelete)) {
			msg = addRule(version, ruleToAdd);
		} else {
			msg = "<br>&nbsp;&nbsp;&nbsp;&nbsp; Could not delete the rule  &nbsp;&nbsp;&nbsp;&nbsp;";
			msg += "<br>&nbsp;&nbsp;&nbsp;&nbsp; Please see the error log for more detail &nbsp;&nbsp;&nbsp;&nbsp;";
		}

		return msg;
	}


    // 01/03/2007 jk
    // fix for conditions
    public String genConditionForPolicyColumn(ColumnDescriptor column, Version version, Rule rule)
    throws SQLException, ConditionGenerationException {
	logger.debug("adding condition for:" + version);
	logger.debug("rule:" + rule);
	final Hashtable entityTableToQuantifier = new Hashtable();

	final DatabaseMetaData metaData = con
	    .getMetaData(version.databaseName);


	int numEntities = rule.entities.size();
	int numOptInChoices = rule.optInChoices.size();
	int numOptOutChoices = rule.optOutChoices.size();

	String defaultCondition = " \n EXISTS (SELECT 1 FROM ";

	// 01/03/2007 jk
	// fix for conditions
	//
	//final Vector tables = getTablesFromText(rule.condition,version.databaseName);

	// exclude entity tables
	//  			for (int i = 0; i < rule.entities.size(); i++) {
	//  				if (tables.contains(((String) rule.entities.get(i).toString())))
	//  					tables.remove(rule.entities.get(i).toString());
	//  			}

	// final String scopeTable = getResource("metadata.schema") + "."
	// + getResource("metadata.table.policy.scope");
	// final String policyTable = getResource("metadata.schema") + "."
	// + getResource("metadata.table.policy");

	/*
	 * ///////////////////// String temp=defaultCondition;
	 * defaultCondition = "EXISTS (SELECT 1 FROM ";
	 * 
	 * for (int i = 0; i < numEntities; i++) { TableDescriptor entity =
	 * (TableDescriptor) rule.entities.elementAt(i);
	 * 
	 * ResultSet primaryKeys = metaData.getPrimaryKeys("",
	 * entity.schemaName.toUpperCase(), entity.tableName
	 * .toUpperCase()); String quantifierName = "A" + i;
	 * 
	 * defaultCondition += "" + entity.schemaName + "." +
	 * entity.tableName +
	 * getResource("metadata.table.suffix.association") + " " +
	 * quantifierName + "\n"; defaultCondition += "WHERE " +
	 * quantifierName + ".policyid = " + "'" +
	 * toSQL(version.collectionName) + "' AND\n"; defaultCondition +=
	 * quantifierName + ".version = " + "'" + toSQL(version.versionName) +
	 * "'";
	 * 
	 * entityTableToQuantifier.put(entity, quantifierName);
	 * 
	 * while (primaryKeys.next()) { final String keyName =
	 * primaryKeys.getString("column_name");
	 * 
	 * defaultCondition += " AND\n"; defaultCondition +=
	 * entity.schemaName + "." + entity.tableName + "." + keyName + " = " +
	 * quantifierName + "." + keyName; } }
	 * System.err.println(defaultCondition); defaultCondition=temp;
	 * /////////////////////
	 */

	boolean first = true;
	if (rule.versioningType == Version.TYPE_COMPLEX) {
				// defaultCondition += scopeTable + " , " + policyTable;

	    // 01/04/2007 jk
	    // should have a single entity
	    //
	    for (int i = 0; i < numEntities; i++) {
		TableDescriptor entity = (TableDescriptor) rule.entities
		    .elementAt(i);
		String quantifierName = "A" + i;

		if (first) first = false;
		else
		    defaultCondition += " , ";

		// String str = entity.schemaName + "." + entity.tableName;
					
		defaultCondition +=
		    // " "+entity.schemaName + "." + entity.tableName + ", " +
		    " " + entity.schemaName + "." + entity.tableName
		    + getResource("metadata.table.suffix.association")
		    + " " + quantifierName + "\n";
	    }
	}

	if (rule.versioningType == Version.TYPE_COMPLEX ||
	    numOptInChoices != 0 || numOptOutChoices != 0) {
	    
	    // 01/04/2007 jk
	    // should have a single entity
	    //
	    for (int i = 0; i < numEntities; i++) {
		TableDescriptor entity = (TableDescriptor) rule.entities
		    .elementAt(i);
		// 01/03/2007 jk
		//
		//  					if (tables.contains(str))
		//  						tables.remove(str);
		if (column.table.schemaName.equals (entity.schemaName)==false ||
		    column.table.tableName.equals(entity.tableName)==false) {
		    
		    // add entity table to the from clause
		    //
		    if (first) first = false;
		    else
			defaultCondition += " , ";
		    defaultCondition += entity.schemaName + "." + entity.tableName;
		}
	    }
	}
	// 01/03/2007 jk
	//
	//  			for (int i = 0; i < tables.size(); i++)
	//  				defaultCondition += " , " + tables.get(i);

	defaultCondition += "\n WHERE ";

	ResultSet primaryKeys;
	ResultSet foreignKeys;

//  	if (rule.versioningType == Version.TYPE_SIMPLE) {
				// defaultCondition += policyTable + ".POLICYID = '"
				// + toSQL(rule.policyName) + "' " + "\n AND "
				// + policyTable + ".VERSION = '" + toSQL(rule.version)
				// + "' " + "\n AND " + policyTable + ".POLICYID = "
				// + scopeTable + ".POLICYID " + "\n AND " + policyTable
				// + ".\"SCHEMA\" = " + scopeTable + ".\"SCHEMA\" "
				// + "\n AND " + policyTable + ".\"TABLE\" = "
				// + scopeTable + ".\"TABLE\" " + "\n AND " + policyTable
				// + ".\"COLUMN\" = " + scopeTable + ".\"COLUMN\" ";

	first = true;
	if (rule.versioningType == Version.TYPE_COMPLEX) {
	    for (int i = 0; i < numEntities; i++) {
		TableDescriptor entity = (TableDescriptor) rule.entities
		    .elementAt(i);

		primaryKeys = metaData.getPrimaryKeys("", entity.schemaName, entity.tableName);
		String quantifierName = "A" + i;

		if (first) first = false;
		else
		    defaultCondition += " AND ";


		defaultCondition += " " + quantifierName + ".policyid = "
		    + "'" + toSQL(version.collectionName) + "' AND\n";
		defaultCondition += quantifierName + ".version = " + "'"
		    + toSQL(version.versionName) + "'";

		entityTableToQuantifier.put(entity, quantifierName);

		boolean foundPrimaryKeys = false;
		while (primaryKeys.next()) {
		    foundPrimaryKeys = true;
		    final String keyName = primaryKeys
			.getString("column_name");

		    defaultCondition += " AND\n";
		    defaultCondition += entity.schemaName + "."
			+ entity.tableName + "." + keyName + " = "
			+ quantifierName + "." + keyName;
		}

		primaryKeys.close();

		if (foundPrimaryKeys==false) {
			throw new ConditionGenerationException("Warning: No primary key metadata was found for table "+entity.schemaName+"."+entity.tableName+" to build complex versioning join condition for rule, please edit rule to create join condition");		    
		}
	    }

	    if (rule.versioningType == Version.TYPE_COMPLEX ||
		numOptInChoices != 0 || numOptOutChoices != 0) {
		// 01/04/2007 jk
		// add foreign key join with the entity table
		//
		for (int i = 0; i < numEntities; i++) {
		    TableDescriptor entity = (TableDescriptor) rule.entities
			.elementAt(i);
		
		    if (column.table.schemaName.equals (entity.schemaName)==false ||
			column.table.tableName.equals(entity.tableName)==false) {
			foreignKeys = metaData.getExportedKeys("", entity.schemaName, entity.tableName);
			boolean foreignkeysfound=false;
			while (foreignKeys.next()) {
			    if (foreignKeys.getString(6).equals(column.table.schemaName) &&
				foreignKeys.getString(7).equals(column.table.tableName)) {
				foreignkeysfound=true;
				if (first) first = false;
				else
				    defaultCondition += " AND ";
				defaultCondition += foreignKeys.getString(2)+"."+foreignKeys.getString(3)+"."+foreignKeys.getString(4)+" = "+foreignKeys.getString(6)+"."+foreignKeys.getString(7)+"."+foreignKeys.getString(8);
			    }
			}
			if (foreignkeysfound==false) {
			    throw new ConditionGenerationException("Warning: No foreign key metadata was found from table "+column.table.schemaName+"."+column.table.tableName+" to table "+entity.schemaName+"."+entity.tableName+" to build complex versioning join condition for rule, please edit rule to create join condition");
			}
		    }					
		}
	    }
	}

	// generate condition = optin / out + advanced
	// condition
	String optInOptOutCondition = "";
	
	if (numOptInChoices != 0) {
				// generate the string to check
				// the primary keys
	    String optInChoicesCondition = "";
	    
	    for (int i = 0; i < numOptInChoices; i++) {
		ChoiceDescriptor optInColumn = (ChoiceDescriptor) rule.optInChoices
		    .elementAt(i);
		
		String quantifierName = (String) entityTableToQuantifier
		    .get(optInColumn.entityTableDescriptor);
		
		primaryKeys = metaData.getPrimaryKeys("",
						      optInColumn.entityTableDescriptor.schemaName,
						      optInColumn.entityTableDescriptor.tableName);
		boolean foundPrimaryKeys = false;		
		while (primaryKeys.next()) {
		    String keyName = primaryKeys.getString("column_name");
		    foundPrimaryKeys = true;

		    if (first) first = false;
		    else
			optInChoicesCondition += " AND ";

		    optInChoicesCondition += quantifierName + "."
			+ keyName + " = "
			+ optInColumn.entityTableDescriptor.schemaName
			+ "."
			+ optInColumn.entityTableDescriptor.tableName
			+ "" + "." + keyName + " ";
		}
		
		primaryKeys.close();
		if (foundPrimaryKeys==false) {
			throw new ConditionGenerationException("Error: No primary key metadata was found for table "+optInColumn.entityTableDescriptor.schemaName+"."+optInColumn.entityTableDescriptor.tableName+" to build choice table join condition for rule");		    
		}
		
		optInChoicesCondition += " AND "
		    + optInColumn.entityTableDescriptor.schemaName
		    + "." + optInColumn.entityTableDescriptor.tableName
		    + "" + "." + "CHOICEID" + " = " + "''"
		    + optInColumn.name + "''" + " AND "
		    + optInColumn.entityTableDescriptor.schemaName
		    + "." + optInColumn.entityTableDescriptor.tableName
		    + "" + "." + "VALUE" + " = " + "''1''";
	    }
	    
	    optInOptOutCondition += optInChoicesCondition;
	}
	
	if (numOptOutChoices != 0) {
				// generate the string to check
				// the primary keys
	    String optOutChoicesCondition = "";
	    
	    for (int i = 0; i < numOptOutChoices; i++) {
		ChoiceDescriptor optOutColumn = (ChoiceDescriptor) rule.optOutChoices
		    .elementAt(i);
		
		String quantifierName = (String) entityTableToQuantifier
		    .get(optOutColumn.entityTableDescriptor);
		
		primaryKeys = metaData.getPrimaryKeys("",
						      optOutColumn.entityTableDescriptor.schemaName,
						      optOutColumn.entityTableDescriptor.tableName);
		
		boolean foundPrimaryKeys = false;		
		while (primaryKeys.next()) {
		    String keyName = primaryKeys.getString("column_name");
		    foundPrimaryKeys = true;
		    
		    if (first) first = false;
		    else
			optOutChoicesCondition += " AND ";
		    optOutChoicesCondition += quantifierName
			+ "." + keyName + " = "
			+ optOutColumn.entityTableDescriptor.schemaName
			+ "."
			+ optOutColumn.entityTableDescriptor.tableName
			+ "" + "." + keyName + " ";
		}
		
		primaryKeys.close();
		if (foundPrimaryKeys==false) {
			throw new ConditionGenerationException("Error: No primary key metadata was found for table "+optOutColumn.entityTableDescriptor.schemaName+"."+optOutColumn.entityTableDescriptor.tableName+" to build choice table join condition for rule");		    
		}
		
					optOutChoicesCondition += " AND "
					    + optOutColumn.entityTableDescriptor.schemaName
					    + "."
					    + optOutColumn.entityTableDescriptor.tableName + ""
					    + "." + "CHOICEID" + " = " + "''"
					    + optOutColumn.name + "''" + " AND "
					    + optOutColumn.entityTableDescriptor.schemaName
					    + "."
					    + optOutColumn.entityTableDescriptor.tableName + ""
					    + "." + "VALUE" + " = " + "''0''";
	    }
	    
	    optInOptOutCondition += optOutChoicesCondition;
	}
	
	String condition = defaultCondition + "    " + optInOptOutCondition + ")";
	if (rule.condition.length() > 0)
	    condition += " AND " + rule.condition;
	
  	if (rule.versioningType == Version.TYPE_SIMPLE && numOptInChoices==0 && numOptOutChoices==0) {
	    condition = "";
  	    if (rule.condition.length() > 0)
  		condition = rule.condition;
  	}
	
	logger.debug("condition generated as:" + condition);
	return condition;
    }

	public String addRule(Version version, Rule rule) {
		logger.debug("adding rule for:" + version);
		logger.debug("rule:" + rule);
		String msg = "";

		try {
			String insertSQL = "INSERT INTO "
					+ getResource("metadata.schema")
					+ ".policy (policyid, ruleid, version, purpose, accessor, recipient, schema, table, column, pseudonym, condition)\n";
			insertSQL += "VALUES (" + "'" + toSQL(version.collectionName)
					+ "', " + "'" + toSQL(rule.name) + "', " + "'"
					+ toSQL(version.versionName) + "', ";


			int numEntities = rule.entities.size();
			String defaultCondition = "";

			int numPurposes = rule.purposes.size();
			int numAccessors = rule.accessors.size();
			int numRecipients = rule.recipients.size();
			int numColumns = rule.columns.size();


			for (int i = 0; i < numPurposes; i++) {
				for (int j = 0; j < numAccessors; j++) {
					for (int k = 0; k < numRecipients; k++) {
						for (int l = 0; l < numColumns; l++) {
							String purpose = (String) rule.purposes
									.elementAt(i);
							String accessor = (String) rule.accessors
									.elementAt(j);
							String recipient = (String) rule.recipients
									.elementAt(k);

							ColumnDescriptor column = (ColumnDescriptor) rule.columns
									.elementAt(l);
							String pseudonymString = (column.pseudonym) ? "1"
									: "0";
							String condition = rule.condition;
							try {
							    condition = genConditionForPolicyColumn(column, version, rule);
							}
							catch (ConditionGenerationException e) {
							    logger.error("creating rule " + rule+", "+e.getMessage());
							    msg += "<br>&nbsp;&nbsp;&nbsp;&nbsp; Warning while creating condition for rules  &nbsp;&nbsp;&nbsp;&nbsp;";
							    msg += "<br>&nbsp;&nbsp;&nbsp;&nbsp; Please see the error log for more detail &nbsp;&nbsp;&nbsp;&nbsp;";
							    //con.rollback(version.databaseName);
							}
							
							final String valueSql = "'" + purpose + "', " + "'"
									+ accessor + "', " + "'" + recipient
									+ "', " + "'" + column.table.schemaName
									+ "', " + "'" + column.table.tableName
									+ "', " + "'" + column.columnName + "', "
									+ pseudonymString + "," + "'"
									+ toSQL(condition) + "')";

							final String sql = insertSQL + valueSql;

							try {
								con.execute(version.databaseName, sql);
								con.commit(version.databaseName);
							} catch (SQLException e) {
								final int index = e.getMessage().indexOf(
										"[IBM][CLI Driver][DB2/NT] SQL0803N");

								if (index >= 0) {
									String value = sql.substring((sql
											.indexOf("VALUES (") + 8), sql
											.length());
									value = value.substring(0, value
											.indexOf("EXISTS"));
									msg += "<br>&nbsp;&nbsp;&nbsp;&nbsp; Could not add the rule matching with the existing one &nbsp;&nbsp;&nbsp;&nbsp;";
									msg += "<br>&nbsp;&nbsp;&nbsp;&nbsp; Matching Rule:"
											+ value
											+ " &nbsp;&nbsp;&nbsp;&nbsp;";
									con.rollback(version.databaseName);
								} else
									throw e;
							}
						}
					}
				}
			}

		}
		catch (SQLException e) {
		    logger.error("creating rule " + rule, e);
		    msg += "<br>&nbsp;&nbsp;&nbsp;&nbsp; Could not add the rule  &nbsp;&nbsp;&nbsp;&nbsp;";
		    msg += "<br>&nbsp;&nbsp;&nbsp;&nbsp; Please see the error log for more detail &nbsp;&nbsp;&nbsp;&nbsp;";
		    con.rollback(version.databaseName);
		}

		firePolicyChanged(version, version);
		return msg;
	}

	public boolean deleteRule(Version version, Rule rule) {
		logger.debug("deleting rule for:" + version);
		logger.debug("rule:" + rule);

		try {
			ColumnDescriptor column = (ColumnDescriptor) rule.columns
					.elementAt(0);

			String sql = "";
			sql += "DELETE\n";
			sql += "FROM " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.policy") + "\n";
			sql += "WHERE policyid = " + "'" + toSQL(rule.policyName) + "'"
					+ " AND\n";
			sql += "      ruleid = " + "'" + toSQL(rule.name) + "'" + " AND\n";
			sql += "      version = " + "'" + toSQL(rule.version) + "'"
					+ " AND\n";
			sql += "      purpose = " + "'"
					+ toSQL((String) rule.purposes.elementAt(0)) + "'"
					+ " AND\n";
			sql += "      accessor = " + "'"
					+ toSQL((String) rule.accessors.elementAt(0)) + "'"
					+ " AND\n";
			sql += "      recipient = " + "'"
					+ toSQL((String) rule.recipients.elementAt(0)) + "'"
					+ " AND\n";
			sql += "      schema = " + "'" + toSQL(column.table.schemaName)
					+ "'" + " AND\n";
			sql += "      table = " + "'" + toSQL(column.table.tableName) + "'"
					+ " AND\n";
			sql += "      column = " + "'" + toSQL(column.columnName) + "'";

			con.execute(version.databaseName, sql);

			final Version newVersion = new Version(version.databaseName,
					rule.policyName, version.versionName);
			firePolicyChanged(version, newVersion);
			con.commit(version.databaseName);

		} catch (SQLException e) {
			logger.error("deleting rule " + rule, e);
			con.rollback(version.databaseName);
			return false;
		}
		return true;
	}

	/*
	 * public ColumnDescriptor newColumnDescriptor() { return new
	 * ColumnDescriptor(); }
	 */
	public ApplicationUsage newApplicationUsage(String name, String purpose,
			String accessor, String recipient, boolean verbose) {
		return new ApplicationUsage(name, purpose, accessor, recipient, verbose);
	}

	// listener methods

	public void addResourceListener(ResourceListener l) {
		resourceListeners.add(l);
	}

	private void fireDatabaseDisconnected(String databaseName) {
		int numListeners = resourceListeners.size();
		for (int i = 0; i < numListeners; i++) {
			ResourceListener l = (ResourceListener) resourceListeners
					.elementAt(i);
			l.databaseDisconnected(databaseName);
		}
	}

	private void fireDatabaseConnected(String databaseName) {
		int numListeners = resourceListeners.size();
		for (int i = 0; i < numListeners; i++) {
			ResourceListener l = (ResourceListener) resourceListeners
					.elementAt(i);
			l.databaseConnected(databaseName);
		}
	}

	private void fireAuditChanged(Version version) {
		for (int i = 0; i < resourceListeners.size(); i++) {
			final ResourceListener listener = (ResourceListener) resourceListeners
					.elementAt(i);
			listener.auditChanged(version);
		}
	}

	private void firePolicyChanged(Version version, Version changedVersion) {
		for (int iListener = 0; iListener < resourceListeners.size(); iListener++) {
			final ResourceListener listener = (ResourceListener) resourceListeners
					.elementAt(iListener);
			listener.policyChanged(version);
			if (version.versionName == null) {
				// Notify all of the policy's versions.

				Vector versionNames = getPolicyVersions(changedVersion);

				for (int iVersion = 0; iVersion < versionNames.size(); iVersion++) {
					final String versionName = (String) versionNames
							.get(iVersion);
					final Version specificVersion = new Version(
							changedVersion.databaseName,
							changedVersion.collectionName, versionName);
					listener.policyChanged(specificVersion);
				}

				versionNames = getPolicyVersions(version);
				for (int iVersion = 0; iVersion < versionNames.size(); iVersion++) {
					final String versionName = (String) versionNames
							.get(iVersion);
					final Version specificVersion = new Version(
							version.databaseName, version.collectionName,
							versionName);
					listener.policyChanged(specificVersion);
				}
			} else {
				// The given version is already a specific version.
				listener.policyChanged(version);
			}
		}
	}

	private void fireApplicationChanged(final String databaseName) {
		for (int iListener = 0; iListener < resourceListeners.size(); iListener++) {
			final ResourceListener listener = (ResourceListener) resourceListeners
					.elementAt(iListener);
			listener.applicationChanged(databaseName);
		}
	}

	private void firePurposesChanged(final String databaseName) {
		for (int iListener = 0; iListener < resourceListeners.size(); iListener++) {
			final ResourceListener listener = (ResourceListener) resourceListeners
					.elementAt(iListener);
			listener.purposesChanged(databaseName);
		}
	}

	private void fireRecipientsChanged(final String databaseName) {
		for (int iListener = 0; iListener < resourceListeners.size(); iListener++) {
			final ResourceListener listener = (ResourceListener) resourceListeners
					.elementAt(iListener);
			listener.recipientsChanged(databaseName);
		}
	}

	private void fireAccessorsChanged(final String databaseName) {
		for (int iListener = 0; iListener < resourceListeners.size(); iListener++) {
			final ResourceListener listener = (ResourceListener) resourceListeners
					.elementAt(iListener);
			listener.accessorsChanged(databaseName);
		}
	}

	private void fireEntitiesChanged(final String databaseName) {
		for (int iListener = 0; iListener < resourceListeners.size(); iListener++) {
			final ResourceListener listener = (ResourceListener) resourceListeners
					.elementAt(iListener);
			listener.entitiesChanged(databaseName);
		}
	}

	private void fireColumnsInScopeChanged(final String databaseName,
			final String policyName) {
		for (int iListener = 0; iListener < resourceListeners.size(); iListener++) {
			final ResourceListener listener = (ResourceListener) resourceListeners
					.elementAt(iListener);
			listener.columnsInScopeChanged(databaseName, policyName);
		}
	}

	private void fireBacklogChanged(final String databaseName) {
		for (int iListener = 0; iListener < resourceListeners.size(); iListener++) {
			final ResourceListener listener = (ResourceListener) resourceListeners
					.elementAt(iListener);
			listener.backlogChanged(databaseName);
		}
	}

	/*
	 * amol pujari 28/09/2006 adding these lines to check whether data already
	 * added or not and to notify the user same message changing the return type
	 * of this function to return the exact sql state previously the return type
	 * was boolean
	 */

	public int addExampleData(String databaseName) {
		logger.debug("adding example data");

		final String[] sql = {
				"INSERT INTO " + getResource("metadata.schema") + "."
						+ getResource("metadata.table.purposes")
						+ " VALUES('Marketing')",
				"INSERT INTO " + getResource("metadata.schema") + "."
						+ getResource("metadata.table.purposes")
						+ " VALUES('Accounting')",
				"INSERT INTO " + getResource("metadata.schema") + "."
						+ getResource("metadata.table.purposes")
						+ " VALUES('Purpose 1')",
				"INSERT INTO " + getResource("metadata.schema") + "."
						+ getResource("metadata.table.purposes")
						+ " VALUES('Purpose 2')",
				"INSERT INTO " + getResource("metadata.schema") + "."
						+ getResource("metadata.table.purposes")
						+ " VALUES('Purpose 3')",

				"INSERT INTO " + getResource("metadata.schema") + "."
						+ getResource("metadata.table.accessors")
						+ " VALUES('Chief Marketing Officer')",
				"INSERT INTO " + getResource("metadata.schema") + "."
						+ getResource("metadata.table.accessors")
						+ " VALUES('Marketing staff')",
				"INSERT INTO " + getResource("metadata.schema") + "."
						+ getResource("metadata.table.accessors")
						+ " VALUES('Finance staff')",
				"INSERT INTO " + getResource("metadata.schema") + "."
						+ getResource("metadata.table.accessors")
						+ " VALUES('IT staff')",
				"INSERT INTO " + getResource("metadata.schema") + "."
						+ getResource("metadata.table.accessors")
						+ " VALUES('Accessor 1')",
				"INSERT INTO " + getResource("metadata.schema") + "."
						+ getResource("metadata.table.accessors")
						+ " VALUES('Accessor 2')",
				"INSERT INTO " + getResource("metadata.schema") + "."
						+ getResource("metadata.table.accessors")
						+ " VALUES('Accessor 3')",

				"INSERT INTO " + getResource("metadata.schema") + "."
						+ getResource("metadata.table.recipients")
						+ " VALUES('Chief Marketing Officer')",
				"INSERT INTO " + getResource("metadata.schema") + "."
						+ getResource("metadata.table.recipients")
						+ " VALUES('Chief Finance Officer')",
				"INSERT INTO " + getResource("metadata.schema") + "."
						+ getResource("metadata.table.recipients")
						+ " VALUES('Chief Information Officer')",
				"INSERT INTO " + getResource("metadata.schema") + "."
						+ getResource("metadata.table.recipients")
						+ " VALUES('Chief Executive Officer')",
				"INSERT INTO " + getResource("metadata.schema") + "."
						+ getResource("metadata.table.recipients")
						+ " VALUES('Recipient 1')",
				"INSERT INTO " + getResource("metadata.schema") + "."
						+ getResource("metadata.table.recipients")
						+ " VALUES('Recipient 2')",
				"INSERT INTO " + getResource("metadata.schema") + "."
						+ getResource("metadata.table.recipients")
						+ " VALUES('Recipient 3')",

				"INSERT INTO "
						+ getResource("metadata.schema")
						+ "."
						+ getResource("metadata.table.audit.query_log")
						+ " (query, purpose, usr, recipient, spec_recip, isolation, tim1, tim2) values "
						+ "('SELECT * FROM my1.t1', 'Purpose 1', 'Accessor 1', 'Recipient 1', 'dummy', 1, '1988-12-25-17.12.30.000000', '2005-09-21-23.59.59.000000')",
				"INSERT INTO "
						+ getResource("metadata.schema")
						+ "."
						+ getResource("metadata.table.audit.query_log")
						+ " (query, purpose, usr, recipient, spec_recip, isolation, tim1, tim2) values "
						+ "('SELECT * FROM my1.t2', 'Purpose 2', 'Accessor 3', 'Recipient 2', 'dummy', 1, '1999-12-25-17.12.30.000000', '2005-09-21-23.59.59.000000')",
				"INSERT INTO "
						+ getResource("metadata.schema")
						+ "."
						+ getResource("metadata.table.audit.query_log")
						+ " (query, purpose, usr, recipient, spec_recip, isolation, tim1, tim2) values "
						+ "('SELECT * FROM my2.t1', 'Purpose 3', 'Accessor 2', 'Recipient 3', 'dummy', 1, '2001-12-25-17.12.30.000000', '2005-09-21-23.59.59.000000')", };

		for (int i = 0; i < sql.length; i++)
			try {
				con.execute(databaseName, sql[i]);
			} catch (SQLException e) {// ignore
			}

		con.commit(databaseName);

		return 0;
	}

	public TableDescriptor getBacklogOfTableMostRecent(TableDescriptor table) {
		logger.debug("getting most recent backlog tables");

		TableDescriptor result;
		try {
			// String sql = "";
			// //032
			// sql += "SELECT bschema, bname\n";
			// sql += "FROM " + getResource("metadata.schema") + "." +
			// getResource("metadata.table.audit.views") + "\n";
			// sql += "WHERE tschema = '" + table.schemaName + "' AND\n";
			// sql += " tname = '" + table.tableName + "'\n";
			// sql += "ORDER BY tim DESC";
			// final ResultSet rs = con.query(table.databaseName,sql);
			final PreparedStatement ps = con.getPreparedStatement(
					table.databaseName, 31);
			setStringParameter(ps, 1, table.schemaName);
			setStringParameter(ps, 2, table.tableName);
			final ResultSet rs = ps.executeQuery();

			if (rs.next())
				result = new TableDescriptor(table.databaseName, rs
						.getString("bschema"), rs.getString("bname"));
			else
				result = null;

			rs.close();
		} catch (SQLException e) {
			return null;
		}

		logger.debug("backlog tables most recent:" + result);
		return result;

	}

	public Vector getBacklogsOfTable(TableDescriptor table) {
		logger.debug("getting backlog tables");

		Vector result = new Vector();
		try {
			// String sql = "";
			// //032
			// sql += "SELECT bschema, bname\n";
			// sql += "FROM " + getResource("metadata.schema") + "." +
			// getResource("metadata.table.audit.views") + "\n";
			// sql += "WHERE tschema = '" + table.schemaName + "' AND\n";
			// sql += " tname = '" + table.tableName + "'\n";
			// sql += "ORDER BY tim DESC";
			// final ResultSet rs = con.query(table.databaseName,sql);
			final PreparedStatement ps = con.getPreparedStatement(
					table.databaseName, 31);
			setStringParameter(ps, 1, table.schemaName);
			setStringParameter(ps, 2, table.tableName);
			final ResultSet rs = ps.executeQuery();

			while (rs.next()){
			    result.add(new TableDescriptor(table.databaseName, rs
							   .getString("bschema"), rs.getString("bname")));
			}

			rs.close();
		} catch (SQLException e) {
			return null;
		}

		logger.debug("backlog table count: " + result.size());
		return result;

	}

	public int getBacklogTableCount(TableDescriptor table) {
		logger.debug("getting backlog tables count for table:" + table);

		int count = 0;

		if (table.schemaName == null)
			return 0;

		try {

			// String sql = "";
			// //033
			// sql += "SELECT COUNT(*)\n";
			// sql += "FROM " + getResource("metadata.schema") + "." +
			// getResource("metadata.table.audit.views") + "\n";
			// sql += "WHERE tschema = '" + table.schemaName + "' AND\n";
			// sql += " tname = '" + table.tableName + "'";
			// ResultSet rs = con.query(table.databaseName,sql);
			final PreparedStatement ps = con.getPreparedStatement(
					table.databaseName, 32);
			setStringParameter(ps, 1, table.schemaName);
			setStringParameter(ps, 2, table.tableName);
			final ResultSet rs = ps.executeQuery();

			if (rs.next())
				count = rs.getInt(1);
			else
				count = 0;

			rs.close();
		} catch (SQLException e) {
			logger.error("listing backlog table count for table " + table, e);
			return 0;
		}

		logger.debug("count:" + count);
		return count;
	}

	/*
	 * Modifies string according to SQL type of quotation. Example: John's
	 * favorite movie is "Titanic". is translated into John"'"s favorite movie
	 * is "Titanic".
	 */
	private String toSQL(String text) {
		if (text != null) {
			final String transformedText = text.replaceAll("'", "''");
			return transformedText;
		}
		return "";
	}

	class ChoiceDescriptor implements Cloneable {
		public String name = "";

		public TableDescriptor entityTableDescriptor;

		public ChoiceDescriptor(String databaseName, String schemaName,
				String tableName, String choiceName) {
			name = choiceName;
			entityTableDescriptor = new TableDescriptor(databaseName,
					schemaName, tableName);
		}

		public Object clone() throws CloneNotSupportedException {
			final ChoiceDescriptor other = (ChoiceDescriptor) super.clone();
			other.name = name;
			other.entityTableDescriptor = (TableDescriptor) entityTableDescriptor
					.clone();
			return other;
		}

		public String toString() {
			return name + " (" + entityTableDescriptor.schemaName + "."
					+ entityTableDescriptor.tableName + ")";
		}
	}

	public void closeConnection(String databaseName) throws SQLException {
		con.reset(databaseName);
		fireDatabaseDisconnected(databaseName);
	}

	public void closeAllConnections() {
		logger.debug("closing all opened connection");

		Enumeration connectedDbs = con.getDBNames();

		try {
			while (connectedDbs.hasMoreElements())
				closeConnection((String) connectedDbs.nextElement());
		} catch (SQLException e) {
			logger.error("clossing db connection", e);
		}

		logger.debug("closed all db connection");
	}

	public Vector parseEntities(final String condition,
			final String databaseName, final boolean isUsingTableDescriptor) {
		logger.debug("parsing entities, condition:" + condition);

		final Vector entities = new Vector(1, 1);

		if (condition.length() < 4)
			return entities;

		String str = condition;
		int index = str.indexOf("_ASSOCIATION");
		int i = 0;
		String str2, table, schemaName, tableName;

		while (index >= 0) {
			try {
				str2 = str.substring(0, index);
				i = str2.lastIndexOf(" ");
				table = str2.substring(i + 1, str2.length());
				i = table.indexOf(".");
				schemaName = table.substring(0, i);
				tableName = table.substring(i + 1, table.length());

				if (isUsingTableDescriptor)
					entities.add(new TableDescriptor(databaseName, schemaName,
							tableName));
				else
					entities.add(table);

				str = str.substring(index + "_ASSOCIATION".length(), str
						.length());
				index = str.indexOf("_ASSOCIATION");
			} catch (StringIndexOutOfBoundsException e) {
				index = -1;
			}
		}

		logger.debug("parsed entities:" + entities);
		return Utility.sortVector(entities);
	}

	public String getDisplayableCondition(final String condition) {
		logger.debug("getting displayable condition for:" + condition);

		String conditionDisplay = condition;

		if (conditionDisplay != null) {
			if (conditionDisplay.length() > 0) {
				String str = conditionDisplay.substring(conditionDisplay
						.indexOf("(") + 1);
				try {
					str = str.substring(str.indexOf("(\n"));
				} catch (StringIndexOutOfBoundsException e) {
				}// array index out of bound in case of audit known ...so
				// ignoring...
				try {
					str = str.substring(1, str.indexOf("\n)"));
				} catch (StringIndexOutOfBoundsException e) {
					str = "";
				}// array index out of bound in case of audit known ...so
				// ignoring...

				conditionDisplay = str;
			}
		}

		logger.debug("displayable condition:" + conditionDisplay);
		return conditionDisplay;
	}

	public String deleteAllEntries(final String databaseName, final int type) {
		logger.debug("deleting all entries");
		return deleteEntries(databaseName, type, null);
	}

	public final static int DELETE_ALL_PURPOSES = 1234;

	public final static int DELETE_ALL_RECIPIENTS = 1235;

	public final static int DELETE_ALL_ACCESSORS = 1236;

	public String batchDeleteEntries(final String databaseName, final int type,
			final String[] entries) {
		String msg = "";
		String sql = "";
		String table = "";

		switch (type) {
		case DELETE_ALL_PURPOSES:
			table = getResource("metadata.table.purposes");
			break;
		case DELETE_ALL_RECIPIENTS:
			table = getResource("metadata.table.recipients");
			break;
		case DELETE_ALL_ACCESSORS:
			table = getResource("metadata.table.accessors");
			break;
		}

		if (table.length() > 0) {
			for (int i = 0; i < entries.length; i++) {
				logger.debug("deleting entry:" + entries[i]);
				sql = "DELETE FROM " + getResource("metadata.schema") + "."
						+ table;

				if (entries[i] != null)
					sql += " WHERE NAME = '" + toSQL(entries[i]) + "' ";

				try {
					con.execute(databaseName, sql);
					con.commit(databaseName);
					msg = "<br>&nbsp;&nbsp;&nbsp;&nbsp; All entries deleted! &nbsp;&nbsp;&nbsp;&nbsp;";
				} catch (SQLException e) {
					msg = "<br>&nbsp;&nbsp;&nbsp;&nbsp; Could not delete entires &nbsp;&nbsp;&nbsp;&nbsp;<br>&nbsp;&nbsp;&nbsp;&nbsp; Please see the error log for more detial";
					logger.error("deleting entries", e);
					con.rollback(databaseName);
				}
			}
		}

		switch (type) {
		case DELETE_ALL_PURPOSES:
			firePurposesChanged(databaseName);
			break;
		case DELETE_ALL_RECIPIENTS:
			fireRecipientsChanged(databaseName);
			break;
		case DELETE_ALL_ACCESSORS:
			fireAccessorsChanged(databaseName);
			break;
		}

		return msg;
	}

	public String deleteEntries(final String databaseName, final int type,
			final String entry) {
		final String[] entries = { entry };
		return batchDeleteEntries(databaseName, type, entries);
	}

	public String activateVersion(final Version version) {
		logger.debug("activating version:" + version);

		String msg = "";
		// String sql = "";

		try {

			// 049
			// sql = " UPDATE " + getResource("metadata.schema") + "." +
			// getResource("metadata.table.policy.descriptions") + "\n";
			// sql += " SET ENABLED = 0 \n";
			// sql += " WHERE NAME = '" + toSQL(version.collectionName) + "'";
			// con.execute(version.databaseName,sql);
			PreparedStatement ps = con.getPreparedStatement(
					version.databaseName, 48);
			setStringParameter(ps, 1, version.collectionName);
			ps.executeUpdate();

			// 050
			// sql = " UPDATE " + getResource("metadata.schema") + "." +
			// getResource("metadata.table.policy.descriptions") + "\n";
			// sql += " SET ENABLED = 1 \n";
			// sql += " WHERE NAME = '" + toSQL(version.collectionName) + "' ";
			// sql += " AND VERSION = '" + toSQL(version.versionName) + "' ";
			// con.execute(version.databaseName,sql);
			ps = con.getPreparedStatement(version.databaseName, 49);
			setStringParameter(ps, 1, version.collectionName);
			setStringParameter(ps, 2, version.versionName);
			ps.executeUpdate();

			// 051
			// sql = " UPDATE " + getResource("metadata.schema") + "." +
			// getResource("metadata.table.policy.scope") + "\n";
			// sql += " SET version = '" + toSQL(version.versionName) + "'\n";
			// sql += " WHERE policyid = '" + toSQL(version.collectionName) +
			// "'";
			// con.execute(version.databaseName,sql);
			ps = con.getPreparedStatement(version.databaseName, 50);
			setStringParameter(ps, 1, version.versionName);
			setStringParameter(ps, 2, version.collectionName);
			ps.executeUpdate();

			con.commit(version.databaseName);

		} catch (SQLException e) {
			msg = "<br>&nbsp;&nbsp;&nbsp;&nbsp; Could not activate version &nbsp;&nbsp;&nbsp;&nbsp;<br>&nbsp;&nbsp;&nbsp;&nbsp; Please see the error log for more detial";
			logger.error("activating version " + version, e);
			con.rollback(version.databaseName);
		}

		return msg;
	}

	private final String[] SQLs = {

			"\n SELECT DISTINCT name FROM " + getResource("metadata.schema")
					+ "." + getResource("metadata.table.policy.descriptions"),// 001

			"\n SELECT name FROM " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.audit.descriptions"),// 002

			"\n SELECT name, version FROM " + getResource("metadata.schema")
					+ "." + getResource("metadata.table.policy.descriptions")
					+ "\n ORDER BY name, version ",// 003

			"\n SELECT 1 FROM " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.policy.scope")
					+ "\n WHERE policyid = ? " + "\n AND schema = ? "
					+ "\n AND table = ? " + "\n AND column = ? ",// 004

			"\n SELECT * FROM " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.policy.scope")
					+ "\n WHERE policyid = ? ",// 005

			"\n SELECT 1 FROM " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.entities")
					+ "\n WHERE schema = ? " + "\n AND table = ? ",// 006

			"\n SELECT VERSION FROM " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.policy.descriptions")
					+ "\n WHERE NAME = ? ",// 007

			"\n SELECT DISTINCT appid FROM " + getResource("metadata.schema")
					+ "."
					+ getResource("metadata.table.policy.application_usage"),// 008

			"\n SELECT * FROM ?",// 009

			"\n SELECT DISTINCT TSCHEMA, TNAME FROM "
					+ getResource("metadata.schema") + "."
					+ getResource("metadata.table.audit.views"),// 010

			"\n SELECT VERSION FROM " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.policy.descriptions")
					+ "\n WHERE NAME = ?",// 011

			"\n SELECT type, enabled FROM " + getResource("metadata.schema")
					+ "." + getResource("metadata.table.policy.descriptions")
					+ "\n WHERE NAME = ? " + "\n FETCH FIRST 1 ROW ONLY ",// 012

			"\n SELECT type, enabled FROM " + getResource("metadata.schema")
					+ "." + getResource("metadata.table.policy.descriptions")
					+ "\n WHERE NAME = ? " + "\n AND VERSION = ? "
					+ "\n FETCH FIRST 1 ROW ONLY ",// 013

			"\n SELECT type, enabled FROM " + getResource("metadata.schema")
					+ "." + getResource("metadata.table.policy.descriptions")
					+ "\n WHERE NAME = ? " + "\n AND VERSION = ? ",// 014

			"\n SELECT * FROM " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.policy")
					+ "\n WHERE policyid = ? ",// 015

			"\n SELECT * FROM " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.policy")
					+ "\n WHERE policyid = ? " + "\n AND version = ? ",// 016

			"\n SELECT a.auditid, a.taskid, a.version, a.purpose, a.accessor, a.recipient, c.schema, c.table, c.column, a.condition, a.begin, a.end "
					+ "\n FROM "
					+ getResource("metadata.schema")
					+ "."
					+ getResource("metadata.table.audit")
					+ " AS a, "
					+ getResource("metadata.schema")
					+ "."
					+ getResource("metadata.table.audit.projection_columns")
					+ " AS c "
					+ "\n WHERE  a.auditid = ? "
					+ "\n AND    a.version = ? "
					+ "\n AND    a.auditid = c.auditid "
					+ "\n AND    a.taskid  = c.taskid "
					+ "\n AND    a.version = c.version ",// 017

			"\n SELECT RULEID FROM " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.policy"),// 018

			"\n SELECT RULEID FROM " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.policy")
					+ "\n WHERE POLICYID = ? AND VERSION = ? ",// 019

			"\n SELECT taskid FROM " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.audit")
					+ "\n WHERE  auditid = ? ",// 020

			"\n SELECT purpose FROM " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.audit.purposes")
					+ "\n WHERE  auditid = ? " + "\n AND 	 version = ? "
					+ "\n AND 	 taskid  = ? ",// 021

			"\n SELECT accessor FROM " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.audit.accessors")
					+ "\n WHERE  auditid = ? " + "\n AND    version = ? "
					+ "\n AND    taskid  = ? ",// 022

			"\n SELECT recipient FROM " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.audit.recipients")
					+ "\n WHERE auditid = ? " + "\n AND   version = ? "
					+ "\n AND   taskid  = ? ",// 023

			"\n SELECT schema, table, column FROM "
					+ getResource("metadata.schema") + "."
					+ getResource("metadata.table.audit.projection_columns")
					+ "\n WHERE  auditid = ?" + "\n AND    version = ?"
					+ "\n AND    taskid  = ? ",// 024

			"\n SELECT name, version FROM " + getResource("metadata.schema")
					+ "." + getResource("metadata.table.audit.descriptions"),// 025

			"\n SELECT idkey, query, purpose, usr, recipient, spec_recip, isolation, tim1, tim2 "
					+ "\n FROM "
					+ getResource("metadata.schema")
					+ "."
					+ getResource("metadata.table.audit.query_log")
					+ "\n WHERE  idkey IN (?) ",// 026

			"\n SELECT * FROM " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.policy.application_usage"),// 027

			"\n SELECT DISTINCT choiceid FROM ? ",// 028

			"\n SELECT * FROM " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.entities"),// 029

			"\n SELECT DISTINCT TSCHEMA, TNAME FROM "
					+ getResource("metadata.schema") + "."
					+ getResource("metadata.table.audit.views"),// 030

			"\n SELECT VERSION FROM " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.policy.descriptions")
					+ "\n WHERE NAME = ? " + "\n AND ENABLED = 1 ",// 031

			"\n SELECT bschema, bname FROM " + getResource("metadata.schema")
					+ "." + getResource("metadata.table.audit.views")
					+ "\n WHERE tschema = ? " + "\n AND     tname = ? "
					+ "\n ORDER BY tim DESC ",// 032

			"\n SELECT COUNT(*) FROM " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.audit.views")
					+ "\n WHERE  tschema = ? " + "\n AND      tname = ? ",// 033

			"\n UPDATE " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.audit.views") + "\n"
					+ "\n SET    abv = ? " + "\n WHERE  bschema = ? "
					+ "\n AND    bname   = ? ",// 034

			"\n UPDATE " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.audit.descriptions")
					+ "\n SET    name = ? " + "\n WHERE  name = ? ",// 035

			"\n UPDATE " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.audit")
					+ "\n SET    auditid = ? " + "\n WHERE  auditid = ? ",// 036

			"\n UPDATE " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.audit.purposes")
					+ "\n SET    auditid = ? " + "\n WHERE  auditid = ? ",// 037

			"\n UPDATE " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.audit.accessors")
					+ "\n SET    auditid = ? " + "\n WHERE  auditid = ? ",// 038

			"\n UPDATE " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.audit.recipients")
					+ "\n SET    auditid = ? " + "\n WHERE  auditid = ? ",// 039

			"\n UPDATE " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.audit.projection_columns")
					+ "\n SET    auditid = ? " + "\n WHERE  auditid = ? ",// 040

			"\n UPDATE " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.policy.descriptions")
					+ "\n SET    name = ? " + "\n WHERE  name = ? ",// 041

			"\n UPDATE " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.policy")
					+ "\n SET    policyid = ? " + "\n WHERE  policyid = ? ",// 042

			"\n UPDATE " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.policy.scope")
					+ "\n SET    policyid = ? " + "\n WHERE  policyid = ? ",// 043

			"\n UPDATE " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.audit.descriptions")
					+ "\n SET    version = ? " + "\n WHERE  version = ? "
					+ "\n AND       name = ? ",// 044

			"\n UPDATE " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.audit")
					+ "\n SET    version = ? " + "\n WHERE  version = ? "
					+ "\n AND    auditid = ? ",// 045

			"\n UPDATE " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.policy.descriptions")
					+ "\n SET    version = ? " + "\n WHERE  version = ? "
					+ "\n AND       name = ? ",// 046

			"\n UPDATE " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.policy")
					+ "\n SET    version = ? " + "\n WHERE  version = ? "
					+ "\n AND   policyid = ? ",// 047

			"\n UPDATE " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.policy.scope")
					+ "\n SET    version = ? " + "\n WHERE  version = ? "
					+ "\n AND   policyid = ? ",// 048

			"\n UPDATE " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.policy.descriptions")
					+ "\n SET    ENABLED = 0 " + "\n WHERE  NAME = ? ",// 049

			"\n UPDATE " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.policy.descriptions")
					+ "\n SET    ENABLED = 1 " + "\n WHERE  NAME = ? "
					+ "\n AND VERSION = ? ",// 050

			"\n UPDATE " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.policy.scope")
					+ "\n SET    version = ? " + "\n WHERE policyid = ? ",// 051

			"\n INSERT INTO " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.entities")
					+ "\n (schema, table, column_name) " + "\n VALUES (?,?,?) ",// 052

			"\n INSERT INTO " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.audit.descriptions")
					+ "\n (name, version) " + "\n VALUES (?,?) ",// 053

			"\n INSERT INTO " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.policy.descriptions")
					+ "\n (name, version, TYPE, ENABLED) "
					+ "\n VALUES (?,?,?,?) ",// 054

			"\n INSERT INTO " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.policy.scope")
					+ "\n (policyid, schema, table, column, VERSION) "
					+ "\n VALUES (?,?,?,?,?) ",// 055

			"\n INSERT INTO " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.policy.application_usage")
					+ "\n (appid, recipient, purpose, accessor)"
					+ "\n VALUES (?,?,?,?) ",// 056

			"\n INSERT INTO " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.audit")
					+ "\n (auditid, taskid, version, begin, end, condition) "
					+ "\n VALUES (?,?,?,?,?,?) ",// 057

			"\n INSERT INTO "
					+ getResource("metadata.schema")
					+ "."
					+ getResource("metadata.table.policy")
					+ "\n ( policyid, ruleid, version, purpose, accessor, recipient, schema, table, column, pseudonym, condition)"
					+ "\n VALUES(?,?,?,?,?,?,?,?,?,?,?)",// 058

			"\n DELETE FROM " + getResource("metadata.schema") + "."
					+ getResource("metadata.table.policy.application_usage")
					+ "\n WHERE APPID = ? ",// 59

			"\n SELECT 1 FROM "
					+ getResource("metadata.schema")
					+ "."
					+ getResource("metadata.table.policy")
					+ "\n WHERE policyid = ? AND schema = ? AND table = ? AND column = ? ",// 060
			"\n INSERT INTO "
					+ getResource("metadata.schema")
					+ "."
					+ getResource("metadata.table.audit.dropped_columns")
					+ "\n (bschema, bname, bcolumn, datatype, typename, colsize, scale, pos,isnullable, tim)"
					+ "\n VALUES(?,?,?,?,?,?,?,?,?,CURRENT TIMESTAMP)",// 061

			"\n SELECT bcolumn, datatype, typename, colsize, scale, pos, isnullable FROM " + getResource("metadata.schema")
					+ "." + getResource("metadata.table.audit.dropped_columns")
					+ "\n WHERE bschema = ? " + "\n AND     bname = ? "
					+ "\n ORDER BY pos, bcolumn ",// 062

			"\n SELECT datatype, typename, colsize, scale, pos, isnullable FROM " + getResource("metadata.schema")
					+ "." + getResource("metadata.table.audit.dropped_columns")
					+ "\n WHERE bschema = ? " + "\n AND     bname = ? "
					+ "\n AND bcolumn = ?"// 063

	};

	private void setStringParameter(final PreparedStatement ps,
			final int index, final String value) throws SQLException {
		logger.debug("param[" + index + "]=\"" + value + "\"");
		ps.setString(index, toSQL(value));
	}

	private void setStringParameterNotToSQL(final PreparedStatement ps,
			final int index, final String value) throws SQLException {
		logger.debug("param[" + index + "]=\"" + value + "\"");
		ps.setString(index, value);
	}

	private void setIntParameter(final PreparedStatement ps, final int index,
			final int value) throws SQLException {
		logger.debug("param[" + index + "]=\"" + value + "\"");
		ps.setInt(index, value);
	}
}
