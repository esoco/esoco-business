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
import org.obrel.core.RelationType;

import java.util.Collection;

/**
 * A base class for fragments that edit the user settings that can be obtained
 * through the method {@link #getUserSettings(boolean)}.
 *
 * @author eso
 */
public abstract class SettingsFragment extends InteractionFragment {

	private static final long serialVersionUID = 1L;

	/**
	 * Applies the edited values of certain settings by transferring them from
	 * the corresponding process parameters to the user's settings and
	 * optionally storing the settings. If no settings object exists for the
	 * user it will be created.
	 *
	 * @param user         The user to apply the settings to
	 * @param settingTypes The settings relation types
	 * @param store        TRUE to store the user settings after applying
	 * @throws StorageException     If updating a settings extra attribute
	 *                              fails
	 * @throws TransactionException If creating or storing the user settings
	 *                              fails
	 */
	public void applySettings(Entity user,
		Collection<RelationType<?>> settingTypes, boolean store)
		throws StorageException, TransactionException {
		Configuration settings = Configuration.getSettings(user, true);

		for (RelationType<?> preference : settingTypes) {
			setPreferenceFromParameter(settings, preference);
		}

		if (store) {
			EntityManager.storeEntity(settings, user);
		}
	}

	/**
	 * Collects certain settings by transferring them from the user's settings
	 * to the corresponding process parameters.
	 *
	 * @param user         The user to collect the settings from
	 * @param settingTypes The settings relation types
	 * @throws StorageException If accessing a settings extra attribute fails
	 */
	public void collectSettings(Entity user,
		Collection<RelationType<?>> settingTypes) throws StorageException {
		Configuration settings;

		try {
			settings = Configuration.getSettings(user, false);

			if (settings != null) {
				for (RelationType<?> preference : settingTypes) {
					setParameterFromPreference(settings, preference);
				}
			}
		} catch (TransactionException e) {
			// cannot happen if settings are queried with create = FALSE
			throw new AssertionError(e);
		}
	}

	/**
	 * Type-safe method to set a preferences parameter from a settings
	 * configuration if it exists.
	 *
	 * @param settings   The settings (can be NULL if no settings are
	 *                   available)
	 * @param preference The preference extra attribute
	 * @throws StorageException If reading the preference value fails
	 */
	protected <T> void setParameterFromPreference(Configuration settings,
		RelationType<T> preference) throws StorageException {
		if (settings != null) {
			setParameter(preference,
				settings.getSettingsValue(preference, null));
		}
	}

	/**
	 * Type-safe method to set a preferences parameter from a settings
	 * configuration.
	 *
	 * @param settings   The settings
	 * @param preference The preference extra attribute
	 * @throws StorageException If reading the preference value fails
	 */
	protected <T> void setPreferenceFromParameter(Configuration settings,
		RelationType<T> preference) throws StorageException {
		settings.setSettingsValue(preference, getParameter(preference));
	}
}
