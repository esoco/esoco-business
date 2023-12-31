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
package de.esoco.process.step.entity;

import de.esoco.entity.Entity;
import de.esoco.entity.EntityManager;

import de.esoco.history.HistoryRecord;

import de.esoco.lib.expression.Action;

import de.esoco.process.ProcessRelationTypes;

import org.obrel.core.RelationType;
import org.obrel.type.MetaTypes;
import org.obrel.type.StandardTypes;

import static de.esoco.process.ProcessRelationTypes.ENTITY_PARAM;
import static de.esoco.process.ProcessRelationTypes.ENTITY_UPDATE_ACTION;
import static de.esoco.process.ProcessRelationTypes.HISTORY_TARGET_PARAM;
import static de.esoco.process.ProcessRelationTypes.PROCESS_USER;

/**
 * A step that stores an entity that is stored in the process parameter
 * referenced by {@link ProcessRelationTypes#ENTITY_PARAM}. If no history target
 * is set internally in the step relation
 * {@link ProcessRelationTypes#HISTORY_TARGET_PARAM} it will be set to the
 * entity parameter.
 *
 * <p>If the process of this step contains an update {@link Action} in the
 * parameter {@link ProcessRelationTypes#ENTITY_UPDATE_ACTION} the action will
 * be invoked on the entity before storing it. Afterwards the action parameter
 * will be set to NULL.</p>
 *
 * @author eso
 */
public class StoreEntity extends EntityStep {

	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new instance.
	 */
	public StoreEntity() {
		set(MetaTypes.TRANSACTIONAL);
	}

	/**
	 * @see EntityStep#execute()
	 */
	@Override
	protected void execute() throws Exception {
		RelationType<? extends Entity> rEntityParam =
			checkParameter(ENTITY_PARAM);

		Entity rEntity = checkParameter(rEntityParam);

		if (rEntity != null) {
			Action<Entity> fUpdate = getParameter(ENTITY_UPDATE_ACTION);

			if (fUpdate != null) {
				fUpdate.evaluate(rEntity);
				setParameter(ENTITY_UPDATE_ACTION, null);
			}

			EntityManager.storeEntity(rEntity, getParameter(PROCESS_USER));
		}
	}

	/**
	 * @see EntityStep#prepareParameters()
	 */
	@Override
	protected void prepareParameters() throws Exception {
		if (get(HISTORY_TARGET_PARAM) == null) {
			set(HISTORY_TARGET_PARAM, checkParameter(ENTITY_PARAM));
		}

		if (get(HistoryRecord.VALUE) == null) {
			set(HistoryRecord.VALUE, getProcess().get(StandardTypes.NAME));
		}
	}
}
