import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import org.apache.log4j.Logger;

/**
 * This made to handle multiple db
 * connections, and their status as well
 * 
 */
public class DBCon {

	private String userId;

	private String password;

	private String dbName;

	private String url;

	private Connection con = null;

	private Properties properties;

	private final static Vector driversLoaded = new Vector(4, 2);

	private final static Hashtable dbCons = new Hashtable();

	private final static Hashtable preparedStatements = new Hashtable();

	private final Logger logger = Logger.getLogger(getClass().getName());

	private static String[] sqls;

	/**
	 * 
	 */
	public DBCon() {
	}

	/**
	 * @param user
	 * @param psswd
	 * @param dbName
	 * @param driver
	 * @param url
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public DBCon(String user, String psswd, String dbName, String driver,
			String url) throws ClassNotFoundException, SQLException, Exception {
		dbName = dbName.toUpperCase();

		if (dbCons.containsKey(dbName))
			throw new Exception("Already connected to " + dbName);

		userId = user;
		password = psswd;
		this.dbName = dbName;
		this.url = url;

		logger.debug("Initiating db connection for " + dbName);

		load(driver);
		setProperties();
		connect();
	}

	/**
	 * @param user
	 * @param psswd
	 * @param dbName
	 * @param driver
	 * @param url
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public DBCon(String user, String psswd, String dbName, String driver,
			String url, final String[] preparedStatements)
			throws ClassNotFoundException, SQLException, Exception {
		this(user, psswd, dbName, driver, url);

		sqls = preparedStatements;

		final PreparedStatement[] ps = new PreparedStatement[preparedStatements.length];

		for (int i = 0; i < ps.length; i++)
			ps[i] = con.prepareStatement(preparedStatements[i]);

		DBCon.preparedStatements.put(dbName, ps);
	}

	/**
	 * @throws ClassNotFoundException
	 */
	private void load(String driver) throws ClassNotFoundException {
		if (!driversLoaded.contains(driver)) {
			Class.forName(driver);
			driversLoaded.add(driver);
			logger.info("driver loaded: " + driver);
		}
	}

	/**
	 * @param driver
	 * @throws ClassNotFoundException
	 */
	public void loadAnotherDriver(String driver) throws ClassNotFoundException {
		if (!driversLoaded.contains(driver)) {
			Class.forName(driver);
			driversLoaded.add(driver);
			logger.info("driver loaded: " + driver);
		}
	}

	/**
	 * 
	 */
	private void setProperties() {
		properties = new Properties();
		properties.put("user", userId);
		properties.put("password", password);
	}

	/**
	 * @param arg
	 * @throws SQLException
	 */
	public void execute(String dbName, String arg) throws SQLException {
		Connection con = ((DBCon) dbCons.get(dbName)).con;
		Statement stmt = null;

		try {
			logger.debug("executing (" + dbName + ")SQL: " + arg);
			stmt = con.createStatement();
			stmt.execute(arg);

			// if(!uncommittedDb.contains(dbName))
			// uncommittedDb.add(dbName);
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
		}
	}

	/**
	 * @param arg
	 * @return
	 * @throws SQLException
	 */
	public ResultSet query(String dbName, String arg) throws SQLException {
		Connection con = ((DBCon) dbCons.get(dbName)).con;
		ResultSet rs = null;
		Statement stmt = null;

		try {
			logger.debug("executing (" + dbName + ")SQL: " + arg);
			stmt = con.createStatement();
			rs = stmt.executeQuery(arg);
		} catch (SQLException e) {
			if (rs != null)
				try {
					rs.close();
				} catch (Exception e1) {
					e1.printStackTrace();
				}

			if (stmt != null)
				try {
					stmt.close();
				} catch (Exception e1) {
					e1.printStackTrace();
				}

			throw e;
		}
		return rs;
	}

	/**
	 * @return
	 * @throws SQLException
	 */
	public void connect() throws SQLException {
		con = DriverManager.getConnection(url, properties);
		con.setAutoCommit(false);
		dbCons.put(dbName, this);
		logger.info("connected to: " + dbName);
	}

	/**
	 * @return
	 */
	public void reset(String dbName) throws SQLException {
		((DBCon) dbCons.get(dbName)).con.commit();
		((DBCon) dbCons.get(dbName)).con.close();

		dbCons.remove(dbName);

		// if(uncommittedDb.contains(dbName))
		// uncommittedDb.remove(dbName);

		logger.info("disconnected from: " + dbName);
	}

	/**
	 * @param dbName
	 * @return
	 * @throws SQLException
	 */
	public DatabaseMetaData getMetaData(String dbName) throws SQLException {
		return ((DBCon) dbCons.get(dbName)).con.getMetaData();
	}

	/**
	 * @param dbName
	 * @return
	 */
	public Connection getConnection(String dbName) {
		return ((DBCon) dbCons.get(dbName)).con;
	}

	/**
	 * @param dbName
	 * @throws SQLException
	 */
	public void commit(String dbName) {
		// if(!uncommittedDb.contains(dbName))
		// return;

		try {
			((DBCon) dbCons.get(dbName)).con.commit();
			logger.debug("committed to: " + dbName);
		} catch (SQLException e) {
			logger.error("commit failed to: " + dbName, e);
		}

	}

	/**
	 * @param dbName
	 * @throws SQLException
	 */
	public void rollback(String dbName) {
		// if(!uncommittedDb.contains(dbName))
		// return;

		try {
			((DBCon) dbCons.get(dbName)).con.rollback();
			logger.info("rollbacked to: " + dbName);
		} catch (SQLException e) {
			logger.error("rollback failed to: " + dbName, e);
		}
	}

	/**
	 * @param dbName
	 * @return
	 */
	public boolean isConnected(String dbName) {
		if (dbCons != null)
			return dbCons.contains(dbName);
		else
			return false;
	}

	/**
	 * @param dbName
	 * @return
	 */
	public DBCon getDBCon(String dbName) {
		if (dbCons.contains(dbName))
			return (DBCon) dbCons.get(dbName);
		else
			return null;
	}

	/**
	 * @return
	 */
	public Enumeration getDBNames() {
		if (dbCons != null)
			return dbCons.keys();
		else
			return null;
	}

	public PreparedStatement getPreparedStatement(String dbName, int count)
			throws SQLException {
		logger.debug("PreparedSQL[" + dbName + "]:" + sqls[count]);
		return ((PreparedStatement[]) DBCon.preparedStatements.get(dbName))[count];
	}

}