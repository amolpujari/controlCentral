package controlCenter;

import java.sql.Timestamp;
import java.util.Iterator;
import java.util.TreeSet;

class Task extends Rule implements Cloneable {
	public Timestamp begin = new Timestamp(System.currentTimeMillis());

	public Timestamp end = new Timestamp(System.currentTimeMillis());

	public Object clone() throws CloneNotSupportedException {
		final Task other = (Task) super.clone();
		other.begin = (Timestamp) begin.clone();
		other.end = (Timestamp) end.clone();

		return other;
	}

	public String toString() {
		final StringBuffer str = new StringBuffer();
		
		str.append(databaseName);
		str.append("_");
		str.append(policyName);
		str.append("_");
		str.append(name);
		
		return str.toString();
//		String text = "";
//		text += policyName + ", ";
//		text += version + ", ";
//		text += name + ", ";
//		text += columns + ", ";
//		text += purposes + ", ";
//		text += accessors + ", ";
//		text += recipients + ", ";
//		text += condition + ", ";
//		text += entities + ", ";
//		text += optInChoices + ", ";
//		text += optOutChoices + ", ";
//		text += begin + ", ";
//		text += end;
//		return text;
	}

	public String getSQLAuditQuery() {
		String sql = "";
		final TreeSet tables = new TreeSet();

		String sqlSelectClause = "SELECT ";
		String sqlFromClause = "FROM   ";
		String sqlWhereClause = "WHERE  ";

		//
		// SELECT
		//
		for (int i = 0; i < columns.size(); i++) {
			final ColumnDescriptor column = (ColumnDescriptor) columns.get(i);
			if (i > 0)
				sqlSelectClause += ", ";

			try {
				sqlSelectClause += column.clone(true);
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
			// Removes duplicates automatically.
			tables.add(column.table);
		}

		//
		// FROM
		//
		final Iterator iterator = tables.iterator();
		for (int i = 0; iterator.hasNext(); i++) {
			final TableDescriptor table = (TableDescriptor) iterator.next();
			if (i > 0)
				sqlFromClause += ", ";
			sqlFromClause += table;
		}

		//
		// WHERE
		//
		sqlWhereClause += "\n";

		if (!condition.equals("''"))
			sqlWhereClause += "(";

		if (condition.length() < 4)
			sqlWhereClause += "1 = 1";
		else
			sqlWhereClause += condition;

		if (!condition.equals("''"))
			sqlWhereClause += ")";

		sql += sqlSelectClause + "\n";
		sql += sqlFromClause + "\n";
		sql += sqlWhereClause;

		return sql;
	}

	public String getDisplayableEntry(String entry) {

		String result = entry;
		String value = "";

		value = entry.replaceAll("%", "");
		int index = entry.indexOf("%");

		if (index > 0)
			result = " STARTS WITH ";
		else {
			if (entry.length() == 1) {
				result = " SELECT ALL";
				value = "";
			} else {
				index = entry.substring(1).lastIndexOf("%");

				if (index == -1)
					result = " ENDS WITH ";
				else
					result = " CONTAINS ";
			}

		}

		return result + value;
	}

	public String getColumnsString() {
		StringBuffer str = new StringBuffer();

		for (int i = 0; i < columns.size(); i++)
			str.append(((ColumnDescriptor) columns.get(i))
					.getDisplayable(false));

		return str.toString();
	}
}
