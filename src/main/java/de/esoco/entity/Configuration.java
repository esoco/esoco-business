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
import org.obrel.core.Relatable;
import org.obrel.core.RelationType;
import org.obrel.type.StandardTypes;

import static de.esoco.lib.expression.Predicates.equalTo;

import static org.obrel.core.RelationTypes.newType;


/********************************************************************
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
public class Configuration extends Entity implements ProvidesConfiguration
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	/**
	 * The name of configurations that store entity-related
	 * settings/preferences.
	 */
	public static final String SETTINGS_CONFIG_NAME = "Settings";

	/** The name of the configuration record */
	public static final RelationType<String> NAME = StandardTypes.NAME;

	/** The optional owner of an configuration entity. */
	public static final RelationType<Entity> OWNER = newType();

	/**
	 * A predicate that matches a configurations that contains entity-related
	 * settings.
	 */
	public static final Predicate<Entity> IS_SETTINGS_CONFIG =
		NAME.is(equalTo(SETTINGS_CONFIG_NAME));

	//- entity definition constants --------------------------------------------

	/** The prefix for global entity IDs */
	public static final String ID_PREFIX = "CFG";

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Copies the value of a certain settings value of a source entity to a
	 * relatable target object.
	 *
	 * @param  rOwner            The owner of the settings
	 * @param  rSettingExtraAttr The settings extra attribute
	 * @param  rTarget           The target object
	 *
	 * @throws StorageException If retrieving the setting fails
	 */
	public static <T> void copySetting(Entity		   rOwner,
									   RelationType<T> rSettingExtraAttr,
									   Relatable	   rTarget)
		throws StorageException
	{
		T rValue = getSettingsValue(rOwner, rSettingExtraAttr, null);

		if (rValue != null)
		{
			rTarget.set(rSettingExtraAttr, rValue);
		}
	}

	/***************************************
	 * Returns the settings configuration object for a certain owner entity.
	 * This will return (and create if necessary and instructed) a
	 * configurations entity that is associated with a certain entity and has
	 * the name {@link #SETTINGS_CONFIG_NAME}. Only the name is specific about
	 * this entity, in every other aspect it's a normal configuration entity.
	 *
	 * @param  rOwner  The owner of the settings
	 * @param  bCreate TRUE to create the settings object if it doesn't exist
	 *
	 * @return The settings configuration object or NULL if none exists and
	 *         bCreate is FALSE
	 *
	 * @throws StorageException     If querying the configuration fails
	 * @throws TransactionException If storing a new configuration entity fails
	 *
	 * @see    #getSettingsValue(RelationType, Object)
	 */
	public static Configuration getSettings(Entity rOwner, boolean bCreate)
		throws StorageException, TransactionException
	{
		Configuration aConfig =
			EntityManager.queryEntity(Configuration.class,
									  IS_SETTINGS_CONFIG.and(OWNER.is(equalTo(rOwner))),

									  true);

		if (aConfig == null && bCreate)
		{
			aConfig = new Configuration();

			aConfig.set(NAME, SETTINGS_CONFIG_NAME);
			aConfig.set(OWNER, rOwner);

			EntityManager.storeEntity(aConfig, rOwner);
		}

		return aConfig;
	}

	/***************************************
	 * A convenience method to query a certain settings value from a user's
	 * settings object. First invokes {@link #getSettings(Entity, boolean)}
	 * without creating the settings and then returns the settings value
	 * returned by {@link #getSettingsValue(RelationType, Object)} or, if no
	 * settings exist, the default value.
	 *
	 * @param  rOwner            The owner of the settings
	 * @param  rSettingExtraAttr The settings extra attribute
	 * @param  rDefaultValue     The default value to return if no setting exist
	 *
	 * @return The settings value or the default value if none exists
	 *
	 * @throws StorageException If reading the extra attribute fails
	 */
	public static <T> T getSettingsValue(Entity			 rOwner,
										 RelationType<T> rSettingExtraAttr,
										 T				 rDefaultValue)
		throws StorageException
	{
		Configuration rSettings		 = null;
		T			  rSettingsValue = rDefaultValue;

		try
		{
			rSettings = getSettings(rOwner, false);
		}
		catch (TransactionException e)
		{
			// cannot happen if the boolean argument of getSettings() is FALSE
		}

		if (rSettings != null)
		{
			rSettingsValue =
				rSettings.getSettingsValue(rSettingExtraAttr, rDefaultValue);
		}

		return rSettingsValue;
	}

	/***************************************
	 * Convenience method to check the existence and state of boolean settings.
	 *
	 * @see #getSettingsValue(RelationType, Object)
	 */
	public static boolean hasSettingsFlag(
		Entity				  rOwner,
		RelationType<Boolean> rSettingsExtraAttr) throws StorageException
	{
		return getSettingsValue(rOwner, rSettingsExtraAttr, Boolean.FALSE) ==
			   Boolean.TRUE;
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Returns a configuration value from the extra attributes of this instance.
	 *
	 * @see ProvidesConfiguration#getConfigValue(RelationType, Object)
	 */
	@Override
	public <T> T getConfigValue(RelationType<T> rType, T rDefaultValue)
	{
		return getXA(rType, rDefaultValue);
	}

	/***************************************
	 * Returns a certain settings value. Instead of directly querying settings
	 * with the corresponding extra attribute users of this class should invoke
	 * this method so that it can perform additional hierarchical or default
	 * lookups.
	 *
	 * @param  rSettingExtraAttr The settings extra attribute
	 * @param  rDefaultValue     The default value to return if no setting exist
	 *
	 * @return The settings value or the default value if none exists
	 *
	 * @throws StorageException If reading the extra attribute fails
	 *
	 * @see    #getSettings(Entity, boolean)
	 * @see    #setSettingsValue(RelationType, Object)
	 */
	public <T> T getSettingsValue(
		RelationType<T> rSettingExtraAttr,
		T				rDefaultValue) throws StorageException
	{
		return getExtraAttribute(rSettingExtraAttr, rDefaultValue);
	}

	/***************************************
	 * Sets a configuration value as an extra attribute of this instance.
	 *
	 * @see ProvidesConfiguration#setConfigValue(RelationType, Object)
	 */
	@Override
	public <T> void setConfigValue(RelationType<T> rType, T rValue)
	{
		setXA(rType, rValue);
	}

	/***************************************
	 * Sets a certain settings value. Instead of directly updating settings with
	 * the corresponding extra attribute users of this class should invoke this
	 * method so that it can perform additional hierarchical or default lookups.
	 *
	 * @param  rSettingExtraAttr The settings extra attribute
	 * @param  rValue            The new settings value
	 *
	 * @throws StorageException If accessing the settings extra attribute fails
	 *
	 * @see    #getSettings(Entity, boolean)
	 * @see    #getSettingsValue(RelationType, Object)
	 */
	public <T> void setSettingsValue(
		RelationType<T> rSettingExtraAttr,
		T				rValue) throws StorageException
	{
		setExtraAttribute(rSettingExtraAttr, rValue);
	}
}
