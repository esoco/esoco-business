//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-business' project.
// Copyright 2017 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
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

import de.esoco.lib.expression.Predicate;
import de.esoco.lib.manage.TransactionException;
import de.esoco.storage.StorageException;
import org.obrel.core.ProvidesConfiguration;
import org.obrel.core.ProvidesSettings;
import org.obrel.core.Relatable;
import org.obrel.core.RelationType;
import org.obrel.type.StandardTypes;

import static de.esoco.lib.expression.Predicates.equalTo;
import static org.obrel.core.RelationTypes.newType;

/**
 * The entity definition for a simple configuration record. The record itself
 * only consists of the entity ID, a name and an optional owner entity. The
 * actual configuration values will be stored as the extra attributes of a
 * configuration instance. The format and contents of these extra attributes is
 * controlled by the application using the configuration.
 *
 * <p>If the optional owner entity is not set (i.e. is NULL) the configuration
 * name must be globally unique because it will be used to query the
 * configuration entity. If an owner is set the configuration is valid only for
 * that user and it's name must be unique for that user.</p>
 *
 * @author eso
 */
public class Configuration extends Entity
	implements ProvidesConfiguration, ProvidesSettings {

	/**
	 * The name of configurations that store entity-related
	 * settings/preferences.
	 */
	public static final String SETTINGS_CONFIG_NAME = "Settings";

	/**
	 * The name of the configuration record
	 */
	public static final RelationType<String> NAME = StandardTypes.NAME;

	/**
	 * The optional owner of an configuration entity.
	 */
	public static final RelationType<Entity> OWNER = newType();

	/**
	 * A reference to another configuration entity containing default values.
	 */
	public static final RelationType<Configuration> DEFAULTS = newType();

	/**
	 * A predicate that matches a configurations that contains entity-related
	 * settings.
	 */
	public static final Predicate<Entity> IS_SETTINGS_CONFIG =
		NAME.is(equalTo(SETTINGS_CONFIG_NAME));

	/**
	 * The prefix for global entity IDs
	 */
	public static final String ID_PREFIX = "CFG";

	//- entity definition constants

	private static final long serialVersionUID = 1L;

	/**
	 * Copies the value of a certain settings value of a source entity to a
	 * relatable target object.
	 *
	 * @param owner            The owner of the settings
	 * @param settingExtraAttr The settings extra attribute
	 * @param target           The target object
	 * @throws StorageException If retrieving the setting fails
	 */
	public static <T> void copySetting(Entity owner,
		RelationType<T> settingExtraAttr, Relatable target)
		throws StorageException {
		T value = getSettingsValue(owner, settingExtraAttr, null);

		if (value != null) {
			target.set(settingExtraAttr, value);
		}
	}

	/**
	 * Returns the settings configuration object for a certain owner entity.
	 * This will return (and create if necessary and instructed) a
	 * configurations entity that is associated with a certain entity and has
	 * the name {@link #SETTINGS_CONFIG_NAME}. Only the name is specific about
	 * this entity, in every other aspect it's a normal configuration entity.
	 *
	 * @param owner  The owner of the settings
	 * @param create TRUE to create the settings object if it doesn't exist
	 * @return The settings configuration object or NULL if none exists and
	 * create is FALSE
	 * @throws StorageException     If querying the configuration fails
	 * @throws TransactionException If storing a new configuration entity fails
	 * @see #getSettingsValue(RelationType, Object)
	 */
	public static Configuration getSettings(Entity owner, boolean create)
		throws StorageException, TransactionException {
		Configuration config = EntityManager.queryEntity(Configuration.class,
			IS_SETTINGS_CONFIG.and(OWNER.is(equalTo(owner))), true);

		if (config == null && create) {
			config = new Configuration();

			config.set(NAME, SETTINGS_CONFIG_NAME);
			config.set(OWNER, owner);

			EntityManager.storeEntity(config, owner);
		}

		return config;
	}

	/**
	 * A convenience method to query a certain settings value from a user's
	 * settings object. First invokes {@link #getSettings(Entity, boolean)}
	 * without creating the settings and then returns the settings value
	 * returned by {@link #getSettingsValue(RelationType, Object)} or, if no
	 * settings exist, the default value.
	 *
	 * @param owner            The owner of the settings
	 * @param settingExtraAttr The settings extra attribute
	 * @param defaultValue     The default value to return if no setting exist
	 * @return The settings value or the default value if none exists
	 * @throws StorageException If reading the extra attribute fails
	 */
	public static <T> T getSettingsValue(Entity owner,
		RelationType<T> settingExtraAttr, T defaultValue)
		throws StorageException {
		Configuration settings = null;
		T settingsValue = defaultValue;

		try {
			settings = getSettings(owner, false);
		} catch (TransactionException e) {
			// cannot happen if the boolean argument of getSettings() is FALSE
		}

		if (settings != null) {
			settingsValue =
				settings.getSettingsValue(settingExtraAttr, defaultValue);
		}

		return settingsValue;
	}

	/**
	 * Convenience method to check the existence and state of boolean settings.
	 *
	 * @see #getSettingsValue(RelationType, Object)
	 */
	public static boolean hasSettingsFlag(Entity owner,
		RelationType<Boolean> settingsExtraAttr) throws StorageException {
		return getSettingsValue(owner, settingsExtraAttr, Boolean.FALSE) ==
			Boolean.TRUE;
	}

	/**
	 * Returns a configuration value from the extra attributes of this
	 * instance.
	 * If no value exists but the attribute {@link #DEFAULTS} references
	 * another
	 * configuration entity this method will try to read the value recursively
	 * from the defaults configuration(s).
	 *
	 * @see ProvidesConfiguration#getConfigValue(RelationType, Object)
	 */
	@Override
	public <T> T getConfigValue(RelationType<T> type, T defaultValue) {
		T value = getXA(type, defaultValue);

		if (value == defaultValue && !hasXA(type)) {
			Configuration defaults = get(DEFAULTS);

			if (defaults != null) {
				value = defaults.getConfigValue(type, defaultValue);
			}
		}

		return value;
	}

	/**
	 * Returns a certain settings value. Instead of directly querying settings
	 * with the corresponding extra attribute users of this class should invoke
	 * this method so that it can perform additional hierarchical or default
	 * lookups. Like {@link #getConfigValue(RelationType, Object)} this method
	 * recursively looks up non-existing settings from any parent configuration
	 * set in the attribute {@link #DEFAULTS}.
	 *
	 * @param settingExtraAttr The settings extra attribute
	 * @param defaultValue     The default value to return if no setting exist
	 * @return The settings value or the default value if none exists
	 * @see #getSettings(Entity, boolean)
	 * @see #setSettingsValue(RelationType, Object)
	 */
	@Override
	public <T> T getSettingsValue(RelationType<T> settingExtraAttr,
		T defaultValue) {
		return getConfigValue(settingExtraAttr, defaultValue);
	}

	/**
	 * Sets a configuration value as an extra attribute of this instance.
	 *
	 * @see ProvidesConfiguration#setConfigValue(RelationType, Object)
	 */
	@Override
	public <T> void setConfigValue(RelationType<T> type, T value) {
		setXA(type, value);
	}

	/**
	 * Sets a certain settings value. Instead of directly updating settings
	 * with
	 * the corresponding extra attribute users of this class should invoke this
	 * method so that it can perform additional hierarchical or default
	 * lookups.
	 *
	 * @param settingExtraAttr The settings extra attribute
	 * @param value            The new settings value
	 * @see #getSettings(Entity, boolean)
	 * @see #getSettingsValue(RelationType, Object)
	 */
	@Override
	public <T> void setSettingsValue(RelationType<T> settingExtraAttr,
		T value) {
		setConfigValue(settingExtraAttr, value);
	}
}
