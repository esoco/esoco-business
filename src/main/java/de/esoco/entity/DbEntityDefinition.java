//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-business' project.
// Copyright 2015 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//	  http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
package de.esoco.entity;

import de.esoco.lib.logging.Log;
import org.obrel.core.RelationType;
import org.obrel.core.RelationTypes;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static de.esoco.storage.impl.jdbc.JdbcRelationTypes.SQL_NAME;
import static java.sql.Types.BIGINT;
import static java.sql.Types.BIT;
import static java.sql.Types.BOOLEAN;
import static java.sql.Types.CHAR;
import static java.sql.Types.DATE;
import static java.sql.Types.DECIMAL;
import static java.sql.Types.DOUBLE;
import static java.sql.Types.FLOAT;
import static java.sql.Types.INTEGER;
import static java.sql.Types.NUMERIC;
import static java.sql.Types.SMALLINT;
import static java.sql.Types.TIMESTAMP;
import static java.sql.Types.TINYINT;
import static java.sql.Types.VARCHAR;
import static org.obrel.type.MetaTypes.AUTOGENERATED;
import static org.obrel.type.MetaTypes.OBJECT_ID_ATTRIBUTE;

/**
 * Entity definition that reads the column names of a given database table and
 * sets these as attributes.
 *
 * @author thomas
 */
@SuppressWarnings({ "boxing" })
public class DbEntityDefinition extends EntityDefinition<Entity> {

	private static final long serialVersionUID = 1L;

	private static final Map<Integer, Class<?>> sqlDatatypeMap;

	private static String defaultJdbcUrl;

	private static Properties defaultConnectionProperties;

	static {
		sqlDatatypeMap = new HashMap<Integer, Class<?>>();

		sqlDatatypeMap.put(BIGINT, Long.class);
		sqlDatatypeMap.put(INTEGER, Integer.class);
		sqlDatatypeMap.put(SMALLINT, Short.class);
		sqlDatatypeMap.put(TINYINT, Byte.class);
		sqlDatatypeMap.put(DECIMAL, BigDecimal.class);
		sqlDatatypeMap.put(NUMERIC, BigDecimal.class);
		sqlDatatypeMap.put(FLOAT, Float.class);
		sqlDatatypeMap.put(DOUBLE, Double.class);
		sqlDatatypeMap.put(CHAR, String.class);
		sqlDatatypeMap.put(VARCHAR, String.class);
		sqlDatatypeMap.put(BOOLEAN, Boolean.class);
		sqlDatatypeMap.put(BIT, Boolean.class);

		sqlDatatypeMap.put(DATE, Date.class);
		sqlDatatypeMap.put(TIMESTAMP, Timestamp.class);
	}

	/**
	 * Creates a new instance from a table in a particular database.
	 *
	 * @param entityName       The entity name
	 * @param idPrefix         The prefix for global entity IDs
	 * @param tableName        connection The database connection
	 * @param jdbcUrl          The database access URL; may be NULL for the
	 *                         default URL
	 * @param autogeneratedKey TRUE to mark primary key fields as automatically
	 *                         generated
	 */
	DbEntityDefinition(String entityName, String idPrefix, String tableName,
		String jdbcUrl, boolean autogeneratedKey) {
		init(entityName, idPrefix, Entity.class,
			readAttributes(jdbcUrl, tableName, autogeneratedKey));

		set(SQL_NAME, tableName);
	}

	/**
	 * Internal method to close a closeable object.
	 *
	 * @param closeable The object to be closed
	 */
	private static void closeObject(Object closeable) {
		if (closeable != null) {
			try {
				if (closeable instanceof ResultSet) {
					((ResultSet) closeable).close();
				} else if (closeable instanceof Connection) {
					((Connection) closeable).close();
				}
			} catch (SQLException e) {
				Log.warn("Closing failed: " + closeable, e);
			}
		}
	}

	/**
	 * Creates a relation type for a certain database column.
	 *
	 * @param column   The name of the database column
	 * @param table    The name of the database table
	 * @param typeCode The SQL datatype of the database column
	 * @return A new relation type instance
	 */
	private static RelationType<?> createRelationType(String column,
		String table, int typeCode) {
		@SuppressWarnings("unchecked")
		Class<Object> datatype = (Class<Object>) sqlDatatypeMap.get(typeCode);

		if (datatype == null) {
			throw new IllegalArgumentException(
				"No datatype mapping for " + column);
		}

		return RelationTypes.newRelationType(
			table.toLowerCase() + "." + column.toUpperCase(), datatype);
	}

	/**
	 * Internal method to open a database connection for the argument JDBC URL.
	 *
	 * @param jdbcUrl The JDBC URL; may be NULL for the default URL
	 * @return The new database connection
	 * @throws IllegalArgumentException If the connection could not be opened
	 */
	private static Connection openConnection(String jdbcUrl) {
		String url = (jdbcUrl != null ? jdbcUrl : defaultJdbcUrl);

		Connection connection;

		try {
			if (defaultConnectionProperties != null) {
				connection = DriverManager.getConnection(url,
					defaultConnectionProperties);
			} else {
				connection = DriverManager.getConnection(url);
			}
		} catch (SQLException e) {
			throw new IllegalArgumentException(
				"Could not open connection: " + url);
		}

		return connection;
	}

	/**
	 * Creates the entity attributes from from the database metadata.
	 *
	 * @param jdbcUrl          The JDBC URL for the database access
	 * @param tableName        The name of the table to read the metadata for
	 * @param autogeneratedKey TRUE to indicate that the primary key is
	 *                         automatically generated by the database
	 * @return A new list containing the attribute relation types
	 */
	private static List<RelationType<?>> readAttributes(String jdbcUrl,
		String tableName, boolean autogeneratedKey) {
		List<RelationType<?>> attributes = new ArrayList<RelationType<?>>();
		Set<String> primaryKeys = new HashSet<String>();
		Connection connection = openConnection(jdbcUrl);
		ResultSet resultSet = null;

		try {
			resultSet =
				connection.getMetaData().getPrimaryKeys(null, null, tableName);

			while (resultSet.next()) {
				primaryKeys.add(resultSet.getString("COLUMN_NAME"));
			}

			closeObject(resultSet);

			resultSet = connection
				.createStatement()
				.executeQuery("SELECT * FROM " + tableName);

			ResultSetMetaData metaData = resultSet.getMetaData();

			for (int i = 1; i <= metaData.getColumnCount(); i++) {
				String column = metaData.getColumnLabel(i);
				int datatype = metaData.getColumnType(i);
				RelationType<?> attribute =
					createRelationType(column, tableName, datatype);

				if (primaryKeys.contains(column)) {
					attribute.set(OBJECT_ID_ATTRIBUTE, true);

					if (autogeneratedKey) {
						attribute.set(AUTOGENERATED, true);
					}
				}

				attributes.add(attribute);
			}
		} catch (SQLException e) {
			throw new IllegalArgumentException(
				"Could not read metadata for " + tableName);
		} finally {
			closeObject(resultSet);
			closeObject(connection);
		}

		return attributes;
	}

	/**
	 * Sets the global connection properties that will be used to create new
	 * entity definitions.
	 *
	 * @param properties The new global connection properties
	 */
	public static final void setDefaultConnectionProperties(
		Properties properties) {
		defaultConnectionProperties = new Properties();

		defaultConnectionProperties.putAll(properties);
	}

	/**
	 * Sets the default JDBC URL for the creation of database entity
	 * definitions.
	 *
	 * @param jdbcUrl The new default JDBC URL
	 */
	public static final void setDefaultJdbcUrl(String jdbcUrl) {
		defaultJdbcUrl = jdbcUrl;
	}
}
