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
package de.esoco.process.step;

import de.esoco.entity.Configuration;
import de.esoco.entity.Entity;
import de.esoco.entity.EntityManager;

import de.esoco.lib.manage.TransactionException;

import de.esoco.storage.StorageException;

import java.util.Collection;

import org.obrel.core.RelationType;


/********************************************************************
 * A base class for fragments that edit the user settings that can be obtained
 * through the method {@link #getUserSettings(boolean)}.
 *
 * @author eso
 */
public abstract class SettingsFragment extends InteractionFragment
{
	//~ Static fields/initializers ---------------------------------------------

	private static final long serialVersionUID = 1L;

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Applies the edited values of certain settings by transferring them from
	 * the corresponding process parameters to the user's settings and
	 * optionally storing the settings. If no settings object exists for the
	 * user it will be created.
	 *
	 * @param  rUser         The user to apply the settings to
	 * @param  rSettingTypes The settings relation types
	 * @param  bStore        TRUE to store the user settings after applying
	 *
	 * @throws StorageException     If updating a settings extra attribute fails
	 * @throws TransactionException If creating or storing the user settings
	 *                              fails
	 */
	public void applySettings(Entity					  rUser,
							  Collection<RelationType<?>> rSettingTypes,
							  boolean					  bStore)
		throws StorageException, TransactionException
	{
		Configuration rSettings = Configuration.getSettings(rUser, true);

		for (RelationType<?> rPreference : rSettingTypes)
		{
			setPreferenceFromParameter(rSettings, rPreference);
		}

		if (bStore)
		{
			EntityManager.storeEntity(rSettings, rUser);
		}
	}

	/***************************************
	 * Collects certain settings by transferring them from the user's settings
	 * to the corresponding process parameters.
	 *
	 * @param  rUser         The user to collect the settings from
	 * @param  rSettingTypes The settings relation types
	 *
	 * @throws StorageException If accessing a settings extra attribute fails
	 */
	public void collectSettings(
		Entity						rUser,
		Collection<RelationType<?>> rSettingTypes) throws StorageException
	{
		Configuration rSettings;

		try
		{
			rSettings = Configuration.getSettings(rUser, false);

			if (rSettings != null)
			{
				for (RelationType<?> rPreference : rSettingTypes)
				{
					setParameterFromPreference(rSettings, rPreference);
				}
			}
		}
		catch (TransactionException e)
		{
			// cannot happen if settings are queried with create = FALSE
			throw new AssertionError(e);
		}
	}

	/***************************************
	 * Type-safe method to set a preferences parameter from a settings
	 * configuration if it exists.
	 *
	 * @param  rSettings   The settings (can be NULL if no settings are
	 *                     available)
	 * @param  rPreference The preference extra attribute
	 *
	 * @throws StorageException If reading the preference value fails
	 */
	protected <T> void setParameterFromPreference(
		Configuration   rSettings,
		RelationType<T> rPreference) throws StorageException
	{
		if (rSettings != null)
		{
			setParameter(rPreference,
						 rSettings.getSettingsValue(rPreference, null));
		}
	}

	/***************************************
	 * Type-safe method to set a preferences parameter from a settings
	 * configuration.
	 *
	 * @param  rSettings   The settings
	 * @param  rPreference The preference extra attribute
	 *
	 * @throws StorageException If reading the preference value fails
	 */
	protected <T> void setPreferenceFromParameter(
		Configuration   rSettings,
		RelationType<T> rPreference) throws StorageException
	{
		rSettings.setSettingsValue(rPreference, getParameter(rPreference));
	}
}
